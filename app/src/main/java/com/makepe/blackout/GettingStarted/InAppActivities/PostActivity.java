package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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
import com.makepe.blackout.GettingStarted.Adapters.FriendsAdapter;
import com.makepe.blackout.GettingStarted.Adapters.PostMediaAdapter;
import com.makepe.blackout.GettingStarted.Adapters.TaggedFriendsAdapter;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.Notifications.SendNotifications;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioRecorder;
import com.makepe.blackout.GettingStarted.OtherClasses.LocationServices;
import com.makepe.blackout.GettingStarted.OtherClasses.UploadFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private DatabaseReference userRef, postRef, followingReference, videoReference;
    private StorageReference postImageReference, audioReference;

    private TextView postBTN, imageCounter, tagFriendsBTN;
    private RelativeLayout goToCameraBTN, goToImageGalleryBTN, goToVideoGalleryBTN;
    private EditText captionArea;
    private ImageView addMoreImagesBTN;
    private CircleImageView postActivityProPic;
    private String postID, timeStamp;
    private CardView imageCardView, videoCardView;
    private TextInputLayout postCaptionArea;
    private Switch commentSwitch;
    private Toolbar postToolbar;
    private RecyclerView mediaRecyclerView, taggedFriendsRecycler;
    private LinearSnapHelper snapHelper;

    private ProgressDialog uploadDialog;

    private PostMediaAdapter mediaAdapter;
    private ArrayList<String> mediaUriList, mediaIdList;
    private ArrayList<User> taggedUsers = new ArrayList<>();
    private TaggedFriendsAdapter taggedFriendsAdapter;
    private UploadFunctions uploadFunctions;
    private SendNotifications sendNotifications;

    private int totalMediaUploaded = 0;
    private static final int PICK_IMAGE_INTENT = 1;
    private static final int PICK_VIDEO_REQUEST = 2;
    private static final int VIDEO_CAMERA_REQUEST_CODE = 3;
    private static final int CAMERA_REQUEST_CODE = 4;

    //for video gallery
    private Uri videoURI;
    private VideoView videoView;
    private ConstraintLayout videoArea;
    private String mediaUrl;

    //for Location services
    private LocationServices locationServices;
    private RelativeLayout myLocationAreaBTN, privacyBTN;
    private TextView tagLocation, audiencePicker;
    private String privacyProtection = "Public";

    //for recording audio status
    private AudioRecorder audioRecorder;
    private LottieAnimationView lavPlaying;
    private ImageView voicePlayBTN, deleteAudioBTN;
    private TextView seekTimer;
    private RelativeLayout playAudioArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poste);

        postToolbar = findViewById(R.id.postToolbar);
        setSupportActionBar(postToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postActivityProPic = findViewById(R.id.addPostProPic);
        postBTN = findViewById(R.id.postFAB);
        tagLocation = findViewById(R.id.locationCheckIn);
        myLocationAreaBTN = findViewById(R.id.postLocationBTN);
        captionArea = findViewById(R.id.addPostArea);
        imageCardView = findViewById(R.id.imageCardView);
        videoView = findViewById(R.id.previewCameraSelectedVideo);
        videoArea = findViewById(R.id.videoArea);
        privacyBTN = findViewById(R.id.privacySettingsBTN);
        audiencePicker = findViewById(R.id.audience_picker);
        videoCardView = findViewById(R.id.videoCardView);
        postCaptionArea = findViewById(R.id.postCaptionArea);
        deleteAudioBTN = findViewById(R.id.recordingDeleteBTN);
        commentSwitch = findViewById(R.id.commentSwitch);
        mediaRecyclerView = findViewById(R.id.postRecyclerView);
        imageCounter = findViewById(R.id.imageCounter);
        addMoreImagesBTN = findViewById(R.id.addMoreImagesBTN);
        goToCameraBTN = findViewById(R.id.goToCameraBTN);
        goToImageGalleryBTN = findViewById(R.id.goToImageGalleryBTN);
        goToVideoGalleryBTN = findViewById(R.id.goToVideoGalleryBTN);
        tagFriendsBTN = findViewById(R.id.tagFriendsBTN);
        taggedFriendsRecycler = findViewById(R.id.taggedFriendsRecycler);

        playAudioArea = findViewById(R.id.playAudioArea);
        voicePlayBTN = findViewById(R.id.post_playVoiceIcon);
        seekTimer = findViewById(R.id.seekTimer);
        lavPlaying = findViewById(R.id.lav_playing);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        postRef = FirebaseDatabase.getInstance().getReference("SecretPosts");
        videoReference = FirebaseDatabase.getInstance().getReference("SecretVideos");
        postImageReference = FirebaseStorage.getInstance().getReference("user_image_posts");
        audioReference = FirebaseStorage.getInstance().getReference();
        followingReference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid()).child("following");

        uploadDialog = new ProgressDialog(this);
        locationServices = new LocationServices(tagLocation, PostActivity.this);
        audioRecorder = new AudioRecorder(lavPlaying, PostActivity.this,
                PostActivity.this, playAudioArea, voicePlayBTN, seekTimer);
        uploadFunctions = new UploadFunctions(PostActivity.this);
        sendNotifications = new SendNotifications(PostActivity.this);
        snapHelper = new LinearSnapHelper();

        postID = postRef.push().getKey();
        timeStamp = String.valueOf(System.currentTimeMillis());
        mediaUriList = new ArrayList<>();
        mediaIdList = new ArrayList<>();

        mediaRecyclerView.hasFixedSize();
        mediaRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mediaAdapter = new PostMediaAdapter(PostActivity.this, mediaUriList, imageCardView);
        mediaRecyclerView.setAdapter(mediaAdapter);
        snapHelper.attachToRecyclerView(mediaRecyclerView);

        taggedFriendsRecycler.hasFixedSize();
        taggedFriendsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        taggedFriendsAdapter = new TaggedFriendsAdapter(PostActivity.this, taggedUsers);
        taggedFriendsRecycler.setAdapter(taggedFriendsAdapter);

        getUserDetails();

        captionArea.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0)
                    postBTN.setText("Post");
                else
                    postBTN.setText("Record");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        tagLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                locationServices.getMyLocation();
            }
        });

        privacyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(PostActivity.this, privacyBTN, Gravity.END);
                popupMenu.getMenu().add(Menu.NONE, 0, 0, "Public");
                popupMenu.getMenu().add(Menu.NONE, 1, 0, "Only To My Followers");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()){
                            case 0:
                                privacyProtection = "Public";
                                audiencePicker.setText(privacyProtection);
                                break;

                            case 1:
                                privacyProtection = "Private";
                                audiencePicker.setText("Only To My Followers");
                                break;

                            default:
                                Toast.makeText(PostActivity.this, "Unknown Selection", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        voicePlayBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!audioRecorder.isPlaying()){
                    if (videoURI != null)
                        videoView.pause();
                    audioRecorder.startPlayingRecording();
                }else{
                    audioRecorder.stopPlayingAudio();

                    if (videoView != null)
                        videoView.start();
                }
            }
        });

        deleteAudioBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioRecorder.resetRecorder();
                postBTN.setText("Record");
            }
        });

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoView.isPlaying())
                    videoView.pause();
                else
                    videoView.start();
            }
        });

        addMoreImagesBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImages();
            }
        });

        postBTN.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {

                if (postBTN.getText().toString().trim().equals("Record")){

                    if(audioRecorder.checkRecordingPermission()){
                        postCaptionArea.setVisibility(View.GONE);
                        if (!audioRecorder.isRecording()){

                            if (videoURI != null)
                                videoView.pause();

                            postBTN.setText("Stop");
                            audioRecorder.startRecording();
                        }
                    }else{
                        audioRecorder.requestRecordingPermission();
                    }

                }else if (postBTN.getText().toString().trim().equals("Stop")){

                    postBTN.setText("Post");
                    audioRecorder.stopRecording();

                    if (videoURI != null)
                        videoView.start();

                }else if (postBTN.getText().toString().trim().equals("Post")){
                    //Button for posting to the database
                    uploadDialog.setMessage("Loading...");
                    uploadDialog.setCancelable(false);
                    uploadDialog.show();

                    if (!TextUtils.isEmpty(captionArea.getText().toString().trim())){
                        if (videoURI != null)
                            uploadTextVideoPost();
                        else
                            uploadTextPost();

                    }else if (audioRecorder.getRecordingFilePath() != null){
                        if (videoURI != null)
                            uploadVideoAudioPost();
                        else
                            uploadAudioPost();
                    }

                }
            }
        });

        goToImageGalleryBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImages();
            }
        });

        goToVideoGalleryBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseVideo();
            }
        });

        goToCameraBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] options = {"Camera", "Video"};

                AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
                builder.setTitle("Choose Selection")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i){
                                    case 0:
                                        Toast.makeText(PostActivity.this, "You will be able to take a picture", Toast.LENGTH_SHORT).show();
                                        break;

                                    case 1:
                                        openCameraRecorder();
                                        break;

                                }
                            }
                        }).show();
            }
        });

        tagFriendsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFriendsDialog();
            }
        });

    }

    private void showFriendsDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.tag_friends_bottom_sheet);

        ImageView close = bottomSheetDialog.findViewById(R.id.closeSheetBTN);
        TextView taggedFriendsDoneBTN = bottomSheetDialog.findViewById(R.id.taggedFriendsDoneBTN);
        SearchView searchView = bottomSheetDialog.findViewById(R.id.searchView);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.tagFriendsRecycler);

        List<User> userList = new ArrayList<>();
        List<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(this));
        friendsRecycler.setNestedScrollingEnabled(true);
        FriendsAdapter friendsAdapter = new FriendsAdapter(userList, PostActivity.this);
        friendsRecycler.setAdapter(friendsAdapter);

        followingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    idList.add(ds.getKey());
                }

                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            User user = ds.getValue(User.class);

                            for (String ID : idList){
                                assert user != null;
                                if (user.getUserID().equals(ID))
                                    userList.add(user);
                            }

                            Collections.sort(userList, new Comparator<User>() {
                                @Override
                                public int compare(User o1, User o2) {
                                    return o1.getUsername().compareTo(o2.getUsername());
                                }
                            });
                            friendsAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        bottomSheetDialog.show();
        bottomSheetDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        assert close != null;
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        taggedFriendsDoneBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taggedUsers.addAll(friendsAdapter.taggedFriends);
                taggedFriendsAdapter.notifyDataSetChanged();
                bottomSheetDialog.dismiss();

            }
        });
    }

    private void uploadTextVideoPost() {

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("user_video_posts")
                .child(postID + "." + uploadFunctions.getFileExtension(videoURI));

        UploadTask uploadTask = ref.putFile(videoURI);

        uploadTask.addOnProgressListener(taskSnapshot ->{
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            Log.d("PROGRESS", "Upload is " + progress + "% done");
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
            }
        });

        Task<Uri> urlTask = uploadTask.continueWithTask(task ->{
            if (!task.isSuccessful())
                throw task.getException();
            return ref.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Uri downloadUri = task.getResult();
                mediaUrl = downloadUri.toString();
                Log.i("downloadTag", mediaUrl);

                HashMap<String, Object> videoTextMap = new HashMap<>();

                //put the post info
                videoTextMap.put("userID", firebaseUser.getUid());//usersID
                videoTextMap.put("postID", postID); //the id of the post is the time at which the post has been added
                videoTextMap.put("postCaption", captionArea.getText().toString()); // the post caption
                videoTextMap.put("postTime", timeStamp);// the time at which the post has been posted
                videoTextMap.put("postPrivacy", privacyProtection);
                videoTextMap.put("postType", "videoPost");
                videoTextMap.put("videoURL", mediaUrl);

                if (!tagLocation.getText().toString().equals("No Location")) {
                    videoTextMap.put("latitude", locationServices.getLatitude());
                    videoTextMap.put("longitude", locationServices.getLongitude());
                }

                if (commentSwitch.isChecked())
                    videoTextMap.put("commentsAllowed", false);
                else
                    videoTextMap.put("commentsAllowed", true);

                videoReference.child(postID).setValue(videoTextMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                checkVideoPostTags();

                                captionArea.setText("");
                                videoView.setVideoURI(null);

                                if (!tagLocation.getText().toString().equals("No Location")) {
                                    tagLocation.setText("No Location");
                                }
                                uploadDialog.dismiss();

                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

            }
        });
    }

    private void uploadVideoAudioPost() {

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("user_video_posts")
                .child(postID + "." + uploadFunctions.getFileExtension(videoURI));

        UploadTask uploadTask = ref.putFile(videoURI);

        uploadTask.addOnProgressListener(taskSnapshot ->{
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            Log.d("PROGRESS", "Upload is " + progress + "% done");
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
            }
        });

        Task<Uri> urlTask = uploadTask.continueWithTask(task ->{
            if (!task.isSuccessful()){
                throw task.getException();
            }
            return ref.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Uri downloadUri = task.getResult();
                mediaUrl = downloadUri.toString();
                Log.i("downloadTag", mediaUrl);

                StorageReference filePath = audioReference.child("user_audio_posts").child(postID + ".3gp");

                Uri audioUri = Uri.fromFile(new File(audioRecorder.getRecordingFilePath()));

                StorageTask<UploadTask.TaskSnapshot> audioTask = filePath.putFile(audioUri);

                audioTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful())
                            throw task.getException();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUri = task.getResult();
                            String audioLink = downloadUri.toString();

                            HashMap<String, Object> videoAudioMap = new HashMap<>();

                            //put the post info
                            videoAudioMap.put("userID", firebaseUser.getUid());//usersID
                            videoAudioMap.put("postID", postID); //the id of the post is the time at which the post has been added
                            videoAudioMap.put("postTime", timeStamp);// the time at which the post has been posted
                            videoAudioMap.put("postType", "audioVideoPost");
                            videoAudioMap.put("videoURL", mediaUrl);
                            videoAudioMap.put("postPrivacy", privacyProtection);
                            videoAudioMap.put("audioURL", audioLink);

                            if (!tagLocation.getText().toString().equals("No Location")) {
                                videoAudioMap.put("latitude", locationServices.getLatitude());
                                videoAudioMap.put("longitude", locationServices.getLongitude());
                            }

                            if (commentSwitch.isChecked())
                                videoAudioMap.put("commentsAllowed", false);
                            else
                                videoAudioMap.put("commentsAllowed", true);

                            videoReference.child(postID).setValue(videoAudioMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            checkVideoPostTags();

                                            if (!tagLocation.getText().toString().equals("No Location")) {
                                                tagLocation.setText("No Location");
                                            }

                                            audioRecorder.resetRecorder();
                                            videoView.setVideoURI(null);
                                            uploadDialog.dismiss();
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostActivity.this, "Uploading Audio Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void uploadAudioPost() {

        StorageReference filePath = audioReference.child("user_audio_posts").child(postID + ".3gp");

        Uri audioUri = Uri.fromFile(new File(audioRecorder.getRecordingFilePath()));

        StorageTask<UploadTask.TaskSnapshot> audioTask = filePath.putFile(audioUri);

        audioTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful())
                    throw task.getException();
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    String audioLink = downloadUri.toString();

                    HashMap<String, Object> audioMap = new HashMap<>();

                    //put the post info
                    audioMap.put("userID", firebaseUser.getUid());//usersID
                    audioMap.put("postID", postID); //the id of the post is the time at which the post has been added
                    audioMap.put("postTime", timeStamp);// the time at which the post has been posted
                    audioMap.put("postPrivacy", privacyProtection);
                    audioMap.put("audioURL", audioLink);

                    if (mediaUriList.isEmpty())
                        audioMap.put("postType", "audioPost");
                    else
                        audioMap.put("postType", "audioImagePost");

                    if (!tagLocation.getText().toString().equals("No Location")) {
                        audioMap.put("latitude", locationServices.getLatitude());
                        audioMap.put("longitude", locationServices.getLongitude());
                    }

                    if (commentSwitch.isChecked())
                        audioMap.put("commentsAllowed", false);
                    else
                        audioMap.put("commentsAllowed", true);

                    postRef.child(postID).setValue(audioMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    uploadDialog.dismiss();

                                    if (mediaUriList.isEmpty()) {

                                        checkPostTags();

                                        if (!tagLocation.getText().toString().equals("No Location")) {
                                            tagLocation.setText("No Location");
                                        }
                                        audioRecorder.resetRecorder();
                                        finish();
                                    }else{
                                        uploadImages();
                                    }

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostActivity.this, "Uploading Audio Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadTextPost() {

        HashMap<String, Object> textMap = new HashMap<>();

        textMap.put("userID", firebaseUser.getUid());//usersID
        textMap.put("postID", postID);
        textMap.put("postCaption", captionArea.getText().toString().trim());
        textMap.put("postTime", timeStamp);
        textMap.put("postPrivacy", privacyProtection);//post security

        if (mediaUriList.isEmpty())
            textMap.put("postType", "textPost");//post security
        else
            textMap.put("postType", "imagePost");

        if (!tagLocation.getText().toString().equals("No Location")) {
            textMap.put("latitude", locationServices.getLatitude());
            textMap.put("longitude", locationServices.getLongitude());
        }

        if (commentSwitch.isChecked())
            textMap.put("commentsAllowed", false);
        else
            textMap.put("commentsAllowed", true);

        postRef.child(postID).setValue(textMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        if (mediaUriList.isEmpty()) {

                            checkPostTags();

                            captionArea.setText("");

                            if (!tagLocation.getText().toString().equals("No Location")) {
                                tagLocation.setText("No Location");
                            }
                            uploadDialog.dismiss();
                            finish();
                        }else{
                            uploadImages();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void uploadImages() {
        HashMap<String, Object> mediaMap = new HashMap<>();

        for (String mediaUri : mediaUriList){

            String mediaID = postRef.child("images").push().getKey();
            mediaIdList.add(mediaID);

            final StorageReference filePath = postImageReference.child(mediaID
                    + "." + uploadFunctions.getFileExtension(Uri.parse(mediaUri)));

            UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mediaMap.put("image" + totalMediaUploaded, uri.toString());

                            totalMediaUploaded++;

                            if (totalMediaUploaded == mediaUriList.size()){
                                postRef.child(postID).child("images").updateChildren(mediaMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                if (!mediaUriList.isEmpty() && !mediaIdList.isEmpty()){

                                                    checkPostTags();

                                                    captionArea.setText("");

                                                    if (!tagLocation.getText().toString().equals("No Location")) {
                                                        tagLocation.setText("No Location");
                                                    }

                                                    mediaIdList.clear();
                                                    mediaUriList.clear();
                                                    totalMediaUploaded = 0;
                                                    uploadDialog.dismiss();
                                                    finish();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(PostActivity.this, "Failed to upload media", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    });
                }
            });
        }
    }

    private void checkVideoPostTags(){

        if (!taggedUsers.isEmpty()) {
            for (int i = 0; i < taggedUsers.size(); i++) {
                videoReference.child(postID).child("taggedFriends").child(taggedUsers.get(i).getUserID()).setValue(true);
                sendNotifications.addTaggedUserNotification(taggedUsers.get(i), postID);
            }
            taggedUsers.clear();
        }

    }

    private void checkPostTags() {

        if (!taggedUsers.isEmpty()) {
            for (int i = 0; i < taggedUsers.size(); i++) {
                postRef.child(postID).child("taggedFriends").child(taggedUsers.get(i).getUserID()).setValue(true);
                sendNotifications.addTaggedUserNotification(taggedUsers.get(i), postID);
            }
            taggedUsers.clear();
        }

    }

    private void openCameraRecorder() {
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(videoIntent, VIDEO_CAMERA_REQUEST_CODE);
    }

    private void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    private void chooseImages() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryIntent.setAction(galleryIntent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture(s)"), PICK_IMAGE_INTENT);
    }

    private void getUserDetails() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUserID().equals(firebaseUser.getUid())){
                        try{
                            Picasso.get().load(user.getImageURL()).into(postActivityProPic);
                        }catch (NullPointerException ignored){}
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

        if (resultCode == RESULT_OK){
            if (requestCode == PICK_IMAGE_INTENT) {
                if (videoURI != null){
                    videoURI = null;

                    videoView.setVisibility(View.GONE);
                    videoCardView.setVisibility(View.GONE);
                }
                if (data.getClipData() == null) {
                    mediaUriList.add(data.getData().toString());
                } else {
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                }

                if (mediaUriList.isEmpty())
                    imageCardView.setVisibility(View.GONE);
                else
                    imageCardView.setVisibility(View.VISIBLE);

                mediaAdapter.notifyDataSetChanged();

            }else if (requestCode == PICK_VIDEO_REQUEST
                    && data != null && data.getData() != null){
                videoURI = data.getData();

                if (videoURI != null)
                    if(!mediaUriList.isEmpty()) {
                        mediaUriList.clear();
                        imageCardView.setVisibility(View.GONE);
                    }

                //video set to video view in the next activity
                videoArea.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.VISIBLE);
                videoCardView.setVisibility(View.VISIBLE);
                videoView.setVideoURI(videoURI);
                videoView.setOnPreparedListener(mp -> {

                    //scaling it to a correct size
                    float videoRatio = mp.getVideoWidth() / (float)mp.getVideoHeight();
                    float screenRatio = videoView.getWidth() / (float)videoView.getHeight();
                    float scale=videoRatio/screenRatio;

                    if(scale >=1f) videoView.setScaleX(scale);
                    else videoView.setScaleY(1f/scale);

                    mp.start();
                    mp.setLooping(true);
                });
            }else if (requestCode == VIDEO_CAMERA_REQUEST_CODE){
                videoURI = data.getData();

                videoArea.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.VISIBLE);
                videoCardView.setVisibility(View.VISIBLE);
                videoView.setVideoURI(videoURI);
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {

                        //scaling it to a correct size
                        float videoRatio = mp.getVideoWidth() / (float)mp.getVideoHeight();
                        float screenRatio = videoView.getWidth() / (float)videoView.getHeight();
                        float scale=videoRatio/screenRatio;

                        if(scale >=1f) videoView.setScaleX(scale);
                        else videoView.setScaleY(1f/scale);

                        mp.start();
                        mp.setLooping(true);
                    }
                });
            }
        }
        /*else if (requestCode == PICK_VIDEO_REQUEST && resultCode == -1
                && data != null & data.getData() != null){

            videoURI = data.getData();

            if (videoURI != null)
                if(!mediaList.isEmpty()) {
                    mediaList.clear();
                    imageCardView.setVisibility(View.GONE);
                }

            //video set to video view in the next activity
            videoArea.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.VISIBLE);
            videoCardView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(videoURI);
            videoView.setOnPreparedListener(mp -> {

                //scaling it to a correct size
                float videoRatio=mp.getVideoWidth()/(float)mp.getVideoHeight();
                float screenRatio=videoView.getWidth()/(float)videoView.getHeight();
                float scale=videoRatio/screenRatio;

                if(scale >=1f) videoView.setScaleX(scale);
                else videoView.setScaleY(1f/scale);

                mp.start();
                mp.setLooping(true);
            });

        }*/
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    } //below is the handler for recording voice status

}