package it.polito.maddroid.lab3.common;


public class RestaurantCategory {
    
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
