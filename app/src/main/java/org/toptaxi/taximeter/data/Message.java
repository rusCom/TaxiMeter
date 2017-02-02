package org.toptaxi.taximeter.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

public class Message {
    public Integer ID;
    public Integer Status;
    public String Text;
    public String RegDate;
    public String Type;
    public Integer Route;


    public Message(JSONObject data) throws JSONException {
        if (data.has("ID"))this.ID = data.getInt("ID");
        if (data.has("Status"))this.Status = data.getInt("Status");
        if (data.has("Text"))this.Text = data.getString("Text");
        if (data.has("vRegDate"))this.RegDate = data.getString("vRegDate");
        if (data.has("Type"))this.Type = data.getString("Type");
        if (data.has("Route"))this.Route = data.getInt("Route");
    }
}
