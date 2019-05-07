package it.polito.maddroid.lab3.user;


import it.polito.maddroid.lab3.common.EAHCONST;


public class CustomerOrder {
    
    private String orderId;
    private String restaurantId;
    private String riderId;
    private EAHCONST.OrderStatus orderStatus;
    
    public CustomerOrder(String orderId, String restaurantId, String riderId, EAHCONST.OrderStatus orderStatus) {
        this.orderId = orderId;
        this.restaurantId = restaurantId;
        this.riderId = riderId;
        this.orderStatus = orderStatus;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public String getRestaurantId() {
        return restaurantId;
    }
    
    public String getRiderId() {
        return riderId;
    }
    
    public EAHCONST.OrderStatus getOrderStatus() {
        return orderStatus;
    }
}
