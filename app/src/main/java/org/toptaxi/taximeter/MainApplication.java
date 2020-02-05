package org.toptaxi.taximeter;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.data.Account;
import org.toptaxi.taximeter.data.DOT;
import org.toptaxi.taximeter.data.DOT2;
import org.toptaxi.taximeter.data.MainActionItem;
import org.toptaxi.taximeter.data.MenuItems;
import org.toptaxi.taximeter.data.Messages;
import org.toptaxi.taximeter.data.Order;
import org.toptaxi.taximeter.data.Orders;
import org.toptaxi.taximeter.data.Parkings;
import org.toptaxi.taximeter.data.Preferences;
import org.toptaxi.taximeter.services.FirebaseService;
import org.toptaxi.taximeter.services.MainService;
import org.toptaxi.taximeter.services.RestService;
import org.toptaxi.taximeter.tools.Constants;
import org.toptaxi.taximeter.tools.OnCompleteOrdersChange;
import org.toptaxi.taximeter.tools.OnMainDataChangeListener;
import org.toptaxi.taximeter.tools.OnPriorOrdersChange;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class MainApplication extends Application implements LocationListener {
    protected static String TAG = "#########" + MainApplication.class.getName();
    protected static MainApplication mainApplication;

    public static MainApplication getInstance() {
        return mainApplication;
    }

    private Integer MainActivityCurView, curViewParkingID = 0;
    private OnMainDataChangeListener onMainDataChangeListener;
    private OnPriorOrdersChange onPriorOrdersChange;
    private OnCompleteOrdersChange onCompleteOrdersChange;
    private Location mainLocation;
    private Account mainAccount;
    private Preferences mainPreferences;
    private DOT dot;
    private Orders curOrders, priorOrders, completeOrders;
    private Parkings parkings;
    MainActivity mainActivity;
    private Messages mainMessages;
    String curOrderData = "", priorOrderData = "", completeOrdersData = "";
    Integer viewOrderID;
    private Order newOrder, curOrder;
    final Handler uiHandler = new Handler(Looper.getMainLooper());
    public int lastRequestUID = 0;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Calendar ServerDate;
    private MenuItems menuItems;
    private DOT2 dot2;
    private String deviceID;
    private RestService restService;
    private FirebaseService firebaseService;
    private String curLocationName = "";

    @Override
    public void onCreate() {
        super.onCreate();
        mainApplication = this;
        MainActivityCurView = Constants.CUR_VIEW_CUR_ORDERS;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        ServerDate = Calendar.getInstance();

        // getFirebaseInstanceToken();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "onTerminate");
        if (mGoogleApiClient != null)
            if (mGoogleApiClient.isConnected())
                mGoogleApiClient.disconnect();
    }

    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        Log.d(TAG, "setGoogleApiClient");
        this.mGoogleApiClient = mGoogleApiClient;
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //mainLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    public FirebaseService getFirebaseService() {
        if (firebaseService == null){
            firebaseService = new FirebaseService();
        }
        return firebaseService;
    }

    void getFirebaseInstanceToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = Objects.requireNonNull(task.getResult()).getToken();
                        getRestService().httpGetThread("/profile/push?push_token=" + token);
                        FirebaseMessaging.getInstance().subscribeToTopic("allDevices");

                    }
                });
    }

    public RestService getRestService() {
        if (restService == null){
            restService = new RestService();
        }
        return restService;
    }

    public String getDeviceID() {
        if (deviceID == null) {
            deviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return deviceID;
    }

    public MenuItems getMenuItems() {
        if (menuItems == null)menuItems = new MenuItems();
        return menuItems;
    }

    public String getCurLocationName() {
        if (curLocationName.equals("")){
            return  getMainLocation().toString();
        }
        return curLocationName;
    }

    public void setCurLocationName(String curLocationName) {
        this.curLocationName = curLocationName;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public static String getRubSymbol(){
        String result = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = String.valueOf(Html.fromHtml("&#x20bd", Html.FROM_HTML_MODE_LEGACY)); // for 24 api and more
        } else {
            result = String.valueOf(Html.fromHtml("&#x20bd"));
        }
        if (result.trim().equals(""))result = "руб.";
        return result;
    }

    public void startMainService(){
        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(50);
        boolean run = false;
        for (int i = 0; i < rs.size(); i++) {
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            //Log.d(TAG, rsi.service.getClassName());
            if(rsi.service.getClassName().contains(getPackageName() + ".services.MainService")){
                run = true;

            }
        }

        if(!run){
            //Log.d(TAG, "Запускаем сервис.");
            startService(new Intent(this, MainService.class));
        }
    }

    public void stopMainService(){
        stopService(new Intent(this, MainService.class));

    }

    public void setOnPriorOrdersChange(OnPriorOrdersChange onPriorOrdersChange) {
        this.onPriorOrdersChange = onPriorOrdersChange;
    }

    public void setOnCompleteOrdersChange(OnCompleteOrdersChange onCompleteOrdersChange) {
        this.onCompleteOrdersChange = onCompleteOrdersChange;
    }

    public Calendar getServerDate() {
        return ServerDate;
    }

    public void parseData(JSONObject dataJSON) throws JSONException{

        if (dataJSON.has("requestUID")){lastRequestUID = dataJSON.getInt("requestUID");}
        if (dataJSON.has("account")){getMainAccount().setFromJSON(dataJSON.getJSONArray("account").getJSONObject(0));}
        if (dataJSON.has("preferences")){getMainPreferences().setFromJSON(dataJSON.getJSONArray("preferences").getJSONObject(0));}
        if (dataJSON.has("orders")){getCurOrders().setFromJSON(dataJSON.getJSONArray("orders"));}
        if (dataJSON.has("prior_orders")){
            if (!priorOrderData.equals(dataJSON.getJSONArray("prior_orders").toString())){
                priorOrderData = dataJSON.getJSONArray("prior_orders").toString();
                getPriorOrders().setFromJSONPrior(dataJSON.getJSONArray("prior_orders"));
                if ((onPriorOrdersChange != null) && (getPriorOrders() != null)){
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onPriorOrdersChange.OnPriorOrdersChange();
                        }
                    });
                }

            }
        }
        //Log.d(TAG, "parseData " + dataJSON.toString());
        if (dataJSON.has("orders_complete")){
            //Log.d(TAG, "orders_complete = " + dataJSON.getJSONArray("orders_complete").length());
            //Log.d(TAG, "orders_complete = " + dataJSON.getJSONArray("orders_complete").toString());
            if (!completeOrdersData.equals(dataJSON.getJSONArray("orders_complete").toString())){
                completeOrdersData = dataJSON.getJSONArray("orders_complete").toString();
                //Log.d(TAG, "completeOrdersData = " + completeOrdersData);
                getCompleteOrders().setFromJSONPrior(dataJSON.getJSONArray("orders_complete"));
                if ((onCompleteOrdersChange != null)){
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onCompleteOrdersChange.OnCompleteOrdersChange();
                        }
                    });
                }
            }
        } else if (onCompleteOrdersChange != null){
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    onCompleteOrdersChange.OnCompleteOrdersNull();
                }
            });
        }

        if (dataJSON.has("messages")){getMainMessages().OnNewMessages(dataJSON.getJSONArray("messages"));}
        if (dataJSON.has("hisMessages")){getMainMessages().setFromJSON(dataJSON.getJSONArray("hisMessages"));}
        if (dataJSON.has("parkingList")){getParkings().setFromJSON(dataJSON.getJSONArray("parkingList"));}
        if (dataJSON.has("parkings")){getParkings().setDataFromJSON(dataJSON.getJSONArray("parkings"));}
        if (dataJSON.has("parking")){getParkings().setSelf(dataJSON.getInt("parking"));}
        if (dataJSON.has("cur_order")){
            //Log.d(TAG, dataJSON.getString("cur_order"));
            if (!curOrderData.equals(dataJSON.getString("cur_order"))){
                getCurOrder().setFromJSON(dataJSON.getJSONArray("cur_order").getJSONObject(0));
                if ((onMainDataChangeListener != null) && (getCurOrder() != null))
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onMainDataChangeListener.OnCurOrderDataChange(getCurOrder());
                        }
                    });
                curOrderData = dataJSON.getString("cur_order");
            }
        }

        if (dataJSON.has("date")){
            ServerDate.setTimeInMillis(Timestamp.valueOf(dataJSON.getString("date")).getTime());
            //Log.d(TAG, "date = " + new SimpleDateFormat("HH:mm:ss dd.MM", Locale.getDefault()).format(ServerDate.getTime()));
        }

        if (onMainDataChangeListener != null){
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    onMainDataChangeListener.OnMainLocationChange();
                }
            });
        }
    }

    public Order getCurOrder() {
        if (curOrder == null){curOrder = new Order();}
        return curOrder;
    }

    public Parkings getParkings() {
        if (parkings == null){parkings = new Parkings();}
        return parkings;
    }

    public int getCurViewParkingID() {
        return curViewParkingID;
    }

    public void setCurViewParkingID(int curViewParkingID) {
        this.curViewParkingID = curViewParkingID;
    }

    public List<MainActionItem> getMainActions(){
        List<MainActionItem> mainActionItems = new ArrayList<>();

        if (getMainAccount().getStatus() != Constants.DRIVER_ON_ORDER){ // водитель на заказк
            if (getMainAccount().getStatus() == Constants.DRIVER_OFFLINE)mainActionItems.add(new MainActionItem(Constants.MAIN_ACTION_GO_ONLINE, "Встать на автораздачу"));
            if (getMainAccount().getStatus() == Constants.DRIVER_ONLINE)mainActionItems.add(new MainActionItem(Constants.MAIN_ACTION_GO_OFFLINE, "Сняться с автораздачи"));
            //mainActionItems.add(new MainActionItem(Constants.MAIN_ACTION_ORDER_SETTINGS, "Настройка автораздачи"));
            if (getMenuItems().getUnlim()){
                if (getMainAccount().UnlimInfo.equals(""))mainActionItems.add(new MainActionItem(Constants.MAIN_ACTION_ACTIVATE_UNLIM, "Активировать безлимит"));
            }
            mainActionItems.add(new MainActionItem(Constants.MAIN_ACTION_PRIOR_ORDER, "Предварительные заказы"));
            if (getMainPreferences().getParkingButtons())mainActionItems.add(new MainActionItem(Constants.MAIN_ACTION_PARKINGS, "Расклад по стоянкам"));
        }

        // Если водитель на заказе и есть шаблоны сообщений, то показываем шаблоны
        if ((getMainAccount().getStatus() == Constants.DRIVER_ON_ORDER) && (getMainPreferences().getTemplateMessages().size() > 0) && (MainApplication.getInstance().getMainPreferences().getDispatcherMessages())){
            for (int itemID = 0; itemID < getMainPreferences().getTemplateMessages().size(); itemID++){
                mainActionItems.add(new MainActionItem(Constants.MENU_TEMPLATE_MESSAGE, getMainPreferences().getTemplateMessages().get(itemID)));
            }

        }

        //Log.d(TAG, "getMainActions status = " + getMainAccount().getStatus() + "; action = " + getCurOrder().getMainAction() + "; menu = " + getMenuItems().getOrdersOnComplete());
        // && (getCurOrder().getMainAction().equals("set_order_done"))





        if (MainApplication.getInstance().getMainPreferences().getDispatcherMessages())
            mainActionItems.add(new MainActionItem(Constants.MAIN_ACTION_SEND_MESSAGE, "Отправить сообщение"));
        return mainActionItems;
    }

    public Order getNewOrder() {
        return newOrder;
    }

    public void setNewOrder(Order newOrder) {
        this.newOrder = newOrder;
    }

    public void setViewOrderID(Integer viewOrderID) {
        this.viewOrderID = viewOrderID;
    }

    public Order getViewOrder(){
        Order result = null;
        if (viewOrderID != null)result = getCurOrders().getByOrderID(viewOrderID);
        return result;
    }

    public Messages getMainMessages() {
        if (mainMessages == null)mainMessages = new Messages();
        return mainMessages;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    public Orders getCurOrders() {
        if (curOrders == null)curOrders = new Orders();
        return curOrders;
    }

    public Orders getPriorOrders() {
        if (priorOrders == null)priorOrders = new Orders();
        return priorOrders;
    }

    public Orders getCompleteOrders() {
        if (completeOrders == null)completeOrders = new Orders();
        return completeOrders;
    }

    public DOT getDot() {
        if (dot == null){
            dot = new DOT();
        }
        return dot;
    }

    public DOT2 getDot2() {
        if (dot2 == null){
            dot2 = new DOT2();
        }
        return dot2;
    }

    public Account getMainAccount() {
        if (mainAccount == null){
            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
            mainAccount = new Account(sPref.getString("accountToken", ""));
            //Log.d(TAG, "createMainAccount");
        }
        return mainAccount;
    }

    public Preferences getMainPreferences() {
        if (mainPreferences == null)mainPreferences = new Preferences();
        return mainPreferences;
    }

    public Location getMainLocation() {
        return mainLocation;
    }


    public String getLocationData(){
        String Data = "";
        if (mainLocation != null){
            Data += mainLocation.getLatitude() + ";";
            Data += mainLocation.getLongitude() + ";";
            Data += mainLocation.getAccuracy() + ";";
            Data += mainLocation.getSpeed() + ";";
            Data += mainLocation.getBearing() + ";";
        }
        return Data;
    }

    public void setOnMainDataChangeListener(OnMainDataChangeListener onMainDataChangeListener) {
        this.onMainDataChangeListener = onMainDataChangeListener;
        if (onMainDataChangeListener != null){
            onMainDataChangeListener.OnMainCurViewChange(MainActivityCurView);
            if (getMainAccount().getStatus() != null)startMainService();
            /*
            GetDataThread mr = new GetDataThread();
            getDataThread = new Thread(mr);
            getDataThread.start();
            */
        }
        //else getDataThread.interrupt();

    }

    public void setMainActivityCurView(Integer mainActivityCurView) {
        if (MainActivityCurView == null)return;
        if (mainActivityCurView == null)return;

        if (!mainActivityCurView.equals(MainActivityCurView)){
            MainActivityCurView = mainActivityCurView;
            if (onMainDataChangeListener != null)
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onMainDataChangeListener.OnMainCurViewChange(MainActivityCurView);
                    }
                });

        }
    }

    public int getMainActivityCurView() {
        return MainActivityCurView;
    }

    @Override
    public void onLocationChanged(Location location) {
        mainLocation = location;
        if (onMainDataChangeListener != null){onMainDataChangeListener.OnMainLocationChange();}
        //Log.d(TAG, "onLocationChanged " + mainLocation.toString());
    }

    public void showToastType(int toastType){
        String message = "";
        switch (toastType){
            case Constants.DOT_HTTP_ERROR:message = getString(R.string.errorConnection);break;
            case Constants.DOT_REST_ERROR:message = getString(R.string.errorRest);break;
            case Constants.DOT_PHONE_WRONG:message = getString(R.string.errorPhoneWrong);break;
            case Constants.DOT_PASSWORD_WRONG:message = getString(R.string.errorPasswordWrong);break;
            case Constants.DOT_IDENTIFICATION:message = getString(R.string.errorIdentification);break;
            case Constants.DOT_DRIVER_WRONG:message = getString(R.string.errorDriverWrong);break;
        }
        if (!message.equals(""))showToast(message);
    }

    public void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public int getGPSStatus(){
        int result = Constants.GPS_OFF;
        if (((LocationManager)getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            result = Constants.GPS_NOT_FIXED;
            if (mainLocation != null){
                if (mainLocation.getAccuracy() <= Constants.GPS_ACCURACY)
                    result = Constants.GPS_FIXED;
            }
        }
        return result;
    }

    public String getVersionName() {

        String versionName = null;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (final PackageManager.NameNotFoundException e) {
            Log.e(getClass().getSimpleName(), "Could not get version from manifest.");
        }
        if (versionName == null) {
            versionName = "unknown";
        }
        return versionName;
    }

    public String getVersionCode() {

        String versionName = null;
        try {
            Integer Code = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            versionName = Code.toString();
        }
        catch (final PackageManager.NameNotFoundException e) {
            Log.e(getClass().getSimpleName(), "Could not get version from manifest.");
        }
        if (versionName == null) {
            versionName = "unknown";
        }
        return versionName;
    }

    /// ***********************************
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    public static boolean isJSONArrayHaveValue(JSONArray jsonArray, String value){
        for (int itemID = 0; itemID < jsonArray.length(); itemID++){
            try {
                if (jsonArray.getString(itemID).equals(value)){
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
