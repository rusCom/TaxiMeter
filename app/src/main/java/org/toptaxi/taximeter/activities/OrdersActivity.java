package org.toptaxi.taximeter.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.adapters.RVCurOrdersAdapter;
import org.toptaxi.taximeter.tools.OnCompleteOrdersChange;

public class OrdersActivity extends AppCompatActivity implements OnCompleteOrdersChange {
    private static String TAG = "#########" + OrdersActivity.class.getName();
    RecyclerView rvOrders;
    RVCurOrdersAdapter ordersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        setTitle("Заказы по выполнению");
        rvOrders = (RecyclerView)findViewById(R.id.rvActivityOrders);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvOrders.setLayoutManager(llm);
        ordersAdapter = new RVCurOrdersAdapter(2);
        rvOrders.setAdapter(ordersAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        MainApplication.getInstance().setOnCompleteOrdersChange(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainApplication.getInstance().setOnCompleteOrdersChange(null);
    }

    @Override
    public void OnCompleteOrdersChange() {
        Log.d(TAG, "OnCompleteOrdersChange count = " + MainApplication.getInstance().getCompleteOrders().getCount());
        ordersAdapter.notifyDataSetChanged();
    }
}
