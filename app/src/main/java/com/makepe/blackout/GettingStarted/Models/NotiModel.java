package com.makepe.blackout.GettingStarted.Models;

public class NotiModel {
    private String userid, text, postid, timeStamp;
    private boolean ispost, isStory;

    public NotiModel(String userid, String text, String postid, String timeStamp, boolean ispost, boolean isStory) {
        this.userid = userid;
        this.text = text;
        this.postid = postid;
        this.timeStamp = timeStamp;
        this.ispost = ispost;
        this.isStory = isStory;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public NotiModel() {
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public boolean isIspost() {
        return ispost;
    }

    public void setIspost(boolean ispost) {
        this.ispost = ispost;
    }

    public boolean isStory() {
        return isStory;
    }

    public void setStory(boolean story) {
        isStory = story;
    }
}
