package com.makepe.blackout.GettingStarted.Models;

import java.util.ArrayList;

public class Chat {
    private String senderID, receiverID, message, audioURL, timeStamp, message_type, mediaURL, chatID, videoURL, storyID;
    private ArrayList<String> mediaUriList;
    private boolean isSeen;
    private double latitude, longitude;

    public Chat() {
    }

    public Chat(String senderID, String receiverID, String message, String audioURL, String timeStamp, String message_type,
                String mediaURL, String chatID, String videoURL, String storyID, ArrayList<String> mediaUriList,
                boolean isSeen, double latitude, double longitude) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.message = message;
        this.audioURL = audioURL;
        this.timeStamp = timeStamp;
        this.message_type = message_type;
        this.mediaURL = mediaURL;
        this.chatID = chatID;
        this.videoURL = videoURL;
        this.storyID = storyID;
        this.mediaUriList = mediaUriList;
        this.isSeen = isSeen;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAudioURL() {
        return audioURL;
    }

    public void setAudioURL(String audioURL) {
        this.audioURL = audioURL;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public String getMediaURL() {
        return mediaURL;
    }

    public void setMediaURL(String mediaURL) {
        this.mediaURL = mediaURL;
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public String getStoryID() {
        return storyID;
    }

    public void setStoryID(String storyID) {
        this.storyID = storyID;
    }

    public ArrayList<String> getMediaUriList() {
        return mediaUriList;
    }

    public void setMediaUriList(ArrayList<String> mediaUriList) {
        this.mediaUriList = mediaUriList;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
