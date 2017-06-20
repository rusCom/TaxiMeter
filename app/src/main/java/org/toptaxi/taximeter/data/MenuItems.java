package org.toptaxi.taximeter.data;

import org.json.JSONException;
import org.json.JSONObject;

public class MenuItems {
    private Boolean Messages, DispatcherMessages, Unlim, Rating, Friends;

    public MenuItems() {
        Messages = false;
        DispatcherMessages = false;
        Unlim = false;
        Rating = false;
        Friends = false;
    }

    public void setFromJSON(JSONObject data) throws JSONException {
        if (data.has("messages"))this.Messages = data.getBoolean("messages");
        if (data.has("dispatcher_messages"))this.DispatcherMessages = data.getBoolean("dispatcher_messages");
        if (data.has("unlim"))this.Unlim = data.getBoolean("unlim");
        if (data.has("rating"))this.Rating = data.getBoolean("rating");
        if (data.has("friends"))this.Friends = data.getBoolean("friends");
    }

    public Boolean getMessages() {
        return Messages;
    }

    public Boolean getDispatcherMessages() {
        return DispatcherMessages;
    }

    public Boolean getUnlim() {
        return Unlim;
    }

    public Boolean getRating() {
        return Rating;
    }

    public Boolean getFriends() {
        return Friends;
    }
}
