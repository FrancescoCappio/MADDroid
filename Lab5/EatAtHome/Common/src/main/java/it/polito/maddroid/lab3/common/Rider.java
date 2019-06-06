package it.polito.maddroid.lab3.common;


import java.io.Serializable;


public class Rider implements Serializable, Comparable<Rider> {
    
    private String id;
    private String name;
    private String email;
    private String description;
    private String phoneNumber;
    private long totalReviewsCount;
    private float averageReview;
    private float distance;
    
    public Rider(String id) {
        this.id = id;
    }
    
    public Rider(String id, String name, String email, float distance) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.distance = distance;
    }

    public Rider(String id, String name, String email, String description, String phoneNumber, float distance) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.distance = distance;
        this.description = description;
        this.phoneNumber = phoneNumber;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public float getDistance() {
        return distance;
    }

    public String getDescription() {
        return description;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        
        Rider rider = (Rider) o;
    
        return id != null ? id.equals(rider.id) : rider.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    
    @Override
    public int compareTo(Rider o) {
        if (getDistance() <= o.getDistance())
            return -1;
        return 1;
    }

    public long getTotalReviewsCount() {
        return totalReviewsCount;
    }

    public void setTotalReviewsCount(long totalReviewsCount) {
        this.totalReviewsCount = totalReviewsCount;
    }

    public float getAverageReview() {
        return averageReview;
    }

    public void setAverageReview(float averageReview) {
        this.averageReview = averageReview;
    }
}
