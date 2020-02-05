package org.toptaxi.taximeter.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.tools.Constants;
import org.toptaxi.taximeter.tools.LockOrientation;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class SplashActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    protected static String TAG = "#########" + SplashActivity.class.getName();
    AsyncTask curTask;
    TextView tvAction;
    boolean isGooglePlayConnect;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new LockOrientation(this).lock();
        setContentView(R.layout.activity_splash);

        tvAction  = findViewById(R.id.tvSplashAction);
        ((TextView)findViewById(R.id.tvSplashVersion)).setText(MainApplication.getInstance().getVersionName());

        ((NotificationManager) Objects.requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE))).cancelAll();

        if (!isNetworkAvailable(getApplicationContext())){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Для работы программы необходимо подключение к интернет. Проверьте подключение и попробуйте еще раз.");
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {finish();}
            });
            alertDialog.create();
            alertDialog.show();
        }
        else {
            init();
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        //Log.d(TAG, "isNetworkAvailable");
        int[] networkTypes = {ConnectivityManager.TYPE_MOBILE,
                ConnectivityManager.TYPE_WIFI};
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            for (int networkType : networkTypes) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null &&
                        activeNetworkInfo.getType() == networkType)
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (curTask != null){
            curTask.cancel(true);
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ACTIVITY_LOGIN){
            if (resultCode == RESULT_CANCELED){
                setResult(RESULT_CANCELED);
                finish();
            }
            else init();
        }

    }

    private void init(){
        Log.d(TAG, "init");
        // Есть ли учетные данные по клиенту
        if (MainApplication.getInstance().getMainAccount().getToken().equals("")){
            Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivityForResult(loginIntent, Constants.ACTIVITY_LOGIN);
        }
        else{
            // подключаемся к сервису playMarket
            if (!isGooglePlayConnect){
                //Log.d(TAG, "init connecting to play service");
                ((TextView)findViewById(R.id.tvSplashAction)).setText(getString(R.string.tvSplashActionPlayConnect));
                // Create an instance of GoogleAPIClient.
                if (mGoogleApiClient == null) {
                    mGoogleApiClient = new GoogleApiClient.Builder(this)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(LocationServices.API)
                            .build();
                    mGoogleApiClient.connect();
                }

            }
            else if (!((LocationManager)getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)){
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage(getString(R.string.gps_network_not_enabled));
                dialog.setPositiveButton("Открыть настройки", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                        Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                        finish();
                        //get gps
                    }
                });
                dialog.show();
            }
            // Если подключение к плей сервсиу уже есть
            else {
                // new GetPreferencesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                new ProfileAuthTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        isGooglePlayConnect = true;
        MainApplication.getInstance().setGoogleApiClient(mGoogleApiClient);
        init();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.getErrorCode() == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED){ // 2
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Необходимо обновить Сервисы Google Play");
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=com.google.android.gms"));
                    startActivity(intent);
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            alertDialog.create();
            alertDialog.show();
        }
        else if (connectionResult.getErrorCode() == ConnectionResult.SERVICE_MISSING){ // 1
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Для корректной работы приложения необходимо установить Сервисы Google Play");
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=com.google.android.gms"));
                    startActivity(intent);
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            alertDialog.create();
            alertDialog.show();
        }
        else if (connectionResult.getErrorCode() == ConnectionResult.SERVICE_INVALID){ // 9
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Для корректной работы приложения необходимо установить Google Play Market");
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Uri address = Uri.parse("https://www.google.com/search?q=%D0%BA%D0%B0%D0%BA+%D1%83%D1%81%D1%82%D0%B0%D0%BD%D0%BE%D0%B2%D0%B8%D1%82%D1%8C+play+market");
                    Intent openLinkIntent = new Intent(Intent.ACTION_VIEW, address);
                    startActivity(openLinkIntent);

                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            alertDialog.create();
            alertDialog.show();
        }
        else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Нет подключения к сервису Google Play (" + connectionResult.getErrorCode() + ". Для корректной работы приложения проверьте наличие Сервисы Google Play и/или при необходимости обновите");
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=com.google.android.gms"));
                    startActivity(intent);
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            alertDialog.create();
            alertDialog.show();
            /*
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(connectionResult.getErrorCode() + " " + connectionResult.getErrorMessage());
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {finish();}
            });
            alertDialog.create();
            alertDialog.show();
            */

        }



    }

    private static class ProfileAuthTask extends AsyncTask<Void, Void, JSONObject>{
        protected String TAG = "#########" + ProfileAuthTask.class.getName();
        private WeakReference<SplashActivity> activityReference;

        ProfileAuthTask(SplashActivity splashActivity) {
            this.activityReference = new WeakReference<>(splashActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SplashActivity splashActivity = activityReference.get();
            if (splashActivity != null){
                splashActivity.curTask = this;
                splashActivity.tvAction.setText("Авторизация приложения ...");
            }

        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            return MainApplication.getInstance().getRestService().httpGet("/profile/auth");
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            SplashActivity splashActivity = activityReference.get();
            if (splashActivity != null){
                splashActivity.setResult(RESULT_CANCELED);
                splashActivity.finish();
            }
        }



        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            SplashActivity splashActivity = activityReference.get();
            if (splashActivity != null){
                if (isCancelled()){
                    splashActivity.setResult(RESULT_CANCELED);
                    splashActivity.finish();
                }
                else {
                    try {
                        if (jsonObject == null){
                            Toast.makeText(splashActivity, "Ошибка связи с сервером. Попоробуйте попозже", Toast.LENGTH_LONG).show();
                        }
                        else if (jsonObject.getString("status_code").equals("401")){
                            Intent loginIntent = new Intent(splashActivity, LoginActivity.class);
                            splashActivity.startActivityForResult(loginIntent, Constants.ACTIVITY_LOGIN);
                        }
                        else if (jsonObject.getString("status_code").equals("403")){
                            showBlockedDialog(splashActivity, jsonObject.getJSONObject("error"));
                        }
                        else if (jsonObject.getString("status_code").equals("426")){
                            showNewVersionDialog(splashActivity, jsonObject.getString("error"));
                        }
                        else if (jsonObject.getString("status_code").equals("451")){
                            showDocumentDialog(splashActivity, jsonObject.getJSONObject("error"));
                        }
                        else if (jsonObject.getString("status_code").equals("200")){
                            new ProfilePreferencesTask(splashActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        else {
                            Toast.makeText(splashActivity, jsonObject.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                    catch (Exception e){
                        Toast.makeText(splashActivity, e.toString(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }

            }

        }
    }

    private static class ProfilePreferencesTask extends AsyncTask<Void, Void, JSONObject>{
        protected String TAG = "#########" + ProfilePreferencesTask.class.getName();
        private WeakReference<SplashActivity> activityReference;

        ProfilePreferencesTask(SplashActivity splashActivity) {
            this.activityReference = new WeakReference<>(splashActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SplashActivity splashActivity = activityReference.get();
            if (splashActivity != null){
                splashActivity.curTask = this;
                splashActivity.tvAction.setText("Получение настроек приложения ...");
            }

        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            JSONObject result = MainApplication.getInstance().getRestService().httpGet("/profile/preferences");
            try {
                if (result.getString("status_code").equals("200")){
                    MainApplication.getInstance().parseData(result.getJSONObject("result"));
                }
            } catch (JSONException e) {e.printStackTrace();}

            return result;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            SplashActivity splashActivity = activityReference.get();
            if (splashActivity != null){
                splashActivity.setResult(RESULT_CANCELED);
                splashActivity.finish();
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            SplashActivity splashActivity = activityReference.get();
            if (splashActivity != null){
                if (isCancelled()){
                    splashActivity.setResult(RESULT_CANCELED);
                    splashActivity.finish();
                }
                else {
                    try {
                        if (jsonObject == null){
                            Toast.makeText(splashActivity, "Ошибка связи с сервером. Попоробуйте попозже", Toast.LENGTH_LONG).show();
                        }
                        else if (jsonObject.getString("status_code").equals("401")){
                            Intent loginIntent = new Intent(splashActivity, LoginActivity.class);
                            splashActivity.startActivityForResult(loginIntent, Constants.ACTIVITY_LOGIN);
                        }
                        else if (jsonObject.getString("status_code").equals("200")){
                            new DataTask(splashActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        else {
                            Toast.makeText(splashActivity, jsonObject.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                    catch (Exception e){
                        Toast.makeText(splashActivity, e.toString(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class DataTask extends AsyncTask<Void, Void, Integer>{
        protected String TAG = "#########" + DataTask.class.getName();
        private WeakReference<SplashActivity> activityReference;

        DataTask(SplashActivity splashActivity) {
            this.activityReference = new WeakReference<>(splashActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SplashActivity splashActivity = activityReference.get();
            if (splashActivity != null){
                splashActivity.curTask = this;
                splashActivity.tvAction.setText("Получение данных ...");
            }

        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return MainApplication.getInstance().getDot().getData();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            SplashActivity splashActivity = activityReference.get();
            if (splashActivity != null){
                splashActivity.setResult(RESULT_CANCELED);
                splashActivity.finish();
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            SplashActivity splashActivity = activityReference.get();
            if (splashActivity != null){
                if (isCancelled()){
                    splashActivity.setResult(RESULT_CANCELED);
                    splashActivity.finish();
                }
                else {
                        if (result == Constants.DOT_REST_OK){
                            splashActivity.setResult(RESULT_OK);
                            splashActivity.finish();
                        }
                        else if (result == Constants.DOT_IDENTIFICATION){
                            Intent loginIntent = new Intent(splashActivity, LoginActivity.class);
                            splashActivity.startActivityForResult(loginIntent, Constants.ACTIVITY_LOGIN);
                        }
                        else {
                            MainApplication.getInstance().showToastType(result);
                            splashActivity.setResult(RESULT_CANCELED);
                            splashActivity.finish();
                        }
                }
            }
        }
    }




    private static void showBlockedDialog(final SplashActivity splashActivity, final JSONObject data) throws JSONException {
        AlertDialog.Builder builder = new AlertDialog.Builder(splashActivity);
        builder.setMessage("Вы заблокированны. Для разблокировки необходимо позвонить по номеру: \n" + data.getString("phone") + ".\nВаш позывной: " + data.getString("callsign"));
        builder.setCancelable(false);
        builder.setPositiveButton("Позвонить", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);// (Intent.ACTION_CALL);
                try {
                    callIntent.setData(Uri.parse("tel:" + data.getString("phone")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                splashActivity.startActivity(callIntent);
                splashActivity.setResult(RESULT_CANCELED);
                splashActivity.finish();
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                splashActivity.setResult(RESULT_CANCELED);
                splashActivity.finish();
            }
        });
        builder.show();
    }

    private static class ProfileDocumentAcceptTask extends AsyncTask<String, Void, JSONObject>{
        protected String TAG = "#########" + ProfileDocumentAcceptTask.class.getName();
        private WeakReference<SplashActivity> activityReference;

        ProfileDocumentAcceptTask(SplashActivity splashActivity) {
            this.activityReference = new WeakReference<>(splashActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SplashActivity splashActivity = activityReference.get();
            if (splashActivity != null){
                splashActivity.curTask = this;
                splashActivity.tvAction.setText("Подтверждение принятия ...");
            }
        }

        @Override
        protected JSONObject doInBackground(String... strings) {

            return MainApplication.getInstance().getRestService().httpGet("/profile/document/accept?id=" + strings[0] );
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            SplashActivity splashActivity = activityReference.get();
            if (splashActivity != null){
                splashActivity.setResult(RESULT_CANCELED);
                splashActivity.finish();
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            SplashActivity splashActivity = this.activityReference.get();
            if (splashActivity != null){
                if (isCancelled()){
                    splashActivity.setResult(RESULT_CANCELED);
                    splashActivity.finish();
                }
                else {
                    new ProfileAuthTask(splashActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }
            }

        }
    }

    private static void showDocumentDialog(final SplashActivity splashActivity, final JSONObject data) throws JSONException {
        AlertDialog.Builder builder = new AlertDialog.Builder(splashActivity);
        String Text = "Для продолжения работы необходимо ознакомиться и подтвердить принятие слудующего документа: \n\"" + data.getString("doc") + "\".\n" +
                "С полным текстом документа можно ознакомиться по адресу в сети интернет: \n" + data.getString("link");

        builder.setTitle(data.getString("doc"));

        final TextView message = new TextView(builder.getContext());
        message.setText(Text);
        message.setAutoLinkMask(Linkify.WEB_URLS);
        message.setLinksClickable(true);
        message.setPadding(50, 50, 50,50);
        message.setMovementMethod(LinkMovementMethod.getInstance());
        builder.setView(message);


        builder.setPositiveButton("Принимаю", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                try {
                    new ProfileDocumentAcceptTask(splashActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data.getString("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                splashActivity.setResult(RESULT_CANCELED);
                splashActivity.finish();
            }
        });
        builder.show();
    }



    private static void showNewVersionDialog(final SplashActivity splashActivity, String obligatory){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(splashActivity);
        alertDialog.setCancelable(false);
        if (obligatory .equals("necessary")){
            alertDialog.setMessage("Для корректной работы приложения необходимо обновить приложение");
            alertDialog.setNegativeButton("Отмена" ,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    splashActivity.setResult(RESULT_CANCELED);
                    splashActivity.finish();
                }
            });
        }
        else {
            alertDialog.setMessage("Для корректной работы приложения рекомендуется обновить приложение");
            alertDialog.setNegativeButton("Позже" ,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new ProfilePreferencesTask(splashActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        }

        alertDialog.setPositiveButton("Обновить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=org.toptaxi.taximeter"));
                splashActivity.startActivity(intent);
                splashActivity.setResult(RESULT_CANCELED);
                splashActivity.finish();
            }
        });
        alertDialog.create();
        alertDialog.show();
    }


}
