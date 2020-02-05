package org.toptaxi.taximeter.services;

import android.location.Location;
import android.os.Build;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RestService {
    protected static String TAG = "###" + RestService.class.getName();
    private OkHttpClient httpClient;
    private JSONObject header;
    private ArrayList<String> restHost;
    private int restIndex;

    public RestService() {
        httpClient = new OkHttpClient();
        header = new JSONObject();
        reloadHeader();
        restHost = new ArrayList<>();
        restHost.add(MainApplication.getInstance().getResources().getString(R.string.mainRestHost));
        restHost.add(MainApplication.getInstance().getResources().getString(R.string.reserveRestHost));
        restIndex = 0;
    }

    public void reloadHeader(){
        header = new JSONObject();
        try {
            header.put("token", MainApplication.getInstance().getMainAccount().getToken());
            header.put("device_id", MainApplication.getInstance().getDeviceID());
            header.put("version", MainApplication.getInstance().getVersionCode());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getHeader(){
        Location location = MainApplication.getInstance().getMainLocation();
        if (location != null){
            try {
                header.put("lt", location.getLatitude());
                header.put("ln", location.getLongitude());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            return Base64.getEncoder().encodeToString(header.toString().getBytes());
        }
        else {
            return android.util.Base64.encodeToString(header.toString().getBytes(), android.util.Base64.DEFAULT);
        }
    }

    public void httpGetThread(final String path){
        new Thread(new Runnable() {
            @Override
            public void run() {
                httpGetHost(path);
            }
        }).start();
    }

    public JSONObject httpGet(String path){
        JSONObject response = httpGetHost(path);
        Log.d(TAG, "httpGet path = '" + path + "'; response = '" + (response != null ? response.toString() : null) + "'");
        return response;
    }



    private JSONObject httpGetHost(String path){
        String url = restHost.get(restIndex) + path;
        Response response = restCall(url);

        if (response == null){
            for (int item = 0; item < restHost.size(); item++){
                if ((item != restIndex) && (response == null)){
                    url = restHost.get(item) + path;
                   // Log.w(TAG, "httpGetHost try " + url);
                    response = restCall(url);
                    if (response != null){
                        restIndex = item;
                    }
                }
            }
        }

        if (response == null){return null;}
        if (response.code() != 200){return null;}
        try {
            return new JSONObject(Objects.requireNonNull(response.body()).string());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Response restCall(String url){
        Response response = null;
        try {

            Request request = new Request.Builder()
                    .url(url)
                    .header("authorization", "Bearer " + getHeader())
                    .build();
            response = httpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    JSONObject httpGetAny(String url){
        try {

            Request request = new Request.Builder()
                    .url(url)
                    .header("authorization", "Bearer " + getHeader())
                    .build();
            Response response = httpClient.newCall(request).execute();
            if (response.code() != 200){return null;}
            try {
                return new JSONObject(Objects.requireNonNull(response.body()).string());
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
