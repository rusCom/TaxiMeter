package org.toptaxi.taximeter.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.MainActionItem;
import org.toptaxi.taximeter.data.UnlimTariff;

import java.util.List;


public class MainActionUnlimAdapter extends ArrayAdapter<UnlimTariff> {
    Context mContext;
    int layoutResourceId;
    List<UnlimTariff> mainActionItems;

    public MainActionUnlimAdapter(Context context, int resource, List<UnlimTariff> objects) {
        super(context, resource, objects);
        mContext = context;
        layoutResourceId = resource;
        mainActionItems = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }
        UnlimTariff mainActionItem = mainActionItems.get(position);
        TextView tvMainActionTitle = (TextView)convertView.findViewById(R.id.tvMainActionUnlimTitle);
        tvMainActionTitle.setText(mainActionItem.getName());
        tvMainActionTitle.setTag(mainActionItem);
        return convertView;
    }
}
