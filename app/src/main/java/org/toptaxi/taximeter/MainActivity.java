package org.toptaxi.taximeter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.activities.BalanceActivity;
import org.toptaxi.taximeter.activities.HisOrdersActivity;
import org.toptaxi.taximeter.activities.MessagesActivity;
import org.toptaxi.taximeter.activities.OrdersOnCompleteActivity;
import org.toptaxi.taximeter.activities.SettingsActivity;
import org.toptaxi.taximeter.activities.ShareDriverActivity;
import org.toptaxi.taximeter.activities.SplashActivity;
import org.toptaxi.taximeter.activities.StatisticsActivity;
import org.toptaxi.taximeter.activities.WebViewActivity;
import org.toptaxi.taximeter.adapters.MainActionAdapter;
import org.toptaxi.taximeter.adapters.MainActionUnlimAdapter;
import org.toptaxi.taximeter.adapters.OnMainActionClickListener;
import org.toptaxi.taximeter.adapters.OnMainActionUnlimClickListener;
import org.toptaxi.taximeter.adapters.RVCurOrdersAdapter;
import org.toptaxi.taximeter.adapters.RecyclerItemClickListener;
import org.toptaxi.taximeter.adapters.RoutePointsAdapter;
import org.toptaxi.taximeter.data.Account;
import org.toptaxi.taximeter.data.Order;
import org.toptaxi.taximeter.data.Orders;
import org.toptaxi.taximeter.data.Parking;
import org.toptaxi.taximeter.data.RoutePoint;
import org.toptaxi.taximeter.data.UnlimTariff;
import org.toptaxi.taximeter.dialogs.UserAgreementDialog;
import org.toptaxi.taximeter.tools.Constants;
import org.toptaxi.taximeter.tools.FontFitTextView;
import org.toptaxi.taximeter.tools.LockOrientation;
import org.toptaxi.taximeter.tools.OnMainDataChangeListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMainDataChangeListener, Orders.OnOrdersChangeListener, Drawer.OnDrawerItemClickListener, Account.OnAccountChangeListener, OnMapReadyCallback {
    private static String TAG = "#########" + MainActivity.class.getName();
    FrameLayout viewCurOrders, viewTaximeter, viewViewOrder, viewCurOrder, viewGPSError, viewMainData, viewParkings;
    TextView tvGPSStatus, tvNullCurOrderInfo;
    Button btnCurOrderMainAction, btnCurOrderAction, btnCompleteOrders;
    private boolean isShowGPSData = false;
    protected Toolbar mToolbar;
    MenuItem miGPSFixed, miGPSNotFixed, miGPSOff, miDriverOffline, miDriverOnOrder, miDriverOnLine;
    RVCurOrdersAdapter curOrdersAdapter;
    RoutePointsAdapter viewOrderPointsAdapter, curOrderPointsAdapter;
    public AlertDialog mainActionsDialog, mainActionsUnlimDialog;
    GoogleMap googleMap;
    FontFitTextView tvCurOrderClientCost;


    protected AccountHeader accountHeader;
    protected Drawer drawer;
    PrimaryDrawerItem balanceItem, themeItem, messagesItem, unlimInfo;
    IProfile profile;
    int viewOrderLastState;
    MediaPlayer mpOrderStateChange;
    RecyclerView rvCurOrders;
    FloatingActionButton fabMainActions;

    static {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Утановка текущего поворота экрана
        new LockOrientation(this).lock();
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        viewCurOrders = (FrameLayout)findViewById(R.id.curOrders);
        viewTaximeter = (FrameLayout)findViewById(R.id.taximeter);
        viewViewOrder = (FrameLayout)findViewById(R.id.flViewOrder);
        viewCurOrder  = (FrameLayout)findViewById(R.id.flCurOrder);
        viewGPSError  = (FrameLayout)findViewById(R.id.flGPSError);
        viewMainData  = (FrameLayout)findViewById(R.id.flMainData);
        viewParkings  = (FrameLayout)findViewById(R.id.flParkings);

        btnCurOrderMainAction   = (Button)findViewById(R.id.btnCurOrderMainAction);
        btnCurOrderAction       = (Button)findViewById(R.id.btnCurOrderAction);
        btnCompleteOrders       = (Button)findViewById(R.id.btnCurOrderCompleteOrders);


        btnCompleteOrders.setVisibility(View.GONE);
        tvGPSStatus = (TextView)findViewById(R.id.tvGPSInfo);
        tvNullCurOrderInfo = (TextView)findViewById(R.id.tvNullCurOrderInfo);
        tvNullCurOrderInfo.setVisibility(View.GONE);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        fabMainActions = (FloatingActionButton) findViewById(R.id.fabMainActions);
        fabMainActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMainActionsClick();
            }
        });

        rvCurOrders = (RecyclerView)findViewById(R.id.rvCurOrders);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvCurOrders.setLayoutManager(llm);
        curOrdersAdapter = new RVCurOrdersAdapter(0);
        rvCurOrders.setAdapter(curOrdersAdapter);
        rvCurOrders.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        MainApplication.getInstance().setViewOrderID(MainApplication.getInstance().getCurOrders().getOrderID(position));
                        MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_VIEW_ORDER);
                    }
                })
        );

        RecyclerView rvViewOrderRoutePoints = (RecyclerView)findViewById(R.id.rvViewOrderRoutePoints);
        rvViewOrderRoutePoints.setLayoutManager(new LinearLayoutManager(this));
        viewOrderPointsAdapter = new RoutePointsAdapter();
        rvViewOrderRoutePoints.setAdapter(viewOrderPointsAdapter);

        RecyclerView rvCurOrderRoutePoints = (RecyclerView)findViewById(R.id.rvCurOrderRoutePoints);
        rvCurOrderRoutePoints.setLayoutManager(new LinearLayoutManager(this));
        curOrderPointsAdapter = new RoutePointsAdapter();
        rvCurOrderRoutePoints.setAdapter(curOrderPointsAdapter);
        rvCurOrderRoutePoints.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        //Log.d(TAG, "on rvCurOrderRoutePoints Click");
                        final RoutePoint routePoint = MainApplication.getInstance().getCurOrder().getRoutePoint(position);
                        final CharSequence[] items = {"Показать маршрут"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Выберите действие");
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {

                                if (item == 0){
                                    //Uri uri = Uri.parse("dgis://");
                                    Uri uri = Uri.parse("dgis://2gis.ru/routeSearch/rsType/car/to/" + routePoint.getLongitude() + "," + routePoint.getLatitude());
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    List<ResolveInfo> activities = getPackageManager().queryIntentActivities(intent, 0);

                                    boolean isIntentSafe = activities.size() > 0;

                                    if (isIntentSafe) { //Если приложение установлено — запускаем его

                                        startActivity(intent);

                                    } else { // Если не установлено — переходим в Google Play.

                                        intent = new Intent(Intent.ACTION_VIEW);

                                        intent.setData(Uri.parse("market://details?id=ru.dublgis.dgismobile"));

                                        startActivity(intent);

                                    }

                                }

                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }
                })
        );

        tvCurOrderClientCost = (FontFitTextView)findViewById(R.id.tvCurOrderClientPrice);
        (findViewById(R.id.tvCurOrderPhone)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callIntent(((TextView)findViewById(R.id.tvCurOrderPhone)).getText().toString());
            }
        });

        mpOrderStateChange = MediaPlayer.create(this, R.raw.order_state_change);
        mpOrderStateChange.setLooping(false);

        // ViewOrderData
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapViewOrder);
        mapFragment.getMapAsync(this);

        btnCompleteOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, OrdersOnCompleteActivity.class));
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.MY_PERMISSIONS_REQUEST_LOCATION){
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Для продолжения работы необходимо дать разрешение на доступ к GPS данным", Toast.LENGTH_LONG).show();
                finish();
            }
            else init();
        }

    }

    public void init(){
        if (MainApplication.getInstance().getMainAccount().getStatus() != null){
            MainApplication.getInstance().setOnMainDataChangeListener(this);
            MainApplication.getInstance().getCurOrders().setOnOrdersChangeListener(this);
            MainApplication.getInstance().getMainAccount().setOnAccountChangeListener(this);
            MainApplication.getInstance().setMainActivity(this);
            if (MainApplication.getInstance().getMainPreferences().checkCurVersion()){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage(getString(R.string.checkNewVersionCaption));
                alertDialog.setPositiveButton(getString(R.string.checkNewVersionOk), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=org.toptaxi.taximeter"));
                        startActivity(intent);
                        finish();
                    }
                });
                alertDialog.setNegativeButton(getString(R.string.checkNewVersionCancel) , null);
                alertDialog.create();
                alertDialog.show();
                MainApplication.getInstance().getMainPreferences().setCheckCurVersion();
            }
            // Проверяем на принятие пользовательского соглашение
            else if (MainApplication.getInstance().getMainAccount().IsShowAgreement()){
                Log.d(TAG, "new agreement link");
                UserAgreementDialog userAgreementDialog = new UserAgreementDialog(this);
                userAgreementDialog.show();
            }
            OnMainLocationChange();
        }
        else {
            Log.d(TAG, "startSplashActivity");
            Intent splashIntent = new Intent(MainActivity.this, SplashActivity.class);
            startActivityForResult(splashIntent, Constants.ACTIVITY_SPLASH);
        }

    }

    public void callIntent(String phone){
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume");
        // Проверяем на доступ к GPS Данным
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.MY_PERMISSIONS_REQUEST_LOCATION);
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
        else init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode = " + requestCode + "; resultCode = " + resultCode);
        if (requestCode == Constants.ACTIVITY_SPLASH){
            if (resultCode == RESULT_CANCELED)finish();
            else {
                Log.d(TAG, "onActivityResult запускаем");
                generateDrawer();
                generateParkingButton();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        }
        else if (MainApplication.getInstance().getMainActivityCurView() == Constants.CUR_VIEW_TAXIMETER){
            MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_CUR_ORDERS);
        }
        else if (MainApplication.getInstance().getMainActivityCurView() == Constants.CUR_VIEW_VIEW_ORDER) {
            MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_CUR_ORDERS);
        }
        else if (MainApplication.getInstance().getMainActivityCurView() == Constants.CUR_VIEW_PARKINGS) {
            MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_CUR_ORDERS);
        }
        else if (MainApplication.getInstance().getCurViewParkingID() != 0){
            MainApplication.getInstance().setCurViewParkingID(0);
            MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_PARKINGS);
            //OnOrdersChange();
        }
        else {
            if (MainApplication.getInstance().getMainAccount().getStatus() == Constants.DRIVER_ON_ORDER)MainApplication.getInstance().showToast(getString(R.string.onCloseDriverOnOrder));
            else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Внимание");
                alertDialog.setMessage("При закрытие программы Вы будете сняты с линии");
                alertDialog.setPositiveButton("Сняться", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainApplication.getInstance().stopMainService();
                        finish();
                    }
                });
                alertDialog.setNegativeButton("Остаться" , null);
                alertDialog.create();
                alertDialog.show();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        MainApplication.getInstance().setOnMainDataChangeListener(null);
        MainApplication.getInstance().setMainActivity(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        miGPSFixed      = menu.findItem(R.id.action_gps_fixed);
        miGPSNotFixed   = menu.findItem(R.id.action_gps_not_fixed);
        miGPSOff        = menu.findItem(R.id.action_gps_off);
        miDriverOffline = menu.findItem(R.id.action_driver_offline);
        miDriverOnLine  = menu.findItem(R.id.action_driver_online);
        miDriverOnOrder = menu.findItem(R.id.action_driver_on_order);




        miGPSFixed.setVisible(false);
        miGPSNotFixed.setVisible(false);
        miGPSOff.setVisible(false);
        switch (MainApplication.getInstance().getGPSStatus()){
            case Constants.GPS_FIXED:miGPSFixed.setVisible(true);break;
            case Constants.GPS_NOT_FIXED:miGPSNotFixed.setVisible(true);break;
            case Constants.GPS_OFF:miGPSOff.setVisible(true);break;
        }

        miDriverOffline.setVisible(false);
        miDriverOnLine.setVisible(false);
        miDriverOnOrder.setVisible(false);
        miDriverOffline.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_gps_fixed:isShowGPSData = !isShowGPSData;break;
            case R.id.action_gps_not_fixed:isShowGPSData = !isShowGPSData;break;
            case R.id.action_gps_off:isShowGPSData = !isShowGPSData;break;
            case R.id.action_driver_offline:driverGoOffLine();break;
            case R.id.action_driver_online:driverGoOffLine();break;
            default: return super.onOptionsItemSelected(item);
        }
        if (isShowGPSData)tvGPSStatus.setVisibility(View.VISIBLE);
        else tvGPSStatus.setVisibility(View.GONE);
        return super.onOptionsItemSelected(item);
    }

    public void driverGoOffLine(){
        if (!MainApplication.getInstance().getMainAccount().getOnLine()){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(getString(R.string.dlgAlert));
            alertDialog.setMessage(getString(R.string.errorDriverBalanceOnLine));
            alertDialog.setPositiveButton(getString(R.string.dlgButtonCaptionOK), null);
            alertDialog.create();
            alertDialog.show();
        }
        else {
            String alertText = "", action = "";
            switch (MainApplication.getInstance().getMainAccount().getStatus()){
                case Constants.DRIVER_OFFLINE:alertText = "Встать на автораздачу?";action = "driver_on_line";break;
                case Constants.DRIVER_ONLINE:alertText = "Сняться с автораздачи?";action = "driver_off_line";break;
            }
            if (!alertText.equals("")){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Внимание");
                alertDialog.setMessage(alertText);
                final String finalAction = action;
                alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainApplication.getInstance().getDot().sendDataResult(finalAction, "");
                    }
                });
                alertDialog.setNegativeButton("Нет" , null);
                alertDialog.create();
                alertDialog.show();

            }

        }


    }

    @Override
    public void OnMainLocationChange() {
        int GPSState = MainApplication.getInstance().getGPSStatus();
        if (miGPSFixed != null){
            miGPSFixed.setVisible(false);
            miGPSNotFixed.setVisible(false);
            miGPSOff.setVisible(false);
            switch (GPSState){
                case Constants.GPS_FIXED:miGPSFixed.setVisible(true);break;
                case Constants.GPS_NOT_FIXED:miGPSNotFixed.setVisible(true);break;
                case Constants.GPS_OFF:miGPSOff.setVisible(true);break;
            }
        }

        if (GPSState == Constants.GPS_OFF){
            tvGPSStatus.setText(getString(R.string.errorGPSOff));
            viewMainData.setVisibility(View.GONE);
            viewGPSError.setVisibility(View.VISIBLE);
            fabMainActions.setVisibility(View.GONE);
            ((TextView)findViewById(R.id.tvGPSErrorInfo)).setText(getString(R.string.errorGPSOff));
        }

        else if (MainApplication.getInstance().getLocationData().equals("")){
            tvGPSStatus.setText(getString(R.string.errorGPSNotFixed));
            viewMainData.setVisibility(View.VISIBLE);
            viewGPSError.setVisibility(View.GONE);
            fabMainActions.setVisibility(View.VISIBLE);

        }
        else {
            viewMainData.setVisibility(View.VISIBLE);
            viewGPSError.setVisibility(View.GONE);
            //fabMainActions.setVisibility(View.VISIBLE);
            tvGPSStatus.setText(MainApplication.getInstance().getCurLocationName());
            //if (!MainApplication.getInstance().getCurPlaceName().equals(""))tvGPSStatus.setText(MainApplication.getInstance().getCurPlaceName());
            //else tvGPSStatus.setText(MainApplication.getInstance().getLocationData());
        }
    }


    @Override
    public void OnMainCurViewChange(int curViewType) {
        viewCurOrders.setVisibility(View.GONE);
        viewTaximeter.setVisibility(View.GONE);
        viewViewOrder.setVisibility(View.GONE);
        viewCurOrder.setVisibility(View.GONE);
        viewParkings.setVisibility(View.GONE);
        fabMainActions.setVisibility(View.GONE);

        switch (curViewType){
            case Constants.CUR_VIEW_CUR_ORDERS:viewCurOrders.setVisibility(View.VISIBLE);fabMainActions.setVisibility(View.VISIBLE);break;
            case Constants.CUR_VIEW_TAXIMETER:viewTaximeter.setVisibility(View.VISIBLE);break;
            case Constants.CUR_VIEW_PARKINGS:viewParkings.setVisibility(View.VISIBLE);break;
            case Constants.CUR_VIEW_VIEW_ORDER:
                viewViewOrder.setVisibility(View.VISIBLE);
                viewOrderLastState = MainApplication.getInstance().getViewOrder().getCheck();
                generateViewOrder();
                break;
            case Constants.CUR_VIEW_CUR_ORDER:
                viewCurOrder.setVisibility(View.VISIBLE);
                //viewOrderLastState = MainApplication.getInstance().getViewOrder().Check;
                viewOrderLastState = -1;
                generateCurOrder();
                break;
        }
        this.setTitle(MainApplication.getInstance().getMainAccount().getMainActivityCaption());
    }

    @Override
    public void OnCurOrderDataChange(Order curOrder) {
        generateCurOrder();
    }

    public void fabMainActionsClick(){
        MainActionAdapter mainActionAdapter = new MainActionAdapter(this, R.layout.main_action_item, MainApplication.getInstance().getMainActions());
        ListView listViewItems = new ListView(this);
        listViewItems.setAdapter(mainActionAdapter);
        listViewItems.setOnItemClickListener(new OnMainActionClickListener());

        // put the ListView in the pop up
        mainActionsDialog = new AlertDialog.Builder(MainActivity.this)
                .setView(listViewItems)
                .show();
    }

    public void onGetUnlimTariffClick(){
        if ((MainApplication.getInstance().getMainAccount().getStatus() != Constants.DRIVER_ON_ORDER) && (MainApplication.getInstance().getMainAccount().UnlimInfo.equals("")))
            new GetUnlimInfoTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class GetUnlimInfoTask extends AsyncTask<Void, Void, String>{
        ProgressDialog progressDialog;
        Context mContext;
        GetUnlimInfoTask(Context context){
            mContext = context;
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Получение списка безлимитов ...");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            return MainApplication.getInstance().getDot().getDataType("unlim_tariff", "");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            try {
                JSONObject response = new JSONObject(s);
                if (response.getString("response").equals("ok")){
                    List<UnlimTariff> unlimTariffs = new ArrayList<>();
                    JSONArray data = response.getJSONArray("unlim");
                    for (int itemID = 0; itemID < data.length(); itemID++){
                        UnlimTariff unlimTariff = new UnlimTariff(data.getJSONObject(itemID));
                        unlimTariffs.add(unlimTariff);
                        //Log.d(TAG, "onPostExecute" + unlimTariff.getName());
                    }


                    MainActionUnlimAdapter mainActionAdapter = new MainActionUnlimAdapter(mContext, R.layout.main_action_unlim_info, unlimTariffs);
                    ListView listViewItems = new ListView(mContext);
                    listViewItems.setAdapter(mainActionAdapter);
                    listViewItems.setOnItemClickListener(new OnMainActionUnlimClickListener());

                    // put the ListView in the pop up
                    mainActionsUnlimDialog = new AlertDialog.Builder(MainActivity.this)
                            .setView(listViewItems)
                            .show();



                }
                else MainApplication.getInstance().showToastType(Constants.DOT_HTTP_ERROR);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void OnOrdersChange() {
        if (MainApplication.getInstance().getCurViewParkingID() != 0){
            findViewById(R.id.tvCurViewParkingName).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.tvCurViewParkingName)).setText(String.valueOf("Заказы по стоянке: " + MainApplication.getInstance().getParkings().getName(MainApplication.getInstance().getCurViewParkingID())));
        }
        else {
            findViewById(R.id.tvCurViewParkingName).setVisibility(View.GONE);
        }
        if (MainApplication.getInstance().getCurOrders().getCount() == 0){
            tvNullCurOrderInfo.setVisibility(View.VISIBLE);
            rvCurOrders.setVisibility(View.GONE);
        }
        else {
            tvNullCurOrderInfo.setVisibility(View.GONE);
            rvCurOrders.setVisibility(View.VISIBLE);
            rvCurOrders.getRecycledViewPool().clear();
            curOrdersAdapter.notifyItemRangeInserted(0, MainApplication.getInstance().getCurOrders().getCount());
            curOrdersAdapter.notifyDataSetChanged();
        }

        if (MainApplication.getInstance().getMainActivityCurView() == Constants.CUR_VIEW_VIEW_ORDER)generateViewOrder();
        if (MainApplication.getInstance().getMainActivityCurView() == Constants.CUR_VIEW_CUR_ORDER)generateCurOrder();
    }

    public void generateDrawer(){

        profile = new ProfileDrawerItem()
                .withName(MainApplication.getInstance().getMainAccount().getName())
                .withEmail(MainApplication.getInstance().getMainAccount().getSerName())
                .withIcon(getResources().getDrawable(R.mipmap.contact_default));
                //.withIcon(ContextCompat.getDrawable(this, R.mipmap.contact_default));

        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.mipmap.header)
                .addProfiles(profile)
                .withCompactStyle(true)
                .withTextColor(ContextCompat.getColor(this, R.color.account_header_text))
                .withSelectionListEnabledForSingleProfile(false)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        Log.d(TAG, "Account item click");
                        //Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        //startActivity(intent);
                        return false;
                    }
                })
                .build();


        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withAccountHeader(accountHeader)
                .withOnDrawerItemClickListener(this)
                .build();

        balanceItem = new PrimaryDrawerItem().withName("Баланс").withIcon(FontAwesome.Icon.faw_rub).withSelectable(false).withBadge(MainApplication.getInstance().getMainAccount().getBalance()).withIdentifier(Constants.MENU_BALANCE);
        drawer.addItem(balanceItem);
        if (MainApplication.getInstance().getMenuItems().getUnlim()){
            unlimInfo = new PrimaryDrawerItem().withName(MainApplication.getInstance().getMainAccount().UnlimInfo).withIcon(FontAwesome.Icon.faw_fire).withSelectable(false).withIdentifier(Constants.MENU_ACITVATE_UNLIM);
            drawer.addItem(unlimInfo);
        }


        drawer.addItem(new DividerDrawerItem());
        //drawer.addItem(new PrimaryDrawerItem().withName("Таксометр").withIcon(FontAwesome.Icon.faw_taxi).withSelectable(false).withIdentifier(Constants.MENU_TAXIMETER));


        if (MainApplication.getInstance().getMenuItems().getMessages()){
            messagesItem = new PrimaryDrawerItem().withName("Сообщения диспетчеру").withIcon(FontAwesome.Icon.faw_commenting_o).withSelectable(false).withBadge(String.valueOf(MainApplication.getInstance().getMainAccount().getNotReadMessageCount())).withIdentifier(Constants.MENU_MESSAGES);
            drawer.addItem(messagesItem);
        }


        drawer.addItem(new PrimaryDrawerItem().withName("Статистика|Рейтинг").withIcon(FontAwesome.Icon.faw_cube).withSelectable(false).withIdentifier(Constants.MENU_STATISTICS));
        if (MainApplication.getInstance().getMainPreferences().getFriends()){
            drawer.addItem(new PrimaryDrawerItem().withName("Пригласить друга").withIcon(FontAwesome.Icon.faw_share_alt).withSelectable(false).withIdentifier(Constants.MENU_SHARE_DRIVER));
        }
        if (!MainApplication.getInstance().getMainPreferences().getClientsFriendsText().equals("")){
            drawer.addItem(new PrimaryDrawerItem().withName("Посоветовать клиенту").withIcon(FontAwesome.Icon.faw_share).withSelectable(false).withIdentifier(Constants.MENU_SHARE_CLIENT));
        }

        drawer.addItem(new PrimaryDrawerItem().withName(getString(R.string.drawerHisOrders)).withIcon(FontAwesome.Icon.faw_history).withSelectable(false).withIdentifier(Constants.MENU_HIS_ORDERS));



        drawer.addItem(new DividerDrawerItem());
        if (!MainApplication.getInstance().getMainPreferences().getFaqLink().equals("")){
            drawer.addItem(new PrimaryDrawerItem().withName("Как пополнить баланс").withIcon(FontAwesome.Icon.faw_question).withSelectable(false).withIdentifier(Constants.MENU_FAQ));

        }

        if (!MainApplication.getInstance().getMainPreferences().getDispatcherPhone().equals("")){
            drawer.addItem(new PrimaryDrawerItem().withName("Позвонить диспетчеру").withIcon(FontAwesome.Icon.faw_phone).withSelectable(false).withIdentifier(Constants.MENU_CALL_DISPATCHING));
        }
        if (!MainApplication.getInstance().getMainPreferences().getAdministrationCallPhone().equals("")){
            drawer.addItem(new PrimaryDrawerItem().withName("Позвонить в администрацию").withIcon(FontAwesome.Icon.faw_phone).withSelectable(false).withIdentifier(Constants.MENU_CALL_ADMINISTRATION));
        }
        drawer.addItem(new PrimaryDrawerItem().withName("Настройки").withIcon(FontAwesome.Icon.faw_cog).withSelectable(false).withIdentifier(Constants.MENU_SETTINGS));
        themeItem = new PrimaryDrawerItem().withName(MainApplication.getInstance().getMainPreferences().getThemeName()).withSelectable(false).withIcon(FontAwesome.Icon.faw_exchange).withIdentifier(Constants.MENU_THEME);
        drawer.addItem(themeItem);
        drawer.addItem(new DividerDrawerItem());
        drawer.addItem(new PrimaryDrawerItem().withName("ver. " + MainApplication.getInstance().getVersionName()).withIcon(FontAwesome.Icon.faw_creative_commons).withEnabled(false).withSelectable(false));

    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        switch ((int) drawerItem.getIdentifier()){
            case Constants.MENU_TAXIMETER:
                if (MainApplication.getInstance().getMainAccount().getStatus() != 3){
                    MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_TAXIMETER);
                }
                break;
            case Constants.MENU_THEME:
                    MainApplication.getInstance().getMainPreferences().changeTheme();
                    getDelegate().setLocalNightMode(MainApplication.getInstance().getMainPreferences().getTheme());
                    recreate();
                    getApplication().setTheme(R.style.AppTheme);
                    themeItem.withName(MainApplication.getInstance().getMainPreferences().getThemeName());
                    drawer.updateItem(themeItem);
                break;
            case Constants.MENU_ACITVATE_UNLIM:onGetUnlimTariffClick();break;
            case Constants.MENU_MESSAGES:
                Intent messagesInten = new Intent(MainActivity.this, MessagesActivity.class);
                startActivity(messagesInten);
                break;
            case Constants.MENU_BALANCE:
                Intent balanceInten = new Intent(MainActivity.this, BalanceActivity.class);
                startActivity(balanceInten);
                break;
            case Constants.MENU_STATISTICS:
                Intent statisticsIntent = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(statisticsIntent);
                break;
            case Constants.MENU_SHARE_DRIVER:
                Intent shareDriverIntent = new Intent(MainActivity.this, ShareDriverActivity.class);
                startActivity(shareDriverIntent);
                break;
            case Constants.MENU_SETTINGS:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case Constants.MENU_CALL_ADMINISTRATION:callIntent(MainApplication.getInstance().getMainPreferences().getAdministrationCallPhone());break;
            case Constants.MENU_CALL_DISPATCHING:callIntent(MainApplication.getInstance().getMainPreferences().getDispatcherPhone());break;
            case Constants.MENU_HIS_ORDERS:startActivity(new Intent(MainActivity.this, HisOrdersActivity.class));break;
            case Constants.MENU_FAQ:
                Uri address = Uri.parse(MainApplication.getInstance().getMainPreferences().getFaqLink());
                Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
                startActivity(openlinkIntent);
                break;
            case Constants.MENU_SHARE_CLIENT:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, MainApplication.getInstance().getMainPreferences().getClientsFriendsText());
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                break;


        }
        return false;
    }

    @Override
    public void OnAccountChange() {
        if (miDriverOffline != null){
            miDriverOffline.setVisible(false);
            miDriverOnLine.setVisible(false);
            miDriverOnOrder.setVisible(false);
            switch (MainApplication.getInstance().getMainAccount().getStatus()){
                case Constants.DRIVER_OFFLINE:miDriverOffline.setVisible(true);break;
                case Constants.DRIVER_ONLINE:miDriverOnLine.setVisible(true);break;
                case Constants.DRIVER_ON_ORDER:miDriverOnOrder.setVisible(true);break;
            }
        }
        if (drawer != null){
            balanceItem.withBadge(MainApplication.getInstance().getMainAccount().getBalance());
            drawer.updateItem(balanceItem);
            profile.withName(MainApplication.getInstance().getMainAccount().getName());
            profile.withEmail(MainApplication.getInstance().getMainAccount().getSerName());
            accountHeader.updateProfile(profile);
            if (messagesItem != null){
                messagesItem.withBadge(MainApplication.getInstance().getMainAccount().getNotReadMessageCount());
                drawer.updateItem(messagesItem);
            }

            if ((MainApplication.getInstance().getMenuItems().getUnlim()) && (unlimInfo != null)){
                if (MainApplication.getInstance().getMainAccount().UnlimInfo.equals(""))
                    unlimInfo.withName("На процентах");
                else unlimInfo.withName(MainApplication.getInstance().getMainAccount().UnlimInfo);
                drawer.updateItem(unlimInfo);
            }
        }
        else generateDrawer();


    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (MainApplication.getInstance().getMainActivityCurView() == Constants.CUR_VIEW_VIEW_ORDER)generateViewOrder();
    }

    private void generateCurOrder(){
        Order viewOrder = MainApplication.getInstance().getCurOrder();
        if (viewOrder.getState() != null){
            btnCompleteOrders.setVisibility(View.GONE);
            //findViewById(R.id.llCurOrderTitleEx).setBackgroundResource(viewOrder.getCaptionColor());
            findViewById(R.id.btnOrderAction).setVisibility(View.GONE);
            viewOrder.fillCurOrderViewData(this);
            curOrderPointsAdapter.setOrder(viewOrder);
            curOrderPointsAdapter.notifyItemRangeInserted(0, MainApplication.getInstance().getCurOrder().getRouteCount());
            curOrderPointsAdapter.notifyDataSetChanged();
            tvCurOrderClientCost.setText(new DecimalFormat("###,###.0").format(viewOrder.getCost()));
            if (viewOrderLastState != viewOrder.getState()){
                mpOrderStateChange.start();
                viewOrderLastState = viewOrder.getState();
            }


            ((TextView)findViewById(R.id.tvCurOrderStateName)).setText(viewOrder.getStateName());
            ((TextView)findViewById(R.id.tvCurOrderTimer)).setText(viewOrder.getTimer());

            switch (viewOrder.getMainAction()){
                case "apply_deny":
                    fabMainActions.setVisibility(View.GONE);
                    btnCurOrderMainAction.setVisibility(View.VISIBLE);
                    btnCurOrderMainAction.setText("Принять");
                    btnCurOrderMainAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            MainApplication.getInstance().getDot().sendDataResult("driver_apply_order", "");
                            }
                        });

                    btnCurOrderAction.setVisibility(View.VISIBLE);
                    btnCurOrderAction.setText("Отказаться");

                    if (viewOrder.IsDenyPenalty()){
                        Log.d(TAG, "view cur order with penalty");
                        btnCurOrderAction.setBackgroundResource(R.drawable.btn_red);
                        btnCurOrderAction.setText(viewOrder.getDenyButtonCaption());
                        //Log.d(TAG, "viewOrder.getDenyButtonCaption() = " + viewOrder.getDenyButtonCaption());
                    }
                    else {
                        Log.d(TAG, "view cur order without penalty");
                        btnCurOrderAction.setBackgroundResource(R.drawable.btn_yellow);
                    }

                    btnCurOrderAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            MainApplication.getInstance().getDot().sendDataResult("driver_deny_order", "");
                            }
                        });
                    fabMainActions.setVisibility(View.GONE);
                    break;
                case "set_driver_at_client":
                    btnCurOrderMainAction.setVisibility(View.VISIBLE);
                    btnCurOrderMainAction.setText("Подъехал");
                    btnCurOrderAction.setVisibility(View.GONE);
                    btnCurOrderMainAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            MainApplication.getInstance().getDot().sendDataResult("set_driver_at_client", "");
                            }
                        });
                    fabMainActions.setVisibility(View.VISIBLE);
                    break;
                case "set_client_in_car":
                    btnCurOrderMainAction.setVisibility(View.VISIBLE);
                    btnCurOrderMainAction.setText("Клиент в машине");
                    btnCurOrderAction.setVisibility(View.GONE);
                    btnCurOrderMainAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            MainApplication.getInstance().getDot().sendDataResult("set_client_in_car", "");
                            }
                        });
                    fabMainActions.setVisibility(View.VISIBLE);
                    break;
                case "set_order_done":
                    if (MainApplication.getInstance().getCompleteOrders().getCount() > 0)btnCompleteOrders.setVisibility(View.VISIBLE);
                    else btnCompleteOrders.setVisibility(View.GONE);
                    btnCurOrderMainAction.setVisibility(View.VISIBLE);
                    btnCurOrderMainAction.setText("Выполнил");
                    btnCurOrderAction.setVisibility(View.GONE);
                    btnCurOrderMainAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            MainApplication.getInstance().getDot().sendDataResult("set_order_done", "");
                            }
                        });
                    fabMainActions.setVisibility(View.GONE);
                    break;
            }
        }

    }

    private void generateViewOrder(){
        final Order viewOrder = MainApplication.getInstance().getViewOrder();
        if (viewOrder == null){
            mpOrderStateChange.start();
            MainApplication.getInstance().showToast("Заказ забрал другой водитель");
            MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_CUR_ORDERS);

        }
        else {
            LatLng coord = viewOrder.getPoint();
            if (coord != null){
                if (googleMap != null){
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(coord));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coord, 15));
                }
            }
            findViewById(R.id.llViewOrderTitle).setBackgroundResource(viewOrder.getCaptionColor());
            ((TextView)findViewById(R.id.tvViewOrderDistance)).setText(viewOrder.getDistanceString());
            ((TextView)findViewById(R.id.tvViewOrderPayType)).setText(viewOrder.getPayTypeName());
            ((TextView)findViewById(R.id.tvViewOrderCalcType)).setText(viewOrder.getCalcType());
            ((TextView)findViewById(R.id.tvViewOrderDispatchingName)).setText(viewOrder.getDispatchingName());
            ((TextView)findViewById(R.id.tvViewOrderPayPercent)).setText(viewOrder.getDispPay());


            findViewById(R.id.btnOrderAction).setVisibility(View.GONE);
            viewOrderPointsAdapter.setOrder(MainApplication.getInstance().getViewOrder());
            viewOrderPointsAdapter.notifyItemRangeInserted(0, MainApplication.getInstance().getViewOrder().getRouteCount());
            viewOrderPointsAdapter.notifyDataSetChanged();
            Button btnOrderMainAction = (Button)findViewById(R.id.btnOrderMainAction);
            switch (viewOrder.getCheck()){
                case 0:btnOrderMainAction.setText("Взять заказ");
                    btnOrderMainAction.setEnabled(true);
                    btnOrderMainAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                            alertDialog.setTitle("Внимание");
                            alertDialog.setMessage("Принять данный заказ?");
                            alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_CUR_ORDERS);
                                    MainApplication.getInstance().getDot().sendDataResult("check_order", String.valueOf(viewOrder.getID()));
                                }
                            });
                            alertDialog.setNegativeButton("Нет" , null);
                            alertDialog.create();
                            alertDialog.show();

                        }
                    });
                    break;
                case 1:btnOrderMainAction.setText("Заказ на автораздаче");btnOrderMainAction.setEnabled(false);break;
                case 2:btnOrderMainAction.setText("Заказ Вам не доступен");btnOrderMainAction.setEnabled(false);break;
            }
            if (viewOrderLastState != viewOrder.getCheck()){
                mpOrderStateChange.start();
                viewOrderLastState = viewOrder.getCheck();
            }

            if (viewOrder.getPriorInfo().equals("")){(findViewById(R.id.llViewOrderPriorInfo)).setVisibility(View.GONE);}
            else {
                (findViewById(R.id.llViewOrderPriorInfo)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.tvViewOrderPriorInfo)).setText(viewOrder.getPriorInfo());
            }

            (findViewById(R.id.llViewOrderNote)).setVisibility(View.GONE);
            if (viewOrder.getNote() != null)
                if (!viewOrder.getNote().equals("")){
                    (findViewById(R.id.llViewOrderNote)).setVisibility(View.VISIBLE);
                    ((TextView)findViewById(R.id.tvViewOrderNote)).setText(viewOrder.getNote());
                }
        }
    }

    /// *************** Расклад по стоянкм *************** ///
    public void generateParkingButton(){
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        buttonLayoutParams.weight = (float) 0.3;
        buttonLayoutParams.setMargins(5, 5, 5, 5);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout ButtonLayout = (LinearLayout)findViewById(R.id.llParkingButtons);
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(layoutParams);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        ButtonLayout.addView(layout);
        //ParkFreeOrders = new Integer[Parkings.length()];

            for (int i = 0; i < MainApplication.getInstance().getParkings().getCount(); i++){
                //JSONObject Parking = Parkings.getJSONObject(i);

                if (i % 3 == 0){
                    // Создадим новый слой
                    layout = new LinearLayout(this);
                    layout.setLayoutParams(layoutParams);
                    layout.setOrientation(LinearLayout.HORIZONTAL);
                    ButtonLayout.addView(layout);
                }
                Button button = new Button(this);
                //button.setBackgroundResource(R.drawable.buttonshape);
                button.setLayoutParams(buttonLayoutParams);
                button.setText(MainApplication.getInstance().getParkings().getItem(i).Name);
                button.setTag(MainApplication.getInstance().getParkings().getItem(i).ID);

                button.setId(MainApplication.getInstance().getParkings().getItem(i).ID * 1578);
                /*
                final int finalI = i;
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainApplication.getInstance().setCurViewParkingID(MainApplication.getInstance().getParkings().getItem(finalI).ID);
                        MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_CUR_ORDERS);
                        OnOrdersChange();
                    }
                });
                */
                layout.addView(button);
            }
        setParkingButton();
    } // public void generateParkingButton(){

    public void setParkingButton(){
        for (int itemID = 0; itemID < MainApplication.getInstance().getParkings().getCount(); itemID++){
            Parking parking = MainApplication.getInstance().getParkings().getItem(itemID);
            Button button = (Button)findViewById(parking.ID * 1578);
            if (button != null){
                button.setText(parking.Name + " [" + parking.CarCount + "\\" + parking.OrderCount+ "]");
                if (parking.isSelf){
                    button.setBackgroundResource(R.drawable.btn_parkings_self);
                }
                else if (!parking.OrderCount.equals("0")){
                    button.setBackgroundResource(R.drawable.btn_parkings_order);
                }
                else {
                    button.setBackgroundResource(R.drawable.btn_parkings_free);
                }
            }
            else generateParkingButton();
        }
    } // public void SetColorsParkingButtons(String inParkInfo)
    /// *************** Расклад по стоянкм *************** ///
}
