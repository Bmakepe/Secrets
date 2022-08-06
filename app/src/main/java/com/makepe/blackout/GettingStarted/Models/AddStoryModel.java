package com.makepe.blackout.GettingStarted.Models;

import android.net.Uri;

public class AddStoryModel {
    private String storyCaption, storyPrivacy;
    private Uri imageURI;
    private long latitude, longitude;
    private boolean commentsEnabled;

    public AddStoryModel(String storyCaption, String storyPrivacy, Uri imageURI, long latitude, long longitude, boolean commentsEnabled) {
        this.storyCaption = storyCaption;
        this.storyPrivacy = storyPrivacy;
        this.imageURI = imageURI;
        this.latitude = latitude;
        this.longitude = longitude;
        this.commentsEnabled = commentsEnabled;
    }

    public String getStoryCaption() {
        return storyCaption;
    }

    public void setStoryCaption(String storyCaption) {
        this.storyCaption = storyCaption;
    }

    public String getStoryPrivacy() {
        return storyPrivacy;
    }

    public void setStoryPrivacy(String storyPrivacy) {
        this.storyPrivacy = storyPrivacy;
    }

    public Uri getImageURI() {
        return imageURI;
    }

    public void setImageURI(Uri imageURI) {
        this.imageURI = imageURI;
    }

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }

    public boolean isCommentsEnabled() {
        return commentsEnabled;
    }

    public void setCommentsEnabled(boolean commentsEnabled) {
        this.commentsEnabled = commentsEnabled;
    }
}
