package org.toptaxi.taximeter.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Payment {
    public String ID, Name, Comment, Summa, Balance, Date;
    public Integer Type;

    public Payment(JSONObject data) throws JSONException {
        if (data.has("ID"))this.ID = data.getString("ID");
        if (data.has("Name"))this.Name = data.getString("Name");
        if (data.has("Comment"))this.Comment = data.getString("Comment");
        if (data.has("Summa"))this.Summa = data.getString("Summa");
        if (data.has("Balance"))this.Balance = data.getString("Balance");
        if (data.has("Date"))this.Date = data.getString("Date");
        if (data.has("Type"))this.Type = data.getInt("Type");
    }
}
