package org.toptaxi.taximeter.data;


import org.json.JSONException;
import org.json.JSONObject;

public class Parking {
    public String Name, CarCount = "0", OrderCount = "0";
    public boolean isSelf = false;
    public Integer ID;

    public Parking(JSONObject data) throws JSONException{
        if (data.has("ID"))this.ID = data.getInt("ID");
        if (data.has("Name"))this.Name = data.getString("Name");
        if (data.has("CarCount"))this.CarCount = data.getString("CarCount");
        if (data.has("OrderCount"))this.OrderCount = data.getString("OrderCount");
        if (data.has("isSelf"))this.isSelf = data.getBoolean("isSelf");
    }
}
