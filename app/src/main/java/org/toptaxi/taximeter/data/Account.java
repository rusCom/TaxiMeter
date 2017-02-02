package org.toptaxi.taximeter.data;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.tools.Constants;

public class Account {
    protected static String TAG = "#########" + Account.class.getName();
    private String Token;
    private String Balance;
    private String Name;
    private String serName;
    private Integer status, lastStatus = -1;
    public RoutePoint routePoint;
    private String NotReadMessageCount;
    public String UnlimInfo = "";
    private Boolean isCheckPriorOrder = true;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private OnAccountChangeListener onAccountChangeListener;
    private Boolean isGetOnLine = false;

    public interface OnAccountChangeListener {
        void OnAccountChange();
    }

    public Account(String token) {
        Token = token;
    }

    public void setFromJSON(JSONObject data) throws JSONException {
        //Log.d(TAG, "setFromJSON data = " + data.toString());
        if (data.has("balance")) {this.Balance = data.getString("balance");}
        if (data.has("name")) {this.Name = data.getString("name");}
        if (data.has("ser_name")) {this.serName = data.getString("ser_name");}
        if (data.has("status")) {this.status = data.getInt("status");}
        if (data.has("nrmc")) {this.NotReadMessageCount = data.getString("nrmc");}
        if (data.has("unlim")) {this.UnlimInfo = data.getString("unlim");}
        else {this.UnlimInfo = "";}
        if (data.has("check_prior")) {this.isCheckPriorOrder = data.getBoolean("check_prior");}
        if (data.has("get_on_line")){this.isGetOnLine = data.getBoolean("get_on_line");}



        if (!status.equals(lastStatus)){
            lastStatus = status;
            if (status == Constants.DRIVER_ON_ORDER)MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_CUR_ORDER);
            else MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_CUR_ORDERS);
        }


        if (data.has("rp")){
            if (routePoint == null){routePoint = new RoutePoint(data.getJSONArray("rp").getJSONObject(0));}
            else {routePoint.setFromJSON(data.getJSONArray("rp").getJSONObject(0));}
        }
        else {routePoint = null;}
        if (onAccountChangeListener != null){
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    onAccountChangeListener.OnAccountChange();
                }
            });
        }
    }

    public void setNullStatus(){
        status = null;
    }

    public void setOnAccountChangeListener(OnAccountChangeListener onAccountChangeListener) {
        this.onAccountChangeListener = onAccountChangeListener;
    }

    public Boolean getOnLine() {
        if (status == Constants.DRIVER_OFFLINE)return isGetOnLine;
        return true;
    }

    public Boolean getCheckPriorOrder() {
        return isCheckPriorOrder;
    }

    public Integer getStatus() {return status;}

    public String getBalance() {
        return Balance;
    }

    public String getName() {
        return Name;
    }

    public String getSerName() {
        return serName;
    }

    public String getToken() {
        return Token;
    }


    public String getStatusName(){
        String result = "";
        switch (status){
            case 0:result = "В свободном полете";break;
            case 1:result = "На авторазадче";break;
            case 2:result = "На заказе";break;
        }
        return result;
    }

    void setToken(String token) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString("accountToken", token);
        editor.apply();
        Token = token;
    }

    public String getNotReadMessageCount() {
        return NotReadMessageCount;
    }
}
