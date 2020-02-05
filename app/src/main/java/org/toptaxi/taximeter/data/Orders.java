package org.toptaxi.taximeter.data;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Orders {
    protected static String TAG = "#########" + Orders.class.getName();
    private OnOrdersChangeListener onOrdersChangeListener;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private MediaPlayer mp;
    private List<Order> lOrders;

    public interface OnOrdersChangeListener {
        void OnOrdersChange();
    }

    public Orders() {
        mp = MediaPlayer.create(MainApplication.getInstance(), R.raw.new_order_view);
        mp.setLooping(false);
        lOrders = new ArrayList<>();
    }

    public void setOnOrdersChangeListener(OnOrdersChangeListener onOrdersChangeListener) {
        this.onOrdersChangeListener = onOrdersChangeListener;
    }

    public void setFromJSONPrior(JSONArray data) throws JSONException {
        lOrders.clear();
        for (int itemID = 0; itemID < data.length(); itemID++) {
            JSONObject orderJSON = data.getJSONObject(itemID);
            Order order = new Order(orderJSON);
            lOrders.add(order);
        }
    }

    public void setFromJSON(JSONArray data) throws JSONException {
        for (int itemID = 0; itemID < data.length(); itemID++) {
            JSONObject orderJSON = data.getJSONObject(itemID);
            Order sOrder = getByOrderID(orderJSON.getInt("ID"));
            //Log.d(TAG, "")
            if (sOrder == null) {
                Order order = new Order(orderJSON);
                lOrders.add(order);
                order.setNew(true);
                order.setLastRequestUID(MainApplication.getInstance().lastRequestUID);
            } else {
                sOrder.setFromJSON(orderJSON);
                sOrder.setNew(false);
                sOrder.setLastRequestUID(MainApplication.getInstance().lastRequestUID);
            }

        }

        for (int itemID = 0; itemID < lOrders.size(); itemID++) {
            Order sOrder = lOrders.get(itemID);
            if (sOrder.getLastRequestUID() != MainApplication.getInstance().lastRequestUID) {

                lOrders.remove(sOrder);
            }
        }


        if (lOrders != null) {
            try {
                Collections.sort(lOrders, new Comparator<Order>() {
                    public int compare(Order order1, Order order2) {
                        if (order1.getDistance() < order2.getDistance()) return -1;
                        else return 1;
                    }
                });
            } catch (Throwable t) {
                t.printStackTrace();
            }

        }

        if (onOrdersChangeListener != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    onOrdersChangeListener.OnOrdersChange();
                }
            });
        }
        // Если водитель не на заказе и включено, что надо озвучивать поступление заказа
        if ((MainApplication.getInstance().getMainAccount().getStatus() != 2) & (MainApplication.getInstance().getMainPreferences().getNewOrderAlarmCheck())) {
            // Провеяем "новые" заказы на необходимость озвучивать
            boolean isNewOrderAlarm = false;
            for (int itemID = 0; itemID < lOrders.size(); itemID++) {
                Order order = lOrders.get(itemID);
                if (order.isNew()) {
                    if ((order.getCost() >= MainApplication.getInstance().getMainPreferences().getNewOrderAlarmCost()) &
                            (
                                    MainApplication.getInstance().getMainPreferences().getNewOrderAlarmDistance() == -1 |
                                            order.getDistance() <= (MainApplication.getInstance().getMainPreferences().getNewOrderAlarmDistance() * 1000)
                            )
                    ) {
                        isNewOrderAlarm = true;
                    }
                }
            }
            if (isNewOrderAlarm) mp.start();

        }
        // if ((IsHaveNewOrder()) & ((MainApplication.getInstance().getMainAccount().getStatus() != 2)))mp.start();


    }

    private boolean IsHaveNewOrder() {
        for (int itemID = 0; itemID < lOrders.size(); itemID++) {
            Order o = lOrders.get(itemID);
            if (o.isNew()) { //return true;
                // проверяем, надо ли включать звук

            }
        }
        return false;
    }

    public Integer getOrderID(int itemID) {
        if (lOrders.size() < itemID) return null;
        if (lOrders.size() == 0) return null;
        if (itemID < 0) return null;
        return lOrders.get(itemID).getID();
    }

    public Order getByOrderID(Integer OrderID) {
        for (int itemID = 0; itemID < lOrders.size(); itemID++) {
            Order sOrder = lOrders.get(itemID);
            if (sOrder.getID().equals(OrderID)) return sOrder;
        }
        return null;
    }

    public int getCount() {
        if (MainApplication.getInstance().getCurViewParkingID() == 0) return lOrders.size();
        else {
            int count = 0;
            for (int itemID = 0; itemID < lOrders.size(); itemID++) {
                Order sOrder = lOrders.get(itemID);
                if (sOrder.getParkingID() == MainApplication.getInstance().getCurViewParkingID())
                    count++;
            }
            return count;
        }
    }

    public Order getOrder(int position) {
        if (lOrders.size() == 0) return null;
        if (lOrders.size() < position) return null;
        if (MainApplication.getInstance().getCurViewParkingID() == 0) return lOrders.get(position);
        else {
            int count = 0;
            for (int itemID = 0; itemID < lOrders.size(); itemID++) {
                Order sOrder = lOrders.get(itemID);
                if (sOrder.getParkingID() == MainApplication.getInstance().getCurViewParkingID()) {
                    if (count == position) return sOrder;
                    count++;
                }
            }
            return null;
        }

    }
}
