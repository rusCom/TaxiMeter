package org.toptaxi.taximeter.activities.settings;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;



public class SettingsAlarmFragment extends Fragment {
    protected static String TAG = "#########" + SettingsAlarmFragment.class.getName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    SeekBar seekBarNewOrderDistance;
    SeekBar seekBarNewOrderCost;
    Switch switchNewOrder;
    TextView tvNewOrderDistance;
    TextView tvNewOrderCost;


    public SettingsAlarmFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsAlarmFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsAlarmFragment newInstance(String param1, String param2) {
        SettingsAlarmFragment fragment = new SettingsAlarmFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings_alarm, container, false);
        seekBarNewOrderDistance = view.findViewById(R.id.seekBarNewOrderDistance);
        seekBarNewOrderCost = view.findViewById(R.id.seekBarNewOrderCost);
        switchNewOrder = view.findViewById(R.id.switchNewOrder);
        tvNewOrderDistance = view.findViewById(R.id.tvNewOrderDistance);
        tvNewOrderCost = view.findViewById(R.id.tvNewOrderCost);

        switchNewOrder.setChecked(MainApplication.getInstance().getMainPreferences().getNewOrderAlarmCheck());
        switch (MainApplication.getInstance().getMainPreferences().getNewOrderAlarmDistance()){
            case 1:seekBarNewOrderDistance.setProgress(0);break;
            case 2:seekBarNewOrderDistance.setProgress(1);break;
            case 3:seekBarNewOrderDistance.setProgress(2);break;
            case 5:seekBarNewOrderDistance.setProgress(3);break;
            case 10:seekBarNewOrderDistance.setProgress(4);break;
            case 15:seekBarNewOrderDistance.setProgress(5);break;
            case -1:seekBarNewOrderDistance.setProgress(6);break;
        }

        switch (MainApplication.getInstance().getMainPreferences().getNewOrderAlarmCost()){
            case 100:seekBarNewOrderCost.setProgress(0);break;
            case 300:seekBarNewOrderCost.setProgress(1);break;
            case 500:seekBarNewOrderCost.setProgress(2);break;
            case 1500:seekBarNewOrderCost.setProgress(3);break;
            case 3000:seekBarNewOrderCost.setProgress(4);break;
        }

        seekBarNewOrderDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                newOrderSetData();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarNewOrderCost.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                newOrderSetData();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        switchNewOrder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                newOrderSetData();

            }
        });


        // seekBarNewOrderDistance.setProgress(MainApplication.getInstance().getMainPreferences().getNewOrderAlarmDistance());
        // seekBarNewOrderCost.setProgress(MainApplication.getInstance().getMainPreferences().getNewOrderAlarmCost());

        newOrderSetData();
        return view;
    }


    void newOrderSetData(){
        seekBarNewOrderDistance.setEnabled(switchNewOrder.isChecked());
        seekBarNewOrderCost.setEnabled(switchNewOrder.isChecked());
        Integer distance = -1, cost = 100;
        switch (seekBarNewOrderDistance.getProgress()){
            case 0:tvNewOrderDistance.setText("1 км.");distance = 1;break;
            case 1:tvNewOrderDistance.setText("2 км.");distance = 2;break;
            case 2:tvNewOrderDistance.setText("3 км.");distance = 3;break;
            case 3:tvNewOrderDistance.setText("5 км.");distance = 5;break;
            case 4:tvNewOrderDistance.setText("10 км.");distance = 10;break;
            case 5:tvNewOrderDistance.setText("15 км.");distance = 15;break;
            case 6:tvNewOrderDistance.setText("Все");distance = -1;break;
        }
        switch (seekBarNewOrderCost.getProgress()){
            case 0:cost = 100;break;
            case 1:cost = 300;break;
            case 2:cost = 500;break;
            case 3:cost = 1500;break;
            case 4:cost = 3000;break;
        }

        tvNewOrderCost.setText(String.valueOf(cost) + " руб.");

        // Log.d(TAG, "distance = " + distance);
        // Log.d(TAG, "newOrderSetData");

        MainApplication.getInstance().getMainPreferences().setNewOrderAlarm(switchNewOrder.isChecked(), distance, cost);

    }



}
