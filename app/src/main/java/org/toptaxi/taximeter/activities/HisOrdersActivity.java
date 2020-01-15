package org.toptaxi.taximeter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.adapters.HisOrdersAdapters;
import org.toptaxi.taximeter.data.Order;

import java.util.ArrayList;

public class HisOrdersActivity extends AppCompatActivity implements AbsListView.OnScrollListener{
    //private static String TAG = "#########" + HisOrdersActivity.class.getName();
    HisOrdersAdapters adapter;
    ListView listView;
    private View footer;
    Boolean isLoadData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_his_orders);
        listView = (ListView)findViewById(R.id.lvHisOrders);

        footer = getLayoutInflater().inflate(R.layout.item_messages_footer, listView, false);
        listView.addFooterView(footer);

        adapter = new HisOrdersAdapters(this);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //Log.d(TAG, "onItemClick orderID = " + adapter.getItem(position).getID());
                Intent intent = new Intent(HisOrdersActivity.this, HisOrderActivity.class);
                intent.putExtra("OrderID", adapter.getItem(position).getID());
                startActivity(intent);
            }
        });
        updateDataAsync();
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        //Log.d(TAG, "onScroll firstVisibleItem = " + firstVisibleItem+" firstVisibleItemVisibleItemCount = "+visibleItemCount+" totalItemCount" + totalItemCount);
        if (((firstVisibleItem + visibleItemCount) >= totalItemCount) && (!isLoadData)){
            //Log.d(TAG, "LoadMore");
            updateDataAsync();
        }
    }


    void updateDataAsync()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                //Log.d(TAG, "start thread");
                isLoadData = true;
                final ArrayList<Order> result =  adapter.LoadMore();
                //Log.d(TAG, "stop load more result = " + result);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //Log.d(TAG, "runOnUiThread result = " + result);
                        if (result.size() == 0){
                            listView.removeFooterView(footer);
                        }
                        else {
                            adapter.AppendNewData(result);
                            adapter.notifyDataSetChanged();
                        }
                        isLoadData = false;
                        //Log.d(TAG, "stop thread");
                    }
                });
            }
        }).start();
    }
}
