package org.toptaxi.taximeter.data;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class RoutePoint {
    private String Name;
    private String Description;
    private String Kind;
    private Double Latitude;
    private Double Longitude;

    RoutePoint(JSONObject data) throws JSONException {
        setFromJSON(data);

    }

    public void setFromJSON(JSONObject data) throws JSONException {


        if (data.has("Name")){this.Name = data.getString("Name");}

        if (data.has("Kind")){this.Kind = data.getString("Kind");}

        if (data.has("lt")){this.Latitude = data.getDouble("lt");}
        if (data.has("ln")){this.Longitude = data.getDouble("ln");}
        if (data.has("dsc")){this.Description = data.getString("dsc");}
        if (data.has("name")){this.Name = data.getString("name");}
        if (Description != null){
            Description = Description.replace("ufim", "Уфимский район, Республика Башкортостан");
            Description = Description.replace("ufa", "Уфа, Республика Башкортостан");
        }
    }

    public Double getLatitude() {
        return Latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public LatLng getLatLng(){
        return new LatLng(Latitude, Longitude);
    }

    public String getName() {
        return Name;
    }

    public String getNameForFind(){
        String result = Name;
        if (Kind.equals("street")){
            result += ", улица";
        }
        return result;
    }

}
