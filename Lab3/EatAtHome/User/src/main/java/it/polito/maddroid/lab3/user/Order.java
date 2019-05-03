package it.polito.maddroid.lab3.user;


import java.io.Serializable;


public class Order implements Serializable {
    
    private String orderId;
    private String customerId;
    private String restaurantId;
    private String riderId;
    
    private String deliveryTime;
    private String date;
    
    private String totalCost;
    
    private OrderStatus orderStatus;
    
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        COMPLETED,
        DECLINED
    }
}
