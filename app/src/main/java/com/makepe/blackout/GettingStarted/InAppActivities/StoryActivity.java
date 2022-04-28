package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.Movement;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioPlayer;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
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

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    private int counter = 0;
    private long pressTime = 0L;
    private long limit = 500L;

    private StoriesProgressView storiesProgressView;
    private ImageView storyPhoto, commentBTN, likeBTN;
    private CircleImageView storyProPic;
    private TextView storyUsername, storyTimeStamp, storyCap, likesCounter,
            commentCounter, shareCounter, storyLocationTV, seen_number;
    private RelativeLayout likesArea, commentsArea, sharesArea, storyAudioArea;
    private LinearLayout r_seen;

    private List<Story> storyList;
    private String userID, storyID;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, storyReference, movementReference;

    private UniversalFunctions universalFunctions;
    private UniversalNotifications notifications;
    private GetTimeAgo getTimeAgo;

    private double latitude, longitude;

    //for playing audio status
    private AudioPlayer audioPlayer;
    public CircleImageView playBTN;
    public LottieAnimationView audioAnimation;
    public TextView audioSeekTimer, postTotalTime;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;

                case MotionEvent.ACTION_UP:
                    Long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;

                case MotionEvent.AXIS_SCROLL:
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        Toolbar movementToolbar = findViewById(R.id.view_story_toolbar);
        setSupportActionBar(movementToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storiesProgressView = findViewById(R.id.stories);
        storyProPic = findViewById(R.id.storyProPic);
        storyPhoto = findViewById(R.id.storyImage);
        storyUsername = findViewById(R.id.storyUsername);
        storyTimeStamp = findViewById(R.id.storyTimeStamp);
        storyCap = findViewById(R.id.storyCap);
        storyLocationTV = findViewById(R.id.storyLocationDetails);

        commentBTN = findViewById(R.id.storyCommentBTN);
        likeBTN = findViewById(R.id.story_postLikeBTN);
        likesCounter = findViewById(R.id.story_likeCounter);
        commentCounter = findViewById(R.id.story_commentCounter);
        shareCounter = findViewById(R.id.movement_shareCounter);
        likesArea = findViewById(R.id.story_likesArea);
        commentsArea = findViewById(R.id.story_commentLayout);
        sharesArea = findViewById(R.id.story_shareArea);
        seen_number = findViewById(R.id.storyViewsCount);
        r_seen = findViewById(R.id.r_seen);

        //for audio buttons
        playBTN = findViewById(R.id.postItem_playVoiceIcon);
        audioAnimation = findViewById(R.id.postItem_lav_playing);
        audioSeekTimer = findViewById(R.id.postItemSeekTimer);
        postTotalTime = findViewById(R.id.postTotalTime);
        storyAudioArea = findViewById(R.id.storyAudioArea);

        userID = getIntent().getStringExtra("userid");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        storyReference = FirebaseDatabase.getInstance().getReference("Story").child(userID);
        movementReference = FirebaseDatabase.getInstance().getReference("Movements");
        universalFunctions = new UniversalFunctions(this);
        notifications = new UniversalNotifications(this);
        getTimeAgo = new GetTimeAgo();

        audioPlayer = new AudioPlayer(StoryActivity.this, playBTN,
                audioSeekTimer, postTotalTime, audioAnimation);

        if (userID.equals(firebaseUser.getUid()))
            r_seen.setVisibility(View.VISIBLE);
        else
            r_seen.setVisibility(View.GONE);

        getStories();
        displayUsersDetails(userID);

        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });

        reverse.setOnTouchListener(onTouchListener);

        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });

        skip.setOnTouchListener(onTouchListener);

        findViewById(R.id.replyStory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                storiesProgressView.pause();
            }
        });

        likesArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent likesIntent = new Intent(StoryActivity.this, ConnectionsActivity.class);
                likesIntent.putExtra("UserID", storyList.get(counter).getStoryID());
                likesIntent.putExtra("Likes", "Interaction");
                startActivity(likesIntent);
            }
        });

        likeBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(likeBTN.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(storyID)
                            .child(firebaseUser.getUid()).setValue(true);
                    if (!firebaseUser.getUid().equals(userID))
                        notifications.addLikesNotifications(userID, storyID);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(storyID)
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        commentsArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoryActivity.this, CommentsActivity.class);
                intent.putExtra("postID", storyID);
                startActivity(intent);
            }
        });
        
        sharesArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StoryActivity.this, "You will be able to share this story", Toast.LENGTH_SHORT).show();
            }
        });

        storyLocationTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = "https://maps.google.com/maps?saddr=" + latitude + "," + longitude;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                startActivity(intent);
            }
        });

    }

    private void getStories(){

        storyList = new ArrayList<>();

        storyReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                storyList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Story story = snapshot.getValue(Story.class);

                    long timecurrent = System.currentTimeMillis();
                    assert story != null;

                    if(timecurrent > story.getTimeStart() && timecurrent < story.getTimeEnd()){
                        storyList.add(story);
                    }
                }

                storiesProgressView.setStoriesCount(storyList.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(counter);

                storyID = storyList.get(counter).getStoryID();

                getStoryDetails(counter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void getStoryDetails(int counter) {

        if (storyList.get(counter).getStoryType().equals("textStory")){
            storyCap.setText(storyList.get(counter).getStoryCaption());

        }else if(storyList.get(counter).getStoryType().equals("audioStory")){
            storyCap.setVisibility(View.GONE);
            storyAudioArea.setVisibility(View.VISIBLE);

            playBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!audioPlayer.isPlaying()){
                        storiesProgressView.pause();
                        audioPlayer.startPlayingAudio(storyList.get(counter).getStoryAudioUrl());
                    }else{
                        storiesProgressView.resume();
                        audioPlayer.stopPlayingAudio();
                    }
                }
            });

        }
        Glide.with(getApplicationContext()).load(storyList.get(counter).getStoryImage()).into(storyPhoto);
        storyTimeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(storyList.get(counter).getStoryTimeStamp()), StoryActivity.this));
        checkInteractions(storyList.get(counter));
        universalFunctions.addView(storyList.get(counter).getStoryID());
        universalFunctions.seenNumber(storyList.get(counter).getStoryID(), seen_number);
    }

    private void checkInteractions(Story story) {

        universalFunctions.isLiked(story.getStoryID(), likeBTN);
        universalFunctions.nrLikes(likesCounter, story.getStoryID());
        universalFunctions.getCommentsCount(story.getStoryID(), commentCounter);

        try{
            latitude = story.getLatitude();
            longitude = story.getLongitude();

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(StoryActivity.this, Locale.getDefault());

            try{
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                String address = addresses.get(0).getAddressLine(0);
                storyLocationTV.setText(address);
                storyLocationTV.setVisibility(View.VISIBLE);

            }catch (Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }catch (NullPointerException ignored){}
    }

    private void displayUsersDetails(final String userID) {
        List<ContactsModel> phoneBook = new ArrayList<>();
        ContactsList contactsList = new ContactsList(phoneBook, StoryActivity.this);
        contactsList.readContacts();
        final List<ContactsModel> phoneContacts = contactsList.getContactsList();

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    if (user.getUSER_ID().equals(userID)){

                        for(ContactsModel contactsModel : phoneContacts){
                            if(userID.equals(firebaseUser.getUid())){
                                storyUsername.setText("Me");
                            }else if(contactsModel.getNumber().equals(user.getNumber())){
                                storyUsername.setText(contactsModel.getUsername());
                            }else{
                                storyUsername.setText(user.getUsername());
                            }
                        }

                        try{
                            Picasso.get().load(user.getImageURL()).placeholder(R.drawable.default_profile_display_pic).into(storyProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(storyProPic);

                        }
                    }else{
                        getMovementDetails();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getMovementDetails() {
        movementReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Movement movement = ds.getValue(Movement.class);

                    if (movement.getMovementID().equals(userID)){
                        storyUsername.setText(movement.getMovementName());

                        try{
                            Picasso.get().load(movement.getMovementProPic()).into(storyProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(storyProPic);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onNext() {

        int position = ++counter;
        storyID = storyList.get(position).getStoryID();

        getStoryDetails(position);

        /*Glide.with(getApplicationContext()).load(storyList.get(position).getStoryImage()).into(storyPhoto);
        storyCap.setText(storyList.get(position).getStoryCaption());
        storyTimeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(storyList.get(position).getStoryTimeStamp()), StoryActivity.this));

        checkInteractions(storyList.get(position));
        universalFunctions.addView(storyList.get(position).getStoryID());
        universalFunctions.seenNumber(storyList.get(position).getStoryID(), seen_number);*/


    }

    @Override
    public void onPrev() {
        if((counter - 1) < 0 ) return;

        int position = --counter;
        storyID = storyList.get(position).getStoryID();

        getStoryDetails(position);

        /*Glide.with(getApplicationContext()).load(storyList.get(position).getStoryImage()).into(storyPhoto);
        storyCap.setText(storyList.get(position).getStoryCaption());
        storyTimeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(storyList.get(position).getStoryTimeStamp()), StoryActivity.this));

        checkInteractions(storyList.get(position));
        universalFunctions.seenNumber(storyList.get(position).getStoryID(), seen_number);*/
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.story_menu, menu);

        if (!userID.equals(firebaseUser.getUid())) {
            menu.findItem(R.id.storyViewItem).setVisible(false);
            menu.findItem(R.id.storyDeleteItem).setVisible(false);
        }else{
            menu.findItem(R.id.storyReportItem).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.storyViewProfileItem:
                Intent proIntent = new Intent(StoryActivity.this, ViewProfileActivity.class);
                proIntent.putExtra("uid", storyList.get(counter).getUserID());
                startActivity(proIntent);
                return true;

            case R.id.storyViewItem:
                Intent viewsIntent = new Intent(StoryActivity.this, ConnectionsActivity.class);
                viewsIntent.putExtra("UserID", storyList.get(counter).getStoryID());
                viewsIntent.putExtra("Interaction", "Views");
                startActivity(viewsIntent);
                return true;

            case R.id.storyReportItem:
                Intent reportIntent = new Intent(StoryActivity.this, ReportActivity.class);
                reportIntent.putExtra("reported", storyList.get(counter).getStoryID());
                startActivity(reportIntent);
                return true;

            case R.id.storyDeleteItem:
                AlertDialog.Builder builder = new AlertDialog.Builder(StoryActivity.this);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this story?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                StorageReference imageRef = FirebaseStorage.getInstance().getReference(storyList.get(counter).getStoryImage());
                                imageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        storyReference.child(storyList.get(counter).getStoryID())
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Toast.makeText(StoryActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();

                return true;

            default:
                Toast.makeText(this, "Unknown Selection", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}
