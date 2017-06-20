package org.toptaxi.taximeter.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;

public class StatisticsActivity extends AppCompatActivity {
    TextView curTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec tabSpec;

        tabSpec = tabHost.newTabSpec("statOrders");
        tabSpec.setIndicator("Заказы");
        tabSpec.setContent(R.id.tvStatisticsOrders);
        tabHost.addTab(tabSpec);
        findViewById(R.id.tvStatisticsOrders).setVisibility(View.VISIBLE);
        curTextView = (TextView) findViewById(R.id.tvStatisticsOrders);

        if (MainApplication.getInstance().getMenuItems().getRating()){
            tabSpec = tabHost.newTabSpec("statRating");
            tabSpec.setIndicator("Рейтинг");
            tabSpec.setContent(R.id.tvStatisticsRating);
            tabHost.addTab(tabSpec);
            findViewById(R.id.tvStatisticsRating).setVisibility(View.VISIBLE);
        }
        else findViewById(R.id.tvStatisticsRating).setVisibility(View.GONE);

        if (MainApplication.getInstance().getMainPreferences().shareDriver){
            tabSpec = tabHost.newTabSpec("statShare");
            tabSpec.setIndicator("Друзья");
            tabSpec.setContent(R.id.tvStatisticsShare);
            tabHost.addTab(tabSpec);
            findViewById(R.id.tvStatisticsShare).setVisibility(View.VISIBLE);
        }
        else {findViewById(R.id.tvStatisticsShare).setVisibility(View.VISIBLE);}

        // обработчик переключения вкладок
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                switch (tabId) {
                    case "statRating":
                        curTextView = (TextView) findViewById(R.id.tvStatisticsRating);
                        break;
                    case "statOrders":
                        curTextView = (TextView) findViewById(R.id.tvStatisticsOrders);
                        break;
                    case "statShare":
                        curTextView = (TextView) findViewById(R.id.tvStatisticsShare);
                        break;
                }
                new GetStatInfoTask(StatisticsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tabId);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetStatInfoTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "statOrders");
    }

    private class GetStatInfoTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        Context mContext;
        GetStatInfoTask(Context context){
            mContext = context;
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Получение данных ...");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... data) {
            return MainApplication.getInstance().getDot().getDataType(data[0], "");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            if (curTextView != null){
                try {
                    JSONObject data = new JSONObject(s);
                    if (data.has("response"))
                        if (data.getString("response").equals("ok"))
                            if (data.has("stat"))
                                curTextView.setText(MainApplication.fromHtml(data.getString("stat")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
