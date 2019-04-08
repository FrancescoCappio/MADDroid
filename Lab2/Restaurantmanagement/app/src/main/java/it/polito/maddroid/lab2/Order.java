package it.polito.maddroid.lab2;

import java.util.HashMap;


public class Order {

    private int id;
    
    private int timeHour;
    private int timeMinutes;
    
    //key = daily offer id
    //value = quantity
    private HashMap<Integer,Integer> dishes;
    
    private int customerId;
    
    private int riderId;
    
    Order(int id, int timeHour, int timeMinutes, int customerId, int riderId) {
        this.id = id;
        this.timeHour = timeHour;
        this.timeMinutes = timeMinutes;
        this.customerId = customerId;
        this.riderId = riderId;
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
}
