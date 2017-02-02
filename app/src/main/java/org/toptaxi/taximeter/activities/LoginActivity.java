package org.toptaxi.taximeter.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.tools.Constants;
import org.toptaxi.taximeter.tools.LockOrientation;

public class LoginActivity extends AppCompatActivity {
    private static final String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private EditText edActivityLoginPhone, edActivityLoginPassword;
    private TextInputLayout ilActivityLoginPhone, ilActivityLoginPassword;
    private LinearLayout llActivityLoginProgress;
    private TextView tvActivityLoginTimer;
    private Button btnActivityLoginGetPassword;
    private SharedPreferences sharedPreferences;
    private boolean IsFirstGetPassword = true, SMSReceived = false;
    private SMSReceive smsReceive;
    int Timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new LockOrientation(this).lock();
        setContentView(R.layout.activity_login);
        edActivityLoginPhone    = (EditText)findViewById(R.id.edActivityLoginPhone);
        edActivityLoginPassword = (EditText)findViewById(R.id.edActivityLoginPassword);
        ilActivityLoginPhone    = (TextInputLayout)findViewById(R.id.ilActivityLoginPhone);
        ilActivityLoginPassword = (TextInputLayout)findViewById(R.id.ilActivityLoginPassword);
        llActivityLoginProgress = (LinearLayout)findViewById(R.id.llActivityLoginProgress);
        tvActivityLoginTimer    = (TextView)findViewById(R.id.tvActivityLoginTimer);
        btnActivityLoginGetPassword = (Button)findViewById(R.id.btnActivityLoginGetPassword);

        edActivityLoginPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        edActivityLoginPhone.setText(sharedPreferences.getString("accountPhone", ""));

        Button okButton = (Button)findViewById(R.id.btnActivityLoginGetToken);
        okButton.setTextSize(okButton.getTextSize() * 2);

        llActivityLoginProgress.setVisibility(View.GONE);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accountPhone", edActivityLoginPhone.getText().toString().trim());
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (smsReceive != null)unregisterReceiver(smsReceive);
    }


    public void getToken(View view) {
        if (!validatePhone())return;
        if (!validatePassword())return;
        String[] params = {edActivityLoginPhone.getText().toString(), edActivityLoginPassword.getText().toString()};
        new GetTokenTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    private boolean validatePassword(){
        if (edActivityLoginPassword.getText().toString().trim().equals("")){
            ilActivityLoginPassword.setError(getString(R.string.errorPasswordNotEntered));
            edActivityLoginPassword.requestFocus();
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


    public void getPassword(View view) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECEIVE_SMS)){
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECEIVE_SMS},
                        Constants.MY_PERMISSIONS_RECEIVE_SMS);
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECEIVE_SMS},
                        Constants.MY_PERMISSIONS_RECEIVE_SMS);
            }
        }
        else startGetPasswordTask();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        startGetPasswordTask();
    }

    public void startGetPasswordTask(){
        if (validatePhone()){
            if (IsFirstGetPassword){
                new GetPasswordTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, edActivityLoginPhone.getText().toString().trim());
            }
            else {
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Подтверждение")
                        .setMessage("Выслать смс повторно?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new GetPasswordTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, edActivityLoginPhone.getText().toString().trim());
                            }

                        })
                        .setNegativeButton("Нет", null)
                        .show();

            }

        }
    }

    private class GetPasswordTask extends AsyncTask<String, Void, Integer>{

        @Override
        protected Integer doInBackground(String... strings) {
            return MainApplication.getInstance().getDot().getPassword(strings[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == Constants.DOT_REST_OK){
                // если есть доступ  к чтению смс
                if (ContextCompat.checkSelfPermission(LoginActivity.this, android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED){
                    if (smsReceive == null){
                        smsReceive = new SMSReceive();
                        IntentFilter intentFilter = new IntentFilter(SMS_ACTION);
                        registerReceiver(smsReceive, intentFilter);
                    }
                }
                new TimerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                IsFirstGetPassword = false;
            }
            else {
                MainApplication.getInstance().showToastType(result);
            }
        }
    }

    private class GetTokenTask extends AsyncTask<String, Void, Integer>{

        @Override
        protected Integer doInBackground(String... params) {
            return MainApplication.getInstance().getDot().getToken(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == Constants.DOT_REST_OK){
                setResult(RESULT_OK);
                finish();
            }
            else MainApplication.getInstance().showToastType(result);
        }
    }



    private class TimerTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Timer = 90;
            SMSReceived = false;
            llActivityLoginProgress.setVisibility(View.VISIBLE);
            btnActivityLoginGetPassword.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            while ((Timer > 0) & (!SMSReceived)){
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
            String message = getString(R.string.smsWait) + " " + String.valueOf(Timer) + " " + getString(R.string.reductionSek);
            tvActivityLoginTimer.setText(message);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            llActivityLoginProgress.setVisibility(View.GONE);
            btnActivityLoginGetPassword.setVisibility(View.VISIBLE);
        }
    }

    private class SMSReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && SMS_ACTION.compareToIgnoreCase(intent.getAction()) == 0) {
                Bundle myBundle = intent.getExtras();

                Object[] pduArray = (Object[]) intent.getExtras().get("pdus");
                assert pduArray != null;
                SmsMessage[] messages = new SmsMessage[pduArray.length];
                for (int i = 0; i < pduArray.length; i++) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        String format = myBundle.getString("format");
                        messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i], format);
                    } else {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i]);
                    }
                }
                StringBuilder bodyText = new StringBuilder();
                for (SmsMessage message : messages) {
                    bodyText.append(message.getMessageBody());
                }
                String body = bodyText.toString();
                if (body.charAt(0) == ':'){
                    SMSReceived = true;
                    String pass = body.substring(1, 5);
                    edActivityLoginPassword.setText(pass);
                    getToken(null);
                }
            }

        }
    }
}
