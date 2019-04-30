package it.polito.maddroid.lab3.common;

public class Dish {

    private String dishID;
    private String name;
    private String description;
    private float price;

    public Dish (String dishID, String name, float price, String description){
        this.dishID = dishID;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public String getDishID() { return dishID; }

    public String getDescription() { return description; }

    public String getName() { return name; }

    public float getPrice() { return price; }
}
