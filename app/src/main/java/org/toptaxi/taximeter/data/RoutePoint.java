package org.toptaxi.taximeter.data;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class RoutePoint {
    private String Name, PlaceId, PlaceTypes;
    private String Description;
    private String Kind;
    private Double Latitude;
    private Double Longitude;
    Integer PlaceType;

    RoutePoint(JSONObject data) throws JSONException {
        setFromJSON(data);

    }

    public RoutePoint() {
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
        if (data.has("place_id")){this.PlaceId = data.getString("place_id");}
        if (data.has("place_type")){this.PlaceType = data.getInt("place_type");}
        if (data.has("place_types")){this.PlaceTypes = data.getString("place_types");}
    }

    public String getDescription() {
        return Description;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("place_id", PlaceId);
        jsonObject.put("name", Name);
        jsonObject.put("description", Description);
        jsonObject.put("latitude", String.valueOf(Latitude));
        jsonObject.put("longitude", String.valueOf(Longitude));
        jsonObject.put("place_type", String.valueOf(PlaceType));
        jsonObject.put("place_types", PlaceTypes);
        return jsonObject;
    }

    public void setAllData(String PlaceID, String Name, String Description, Double Latitude, Double Longitude, Integer PlaceType, String Types){
        this.PlaceId = PlaceID;
        this.Name = Name;
        this.Description = Description;
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.PlaceType = PlaceType;
        this.PlaceTypes = Types;
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
