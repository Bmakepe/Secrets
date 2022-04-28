package com.makepe.blackout.GettingStarted.Models;

public class Movement {

    private String movementID, movementAdmin, movementName, movementPurpose, movementPrivacy,
            movementTimestamp, movementCoverPic, movementProPic;

    public Movement() {
    }

    public Movement(String movementID, String movementAdmin, String movementName, String movementPurpose,
                    String movementPrivacy, String movementTimestamp, String movementCoverPic, String movementProPic) {
        this.movementID = movementID;
        this.movementAdmin = movementAdmin;
        this.movementName = movementName;
        this.movementPurpose = movementPurpose;
        this.movementPrivacy = movementPrivacy;
        this.movementTimestamp = movementTimestamp;
        this.movementCoverPic = movementCoverPic;
        this.movementProPic = movementProPic;
    }

    public String getMovementID() {
        return movementID;
    }

    public void setMovementID(String movementID) {
        this.movementID = movementID;
    }

    public String getMovementAdmin() {
        return movementAdmin;
    }

    public void setMovementAdmin(String movementAdmin) {
        this.movementAdmin = movementAdmin;
    }

    public String getMovementName() {
        return movementName;
    }

    public void setMovementName(String movementName) {
        this.movementName = movementName;
    }

    public String getMovementPurpose() {
        return movementPurpose;
    }

    public void setMovementPurpose(String movementPurpose) {
        this.movementPurpose = movementPurpose;
    }

    public String getMovementPrivacy() {
        return movementPrivacy;
    }

    public void setMovementPrivacy(String movementPrivacy) {
        this.movementPrivacy = movementPrivacy;
    }

    public String getMovementTimestamp() {
        return movementTimestamp;
    }

    public void setMovementTimestamp(String movementTimestamp) {
        this.movementTimestamp = movementTimestamp;
    }

    public String getMovementCoverPic() {
        return movementCoverPic;
    }

    public void setMovementCoverPic(String movementCoverPic) {
        this.movementCoverPic = movementCoverPic;
    }

    public String getMovementProPic() {
        return movementProPic;
    }

    public void setMovementProPic(String movementProPic) {
        this.movementProPic = movementProPic;
    }
}
