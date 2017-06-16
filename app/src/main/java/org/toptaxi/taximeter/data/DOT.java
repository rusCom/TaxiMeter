package org.toptaxi.taximeter.data;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.tools.Constants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DOT {
    protected static String TAG = "#########" + DOT.class.getName();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String AppToken, HostIP, getDataPort, sendDataPort;
    OkHttpClient httpClient;

    public DOT() {
        AppToken = MainApplication.getInstance().getPackageName();
        HostIP = MainApplication.getInstance().getResources().getString(R.string.mainIP);
        getDataPort = MainApplication.getInstance().getResources().getString(R.string.loginPort);
        sendDataPort = MainApplication.getInstance().getResources().getString(R.string.loginPort);
        httpClient = new OkHttpClient();
    }

    void setGetDataPort(String getDataPort) {
        this.getDataPort = getDataPort;
    }

    void setSendDataPort(String sendDataPort) {
        this.sendDataPort = sendDataPort;
    }

    public String getDataType(String Type, String Data){
        String URL = getGetDataRest() + "gettype=" + AppToken + "&token=" + MainApplication.getInstance().getMainAccount().getToken();
        URL += "&location=" + MainApplication.getInstance().getLocationData();
        try {
            if (Type != null)
                if (!Type.equals(""))
                    URL += "&type=" + URLEncoder.encode(Type, "UTF-8");

            if (Data != null)
                if (!Data.equals(""))
                    URL += "&data=" + URLEncoder.encode(Data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return httpGet(URL);
    }

    public int sendDataResponse(String type, String data){
        int result = Constants.DOT_HTTP_ERROR;
        try {
            String URL = getSendDataRest() + "send=" + AppToken + "&token=" + MainApplication.getInstance().getMainAccount().getToken();
            URL += "&location=" + MainApplication.getInstance().getLocationData();
            if (type != null)
                if (!type.equals(""))
                    URL += "&type=" + URLEncoder.encode(type, "UTF-8");
            if (data != null)
                if (!data.equals(""))
                    URL += "&data=" + URLEncoder.encode(data, "UTF-8");

            JSONObject response = new JSONObject(httpGet(URL));
            if (response.getString("response").equals("ok")){
                result = Constants.DOT_REST_OK;
                MainApplication.getInstance().parseData(response);
            }
            if (response.getString("response").equals("rest_error"))result = Constants.DOT_REST_ERROR;
            if (response.getString("response").equals("identification_error"))result = Constants.DOT_IDENTIFICATION;


        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public int getData(){
        int result = Constants.DOT_HTTP_ERROR;
        try {
            String URL = getGetDataRest() + "get=" + AppToken + "&token=" + MainApplication.getInstance().getMainAccount().getToken();
            URL += "&location=" + MainApplication.getInstance().getLocationData();
            //Log.d(TAG, "getData URL = " + URL);
            JSONObject response = new JSONObject(httpGet(URL));
            //Log.d(TAG, "getData response = " + response.toString());
            if (response.getString("response").equals("ok")){
                result = Constants.DOT_REST_OK;
                MainApplication.getInstance().parseData(response);
            }
            if (response.getString("response").equals("rest_error"))result = Constants.DOT_REST_ERROR;
            if (response.getString("response").equals("identification_error"))result = Constants.DOT_IDENTIFICATION;
        }
        catch (JSONException e) {
            e.printStackTrace();
            result = Constants.DOT_REST_ERROR;
        }
        return result;
    }

    public int getPreferences(){
        int result = Constants.DOT_HTTP_ERROR;
        try {
            String rest = "http://" + MainApplication.getInstance().getString(R.string.mainIP) + ":" + MainApplication.getInstance().getString(R.string.loginPort) + "/";
            String URL = rest + "preferences=" + AppToken + "&token=" + MainApplication.getInstance().getMainAccount().getToken() + "&version=" + MainApplication.getInstance().getVersionCode();
            //Log.d(TAG, "getPreferences URL = " + URL.toString());
            JSONObject response = new JSONObject(httpGet(URL));
            //Log.d(TAG, "getPreferences response = " + response.toString());
            if (response.getString("response").equals("ok")){
                result = Constants.DOT_REST_OK;
                MainApplication.getInstance().parseData(response);
            }
            if (response.getString("response").equals("rest_error"))result = Constants.DOT_REST_ERROR;
            if (response.getString("response").equals("identification_error"))result = Constants.DOT_IDENTIFICATION;
            if (response.getString("response").equals("new_version"))result = Constants.DOT_NEW_VERSION;
            if (response.getString("response").equals("blocked"))result = Constants.DOT_BLOCKED;
            HostIP = MainApplication.getInstance().getString(R.string.mainIP);
        } catch (JSONException e) {
            e.printStackTrace();
            result = Constants.DOT_REST_ERROR;
        }

        //Log.d(TAG, "getPreferences result = " + result);

        // Если основной IP не пашет
        if (result == Constants.DOT_HTTP_ERROR){
            //Log.d(TAG, "getPreferences try reserveIP");
            try {
                String rest = "http://" + MainApplication.getInstance().getString(R.string.reserveIP) + ":" + MainApplication.getInstance().getString(R.string.loginPort) + "/";
                String URL = rest + "preferences=" + AppToken + "&token=" + MainApplication.getInstance().getMainAccount().getToken() + "&version=" + MainApplication.getInstance().getVersionCode();
                //Log.d(TAG, "getPreferences URL = " + URL.toString());
                JSONObject response = new JSONObject(httpGet(URL));
                //Log.d(TAG, "getPreferences response = " + response.toString());
                if (response.getString("response").equals("ok")){
                    result = Constants.DOT_REST_OK;
                    MainApplication.getInstance().parseData(response);
                }
                if (response.getString("response").equals("rest_error"))result = Constants.DOT_REST_ERROR;
                if (response.getString("response").equals("identification_error"))result = Constants.DOT_IDENTIFICATION;
                if (response.getString("response").equals("new_version"))result = Constants.DOT_NEW_VERSION;
                HostIP = MainApplication.getInstance().getString(R.string.reserveIP);
            } catch (JSONException e) {
                e.printStackTrace();
                result = Constants.DOT_REST_ERROR;
            }


        }
        return result;
    }

    public int getPassword(String phone){
        int result = Constants.DOT_HTTP_ERROR;
        try {
            String URL = getLoginDataRest() + "getpass=" + AppToken + "&phone=" + URLEncoder.encode(phone, "UTF-8");
            Log.d(TAG, "getPassword URL = " + URL);
            JSONObject response = new JSONObject(httpGet(URL));
            Log.d(TAG, "getPassword response = " + response);
            if (response.getString("response").equals("ok"))result = Constants.DOT_REST_OK;
            if (response.getString("response").equals("rest_error"))result = Constants.DOT_REST_ERROR;
            if (response.getString("response").equals("phone_wrong"))result = Constants.DOT_PHONE_WRONG;
            if (response.getString("response").equals("driver_wrong"))result = Constants.DOT_DRIVER_WRONG;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            result = Constants.DOT_REST_ERROR;
        }
        return result;
    }

    public int getToken(String phone, String password){
        int result = Constants.DOT_HTTP_ERROR;
        try {
            String URL = getLoginDataRest() + "gettoken=" + AppToken + "&phone=" + URLEncoder.encode(phone, "UTF-8") + "&psw=" + URLEncoder.encode(password, "UTF-8");
            JSONObject response = new JSONObject(httpGet(URL));
            if (response.getString("response").equals("rest_error"))result = Constants.DOT_REST_ERROR;
            else if (response.getString("response").equals("phone_wrong"))result = Constants.DOT_PHONE_WRONG;
            else if (response.getString("response").equals("password_wrong"))result = Constants.DOT_PASSWORD_WRONG;
            else {
                result = Constants.DOT_REST_OK;
                MainApplication.getInstance().getMainAccount().setToken(response.getString("response"));
            }
            Log.d(TAG, "getToken response = " + response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            result = Constants.DOT_REST_ERROR;
        }

        return result;
    }

    public void getDataParseTask(String Type){
        String URL = getGetDataRest() + "gettype=" + AppToken + "&token=" + MainApplication.getInstance().getMainAccount().getToken() + "&type=" + Type;
        //Log.d(TAG, "getDataParseTask URl = " + URL);
        GetDataParseTypeTask sendDataTask = new GetDataParseTypeTask();
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
            sendDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL);
        else
            sendDataTask.execute(URL);
    }

    private void getDataTask(){
        String URL = getGetDataRest() + "get=" + AppToken + "&token=" + MainApplication.getInstance().getMainAccount().getToken();
        URL += "&location=" + MainApplication.getInstance().getLocationData();
        new GetDataParseTypeTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL);
    }

    private static class GetDataParseTypeTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return httpGet(params[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                MainApplication.getInstance().parseData(new JSONObject(s));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendData(String type, String data){
        try {
            String URL = getSendDataRest() + "send=" + AppToken + "&token=" + MainApplication.getInstance().getMainAccount().getToken();
            URL += "&location=" + MainApplication.getInstance().getLocationData();
            if (type != null)
                if (!type.equals(""))
                    URL += "&type=" + URLEncoder.encode(type, "UTF-8");
            if (data != null)
                if (!data.equals(""))
                    URL += "&data=" + URLEncoder.encode(data, "UTF-8");
            new SendDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }



    private static class SendDataTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            httpGet(params[0]);
            return null;
        }
    }

    public void sendDataResult(String type, String data){
        try {
            String URL = getSendDataRest() + "send=" + AppToken + "&token=" + MainApplication.getInstance().getMainAccount().getToken();
            URL += "&location=" + MainApplication.getInstance().getLocationData();
            if (type != null)
                if (!type.equals(""))
                    URL += "&type=" + URLEncoder.encode(type, "UTF-8");
            if (data != null)
                if (!data.equals(""))
                    URL += "&data=" + URLEncoder.encode(data, "UTF-8");
            new SendDataResultTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    private static class SendDataResultTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        SendDataResultTask() {
            if (MainApplication.getInstance().getMainActivity() != null){
                progressDialog = new ProgressDialog(MainApplication.getInstance().getMainActivity());
                progressDialog.setMessage(MainApplication.getInstance().getResources().getString(R.string.dlgSendData));
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressDialog != null)
                progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return httpGet(params[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (progressDialog != null && progressDialog.isShowing())progressDialog.dismiss();
            try {
                JSONObject data = new JSONObject(s);
                if (data.getString("response").equals("ok"))MainApplication.getInstance().getDot().getDataTask();
                else MainApplication.getInstance().showToast(data.getString("response"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public static String httpGet(String url)  {
        String result = "{\"response\":\"httpError\", \"status\":\"error\",\"value\":\"" + MainApplication.getInstance().getResources().getString(R.string.errorConnection) + "\"}";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            result = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response != null)response.body().close();
        return result;
    }

    public DOTResponse httpGetDOT(String url){
        DOTResponse result = new DOTResponse(400);
        if (url.equals(""))return result;
        Response response = null;
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            response = httpClient.newCall(request).execute();
            //Log.d(TAG, "httpGet main_ur success");
        } catch (IOException e) {
            e.printStackTrace();
            //Log.d(TAG, "httpGet main_ur unsuccessful");
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

    public DOTResponse httpPostDOT(String url, String body){
        DOTResponse result = new DOTResponse(400);
        if (url.equals(""))return result;
        Response response = null;
        RequestBody requestBody = RequestBody.create(JSON, body);
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            response = httpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
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


    private String getSendDataRest(){
        return "http://" + HostIP + ":" + sendDataPort + "/";
    }

    private String getGetDataRest(){
        return "http://" + HostIP + ":" + getDataPort + "/";
    }

    private String getLoginDataRest(){
        return "http://" + MainApplication.getInstance().getString(R.string.mainIP) + ":" + MainApplication.getInstance().getString(R.string.loginPort) + "/";
    }
}
