package it.polito.maddroid.lab3.common;

public class Dish {
    
    private String dishID;
    private String name;
    private String description;
    private float price;
    private int quantity;
    
    public Dish (String dishID, String name, float price, String description){
        this.dishID = dishID;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = 0;
    }
    
    public String getDishID() { return dishID; }
    
    public String getDescription() { return description; }
    
    public String getName() { return name; }
    
    public float getPrice() { return price; }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}