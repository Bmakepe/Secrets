package com.makepe.blackout.GettingStarted.Models;

public class GroupChat {
    String groupID, chatID, senderID, message, timeStamp, media, audio, msg_type;

    public GroupChat() {
    }

    public GroupChat(String groupID, String chatID, String senderID,
                     String message, String timeStamp, String media, String audio, String msg_type) {
        this.groupID = groupID;
        this.chatID = chatID;
        this.senderID = senderID;
        this.message = message;
        this.timeStamp = timeStamp;
        this.media = media;
        this.audio = audio;
        this.msg_type = msg_type;
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

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }
}
