package org.toptaxi.taximeter.data;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;

import java.util.ArrayList;

public class Preferences {
    protected static String TAG = "#########" + Preferences.class.getName();
    private String balanceCaption, ShareDriverText = "";
    public Boolean balanceView;
    private Integer dataTimer;
    public String dispatchingCallCaption, shareDriverInfo = "", checkPriorErrorText = "";
    public Boolean dispatchingCallView = false;
    public String dispatchingCallPhone;
    public String administrationCallCaption;
    public Boolean administrationCallView = false;
    public Boolean shareDriver = false;
    public String administrationCallPhone;
    public ClientTariff taximeterTariff;
    private Integer curTheme, screenOrientation, curVersion;
    private Boolean checkCurVersion = false;
    private ArrayList<String> templateMessages;

    public Preferences(){
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
        curTheme = sPref.getInt("curTheme", 0);
        curTheme = 2;
        dataTimer = 5;
        try {
            curVersion = Integer.valueOf(MainApplication.getInstance().getVersionCode());
        } catch(NumberFormatException nfe) {
            curVersion = 9999;
        }

        templateMessages = new ArrayList<>();

    }

    public void setFromJSON(JSONObject data) throws JSONException {
        if (data.has("share_driver_text"))ShareDriverText = data.getString("share_driver_text");
        if (data.has("cur_version"))curVersion = data.getInt("cur_version");

        if (data.has("template_messages")){
            templateMessages.clear();
            JSONArray templateMessagesJSON = data.getJSONArray("template_messages");
            for (int itemID= 0; itemID < templateMessagesJSON.length(); itemID++){
                templateMessages.add(templateMessagesJSON.getJSONObject(itemID).getString("message"));
            }
            //Log.d(TAG, "templateMessages = " + templateMessages.toString());
        }

        //Log.d(TAG, "onCreate data = " + data.toString());
        taximeterTariff = new ClientTariff();
        this.balanceCaption = data.getString("balance_caption");
        this.balanceView = data.getBoolean("balance_view");
        this.dataTimer = data.getInt("timer");
        this.dispatchingCallCaption = data.getString("dispathing_call_caption");
        this.dispatchingCallView = data.getBoolean("dispathing_call");
        this.dispatchingCallPhone = data.getString("dispathing_call_phone");
        this.administrationCallCaption = data.getString("administration_call_caption");
        this.administrationCallView = data.getBoolean("administration_call");
        this.administrationCallPhone = data.getString("administration_call_phone");

        if (data.has("taximeterTariff")) {
            taximeterTariff.setFromJSON(data.getJSONArray("taximeterTariff").getJSONObject(0));
        }
        if (data.has("share_driver"))this.shareDriver = data.getBoolean("share_driver");
        if (data.has("share_driver_info"))this.shareDriverInfo = data.getString("share_driver_info");

        if (data.has("send_data_port")){MainApplication.getInstance().getDot().setSendDataPort(data.getString("send_data_port"));}
        if (data.has("get_data_port")){MainApplication.getInstance().getDot().setGetDataPort(data.getString("get_data_port"));}

        if (data.has("check_prior_error")){this.checkPriorErrorText = data.getString("check_prior_error");}
    }

    public ArrayList<String> getTemplateMessages() {
        return templateMessages;
    }

    public Boolean checkCurVersion(){
        //Log.d(TAG, "checkCurVersion checkCurVersion = " + checkCurVersion + "curVersion = " + curVersion + " Integer.parseInt(MainApplication.getInstance().getVersionCode())" + Integer.parseInt(MainApplication.getInstance().getVersionCode()));
        try {

            if ((!checkCurVersion) && (curVersion > Integer.parseInt(MainApplication.getInstance().getVersionCode())))
                return true;
        }
        catch (NumberFormatException nfe){return false;}
        return false;
    }

    public void setCheckCurVersion(){
        checkCurVersion = true;
    }

    public String getShareDriverText() {
        return ShareDriverText;
    }

    public String getCheckPriorErrorText() {
        return checkPriorErrorText;
    }



    public void changeTheme(){
        curTheme++;
        if (curTheme > 2)curTheme = 0;
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
        SharedPreferences.Editor editor = sPref.edit();
        editor.putInt("curTheme", curTheme);
        editor.commit();
    }

    public int getTheme(){
        Log.d(TAG, "curTheme = " + curTheme);
        switch (curTheme){
            case 0:return AppCompatDelegate.MODE_NIGHT_NO;
            case 1:return AppCompatDelegate.MODE_NIGHT_YES;
            case 2:return AppCompatDelegate.MODE_NIGHT_AUTO;
        }
        return AppCompatDelegate.MODE_NIGHT_AUTO;
    }

    public String getThemeName(){
        switch (curTheme){
            case 0:return "Дневная";
            case 1:return "Ночная";
            case 2:return "Авто смена темы";
        }
        return "Авто смена темы";
    }



    public Integer getScreenOrientation() {
        return screenOrientation;
    }

    public void setScreenOrientation(Integer screenOrientation) {
        this.screenOrientation = screenOrientation;
    }

    public Integer getDataTimer() {
        return dataTimer;
    }
}
