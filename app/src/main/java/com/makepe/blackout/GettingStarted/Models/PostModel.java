package com.makepe.blackout.GettingStarted.Models;

public class PostModel {
    private String userID, postID, postCaption, postImage, postTime, postPrivacy, postType,
            videoURL, audioURL, sharedPost, movementID;
    private double longitude, latitude;
    private boolean commentsAllowed;

    public PostModel() {
    }

    public PostModel(String userID, String postID, String postCaption, String postImage, String postTime,
                     String postPrivacy, String postType, String videoURL, String audioURL, String sharedPost,
                     String movementID, double longitude, double latitude, boolean commentsAllowed) {
        this.userID = userID;
        this.postID = postID;
        this.postCaption = postCaption;
        this.postImage = postImage;
        this.postTime = postTime;
        this.postPrivacy = postPrivacy;
        this.postType = postType;
        this.videoURL = videoURL;
        this.audioURL = audioURL;
        this.sharedPost = sharedPost;
        this.movementID = movementID;
        this.longitude = longitude;
        this.latitude = latitude;
        this.commentsAllowed = commentsAllowed;
    }

    public boolean isCommentsAllowed() {
        return commentsAllowed;
    }

    public void setCommentsAllowed(boolean commentsAllowed) {
        this.commentsAllowed = commentsAllowed;
    }

    public String getAudioURL() {
        return audioURL;
    }

    public void setAudioURL(String audioURL) {
        this.audioURL = audioURL;
    }

    public String getMovementID() {
        return movementID;
    }

    public void setMovementID(String movementID) {
        this.movementID = movementID;
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

    public String getSharedPost() {
        return sharedPost;
    }

    public void setSharedPost(String sharedPost) {
        this.sharedPost = sharedPost;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getPostCaption() {
        return postCaption;
    }

    public void setPostCaption(String postCaption) {
        this.postCaption = postCaption;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getPostPrivacy() {
        return postPrivacy;
    }

    public void setPostPrivacy(String postPrivacy) {
        this.postPrivacy = postPrivacy;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }
}