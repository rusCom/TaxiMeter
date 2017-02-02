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
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        findViewById(R.id.tvStatisticsShare).setVisibility(View.GONE);
        mContext = this;
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        // инициализация
        tabHost.setup();

        TabHost.TabSpec tabSpec;

        tabSpec = tabHost.newTabSpec("statRating");
        tabSpec.setIndicator("Рейтинг");
        tabSpec.setContent(R.id.tvStatisticsRating);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("statOrders");
        tabSpec.setIndicator("Заказы");
        tabSpec.setContent(R.id.tvStatisticsOrders);
        tabHost.addTab(tabSpec);

        if (MainApplication.getInstance().getMainPreferences().shareDriver){
            tabSpec = tabHost.newTabSpec("statShare");
            tabSpec.setIndicator("Друзья");
            tabSpec.setContent(R.id.tvStatisticsShare);
            tabHost.addTab(tabSpec);
            findViewById(R.id.tvStatisticsShare).setVisibility(View.VISIBLE);
        }



        new GetStatInfoTask(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "statRating");
        curTextView = (TextView)findViewById(R.id.tvStatisticsRating);


        // обработчик переключения вкладок
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                if (tabId.equals("statRating")){curTextView = (TextView)findViewById(R.id.tvStatisticsRating);}
                if (tabId.equals("statOrders")){curTextView = (TextView)findViewById(R.id.tvStatisticsOrders);}
                if (tabId.equals("statShare")){curTextView = (TextView)findViewById(R.id.tvStatisticsShare);}
                new GetStatInfoTask(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tabId);
            }
        });
    }

    private class GetStatInfoTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        Context mContext;
        public GetStatInfoTask(Context context){
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
                                curTextView.setText(Html.fromHtml(data.getString("stat")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
