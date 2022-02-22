package com.makepe.blackout.GettingStarted.Models;

public class Story {

    private String PostImage;
    private long timestart;
    private long timeend;
    private String storyid;
    private String userid;
    private String storyOwner;
    private String userNum;
    private String storyTimeStamp;

    public Story() {
    }

    public Story(String PostImage, long timestart, long timeend, String storyid, String userid, String storyOwner, String userNum, String storyTimeStamp) {
        this.PostImage = PostImage;
        this.timestart = timestart;
        this.timeend = timeend;
        this.storyid = storyid;
        this.userid = userid;
        this.storyOwner = storyOwner;
        this.userNum = userNum;
        this.storyTimeStamp = storyTimeStamp;
    }

    public String getStoryTimeStamp() {
        return storyTimeStamp;
    }

    public void setStoryTimeStamp(String storyTimeStamp) {
        this.storyTimeStamp = storyTimeStamp;
    }

    public String getPostImage() {
        return PostImage;
    }

    public void setPostImage(String postImage) {
        PostImage = postImage;
    }

    public long getTimestart() {
        return timestart;
    }

    public void setTimestart(long timestart) {
        this.timestart = timestart;
    }

    public long getTimeend() {
        return timeend;
    }

    public void setTimeend(long timeend) {
        this.timeend = timeend;
    }

    public String getStoryid() {
        return storyid;
    }

    public void setStoryid(String storyid) {
        this.storyid = storyid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getStoryOwner() {
        return storyOwner;
    }

    public void setStoryOwner(String storyOwner) {
        this.storyOwner = storyOwner;
    }

    public String getUserNum() {
        return userNum;
    }

    public void setUserNum(String userNum) {
        this.userNum = userNum;
    }
}
