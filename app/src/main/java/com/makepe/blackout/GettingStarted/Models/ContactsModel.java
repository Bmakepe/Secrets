package com.makepe.blackout.GettingStarted.Models;

public class ContactsModel {

    private String username, phoneNumber, imageURL, userID;

    public ContactsModel() {
    }

    public ContactsModel(String username, String phoneNumber, String imageURL, String userID) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.imageURL = imageURL;
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
