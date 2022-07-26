package com.makepe.blackout.GettingStarted.Models;

public class GroupChat {
    String groupID, chatID, senderID, message, timeStamp, mediaURL, audioURL, message_type, videoURL;
    private double latitude, longitude;

    public GroupChat() {
    }

    public GroupChat(String groupID, String chatID, String senderID, String message, String timeStamp,
                     String mediaURL, String audioURL, String message_type, String videoURL,
                     double latitude, double longitude) {
        this.groupID = groupID;
        this.chatID = chatID;
        this.senderID = senderID;
        this.message = message;
        this.timeStamp = timeStamp;
        this.mediaURL = mediaURL;
        this.audioURL = audioURL;
        this.message_type = message_type;
        this.videoURL = videoURL;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMediaURL() {
        return mediaURL;
    }

    public void setMediaURL(String mediaURL) {
        this.mediaURL = mediaURL;
    }

    public String getAudioURL() {
        return audioURL;
    }

    public void setAudioURL(String audioURL) {
        this.audioURL = audioURL;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
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
