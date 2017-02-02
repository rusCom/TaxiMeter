package org.toptaxi.taximeter.data;


import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.tools.Constants;

import java.util.ArrayList;
import java.util.List;

public class Parkings {
    protected static String TAG = "#########" + Parkings.class.getName();
    List<Parking> parkings;
    final Handler uiHandler = new Handler(Looper.getMainLooper());
    Integer parkingID = 0;

    public Parkings(){
        parkings = new ArrayList<>();
    }

    public void setFromJSON(JSONArray data) throws JSONException{
        parkings.clear();
        for (int itemID = 0; itemID < data.length(); itemID ++){
            JSONObject orderJSON = data.getJSONObject(itemID);
            Parking parking = new Parking(orderJSON);
            parkings.add(parking);
        }
        if (MainApplication.getInstance().getMainActivity() != null)
            MainApplication.getInstance().getMainActivity().generateParkingButton();
    }

    public String getName(int ParkingID){
        Parking parking = getItemByID(ParkingID);
        if (parking!=null)return parking.Name;
        else return "";
        }

    public void setSelf(int ParkigID){
        if (this.parkingID != ParkigID){
            for (int itemID = 0; itemID < parkings.size(); itemID++){
                if (parkings.get(itemID).ID == ParkigID){parkings.get(itemID).isSelf = true;}
                else {parkings.get(itemID).isSelf = false;}
            }
            this.parkingID = ParkigID;
            if (MainApplication.getInstance().getMainActivity() != null){
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MainApplication.getInstance().getMainActivity().setParkingButton();
                    }
                });
            }
        }

    }

    public void setDataFromJSON(JSONArray data) throws JSONException {
        for (int itemID = 0; itemID < data.length(); itemID++){
            JSONObject parkingJSON = data.getJSONObject(itemID);
            Parking parking = getItemByID(parkingJSON.getInt("ID"));
            if (parking != null){
                parking.OrderCount = parkingJSON.getString("orders");
                parking.CarCount = parkingJSON.getString("cars");
            }

        }


        if (MainApplication.getInstance().getMainActivity() != null){
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    MainApplication.getInstance().getMainActivity().setParkingButton();
                }
            });
        }

    }

    public int getCount(){return parkings.size();}

    public Parking getItemByID(int ID){
        for (int itemID = 0; itemID < parkings.size(); itemID++){
            if (parkings.get(itemID).ID == ID){
                return parkings.get(itemID);
            }
        }
        return null;
    }

    public Parking getItem(int itemID){
        return parkings.get(itemID);
    }
}
