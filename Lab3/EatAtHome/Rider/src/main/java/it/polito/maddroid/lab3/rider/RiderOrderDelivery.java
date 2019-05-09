package it.polito.maddroid.lab3.rider;

import java.io.Serializable;

import it.polito.maddroid.lab3.common.EAHCONST;

public class RiderOrderDelivery implements Serializable, Comparable<RiderOrderDelivery> {

    private String orderId;
    private String restaurantId;
    private String customerId;
    private EAHCONST.OrderStatus orderStatus;
    
    private String restaurantName;
    private String restaurantAddress;
    private String deliveryAddress;
    private String deliveryTime;
    private String totalCost;
    private String deliveryDate;

    public RiderOrderDelivery(String orderId, String restaurantId, String riderId, EAHCONST.OrderStatus orderStatus) {
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
    
    public String getRestaurantAddress() {
        return restaurantAddress;
    }
    
    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }
    
    public String getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
    
    public String getDeliveryTime() {
        return deliveryTime;
    }
    
    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }
    
    public String getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }
    
    public String getDeliveryDate() {
        return deliveryDate;
    }
    
    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
    
    public String getRestaurantName() {
        return restaurantName;
    }
    
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    
    @Override
    public int compareTo(RiderOrderDelivery o) {
        
        if (orderStatus != o.orderStatus) {
            
            if (orderStatus == EAHCONST.OrderStatus.PENDING)
                return -1;
            
            if (o.orderStatus == EAHCONST.OrderStatus.PENDING)
                return 1;
            
            if (orderStatus == EAHCONST.OrderStatus.ONGOING)
                return -1;
            
            if (orderStatus == EAHCONST.OrderStatus.COMPLETED)
                return -1;
            
            if (orderStatus == EAHCONST.OrderStatus.CONFIRMED)
                return -1;
            
            if (o.orderStatus == EAHCONST.OrderStatus.CONFIRMED)
                return 1;
            
        }
        
        if (deliveryDate.equals(o.deliveryDate)) {
            return o.deliveryTime.compareTo(deliveryTime);
        }
        
        return o.deliveryDate.compareTo(deliveryDate);
    }
}
