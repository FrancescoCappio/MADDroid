package it.polito.maddroid.lab3.common;

import java.io.Serializable;
import java.util.Map;

public class Order implements Serializable, Comparable<Order> {

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

    private String restaurantName;
    private String customerName;

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

    public String getCustomerName() {
        return customerName;
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

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public void setRiderId(String s) {
        this.riderId = s;
    }

    public void setCustomerName(String s) {
        this.customerName = s;
    }
    
    @Override
    public int compareTo(Order o) {
        
        if (orderStatus != o.orderStatus) {
    
            if (orderStatus == EAHCONST.OrderStatus.PENDING)
                return -1;
    
            if (o.orderStatus == EAHCONST.OrderStatus.PENDING)
                return 1;
    
            if (orderStatus == EAHCONST.OrderStatus.CONFIRMED)
                return -1;
    
            if (o.orderStatus == EAHCONST.OrderStatus.CONFIRMED)
                return 1;
    
        }
        
        if (date.equals(o.date)) {
            return o.deliveryTime.compareTo(deliveryTime);
        }
        
        return o.date.compareTo(date);
    }
}
