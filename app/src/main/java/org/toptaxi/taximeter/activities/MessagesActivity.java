package org.toptaxi.taximeter.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.adapters.ListViewMessageAdapter;
import org.toptaxi.taximeter.data.Messages;
import org.toptaxi.taximeter.tools.Constants;
import org.toptaxi.taximeter.tools.LockOrientation;

public class MessagesActivity extends AppCompatActivity implements AbsListView.OnScrollListener, Messages.OnMessagesListener {
    private static String TAG = "#########" + MessagesActivity.class.getName();
    ListViewMessageAdapter adapter;
    ListView listView;
    private View footer;
    LoadMoreAsyncTask loadMoreAsyncTask = new LoadMoreAsyncTask();
    boolean isFirst = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Утановка текущего поворота экрана
        new LockOrientation(this).lock();
        setContentView(R.layout.activity_messages);
        listView = (ListView)findViewById(R.id.lvMessages);

        footer = getLayoutInflater().inflate(R.layout.item_messages_footer, null);
        listView.addHeaderView(footer);

        adapter = new ListViewMessageAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(this);

        loadMoreAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        listView.setSelection(listView.getCount());
        MainApplication.getInstance().getMainMessages().setOnMessagesListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainApplication.getInstance().getMainMessages().setOnMessagesListener(null);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if ((MainApplication.getInstance().getMainMessages().getCount() < 10) ){
            listView.removeHeaderView(footer);
        }
        else {
            if ((firstVisibleItem == 0) && (loadMoreAsyncTask.getStatus() == AsyncTask.Status.FINISHED)){
                //Log.d(TAG, "onScroll stra = " + firstVisibleItem);
                loadMoreAsyncTask = new LoadMoreAsyncTask();
                loadMoreAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Override
    public void OnNewMessage() {
        adapter.notifyDataSetChanged();
        listView.setSelection(listView.getCount());
    }

    private class LoadMoreAsyncTask extends AsyncTask<Void, Void, Integer>{
        @Override
        protected Integer doInBackground(Void... voids) {
            if (isFirst){
                isFirst = false;
                return -1;

            }
            else {
                Log.d(TAG, "doInBackground");
                try {
                    return MainApplication.getInstance().getMainMessages().LoadMore();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            Log.d(TAG, "onPostExecute result = " + result);
            if (result == 0){
                //footer.setVisibility(View.GONE);
                listView.removeHeaderView(footer);
            }
            else if (result > 0) {
                adapter.notifyDataSetChanged();
                listView.setSelection(result);
            }

        }
    }

    public void btnSendMessageClick(View view){
        String Text = ((EditText)findViewById(R.id.etMessagesMessage)).getText().toString();
        if (!Text.equals("")){
            SendMessageTask sendMessageTask = new SendMessageTask(this);
            sendMessageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Text);
        }
    }

    private class SendMessageTask extends AsyncTask<String, Void, Integer>{
        ProgressDialog progressDialog;
        public SendMessageTask(Context mContext){
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Передача данных ...");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
            findViewById(R.id.btnMessagesSend).setEnabled(false);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            int result = 0;
            int t_result =  MainApplication.getInstance().getDot().sendDataResponse("message" ,strings[0]);
            if (t_result == Constants.DOT_REST_OK){
                try {
                    MainApplication.getInstance().getMainMessages().LoadNew();
                    result = 1;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result == 0){
                MainApplication.getInstance().showToastType(Constants.DOT_HTTP_ERROR);
            }
            else {
                adapter.notifyDataSetChanged();
                listView.setSelection(listView.getCount());
            }
            findViewById(R.id.btnMessagesSend).setEnabled(true);
            ((EditText)findViewById(R.id.etMessagesMessage)).setText("");
            ((EditText)findViewById(R.id.etMessagesMessage)).clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow((findViewById(R.id.etMessagesMessage)).getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);


        }
    }

}
