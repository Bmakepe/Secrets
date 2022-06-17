package com.makepe.blackout.GettingStarted.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.ChatActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.CommentsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ConnectionsActivity;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioPlayer;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalNotifications;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;

public class ViewStoryAdapter extends RecyclerView.Adapter<ViewStoryAdapter.ViewHolder> implements StoriesProgressView.StoriesListener{

    private Context context;
    private ArrayList<Story> stories;
    private ArrayList<Story> storyList;

    private int counter = 0;
    private long pressTime = 0L;
    private long limit = 500L;

    private String storyID;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, storyReference;

    private UniversalFunctions universalFunctions;
    private UniversalNotifications notifications;
    private GetTimeAgo getTimeAgo;

    private double latitude, longitude;
    private AudioPlayer audioPlayer;

    public ViewStoryAdapter(Context context, ArrayList<Story> stories) {
        this.context = context;
        this.stories = stories;
    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            ViewHolder holder = new ViewHolder(view);
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    holder.storiesProgressView.pause();
                    return false;

                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    holder.storiesProgressView.resume();
                    return limit < now - pressTime;
            }

            return false;
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_story_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Story story = stories.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        storyReference = FirebaseDatabase.getInstance().getReference("Story").child(story.getUserID());
        universalFunctions = new UniversalFunctions(context);
        notifications = new UniversalNotifications(context);
        getTimeAgo = new GetTimeAgo();

        audioPlayer = new AudioPlayer(context, holder.playBTN,
                holder.audioSeekTimer, holder.postTotalTime, holder.audioAnimation);

        if (story.getUserID().equals(firebaseUser.getUid())){
            holder.r_seen.setVisibility(View.VISIBLE);
            holder.sharesArea.setVisibility(View.GONE);
            holder.story_slideDMArea.setVisibility(View.GONE);
        }else{
            holder.r_seen.setVisibility(View.GONE);
        }

        getStories(story, holder);
        displayUserDetails(story, holder);

        holder.reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.storiesProgressView.reverse();
            }
        });

        holder.skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.storiesProgressView.skip();
            }
        });

        holder.skip.setOnTouchListener(onTouchListener);

        holder.storyPauseBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                holder.storiesProgressView.pause();
            }
        });

        holder.story_slideDMArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent messageIntent = new Intent(context, ChatActivity.class);
                messageIntent.putExtra("userid", story.getUserID());
                context.startActivity(messageIntent);
            }
        });

        holder.likesArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent likesIntent = new Intent(context, ConnectionsActivity.class);
                likesIntent.putExtra("UserID", storyList.get(counter).getStoryID());
                likesIntent.putExtra("Likes", "Interaction");
                context.startActivity(likesIntent);
            }
        });

        holder.likeBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.likeBTN.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(storyID)
                            .child(firebaseUser.getUid()).setValue(true);
                    if (!firebaseUser.getUid().equals(story.getUserID()))
                        notifications.addLikesNotifications(story.getUserID(), storyID);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(storyID)
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.commentsArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentsActivity.class);
                intent.putExtra("postID", storyID);
                context.startActivity(intent);
            }
        });

        holder.sharesArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "You will be able to share this story", Toast.LENGTH_SHORT).show();
            }
        });

        holder.storyLocationTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = "https://maps.google.com/maps?saddr=" + latitude + "," + longitude;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                context.startActivity(intent);
            }
        });

    }

    private void displayUserDetails(Story story, ViewHolder holder) {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUSER_ID().equals(story.getUserID())){

                        if (user.getUSER_ID().equals(firebaseUser.getUid()))
                            holder.storyUsername.setText("Me");
                        else
                            holder.storyUsername.setText(user.getUsername());

                        try{
                            Picasso.get().load(user.getImageURL()).into(holder.storyProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.storyProPic);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getStories(Story story, ViewHolder holder) {
        storyList = new ArrayList<>();

        storyReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storyList.clear();

                for (DataSnapshot ds : snapshot.getChildren()){
                    Story myStory = ds.getValue(Story.class);

                    if (story.getStoryID().equals(myStory.getStoryID())){
                        long timecurrent = System.currentTimeMillis();

                        if (timecurrent > story.getTimeStart() && timecurrent < story.getTimeEnd())
                            storyList.add(story);
                    }
                }

                holder.storiesProgressView.setStoriesCount(storyList.size());
                holder.storiesProgressView.setStoryDuration(5000L);
                holder.storiesProgressView.setStoriesListener((StoriesProgressView.StoriesListener) context);
                holder.storiesProgressView.startStories(counter);

                storyID = storyList.get(counter).getStoryID();

                getStoryDetails(counter, holder);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getStoryDetails(int counter, ViewHolder holder) {
        if (storyList.get(counter).getStoryType().equals("textStory")){
            holder.storyCap.setText(storyList.get(counter).getStoryCaption());
        }else if(storyList.get(counter).getStoryType().equals("audioStory")){
            holder.storyCap.setVisibility(View.GONE);
            holder.storyAudioArea.setVisibility(View.VISIBLE);

            holder.playBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!audioPlayer.isPlaying){
                        holder.storiesProgressView.pause();
                        audioPlayer.startPlayingAudio(storyList.get(counter).getStoryAudioUrl());
                    }else{
                        holder.storiesProgressView.resume();
                        audioPlayer.stopPlayingAudio();
                    }
                }
            });
        }

        Picasso.get().load(storyList.get(counter).getStoryImage()).into(holder.storyPhoto);
        holder.storyTimeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(storyList.get(counter).getStoryTimeStamp()), context));
        checkInteractions(storyList.get(counter), holder);
        universalFunctions.addView(storyList.get(counter).getStoryID());
        universalFunctions.seenNumber(storyList.get(counter).getStoryID(), holder.seen_number);

    }

    private void checkInteractions(Story story, ViewHolder holder) {
        universalFunctions.isLiked(story.getStoryID(), holder.likeBTN);
        universalFunctions.nrLikes(holder.likesCounter, story.getStoryID());
        universalFunctions.getCommentsCount(story.getStoryID(), holder.commentCounter);

        try{
            latitude = story.getLatitude();
            longitude = story.getLongitude();

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context, Locale.getDefault());

            try{
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                String address = addresses.get(0).getAddressLine(0);
                holder.storyLocationTV.setText(address);
                holder.storyLocationTV.setVisibility(View.VISIBLE);

            }catch (Exception e){
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }catch (NullPointerException ignored){}
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    @Override
    public void onNext() {
        View view = null;
        ViewHolder holder = new ViewHolder(view);
        int position = ++counter;
        storyID = storyList.get(position).getStoryID();

        getStoryDetails(position, holder);

    }

    @Override
    public void onPrev() {
        View view = null;
        ViewHolder holder = new ViewHolder(view);

        if((counter - 1) < 0 ) return;

        int position = --counter;
        storyID = storyList.get(position).getStoryID();

        getStoryDetails(position, holder);

    }

    @Override
    public void onComplete() {

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public StoriesProgressView storiesProgressView;
        public ImageView storyPhoto, commentBTN, likeBTN, storyPauseBTN;
        public CircleImageView storyProPic;
        public TextView storyUsername, storyTimeStamp, storyCap, likesCounter, commentCounter,
                shareCounter, storyLocationTV, seen_number;
        public RelativeLayout likesArea, commentsArea, sharesArea, storyAudioArea, story_slideDMArea;
        public LinearLayout r_seen;

        //for playing audio stories
        public CircleImageView playBTN;
        public LottieAnimationView audioAnimation;
        public TextView audioSeekTimer, postTotalTime;

        //for story navigation
        public View reverse, skip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            storiesProgressView = itemView.findViewById(R.id.stories);
            storyProPic = itemView.findViewById(R.id.storyProPic);
            storyPhoto = itemView.findViewById(R.id.storyImage);
            storyUsername = itemView.findViewById(R.id.storyUsername);
            storyTimeStamp = itemView.findViewById(R.id.storyTimeStamp);
            storyCap = itemView.findViewById(R.id.storyCap);
            storyLocationTV = itemView.findViewById(R.id.storyLocationDetails);

            commentBTN = itemView.findViewById(R.id.storyCommentBTN);
            likeBTN = itemView.findViewById(R.id.story_postLikeBTN);
            likesCounter = itemView.findViewById(R.id.story_likeCounter);
            commentCounter = itemView.findViewById(R.id.story_commentCounter);
            shareCounter = itemView.findViewById(R.id.movement_shareCounter);
            likesArea = itemView.findViewById(R.id.story_likesArea);
            commentsArea = itemView.findViewById(R.id.story_commentLayout);
            sharesArea = itemView.findViewById(R.id.story_shareArea);
            seen_number = itemView.findViewById(R.id.storyViewsCount);
            r_seen = itemView.findViewById(R.id.r_seen);
            story_slideDMArea = itemView.findViewById(R.id.story_slideDMArea);
            storyPauseBTN = itemView.findViewById(R.id.pauseStory);

            //for audio buttons
            playBTN = itemView.findViewById(R.id.postItem_playVoiceIcon);
            audioAnimation = itemView.findViewById(R.id.postItem_lav_playing);
            audioSeekTimer = itemView.findViewById(R.id.postItemSeekTimer);
            postTotalTime = itemView.findViewById(R.id.postTotalTime);
            storyAudioArea = itemView.findViewById(R.id.storyAudioArea);

            //for story navigation
            reverse = itemView.findViewById(R.id.reverse);
            skip = itemView.findViewById(R.id.skip);

        }
    }
}
