package org.toptaxi.taximeter.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.toptaxi.taximeter.MainApplication;

import java.util.Objects;

public class FirebaseService {
    private static String TAG = "#########" + FirebaseService.class.getName();
    private SharedPreferences sharedPreferences;

    public FirebaseService() {
        // C:\Projects\DevTools\AndroidStudio\jre
        sharedPreferences = MainApplication.getInstance().getSharedPreferences("firebase", Context.MODE_PRIVATE);
        String pushToken = sharedPreferences.getString("pushToken", "");
        if (pushToken.equals("")){
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            String token = Objects.requireNonNull(task.getResult()).getToken();
                            setPushToken(token);
                        }
                    });
        }
    } // public FirebaseService()

    void setPushToken(String token){
        MainApplication.getInstance().getRestService().httpGetThread("/profile/push?push_token=" + token);
        FirebaseMessaging.getInstance().subscribeToTopic("allDevices");
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString( "pushToken", "token" );
        sharedPreferencesEditor.apply();
    }

    public void CheckTopics(JSONArray topics){
        Log.d(TAG, "topics = " + topics.toString());
        String oldTopicsString = sharedPreferences.getString("pushTopics", "[]");
        try {
            JSONArray oldTopics = new JSONArray(oldTopicsString);
            if (oldTopics.equals(topics)){return;}
            Log.d(TAG, "equals topics = " + oldTopics.equals(topics));
            Log.d(TAG, "oldTopics = " + oldTopics.toString());
            for (int itemID = 0; itemID < oldTopics.length(); itemID++){
                if (!MainApplication.isJSONArrayHaveValue(topics, oldTopics.getString(itemID))){
                    Log.d(TAG, "unsubscribeFromTopic " + oldTopics.getString(itemID));
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(oldTopics.getString(itemID));
                }
            }
            for (int itemID = 0; itemID < topics.length(); itemID++){
                if (!MainApplication.isJSONArrayHaveValue(oldTopics, topics.getString(itemID))){
                    Log.d(TAG, "subscribeToTopic " + topics.getString(itemID));
                    FirebaseMessaging.getInstance().subscribeToTopic(topics.getString(itemID));
                }
            }

            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putString( "pushTopics", topics.toString() );
            sharedPreferencesEditor.apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



}
