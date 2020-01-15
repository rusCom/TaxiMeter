package org.toptaxi.taximeter.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.adapters.RVCurOrdersAdapter;
import org.toptaxi.taximeter.adapters.RecyclerItemClickListener;
import org.toptaxi.taximeter.data.DOTResponse;
import org.toptaxi.taximeter.data.Order;
import org.toptaxi.taximeter.dialogs.CheckOrderOnCompleteDialog;
import org.toptaxi.taximeter.tools.Constants;
import org.toptaxi.taximeter.tools.OnCompleteOrdersChange;

public class OrdersOnCompleteActivity extends AppCompatActivity implements OnCompleteOrdersChange, RecyclerItemClickListener.OnItemClickListener, CheckOrderOnCompleteDialog.OnCheckOrderOnComplete {
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
        rvOrders.addOnItemTouchListener(new RecyclerItemClickListener(this, this));

    }

    @Override
    protected void onResume() {
        super.onResume();
        MainApplication.getInstance().setOnCompleteOrdersChange(this);
        OnCompleteOrdersChange();

    }

    @Override
    protected void onPause() {
        super.onPause();
        MainApplication.getInstance().setOnCompleteOrdersChange(null);

    }

    @Override
    public void OnCompleteOrdersChange() {
        if (MainApplication.getInstance().getCompleteOrders().getCount() > 0)
            ordersAdapter.notifyDataSetChanged();
        else
            finish();

    }

    @Override
    public void OnCompleteOrdersNull() {
        MainApplication.getInstance().setOnCompleteOrdersChange(null);
        finish();
    }

    @Override
    public void onItemClick(View view, int position) {
        Order order = MainApplication.getInstance().getCompleteOrders().getOrder(position);
        if (order.getCheck() == 0){
            CheckOrderOnCompleteDialog checkOrderOnCompleteDialog = new CheckOrderOnCompleteDialog(this);
            checkOrderOnCompleteDialog.SetOrderInfo(order);
            checkOrderOnCompleteDialog.setOnCheckOrderOnComplete(this);
            checkOrderOnCompleteDialog.show();
        }
    }

    @Override
    public void OnCheckOrder(Order curOrder) {
        new CheckOrderTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, curOrder);
    }

    private class CheckOrderTask extends AsyncTask<Order, Void, DOTResponse>{
        ProgressDialog progressDialog;
        Context mContext;

        CheckOrderTask(Context mContext) {
            this.mContext = mContext;
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage(MainApplication.getInstance().getResources().getString(R.string.dlgSendData));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressDialog != null)progressDialog.show();
        }

        @Override
        protected DOTResponse doInBackground(Order... orders) {
            DOTResponse response = MainApplication.getInstance().getDot2().httpGet("orders/check", "order_id=" + String.valueOf(orders[0].getID()));
            if (response.getCode() == 200)MainApplication.getInstance().getDot().getData();
            return response;
        }

        @Override
        protected void onPostExecute(DOTResponse dotResponse) {
            super.onPostExecute(dotResponse);
            if (OrdersOnCompleteActivity.this.isFinishing())return;
            if (progressDialog != null && progressDialog.isShowing())progressDialog.dismiss();
            if (dotResponse.getCode() == 504)MainApplication.getInstance().showToastType(Constants.DOT_HTTP_ERROR);
            else if (dotResponse.getCode() == 200){
                finish();
            }
            else if (!dotResponse.getBody().equals("")) {
                MainApplication.getInstance().showToast(dotResponse.getBody());
                finish();
            }
        }
    }


}
