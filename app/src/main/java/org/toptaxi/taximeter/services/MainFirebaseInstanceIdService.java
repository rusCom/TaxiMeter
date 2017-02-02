package org.toptaxi.taximeter.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import org.toptaxi.taximeter.MainApplication;

public class MainFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        MainApplication.getInstance().getDot().sendData("GCMToken", FirebaseInstanceId.getInstance().getToken());
    }
}
