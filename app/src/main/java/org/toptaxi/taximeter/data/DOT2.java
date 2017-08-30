package org.toptaxi.taximeter.data;

import org.toptaxi.taximeter.MainApplication;


import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DOT2 {
    protected static String TAG = "#########" + DOT2.class.getName();
    private OkHttpClient httpClient;
    private String mainAddress, reserveAddress, restPort;

    public DOT2() {
        httpClient = new OkHttpClient();
        mainAddress = "192.168.1.32"; //MainApplication.getInstance().getResources().getString(R.string.mainIP);
        reserveAddress = "192.168.1.34"; //MainApplication.getInstance().getResources().getString(R.string.reserveIP);
    }

    void setRestPort(String restPort) {
        this.restPort = restPort;
    }

    private void replaceAddresses(){
        String lAddress = mainAddress;
        mainAddress = reserveAddress;
        reserveAddress = lAddress;
    }

    public DOTResponse httpGet(String method, String params){
        DOTResponse result = new DOTResponse(504);

        String param = "token=" + MainApplication.getInstance().getMainAccount().getToken() + "&location=" + MainApplication.getInstance().getLocationData();
        if (!params.equals(""))param += "&" + params;


        String main_url = "http://" + mainAddress + ":" + restPort + "/" + method + "?" + param;
        String reserve_url = "http://" + reserveAddress + ":" + restPort + "/" + method + "?" + param;

        //Log.d(TAG, "httpGet main_url = " + main_url + ";reserve_url = " + reserve_url);


        //OkHttpClient client = new OkHttpClient();
        Response response = null;
        try {
            Request request = new Request.Builder()
                    .url(main_url)
                    .build();
            response = httpClient.newCall(request).execute();
            //Log.d(TAG, "httpGet main_ur success");
        } catch (IOException e) {
            e.printStackTrace();
            //Log.d(TAG, "httpGet main_ur unsuccessful");
        }

        if (response == null){
            Request request = new Request.Builder()
                    .url(reserve_url)
                    .build();
            try {
                response = httpClient.newCall(request).execute();
                replaceAddresses();
                //Log.d(TAG, "httpGet reserve success");
            } catch (IOException e) {
                e.printStackTrace();
                //Log.d(TAG, "httpGet reserve unsuccessful");
            }
        }

        if (response != null){
            String responseBody = "";
            try {
                responseBody = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            result.Set(response.code(), responseBody);

        }

        return result;
    }
}
