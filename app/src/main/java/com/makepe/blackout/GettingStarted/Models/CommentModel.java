package com.makepe.blackout.GettingStarted.Models;

public class CommentModel {
    private String commentID, comment, timeStamp, userID, commentImage, commentType, postID,
            audioUrl, replyCommentID;
    private double longitude, latitude;

    public CommentModel() {
    }

    public CommentModel(String commentID, String comment, String timeStamp, String userID,
                        String commentImage, String commentType, String postID, String audioUrl,
                        String replyCommentID, double longitude, double latitude) {
        this.commentID = commentID;
        this.comment = comment;
        this.timeStamp = timeStamp;
        this.userID = userID;
        this.commentImage = commentImage;
        this.commentType = commentType;
        this.postID = postID;
        this.audioUrl = audioUrl;
        this.replyCommentID = replyCommentID;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getReplyCommentID() {
        return replyCommentID;
    }

    public void setReplyCommentID(String replyCommentID) {
        this.replyCommentID = replyCommentID;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
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

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getCommentImage() {
        return commentImage;
    }

    public void setCommentImage(String commentImage) {
        this.commentImage = commentImage;
    }

    public String getCommentType() {
        return commentType;
    }

    public void setCommentType(String commentType) {
        this.commentType = commentType;
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
