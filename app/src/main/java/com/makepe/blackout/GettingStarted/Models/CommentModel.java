package com.makepe.blackout.GettingStarted.Models;

public class CommentModel {
    String commentID, comment, timeStamp, userID;

    public CommentModel() {
    }

    public CommentModel(String commentID, String comment, String timeStamp, String userID) {
        this.commentID = commentID;
        this.comment = comment;
        this.timeStamp = timeStamp;
        this.userID = userID;
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
