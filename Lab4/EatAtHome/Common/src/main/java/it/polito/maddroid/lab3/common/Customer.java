package it.polito.maddroid.lab3.common;


import java.io.Serializable;


public class Customer implements Serializable {
    
    private String userId;
    private String name;
    private String address;
    private String addressNotes;
    private String description;
    private String phoneNumber;
    private String emailAddress;
    
    public Customer(String userId, String name, String address, String addressNotes, String description, String phoneNumber, String emailAddress) {
        this.userId = userId;
        this.name = name;
        this.address = address;
        this.addressNotes = addressNotes;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public String getAddressNotes() {
        return addressNotes;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public String getEmailAddress() {
        return emailAddress;
    }
}
