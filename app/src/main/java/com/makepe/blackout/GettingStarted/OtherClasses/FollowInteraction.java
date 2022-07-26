package com.makepe.blackout.GettingStarted.OtherClasses;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Notifications.SendNotifications;

public class FollowInteraction {

    private Context context;
    private final DatabaseReference followReference = FirebaseDatabase.getInstance().getReference("Follow");
    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private final SendNotifications notifications = new SendNotifications();

    private boolean isFollowing = false, isFollower = false;

    public FollowInteraction(Context context) {
        this.context = context;
    }

    public FollowInteraction() {
    }

    public boolean checkFollowing(String hisUserID){
        assert firebaseUser != null;
        followReference.child(firebaseUser.getUid()).child("following")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            isFollowing = snapshot.child(hisUserID).exists();
                        }else
                            isFollowing = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        return isFollowing;
    }//am i following the user

    public boolean checkFollower(String hisUserID){
        followReference.child(hisUserID).child("following")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            assert firebaseUser != null;
                            if (snapshot.child(firebaseUser.getUid()).exists()) {
                                isFollower = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        return isFollower;
    }//is the user following me

    public void updateFollowing(String hisUserID, TextView followTV){
        assert firebaseUser != null;
        followReference.child(firebaseUser.getUid()).child("following")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (snapshot.child(hisUserID).exists()){
                                followTV.setText("Following");
                            }else{
                                followTV.setText("Follow");
                            }
                        }else{
                            followTV.setText("Follow");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }//check if i follow the user and updates follow textview

    public void followUser(String hisUserID){
        assert firebaseUser != null;
        followReference.
                child(firebaseUser.getUid())
                .child("following")
                .child(hisUserID)
                .setValue(true)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        notifications.addFollowingNotification(hisUserID);
                    }
                });

        followReference
                .child(hisUserID)
                .child("followers")
                .child(firebaseUser.getUid())
                .setValue(true);

        Toast.makeText(context, "Followed User", Toast.LENGTH_SHORT).show();
    }

    public void unFollowUser(String hisUserID){
        followReference
                .child(firebaseUser.getUid())
                .child("following")
                .child(hisUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        notifications.removeFollowingNotification(hisUserID);

                    }
                });
        
        followReference
                .child(hisUserID)
                .child("followers")
                .child(firebaseUser.getUid())
                .removeValue();

        Toast.makeText(context, "Unfollowed User", Toast.LENGTH_SHORT).show();
    }

    public void getFollowersNo(String userID, TextView followersList){
        followReference.child(userID).child("followers")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        followersList.setText(snapshot.getChildrenCount() + "");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void getFollowingNo(String userID, TextView followingList){
        followReference.child(userID).child("following")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        followingList.setText(snapshot.getChildrenCount() + "");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}
