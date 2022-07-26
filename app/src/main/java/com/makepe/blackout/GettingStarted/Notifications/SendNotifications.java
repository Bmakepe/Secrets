package com.makepe.blackout.GettingStarted.Notifications;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.NotificationAdapter;
import com.makepe.blackout.GettingStarted.Models.CommentModel;
import com.makepe.blackout.GettingStarted.Models.NotiModel;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.Models.User;

import java.util.HashMap;

public class SendNotifications {

    private Context context;

    private final DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference("SecretNotifications");
    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    public SendNotifications() {
    }

    public SendNotifications(Context context) {
        this.context = context;
    }

    //-----------------for post/video likes and comments notifications
    public void addPostLikeNotification(PostModel postModel){
        String notificationID = notificationsReference.push().getKey();

        assert firebaseUser != null;
        assert notificationID != null;

        HashMap<String, Object> notificationMap = new HashMap<>();

        notificationMap.put("notificationID", notificationID);
        notificationMap.put("userID", firebaseUser.getUid());
        notificationMap.put("postID", postModel.getPostID());
        notificationMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        notificationMap.put("text", "Liked Your Post");
        notificationMap.put("notificationType", "likesNotification");

        notificationsReference.child(postModel.getUserID()).child("LikesNotifications").child(postModel.getPostID())
                .child(notificationID).setValue(notificationMap);
    }

    public void removePostLikeNotification(PostModel postModel){
        notificationsReference.child(postModel.getUserID()).child("LikesNotifications").child(postModel.getPostID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            NotiModel notiModel = ds.getValue(NotiModel.class);

                            assert notiModel != null;
                            assert firebaseUser != null;
                            if (notiModel.getUserID().equals(firebaseUser.getUid()))
                                ds.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void addPostCommentNotification(String owner, String postID, String commentText){
        String notificationID = notificationsReference.push().getKey();

        assert firebaseUser != null;
        assert notificationID != null;

        HashMap<String, Object> notificationMap = new HashMap<>();

        notificationMap.put("notificationID", notificationID);
        notificationMap.put("userID", firebaseUser.getUid());
        notificationMap.put("postID", postID);
        notificationMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        notificationMap.put("text", commentText);
        notificationMap.put("notificationType", "commentNotification");

        notificationsReference.child(owner).child("CommentNotifications").child(postID)
                .child(notificationID).setValue(notificationMap);
    }

    public void addFollowingNotification(String followedUser){
        String notificationID = notificationsReference.push().getKey();

        assert firebaseUser != null;
        assert notificationID != null;

        HashMap<String, Object> followMap = new HashMap<>();

        followMap.put("notificationID", notificationID);
        followMap.put("userID", firebaseUser.getUid());
        followMap.put("text", "started following you");
        followMap.put("notificationType", "followingNotification");
        followMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));

        notificationsReference.child(followedUser).child("FollowNotifications")
                .child(notificationID).setValue(followMap);

    }

    public void removeFollowingNotification(String followedUser){
        notificationsReference.child(followedUser).child("FollowNotifications")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            NotiModel notiModel = ds.getValue(NotiModel.class);

                            assert notiModel != null;
                            assert firebaseUser != null;
                            if (notiModel.getUserID().equals(firebaseUser.getUid()))
                                ds.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void addCommentLikesNotification(CommentModel model){
        String notificationID = notificationsReference.push().getKey();

        assert firebaseUser != null;
        assert notificationID != null;

        HashMap<String, Object> notificationMap = new HashMap<>();

        notificationMap.put("notificationID", notificationID);
        notificationMap.put("userID", firebaseUser.getUid());
        notificationMap.put("commentID", model.getCommentID());
        notificationMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        notificationMap.put("text", "Liked Your Comment");
        notificationMap.put("notificationType", "likesNotification");

        notificationsReference.child(model.getUserID()).child("LikesNotifications").child(model.getCommentID())
                .child(notificationID).setValue(notificationMap);

    }

    public void removeCommentLikesNotification(CommentModel model){
        notificationsReference.child(model.getUserID()).child("LikesNotifications").child(model.getCommentID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            NotiModel notiModel = ds.getValue(NotiModel.class);

                            assert notiModel != null;
                            assert firebaseUser != null;
                            if (notiModel.getUserID().equals(firebaseUser.getUid()))
                                ds.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void addStoryLikeNotification(Story story){
        String notificationID = notificationsReference.push().getKey();

        assert firebaseUser != null;
        assert notificationID != null;

        HashMap<String, Object> notificationMap = new HashMap<>();

        notificationMap.put("notificationID", notificationID);
        notificationMap.put("userID", firebaseUser.getUid());
        notificationMap.put("storyID", story.getStoryID());
        notificationMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        notificationMap.put("text", "Liked Your Post");
        notificationMap.put("notificationType", "likesNotification");

        notificationsReference.child(story.getUserID()).child("LikesNotifications").child(story.getStoryID())
                .child(notificationID).setValue(notificationMap);
    }

    public void removeStoryLikeNotification(Story story){
        notificationsReference.child(story.getUserID()).child("LikesNotifications").child(story.getStoryID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            NotiModel notiModel = ds.getValue(NotiModel.class);

                            assert notiModel != null;
                            assert firebaseUser != null;
                            if (notiModel.getUserID().equals(firebaseUser.getUid()))
                                ds.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void addPostShareNotification(PostModel postModel, String caption){
        String notificationID = notificationsReference.push().getKey();

        assert firebaseUser != null;
        assert notificationID != null;

        HashMap<String, Object> notificationMap = new HashMap<>();

        notificationMap.put("notificationID", notificationID);
        notificationMap.put("userID", firebaseUser.getUid());
        notificationMap.put("text", caption);
        notificationMap.put("postID", postModel.getPostID());
        notificationMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        notificationMap.put("notificationType", "shareNotification");

        notificationsReference.child(postModel.getUserID()).child("PostShareNotifications").child(postModel.getPostID())
                .child(notificationID).setValue(notificationMap);

    }

    public void addTaggedUserNotification(User user, String postID){
        String notificationID = notificationsReference.push().getKey();

        assert firebaseUser != null;
        assert notificationID != null;

        HashMap<String, Object> notificationMap = new HashMap<>();

        notificationMap.put("notificationID", notificationID);
        notificationMap.put("userID", firebaseUser.getUid());
        notificationMap.put("text", "Tagged you in a post");
        notificationMap.put("postID", postID);
        notificationMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        notificationMap.put("notificationType", "taggedNotification");

        notificationsReference.child(user.getUserID()).child("TagNotifications")
                .child(notificationID).setValue(notificationMap);

    }

    public void addStoryTaggedUserNotification(User user, String postID){
        String notificationID = notificationsReference.push().getKey();

        assert firebaseUser != null;
        assert notificationID != null;

        HashMap<String, Object> notificationMap = new HashMap<>();

        notificationMap.put("notificationID", notificationID);
        notificationMap.put("userID", firebaseUser.getUid());
        notificationMap.put("text", "Tagged you in their story");
        notificationMap.put("postID", postID);
        notificationMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        notificationMap.put("notificationType", "taggedNotification");

        notificationsReference.child(user.getUserID()).child("TagNotifications").child(postID)
                .child(notificationID).setValue(notificationMap);

    }

}
