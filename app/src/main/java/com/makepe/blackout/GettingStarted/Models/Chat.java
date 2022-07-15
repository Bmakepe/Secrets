package com.makepe.blackout.GettingStarted.Models;

import java.util.ArrayList;

public class Chat {
    private String sender, receiver, message, audio, timeStamp, message_type, media, chatID, videoURL, storyID;
    private ArrayList<String> mediaUriList;
    private boolean isSeen;
    private double latitude, longitude;

    public Chat() {
    }

    public Chat(String sender, String receiver, String message, String audio, String timeStamp,
                String message_type, String media, String chatID, String videoURL, String storyID,
                ArrayList<String> mediaUriList, boolean isSeen, double latitude, double longitude) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.audio = audio;
        this.timeStamp = timeStamp;
        this.message_type = message_type;
        this.media = media;
        this.chatID = chatID;
        this.videoURL = videoURL;
        this.storyID = storyID;
        this.mediaUriList = mediaUriList;
        this.isSeen = isSeen;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getStoryID() {
        return storyID;
    }

    public void setStoryID(String storyID) {
        this.storyID = storyID;
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

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public ArrayList<String> getMediaUriList() {
        return mediaUriList;
    }

    public void setMediaUriList(ArrayList<String> mediaUriList) {
        this.mediaUriList = mediaUriList;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
