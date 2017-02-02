package org.toptaxi.taximeter.adapters;


import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.Order;

public class RVCurOrdersAdapter extends RecyclerView.Adapter<RVCurOrdersAdapter.OrderViewHolder> {
    protected static String TAG = "#########" + RVCurOrdersAdapter.class.getName();
    private int mOrderType;



    public RVCurOrdersAdapter(int orderType) {
        mOrderType = orderType;
    }

    @Override
    public int getItemCount() {
        switch (mOrderType){
            case 0:return MainApplication.getInstance().getCurOrders().getCount();
            case 1:return MainApplication.getInstance().getPriorOrders().getCount();
        }
        return MainApplication.getInstance().getCurOrders().getCount();
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_cur_orders_list, viewGroup, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder orderViewHolder, final int position) {
        Order curOrder = null;
        switch (mOrderType){
            case 0:curOrder = MainApplication.getInstance().getCurOrders().getOrder(position);break;
            case 1:curOrder = MainApplication.getInstance().getPriorOrders().getOrder(position);break;
        }

        if (curOrder != null) {
            //orderViewHolder.tvDispatchingName.setText(curOrder.DispatchingName);
            orderViewHolder.tvPayType.setText(curOrder.getPayTypeName());
            orderViewHolder.tvCalcType.setText(curOrder.getCalcType());
            //orderViewHolder.tvRoute.setText(curOrder.getRoute());
            orderViewHolder.tvDistance.setText(curOrder.getDistanceString());
            orderViewHolder.tvFirstPointInfo.setText(curOrder.getFirstPointInfo());

            if (curOrder.getDispPay().equals("")){orderViewHolder.tvDispPay.setVisibility(View.GONE);}
            else {
                orderViewHolder.tvDispPay.setVisibility(View.VISIBLE);
                orderViewHolder.tvDispPay.setText(curOrder.getDispPay());
            }


            orderViewHolder.llPointInfo.setVisibility(View.GONE);
            orderViewHolder.llLastPointInfo.setVisibility(View.GONE);
            orderViewHolder.llNote.setVisibility(View.GONE);
            orderViewHolder.tvNote.setText("");

            if (curOrder.getRouteCount() > 1){
                orderViewHolder.llLastPointInfo.setVisibility(View.VISIBLE);
                orderViewHolder.tvLastPointInfo.setText(curOrder.getLastPointInfo());
            }
            if (curOrder.getRouteCount() == 3){
                orderViewHolder.llPointInfo.setVisibility(View.VISIBLE);
                orderViewHolder.tvPointInfo.setText(curOrder.getSecondPointInfo());
            }
            if (curOrder.getRouteCount() > 3){
                orderViewHolder.llPointInfo.setVisibility(View.VISIBLE);
            }

            if (curOrder.getNote() != null)
                if (!curOrder.getNote().equals("")){
                    orderViewHolder.llNote.setVisibility(View.VISIBLE);
                    orderViewHolder.tvNote.setText(curOrder.getNote());
                }

            if (curOrder.isNew() & mOrderType == 0){orderViewHolder.llTitle.setBackgroundResource(R.color.primaryRed);}
            else {
                switch (curOrder.getCheck()){
                    case 0:orderViewHolder.llTitle.setBackgroundResource(curOrder.getCaptionColor());break;
                    case 1:orderViewHolder.llTitle.setBackgroundResource(R.color.primaryGreen);break;
                    case 2:orderViewHolder.llTitle.setBackgroundResource(R.color.primaryGrayDark);break;
                }
            }



            if (curOrder.getPriorInfo().equals("")){
                orderViewHolder.llPrior.setVisibility(View.GONE);
                orderViewHolder.tvPrior.setText("");
            }
            else {
                orderViewHolder.llPrior.setVisibility(View.VISIBLE);
                orderViewHolder.tvPrior.setText(curOrder.getPriorInfo());
            }

            if (curOrder.getDispatchingName().equals("")){orderViewHolder.llDispName.setVisibility(View.GONE);}
            else {
                orderViewHolder.llDispName.setVisibility(View.VISIBLE);
                orderViewHolder.tvDispName.setText(curOrder.getDispatchingName());
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView tvPayType, tvCalcType, tvRoute, tvDistance, tvFirstPointInfo, tvLastPointInfo, tvPointInfo, tvNote, tvPrior, tvDispName, tvDispPay;
        LinearLayout llLastPointInfo, llPointInfo, llNote, llTitle, llPrior, llDispName;



        OrderViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cvCurOrdersList);
            tvPayType           = (TextView)itemView.findViewById(R.id.tvCurOrdersListPayType);
            tvDispPay           = (TextView)itemView.findViewById(R.id.tvCurOrdersListPayPercent);
            tvCalcType          = (TextView)itemView.findViewById(R.id.tvCurOrdersListCalcType);
            tvRoute             = (TextView)itemView.findViewById(R.id.tvCurOrdersListRoute);
            tvDistance          = (TextView)itemView.findViewById(R.id.tvCurOrdersListDistance);
            tvFirstPointInfo    = (TextView)itemView.findViewById(R.id.tvCurOrdersListRouteFirstPoint);
            tvLastPointInfo     = (TextView)itemView.findViewById(R.id.tvCurOrdersListRouteLastPoint);
            tvPointInfo         = (TextView)itemView.findViewById(R.id.tvCurOrdersListRoutePoint);
            tvNote              = (TextView)itemView.findViewById(R.id.tvOrderNote);
            tvPrior             = (TextView)itemView.findViewById(R.id.tvCurOrdersListPriorInfo);
            tvDispName          = (TextView)itemView.findViewById(R.id.tvOrderDispatching);
            llLastPointInfo     = (LinearLayout)itemView.findViewById(R.id.llCurOrdersListRouteLastPoint);
            llPointInfo         = (LinearLayout)itemView.findViewById(R.id.llCurOrdersListRoutePoint);
            llNote              = (LinearLayout)itemView.findViewById(R.id.llOrderNote);
            llTitle             = (LinearLayout)itemView.findViewById(R.id.llCurOrdersListTitle);
            llPrior             = (LinearLayout)itemView.findViewById(R.id.llCurOrdersListPriorInfo);
            llDispName          = (LinearLayout)itemView.findViewById(R.id.llOrderDispatching);
        }
    }
}
