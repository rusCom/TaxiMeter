package org.toptaxi.taximeter.activities;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.tools.Constants;
import org.toptaxi.taximeter.tools.LockOrientation;

public class SplashActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    protected static String TAG = "#########" + SplashActivity.class.getName();
    AsyncTask curTask;
    TextView tvAction;
    boolean isGooglePlayConnect, isLocationEnabled;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new LockOrientation(this).lock();
        setContentView(R.layout.activity_splash);

        tvAction  = (TextView)findViewById(R.id.tvSplashAction);
        ((TextView)findViewById(R.id.tvSplashVersion)).setText(MainApplication.getInstance().getVersionName());
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();

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
                new GetPreferencesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        isGooglePlayConnect = true;
        MainApplication.getInstance().setGoogleApiClient(mGoogleApiClient);
        //Log.d(TAG, "onConnected");
        init();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(connectionResult.getErrorCode() + " " + connectionResult.getErrorMessage());
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {finish();}
            });
            alertDialog.create();
            alertDialog.show();

    }

    private class GetPreferencesTask extends AsyncTask<Void, Void, Integer>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            curTask = this;
            tvAction.setText(getString(R.string.tvSplashActionConnect));
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return MainApplication.getInstance().getDot().getPreferences();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            setResult(RESULT_CANCELED);
            finish();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (isCancelled()){
                setResult(RESULT_CANCELED);
                finish();
            }
            if (result == Constants.DOT_REST_OK){
                new GetDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            else if (result == Constants.DOT_NEW_VERSION){
                showNewVersionDialog();
            }
            else if (result == Constants.DOT_BLOCKED){
                showBlockedDialog();
            }
            else if (result == Constants.DOT_IDENTIFICATION){
                Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivityForResult(loginIntent, Constants.ACTIVITY_LOGIN);
            }
            else {
                MainApplication.getInstance().showToastType(result);
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    public void showNewVersionDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(getResources().getString(R.string.errorNewVersion));
        alertDialog.setPositiveButton("Обновить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=org.toptaxi.taximeter"));
                startActivity(intent);
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        alertDialog.setNegativeButton(getResources().getString(R.string.btnCancel) ,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        alertDialog.create();
        alertDialog.show();
    }

    public void showBlockedDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Вы заблокированны. Для выхода свяжитесь с администрацией");
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        alertDialog.create();
        alertDialog.show();
    }

    private class GetDataTask extends AsyncTask<Void, Void, Integer>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            curTask = this;
            tvAction.setText(getString(R.string.tvSplashActionGetData));
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return MainApplication.getInstance().getDot().getData();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            setResult(RESULT_CANCELED);
            finish();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (isCancelled()){
                setResult(RESULT_CANCELED);
                finish();
            }
            if (result == Constants.DOT_REST_OK){
                setResult(RESULT_OK);
                finish();
            }
            else if (result == Constants.DOT_IDENTIFICATION){
                Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivityForResult(loginIntent, Constants.ACTIVITY_LOGIN);
            }
            else {
                MainApplication.getInstance().showToastType(result);
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }
}
