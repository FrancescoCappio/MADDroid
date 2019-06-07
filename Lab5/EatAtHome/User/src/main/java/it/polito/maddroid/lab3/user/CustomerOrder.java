package it.polito.maddroid.lab3.user;


import it.polito.maddroid.lab3.common.EAHCONST;


public class CustomerOrder {
    
    private String orderId;
    private String restaurantId;
    private String riderId;
    private boolean riderRated = false;
    private boolean restaurantRated = false;
    private EAHCONST.OrderStatus orderStatus;
    private String stringControl;

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
    
    public void setRiderRated(boolean riderRated) {
        this.riderRated = riderRated;
    }
    
    public void setRestaurantRated(boolean restaurantRated) {
        this.restaurantRated = restaurantRated;
    }
    
    public boolean getRiderRated() {
        return riderRated;
    }
    
    public boolean getRestaurantRated() {
        return restaurantRated;
    }

    public String getStringControl() {
        return this.stringControl;
    }

    public void setControlString(String s) {
        this.stringControl = s;
    }
}
