package it.polito.maddroid.lab3.common;


import java.io.Serializable;


public class Restaurant implements Serializable {

    private String restaurantID;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
    private String categoriesIds;

    public Restaurant(String restaurantID, String name, String description, String address, String phone, String email, String categoriesIds){
        this.restaurantID = restaurantID;
        this.name = name ;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.categoriesIds = categoriesIds;
    }

    public String getRestaurantID(){
        return restaurantID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone(){
        return phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getCategoriesIds() {
        return categoriesIds;
    }
    
    public boolean matchesCategoryId(String categoryId) {
        for (String id : categoriesIds.split(";")) {
            if (id.equals(categoryId))
                return true;
        }
        return false;
    }
}
