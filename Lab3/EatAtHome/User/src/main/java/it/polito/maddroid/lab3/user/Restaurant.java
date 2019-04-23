package it.polito.maddroid.lab3.user;

public class Restaurant {

    private String restaurantID;
    private String name;
    private String description;
    private String address;
    private String phone;

    public Restaurant(String restaurantID, String name, String description, String address, String phone){
        this.restaurantID = restaurantID;
        this.name = name ;
        this.description = description;
        this.address = address;
        this.phone = phone;
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



}
