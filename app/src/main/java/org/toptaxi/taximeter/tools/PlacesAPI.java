package org.toptaxi.taximeter.tools;


import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.data.DOTResponse;
import org.toptaxi.taximeter.data.RoutePoint;

import java.util.ArrayList;
import java.util.Locale;

public class PlacesAPI {
    private static String TAG = "#########" + PlacesAPI.class.getName();
    private GoogleApiClient mGoogleApiClient;
    private String geoIP, geoPort;

    public PlacesAPI() {
        geoIP = "lk.toptaxi.org";
        geoPort = "5875";
    }

    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }


    public void getCurPlace(){
        RoutePoint routePoint = getCurPlaceByCache();
        if (routePoint == null)routePoint = getCurPlaceByGoogleAPIClient();

        if (routePoint != null)MainApplication.getInstance().setCurPlaceName(routePoint.getName() + "(" + routePoint.getDescription() + ")");
        else MainApplication.getInstance().setCurPlaceName("");

    }

    private RoutePoint getCurPlaceByCache(){
        RoutePoint resultRoutePoint = null;
        Location mLocation = MainApplication.getInstance().getMainLocation();
        if (mLocation != null){
            String httpRequest = "http://" + geoIP + ":" + geoPort + "/get_android_location_point?latitude=" + mLocation.getLatitude() + "&longitude=" + mLocation.getLongitude();
            Log.d(TAG, "getCurPlaceByCache " + httpRequest);
            DOTResponse dotResponse = MainApplication.getInstance().getDot().httpGetDOT(httpRequest);
            if (dotResponse.getCode() == 200){
                resultRoutePoint = new RoutePoint();
                try {
                    resultRoutePoint.setFromJSON(new JSONObject(dotResponse.getBody()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
        return resultRoutePoint;

    }

    private RoutePoint getCurPlaceByGoogleAPIClient(){
        RoutePoint resultRoutePoint = null;
        if (mGoogleApiClient != null){
            Location mLocation = MainApplication.getInstance().getMainLocation();
            if (mLocation != null){
                String httpRequest = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + String.valueOf(mLocation.getLatitude()) +"," + String.valueOf(mLocation.getLongitude());
                httpRequest += "&language=" + Locale.getDefault().toString();
                Log.d(TAG, "getCurPlaceByGoogleAPIClient " + httpRequest);
                DOTResponse dotResponse = MainApplication.getInstance().getDot().httpGetDOT(httpRequest);
                if (dotResponse.getCode() == 200){
                    try {
                        JSONObject resultJSON = new JSONObject(dotResponse.getBody());
                        if (resultJSON.getString("status").equals("OK")){
                            ArrayList<RoutePoint> routePoints = new ArrayList<>();
                            JSONArray routePointsJSON = resultJSON.getJSONArray("results");
                            for (int routePointItem = 0; routePointItem < routePointsJSON.length(); routePointItem++){
                                JSONObject point = routePointsJSON.getJSONObject(routePointItem);
                                int PlaceType = Constants.ROUTE_POINT_TYPE_UNKNOWN;
                                //Log.d(TAG, "getByLocation point = " + point.toString());
                                JSONArray address_components = point.getJSONArray("address_components");
                                String Name = "", HouseNumber = "", StreetName = "";
                                JSONArray types = new JSONArray();
                                for (int itemID = 0; itemID < address_components.length(); itemID++){
                                    JSONObject address_component = address_components.getJSONObject(itemID);
                                    types = address_component.getJSONArray("types");
                                    if (types.toString().contains("street_number")){
                                        HouseNumber = address_component.getString("short_name");
                                        PlaceType = Constants.ROUTE_POINT_TYPE_HOUSE;
                                    }
                                    else if (types.toString().contains("route")){
                                        if (PlaceType == Constants.ROUTE_POINT_TYPE_UNKNOWN){
                                            StreetName = address_component.getString("long_name");
                                            PlaceType = Constants.ROUTE_POINT_TYPE_STREET;
                                        }
                                        else StreetName = address_component.getString("short_name");
                                    }
                                    else if (types.toString().contains("train_station")){
                                        PlaceType = Constants.ROUTE_POINT_TYPE_STATION;
                                        Name = address_component.getString("long_name");
                                    }
                                    else if (types.toString().contains("airport")){
                                        PlaceType = Constants.ROUTE_POINT_TYPE_AIRPORT;
                                        Name = address_component.getString("long_name");
                                    }
                                }
                                if (StreetName.equals("Unnamed Road"))PlaceType = Constants.ROUTE_POINT_TYPE_UNKNOWN;

                                if (PlaceType != Constants.ROUTE_POINT_TYPE_UNKNOWN){
                                    if (Name.equals("")){
                                        Name = StreetName;
                                        if (!HouseNumber.equals(""))Name += ", " + HouseNumber;
                                    }
                                    String Description = point.getString("formatted_address").replace(Name + ", ", "");
                                    Description = Description.trim();
                                    Double lat = point.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                                    Double lng = point.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                                    RoutePoint routePoint = new RoutePoint();
                                    routePoint.setAllData(point.getString("place_id"), Name, Description, lat, lng, PlaceType, types.toString());
                                    routePoints.add(routePoint);
                                }

                            }
                            if (routePoints.size() == 1)resultRoutePoint = routePoints.get(0);
                            else if (routePoints.size() > 1)resultRoutePoint = routePoints.get(routePoints.size() - 1);

                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                try {
                    if (resultRoutePoint != null){
                        String url = "http://" + geoIP + ":" + geoPort + "/set_android_location_point";
                        JSONObject cacheData = new JSONObject();
                        cacheData.put("latitude", String.valueOf(mLocation.getLatitude()));
                        cacheData.put("longitude", String.valueOf(mLocation.getLongitude()));
                        cacheData.put("point", resultRoutePoint.toJSON());
                        Log.d(TAG, url + ";" + cacheData.toString());
                        MainApplication.getInstance().getDot().httpPostDOT(url, cacheData.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }


        return resultRoutePoint;
    }
}
