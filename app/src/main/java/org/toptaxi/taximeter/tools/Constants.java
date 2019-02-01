package org.toptaxi.taximeter.tools;

import java.text.DecimalFormat;

public class Constants {
    public static final int CUR_VIEW_CUR_ORDERS   = 1;
    public static final int CUR_VIEW_TAXIMETER    = 2;
    public static final int CUR_VIEW_VIEW_ORDER   = 3;
    public static final int CUR_VIEW_CUR_ORDER    = 4;
    public static final int CUR_VIEW_PARKINGS     = 5;

    public static final int GPS_FIXED = 0;
    public static final int GPS_NOT_FIXED = 1;
    public static final int GPS_OFF = 2;
    public static final int GPS_ACCURACY     = 30;
    public static final int GPS_MIN_DISTANCE = 10;

    public static final int DRIVER_OFFLINE = 0;
    public static final int DRIVER_ONLINE = 1;
    public static final int DRIVER_ON_ORDER = 2;

    public static final int DOT_HTTP_ERROR      = 101;
    public static final int DOT_REST_ERROR      = 102;
    public static final int DOT_REST_OK         = 103;
    public static final int DOT_PHONE_WRONG     = 104;
    public static final int DOT_PASSWORD_WRONG  = 105;
    public static final int DOT_IDENTIFICATION  = 106;
    public static final int DOT_DRIVER_WRONG    = 107;
    public static final int DOT_NEW_VERSION     = 108;
    public static final int DOT_BLOCKED         = 109;

    public static final int ACTIVITY_LOGIN      = 100;
    public static final int ACTIVITY_SPLASH     = 101;

    public static final int MENU_CALL_DISPATCHING       = 200;
    public static final int MENU_CALL_ADMINISTRATION    = 201;
    public static final int MENU_BALANCE                = 202;
    public static final int MENU_ACITVATE_UNLIM         = 203;
    public static final int MENU_CUR_ORDER              = 204;
    public static final int MENU_TAXIMETER              = 205;
    public static final int MENU_THEME                  = 206;
    public static final int MENU_MESSAGES               = 207;
    public static final int MENU_STATISTICS             = 208;
    public static final int MENU_SHARE_DRIVER           = 209;
    public static final int MENU_HIS_ORDERS             = 210;
    public static final int MENU_TEMPLATE_MESSAGE       = 211;
    public static final int MENU_FAQ                    = 212;
    public static final int MENU_SHARE_CLIENT           = 213;

    public static final int MAIN_ACTION_GO_ONLINE       = 100;
    public static final int MAIN_ACTION_GO_OFFLINE      = 101;
    public static final int MAIN_ACTION_ACTIVATE_UNLIM  = 102;
    public static final int MAIN_ACTION_ORDER_SETTINGS  = 103;
    public static final int MAIN_ACTION_PRIOR_ORDER     = 104;
    public static final int MAIN_ACTION_SEND_MESSAGE    = 105;
    public static final int MAIN_ACTION_PARKINGS        = 106;
    public static final int MAIN_ACTION_ORDERS_COMPLETE = 107;

    public static final int DATABASE_VERSION            = 1;

    public static final int ORDER_VIEW_ORDER            = 1;
    public static final int ORDER_VIEW_NEW_ORDER        = 2;
    public static final int ORDER_VIEW_CUR_ORDER        = 3;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 101;


    public static final int ROUTE_POINT_TYPE_UNKNOWN        = 0;
    public static final int ROUTE_POINT_TYPE_STREET         = 1;
    public static final int ROUTE_POINT_TYPE_HOUSE          = 2;
    public static final int ROUTE_POINT_TYPE_LOCALITY       = 3;
    public static final int ROUTE_POINT_TYPE_POINT          = 4;
    public static final int ROUTE_POINT_TYPE_AIRPORT        = 100;
    public static final int ROUTE_POINT_TYPE_STATION        = 101;



    public static String getDistance(Double Distance){
        String result = "0";
        if (Distance > 0){
            if (Distance < 1000){
                String pattern = "##0";
                DecimalFormat decimalFormat = new DecimalFormat(pattern);
                String format = decimalFormat.format(Distance);
                result = "~" + format + " м.";
            }
            else{
                String pattern = "##0.00";
                DecimalFormat decimalFormat = new DecimalFormat(pattern);
                String format = decimalFormat.format(Distance/1000);
                result = "~" + format + " км.";
            }
        }

        return result;
    }

}
