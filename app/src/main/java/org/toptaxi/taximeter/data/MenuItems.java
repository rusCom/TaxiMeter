package org.toptaxi.taximeter.data;

import org.json.JSONException;
import org.json.JSONObject;

public class MenuItems {
    private Boolean Messages, Unlim, Rating, OrdersOnComplete, Covid19;

    public MenuItems() {
        Messages = false;
        Unlim = false;
        Rating = false;
        OrdersOnComplete = false;
        Covid19 = false;
    }

    public void setFromJSON(JSONObject data) throws JSONException {
        if (data.has("messages"))this.Messages = data.getBoolean("messages");
        if (data.has("unlim"))this.Unlim = data.getBoolean("unlim");
        if (data.has("rating"))this.Rating = data.getBoolean("rating");
        if (data.has("orders_on_complete"))this.OrdersOnComplete = data.getBoolean("orders_on_complete");
        if (data.has("covid19"))this.Covid19 = data.getBoolean("covid19");
    }

    public Boolean getMessages() {
        return Messages;
    }

    public Boolean getUnlim() {
        return Unlim;
    }

    public Boolean getRating() {
        return Rating;
    }

    public Boolean getCovid19() {
        return Covid19;
    }

    public Boolean getOrdersOnComplete() {
        return OrdersOnComplete;
    }
}
