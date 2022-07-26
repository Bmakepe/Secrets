package com.makepe.blackout.GettingStarted.Models;

public class NotiModel {
    private String notificationID, userID, postID, timeStamp, text, notificationType,
            commentID, storyID;

    public NotiModel(String notificationID, String userID, String postID,
                     String timeStamp, String text, String notificationType,
                     String commentID, String storyID) {
        this.notificationID = notificationID;
        this.userID = userID;
        this.postID = postID;
        this.timeStamp = timeStamp;
        this.text = text;
        this.notificationType = notificationType;
        this.commentID = commentID;
        this.storyID = storyID;
    }

    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
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

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    public String getStoryID() {
        return storyID;
    }

    public void setStoryID(String storyID) {
        this.storyID = storyID;
    }
}
