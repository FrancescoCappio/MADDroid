package it.polito.maddroid.lab3.common;


import java.io.Serializable;


public class Dish implements Serializable {

    private int dishID;
    private String name;
    private String description;
    private float price;
    private int quantity;
    
    // necessary to mark the dish to be updated
    private boolean update;

    public Dish (int dishID, String name, float price, String description){
        this.dishID = dishID;
        this.name = name;
        this.description = description;
        this.price = price;
        this.update = false;
        this.quantity = 0;
    }

    public int getDishID() { return dishID; }

    public String getDescription() { return description; }

    public String getName() { return name; }

    public float getPrice() { return price; }
    
    public boolean getUpdate() {
        return update;
    }
    
    public void markUpdated() {
        update = !update;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
