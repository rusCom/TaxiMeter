package org.toptaxi.taximeter.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import org.toptaxi.taximeter.MainActivity;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.UnlimTariff;
import org.toptaxi.taximeter.tools.Constants;

public class OnMainActionUnlimClickListener implements AdapterView.OnItemClickListener {
    protected static String TAG = "#########" + OnMainActionUnlimClickListener.class.getName();
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Context context = view.getContext();
        TextView textViewItem = ((TextView) view.findViewById(R.id.tvMainActionUnlimTitle));
        final UnlimTariff unlimTariff = (UnlimTariff)textViewItem.getTag();
        Log.d(TAG,  "onItemClick " + unlimTariff.getName());


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainApplication.getInstance().getMainActivity());
        alertDialog.setTitle("Внимание");
        alertDialog.setMessage("Активировать безлимит: " + unlimTariff.getName());
        alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MainApplication.getInstance().getDot().sendDataResult("activate_unlim", String.valueOf(unlimTariff.getID()));
                MainApplication.getInstance().getMainActivity().mainActionsUnlimDialog.cancel();
            }
        });
        alertDialog.setNegativeButton("Нет" , null);
        alertDialog.create();
        alertDialog.show();
    }
}
