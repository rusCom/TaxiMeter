package org.toptaxi.taximeter.data;


import org.json.JSONException;
import org.json.JSONObject;

public class UnlimTariff {
    int ID;
    String Name;

    public UnlimTariff(JSONObject data) throws JSONException {
        this.ID = data.getInt("id");
        this.Name = data.getString("name");
    }

    public String getName() {
        return Name;
    }

    public int getID() {
        return ID;
    }
}
