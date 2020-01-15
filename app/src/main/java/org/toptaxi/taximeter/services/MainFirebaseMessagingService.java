package org.toptaxi.taximeter.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.toptaxi.taximeter.MainApplication;

public class MainFirebaseMessagingService extends FirebaseMessagingService {
    private static String TAG = "#########" + MainFirebaseMessagingService.class.getName();

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0){
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

    }

    @Override
    public void onNewToken(@NonNull String token) {
        MainApplication.getInstance().getFirebaseService().setPushToken(token);
        // MainApplication.getInstance().getDotService().sendRegistrationToServer(token);
    }

}
