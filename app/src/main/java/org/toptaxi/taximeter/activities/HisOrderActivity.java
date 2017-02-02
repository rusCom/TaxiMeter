package org.toptaxi.taximeter.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.adapters.RoutePointsAdapter;
import org.toptaxi.taximeter.data.Order;
import org.toptaxi.taximeter.data.RoutePoint;

public class HisOrderActivity extends AppCompatActivity implements OnMapReadyCallback {
    //private static String TAG = "#########" + HisOrderActivity.class.getName();
    GoogleMap googleMap;
    Integer OrderID;
    Order viewOrder;
    RecyclerView rvRoutePoints;
    RoutePointsAdapter routePointsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_his_order);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapHisOrder);
        mapFragment.getMapAsync(this);
        OrderID = getIntent().getIntExtra("OrderID", 0);
        viewOrder = null;
        rvRoutePoints = (RecyclerView)findViewById(R.id.rvCurOrderRoutePoints);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvRoutePoints.setLayoutManager(linearLayoutManager);
        routePointsAdapter = new RoutePointsAdapter();
        rvRoutePoints.setAdapter(routePointsAdapter);
        new GetOrderInfoTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        init();
    }

    private void init(){
        if ((googleMap != null) && (viewOrder != null)){
            googleMap.clear();
            if (viewOrder.getRouteCount() == 1){
                RoutePoint routePoint = viewOrder.getRoutePoint(0);
                googleMap.addMarker(new MarkerOptions().position(routePoint.getLatLng()));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routePoint.getLatLng(), 15));
            }
            else if (viewOrder.getRouteCount() == 0){
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MainApplication.getInstance().getMainLocation().getLatitude(), MainApplication.getInstance().getMainLocation().getLongitude()), 15));
            }
            else {
                LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
                int size = this.getResources().getDisplayMetrics().widthPixels;
                for (int itemID = 0; itemID < viewOrder.getRouteCount(); itemID ++){
                    RoutePoint routePoint = viewOrder.getRoutePoint(itemID);
                    googleMap.addMarker(new MarkerOptions().position(routePoint.getLatLng()));
                    latLngBuilder.include(routePoint.getLatLng());
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBuilder.build(), size, size, 200));
            }
            routePointsAdapter.setOrder(viewOrder);
            routePointsAdapter.notifyItemRangeInserted(0, viewOrder.getRouteCount());
            routePointsAdapter.notifyDataSetChanged();
            findViewById(R.id.llCurOrderTitleEx).setVisibility(View.GONE);
            viewOrder.fillCurOrderViewData(this);
        }

    }

    private class GetOrderInfoTask extends AsyncTask<Void, Void, Void>{
        Context mContext;
        ProgressDialog progressDialog;
        GetOrderInfoTask(Context mContext) {
            this.mContext = mContext;
            if (mContext != null){
                progressDialog = new ProgressDialog(mContext);
                progressDialog.setMessage(getString(R.string.dlgGetData));
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressDialog != null)progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject data = new JSONObject(MainApplication.getInstance().getDot().getDataType("his_order", String.valueOf(OrderID)));
                if (data.getString("response").equals("ok")){
                    viewOrder = new Order();
                    viewOrder.setFromJSON(data);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog != null)
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            if (viewOrder == null){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                alertDialog.setTitle("Внимание");
                alertDialog.setMessage("Ошибка при получении данных с сервера");
                alertDialog.setPositiveButton(getString(R.string.dlgButtonCaptionOK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                alertDialog.create();
                alertDialog.show();
            }
            else {init();}
        }
    }
}
