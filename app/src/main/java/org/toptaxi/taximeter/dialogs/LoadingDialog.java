package org.toptaxi.taximeter.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;

import org.toptaxi.taximeter.R;

public class LoadingDialog {
    AlertDialog dialog;

    protected LoadingDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setView(R.layout.layout_loading_dialog);
        }
        dialog = builder.create();
    }

    public void show(){
        dialog.show();
    }

    public void dismiss(){
        dialog.dismiss();
    }
}
