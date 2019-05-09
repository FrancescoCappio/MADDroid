package it.polito.maddroid.lab3.rider;

import it.polito.maddroid.lab3.common.EAHCONST;

public class RiderOrder {

    private String orderId;
    private String restaurantId;
    private String customerId;
    private EAHCONST.OrderStatus orderStatus;

    public RiderOrder(String orderId, String restaurantId, String riderId, EAHCONST.OrderStatus orderStatus) {
        this.orderId = orderId;
        this.restaurantId = restaurantId;
        this.customerId = riderId;
        this.orderStatus = orderStatus;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public EAHCONST.OrderStatus getOrderStatus() {
        return orderStatus;
    }
}
