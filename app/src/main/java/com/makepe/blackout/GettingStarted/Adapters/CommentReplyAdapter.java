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
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenImageActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenPictureActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.StoryActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.CommentModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.Notifications.SendNotifications;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioPlayer;
import com.makepe.blackout.GettingStarted.OtherClasses.FollowInteraction;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentReplyAdapter extends RecyclerView.Adapter<CommentReplyAdapter.MyHolder> {
    private Context context;
    private List<CommentModel> commentList;

    public static final int COMMENT_TEXT_POST = 100;
    public static final int COMMENT_AUDIO_POST = 200;

    private DatabaseReference userReference, likesReference;
    private FirebaseUser firebaseUser;

    private GetTimeAgo timeAgo;

    private UniversalFunctions universalFunctions;
    private SendNotifications notifications;
    private FollowInteraction followInteraction;

    public CommentReplyAdapter(Context context, List<CommentModel> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate comment layout xml
        switch (viewType){
            case COMMENT_TEXT_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.comment_layout, parent, false));

            case COMMENT_AUDIO_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.comment_audio_item, parent, false));

            default:
                throw new IllegalStateException("Unexpected value " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get the data
        CommentModel comments = commentList.get(position);
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        likesReference = FirebaseDatabase.getInstance().getReference("Likes");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        timeAgo = new GetTimeAgo();
        universalFunctions = new UniversalFunctions(context);
        notifications = new SendNotifications(context);
        followInteraction = new FollowInteraction(context);

        holder.commentReplyArea.setVisibility(View.GONE);

        switch (getItemViewType(position)){

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

            default:
                Toast.makeText(context, "Unknown comment post type", Toast.LENGTH_SHORT).show();
        }

    }

    private void getAudioCommentDetails(CommentModel comments, MyHolder holder) {

        AudioPlayer audioPlayer = new AudioPlayer(context, holder.playBTN,
                holder.seekTimer, holder.postTotalTime, holder.audioAnimation);

        audioPlayer.init();

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
                if (!audioPlayer.mediaPlayer.isPlaying())
                    audioPlayer.startPlayingAudio(comments.getAudioUrl());
                else if(audioPlayer.mediaPlayer.isPlaying())
                    audioPlayer.stopPlayingAudio();
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
                    likesReference.child(comments.getCommentID())
                            .child(firebaseUser.getUid()).setValue(true);
                    if (!firebaseUser.getUid().equals(comments.getUserID()))
                        notifications.addCommentLikesNotification(comments);
                }else{
                    likesReference.child(comments.getCommentID())
                            .child(firebaseUser.getUid()).removeValue();
                    if (!firebaseUser.getUid().equals(comments.getUserID()))
                        notifications.removeCommentLikesNotification(comments);
                }
            }
        });

        holder.likesCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLikesDialog(comments);
            }
        });
    }


    //for the rest of the funtions
    private void showLikesDialog(CommentModel post) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);

        assert interactionHeader != null;
        interactionHeader.setText("Likes");

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(context));
        friendsRecycler.setNestedScrollingEnabled(true);

        likesReference.child(post.getCommentID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    idList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        idList.add(ds.getKey());
                    }
                    friendsRecycler.setAdapter(new UserAdapter(context, idList, "goToProfile"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        bottomSheetDialog.show();
        bottomSheetDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
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
                            if (user.getUserID().equals(comments.getUserID()))
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
                            if (user.getUserID().equals(comments.getUserID()))
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

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);

                        assert user != null;
                        if (user.getUserID().equals(comments.getUserID())) {
                            if (user.getUserID().equals(firebaseUser.getUid())) {
                                holder.commentOwner.setText("Me");
                            } else{
                                holder.commentOwner.setText(user.getUsername());
                            }

                            try{
                                Picasso.get().load(user.getImageURL()).into(holder.cProPic);
                            }catch (NullPointerException e){
                                Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.cProPic);
                            }
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
        public TextView commentCaption, commentOwner, commentTime, likesCounter, commentLocation, replyCounter;
        public ImageView likesBTN, commentImage, menuIcon;
        public LinearLayout commentLocationArea;
        public RelativeLayout commentReplyArea;
        public RecyclerView commentReplyRecycler;

        //--------------for audio post buttons
        public CircleImageView playBTN;
        public SeekBar audioAnimation;
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
            replyCounter = itemView.findViewById(R.id.commentReplyCounter);
            commentReplyArea = itemView.findViewById(R.id.commentReplyArea);
            commentReplyRecycler = itemView.findViewById(R.id.commentReplyRecycler);

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

            case "audioComment":
                return COMMENT_AUDIO_POST;

            default:
                throw new IllegalStateException("Unexpected value " + commentList.get(position).getCommentType());
        }
    }
}
