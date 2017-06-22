package org.toptaxi.taximeter.data;

import org.json.JSONException;
import org.json.JSONObject;

public class MenuItems {
    private Boolean Messages, Unlim, Rating;

    public MenuItems() {
        Messages = false;
        Unlim = false;
        Rating = false;
    }

    public void setFromJSON(JSONObject data) throws JSONException {
        if (data.has("messages"))this.Messages = data.getBoolean("messages");
        if (data.has("unlim"))this.Unlim = data.getBoolean("unlim");
        if (data.has("rating"))this.Rating = data.getBoolean("rating");
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

}
