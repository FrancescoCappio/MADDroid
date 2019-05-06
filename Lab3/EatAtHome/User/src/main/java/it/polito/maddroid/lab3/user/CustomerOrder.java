package it.polito.maddroid.lab3.user;


import it.polito.maddroid.lab3.common.Order;


public class CustomerOrder extends Order {
    
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
