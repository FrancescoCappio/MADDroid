package it.polito.maddroid.lab2;

import java.util.HashMap;
import java.util.Objects;


public class Order {
    
    public Order(Order o) {
        this.id = o.id;
        this.customerId = o.customerId;
        this.riderId = o.riderId;
        this.dishes = new HashMap<>();
        
        this.timeHour = o.timeHour;
        this.timeMinutes = o.timeMinutes;
        
        if (o.dishes != null)
            for (int i: o.dishes.keySet())
                this.dishes.put(i, o.dishes.get(i));
    }
    
    private int id;

    private int timeHour;
    private int timeMinutes;


    //key = daily offer id
    //value = quantity
    private HashMap<Integer,Integer> dishes;

    private int customerId;

    private int riderId;

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getRiderId() {
        return riderId;
    }

    public void setRiderId(int riderId) {
        this.riderId = riderId;
    }

    Order(int id, int timeHour, int timeMinutes, int customerId, int riderId) {
        this.id = id;
        this.timeHour = timeHour;
        this.timeMinutes = timeMinutes;
        this.customerId = customerId;
        this.riderId = riderId;
        this.dishes = new HashMap<Integer, Integer>();
    }

    public int getId() {
        return id;
    }

    public int getTimeHour() {
        return timeHour;
    }

    public int getTimeMinutes() {
        return timeMinutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    public HashMap<Integer, Integer> getDishes() {
        if(dishes == null)
            dishes = new HashMap<Integer, Integer>();
        return dishes;
    }
}
