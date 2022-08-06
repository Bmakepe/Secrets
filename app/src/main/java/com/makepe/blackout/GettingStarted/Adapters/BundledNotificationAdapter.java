package com.makepe.blackout.GettingStarted.Adapters;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.CommentsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.CommentModel;
import com.makepe.blackout.GettingStarted.Models.NotiModel;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.Notifications.Data;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class BundledNotificationAdapter extends RecyclerView.Adapter<BundledNotificationAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> notificationBundles;

    private GetTimeAgo getTimeAgo;

    private DatabaseReference notificationReference, userReference, postReference, videoReference, storyReference, commentReference;
    private FirebaseUser firebaseUser;

    public BundledNotificationAdapter(Context context, ArrayList<String> notificationBundles) {
        this.context = context;
        this.notificationBundles = notificationBundles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.notification_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String notificationID = notificationBundles.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        notificationReference = FirebaseDatabase.getInstance().getReference("SecretNotifications");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        postReference = FirebaseDatabase.getInstance().getReference("SecretPosts");
        videoReference = FirebaseDatabase.getInstance().getReference("SecretVideos");
        storyReference = FirebaseDatabase.getInstance().getReference("Story");
        commentReference = FirebaseDatabase.getInstance().getReference("Comments");
        getTimeAgo = new GetTimeAgo();

        getNotificationType(holder, notificationID);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationReference.child(firebaseUser.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot ds : snapshot.getChildren()){

                                notificationReference.child(firebaseUser.getUid()).child(ds.getKey())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()){
                                                    for (DataSnapshot data : snapshot.getChildren()){
                                                        if (notificationID.equals(data.getKey())){
                                                            switch (ds.getKey()){
                                                                case "FollowNotifications":
                                                                    notificationReference.child(firebaseUser.getUid()).child(ds.getKey())
                                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                    if (snapshot.exists()){
                                                                                        for (DataSnapshot snap : snapshot.getChildren()){
                                                                                            NotiModel myNotification = snap.getValue(NotiModel.class);

                                                                                            assert myNotification != null;
                                                                                            if (myNotification.getNotificationID().equals(notificationID)){

                                                                                                Intent profileIntent = new Intent(context, ViewProfileActivity.class);
                                                                                                profileIntent.putExtra("uid", myNotification.getUserID());
                                                                                                context.startActivity(profileIntent);
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                                }
                                                                            });
                                                                    break;

                                                                case "TagNotifications":
                                                                    notificationReference.child(firebaseUser.getUid()).child(ds.getKey())
                                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                    if (snapshot.exists()){
                                                                                        for (DataSnapshot snap : snapshot.getChildren()){
                                                                                            NotiModel myNotification = snap.getValue(NotiModel.class);

                                                                                            assert myNotification != null;
                                                                                            if (myNotification.getNotificationID().equals(notificationID)){

                                                                                                Intent postIntent = new Intent(context, CommentsActivity.class);
                                                                                                postIntent.putExtra("postID", myNotification.getPostID());
                                                                                                context.startActivity(postIntent);
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                                }
                                                                            });
                                                                    break;

                                                                case "CommentNotifications":
                                                                    notificationReference.child(firebaseUser.getUid())
                                                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                            if (snapshot.exists()){
                                                                                                for (DataSnapshot snap : snapshot.getChildren()){
                                                                                                    if (snap.getKey().equals(ds.getKey())){
                                                                                                        Toast.makeText(context, "Notification Type: " + ds.getKey(), Toast.LENGTH_SHORT).show();
                                                                                                        openCommentNotificationsDialog();
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                                        }
                                                                                    });
                                                                    break;

                                                                case "LikesNotifications":
                                                                    notificationReference.child(firebaseUser.getUid())
                                                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                            if (snapshot.exists()){
                                                                                                for (DataSnapshot snap : snapshot.getChildren()){
                                                                                                    if (snap.getKey().equals(ds.getKey())){
                                                                                                        Toast.makeText(context, "Notification Type: " + ds.getKey(), Toast.LENGTH_SHORT).show();
                                                                                                        openLikesNotificationDialog(data.getKey());
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                                        }
                                                                                    });
                                                                    break;

                                                                case "PostShareNotifications":
                                                                    openPostSharesDialog(data.getKey());

                                                                default:
                                                                    Toast.makeText(context, "Unknown Notification type: " + ds.getKey(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
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
        });
    }

    private void getNotificationType(ViewHolder holder, String notification) {
        notificationReference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){

                        notificationReference.child(firebaseUser.getUid()).child(ds.getKey())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            for (DataSnapshot data : snapshot.getChildren()){
                                                if (notification.equals(data.getKey())){
                                                    getNotificationDetails(ds.getKey(), holder, notification);
                                                }
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

    private void getNotificationDetails(String notificationType, ViewHolder holder, String notification) {
        switch (notificationType){

            case "CommentNotifications":
                getCommentNotifications(notificationType, holder, notification);
                break;

            case "FollowNotifications":
            case "TagNotifications":
                getFollowTagsNotifications(notificationType, holder, notification);
                break;

            case "LikesNotifications":
                getLikesNotifications(notificationType, holder, notification);
                break;

            case "PostShareNotifications":
                getPostSharesNotification(notificationType, holder, notification);
                break;

            default:
                Toast.makeText(context, "Unknown Notification type: " + notificationType, Toast.LENGTH_SHORT).show();
        }
    }

    private void getFollowTagsNotifications(String notificationType, ViewHolder holder, String notification) {
        notificationReference.child(firebaseUser.getUid()).child(notificationType).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        NotiModel myNotification = ds.getValue(NotiModel.class);

                        assert myNotification != null;
                        if (myNotification.getNotificationID().equals(notification)){
                            holder.followTagTimestamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(myNotification.getTimeStamp()), context));

                            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        for (DataSnapshot data : snapshot.getChildren()){
                                            User user = data.getValue(User.class);

                                            if (user.getUserID().equals(myNotification.getUserID())){
                                                holder.notificationMessage.setText(user.getUsername() + "\n" + myNotification.getText());

                                                try{
                                                    Picasso.get().load(user.getImageURL()).into(holder.notificationOwnerPic);
                                                }catch (NullPointerException e){
                                                    Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.notificationOwnerPic);
                                                }
                                            }

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getLikesNotifications(String notificationType, ViewHolder holder, String notification) {
        notificationReference.child(firebaseUser.getUid()).child(notificationType).child(notification)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot mySnapshot) {
                if (mySnapshot.exists()){
                    postReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    PostModel model = ds.getValue(PostModel.class);

                                    assert model != null;
                                    if (model.getPostID().equals(mySnapshot.getKey())){
                                        if (mySnapshot.getChildrenCount() == 1)
                                            holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friend liked your post:\n" + model.getPostCaption());
                                        else
                                            holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friends liked your post:\n" + model.getPostCaption());

                                        if (model.getPostType().equals("imagePost")
                                                || model.getPostType().equals("audioImage")){
                                            holder.postImage.setVisibility(View.VISIBLE);
                                            try{
                                                Picasso.get().load(model.getPostImage()).into(holder.postImage);
                                            }catch (NullPointerException ignored){}
                                        }
                                    }else{
                                        videoReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()){
                                                    for (DataSnapshot ds : snapshot.getChildren()){
                                                        PostModel model = ds.getValue(PostModel.class);

                                                        assert model != null;
                                                        if (model.getPostID().equals(mySnapshot.getKey())){
                                                            if (mySnapshot.getChildrenCount() == 1)
                                                                holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friend liked your video:\n" + model.getPostCaption());
                                                            else
                                                                holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friends liked your video:\n" + model.getPostCaption());
                                                        }else{
                                                            storyReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if (snapshot.exists()){
                                                                        for (DataSnapshot ds : snapshot.getChildren()){
                                                                            Story story = ds.getValue(Story.class);

                                                                            assert story != null;
                                                                            if (story.getStoryID().equals(mySnapshot.getKey())){
                                                                                if (mySnapshot.getChildrenCount() == 1)
                                                                                    holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friend liked your story:\n" + story.getStoryCaption());
                                                                                else
                                                                                    holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friends liked your story:\n" + story.getStoryCaption());

                                                                                holder.postImage.setVisibility(View.VISIBLE);
                                                                                try{
                                                                                    Picasso.get().load(story.getImageURL()).into(holder.postImage);
                                                                                }catch (NullPointerException ignored){}
                                                                            }else{
                                                                                commentReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                        if (snapshot.exists()){
                                                                                            for (DataSnapshot ds : snapshot.getChildren()){
                                                                                                CommentModel commentModel = ds.getValue(CommentModel.class);

                                                                                                assert commentModel != null;
                                                                                                if (commentModel.getCommentID().equals(mySnapshot.getKey())){

                                                                                                    if (mySnapshot.getChildrenCount() == 1)
                                                                                                        holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friend liked your comment:\n" + commentModel.getComment());
                                                                                                    else
                                                                                                        holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friends liked your comment:\n" + commentModel.getComment());

                                                                                                    if (commentModel.getCommentType().equals("audioImageComment")
                                                                                                            || commentModel.getCommentType().equals("imageComment")){

                                                                                                        holder.postImage.setVisibility(View.VISIBLE);
                                                                                                        try{
                                                                                                            Picasso.get().load(commentModel.getCommentImage()).into(holder.postImage);
                                                                                                        }catch (NullPointerException ignored){}
                                                                                                    }
                                                                                                }
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
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                }
                                                            });
                                                        }
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
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCommentNotifications(String notificationType, ViewHolder holder, String notification) {
        notificationReference.child(firebaseUser.getUid()).child(notificationType).child(notification)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot mySnapshot) {
                        if (mySnapshot.exists()){
                            postReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        for (DataSnapshot ds : snapshot.getChildren()){
                                            PostModel model = ds.getValue(PostModel.class);

                                            assert model != null;
                                            if (model.getPostID().equals(mySnapshot.getKey())){
                                                if (mySnapshot.getChildrenCount() == 1)
                                                    holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friend commented on your post:\n" + model.getPostCaption());
                                                else
                                                    holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friends commented on your post:\n" + model.getPostCaption());

                                                if (model.getPostType().equals("imagePost")
                                                        || model.getPostType().equals("audioImage")){
                                                    holder.postImage.setVisibility(View.VISIBLE);
                                                    try{
                                                        Picasso.get().load(model.getPostImage()).into(holder.postImage);
                                                    }catch (NullPointerException ignored){}
                                                }
                                            }else{
                                                videoReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(snapshot.exists()){
                                                            for (DataSnapshot ds : snapshot.getChildren()){
                                                                PostModel model = ds.getValue(PostModel.class);

                                                                assert model != null;
                                                                if (model.getPostID().equals(mySnapshot.getKey())){
                                                                    if (mySnapshot.getChildrenCount() == 1)
                                                                        holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friend commented on your video:\n" + model.getPostCaption());
                                                                    else
                                                                        holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friends commented on your video:\n" + model.getPostCaption());
                                                                }else{
                                                                    storyReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                            if (snapshot.exists()){
                                                                                for (DataSnapshot ds : snapshot.getChildren()){
                                                                                    Story story = ds.getValue(Story.class);

                                                                                    assert story != null;
                                                                                    if (story.getStoryID().equals(mySnapshot.getKey())){
                                                                                        if (mySnapshot.getChildrenCount() == 1)
                                                                                            holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friend commented on your story:\n" + story.getStoryCaption());
                                                                                        else
                                                                                            holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friends commented on your story:\n" + story.getStoryCaption());

                                                                                        holder.postImage.setVisibility(View.VISIBLE);
                                                                                        try{
                                                                                            Picasso.get().load(story.getImageURL()).into(holder.postImage);
                                                                                        }catch (NullPointerException ignored){}
                                                                                    }
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
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void getPostSharesNotification(String notificationType, ViewHolder holder, String notification) {
        notificationReference.child(firebaseUser.getUid()).child(notificationType).child(notification)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot mySnapshot) {
                        if (mySnapshot.exists()){
                            postReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        for (DataSnapshot ds : snapshot.getChildren()){
                                            PostModel model = ds.getValue(PostModel.class);

                                            assert model != null;
                                            if (model.getPostID().equals(mySnapshot.getKey())){
                                                if (mySnapshot.getChildrenCount() == 1)
                                                    holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friend shared your post:\n" + model.getPostCaption());
                                                else
                                                    holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friends shared your post:\n" + model.getPostCaption());

                                                if (model.getPostType().equals("imagePost")
                                                        || model.getPostType().equals("audioImage")){
                                                    holder.postImage.setVisibility(View.VISIBLE);
                                                    try{
                                                        Picasso.get().load(model.getPostImage()).into(holder.postImage);
                                                    }catch (NullPointerException ignored){}
                                                }
                                            }else{
                                                videoReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(snapshot.exists()){
                                                            for (DataSnapshot ds : snapshot.getChildren()){
                                                                PostModel model = ds.getValue(PostModel.class);

                                                                assert model != null;
                                                                if (model.getPostID().equals(mySnapshot.getKey())){
                                                                    if (mySnapshot.getChildrenCount() == 1)
                                                                        holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friend shared your video:\n" + model.getPostCaption());
                                                                    else
                                                                        holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friends shared your video:\n" + model.getPostCaption());
                                                                }else{
                                                                    storyReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                            if (snapshot.exists()){
                                                                                for (DataSnapshot ds : snapshot.getChildren()){
                                                                                    Story story = ds.getValue(Story.class);

                                                                                    assert story != null;
                                                                                    if (story.getStoryID().equals(mySnapshot.getKey())){
                                                                                        if (mySnapshot.getChildrenCount() == 1)
                                                                                            holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friend shared your story:\n" + story.getStoryCaption());
                                                                                        else
                                                                                            holder.notificationMessage.setText(mySnapshot.getChildrenCount() + " friends shared your story:\n" + story.getStoryCaption());

                                                                                        holder.postImage.setVisibility(View.VISIBLE);
                                                                                        try{
                                                                                            Picasso.get().load(story.getImageURL()).into(holder.postImage);
                                                                                        }catch (NullPointerException ignored){}
                                                                                    }
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
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void openLikesNotificationDialog(String key) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);

        assert interactionHeader != null;
        interactionHeader.setText("Post Likers");

        ArrayList<NotiModel> notificationsList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(context));
        friendsRecycler.setNestedScrollingEnabled(true);
        NotificationAdapter notificationAdapter = new NotificationAdapter(context, notificationsList);
        friendsRecycler.setAdapter(notificationAdapter);

        notificationReference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){

                        notificationReference.child(firebaseUser.getUid()).child(ds.getKey())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            for (DataSnapshot data : snapshot.getChildren()){

                                                if (key.equals(data.getKey())) {

                                                    notificationReference.child(firebaseUser.getUid()).child(ds.getKey()).child(data.getKey())
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if (snapshot.exists()) {
                                                                        notificationsList.clear();
                                                                        for (DataSnapshot snap : snapshot.getChildren()) {
                                                                            NotiModel notiModel = snap.getValue(NotiModel.class);
                                                                            notificationsList.add(notiModel);
                                                                        }
                                                                        notificationAdapter.notifyDataSetChanged();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                }
                                                            });
                                                }
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

        bottomSheetDialog.show();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void openCommentNotificationsDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);

        assert interactionHeader != null;
        interactionHeader.setText("Post Commenters");

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(context));
        friendsRecycler.setNestedScrollingEnabled(true);

        bottomSheetDialog.show();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void openPostSharesDialog(String key) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);

        assert interactionHeader != null;
        interactionHeader.setText("Post Shares");

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(context));
        friendsRecycler.setNestedScrollingEnabled(true);

        bottomSheetDialog.show();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationBundles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView followTagTimestamp, notificationMessage;
        public CircleImageView notificationOwnerPic;
        public ImageView postImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            notificationMessage = itemView.findViewById(R.id.notificationOwner);
            followTagTimestamp = itemView.findViewById(R.id.notificationTimeStamp);
            notificationOwnerPic = itemView.findViewById(R.id.notificationOwnerPic);
            postImage = itemView.findViewById(R.id.post_notification_image);
        }

    }

}
