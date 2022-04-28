package com.makepe.blackout.GettingStarted.OtherClasses;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.CommentsActivity;
import com.makepe.blackout.GettingStarted.Models.CommentModel;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.Story;

import java.util.HashMap;

public class UniversalNotifications {

    private Context context;

    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications");
    private DatabaseReference postReference = FirebaseDatabase.getInstance().getReference("Posts");
    private DatabaseReference movementPostReference = FirebaseDatabase.getInstance().getReference("MovementsPosts");
    private DatabaseReference storyReference = FirebaseDatabase.getInstance().getReference("Story");
    private DatabaseReference commentsReference = FirebaseDatabase.getInstance().getReference("Comments");

    String key = notificationsReference.push().getKey();

    public UniversalNotifications(Context context) {
        this.context = context;
    }

    public UniversalNotifications() {
    }

    public void addLikesNotifications(String userID, String postid){
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()){
                    PostModel post = snap.getValue(PostModel.class);

                    if (post.getPostID().equals(postid)){
                        if (post.getPostType().equals("videoPost")
                                || post.getPostType().equals("sharedVideoPost")){
                            sendAppropriateLikesNotification(postid, userID, "Liked your video");
                        }else{
                            sendAppropriateLikesNotification(postid, userID, "liked your post");
                        }
                    }else{
                        movementPostReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    PostModel postModel = ds.getValue(PostModel.class);

                                    assert postModel != null;
                                    if (postModel.getPostID().equals(postid)){
                                        sendAppropriateLikesNotification(postid, userID, "Liked your movement post");
                                    }else{
                                        storyReference.child(userID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds : snapshot.getChildren()){
                                                    Story story = ds.getValue(Story.class);

                                                    assert story != null;
                                                    if (story.getStoryID().equals(postid)){
                                                        sendAppropriateLikesNotification(postid, userID, "Liked your story");
                                                    }else{
                                                        commentsReference.addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                for (DataSnapshot ds : snapshot.getChildren()){
                                                                    CommentModel commentModel = ds.getValue(CommentModel.class);

                                                                    assert commentModel != null;
                                                                    if (ds.child(commentModel.getCommentID()).exists()){
                                                                        sendAppropriateLikesNotification(commentModel.getPostID(), commentModel.getUserID(), "Liked your comment");
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendAppropriateLikesNotification(String postid, String userID, String notString) {

        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("notificationID", key);
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", notString);
        hashMap.put("postid", postid);
        hashMap.put("ispost", true);
        hashMap.put("isStory", false);
        hashMap.put("timeStamp", timeStamp);

        notificationsReference.child(userID).push().setValue(hashMap);
    }

    public void addFollowNotifications(String userID) {
        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("notificationID", key);
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);
        hashMap.put("isStory", false);
        hashMap.put("timeStamp", timeStamp);

        notificationsReference.child(userID).push().setValue(hashMap);
    }

    public void sendCommentNotification(String itemID, String userID, String commentText) {

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("notificationID", key);
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "commented: " + commentText);
        hashMap.put("postid", itemID);
        hashMap.put("ispost", true);
        hashMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));

        notificationsReference.child(userID).child(key).setValue(hashMap);
    }

}
