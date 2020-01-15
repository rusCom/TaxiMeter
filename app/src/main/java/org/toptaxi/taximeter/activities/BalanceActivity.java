package org.toptaxi.taximeter.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.adapters.ListViewPaymentAdapter;
import org.toptaxi.taximeter.data.Payment;

import java.util.ArrayList;

public class BalanceActivity extends AppCompatActivity implements AbsListView.OnScrollListener {
    private static String TAG = "#########" + BalanceActivity.class.getName();
    ListViewPaymentAdapter adapter;
    ListView listView;
    private View footer;
    Boolean isLoadData = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        listView = (ListView)findViewById(R.id.lvPayments);

        footer = getLayoutInflater().inflate(R.layout.item_messages_footer, null);
        listView.addFooterView(footer);

        adapter = new ListViewPaymentAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(this);
        ///loadMoreAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            Log.d(TAG, "LoadMore");
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
                Log.d(TAG, "start thread");
                isLoadData = true;
                final ArrayList<Payment> result =  adapter.LoadMore();
                Log.d(TAG, "stop load more result = " + result);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Log.d(TAG, "runOnUiThread result = " + result);
                        if (result.size() == 0){
                            listView.removeFooterView(footer);
                        }
                        else {
                            adapter.AppendNewData(result);
                            adapter.notifyDataSetChanged();
                        }
                        isLoadData = false;
                        Log.d(TAG, "stop thread");
                    }
                });
            }
        }).start();
    }

}
