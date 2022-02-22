package com.makepe.blackout.GettingStarted.Models;

public class ContactsModel {

    private String Username, Number, ImageURL, USER_ID;

    public ContactsModel() {
    }

    public ContactsModel(String username, String number, String imageURL, String USER_ID) {
        Username = username;
        Number = number;
        ImageURL = imageURL;
        this.USER_ID = USER_ID;
    }

    public ContactsModel(String username, String number) {
        Username = username;
        Number = number;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }

    public String getUSER_ID() {
        return USER_ID;
    }

    public void setUSER_ID(String USER_ID) {
        this.USER_ID = USER_ID;
    }
}
