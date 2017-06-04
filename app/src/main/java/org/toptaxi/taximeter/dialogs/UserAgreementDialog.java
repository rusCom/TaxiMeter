package org.toptaxi.taximeter.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;


public class UserAgreementDialog extends Dialog implements View.OnClickListener {
    //protected static String TAG = "#########" + UserAgreementDialog.class.getName();

    public UserAgreementDialog(Context context) {
        super(context);
        this.setContentView(R.layout.dialog_user_agreement);
        this.setCanceledOnTouchOutside(false);
        String Link = "<a href=" + MainApplication.getInstance().getMainPreferences().getAgreementLink() + ">адресу</a>";
        ((TextView)findViewById(R.id.tvUserAgreementLink)).setText(Html.fromHtml("С полным текстом Вы можете ознакомиться по " + Link + " в сети Интернет."));
        ((TextView)findViewById(R.id.tvUserAgreementLink)).setMovementMethod(LinkMovementMethod.getInstance());
        findViewById(R.id.btnUserAgreementApply).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        MainApplication.getInstance().getMainActivity().finish();
        MainApplication.getInstance().stopMainService();
        dismiss();

    }


    @Override
    public void onClick(View view) {
        MainApplication.getInstance().getMainAccount().setUserAgreementApply();
        dismiss();
    }
}