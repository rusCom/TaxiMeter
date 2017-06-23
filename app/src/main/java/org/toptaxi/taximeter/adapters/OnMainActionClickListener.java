package org.toptaxi.taximeter.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import org.toptaxi.taximeter.MainActivity;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.activities.MessagesActivity;
import org.toptaxi.taximeter.activities.OrdersActivity;
import org.toptaxi.taximeter.activities.PriorOrderActivity;
import org.toptaxi.taximeter.data.MainActionItem;
import org.toptaxi.taximeter.tools.Constants;

public class OnMainActionClickListener implements AdapterView.OnItemClickListener {
    protected static String TAG = "#########" + OnMainActionClickListener.class.getName();
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Context context = view.getContext();
        TextView textViewItem = ((TextView) view.findViewById(R.id.tvMainActionTitle));
        MainActionItem mainActionItem = (MainActionItem)textViewItem.getTag();
        //Log.d(TAG,  "onItemClick " + mainActionItem.getActionName());

        if (MainApplication.getInstance().getMainActivity() != null){
            ((MainActivity) context).mainActionsDialog.cancel();

            switch (mainActionItem.getAction()){
                case Constants.MAIN_ACTION_GO_ONLINE:
                    MainApplication.getInstance().getMainActivity().driverGoOffLine();
                    break;
                case Constants.MAIN_ACTION_GO_OFFLINE:
                    MainApplication.getInstance().getMainActivity().driverGoOffLine();
                    break;
                case Constants.MAIN_ACTION_ACTIVATE_UNLIM:
                    MainApplication.getInstance().getMainActivity().onGetUnlimTariffClick();
                    break;
                case Constants.MAIN_ACTION_PRIOR_ORDER:

                        if (MainApplication.getInstance().getMainAccount().getCheckPriorOrder()){
                            Intent priorOrders = new Intent(MainApplication.getInstance().getMainActivity(), PriorOrderActivity.class);
                            MainApplication.getInstance().getMainActivity().startActivity(priorOrders);
                        }
                        else {
                            AlertDialog.Builder adb = new AlertDialog.Builder(MainApplication.getInstance().getMainActivity());
                            adb.setMessage(MainApplication.getInstance().getMainPreferences().getCheckPriorErrorText());
                            adb.setIcon(android.R.drawable.ic_dialog_info);
                            adb.setPositiveButton("Ok", null);
                            adb.create();
                            adb.show();
                        }


                    break;
                case Constants.MAIN_ACTION_SEND_MESSAGE:
                        Intent messagesIntent = new Intent(MainApplication.getInstance().getMainActivity(), MessagesActivity.class);
                        MainApplication.getInstance().getMainActivity().startActivity(messagesIntent);
                    break;
                case Constants.MAIN_ACTION_PARKINGS:
                    MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_PARKINGS);
                    break;
                case Constants.MENU_TEMPLATE_MESSAGE:
                    MainApplication.getInstance().getDot().sendDataResult("message", textViewItem.getText().toString());
                    break;
                case Constants.MAIN_ACTION_ORDERS_COMPLETE:
                    MainApplication.getInstance().getMainActivity().startActivity(new Intent(MainApplication.getInstance().getMainActivity(), OrdersActivity.class));
                    break;

            }

        }

    }
}
