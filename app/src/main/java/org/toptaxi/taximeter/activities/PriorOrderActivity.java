package org.toptaxi.taximeter.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.adapters.RVCurOrdersAdapter;
import org.toptaxi.taximeter.adapters.RecyclerItemClickListener;
import org.toptaxi.taximeter.data.Order;
import org.toptaxi.taximeter.tools.Constants;
import org.toptaxi.taximeter.tools.LockOrientation;
import org.toptaxi.taximeter.tools.OnPriorOrdersChange;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class PriorOrderActivity extends AppCompatActivity implements RecyclerItemClickListener.OnItemClickListener, OnPriorOrdersChange {
    private static String TAG = "#########" + PriorOrderActivity.class.getName();
    RecyclerView rvPriorOrders;
    RVCurOrdersAdapter curOrdersAdapter;
    Calendar ServerDate;
    SimpleDateFormat simpleDateFormat;
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Утановка текущего поворота экрана
        new LockOrientation(this).lock();
        setContentView(R.layout.activity_prior_order);

        rvPriorOrders = (RecyclerView)findViewById(R.id.rvPriorOrders);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvPriorOrders.setLayoutManager(llm);
        curOrdersAdapter = new RVCurOrdersAdapter(1);
        rvPriorOrders.setAdapter(curOrdersAdapter);
        rvPriorOrders.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
        MainApplication.getInstance().setOnPriorOrdersChange(this);
        ServerDate = MainApplication.getInstance().getServerDate();
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd.MM:yyyy", Locale.getDefault());
        setTitle(simpleDateFormat.format(ServerDate.getTime()));
    }

    @Override
    public void onItemClick(View view, int position) {
        final Order curOrder = MainApplication.getInstance().getPriorOrders().getOrder(position);
        if (curOrder != null){
            String alertText = "Принять предварительный заказ " + curOrder.getPriorInfo() + " по маршруту " + curOrder.getRoute();
            if (!curOrder.getNote().equals("")){
                alertText += "(" + curOrder.getNote() + ")";
            }
            Log.d(TAG, alertText);
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Внимание");
            alertDialog.setMessage(alertText);
            alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_CUR_ORDERS);
                    MainApplication.getInstance().getDot().sendDataResult("check_order", String.valueOf(curOrder.getID()));
                    finish();
                }
            });
            alertDialog.setNegativeButton("Нет" , null);
            alertDialog.create();
            alertDialog.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            Log.d(TAG, "StopTimer");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTimer != null) {
            mTimer.cancel();
        }

        // re-schedule timer here
        // otherwise, IllegalStateException of
        // "TimerTask is scheduled already"
        // will be thrown
        ServerDate = MainApplication.getInstance().getServerDate();
        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, 1000, 1000);
        Log.d(TAG, "StartTimer");
    }

    @Override
    public void OnPriorOrdersChange() {
        Log.d(TAG, "OnPriorOrdersChange count = " + MainApplication.getInstance().getPriorOrders().getCount());
        curOrdersAdapter.notifyDataSetChanged();
        ServerDate = MainApplication.getInstance().getServerDate();
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            ServerDate.add(Calendar.SECOND, 1);
            final String strDate = simpleDateFormat.format(ServerDate.getTime());

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    setTitle(strDate);
                }
            });
        }
    }
}
