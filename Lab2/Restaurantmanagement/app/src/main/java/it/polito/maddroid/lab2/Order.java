package it.polito.maddroid.lab2;


import java.util.Date;
import java.util.HashMap;


public class Order {
    
    private int id;
    
    private Date dateTime;
    
    //key = daily offer id
    //value = quantity
    private HashMap<Integer,Integer> dishes;
    
    private int customerId;
    
    private int riderId;
}
