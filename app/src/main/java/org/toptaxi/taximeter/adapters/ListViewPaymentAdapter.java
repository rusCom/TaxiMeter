package org.toptaxi.taximeter.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.Payment;

import java.util.ArrayList;

public class ListViewPaymentAdapter extends BaseAdapter {
    protected static String TAG = "#########" + ListViewMessageAdapter.class.getName();
    Context mContext;
    LayoutInflater lInflater;
    private ArrayList<Payment> payments;
    String LastID = "0";


    public ListViewPaymentAdapter(Context mContext) {
        this.mContext = mContext;
        lInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        payments = new ArrayList<>();
    }

    public void AppendNewData(ArrayList<Payment> data){
        payments.addAll(data);
    }


    public ArrayList<Payment> LoadMore() {
        ArrayList<Payment> results = new ArrayList<>();
        int resultCount = 0;
        try {
            JSONObject data = new JSONObject(MainApplication.getInstance().getDot().getDataType("payments", LastID));
            //Log.d(TAG, "loadMore data = " + data.toString());
            if (data.has("payments")){
                JSONArray paymentsJSON = data.getJSONArray("payments");
                for (int itemID = 0; itemID < paymentsJSON.length(); itemID ++){
                    //Log.d(TAG, "itemID = " + itemID + " payment = " + paymentsJSON.getJSONObject(itemID));
                    Payment payment = new Payment(paymentsJSON.getJSONObject(itemID));
                    results.add(payment);
                    LastID = payment.ID;
                    resultCount ++;
                }
                resultCount = paymentsJSON.length();
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
        return payments.size();
    }

    @Override
    public Payment getItem(int position) {
        return payments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item_balance, parent, false);
        }
        Payment payment = payments.get(position);
        //Log.d(TAG, "getView position=" + position + ";id="+payment.ID+";Name="+payment.Name);

        LinearLayout layout = (LinearLayout) view
                .findViewById(R.id.bubble_layout);



        if (payment.Type == 0){
            layout.setBackgroundResource(R.drawable.out_message_bg);
        }
        else {
            layout.setBackgroundResource(R.drawable.in_message_bg);
        }



        ((TextView)view.findViewById(R.id.tvItemBalanceName)).setText(payment.Name);
        //((TextView)view.findViewById(R.id.tvItemMessageRegDate)).setText(payment.);
        if (payment.Comment.equals("")){view.findViewById(R.id.tvItemBalanceComment).setVisibility(View.GONE);}
        else{
            view.findViewById(R.id.tvItemBalanceComment).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.tvItemBalanceComment)).setText(payment.Comment);
        }
        ((TextView)view.findViewById(R.id.tvItemBalanceDate)).setText(payment.Date);
        ((TextView)view.findViewById(R.id.tvItemBalanceSumma)).setText(payment.Summa);
        return view;
    }
}
