package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.Notifications.SendNotifications;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioPlayer;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioRecorder;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    private CircleImageView hisProPic, myProPic;
    private TextView hisName, postLocation, postTimestamp, likeCounter,
            commentCounter, postCaption, postCommentBTN, commentsLocationTV, shareCounter, taggedPeopleList;
    private ImageView likesIcon, saveIcon, goToGalleryBTN, commentPostImage;
    private RelativeLayout likesArea, shareArea, saveArea, commentMediaPlayer;
    private LinearLayout locationArea, commentLocationArea, tagsArea;
    private EditText commentET;
    private RecyclerView commentsRecycler;
    private TextInputLayout commentCaptionArea;

    private DatabaseReference commentReference, userReference, postReference,
            storyReference, likesReference, videoReference;
    private FirebaseUser firebaseUser;

    private String itemID, userID, commentID;
    private UniversalFunctions universalFunctions;
    private SendNotifications notifications;
    private GetTimeAgo getTimeAgo;
    private LocationServices locationServices;
    private UploadFunctions uploadFunctions;

    private List<CommentModel> commentList;

    private Uri imageUri;
    private StorageTask uploadTask;
    private StorageReference storageReference, audioReference;
    private ProgressDialog uploadDialog;

    //for recording audio status
    private AudioRecorder audioRecorder;
    private LottieAnimationView lavPlaying;
    private ImageView voicePlayBTN, deleteAudioBTN;
    private TextView seekTimer;
    private RelativeLayout playAudioArea;

    //for playing audio status
    private AudioPlayer audioPlayer;
    public CircleImageView playBTN;
    public LottieAnimationView audioAnimation;
    public TextView audioSeekTimer, postTotalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Toolbar movementToolbar = findViewById(R.id.comments_toolbar);
        setSupportActionBar(movementToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        hisProPic = findViewById(R.id.comments_his_moment_image_user);
        myProPic = findViewById(R.id.comments_my_image_profile);
        hisName = findViewById(R.id.comments_moment_username);
        postLocation = findViewById(R.id.comments_postCheckIn);
        postTimestamp = findViewById(R.id.comments_timestamp);
        likeCounter = findViewById(R.id.likeCounter);
        commentCounter = findViewById(R.id.commentCounter);
        postCaption = findViewById(R.id.comments_Caption);
        postCommentBTN = findViewById(R.id.postCommentBTN);
        likesIcon = findViewById(R.id.postLikeBTN);
        saveIcon = findViewById(R.id.savePostBTN);
        likesArea = findViewById(R.id.likesArea);
        shareArea = findViewById(R.id.shareArea);
        saveArea = findViewById(R.id.saveArea);
        locationArea = findViewById(R.id.commentsLocationArea);
        commentET = findViewById(R.id.comments_add_comment);
        commentsRecycler = findViewById(R.id.commentsRecycler);
        goToGalleryBTN = findViewById(R.id.commentsGoToGallery);
        commentPostImage = findViewById(R.id.commentPostImage);
        commentsLocationTV = findViewById(R.id.commentsLocationCheckIn);
        commentLocationArea = findViewById(R.id.comments_location_area);
        commentCaptionArea = findViewById(R.id.commentCaptionArea);
        commentMediaPlayer = findViewById(R.id.commentMediaPlayer);
        shareCounter = findViewById(R.id.shareCounter);
        taggedPeopleList = findViewById(R.id.taggedPeopleList);
        tagsArea = findViewById(R.id.tagsArea);

        //for recording the audio
        playAudioArea = findViewById(R.id.playAudioArea);
        voicePlayBTN = findViewById(R.id.post_playVoiceIcon);
        seekTimer = findViewById(R.id.seekTimer);
        lavPlaying = findViewById(R.id.lav_playing);
        deleteAudioBTN = findViewById(R.id.recordingDeleteBTN);

        //for audio buttons
        playBTN = findViewById(R.id.postItem_playVoiceIcon);
        audioAnimation = findViewById(R.id.postItem_lav_playing);
        audioSeekTimer = findViewById(R.id.postItemSeekTimer);
        postTotalTime = findViewById(R.id.postTotalTime);

        itemID = getIntent().getStringExtra("postID");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        commentReference = FirebaseDatabase.getInstance().getReference("Comments");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        postReference = FirebaseDatabase.getInstance().getReference("SecretPosts");
        storyReference = FirebaseDatabase.getInstance().getReference("Story");
        storageReference = FirebaseStorage.getInstance().getReference("comment_images");
        audioReference = FirebaseStorage.getInstance().getReference();
        likesReference = FirebaseDatabase.getInstance().getReference("Likes");
        videoReference = FirebaseDatabase.getInstance().getReference("SecretVideos");

        universalFunctions = new UniversalFunctions(this);
        notifications = new SendNotifications(this);
        getTimeAgo = new GetTimeAgo();
        commentList = new ArrayList<>();
        uploadFunctions = new UploadFunctions(this);
        uploadDialog = new ProgressDialog(this);
        locationServices = new LocationServices(commentsLocationTV, CommentsActivity.this);
        audioRecorder = new AudioRecorder(lavPlaying, CommentsActivity.this,
                CommentsActivity.this, playAudioArea, voicePlayBTN, seekTimer);

        audioPlayer = new AudioPlayer(CommentsActivity.this, playBTN,
                audioSeekTimer, postTotalTime, audioAnimation);

        commentsRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        commentsRecycler.setLayoutManager(layoutManager);

        commentID = commentReference.push().getKey();

        getMyDetails();
        getPostDetails();
        readComments();

        universalFunctions.isLiked(itemID, likesIcon);
        universalFunctions.nrLikes(likeCounter, itemID);
        universalFunctions.getCommentsCount(itemID, commentCounter);
        universalFunctions.isSaved(itemID, saveIcon);
        universalFunctions.getSharedNumber(itemID, shareCounter);

        commentET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0)
                    postCommentBTN.setText("Post");
                else
                    postCommentBTN.setText("Record");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        likeCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showLikesDialog();

            }
        });

        shareArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(CommentsActivity.this, SharePostActivity.class);
                shareIntent.putExtra("postID", itemID);
                startActivity(shareIntent);
            }
        });

        saveArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(saveIcon.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(itemID).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(itemID).removeValue();
                }
            }
        });

        postCommentBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (postCommentBTN.getText().toString().trim().equals("Record")){

                    if (audioRecorder.checkRecordingPermission()){
                        commentCaptionArea.setVisibility(View.GONE);
                        if(!audioRecorder.isRecording()){
                            postCommentBTN.setText("Stop");
                            audioRecorder.startRecording();
                        }
                    }else{
                        audioRecorder.requestRecordingPermission();
                    }

                }else if (postCommentBTN.getText().toString().trim().equals("Stop")){

                    postCommentBTN.setText("Post");
                    audioRecorder.stopRecording();

                }else if (postCommentBTN.getText().toString().trim().equals("Post")){

                    uploadDialog.setMessage("loading...");
                    uploadDialog.show();

                    if (!TextUtils.isEmpty(commentET.getText().toString())){
                        if (imageUri != null){
                            uploadImageTextComment();
                        }else{
                            uploadTextComment();
                        }

                    }else if(audioRecorder.getRecordingFilePath() != null){

                        if (imageUri != null)
                            uploadImageAudioComment();
                        else
                            uploadAudioComment();
                    }

                }

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

        deleteAudioBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //audioRecorder.deleteRecording();
                Toast.makeText(CommentsActivity.this, "Recording will be deleted", Toast.LENGTH_SHORT).show();
            }
        });

        likesArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(likesIcon.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(itemID)
                            .child(firebaseUser.getUid()).setValue(true);
                    /*if (!userID.equals(firebaseUser.getUid()))
                        notifications.addLikesNotification(itemID, userID);*/
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(itemID)
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        goToGalleryBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri == null){
                    CropImage.activity()
                            .setAspectRatio(1, 1)
                            .start(CommentsActivity.this);
                }else{
                    Toast.makeText(CommentsActivity.this, "Options will appear here", Toast.LENGTH_SHORT).show();
                }
            }
        });

        hisProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent picIntent = new Intent(CommentsActivity.this, FullScreenImageActivity.class);
                picIntent.putExtra("itemID", userID);
                picIntent.putExtra("reason", "userImage");
                startActivity(picIntent);
            }
        });

        commentLocationArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationServices.getMyLocation();
            }
        });

        tagsArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTaggedFriends();
            }
        });

        shareCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showShareDialog();
            }
        });

    }

    private void uploadAudioComment() {
        StorageReference audioPath = audioReference.child("AudioComments").child(commentID + ".3gp");
        Uri audioUri = Uri.fromFile(new File(audioRecorder.getRecordingFilePath()));

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
                    audioMap.put("postID", itemID);
                    audioMap.put("commentType", "audioComment");
                    audioMap.put("audioUrl", audioDownloadLink.toString());

                    if(!commentsLocationTV.getText().toString().equals("No Location")){
                        audioMap.put("latitude", locationServices.getLatitude());
                        audioMap.put("longitude", locationServices.getLongitude());
                    }

                    commentReference.child(itemID).child(commentID).setValue(audioMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    resetLayout();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CommentsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    private void uploadImageAudioComment() {

        StorageReference audioPath = audioReference.child("AudioComments").child(commentID + ".3gp");
        Uri audioUri = Uri.fromFile(new File(audioRecorder.getRecordingFilePath()));

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

                    final StorageReference imageReference = storageReference.child(System.currentTimeMillis()
                            + "." + uploadFunctions.getFileExtension(imageUri));

                    uploadTask = imageReference.putFile(imageUri);
                    uploadTask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if (!task.isSuccessful())
                                throw task.getException();
                            return imageReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                Uri imageDownloadLink = task.getResult();

                                HashMap<String, Object> audioImageMap = new HashMap<>();

                                audioImageMap.put("commentID", commentID);
                                audioImageMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
                                audioImageMap.put("userID", firebaseUser.getUid());
                                audioImageMap.put("commentImage", imageDownloadLink.toString());
                                audioImageMap.put("postID", itemID);
                                audioImageMap.put("commentType", "audioImageComment");
                                audioImageMap.put("audioUrl", audioDownloadLink.toString());

                                if(!commentsLocationTV.getText().toString().equals("No Location")){
                                    audioImageMap.put("latitude", locationServices.getLatitude());
                                    audioImageMap.put("longitude", locationServices.getLongitude());
                                }

                                commentReference.child(itemID).child(commentID).setValue(audioImageMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                resetLayout();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(CommentsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

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
        commentMap.put("postID", itemID);
        commentMap.put("commentType", "textComment");

        if(!commentsLocationTV.getText().toString().equals("No Location")){
            commentMap.put("latitude", locationServices.getLatitude());
            commentMap.put("longitude", locationServices.getLongitude());
        }

        commentReference.child(itemID).child(commentID).setValue(commentMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        resetLayout();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CommentsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImageTextComment() {
        final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                + "." + uploadFunctions.getFileExtension(imageUri));

        uploadTask = fileReference.putFile(imageUri);
        uploadTask.continueWithTask(
                new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful())
                            throw task.getException();

                        return fileReference.getDownloadUrl();
                    }
                }
        ).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri downloadUri = task.getResult();

                    HashMap<String, Object> commentMap = new HashMap<>();

                    commentMap.put("commentID", commentID);
                    commentMap.put("comment", commentET.getText().toString());
                    commentMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
                    commentMap.put("userID", firebaseUser.getUid());
                    commentMap.put("commentImage", String.valueOf(downloadUri));
                    commentMap.put("postID", itemID);
                    commentMap.put("commentType", "imageComment");

                    if (!commentsLocationTV.getText().toString().equals("No Location")){
                        commentMap.put("latitude", locationServices.getLatitude());
                        commentMap.put("longitude", locationServices.getLongitude());
                    }

                    commentReference.child(itemID).child(commentID).setValue(commentMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    resetLayout();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CommentsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetLayout() {

        if (!userID.equals(firebaseUser.getUid()))
            if (TextUtils.isEmpty(commentET.getText().toString()))
                notifications.addPostCommentNotification(userID, itemID, "recorded a comment");
            else
                notifications.addPostCommentNotification(userID, itemID, commentET.getText().toString());

        if (imageUri != null) {
            commentPostImage.setImageURI(null);
            commentPostImage.setVisibility(View.GONE);
        }

        if (audioRecorder.getRecordingFilePath() != null){
            audioRecorder.resetRecorder();
        }

        commentET.setText("");

        if (!commentsLocationTV.getText().toString().equals("No Location"))
            commentsLocationTV.setText("No Location");

        uploadDialog.dismiss();
    }

    private void showLikesDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);

        assert interactionHeader != null;
        interactionHeader.setText("Likes");

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(this));
        friendsRecycler.setNestedScrollingEnabled(true);

        likesReference.child(itemID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    idList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        idList.add(ds.getKey());
                    }
                    friendsRecycler.setAdapter(new UserAdapter(CommentsActivity.this, idList, "goToProfile"));
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

    private void showShareDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);

        assert interactionHeader != null;
        interactionHeader.setText("Shared By");

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(this));
        friendsRecycler.setNestedScrollingEnabled(true);
        UserAdapter userAdapter = new UserAdapter(CommentsActivity.this, idList, "goToProfile");
        friendsRecycler.setAdapter(userAdapter);

        postReference.child(itemID).child("sharedBy").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    idList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        idList.add(ds.getKey());
                    }
                    userAdapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(CommentsActivity.this, "Could not retrieve shares", Toast.LENGTH_SHORT).show();
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

    private void showTaggedFriends() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);

        assert interactionHeader != null;
        interactionHeader.setText("Tagged Friends");

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(this));
        friendsRecycler.setNestedScrollingEnabled(true);
        UserAdapter userAdapter = new UserAdapter(CommentsActivity.this, idList, "goToProfile");
        friendsRecycler.setAdapter(userAdapter);

        postReference.child(itemID).child("taggedFriends")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            idList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()){
                                idList.add(ds.getKey());
                            }
                            userAdapter.notifyDataSetChanged();
                        }else{
                            storyReference.child(userID).child(itemID).child("taggedFriends")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()){
                                                idList.clear();
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

    private void readComments() {
        commentReference.child(itemID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    commentList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        CommentModel comments = ds.getValue(CommentModel.class);
                        commentList.add(comments);
                    }
                    commentsRecycler.setAdapter(new CommentsAdapter(CommentsActivity.this, commentList));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPostDetails() {

        postReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        PostModel model = ds.getValue(PostModel.class);

                        assert model != null;
                        if (model.getPostID().equals(itemID)){
                            displayPostDetails(model);
                        }else{
                            getOthersCategories();
                        }
                    }
                }else{
                    getOthersCategories();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getOthersCategories() {
        videoReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot data : snapshot.getChildren()){
                        PostModel video = data.getValue(PostModel.class);

                        assert video != null;
                        if (video.getPostID().equals(itemID))
                            displayPostDetails(video);
                        else
                            getStoryDetails();
                    }
                }else{
                    getStoryDetails();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getStoryDetails() {
        storyReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    if (ds.child(itemID).exists()){
                        shareCounter.setVisibility(View.GONE);
                        Story story = ds.child(itemID).getValue(Story.class);

                        assert story != null;
                        userID = story.getUserID();

                        if (story.getStoryType().equals("textStory")){
                            postCaption.setText(story.getStoryCaption());

                        }else if(story.getStoryType().equals("audioStory")){
                            postCaption.setVisibility(View.GONE);
                            commentMediaPlayer.setVisibility(View.VISIBLE);

                            playBTN.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    if (!audioPlayer.isPlaying()){
                                        audioPlayer.startPlayingAudio(story.getStoryAudioUrl());
                                    }else{
                                        audioPlayer.stopPlayingAudio();
                                    }
                                }
                            });

                        }

                        try{
                            universalFunctions.findAddress(story.getLatitude(), story.getLongitude(), postLocation, locationArea);
                        }catch (Exception ignored){}

                        try {
                            postTimestamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(story.getStoryTimeStamp()), CommentsActivity.this));
                        } catch (NumberFormatException ignored) {}

                        storyReference.child(story.getUserID()).child(story.getStoryID()).child("taggedFriends")
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){

                                            if (snapshot.getChildrenCount() == 1)
                                                taggedPeopleList.setText("with: "+ snapshot.getChildrenCount() +" friend");
                                            else
                                                taggedPeopleList.setText("with: "+ snapshot.getChildrenCount() +" friends");

                                            tagsArea.setVisibility(View.VISIBLE);
                                        }else {
                                            tagsArea.setVisibility(View.GONE);
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                        userReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    User user = ds.getValue(User.class);

                                    assert user != null;
                                    if (user.getUserID().equals(story.getUserID())) {

                                        if (user.getUserID().equals(firebaseUser.getUid()))
                                            hisName.setText("Me");
                                        else
                                            hisName.setText(user.getUsername());

                                        try {
                                            Picasso.get().load(user.getImageURL()).into(hisProPic);
                                        } catch (NullPointerException e) {
                                            Picasso.get().load(R.drawable.default_profile_display_pic).into(hisProPic);
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

    private void displayPostDetails(PostModel model) {

        userID = model.getUserID();

        if (model.getPostType().equals("audioPost")
                || model.getPostType().equals("audioImagePost")
                || model.getPostType().equals("audioVideoPost")
                || model.getPostType().equals("sharedAudioTextPost")
                || model.getPostType().equals("sharedAudioVideoPost")
                || model.getPostType().equals("sharedAudioImagePost")
                || model.getPostType().equals("sharedAudioAudioPost")
                || model.getPostType().equals("sharedAudioAudioImagePost")){

            postCaption.setVisibility(View.GONE);
            commentMediaPlayer.setVisibility(View.VISIBLE);

            playBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!audioPlayer.isPlaying()){
                        audioPlayer.startPlayingAudio(model.getAudioURL());
                    }else{
                        audioPlayer.stopPlayingAudio();
                    }
                }
            });

        }else{
            postCaption.setText(model.getPostCaption());
        }

        try{
            postTimestamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(model.getPostTime()), this));
        }catch (NumberFormatException ignored){}

        try{
            universalFunctions.findAddress(model.getLatitude(), model.getLongitude(), postLocation, locationArea);
        }catch (Exception ignored){}

        try{
            if (model.getPostPrivacy().equals("Private")){
                shareArea.setVisibility(View.GONE);
            }
        }catch (NullPointerException ignored){}

        postReference.child(model.getPostID()).child("taggedFriends")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){

                            if (snapshot.getChildrenCount() == 1)
                                taggedPeopleList.setText("with: "+ snapshot.getChildrenCount() +" friend");
                            else
                                taggedPeopleList.setText("with: "+ snapshot.getChildrenCount() +" friends");

                            tagsArea.setVisibility(View.VISIBLE);
                        }else {
                            tagsArea.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUserID().equals(model.getUserID())){
                        try {
                            Picasso.get().load(user.getImageURL()).into(hisProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(hisProPic);
                        }

                        if (user.getUserID().equals(firebaseUser.getUid()))
                            hisName.setText("Me");
                        else
                            hisName.setText(user.getUsername());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMyDetails() {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUserID().equals(firebaseUser.getUid())){
                        try{
                            Picasso.get().load(user.getImageURL()).into(myProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(myProPic);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            commentPostImage.setImageURI(imageUri);
            commentPostImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == audioRecorder.REQUEST_AUDIO_PERMISSION){
            if (grantResults.length > 0){
                boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if (permissionToRecord){
                    Toast.makeText(this, "Recording permission granted", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "Recording permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.story_menu, menu);

        menu.findItem(R.id.storyViewItem).setVisible(false);
        menu.findItem(R.id.storyDeleteItem).setVisible(false);
        menu.findItem(R.id.storyReportItem).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.storyViewProfileItem:
                Intent profileIntent = new Intent(CommentsActivity.this, ViewProfileActivity.class);
                profileIntent.putExtra("uid", userID);
                startActivity(profileIntent);
                return true;

        }
        return false;
    }

}