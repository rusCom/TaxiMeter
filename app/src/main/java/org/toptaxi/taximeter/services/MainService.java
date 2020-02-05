package org.toptaxi.taximeter.services;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainActivity;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.DOT;
import org.toptaxi.taximeter.tools.MainUtils;

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
        if (m_powerManager == null) {
            m_powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        }

        if (m_wakeLock == null) {
            m_wakeLock = m_powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyApp::MyWakelockTag");
            m_wakeLock.acquire();
        }
        if (MainApplication.getInstance().getGoogleApiClient() == null) {
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

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "aTaxi.Taximetr";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setContentIntent(contentIntent)
                .setOngoing(true)   //Can't be swiped out
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(MainApplication.getInstance().getResources().getString(R.string.app_name)) //Заголовок
                .setContentText(Text) // Текст уведомления
                .setWhen(System.currentTimeMillis())
                .setOnlyAlertOnce(true);

        Notification notification;
        notification = notificationBuilder.build();
        startForeground(DEFAULT_NOTIFICATION_ID, notification);


        /*



        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(contentIntent)
                .setOngoing(true)   //Can't be swiped out
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(MainApplication.getInstance().getResources().getString(R.string.app_name)) //Заголовок
                .setContentText(Text) // Текст уведомления
                .setWhen(System.currentTimeMillis());

        Notification notification;
        notification = builder.build();
        startForeground(DEFAULT_NOTIFICATION_ID, notification);
        */

        /*

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {//startMyOwnForeground();
             }
        else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentIntent(contentIntent)
                    .setOngoing(true)   //Can't be swiped out
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(MainApplication.getInstance().getResources().getString(R.string.app_name)) //Заголовок
                    .setContentText(Text) // Текст уведомления
                    .setWhen(System.currentTimeMillis());

            Notification notification;
            notification = builder.build();
            startForeground(DEFAULT_NOTIFICATION_ID, notification);

        }
        */


    }

    @Override
    public void onDestroy() {
        isRunning = false;
        MainApplication.getInstance().getMainAccount().setNullStatus();
        MainApplication.getInstance().getDot().sendData("driver_off_line", "");
        if (m_wakeLock != null) {
            m_wakeLock.release();
            m_wakeLock = null;
        }
        Log.d(TAG, "OnDestroy service");
    }

    void getDataTask() {
        new Thread(new Runnable() {
            public void run() {

                Integer calcTimer = 0,
                        placesTimer = MainApplication.getInstance().getMainPreferences().getPlacesTimeOut();
                Double lastLongitude = 0.0, lastLatitude = 0.0;

                while (isRunning) {

                    MainApplication.getInstance().getDot().getData();
                    String notificationMessage = MainApplication.getInstance().getMainAccount().getBalance() + " " + MainApplication.getInstance().getMainAccount().getStatusName() + " " + MainApplication.getInstance().getMainAccount().getName();
                    sendNotification(notificationMessage);


                    placesTimer += MainApplication.getInstance().getMainPreferences().getDataTimer();
                    if (placesTimer >= MainApplication.getInstance().getMainPreferences().getPlacesTimeOut()) {
                        placesTimer = 0;
                        try {
                            if (MainApplication.getInstance().getMainLocation() != null){
                                if (!lastLatitude.equals(MainUtils.round(MainApplication.getInstance().getMainLocation().getLatitude(), 6))
                                        & !lastLongitude.equals(MainUtils.round(MainApplication.getInstance().getMainLocation().getLongitude(), 6))
                                ) {
                                    lastLatitude = MainUtils.round(MainApplication.getInstance().getMainLocation().getLatitude(), 6);
                                    lastLongitude = MainUtils.round(MainApplication.getInstance().getMainLocation().getLongitude(), 6);
                                    // MainApplication.getInstance().getPlacesAPI().getCurPlace();
                                    String url = "http://api.toptaxi.org/geo/geocode?lt=" + lastLatitude + "&ln=" + lastLongitude;
                                    // Log.d(TAG, "CurLocationName url = " + url);
                                    JSONObject data = MainApplication.getInstance().getRestService().httpGetAny(url);
                                    JSONObject result = data.getJSONObject("result");
                                    MainApplication.getInstance().setCurLocationName(result.getString("name") + " (" + result.getString("dsc") + ")");
                                    // Log.d(TAG, "CurLocationName = " + result.toString());
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
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
