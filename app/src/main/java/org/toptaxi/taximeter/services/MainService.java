package org.toptaxi.taximeter.services;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainActivity;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.DOT;

import java.util.concurrent.TimeUnit;

public class MainService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    protected static String TAG = "#########" + MainService.class.getName();
    private boolean isRunning = false;
    private static final int DEFAULT_NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;
    private GoogleApiClient mGoogleApiClient;

    PowerManager m_powerManager = null;
    PowerManager.WakeLock m_wakeLock = null;


    public MainService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        //Log.d(TAG, MainApplication.getInstance().getPackageName());
        isRunning = true;
        sendNotification("");
        getDataTask();
        if( m_powerManager == null )
        {
            m_powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        }

        if( m_wakeLock == null )
        {
            m_wakeLock = m_powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Keep background services running");
            m_wakeLock.acquire();
        }
        if (MainApplication.getInstance().getGoogleApiClient() == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
        return START_STICKY;
    }

    //Send custom notification
    public void sendNotification(String Text) {

        //These three lines makes Notification to open main activity after clicking on it
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(contentIntent)
                .setOngoing(true)   //Can't be swiped out
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(MainApplication.getInstance().getResources().getString(R.string.app_name)) //Заголовок
                .setContentText(Text) // Текст уведомления
                .setWhen(System.currentTimeMillis());

        Notification notification;
        if (android.os.Build.VERSION.SDK_INT<=15) {
            notification = builder.getNotification(); // API-15 and lower
        }else{
            notification = builder.build();
        }

        startForeground(DEFAULT_NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        MainApplication.getInstance().getMainAccount().setNullStatus();
        MainApplication.getInstance().getDot().sendData("driver_off_line", "");
        if( m_wakeLock != null )
        {
            m_wakeLock.release();
            m_wakeLock = null;
        }
        Log.d(TAG, "OnDestroy service");
    }

    void getDataTask(){
        new Thread(new Runnable() {
            public void run() {

                Integer calcTimer = 0,
                        placesTimer = MainApplication.getInstance().getMainPreferences().getPlacesTimeOut();

                while (isRunning){

                    MainApplication.getInstance().getDot().getData();
                    String notificationMessage = MainApplication.getInstance().getMainAccount().getBalance() + " " + MainApplication.getInstance().getMainAccount().getStatusName() + " " + MainApplication.getInstance().getMainAccount().getName();
                    sendNotification(notificationMessage);

                    calcTimer += MainApplication.getInstance().getMainPreferences().getDataTimer();
                    if (calcTimer >= 60){
                        calcTimer = 0;
                        calcBaseDistance();
                    }
                    placesTimer += MainApplication.getInstance().getMainPreferences().getDataTimer();
                    if (placesTimer >= MainApplication.getInstance().getMainPreferences().getPlacesTimeOut()){
                        placesTimer = 0;
                        MainApplication.getInstance().getPlacesAPI().getCurPlace();
                    }

                    try {
                        TimeUnit.SECONDS.sleep(MainApplication.getInstance().getMainPreferences().getDataTimer());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //Removing any notifications
                notificationManager.cancel(DEFAULT_NOTIFICATION_ID);
                MainApplication.getInstance().getMainAccount().setNullStatus();
                MainApplication.getInstance().getDot().sendData("driver_off_line", "");
                MainApplication.getInstance().onTerminate();
                stopSelf();
            }
        }).start();
    }

    public void calcBaseDistance(){

        try {
            //Log.d(TAG, "calcBaseDistance start");
            JSONObject calcData = new JSONObject(MainApplication.getInstance().getDot().getDataType("getDistanceForGoogleSearch", ""));
            //Log.d(TAG, "getDistanceForGoogleSearch calcData = " + calcData.toString());
            String httpRequest = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="+calcData.getString("bLatitude")+","+calcData.getString("bLongitude")+"&destinations="+calcData.getString("eLatitude")+","+calcData.getString("eLongitude");
            JSONObject response = new JSONObject(DOT.httpGet(httpRequest));
            //Log.d(TAG, "getDistanceForGoogleSearch response = " + response.toString());
            JSONObject location = response.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0);
            String distance = location.getJSONObject("distance").getString("value");
            httpRequest = calcData.getString("bLatitude")+"|"+calcData.getString("bLongitude")+"|"+calcData.getString("eLatitude")+"|"+calcData.getString("eLongitude")+"|"+distance+"|";
            //Log.d(TAG, "getDistanceForGoogleSearch httpRequest = " + httpRequest);
            MainApplication.getInstance().getDot().sendData("setDistanceForGoogleSearch", httpRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        MainApplication.getInstance().setGoogleApiClient(mGoogleApiClient);
        new Thread(new Runnable() {
            @Override
            public void run() {
                MainApplication.getInstance().getDot().getPreferences();
            }
        }).start();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
