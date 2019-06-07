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
    private String DeliveryStringControl;
    private String riderId;
    private String customerId;
    private String restaurantId;
    private String restaurantName;
    private String customerName;
    private String deliveryTime;
    private String date;
    private String deliveryAddress;
    
    private boolean riderRated = false;
    private boolean restaurantRated = false;
    
    private int timeForDelivery = -1;

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

    public void setDeliveryStringControl(String s){ this.DeliveryStringControl = s; }

    public int getTimeForDelivery() {
        return timeForDelivery;
    }
    
    public void setTimeForDelivery(int timeForDelivery) {
        this.timeForDelivery = timeForDelivery;
    }
    
    public boolean getRiderRated() {
        return riderRated;
    }
    
    public void setRiderRated(boolean riderRated) {
        this.riderRated = riderRated;
    }
    
    public boolean getRestaurantRated() {
        return restaurantRated;
    }
    
    public void setRestaurantRated(boolean restaurantRated) {
        this.restaurantRated = restaurantRated;
    }
    
    public String getOrderReadyTime() {
        if (timeForDelivery == -1)
            return deliveryTime;
        
        String[] splitted = deliveryTime.split(":");
        
        int hour = Integer.parseInt(splitted[0]);
        int minute = Integer.parseInt(splitted[1]);
        
        if (minute > timeForDelivery) {
            int res = minute - timeForDelivery;
            return hour + ":" + res;
        } else {
            int totMinutes = hour*60 + minute;
            int time = totMinutes - timeForDelivery;
            
            hour = time/60;
            int remain = time - hour*60;
            return hour + ":" + remain;
        }
    }
    
    @Override
    public int compareTo(Order o) {
        
        if (orderStatus != o.orderStatus) {
    
            if (orderStatus == EAHCONST.OrderStatus.PENDING)
                return -1;
    
            if (o.orderStatus == EAHCONST.OrderStatus.PENDING)
                return 1;
    
            if (orderStatus == EAHCONST.OrderStatus.ONGOING)
                return -1;
    
            if (o.orderStatus == EAHCONST.OrderStatus.ONGOING)
                return 1;
    
            if (orderStatus == EAHCONST.OrderStatus.WAITING_RIDER)
                return -1;
    
            if (o.orderStatus == EAHCONST.OrderStatus.WAITING_RIDER)
                return 1;
    
            if (orderStatus == EAHCONST.OrderStatus.CONFIRMED)
                return -1;
    
            if (o.orderStatus == EAHCONST.OrderStatus.CONFIRMED)
                return 1;
    
        }
        
        if (date.equals(o.date)) {
            return this.deliveryTime.compareTo(o.deliveryTime);
        }
        
        return this.date.compareTo(o.date);
    }

    public String getDeliveryStringControl() {
        return  this.DeliveryStringControl;
    }
}
