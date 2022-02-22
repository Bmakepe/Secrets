package com.makepe.blackout.GettingStarted.Models;

public class User {
    private String USER_ID, Username, Bio, ImageURL, CoverURL, DOB, onlineStatus, Number, Gender, Terms;
    private double longitude, latitude;

    public User() {
    }

    public User(String USER_ID, String username, String bio, String imageURL, String coverURL, String DOB,
                String onlineStatus, String number, String gender, String terms, double longitude, double latitude) {
        this.USER_ID = USER_ID;
        Username = username;
        Bio = bio;
        ImageURL = imageURL;
        CoverURL = coverURL;
        this.DOB = DOB;
        this.onlineStatus = onlineStatus;
        Number = number;
        Gender = gender;
        Terms = terms;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getUSER_ID() {
        return USER_ID;
    }

    public void setUSER_ID(String USER_ID) {
        this.USER_ID = USER_ID;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getBio() {
        return Bio;
    }

    public void setBio(String bio) {
        Bio = bio;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }

    public String getCoverURL() {
        return CoverURL;
    }

    public void setCoverURL(String coverURL) {
        CoverURL = coverURL;
    }

    public String getDOB() {
        return DOB;
    }

    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getTerms() {
        return Terms;
    }

    public void setTerms(String terms) {
        Terms = terms;
    }
}
