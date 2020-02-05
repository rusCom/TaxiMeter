package org.toptaxi.taximeter.activities;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.dialogs.LoadingDialog;
import org.toptaxi.taximeter.tools.LockOrientation;

import java.lang.ref.WeakReference;

public class LoginActivity extends AppCompatActivity {
    private EditText edActivityLoginPhone, edActivityLoginCode;
    private TextInputLayout ilActivityLoginPhone, ilActivityLoginPassword;
    private LinearLayout llActivityLoginProgress;
    private TextView tvActivityLoginTimer;
    private Button btnActivityLoginGetPassword;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new LockOrientation(this).lock();
        setContentView(R.layout.activity_login);
        edActivityLoginPhone    = findViewById(R.id.edActivityLoginPhone);
        edActivityLoginCode = findViewById(R.id.edActivityLoginCode);
        ilActivityLoginPhone    = findViewById(R.id.ilActivityLoginPhone);
        ilActivityLoginPassword = findViewById(R.id.ilActivityLoginPassword);
        llActivityLoginProgress = findViewById(R.id.llActivityLoginProgress);
        tvActivityLoginTimer    = findViewById(R.id.tvActivityLoginTimer);
        btnActivityLoginGetPassword = findViewById(R.id.btnActivityLoginGetPassword);

        edActivityLoginPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());



        Button okButton = findViewById(R.id.btnActivityLoginGetToken);
        okButton.setTextSize((float) (okButton.getTextSize() * 1.5));

        llActivityLoginProgress.setVisibility(View.GONE);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        edActivityLoginPhone.setText(sharedPreferences.getString("accountPhone", ""));
        edActivityLoginCode.setText(sharedPreferences.getString("accountCode", ""));
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accountPhone", edActivityLoginPhone.getText().toString().trim());
        editor.apply();
    }

    private boolean validatePassword(){
        if (edActivityLoginCode.getText().toString().trim().equals("")){
            ilActivityLoginPassword.setError(getString(R.string.errorPasswordNotEntered));
            edActivityLoginCode.requestFocus();
            return false;
        }
        else if (edActivityLoginCode.getText().toString().trim().length() != 4){
            ilActivityLoginPassword.setError(getString(R.string.errorPasswordNotEntered));
            edActivityLoginCode.requestFocus();
            return false;
        }
        else {
            ilActivityLoginPassword.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validatePhone(){
        if (edActivityLoginPhone.getText().toString().trim().equals("")){
            ilActivityLoginPhone.setError(getString(R.string.errorPhoneNotEntered));
            edActivityLoginPhone.requestFocus();
            return false;
        }
        else {
            ilActivityLoginPhone.setErrorEnabled(false);
        }
        return true;
    }

    public void profileLoginClick(View view){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accountPhone", edActivityLoginPhone.getText().toString().trim());
        editor.apply();
        if (validatePhone()){
            new ProfileLoginTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, edActivityLoginPhone.getText().toString().trim());
        }
    }

    public void profileRegistrationClick(View view) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accountPhone", edActivityLoginPhone.getText().toString().trim());
        editor.putString("accountCode", edActivityLoginCode.getText().toString().trim());
        editor.apply();

        if (!validatePhone())return;
        if (!validatePassword())return;
        String[] params = {edActivityLoginPhone.getText().toString(), edActivityLoginCode.getText().toString()};
        new ProfileRegistrationTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }



    private static class ProfileRegistrationTask extends AsyncTask<String, Void, JSONObject>{
        protected String TAG = "#########" + ProfileRegistrationTask.class.getName();
        private WeakReference<LoginActivity> activityReference;
        LoadingDialog loadingDialog;

        ProfileRegistrationTask(LoginActivity loginActivity) {
            this.activityReference = new WeakReference<>(loginActivity);
            loadingDialog = new LoadingDialog(this.activityReference.get());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            return MainApplication.getInstance().getRestService().httpGet("/profile/registration?phone=" + strings[0] + "&code=" + strings[1]);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            loadingDialog.dismiss();
            LoginActivity loginActivity = this.activityReference.get();
            if (loginActivity == null) return;
            try {
                if (jsonObject == null){
                    Toast.makeText(loginActivity, "Ошибка связи с сервером. Попоробуйте попозже", Toast.LENGTH_LONG).show();
                }
                else if (jsonObject.getString("status_code").equals("401")){
                    showRegisterDialog(this.activityReference.get(), jsonObject.getString("error"));
                }
                else if (jsonObject.getString("status_code").equals("404")){
                    Toast.makeText(loginActivity, jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                }
                else if (jsonObject.getString("status_code").equals("200")){
                    if (jsonObject.getString("result").equals("")){
                        Toast.makeText(loginActivity, jsonObject.toString(), Toast.LENGTH_LONG).show();
                    }
                    else {
                        MainApplication.getInstance().getMainAccount().setToken(jsonObject.getString("result"));
                        MainApplication.getInstance().getRestService().reloadHeader();
                        loginActivity.setResult(RESULT_OK);
                        loginActivity.finish();
                    }
                }
                else {
                    Toast.makeText(loginActivity, jsonObject.toString(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(loginActivity, e.toString(), Toast.LENGTH_LONG).show();
            }

        }
    }

    private static class TimerTask extends AsyncTask<Void, Void, Void>{
        protected String TAG = "#########" + TimerTask.class.getName();
        private WeakReference<LoginActivity> activityReference;
        int Timer = 90;

        TimerTask(LoginActivity loginActivity) {
            this.activityReference = new WeakReference<>(loginActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LoginActivity loginActivity = this.activityReference.get();
            if (loginActivity == null) return;

            loginActivity.llActivityLoginProgress.setVisibility(View.VISIBLE);
            loginActivity.btnActivityLoginGetPassword.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (Timer > 0){
                publishProgress();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Timer = Timer - 1;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            LoginActivity loginActivity = this.activityReference.get();
            if (loginActivity == null) return;
            String message = loginActivity.getString(R.string.smsWait) + " " + Timer + " " + loginActivity.getString(R.string.reductionSek);
            loginActivity.tvActivityLoginTimer.setText(message);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            LoginActivity loginActivity = this.activityReference.get();
            if (loginActivity == null) return;
            loginActivity.llActivityLoginProgress.setVisibility(View.GONE);
            loginActivity.btnActivityLoginGetPassword.setVisibility(View.VISIBLE);
        }
    }


    private static class ProfileLoginTask extends AsyncTask<String, Void, JSONObject>{
        protected String TAG = "#########" + ProfileLoginTask.class.getName();
        private WeakReference<LoginActivity> activityReference;
        LoadingDialog loadingDialog;

        ProfileLoginTask(LoginActivity loginActivity) {
            this.activityReference = new WeakReference<>(loginActivity);
            loadingDialog = new LoadingDialog(this.activityReference.get());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            return MainApplication.getInstance().getRestService().httpGet("/profile/login?phone=" + strings[0]);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            loadingDialog.dismiss();
            LoginActivity loginActivity = this.activityReference.get();
            if (loginActivity == null) return;
            try {
                if (jsonObject == null){
                    Toast.makeText(loginActivity, "Ошибка связи с сервером. Попоробуйте попозже", Toast.LENGTH_LONG).show();
                }
                else if (jsonObject.getString("status_code").equals("401")){
                    showRegisterDialog(this.activityReference.get(), jsonObject.getString("error"));
                }
                else if (jsonObject.getString("status_code").equals("404")){
                    Toast.makeText(loginActivity, jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                }
                else if (jsonObject.getString("status_code").equals("200")){
                    new TimerTask(loginActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                else {
                    Toast.makeText(loginActivity, jsonObject.toString(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(loginActivity, e.toString(), Toast.LENGTH_LONG).show();
            }

        }
    }

    private static void showRegisterDialog(final LoginActivity loginActivity, final String phone){
        AlertDialog.Builder builder = new AlertDialog.Builder(loginActivity);
        builder.setMessage("Водитель с данным номером телефона не зарегестрирован. Для регистрации необходимо позвонить по номеру: " + phone);
        builder.setCancelable(false);
        builder.setPositiveButton("Позвонить", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);// (Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phone));
                loginActivity.startActivity(callIntent);
                dialog.dismiss();
                loginActivity.setResult(RESULT_CANCELED);
                loginActivity.finish();

            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        builder.show();
    }

}
