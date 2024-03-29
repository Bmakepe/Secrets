package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.makepe.blackout.GettingStarted.Adapters.CommentsAdapter;
import com.makepe.blackout.GettingStarted.Adapters.UserAdapter;
import com.makepe.blackout.GettingStarted.Models.CommentModel;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.Notifications.SendNotifications;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioPlayer;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioRecorder;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.LocationServices;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.GettingStarted.OtherClasses.UploadFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
            commentCounter, shareCounter, storyLocationTV, seen_number, sendBTN,
            storyTaggedPeopleList;
    private RelativeLayout likesArea, commentsArea, sharesArea, storyAudioArea, story_slideDMArea;
    private LinearLayout r_seen, storyTagsArea;
    private EditText messageET, commentET;
    private TextInputLayout dmArea;

    private List<Story> storyList;
    private String userID, storyID, chatID, commentID;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, storyReference, chatReference,
            likesReference, postViewsReference, commentsReference;
    private StorageReference messageAudioReference;

    private UniversalFunctions universalFunctions;
    private SendNotifications notifications;
    private GetTimeAgo getTimeAgo;

    private double latitude, longitude;
    private ProgressDialog messageDialog;

    //for playing audio status
    private AudioPlayer audioPlayer;
    public CircleImageView playBTN;
    public SeekBar audioAnimation;
    public TextView audioSeekTimer, postTotalTime;

    //for sending voice note replies
    private AudioRecorder audioRecorder, commentRecorder;
    private LottieAnimationView lavPlaying;
    private ImageView voicePlayBTN, deleteAudioBTN;
    private TextView seekTimer;
    private RelativeLayout playAudioArea;

    //for comments
    private StorageReference commentAudioReference;
    private ProgressDialog commentDialog;

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
        storyTaggedPeopleList = findViewById(R.id.storyTaggedPeopleList);
        storyTagsArea = findViewById(R.id.storyTagsArea);

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
        story_slideDMArea = findViewById(R.id.story_slideDMArea);
        messageET = findViewById(R.id.storyMessageET);
        sendBTN = findViewById(R.id.storyVoiceBTN);
        dmArea = findViewById(R.id.dmTextInputField);

        //for audio buttons
        playBTN = findViewById(R.id.postItem_playVoiceIcon);
        audioAnimation = findViewById(R.id.postItem_lav_playing);
        audioSeekTimer = findViewById(R.id.postItemSeekTimer);
        postTotalTime = findViewById(R.id.postTotalTime);
        storyAudioArea = findViewById(R.id.storyAudioArea);

        //for recording the audio
        playAudioArea = findViewById(R.id.playAudioArea);
        voicePlayBTN = findViewById(R.id.post_playVoiceIcon);
        seekTimer = findViewById(R.id.seekTimer);
        lavPlaying = findViewById(R.id.lav_playing);
        deleteAudioBTN = findViewById(R.id.recordingDeleteBTN);

        userID = getIntent().getStringExtra("userid");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        storyReference = FirebaseDatabase.getInstance().getReference("Story").child(userID);
        messageAudioReference = FirebaseStorage.getInstance().getReference("voice_notes");
        chatReference = FirebaseDatabase.getInstance().getReference("Chats");
        likesReference = FirebaseDatabase.getInstance().getReference("Likes");
        postViewsReference = FirebaseDatabase.getInstance().getReference("Views");
        commentsReference = FirebaseDatabase.getInstance().getReference("Comments");
        commentAudioReference = FirebaseStorage.getInstance().getReference();

        universalFunctions = new UniversalFunctions(this);
        notifications = new SendNotifications(this);
        getTimeAgo = new GetTimeAgo();

        audioPlayer = new AudioPlayer(StoryActivity.this, playBTN,
                audioSeekTimer, postTotalTime, audioAnimation);

        audioRecorder = new AudioRecorder(lavPlaying, StoryActivity.this,
                StoryActivity.this, playAudioArea, voicePlayBTN, seekTimer);

        messageDialog = new ProgressDialog(this);
        messageDialog.setMessage("Loading");

        if (userID.equals(firebaseUser.getUid())) {
            r_seen.setVisibility(View.VISIBLE);
            sharesArea.setVisibility(View.GONE);
            story_slideDMArea.setVisibility(View.GONE);
        } else {
            r_seen.setVisibility(View.GONE);
        }

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

        messageET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0) {
                    sendBTN.setText("Send");
                    storiesProgressView.pause();

                } else {
                    sendBTN.setText("Record");
                    storiesProgressView.resume();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        findViewById(R.id.pauseStory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                storiesProgressView.pause();
            }
        });

        likesArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                storiesProgressView.pause();
                showLikesDialog(storyList.get(counter));
            }
        });

        likeBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (likeBTN.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(storyID)
                            .child(firebaseUser.getUid()).setValue(true);
                    if (!userID.equals(firebaseUser.getUid()))
                        notifications.addStoryLikeNotification(storyList.get(counter));
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(storyID)
                            .child(firebaseUser.getUid()).removeValue();
                    if (!userID.equals(firebaseUser.getUid()))
                        notifications.removeStoryLikeNotification(storyList.get(counter));
                }
            }
        });

        commentsArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                storiesProgressView.pause();
                showCommentsDialog(storyList.get(counter));

                /*Intent intent = new Intent(StoryActivity.this, CommentsActivity.class);
                intent.putExtra("postID", storyID);
                startActivity(intent);*/
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

        sendBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sendBTN.getText().toString().trim().equals("Record")) {

                    if (audioRecorder.checkRecordingPermission()) {
                        dmArea.setVisibility(View.GONE);
                        storiesProgressView.pause();

                        if (!audioRecorder.isRecording) {
                            audioRecorder.startRecording();
                            sendBTN.setText("Stop");
                        }
                    } else {
                        audioRecorder.requestRecordingPermission();
                    }

                } else if (sendBTN.getText().toString().trim().equals("Stop")) {

                    sendBTN.setText("Send");
                    audioRecorder.stopRecording();

                } else if (sendBTN.getText().toString().equals("Send")) {

                    if (TextUtils.isEmpty(messageET.getText().toString()))
                        sendAudioStoryReply();
                    else
                        sendMessage();
                }
            }
        });

        deleteAudioBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioRecorder.resetRecorder();
                sendBTN.setText("Record");
            }
        });

        voicePlayBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!audioRecorder.isPlaying()){
                    audioRecorder.startPlayingRecording();
                }else{
                    audioRecorder.stopPlayingAudio();
                }
            }
        });

        storyTagsArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.pause();
                showTaggedFriendsDialog();
            }
        });

    }

    //functions to deal with comments only
    private void showCommentsDialog(Story story) {
        final BottomSheetDialog commentSheetDialog = new BottomSheetDialog(this);
        commentSheetDialog.setContentView(R.layout.post_comment_layout);

        ImageView close = commentSheetDialog.findViewById(R.id.closeCommentSheetBTN);
        TextView commentHeader = commentSheetDialog.findViewById(R.id.commentSheetHeader);
        RecyclerView commentSheetRecycler = commentSheetDialog.findViewById(R.id.commentSheetRecycler);
        CircleImageView myProfilePicture = commentSheetDialog.findViewById(R.id.commentSheetProfilePic);
        commentET = commentSheetDialog.findViewById(R.id.commentSheetCaptionET);
        ImageView postCommentBTN = commentSheetDialog.findViewById(R.id.commentSheetPostBTN);

        //for recording audio status
        LottieAnimationView lavPlaying = commentSheetDialog.findViewById(R.id.lav_playing);
        ImageView deleteAudioBTN = commentSheetDialog.findViewById(R.id.recordingDeleteBTN);
        ImageView voicePlayBTN = commentSheetDialog.findViewById(R.id.post_playVoiceIcon);
        TextView seekTimer = commentSheetDialog.findViewById(R.id.seekTimer);
        RelativeLayout playAudioArea = commentSheetDialog.findViewById(R.id.playAudioArea);

        commentRecorder = new AudioRecorder(lavPlaying, this,
                StoryActivity.this, playAudioArea, voicePlayBTN, seekTimer);

        commentDialog = new ProgressDialog(this);
        postCommentBTN.setTag("notRecording");

        ArrayList<CommentModel> commentList = new ArrayList<>();

        commentSheetRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        commentSheetRecycler.setLayoutManager(layoutManager);
        CommentsAdapter commentsAdapter = new CommentsAdapter(StoryActivity.this, commentList);
        commentSheetRecycler.setAdapter(commentsAdapter);

        commentsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int counter = 0;
                    commentList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        CommentModel comments = ds.getValue(CommentModel.class);

                        if (comments.getPostID().equals(story.getStoryID())) {
                            commentList.add(comments);
                            counter++;
                        }
                        if (counter == 1)
                            commentHeader.setText(counter + " Comment");
                        else
                            commentHeader.setText(counter + " Comments");
                    }

                    commentsAdapter.notifyDataSetChanged();
                }else
                    commentHeader.setText("0 Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        User user = ds.getValue(User.class);

                        if (user.getUserID().equals(firebaseUser.getUid()))
                            try{
                                Picasso.get().load(user.getImageURL()).into(myProfilePicture);
                            }catch (NullPointerException e){
                                Picasso.get().load(R.drawable.default_profile_display_pic).into(myProfilePicture);
                            }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        commentET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0)
                    postCommentBTN.setImageResource(R.drawable.ic_send_black_24dp);
                else
                    postCommentBTN.setImageResource(R.drawable.ic_mic_black_24dp);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        commentSheetDialog.show();
        commentSheetDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        postCommentBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(commentET.getText().toString())
                        && postCommentBTN.getTag().equals("notRecording")){

                    if (commentRecorder.checkRecordingPermission()){
                        postCommentBTN.setImageResource(R.drawable.ic_baseline_stop_circle_24);

                        if (!commentRecorder.isRecording){
                            commentRecorder.startRecording();
                            postCommentBTN.setTag("Recording");
                        }

                    }else{
                        commentRecorder.requestRecordingPermission();
                    }

                }else if (postCommentBTN.getTag().equals("Recording")){
                    commentRecorder.stopRecording();
                    postCommentBTN.setTag("sendAudio");
                    postCommentBTN.setImageResource(R.drawable.ic_send_black_24dp);

                }else{
                    commentID = commentsReference.push().getKey();
                    commentDialog.setMessage("Loading...");
                    commentDialog.setCancelable(false);
                    commentDialog.show();

                    if (!TextUtils.isEmpty(commentET.getText().toString()))
                        uploadTextComment();
                    else if (commentRecorder.getRecordingFilePath() != null){
                        if (postCommentBTN.getTag().equals("sendAudio"))
                            uploadAudioComment();
                        postCommentBTN.setImageResource(R.drawable.ic_mic_black_24dp);
                    }

                }
            }
        });

        deleteAudioBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentRecorder.resetRecorder();
                postCommentBTN.setImageResource(R.drawable.ic_mic_black_24dp);
                postCommentBTN.setTag("notRecording");
            }
        });

        voicePlayBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!commentRecorder.isPlaying()){
                    commentRecorder.startPlayingRecording();
                }else{
                    commentRecorder.stopPlayingAudio();
                }
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentSheetDialog.dismiss();
            }
        });
    }

    private void uploadAudioComment() {
        StorageReference audioPath = commentAudioReference.child("story_comment_audio_files").child(commentID + ".3gp");
        Uri audioUri = Uri.fromFile(new File(commentRecorder.getRecordingFilePath()));

        StorageTask<UploadTask.TaskSnapshot> audioTask = audioPath.putFile(audioUri);

        audioTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful())
                    throw task.getException();
                return audioPath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri audioDownloadLink = task.getResult();

                    HashMap<String, Object> audioMap = new HashMap<>();

                    audioMap.put("commentID", commentID);
                    audioMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
                    audioMap.put("userID", firebaseUser.getUid());
                    audioMap.put("postID", storyID);
                    audioMap.put("commentType", "audioComment");
                    audioMap.put("audioUrl", audioDownloadLink.toString());

                    commentsReference.child(commentID).setValue(audioMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    resetLayout();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(StoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    private void uploadTextComment() {
        HashMap<String, Object> commentMap = new HashMap<>();

        commentMap.put("commentID", commentID);
        commentMap.put("comment", commentET.getText().toString());
        commentMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        commentMap.put("userID", firebaseUser.getUid());
        commentMap.put("postID", storyID);
        commentMap.put("commentType", "textComment");

        commentsReference.child(commentID).setValue(commentMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        resetLayout();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StoryActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetLayout() {

        if (!userID.equals(firebaseUser.getUid()))
            if (TextUtils.isEmpty(commentET.getText().toString()))
                notifications.addPostCommentNotification(userID, storyID, "recorded a comment");
            else
                notifications.addPostCommentNotification(userID, storyID, commentET.getText().toString());

        if (commentRecorder.getRecordingFilePath() != null){
            commentRecorder.resetRecorder();
        }

        commentET.setText("");

        commentDialog.dismiss();
    }


    //Other dialog functions
    private void showLikesDialog(Story story) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);

        interactionHeader.setText("Likes");

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(this));
        friendsRecycler.setNestedScrollingEnabled(true);

        likesReference.child(story.getStoryID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    idList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        idList.add(ds.getKey());
                    }
                    friendsRecycler.setAdapter(new UserAdapter(StoryActivity.this, idList, "goToProfile"));
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
                storiesProgressView.resume();
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void showTaggedFriendsDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);

        interactionHeader.setText("Tagged Friends");

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(this));
        UserAdapter userAdapter = new UserAdapter(StoryActivity.this, idList, "goToProfile");
        friendsRecycler.setAdapter(userAdapter);

        storyReference.child(storyID).child("taggedFriends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        idList.add(ds.getKey());
                    }
                    userAdapter.notifyDataSetChanged();
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
                storiesProgressView.resume();
                bottomSheetDialog.dismiss();
            }
        });
    }

    public void showViewsDialog(Story story) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);

        assert interactionHeader != null;
        interactionHeader.setText("Views");

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(this));
        friendsRecycler.setNestedScrollingEnabled(true);
        UserAdapter userAdapter = new UserAdapter(StoryActivity.this, idList, "goToProfile");
        friendsRecycler.setAdapter(userAdapter);

        postViewsReference.child(story.getStoryID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    idList.add(ds.getKey());
                }
                userAdapter.notifyDataSetChanged();
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
                storiesProgressView.resume();
                bottomSheetDialog.dismiss();
            }
        });
    }


    //for send messages
    private void sendMessage() {
        chatID = chatReference.push().getKey();

        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("chatID", chatID);
        messageMap.put("senderID", firebaseUser.getUid());
        messageMap.put("receiverID", userID);
        messageMap.put("isSeen", false);
        messageMap.put("message", messageET.getText().toString());
        messageMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        messageMap.put("message_type", "storyTextMessage");
        messageMap.put("storyID", storyID);

        chatReference.child(chatID).setValue(messageMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        updateChatList();
                        resetAudioLayout();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

    private void sendAudioStoryReply() {
        messageDialog.show();
        chatID = chatReference.push().getKey();

        StorageReference audioPath = messageAudioReference.child(firebaseUser.getUid()).child(chatID + ".3gp");
        Uri audioUrl = Uri.fromFile(new File(audioRecorder.getRecordingFilePath()));

        StorageTask<UploadTask.TaskSnapshot> audioTask = audioPath.putFile(audioUrl);

        audioTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful())
                    throw task.getException();
                return audioPath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri audioDownloadLink = task.getResult();

                    HashMap<String, Object> messageMap = new HashMap<>();

                    messageMap.put("chatID", chatID);
                    messageMap.put("senderID", firebaseUser.getUid());
                    messageMap.put("receiverID", userID);
                    messageMap.put("isSeen", false);
                    messageMap.put("storyID", storyID);
                    messageMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
                    messageMap.put("audio", audioDownloadLink.toString());
                    messageMap.put("message_type", "storyAudioMessage");

                    chatReference.child(chatID).setValue(messageMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    updateChatList();
                                    resetAudioLayout();
                                }
                            });
                }
            }
        });
    }

    private void resetAudioLayout() {

        if (audioRecorder.getRecordingFilePath() != null){
            audioRecorder.resetRecorder();
        }

        dmArea.setVisibility(View.VISIBLE);
        messageDialog.dismiss();
        sendBTN.setText("Record");
        storiesProgressView.resume();
    }

    private void updateChatList() {
        //create chatlist node/child in firebase database
        final DatabaseReference senderReference = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(firebaseUser.getUid())
                .child(userID);
        senderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    senderReference.child("userID").setValue(userID);
                    senderReference.child("timeStamp").setValue(String.valueOf(System.currentTimeMillis()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference receiverReference = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(userID)
                .child(firebaseUser.getUid());
        receiverReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    receiverReference.child("userID").setValue(firebaseUser.getUid());
                    senderReference.child("timeStamp").setValue(String.valueOf(System.currentTimeMillis()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    //for retrieving appropriate story and story user details
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
                    if (!audioPlayer.mediaPlayer.isPlaying()) {
                        storiesProgressView.pause();
                        audioPlayer.startPlayingAudio(storyList.get(counter).getStoryAudioUrl());
                    }else if(audioPlayer.mediaPlayer.isPlaying()){
                        storiesProgressView.resume();
                        audioPlayer.stopPlayingAudio();
                    }
                }
            });

        }

        Glide.with(getApplicationContext()).load(storyList.get(counter).getImageURL()).into(storyPhoto);

        storyTimeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(storyList.get(counter).getStoryTimeStamp()), StoryActivity.this));

        checkInteractions(storyList.get(counter));
        checkStoryTags(storyList.get(counter));
        universalFunctions.addView(storyList.get(counter).getStoryID());
        universalFunctions.seenNumber(storyList.get(counter).getStoryID(), seen_number);
    }

    private void checkStoryTags(Story story) {
        storyReference.child(story.getStoryID()).child("taggedFriends")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    if (snapshot.getChildrenCount() == 1)
                        storyTaggedPeopleList.setText("with: " + snapshot.getChildrenCount() + " friend");
                    else
                        storyTaggedPeopleList.setText("with: " + snapshot.getChildrenCount() + " friends");

                    storyTagsArea.setVisibility(View.VISIBLE);
                }else{
                    storyTagsArea.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                    if (user.getUserID().equals(userID)){

                        for(ContactsModel contactsModel : phoneContacts){
                            if(userID.equals(firebaseUser.getUid())){
                                storyUsername.setText("Me");
                            }else if(contactsModel.getPhoneNumber().equals(user.getPhoneNumber())){
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
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNext() {

        int position = ++counter;
        storyID = storyList.get(position).getStoryID();

        getStoryDetails(position);

    }

    @Override
    public void onPrev() {
        if((counter - 1) < 0 ) return;

        int position = --counter;
        storyID = storyList.get(position).getStoryID();

        getStoryDetails(position);
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
                break;

            case R.id.storyViewItem:
                storiesProgressView.pause();
                showViewsDialog(storyList.get(counter));
                break;

            case R.id.storyReportItem:
                Intent reportIntent = new Intent(StoryActivity.this, ReportActivity.class);
                reportIntent.putExtra("reported", storyList.get(counter).getStoryID());
                startActivity(reportIntent);
                break;

            case R.id.storyDeleteItem:
                AlertDialog.Builder builder = new AlertDialog.Builder(StoryActivity.this);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this story?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                StorageReference imageRef = FirebaseStorage.getInstance().getReference(storyList.get(counter).getImageURL());
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
                break;
        }
        return false;
    }

}
