package com.makepe.blackout.GettingStarted.Models;

public class User {
    private String userID, username, biography, dateOfBirth, phoneNumber, terms, imageURL,
            coverURL, timeStamp, gender;
    private double longitude, latitude;
    private boolean isVerified;

    public User() {
    }

    public User(String userID, String username, String biography, String dateOfBirth, String phoneNumber,
                String terms, String imageURL, String coverURL, String timeStamp, String gender,
                double longitude, double latitude, boolean isVerified) {
        this.userID = userID;
        this.username = username;
        this.biography = biography;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.terms = terms;
        this.imageURL = imageURL;
        this.coverURL = coverURL;
        this.timeStamp = timeStamp;
        this.gender = gender;
        this.longitude = longitude;
        this.latitude = latitude;
        this.isVerified = isVerified;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getCoverURL() {
        return coverURL;
    }

    public void setCoverURL(String coverURL) {
        this.coverURL = coverURL;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
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
}
