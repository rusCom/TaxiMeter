package org.toptaxi.taximeter.services;


import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.toptaxi.taximeter.MainApplication;

public class MainFirebaseMessagingService extends FirebaseMessagingService {
    protected static String TAG = "#########" + MainFirebaseMessagingService.class.getName();
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        //Log.d(TAG, "onMessageReceived " + remoteMessage.getData().get("Destenation"));
        //Log.d(TAG, "onMessageReceived " + FirebaseInstanceId.getInstance().getToken());
        if (remoteMessage.getData().get("Destenation").equals(FirebaseInstanceId.getInstance().getToken())){
            MainApplication.getInstance().getDot().getDataParseTask("messages");
        }
    }
}
