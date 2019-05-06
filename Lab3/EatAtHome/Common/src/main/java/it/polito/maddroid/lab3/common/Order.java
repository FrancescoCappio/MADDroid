package it.polito.maddroid.lab3.common;


import java.io.Serializable;


public abstract class Order implements Serializable {
    
    private String orderId;
    
    private String riderId;
    private String customerId;
    private String restaurantId;
    
    private int totalCost;
    private String deliveryTime;
    private String date;
    
    
    
    
}