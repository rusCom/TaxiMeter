package org.toptaxi.taximeter.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.toptaxi.taximeter.MainActivity;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.adapters.RoutePointsAdapter;
import org.toptaxi.taximeter.data.Order;
import org.toptaxi.taximeter.tools.LockOrientation;

public class NewOrderActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static String TAG = "#########" + NewOrderActivity.class.getName();
    Button btnApplyOrder, btnDenyOrder;
    MediaPlayer mp;
    TextView tvTimer;
    MyTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Утановка текущего поворота экрана
        new LockOrientation(this).lock();
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_new_order);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapNewOrder);
        mapFragment.getMapAsync(this);

        Order viewOrder = MainApplication.getInstance().getNewOrder();
        btnApplyOrder = (Button)findViewById(R.id.btnOrderMainAction);
        btnDenyOrder = (Button)findViewById(R.id.btnOrderAction);
        tvTimer = (TextView)findViewById(R.id.tvOrderTimer);
        tvTimer.setText("!!!!");
        tvTimer.setVisibility(View.VISIBLE);
        btnApplyOrder.setText("Принять");
        btnDenyOrder.setText("Отказаться");

        if (viewOrder.IsDenyPenalty()){
            if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)btnDenyOrder.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.btn_red));
            else btnDenyOrder.setBackgroundResource(R.drawable.btn_red);
            btnDenyOrder.setText(viewOrder.getDenyButtonCaption());
        }
        else {
            if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)btnDenyOrder.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.btn_yellow));
            else btnDenyOrder.setBackgroundResource(R.drawable.btn_yellow);
        }




        btnApplyOrder.setVisibility(View.VISIBLE);
        btnDenyOrder.setVisibility(View.VISIBLE);

        btnApplyOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainApplication.getInstance().getDot().sendDataResult("driver_apply_order", "");
                finish();
            }
        });

        btnDenyOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainApplication.getInstance().getDot().sendDataResult("driver_deny_order", "");
                finish();
            }
        });

        RecyclerView rvViewOrderRoutePoints = (RecyclerView)findViewById(R.id.rvViewOrderRoutePoints);
        rvViewOrderRoutePoints.setLayoutManager(new LinearLayoutManager(this));
        //RVCurOrderRoutePointsAdapter viewOrderPointsAdapter = new RVCurOrderRoutePointsAdapter(Constants.ORDER_VIEW_NEW_ORDER);
        RoutePointsAdapter viewOrderPointsAdapter = new RoutePointsAdapter();
        rvViewOrderRoutePoints.setAdapter(viewOrderPointsAdapter);
        viewOrderPointsAdapter.setOrder(MainApplication.getInstance().getNewOrder());
        viewOrderPointsAdapter.notifyItemRangeInserted(0, MainApplication.getInstance().getNewOrder().getRouteCount());
        viewOrderPointsAdapter.notifyDataSetChanged();

        findViewById(R.id.llViewOrderTitle).setBackgroundResource(viewOrder.getCaptionColor());

        ((TextView)findViewById(R.id.tvViewOrderDistance)).setText(viewOrder.getDistanceString());
        ((TextView)findViewById(R.id.tvViewOrderPayType)).setText(viewOrder.getPayTypeName());
        ((TextView)findViewById(R.id.tvViewOrderCalcType)).setText(viewOrder.getCalcType());
        ((TextView)findViewById(R.id.tvViewOrderDispatchingName)).setText(viewOrder.getDispatchingName());
        ((TextView)findViewById(R.id.tvViewOrderPayPercent)).setText(viewOrder.getDispPay());

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

        mp = MediaPlayer.create(this, R.raw.new_order_activity);
        mp.setLooping(true);
        mp.start();
        //tvTimer.setText("60");
        timer = new MyTimer(viewOrder.getNewOrderTimer(), 1000);
        timer.start();
    }

    public class MyTimer extends CountDownTimer
    {

        MyTimer(long millisInFuture, long countDownInterval)
        {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish()
        {
            MainApplication.getInstance().getDot().sendData("driver_deny_order", "");
            finish();
        }

        public void onTick(long millisUntilFinished)
        {
            //Log.d(TAG, "onTimerTick");
            tvTimer.setText(String.valueOf(millisUntilFinished/1000));
            MainApplication.getInstance().getNewOrder().setNewOrderTimer(millisUntilFinished);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Log.d(TAG, "onDestroy");
        mp.stop();
        timer.cancel();
        // Если основное окно не запущено, то запускаем
        if (MainApplication.getInstance().getMainActivity() == null){
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng coord = MainApplication.getInstance().getNewOrder().getPoint();
        googleMap.addMarker(new MarkerOptions().position(coord));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coord, 15));
    }
}
