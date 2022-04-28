package com.makepe.blackout.GettingStarted.Models;

public class GroupsModel {

    String groupID, groupAdmin, groupName, groupRole, groupPrivacy,
            timeStamp, groupProPic, groupCoverPic, groupPurpose, userID;

    public GroupsModel() {
    }

    public GroupsModel(String groupID, String groupAdmin, String groupName, String groupRole,
                       String groupPrivacy, String timeStamp, String groupProPic, String groupCoverPic,
                       String groupPurpose, String userID) {
        this.groupID = groupID;
        this.groupAdmin = groupAdmin;
        this.groupName = groupName;
        this.groupRole = groupRole;
        this.groupPrivacy = groupPrivacy;
        this.timeStamp = timeStamp;
        this.groupProPic = groupProPic;
        this.groupCoverPic = groupCoverPic;
        this.groupPurpose = groupPurpose;
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getGroupPurpose() {
        return groupPurpose;
    }

    public void setGroupPurpose(String groupPurpose) {
        this.groupPurpose = groupPurpose;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getGroupAdmin() {
        return groupAdmin;
    }

    public void setGroupAdmin(String groupAdmin) {
        this.groupAdmin = groupAdmin;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupRole() {
        return groupRole;
    }

    public void setGroupRole(String groupRole) {
        this.groupRole = groupRole;
    }

    public String getGroupPrivacy() {
        return groupPrivacy;
    }

    public void setGroupPrivacy(String groupPrivacy) {
        this.groupPrivacy = groupPrivacy;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getGroupProPic() {
        return groupProPic;
    }

    public void setGroupProPic(String groupProPic) {
        this.groupProPic = groupProPic;
    }

    public String getGroupCoverPic() {
        return groupCoverPic;
    }

    public void setGroupCoverPic(String groupCoverPic) {
        this.groupCoverPic = groupCoverPic;
    }
}
