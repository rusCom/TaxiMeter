package org.toptaxi.taximeter.data;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainActivity;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.activities.MessagesActivity;
import org.toptaxi.taximeter.activities.NewOrderActivity;
import org.toptaxi.taximeter.tools.DBHelper;

import java.util.Iterator;
import java.util.TreeSet;

public class Messages {
    protected static String TAG = "#########" + Messages.class.getName();
    private SQLiteDatabase dataBase;
    private TreeSet<Message> messages;
    private OnMessagesListener onMessagesListener;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public interface OnMessagesListener{
        void OnNewMessage();
    }

    public Messages() {
        DBHelper dbHelper = new DBHelper(MainApplication.getInstance());
        dataBase = dbHelper.getWritableDatabase();
        messages = new TreeSet<>(new MessageComp());
    }

    public void OnNewMessages(JSONArray jsonMessages) throws JSONException {
        for (int itemID = 0; itemID < jsonMessages.length(); itemID++){
            JSONObject jsonMessage = jsonMessages.getJSONObject(itemID);
            final Message message = new Message(jsonMessage);
            if ((message.Type.equals("disp")) || (message.Type.equals("info"))){
                messages.add(message);

            }
            MainApplication.getInstance().getDot().sendData("MessageDeliver", String.valueOf(message.ID));
            //Log.d(TAG, "OnNewMessages " + message.Text);
            if (isNewMessage(message)){
                MainApplication.getInstance().getDot().sendData("MessageDeliver", String.valueOf(message.ID));
                Log.d(TAG, "new message OnNewMessages " + message.Text);
                if (message.Type.equals("new_order")){
                    JSONArray dataArray = new JSONArray(message.Text);
                    Order newOrder = new Order(dataArray.getJSONObject(0));
                    MainApplication.getInstance().setNewOrder(newOrder);
                    Intent dialogIntent = new Intent(MainApplication.getInstance(), NewOrderActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainApplication.getInstance().startActivity(dialogIntent);
                } // if (message.Type.equals("new_order")){
                else {
                    if (MainApplication.getInstance().getMainActivity() != null) {
                        if (onMessagesListener != null){
                            MediaPlayer mp = MediaPlayer.create(MainApplication.getInstance().getMainActivity(), R.raw.incomming_message_frg);
                            mp.setLooping(false);
                            mp.start();
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onMessagesListener.OnNewMessage();
                                }
                            });
                        }
                        else {
                            MainApplication.getInstance().getMainActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    MediaPlayer mp = MediaPlayer.create(MainApplication.getInstance().getMainActivity(), R.raw.incomming_message);
                                    mp.setLooping(false);
                                    mp.start();
                                    AlertDialog.Builder adb = new AlertDialog.Builder(MainApplication.getInstance().getMainActivity());
                                    adb.setMessage(message.Text);
                                    adb.setIcon(android.R.drawable.ic_dialog_info);
                                    adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            setRead(message.ID);
                                        }
                                    });
                                    if (message.Type.equals("disp")){
                                        adb.setTitle("Сообщение от диспетчера");
                                        adb.setNegativeButton("Ответить", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                setRead(message.ID);
                                                Intent messagesIntent = new Intent(MainApplication.getInstance().getMainActivity(), MessagesActivity.class);
                                                MainApplication.getInstance().getMainActivity().startActivity(messagesIntent);
                                            }
                                        });
                                    }

                                    adb.create();
                                    adb.show();
                                }
                            });
                        }
                    }
                    else {
                        sendNotification(message.Text, String.valueOf(message.ID));
                    }
                }
            }
        }
    }

    public void setOnMessagesListener(OnMessagesListener onMessagesListener) {
        this.onMessagesListener = onMessagesListener;
    }

    private void sendNotification(String messageBody, String ID) {
        Intent intent = new Intent(MainApplication.getInstance(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainApplication.getInstance(), 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MainApplication.getInstance())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(MainApplication.getInstance().getResources().getString(R.string.app_name))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        ((NotificationManager) MainApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE)).notify(Integer.parseInt(ID), notificationBuilder.build());
    }


    private boolean isNewMessage(Message message){
        boolean result = false;
        Cursor cursor = dataBase.rawQuery("select * from messages where id = ? ", new String[]{String.valueOf(message.ID)});
        if (cursor.getCount() == 0){
            ContentValues cv = new ContentValues();
            cv.put("id", message.ID);
            dataBase.insert("messages", null, cv);
            result = true;
        }
        return result;
    }

    public void setFromJSON(JSONArray data) throws JSONException {
        for (int itemID = 0; itemID < data.length(); itemID++){
            Message message = new Message(data.getJSONObject(itemID));
            messages.add(message);
        }
    }

    public Message getItem(int position){
        return (Message) (messages.toArray())[position];
    }

    public int getCount(){
        return messages.size();
    }

    public void setRead(int MessageID){
        MainApplication.getInstance().getDot().sendData("MessageRead", String.valueOf(MessageID));

    }

    public int getLastID(){
        int result = 0;
        Iterator<Message> iterator = messages.iterator();
        if (iterator.hasNext()){
            result = iterator.next().ID;
            while (iterator.hasNext()){
                int messageID = iterator.next().ID;
                if (messageID < result)
                    result = messageID;
            }
        }
        return result;
    }

    public int LoadMore() throws JSONException {
        int resultCount = 0;
        JSONObject data = new JSONObject(MainApplication.getInstance().getDot().getDataType("his_messages", String.valueOf(getLastID())));
        if (data.has("hisMessages")){
            setFromJSON(data.getJSONArray("hisMessages"));
            resultCount = data.getJSONArray("hisMessages").length();
        }
        return resultCount;
    }

    public int LoadNew() throws JSONException {
        int resultCount = 0;
        JSONObject data = new JSONObject(MainApplication.getInstance().getDot().getDataType("his_messages", ""));
        if (data.has("hisMessages")){
            setFromJSON(data.getJSONArray("hisMessages"));
            resultCount = data.getJSONArray("hisMessages").length();
        }
        return resultCount;
    }


}
