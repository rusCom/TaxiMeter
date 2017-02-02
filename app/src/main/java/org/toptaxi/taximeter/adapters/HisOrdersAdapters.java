package org.toptaxi.taximeter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.Order;

import java.util.ArrayList;

public class HisOrdersAdapters extends BaseAdapter {
    protected static String TAG = "#########" + HisOrdersAdapters.class.getName();
    private LayoutInflater lInflater;
    private ArrayList<Order> orders;
    private Integer LastID = 0;


    public HisOrdersAdapters(Context mContext) {
        lInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        orders = new ArrayList<>();
    }

    public void AppendNewData(ArrayList<Order> data){
        orders.addAll(data);
    }


    public ArrayList<Order> LoadMore() {
        ArrayList<Order> results = new ArrayList<>();
        try {
            JSONObject data = new JSONObject(MainApplication.getInstance().getDot().getDataType("his_orders", String.valueOf(LastID)));
            //Log.d(TAG, "loadMore data = " + data.toString());
            if (data.has("his_orders")){
                JSONArray paymentsJSON = data.getJSONArray("his_orders");
                for (int itemID = 0; itemID < paymentsJSON.length(); itemID ++){
                    Order order = new Order();
                    order.setFromJSON(paymentsJSON.getJSONObject(itemID));
                    results.add(order);
                    LastID = order.getID();

                }
            }
        } catch (JSONException e) {
            //Log.d(TAG, "JSONException");
            e.printStackTrace();
        }
        //Log.d(TAG, "LoadMore resultCount = " + resultCount);

        return results;
    }


    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Order getItem(int position) {
        return orders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item_his_orders, parent, false);
        }

        Order order = orders.get(position);
        if (order != null){
            ((TextView)view.findViewById(R.id.tvHisOrderDate)).setText(order.getDate());
            ((TextView)view.findViewById(R.id.tvHisOrderStatus)).setText(order.getStateName());
            view.findViewById(R.id.llHisOrderCaption).setBackgroundResource(order.getCaptionColor());
            if (order.getFirstPointInfo() != null){
                view.findViewById(R.id.tvHisOrderFirstRoutePoint).setVisibility(View.VISIBLE);
                ((TextView)view.findViewById(R.id.tvHisOrderFirstRoutePoint)).setText(order.getFirstPointInfo());
            }
            else {view.findViewById(R.id.tvHisOrderFirstRoutePoint).setVisibility(View.GONE);}
            if (order.getLastPointInfo() != null){
                view.findViewById(R.id.tvHisOrderLastRoutePoint).setVisibility(View.VISIBLE);
                ((TextView)view.findViewById(R.id.tvHisOrderLastRoutePoint)).setText(order.getLastPointInfo());
            }
            else {view.findViewById(R.id.tvHisOrderLastRoutePoint).setVisibility(View.GONE);}
        }

        return view;
    }

}
