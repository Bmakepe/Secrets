package com.makepe.blackout.GettingStarted.Models;

public class NotiModel {
    private String notificationID, userID, text, postID, commentID, timeStamp, notificationType;
    private boolean isPost, isStory, isComment, isFollowing, isShared, isLiked;

    public NotiModel() {
    }

    public NotiModel(String notificationID, String userID, String text, String postID, String commentID,
                     String timeStamp, String notificationType, boolean isPost, boolean isStory,
                     boolean isComment, boolean isFollowing, boolean isShared, boolean isLiked) {
        this.notificationID = notificationID;
        this.userID = userID;
        this.text = text;
        this.postID = postID;
        this.commentID = commentID;
        this.timeStamp = timeStamp;
        this.notificationType = notificationType;
        this.isPost = isPost;
        this.isStory = isStory;
        this.isComment = isComment;
        this.isFollowing = isFollowing;
        this.isShared = isShared;
        this.isLiked = isLiked;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    public boolean isShared() {
        return isShared;
    }

    public void setShared(boolean shared) {
        isShared = shared;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isPost() {
        return isPost;
    }

    public void setPost(boolean post) {
        isPost = post;
    }

    public boolean isStory() {
        return isStory;
    }

    public void setStory(boolean story) {
        isStory = story;
    }

    public boolean isComment() {
        return isComment;
    }

    public void setComment(boolean comment) {
        isComment = comment;
    }
}
