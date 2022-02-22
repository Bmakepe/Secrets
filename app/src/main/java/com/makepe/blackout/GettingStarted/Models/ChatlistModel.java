package com.makepe.blackout.GettingStarted.Models;

public class ChatlistModel {
    String id, timeStamp;

    public ChatlistModel(String id, String timeStamp) {
        this.id = id;
        this.timeStamp = timeStamp;
    }

    public ChatlistModel() {
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
