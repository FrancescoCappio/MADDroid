package it.polito.maddroid.lab3.common;


import java.io.Serializable;


public class Restaurant implements Serializable, Comparable<Restaurant> {

    private String restaurantID;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
    private String categoriesIds;
    private String timeTableString;
    private EAHCONST.GeoLocation geoLocation;
    private int reviewCount = 0;
    private float reviewAvg = 0.0f;
    private int avgOrderTime = 15;

    public Restaurant(String restaurantID, String name, String description, String address, String phone, String email, String categoriesIds, String timeTableString){
        this.restaurantID = restaurantID;
        this.name = name ;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.categoriesIds = categoriesIds;
        this.timeTableString = timeTableString;
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
    
    public String getTimeTableString() {
        return timeTableString;
    }
    
    public int getReviewCount() {
        return reviewCount;
    }
    
    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public float getReviewAvg() {
        return reviewAvg;
    }
    
    public void setReviewAvg(float reviewAvg) {
        this.reviewAvg = reviewAvg;
    }
    
    @Override
    public int compareTo(Restaurant o) {
        return (int) (o.getReviewAvg() - getReviewAvg());
    }
    
    public int getAvgOrderTime() {
        return avgOrderTime;
    }
    
    public void setAvgOrderTime(int avgOrderTime) {
        this.avgOrderTime = avgOrderTime;
    }
    
    public EAHCONST.GeoLocation getGeoLocation() {
        return geoLocation;
    }
    
    public void setGeoLocation(EAHCONST.GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }
}
