package org.toptaxi.taximeter.data;

import org.json.JSONException;
import org.json.JSONObject;

public class ClientTariff {
    public double Tariff, RemoteKM, RemoteTariff, IdleCost;
    public int LandingCost, PointCost, MinCost, Rounding, IdleTime;

    public ClientTariff() {
    }

    public void setFromJSON(JSONObject data) throws JSONException {
        if (data.has("Tariff")) {this.Tariff = data.getDouble("Tariff");}
        if (data.has("LandingCost")) {this.LandingCost = data.getInt("LandingCost");}
        if (data.has("PointCost")) {this.PointCost = data.getInt("PointCost");}
        if (data.has("RemoteKM")) {this.RemoteKM = data.getDouble("RemoteKM");}
        if (data.has("RemoteTariff")) {this.RemoteTariff = data.getDouble("RemoteTariff");}
        if (data.has("MinCost")) {this.MinCost = data.getInt("MinCost");}
        if (data.has("Rounding")) {this.Rounding = data.getInt("Rounding");}
        if (data.has("IdleCost")) {this.IdleCost = data.getDouble("IdleCost");}
        if (data.has("IdleTime")) {this.IdleTime = data.getInt("IdleTime");}

    }
}
