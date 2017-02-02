package org.toptaxi.taximeter.activities;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.adapters.RVCurOrdersAdapter;
import org.toptaxi.taximeter.adapters.RecyclerItemClickListener;
import org.toptaxi.taximeter.data.Order;
import org.toptaxi.taximeter.tools.Constants;
import org.toptaxi.taximeter.tools.LockOrientation;

public class PriorOrderActivity extends AppCompatActivity implements RecyclerItemClickListener.OnItemClickListener {
    private static String TAG = "#########" + PriorOrderActivity.class.getName();
    RecyclerView rvPriorOrders;
    RVCurOrdersAdapter curOrdersAdapter;

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
    }

    @Override
    public void onItemClick(View view, int position) {
        final Order curOrder = MainApplication.getInstance().getPriorOrders().getOrder(position);
        if (curOrder != null){
            String alertText = "Принять предварительный заказ " + curOrder.getPriorInfo() + " по маршруту " + curOrder.getRoute();
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


    /*
    new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        //MainApplication.getInstance().setViewOrderID(MainApplication.getInstance().getCurOrders().getOrderID(position));
                        //MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_VIEW_ORDER);
                        final Order curOrder = MainApplication.getInstance().getPriorOrders().getOrder(position);
                        String alertText = "Принять предварительный заказ " + curOrder.getPriorInfo() + " по маршруту " + curOrder.getRoute();
                        Log.d(TAG, alertText);
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainApplication.getInstance().getMainActivity());
                        alertDialog.setTitle("Внимание");
                        alertDialog.setMessage(alertText);
                        alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_CUR_ORDERS);
                                //MainApplication.getInstance().getDot().sendDataResult("check_order", curOrder.ID);
                                finish();
                            }
                        });
                        alertDialog.setNegativeButton("Нет" , null);
                        alertDialog.create();
                        alertDialog.show();
                    }
                })
     */

}
