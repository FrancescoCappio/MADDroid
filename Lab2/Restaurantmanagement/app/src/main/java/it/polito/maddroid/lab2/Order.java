package it.polito.maddroid.lab2;


import java.util.Date;
import java.util.HashMap;


public class Order {

    private int id;
    
    private String dateTime;
    
    //key = daily offer id
    //value = quantity
    private HashMap<Integer,Integer> dishes;
    
    private int customerId;
    
    private int riderId;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public HashMap<Integer, Integer> getDishes() {
        return dishes;
    }

    public void setDishes(HashMap<Integer, Integer> dishes) {
        this.dishes = dishes;
    }

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
}
