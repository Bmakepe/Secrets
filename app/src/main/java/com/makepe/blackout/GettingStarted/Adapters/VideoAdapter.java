package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.ConnectionsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.PostDetailActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.SharePostActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private List<PostModel> videosList;
    private Context context;

    //for videos
    private boolean isPlaying = false;
    private boolean soundPlaying = true;

    private boolean isFollowing = false;

    private GetTimeAgo getTimeAgo;
    private UniversalFunctions universalFunctions;

    private FirebaseUser firebaseUser;
    private DatabaseReference userRef, viewReference, followRef;

    String postTimeStamp;

    public VideoAdapter(List<PostModel> videosList, Context context) {
        this.videosList = videosList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_post_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        viewReference = FirebaseDatabase.getInstance().getReference("VideoViews");
        followRef = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        getTimeAgo = new GetTimeAgo();
        universalFunctions = new UniversalFunctions(context);

        PostModel videoPost = videosList.get(position);

        getVideoDetails(videoPost, holder);
        getPublisherInfo(videoPost, holder);
        checkFollow(videoPost, holder);
        universalFunctions.isLiked(videoPost.getPostID(), holder.like);
        universalFunctions.nrLikes(holder.likes, videoPost.getPostID());
        universalFunctions.getCommentsCount(videoPost.getPostID(), holder.comments);
        universalFunctions.isSaved(videoPost.getPostID(), holder.bookmarkBTN);
        universalFunctions.checkVideoViewCount(holder.videoViews, videoPost);

        holder.playBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying){
                    holder.postVideoView.pause();
                    isPlaying = false;
                    holder.playBTN.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                }else{
                    holder.postVideoView.start();
                    isPlaying = true;
                    holder.playBTN.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                }
            }
        });

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.like.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(videoPost.getPostID())
                            .child(firebaseUser.getUid()).setValue(true);
                    if(videoPost.getUserID().equals(firebaseUser.getUid()))
                        universalFunctions.addVideoLikesNotification(videoPost.getPostID(), videoPost.getUserID());
                }else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(videoPost.getPostID())
                            .child(firebaseUser.getUid()).removeValue();
                    //removeLikeNotification(listItem.getMomentId(), listItem.getUsername());
                }
            }
        });

        holder.itemView.findViewById(R.id.shareArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(context, SharePostActivity.class);
                shareIntent.putExtra("postID", videoPost.getPostID());
                context.startActivity(shareIntent);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(context, "You will be able to interact with this video", Toast.LENGTH_SHORT).show();
                //universalFunctions.addView(listItem.getMomentId());

                Intent momentActivity = new Intent(context, PostDetailActivity.class);
                momentActivity.putExtra("postID", videoPost.getPostID());
                context.startActivity(momentActivity);
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!videoPost.getUserID().equals(firebaseUser.getUid())) {
                    Intent intent = new Intent(context, ViewProfileActivity.class);
                    intent.putExtra("uid", videoPost.getUserID());
                    context.startActivity(intent);
                }
            }
        });

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!videoPost.getUserID().equals(firebaseUser.getUid())){
                    Intent intent = new Intent(context, ViewProfileActivity.class);
                    intent.putExtra("uid", videoPost.getUserID());
                    context.startActivity(intent);
                }
            }
        });

        holder.itemView.findViewById(R.id.likesArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (holder.likes.getText().toString().equals("Like")){
                    Toast.makeText(context, "No Likes", Toast.LENGTH_SHORT).show();
                }else {
                    Intent likesIntent = new Intent(context, ConnectionsActivity.class);
                    likesIntent.putExtra("Interaction", "Likes");
                    likesIntent.putExtra("UserID", videoPost.getPostID());
                    context.startActivity(likesIntent);
                }

            }
        });

        holder.followBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "You will be able to follow this user", Toast.LENGTH_SHORT).show();
                if(holder.followBTN.getText().toString().equals("Follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(videoPost.getUserID()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(videoPost.getUserID())
                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                    universalFunctions.addFollowNotifications(videoPost.getUserID());
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(videoPost.getUserID()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(videoPost.getUserID())
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.bookmarkBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.bookmarkBTN.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(videoPost.getPostID()).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(videoPost.getPostID()).removeValue();
                }
            }
        });
        
        holder.commentArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent postDetailIntent= new Intent(context, PostDetailActivity.class);
                postDetailIntent.putExtra("postID", videoPost.getPostID());
                context.startActivity(postDetailIntent);
            }
        });

    }

    private void checkFollow(PostModel videoPost, ViewHolder holder) {
        followRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(videoPost.getUserID()).exists())
                    holder.followBTN.setText("Following");
                else
                    holder.followBTN.setText("Follow");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPublisherInfo(PostModel videoPost, ViewHolder holder) {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUSER_ID().equals(videoPost.getUserID())){

                        try{
                            Picasso.get().load(user.getImageURL()).into(holder.image_profile);
                        }catch (NullPointerException ignored){}

                        holder.username.setText(user.getUsername());

                        if (user.getUSER_ID().equals(firebaseUser.getUid())){
                            holder.followBTN.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getVideoDetails(PostModel videoPost, ViewHolder holder) {
        holder.postVideoView.setVideoURI(Uri.parse(videoPost.getVideoURL()));
        holder.postVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                holder.bufferProgress.setVisibility(View.GONE);
                isPlaying = true;
                soundPlaying = true;
                mediaPlayer.setLooping(true);

                holder.playBTN.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                holder.volumeBTN.setImageResource(R.drawable.ic_baseline_volume_up_24);

                float videoRatio = mediaPlayer.getVideoWidth() / (float)mediaPlayer.getVideoHeight();
                float screenRatio = holder.postVideoView.getWidth() / (float)holder.postVideoView.getHeight();
                float scale  = videoRatio / screenRatio;
                if (scale >= 1f){
                    holder.postVideoView.setScaleX(scale);
                }else {
                    holder.postVideoView.setScaleY(1f / scale);
                }
            }
        });

        holder.postVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });

        try{
            postTimeStamp = getTimeAgo.getTimeAgo(Long.parseLong(videoPost.getPostTime()), context);
            holder.date.setText(postTimeStamp);
        }catch (NumberFormatException ignored){}

        holder.momentDesc.setText(videoPost.getPostCaption());

        try{
            if (videoPost.getPostPrivacy().equals("Private")){
                holder.shareArea.setVisibility(View.GONE);
            }
        }catch (NullPointerException ignored){}

        try{
            universalFunctions.findAddress(videoPost, holder.postVideoCheckIn);
        }catch (Exception ignored){}

    }

    @Override
    public int getItemCount() {
        return videosList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //for regular posts
        public ImageView like, image_profile, comment, postMenuBTN, bookmarkBTN, volumeBTN;
        public TextView comments, date, username, momentDesc, likes, postCheckIn, followBTN, videoViews, postVideoCheckIn;
        CardView postPicArea;
        RelativeLayout shareArea, commentArea;

        //for videos
        VideoView postVideoView;
        ImageView playBTN;

        ProgressBar bufferProgress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //for regular posts
            momentDesc = itemView.findViewById(R.id.desc);
            like = itemView.findViewById(R.id.post_likes);
            image_profile = itemView.findViewById(R.id.moment_image_user);
            username = itemView.findViewById(R.id.moment_username);
            date = itemView.findViewById(R.id.postDate);
            likes =itemView.findViewById(R.id.likes);
            comments = itemView.findViewById(R.id.post_comments);
            comment = itemView.findViewById(R.id.comment);
            postPicArea = itemView.findViewById(R.id.postPicArea);
            postMenuBTN = itemView.findViewById(R.id.postMenuBTN);
            bookmarkBTN = itemView.findViewById(R.id.bookmarkBTN);
            postCheckIn = itemView.findViewById(R.id.postCheckIn);
            followBTN = itemView.findViewById(R.id.followBTN);
            videoViews = itemView.findViewById(R.id.videoViewsCounter);
            volumeBTN = itemView.findViewById(R.id.videoVolumeBTN);
            postVideoCheckIn = itemView.findViewById(R.id.postVideoCheckIn);
            shareArea = itemView.findViewById(R.id.shareArea);
            commentArea = itemView.findViewById(R.id.commentsArea);

            //for video
            postVideoView = itemView.findViewById(R.id.postVideoView);
            playBTN = itemView.findViewById(R.id.videoPlayBTN);
            bufferProgress = itemView.findViewById(R.id.fullLoadingProgressBar);
        }
    }
}
