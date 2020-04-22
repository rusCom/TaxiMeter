package org.toptaxi.taximeter.data;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;

import java.util.ArrayList;

public class Preferences {
    protected static String TAG = "#########" + Preferences.class.getName();

    private SharedPreferences sPref;


    private Integer dataTimer;
    private String checkPriorErrorText = "", covidLink = "";
    private String agreementLink = "";
    private Integer curTheme, curVersion, agreementVersion = 0, placesTimeOut = 60;
    private Boolean checkCurVersion = false;
    private ArrayList<String> templateMessages;
    private Boolean ParkingButtons = false;

    private Boolean Friends = false, DispatcherMessages = false;
    private String FriendsCaption = "", FriendsText = "", DispatcherPhone = "", ClientsFriendsText = "", administrationCallPhone = "", faqLink = "";

    private Boolean newOrderAlarmCheck;
    private Integer newOrderAlarmDistance, newOrderAlarmCost;

    public Preferences(){
        sPref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
        curTheme = sPref.getInt("curTheme", 0);
        curTheme = 2;
        dataTimer = 5;
        try {
            curVersion = Integer.valueOf(MainApplication.getInstance().getVersionCode());
        } catch(NumberFormatException nfe) {
            curVersion = 9999;
        }

        templateMessages = new ArrayList<>();

        newOrderAlarmCheck      = sPref.getBoolean("newOrderAlarmCheck", true);
        newOrderAlarmDistance   = sPref.getInt("newOrderAlarmDistance", -1);
        newOrderAlarmCost       = sPref.getInt("newOrderAlarmCost", 100);

    }

    public void setFromJSON(JSONObject data) throws JSONException {

        if (data.has("cur_version"))curVersion = data.getInt("cur_version");
        if (data.has("parkings_buttons"))ParkingButtons = data.getBoolean("parkings_buttons");

        if (data.has("template_messages")){
            templateMessages.clear();
            JSONArray templateMessagesJSON = data.getJSONArray("template_messages");
            for (int itemID= 0; itemID < templateMessagesJSON.length(); itemID++){
                templateMessages.add(templateMessagesJSON.getJSONObject(itemID).getString("message"));
            }
            //Log.d(TAG, "templateMessages = " + templateMessages.toString());
        }

        //Log.d(TAG, "onCreate data = " + data.toString());
        ClientTariff taximeterTariff = new ClientTariff();
        this.dataTimer = data.getInt("timer");
        this.administrationCallPhone = data.getString("administration_call_phone");

        if (data.has("taximeterTariff")) {
            taximeterTariff.setFromJSON(data.getJSONArray("taximeterTariff").getJSONObject(0));
        }

        if (data.has("send_data_port")){MainApplication.getInstance().getDot().setSendDataPort(data.getString("send_data_port"));}
        if (data.has("get_data_port")){MainApplication.getInstance().getDot().setGetDataPort(data.getString("get_data_port"));}
        if (data.has("rest_port")){MainApplication.getInstance().getDot2().setRestPort(data.getString("rest_port"));}

        if (data.has("check_prior_error")){this.checkPriorErrorText = data.getString("check_prior_error");}

        if (data.has("agreement_link")){this.agreementLink = data.getString("agreement_link");}
        if (data.has("agreement_version")){this.agreementVersion = data.getInt("agreement_version");}
        if (data.has("places_timeout")){this.placesTimeOut = data.getInt("places_timeout");}
        if (data.has("faq_link")){this.faqLink = data.getString("faq_link");}
        if (data.has("covid_link")){this.covidLink = data.getString("covid_link");}
        if (data.has("menu")){MainApplication.getInstance().getMenuItems().setFromJSON(data.getJSONObject("menu"));}

        if (data.has("friends")){
            this.Friends = true;
            JSONObject friends = data.getJSONObject("friends");
            this.FriendsText = friends.getString("text");
            this.FriendsCaption = friends.getString("caption");
        }
        if (data.has("dispatcher")){
            JSONObject d = data.getJSONObject("dispatcher");
            if (d.has("phone"))this.DispatcherPhone = d.getString("phone");
            if (d.has("messages"))this.DispatcherMessages = d.getBoolean("messages");
        }
        if (data.has("client_friends_text"))this.ClientsFriendsText = data.getString("client_friends_text");

        if (data.has("topics")){MainApplication.getInstance().getFirebaseService().CheckTopics(data.getJSONArray("topics"));}
    }

    public String getAdministrationCallPhone() {
        return administrationCallPhone;
    }

    public String getClientsFriendsText() {
        return ClientsFriendsText;
    }

    public Boolean getFriends() {
        return Friends;
    }

    public String getFriendsCaption() {
        return FriendsCaption;
    }

    public String getFriendsText() {
        return FriendsText;
    }

    public Boolean getDispatcherMessages() {
        return DispatcherMessages;
    }

    public String getDispatcherPhone() {
        return DispatcherPhone;
    }

    public String getFaqLink() {
        return faqLink;
    }

    public Integer getPlacesTimeOut() {
        return placesTimeOut;
    }

    public String getAgreementLink() {
        return agreementLink;
    }

    Integer getAgreementVersion() {
        return agreementVersion;
    }

    public String getCovidLink() {
        return covidLink;
    }

    public Boolean getParkingButtons() {
        return ParkingButtons;
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


    public String getCheckPriorErrorText() {
        return checkPriorErrorText;
    }



    public void changeTheme(){
        curTheme++;
        if (curTheme > 2)curTheme = 0;
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
        SharedPreferences.Editor editor = sPref.edit();
        editor.putInt("curTheme", curTheme);
        editor.apply();
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
            case 2:return "Подсветка";
        }
        return "Подсветка";
    }

    public Boolean getNewOrderAlarmCheck() {
        return newOrderAlarmCheck;
    }

    public Integer getNewOrderAlarmDistance() {
        return newOrderAlarmDistance;
    }

    public Integer getNewOrderAlarmCost() {
        return newOrderAlarmCost;
    }

    public void setNewOrderAlarm(Boolean newOrderAlarmCheck, Integer newOrderAlarmDistance, Integer newOrderAlarmCost){
        this.newOrderAlarmCheck = newOrderAlarmCheck;
        this.newOrderAlarmDistance = newOrderAlarmDistance;
        this.newOrderAlarmCost = newOrderAlarmCost;

        SharedPreferences.Editor editor = sPref.edit();
        editor.putBoolean("newOrderAlarmCheck", this.newOrderAlarmCheck);
        editor.putInt("newOrderAlarmDistance", this.newOrderAlarmDistance);
        editor.putInt("newOrderAlarmCost", this.newOrderAlarmCost);
        editor.apply();

    }

    public Integer getDataTimer() {
        return dataTimer;
    }
}
