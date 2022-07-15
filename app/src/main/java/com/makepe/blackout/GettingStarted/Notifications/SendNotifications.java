package com.makepe.blackout.GettingStarted.Notifications;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Models.CommentModel;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.Story;

import java.util.HashMap;

public class SendNotifications {

    Context context;

    private DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications");
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference postReference = FirebaseDatabase.getInstance().getReference("Posts");
    private DatabaseReference storyReference = FirebaseDatabase.getInstance().getReference("Story");
    private DatabaseReference commentsReference = FirebaseDatabase.getInstance().getReference("Comments");

    private String key = notificationsReference.push().getKey();

    public SendNotifications() {
    }

    public SendNotifications(Context context) {
        this.context = context;
    }

    //-----------------for post/video/story/comment likes notifications

    public void addLikesNotification(String postID, String userID) {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel model = ds.getValue(PostModel.class);

                    assert model != null;
                    if (model.getPostID().equals(postID)){
                        sendPostLikesNotification(postID, userID);
                    }else{
                        storyReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot snap : snapshot.getChildren()){
                                    Story story = snap.getValue(Story.class);

                                    assert story != null;
                                    if (story.getStoryID().equals(postID)){
                                        sendStoryLikesNotification(postID, userID);
                                    }else{
                                        commentsReference.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot data : snapshot.getChildren()){
                                                    CommentModel comment = data.getValue(CommentModel.class);

                                                    assert comment != null;
                                                    if (comment.getPostID().equals(postID)){
                                                        sendCommentLikeNotification(postID, userID);
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

    private void sendCommentLikeNotification(String postID, String userID) {

        HashMap<String, Object> notificationMap = new HashMap<>();

        notificationMap.put("notificationID", key);
        notificationMap.put("commentID", postID);
        notificationMap.put("userID", firebaseUser.getUid());
        notificationMap.put("text", "Liked your comment");
        notificationMap.put("isLiked", true);
        notificationMap.put("isComment", true);
        notificationMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));

        notificationsReference.child(userID).child(key).setValue(notificationMap);
    }

    private void sendStoryLikesNotification(String postID, String userID) {

        HashMap<String, Object> notificationMap = new HashMap<>();

        notificationMap.put("notificationID", key);
        notificationMap.put("postID", postID);
        notificationMap.put("userID", firebaseUser.getUid());
        notificationMap.put("text", "Liked your story");
        notificationMap.put("isLiked", true);
        notificationMap.put("isStory", true);
        notificationMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));

        notificationsReference.child(userID).child(key).setValue(notificationMap);
    }

    private void sendPostLikesNotification(String postID, String userID) {

        HashMap<String, Object> notificationMap = new HashMap<>();

        notificationMap.put("notificationID", key);
        notificationMap.put("postID", postID);
        notificationMap.put("userID", firebaseUser.getUid());
        notificationMap.put("text", "Liked your post");
        notificationMap.put("isLiked", true);
        notificationMap.put("isPost", true);
        notificationMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));

        notificationsReference.child(userID).child(key).setValue(notificationMap);
    }

    //-----------------for post/story comment notifications

    public void addCommentNotification(String postID, String userID){
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    assert postModel != null;
                    if (postModel.getPostID().equals(postID)){

                        switch (postModel.getPostType()) {
                            case "textPost":
                            case "sharedTextPost":
                            case "audioPost":
                            case "sharedAudioTextPost":
                            case "sharedTextAudioPost":
                            case "sharedImagePost":
                            case "sharedAudioImagePost":
                            case "sharedTextAudioImagePost":
                            case "sharedAudioAudioImagePost":
                            case "sharedAudioAudioPost":
                                sendPostCommentNotification(postModel.getPostID(), userID);
                                break;
                            case "imagePost":
                            case "audioImagePost":
                                sendImagePostNotification(postModel.getPostID(), userID);
                                break;
                            case "videoPost":
                            case "audioVideoPost":
                            case "sharedAudioAudioVideoPost":
                            case "sharedAudioVideoPost":
                            case "sharedAudioTextVideoPost":
                            case "sharedVideoPost":
                                sendVideoPostNotification(postModel.getPostID(), userID);
                                break;

                            default:
                                Toast.makeText(context, "Unidentified post type " + postModel.getPostType() , Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        storyReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    Story story = ds.getValue(Story.class);

                                    assert story != null;
                                    if (story.getStoryID().equals(postID)){
                                        sendStoryCommentNotification(story.getStoryID(), userID);
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

    private void sendStoryCommentNotification(String storyID, String userID) {
        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("notificationID", key);
        postMap.put("userID", firebaseUser.getUid());
        postMap.put("text", "commented on your story");
        postMap.put("postID", storyID);
        postMap.put("isStory", true);
        postMap.put("isComment", true);
        postMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));

        notificationsReference.child(userID).child(key).setValue(postMap);
    }

    private void sendVideoPostNotification(String postID, String userID) {

        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("notificationID", key);
        postMap.put("userID", firebaseUser.getUid());
        postMap.put("text", "commented on your video post");
        postMap.put("postID", postID);
        postMap.put("isPost", true);
        postMap.put("isComment", true);
        postMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));

        notificationsReference.child(userID).child(key).setValue(postMap);
    }

    private void sendImagePostNotification(String postID, String userID) {

        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("notificationID", key);
        postMap.put("userID", firebaseUser.getUid());
        postMap.put("text", "commented on your post");
        postMap.put("postID", postID);
        postMap.put("isPost", true);
        postMap.put("isComment", true);
        postMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));

        notificationsReference.child(userID).child(key).setValue(postMap);
    }

    private void sendPostCommentNotification(String postID, String userID) {
        HashMap<String, Object> postMap = new HashMap<>();

        postMap.put("notificationID", key);
        postMap.put("userID", firebaseUser.getUid());
        postMap.put("text", "commented on your post");
        postMap.put("postID", postID);
        postMap.put("isPost", true);
        postMap.put("isComment", true);
        postMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));

        notificationsReference.child(userID).child(key).setValue(postMap);
    }

    //-----------------for interactions notifications

    public void addFollowNotification(String userID){
        HashMap<String, Object> followMap = new HashMap<>();

        followMap.put("notificationID", key);
        followMap.put("userID", firebaseUser.getUid());
        followMap.put("text", "started following you");
        followMap.put("isFollowing", true);
        followMap.put("notificationType", "followingNotification");
        followMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));

        notificationsReference.child(userID).child(key).setValue(followMap);
    }

    //------------------for shared posts/videos notifications

    public void addShareNotification(String postID, String userID){
        HashMap <String, Object> sharedMap = new HashMap<>();

        sharedMap.put("notificationID", key);
        sharedMap.put("userID", firebaseUser.getUid());
        sharedMap.put("text", "shared your post");
        sharedMap.put("postID", postID);
        sharedMap.put("isShared", true);
        sharedMap.put("notificationType", "sharedPostNotification");
        sharedMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));

        notificationsReference.child(userID).child(key).setValue(sharedMap);
    }

}
