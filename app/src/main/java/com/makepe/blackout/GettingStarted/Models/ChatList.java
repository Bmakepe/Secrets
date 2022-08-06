package com.makepe.blackout.GettingStarted.Models;

public class ChatList {
    public String userID;

    public ChatList() {
    }

    public ChatList(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
