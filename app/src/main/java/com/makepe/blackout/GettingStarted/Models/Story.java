package com.makepe.blackout.GettingStarted.Models;

public class Story {

    private String storyID, userID, storyTimeStamp, storyCaption, imageURL, storyType, storyAudioUrl;
    private long timeStart, timeEnd;
    private double longitude, latitude;

    public Story() {
    }

    public Story(String storyID, String userID, String storyTimeStamp,String storyCaption, String imageURL,
                 String storyType, String storyAudioUrl, long timeStart, long timeEnd, double longitude, double latitude) {
        this.storyID = storyID;
        this.userID = userID;
        this.storyTimeStamp = storyTimeStamp;
        this.storyCaption = storyCaption;
        this.imageURL = imageURL;
        this.storyType = storyType;
        this.storyAudioUrl = storyAudioUrl;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getStoryType() {
        return storyType;
    }

    public void setStoryType(String storyType) {
        this.storyType = storyType;
    }

    public String getStoryAudioUrl() {
        return storyAudioUrl;
    }

    public void setStoryAudioUrl(String storyAudioUrl) {
        this.storyAudioUrl = storyAudioUrl;
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

    public String getStoryID() {
        return storyID;
    }

    public void setStoryID(String storyID) {
        this.storyID = storyID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getStoryTimeStamp() {
        return storyTimeStamp;
    }

    public void setStoryTimeStamp(String storyTimeStamp) {
        this.storyTimeStamp = storyTimeStamp;
    }

    public String getStoryCaption() {
        return storyCaption;
    }

    public void setStoryCaption(String storyCaption) {
        this.storyCaption = storyCaption;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public long getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(long timeStart) {
        this.timeStart = timeStart;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }
}
