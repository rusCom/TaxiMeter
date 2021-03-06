package org.toptaxi.taximeter.data;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.activities.WebViewActivity;
import org.toptaxi.taximeter.tools.DateTimeTools;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Order {
    protected static String TAG = "#########" + Order.class.getName();
    private Integer PayType, IsFree = 0,CalcType = 0, Timer, ParkingID, ID, lastRequestUID = 0, State, Check, DenyPenaltyCost = 0;
    private String Note = "", ClientPhone = "", DispatchingName = "", MainAction = "", StateName = "", DispPay = "";
    private Double Cost, Distance;
    private Boolean IsNew = false, IsDenyPenalty = false;
    private Calendar WorkDate;
    private long NewOrderTimer = 15000;

    private List<RoutePoint> routePoints;

    public Order() {
    }

    public void setFromJSON(JSONObject data) throws JSONException {
        this.IsFree = 0;

        if (data.has("ID"))this.ID = data.getInt("ID");
        if (data.has("pay"))this.PayType = data.getInt("pay");
        if (data.has("calc"))this.CalcType = data.getInt("calc");
        if (data.has("phone"))this.ClientPhone = data.getString("phone");
        if (data.has("cost"))this.Cost = data.getDouble("cost");
        if (data.has("state"))this.State = data.getInt("state");
        if (data.has("disp"))this.DispatchingName = data.getString("disp");
        if (data.has("dist"))this.Distance = data.getDouble("dist");
        if (data.has("check"))this.Check = data.getInt("check");
        if (data.has("note"))this.Note = data.getString("note");
        if (data.has("disp_pay"))this.DispPay = data.getString("disp_pay");
        if (data.has("main_action"))this.MainAction = data.getString("main_action");
        if (data.has("state_name"))this.StateName = data.getString("state_name");
        if (data.has("timer"))this.Timer = data.getInt("timer");
        if (data.has("parking"))this.ParkingID = data.getInt("parking");
        if (data.has("is_free"))this.IsFree = data.getInt("is_free");
        if (data.has("is_deny_penalty"))this.IsDenyPenalty = data.getBoolean("is_deny_penalty");
        if (data.has("deny_penalty_cost"))this.DenyPenaltyCost = data.getInt("deny_penalty_cost");


        if (data.has("date")){
            WorkDate = Calendar.getInstance();
            WorkDate.setTimeInMillis(Timestamp.valueOf(data.getString("date")).getTime());
        }
        else {WorkDate = null;}
        if (data.has("new_order_timer")){this.NewOrderTimer = data.getInt("new_order_timer") * 1000;}

        routePoints = new ArrayList<>();
        if (data.has("route")){
            JSONArray routePointsJSON = data.getJSONArray("route");
            for (int itemID = 0; itemID < routePointsJSON.length(); itemID++){
                RoutePoint routePoint = new RoutePoint(routePointsJSON.getJSONObject(itemID));
                routePoints.add(routePoint);
            }
        }
    }

    public Boolean IsDenyPenalty() {
        return IsDenyPenalty;
    }

    public String getDenyButtonCaption(){
        if (DenyPenaltyCost > 0){
            return  "Отказаться\n" + String.valueOf(DenyPenaltyCost)+ " " + MainApplication.getRubSymbol() + "";
        }
        else {
            return "Отказаться";
        }
    }

    public Double getCost() {
        return Cost;
    }

    public Integer getState() {
        return State;
    }

    public Integer getCheck() {
        return Check;
    }

    Integer getParkingID() {
        return ParkingID;
    }

    public Integer getID() {
        return ID;
    }


    public String getNote() {
        return Note;
    }

    public String getDispatchingName() {
        return DispatchingName;
    }

    Integer getLastRequestUID() {
        return lastRequestUID;
    }

    void setLastRequestUID(Integer lastRequestUID) {
        this.lastRequestUID = lastRequestUID;
    }



    public int getCaptionColor(){
        int result = R.color.orderFree;
        if (IsFree == 1)result = R.color.orderFreePercent;
        if (PayType == 0)result = R.color.orderCashless;
        return result;
    }

    public void fillCurOrderViewData(AppCompatActivity view){
        if (MainApplication.getInstance().getMainPreferences().getCovidLink().equals("")){
            view.findViewById(R.id.tvCurOrderCovid).setVisibility(View.GONE);
        }
        else {
            view.findViewById(R.id.tvCurOrderCovid).setVisibility(View.VISIBLE);
            view.findViewById(R.id.tvCurOrderCovid).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent webViewIntent = new Intent(MainApplication.getInstance().getMainActivity(), WebViewActivity.class);
                    Bundle webViewIntentParams = new Bundle();
                    webViewIntentParams.putString("link", MainApplication.getInstance().getMainPreferences().getCovidLink() + MainApplication.getInstance().getMainAccount().getToken());
                    webViewIntent.putExtras(webViewIntentParams);
                    MainApplication.getInstance().getMainActivity().startActivity(webViewIntent);
                }
            });
        }


        view.findViewById(R.id.llCurOrderTitle).setBackgroundResource(getCaptionColor());
        if (Note.trim().equals("")){view.findViewById(R.id.tvCurOrderNote).setVisibility(View.GONE);}
        else {
            view.findViewById(R.id.tvCurOrderNote).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.tvCurOrderNote)).setText(Note.trim());
        }
        if (ClientPhone.trim().equals("")){view.findViewById(R.id.tvCurOrderPhone).setVisibility(View.GONE);}
        else {
            view.findViewById(R.id.tvCurOrderPhone).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.tvCurOrderPhone)).setText(ClientPhone);
        }
        if (DispatchingName.trim().equals("")){view.findViewById(R.id.tvCurOrderDispatchingName).setVisibility(View.GONE);}
        else {
            view.findViewById(R.id.tvCurOrderDispatchingName).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.tvCurOrderDispatchingName)).setText(DispatchingName);
        }
        if (WorkDate == null){view.findViewById(R.id.tvCurOrderDate).setVisibility(View.GONE);}
        else {
            view.findViewById(R.id.tvCurOrderDate).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.tvCurOrderDate)).setText(getPriorInfo());
        }
        ((TextView)view.findViewById(R.id.tvCurOrderPayType)).setText(getPayTypeName());
        ((TextView)view.findViewById(R.id.tvCurOrderCalcType)).setText(getCalcType());
        ((TextView)view.findViewById(R.id.tvCurOrderPayPercent)).setText(DispPay);



    }



    public Order(JSONObject data) throws JSONException {
        setFromJSON(data);
    }

    public long getNewOrderTimer() {
        return NewOrderTimer;
    }

    public void setNewOrderTimer(long newOrderTimer) {
        NewOrderTimer = newOrderTimer;
    }

    public String getPriorInfo(){
        String result = "";
        if (WorkDate != null){
            String hour = String.valueOf(WorkDate.get(Calendar.HOUR_OF_DAY));
            if (WorkDate.get(Calendar.HOUR_OF_DAY) < 10){hour = "0" + String.valueOf(WorkDate.get(Calendar.HOUR_OF_DAY));}
            String minute = String.valueOf(WorkDate.get(Calendar.MINUTE));
            if (WorkDate.get(Calendar.MINUTE) < 10){minute = "0" + String.valueOf(WorkDate.get(Calendar.MINUTE));}
            result = hour + ":" + minute;
            if (DateTimeTools.isTomorrow(WorkDate)){result = "Завтра на " + result;}
            else if (DateTimeTools.isAfterTomorrow(WorkDate)){result = "Послезавтра на " + result;}
            else if (!DateTimeTools.isCurDate(WorkDate)){
                result = WorkDate.get(Calendar.DAY_OF_MONTH) + " " + DateTimeTools.getSklonMonthName(WorkDate) + " на " + result;
            }
            else {result = "Сегодня на " + result;}

        }
        return result;
    }

    public String getStateName() {
        return StateName;
    }

    public String getMainAction() {
        return MainAction;
    }

    public String getDispPay() {
        return DispPay;
    }



    public String getDate(){
        return new SimpleDateFormat("HH:mm dd.MM", Locale.getDefault()).format(WorkDate.getTime());
    }

    public String getTimer(){
        Integer hour = Timer / 3600;
        Integer min = (Timer - hour/3600)/60;
        Integer sek = Timer - (min * 60);
        min = min - (hour * 60);

        String s_min = ":" + String.valueOf(min);
        if (min < 10)s_min = ":0" + String.valueOf(min);
        String s_sek = ":" + String.valueOf(sek);
        if (sek < 10)s_sek = ":0" + String.valueOf(sek);

        if (hour < 10)return  "0" + String.valueOf(hour) + s_min + s_sek;
        return String.valueOf(hour) + s_min + s_sek;
    }

    public boolean isNew() {
        return IsNew;
    }

    public void setNew(boolean aNew) {
        IsNew = aNew;
    }

    public String getPayTypeName(){
        return MainApplication.getInstance().getResources().getStringArray(R.array.OrderPayType)[PayType];
    }


    public String getCalcType(){
        if (CalcType == 0){

            return new DecimalFormat("###,###.##").format(Cost) + " " + MainApplication.getRubSymbol();
        }

        return MainApplication.getInstance().getResources().getStringArray(R.array.OrderCalcType)[CalcType];
    }

    Double getDistance() {
        return Distance;
    }

    public String getDistanceString(){
        if (Distance == 0){return  "";}
        String result = "~";
        if (Distance < 1000)result += new DecimalFormat("##0").format(Distance) + " м";
        else {result += new DecimalFormat("##0.0").format(Distance/1000) + " км";}
        return result;
    }

    public LatLng getPoint(){
        if (routePoints != null)
            if (routePoints.size() > 0){
                return routePoints.get(0).getLatLng();
            }
        return null;
    }

    public String getFirstPointInfo(){
        if (routePoints != null)
            if (routePoints.size() > 0){
                return routePoints.get(0).getName();
            }
        return null;
    }

    public String getSecondPointInfo(){
        if (routePoints != null)
            if (routePoints.size() > 1){
                return routePoints.get(1).getName();
            }
        return null;
    }

    public String getLastPointInfo(){
        if (routePoints != null)
            if (routePoints.size() > 1){
                return routePoints.get(routePoints.size() - 1).getName();
            }
        return null;
    }

    public String getRoute(){
        String result = "";
        for (int itemID = 0; itemID < routePoints.size(); itemID++){
            result += routePoints.get(itemID).getName();
            if (itemID != (routePoints.size() - 1)){result += "->";}
        }
        return result;
    }

    public int getRouteCount(){
        return routePoints.size();
    }

    public RoutePoint getRoutePoint(int itemID){
        if (itemID == routePoints.size())return null;
        if (routePoints.size() < itemID)return null;
        if (routePoints.size() == 0)return null;
        return routePoints.get(itemID);
    }
}
