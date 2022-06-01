package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.ConnectionsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenImageActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.StoryActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.CommentModel;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioPlayer;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.FollowInteraction;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalNotifications;
import com.makepe.blackout.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyHolder> {

    private Context context;
    private List<CommentModel> commentList;

    private DatabaseReference userReference, commentsRef;
    private FirebaseUser firebaseUser;

    private GetTimeAgo timeAgo;

    private UniversalFunctions universalFunctions;
    private UniversalNotifications notifications;
    private FollowInteraction followInteraction;
    private AudioPlayer audioPlayer;

    public static final int COMMENT_IMAGE_POST = 100;
    public static final int COMMENT_TEXT_POST = 200;
    public static final int COMMENT_AUDIO_POST = 300;
    public static final int COMMENT_AUDIO_IMAGE_POST = 400;

    public CommentsAdapter() {
    }

    public CommentsAdapter(Context context, List<CommentModel> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate comment layout xml
        switch (viewType){
            case COMMENT_IMAGE_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.comment_image_layout, parent, false));

            case COMMENT_TEXT_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.comment_layout, parent, false));

            case COMMENT_AUDIO_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.comment_audio_item, parent, false));

            case COMMENT_AUDIO_IMAGE_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.comment_audio_image_item, parent, false));

            default:
                throw new IllegalStateException("Unexpected value " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get the data
        CommentModel comments = commentList.get(position);
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        commentsRef = FirebaseDatabase.getInstance().getReference("Comments");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        timeAgo = new GetTimeAgo();
        universalFunctions = new UniversalFunctions(context);
        notifications = new UniversalNotifications(context);
        followInteraction = new FollowInteraction(context);

        switch (getItemViewType(position)){
            case COMMENT_IMAGE_POST:
                getCommentInfo(comments, holder);
                getImageCommentDetails(comments, holder);
                loadOtherFunctions(comments, holder);
                break;

            case COMMENT_TEXT_POST:
                getCommentInfo(comments, holder);
                getTextCommentDetails(comments, holder);
                loadOtherFunctions(comments, holder);
                break;

            case COMMENT_AUDIO_POST:
                getCommentInfo(comments, holder);
                getAudioCommentDetails(comments, holder);
                loadOtherFunctions(comments, holder);
                break;

            case COMMENT_AUDIO_IMAGE_POST:
                getCommentInfo(comments, holder);
                getAudioCommentDetails(comments, holder);
                try{
                    Picasso.get().load(comments.getCommentImage()).into(holder.commentImage);
                }catch (NullPointerException ignored){}
                loadOtherFunctions(comments, holder);
                break;

            default:
                Toast.makeText(context, "Unknown comment post type", Toast.LENGTH_SHORT).show();
        }

    }

    private void getAudioCommentDetails(CommentModel comments, MyHolder holder) {

        audioPlayer = new AudioPlayer(context, holder.playBTN,
                holder.seekTimer, holder.postTotalTime, holder.audioAnimation);

        try{
            universalFunctions.findAddress(comments.getLatitude(), comments.getLongitude(), holder.commentLocation, holder.commentLocationArea);
        }catch (Exception ignored){}

        try{//convert timestamp
            holder.commentTime.setText(timeAgo.getTimeAgo(Long.parseLong(comments.getTimeStamp()), context));
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }

        holder.playBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!audioPlayer.isPlaying()){
                    audioPlayer.startPlayingAudio(comments.getAudioUrl());
                }else{
                    audioPlayer.stopPlayingAudio();
                }
            }
        });
    }

    private void getTextCommentDetails(CommentModel comments, MyHolder holder) {

        try{
            universalFunctions.findAddress(comments.getLatitude(), comments.getLongitude(), holder.commentLocation, holder.commentLocationArea);
        }catch (Exception ignored){}

        try{//convert timestamp
            holder.commentTime.setText(timeAgo.getTimeAgo(Long.parseLong(comments.getTimeStamp()), context));
            holder.commentCaption.setText(comments.getComment());
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadOtherFunctions(CommentModel comments, MyHolder holder) {

        universalFunctions.isLiked(comments.getCommentID(), holder.likesBTN);
        universalFunctions.nrLikes(holder.likesCounter, comments.getCommentID());

        universalFunctions.checkActiveStories(holder.cProPic, comments.getUserID());

        holder.menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuOptions(holder.menuIcon, comments);
            }
        });

        holder.cProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.cProPic.getTag().equals("storyActive")){

                    Intent intent = new Intent(context, StoryActivity.class);
                    intent.putExtra("userid", comments.getUserID());
                    context.startActivity(intent);
                }else{
                    Intent picIntent = new Intent(context, FullScreenImageActivity.class);
                    picIntent.putExtra("itemID", comments.getUserID());
                    picIntent.putExtra("reason", "userImage");
                    context.startActivity(picIntent);
                }
            }
        });

        holder.likesBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.likesBTN.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(comments.getCommentID())
                            .child(firebaseUser.getUid()).setValue(true);
                    if (!firebaseUser.getUid().equals(comments.getUserID()))
                        notifications.addLikesNotifications(comments.getUserID(), comments.getCommentID());
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(comments.getCommentID())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.likesCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ConnectionsActivity.class);
                intent.putExtra("UserID", comments.getCommentID());
                intent.putExtra("Interaction", "Likes");
                context.startActivity(intent);
            }
        });

        holder.commentImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageIntent = new Intent(context, FullScreenImageActivity.class);
                imageIntent.putExtra("itemID", comments.getCommentID());
                imageIntent.putExtra("reason", "commentImage");
                context.startActivity(imageIntent);
            }
        });
    }

    private void getImageCommentDetails(CommentModel comments, MyHolder holder) {

        try{
            universalFunctions.findAddress(comments.getLatitude(), comments.getLongitude(), holder.commentLocation, holder.commentLocationArea);
        }catch (Exception ignored){}

        try{
            Picasso.get().load(comments.getCommentImage()).into(holder.commentImage);
        }catch (NullPointerException ignored){}

        try{//convert timestamp
            holder.commentTime.setText(timeAgo.getTimeAgo(Long.parseLong(comments.getTimeStamp()), context));
            holder.commentCaption.setText(comments.getComment());
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMenuOptions(ImageView menuIcon, CommentModel comments) {
        PopupMenu popupMenu = new PopupMenu(context, menuIcon, Gravity.END);
        
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "View Profile");
        
        if (comments.getUserID().equals(firebaseUser.getUid())) {
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Delete Comment");
        }else if (!comments.getUserID().equals(firebaseUser.getUid())){
            if (followInteraction.checkFollowing(comments.getUserID())){
                userReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            User user = ds.getValue(User.class);

                            assert user != null;
                            if (user.getUSER_ID().equals(comments.getUserID()))
                                popupMenu.getMenu().add(Menu.NONE, 2, 0, "Unfollow " + user.getUsername());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }else{
                userReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            User user = ds.getValue(User.class);

                            assert user != null;
                            if (user.getUSER_ID().equals(comments.getUserID()))
                                popupMenu.getMenu().add(Menu.NONE, 2, 0, "Follow " + user.getUsername());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        }
        
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case 0:
                        if (!comments.getUserID().equals(firebaseUser.getUid())) {
                            Intent intent = new Intent(context, ViewProfileActivity.class);
                            intent.putExtra("uid", comments.getUserID());
                            context.startActivity(intent);
                        }
                        break;
                        
                    case 1:
                        Toast.makeText(context, "You will be able to delete", Toast.LENGTH_SHORT).show();
                        break;

                    case 2:
                        if (followInteraction.checkFollowing(comments.getUserID()))
                            followInteraction.unFollowUser(comments.getUserID());
                        else
                            followInteraction.followUser(comments.getUserID());
                        
                    default:
                        Toast.makeText(context, "Unknown Selection", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void getCommentInfo(CommentModel comments, final MyHolder holder) {
        List<ContactsModel> phoneBook = new ArrayList<>();
        ContactsList contactsList = new ContactsList(phoneBook, context);
        contactsList.readContacts();
        final List<ContactsModel> phoneContacts = contactsList.getContactsList();

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);

                        assert user != null;
                        if (user.getUSER_ID().equals(comments.getUserID())) {

                            for (ContactsModel contactsModel : phoneContacts) {
                                if (user.getUSER_ID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    holder.commentOwner.setText("Me");
                                } else if (contactsModel.getNumber().equals(user.getNumber())) {
                                    holder.commentOwner.setText(contactsModel.getUsername());
                                }else{
                                    holder.commentOwner.setText(user.getUsername());
                                }
                            }

                            Picasso.get().load(user.getImageURL()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.cProPic, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {

                                    Picasso.get().load(user.getImageURL()).placeholder(R.drawable.default_profile_display_pic).into(holder.cProPic);
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        public CircleImageView cProPic;
        public TextView commentCaption, commentOwner, commentTime, likesCounter, commentLocation;
        public ImageView likesBTN, commentImage, menuIcon;
        public LinearLayout commentLocationArea;

        //--------------for audio post buttons
        public CircleImageView playBTN;
        public LottieAnimationView audioAnimation;
        public TextView seekTimer, postTotalTime;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            cProPic = itemView.findViewById(R.id.comProPic);
            commentCaption = itemView.findViewById(R.id.commentCaption);
            commentOwner = itemView.findViewById(R.id.commentOwner);
            commentTime = itemView.findViewById(R.id.commentTime);
            likesBTN = itemView.findViewById(R.id.likeComBTN);
            commentImage = itemView.findViewById(R.id.commentImage);
            likesCounter = itemView.findViewById(R.id.commentItemLikesCounter);
            menuIcon = itemView.findViewById(R.id.commentMenuIcon);
            commentLocation = itemView.findViewById(R.id.commentLocation);
            commentLocationArea = itemView.findViewById(R.id.commentLocationArea);

            //for audio buttons
            playBTN = itemView.findViewById(R.id.postItem_playVoiceIcon);
            audioAnimation = itemView.findViewById(R.id.postItem_lav_playing);
            seekTimer = itemView.findViewById(R.id.postItemSeekTimer);
            postTotalTime = itemView.findViewById(R.id.postTotalTime);

        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (commentList.get(position).getCommentType()){
            case "textComment":
                return COMMENT_TEXT_POST;

            case "imageComment":
                return COMMENT_IMAGE_POST;

            case "audioImageComment":
                return COMMENT_AUDIO_IMAGE_POST;

            case "audioComment":
                return COMMENT_AUDIO_POST;

            default:
                throw new IllegalStateException("Unexpected value " + commentList.get(position).getCommentType());
        }
    }
}
