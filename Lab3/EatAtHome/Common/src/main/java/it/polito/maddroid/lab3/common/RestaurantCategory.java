package it.polito.maddroid.lab3.common;


import java.io.Serializable;


public class RestaurantCategory implements Serializable {
    
    private String id;
    private String name;
    
    public RestaurantCategory(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
}
