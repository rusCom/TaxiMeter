package org.toptaxi.taximeter.activities;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.tools.Constants;

import java.util.ArrayList;

public class ShareDriverActivity extends AppCompatActivity {
    private static String TAG = "#########" + ShareDriverActivity.class.getName();
    private static final int CONTACT_PICK_RESULT = 666;
    private EditText edPhone;
    private TextInputLayout ilSharedDriverPhone;
    private LinearLayout llShareDriverTimer;
    private Button btnCheckPhone;
    private TextView tvShareDriver;
    private String phone = "", smsText = "";

    String mContactId;
    String mPhoneNumber;
    String mContactName;
    String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_driver);
        ((TextView)findViewById(R.id.tvShareDriverInfo)).setText(MainApplication.getInstance().getMainPreferences().shareDriverInfo);
        ilSharedDriverPhone = (TextInputLayout)findViewById(R.id.ilSharedDriverPhone);
        edPhone = (EditText)findViewById(R.id.edShareDriverPhone);
        edPhone.setText("");
        edPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        llShareDriverTimer = (LinearLayout)findViewById(R.id.llShareDriverTimer);
        llShareDriverTimer.setVisibility(View.GONE);
        btnCheckPhone = (Button)findViewById(R.id.btnShareDriverCheckPhone);
        tvShareDriver = (TextView)findViewById(R.id.tvShareDriver);
    }

    public void checkPhoneClick(View view){
        if (validatePhone()){
            (new CheckPhoneTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, edPhone.getText().toString());
        }
    }

    public void pickContactClick(View v) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)){
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                        Constants.MY_PERMISSIONS_READ_CONTACTS);
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                        Constants.MY_PERMISSIONS_READ_CONTACTS);
            }
        }
        else pickContact();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.MY_PERMISSIONS_READ_CONTACTS){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
                    pickContact();
                }
            }
        }

        if (requestCode == Constants.MY_PERMISSIONS_SEND_SMS){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
                    sendSMS();
                }
            }
        }

    }

    public void sendSMS(){
        //MainApplication.getInstance().showToast("phone = " + phone + " text = " + smsText);
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> parts = sms.divideMessage(smsText);
        Log.d(TAG, "parts = " + parts.size());


        ArrayList<PendingIntent> sentPIarr = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveredPIarr = new ArrayList<PendingIntent>();

        for (int i = 0; i < parts.size(); i++) {
            sentPIarr.add(PendingIntent.getBroadcast(this, 0,new Intent(SENT), 0));
            deliveredPIarr.add(PendingIntent.getBroadcast(this, 0,new Intent(DELIVERED), 0));
        }

        sms.sendMultipartTextMessage(phone, null, parts, sentPIarr, null);


        showDialog("Приглашение успешно отправлено", true);


    }

    public void btnShareClick(View view){
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, MainApplication.getInstance().getMainPreferences().getShareDriverText());
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public void pickContact(){
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICK_RESULT);
    }



    private class CheckPhoneTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            edPhone.setEnabled(false);
            btnCheckPhone.setVisibility(View.GONE);
            llShareDriverTimer.setVisibility(View.VISIBLE);
            tvShareDriver.setText("Проверка номера телефона ...");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return MainApplication.getInstance().getDot().getDataType("shareDriver", params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "onPostExecute result = " + result);
            try {
                JSONObject data = new JSONObject(result);
                if (data.has("response")){
                    if (data.getString("response").equals("ok")){
                        tvShareDriver.setText("Отправка СМС ...");
                        phone = data.getString("phone");
                        smsText = data.getString("result");
                        if (ContextCompat.checkSelfPermission(ShareDriverActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)        {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(ShareDriverActivity.this, android.Manifest.permission.READ_CONTACTS)){
                                ActivityCompat.requestPermissions(ShareDriverActivity.this, new String[]{Manifest.permission.SEND_SMS},
                                        Constants.MY_PERMISSIONS_SEND_SMS);
                            }
                            else{
                                ActivityCompat.requestPermissions(ShareDriverActivity.this, new String[]{Manifest.permission.SEND_SMS},
                                        Constants.MY_PERMISSIONS_SEND_SMS);
                            }
                        }
                        else sendSMS();
                    }
                    else {
                        showDialog(data.getString("result"), false);
                        edPhone.setEnabled(true);
                        btnCheckPhone.setVisibility(View.VISIBLE);
                        llShareDriverTimer.setVisibility(View.GONE);
                    }

                }
                else {
                    showDialog("Ошибка при отправке данных", false);
                    edPhone.setEnabled(true);
                    btnCheckPhone.setVisibility(View.VISIBLE);
                    llShareDriverTimer.setVisibility(View.GONE);
                }
            } catch (JSONException e) {
                showDialog("Ошибка при отправке данных", false);
                edPhone.setEnabled(true);
                btnCheckPhone.setVisibility(View.VISIBLE);
                llShareDriverTimer.setVisibility(View.GONE);
            }
        }
    }

    private void showDialog(String text, final boolean isClose){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Внимание");
        alertDialog.setMessage(text);
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isClose)
                    finish();
            }
        });
        alertDialog.create();
        alertDialog.show();
    }

    private boolean validatePhone(){
        if (edPhone.getText().toString().trim().equals("")){
            ilSharedDriverPhone.setError(getString(R.string.errorPhoneNotEntered));
            edPhone.requestFocus();
            return false;
        }
        else {
            ilSharedDriverPhone.setErrorEnabled(false);
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case CONTACT_PICK_RESULT:
                    Uri contactData = data.getData();
                    Cursor c =  getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToNext()) {
                        mContactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                        mContactName = c.getString(c.getColumnIndexOrThrow(
                                ContactsContract.Contacts.DISPLAY_NAME));

                        String hasPhone = c.getString(c.getColumnIndex(
                                ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        Log.d(TAG, "name: " + mContactName);
                        Log.d(TAG, "hasPhone:" + hasPhone);
                        Log.d(TAG, "contactId:" + mContactId);

                        // если есть телефоны, получаем и выводим их
                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ mContactId,
                                    null,
                                    null);


                            while (phones.moveToNext()) {
                                mPhoneNumber = phones.getString(phones.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                                Log.d(TAG, "телефон:" + mPhoneNumber);
                                edPhone.setText(mPhoneNumber);
                            }
                            phones.close();
                        }
                    }
                    break;
            }

        } else {
            Log.d(TAG, "ERROR");
        }
    }

}
