package it.polito.maddroid.lab3.common;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Order implements Serializable {

    public Order(String orderId, String totalCost, String riderId, String customerId, String restaurantId, String deliveryTime, String date, String deliveryAddress, EAHCONST.OrderStatus orderStatus) {
        this.orderId = orderId;
        this.totalCost = totalCost;
        this.riderId = riderId;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.deliveryTime = deliveryTime;
        this.date = date;
        this.deliveryAddress = deliveryAddress;
        this.orderStatus = orderStatus;
    }

    private String orderId;

    private String totalCost;

    private String riderId;
    private String customerId;
    private String restaurantId;

    private String deliveryTime;

    private String date;

    private String deliveryAddress;

    private Map<String,Integer> dishesMap;

    private EAHCONST.OrderStatus orderStatus;

    public String getOrderId() {
        return orderId;
    }

    public String getTotalCost() {
        return totalCost;
    }

    public String getRiderId() {
        return riderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public String getDate() {
        return date;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public Map<String, Integer> getDishesMap() {
        return dishesMap;
    }

    public EAHCONST.OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setDishesMap(Map<String, Integer> dishesMap) {
        this.dishesMap = dishesMap;
    }

    public void setOrderStatus(EAHCONST.OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
    
}
