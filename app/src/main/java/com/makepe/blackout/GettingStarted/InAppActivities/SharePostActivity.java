package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioPlayer;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioRecorder;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.LocationServices;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SharePostActivity extends AppCompatActivity{

    //for normal post views
    private CircleImageView myProfilePic;
    private EditText myCaptionET;
    private Toolbar toolbar;

    //for shared post views
    private CircleImageView hisProfilePic;
    private TextView hisNameTV, hisLocationTV, hisCaptionTV, hisPostDate, postDurationTV;
    private ImageView hisPostImage;
    private RelativeLayout picArea, postDurationArea;
    private LinearLayout hisLocationArea;
    private ProgressBar sharedImageLoader;

    //for other views
    private TextView shareBTN;
    private RelativeLayout privacyBTN;
    private LinearLayout myLocationArea;
    private TextInputLayout postCaptionArea;

    //for video sharing
    private VideoView videoView;
    private RelativeLayout videoArea;
    private ProgressBar sharedVideoLoader;

    private DatabaseReference postRef, userRef, notificationsRef;
    private FirebaseUser firebaseUser;
    private StorageReference audioReference;

    private String sharePostID, userID, postType, postID;

    private GetTimeAgo getTimeAgo;
    private UniversalFunctions universalFunctions;

    //for audio recorder
    private AudioRecorder audioRecorder;
    private LottieAnimationView lavPlaying;
    private ImageView voicePlayBTN, deleteAudioBTN;
    private TextView seekTimer;
    private RelativeLayout playAudioArea;

    //for shared post media player
    private RelativeLayout mediaPlayerArea;
    private AudioPlayer audioPlayer;
    public CircleImageView playBTN;
    public LottieAnimationView audioAnimation;
    public TextView audioSeekTimer, postTotalTime;

    private ProgressDialog uploadProgress;

    //for Location services & privacy settings
    private LocationServices locationServices;
    private TextView myLocationCheckIn, audiencePicker;
    private String privacyProtection = "Public";

    private int postTimeDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_post);

        toolbar = findViewById(R.id.sharingToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //for normal post views
        myProfilePic = findViewById(R.id.share_userProPic);
        myCaptionET = findViewById(R.id.shareCaption);
        postDurationArea = findViewById(R.id.postDurationArea);
        postDurationTV = findViewById(R.id.postDurationTV);

        //for shared post views
        hisProfilePic = findViewById(R.id.share_image_user);
        hisNameTV = findViewById(R.id.share_username);
        hisLocationTV = findViewById(R.id.share_postCheckIn);
        hisCaptionTV = findViewById(R.id.share_post_desc);
        hisPostDate = findViewById(R.id.share_postDate);
        hisPostImage = findViewById(R.id.share_postImage);
        picArea = findViewById(R.id.share_postPicArea);
        hisLocationArea = findViewById(R.id.shared_postLocationArea);
        sharedImageLoader = findViewById(R.id.progress_load_media);

        //for other views
        shareBTN = findViewById(R.id.postFAB);
        myLocationArea = findViewById(R.id.myLocationArea);
        privacyBTN = findViewById(R.id.privacySettingsBTN);
        myLocationCheckIn = findViewById(R.id.locationCheckIn);
        audiencePicker = findViewById(R.id.audience_picker);
        postCaptionArea = findViewById(R.id.textMessage);

        //for video views
        videoView = findViewById(R.id.sharePreviewCameraSelectedVideo);
        videoArea = findViewById(R.id.shareVideoCardView);
        sharedVideoLoader = findViewById(R.id.sharedVideoLoader);

        //for audio recorder
        playAudioArea = findViewById(R.id.playAudioArea);
        voicePlayBTN = findViewById(R.id.post_playVoiceIcon);
        seekTimer = findViewById(R.id.seekTimer);
        lavPlaying = findViewById(R.id.lav_playing);

        //for shared audio posts
        mediaPlayerArea = findViewById(R.id.shared_audio_media_player);
        playBTN = findViewById(R.id.postItem_playVoiceIcon);
        audioAnimation = findViewById(R.id.postItem_lav_playing);
        seekTimer = findViewById(R.id.postItemSeekTimer);
        postTotalTime = findViewById(R.id.postTotalTime);

        Intent intent = getIntent();
        sharePostID = intent.getStringExtra("postID");
        getTimeAgo = new GetTimeAgo();
        uploadProgress = new ProgressDialog(this);
        universalFunctions = new UniversalFunctions(SharePostActivity.this);
        locationServices = new LocationServices(myLocationCheckIn, SharePostActivity.this);
        audioRecorder = new AudioRecorder(lavPlaying, SharePostActivity.this,
                SharePostActivity.this, playAudioArea, voicePlayBTN, seekTimer);

        audioPlayer = new AudioPlayer(SharePostActivity.this, playBTN,
                audioSeekTimer, postTotalTime, audioAnimation);

        postRef = FirebaseDatabase.getInstance().getReference("Posts");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        notificationsRef = FirebaseDatabase.getInstance().getReference("Notifications");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        audioReference = FirebaseStorage.getInstance().getReference();

        postID = postRef.push().getKey();

        getMyDetails();
        getSharedPostDescription();

        myCaptionET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0)
                    shareBTN.setText("Post");
                else
                    shareBTN.setText("Record");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        hisPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SharePostActivity.this, FullScreenImageActivity.class);
                intent.putExtra("itemID", sharePostID);
                intent.putExtra("reason", "postImage");
                startActivity(intent);
            }
        });

        myLocationArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationServices.getMyLocation();
            }
        });

        privacyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(SharePostActivity.this, privacyBTN, Gravity.END);
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
                                Toast.makeText(SharePostActivity.this, "Unknown Selection", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        postDurationArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(SharePostActivity.this, postDurationTV, Gravity.END);
                popupMenu.getMenu().add(Menu.NONE, 0, 0, "Default");
                popupMenu.getMenu().add(Menu.NONE, 1, 0, "1 Hour");
                popupMenu.getMenu().add(Menu.NONE, 2, 0, "6 Hours");
                popupMenu.getMenu().add(Menu.NONE, 3, 0, "12 Hours");
                popupMenu.getMenu().add(Menu.NONE, 4, 0, "24 Hours");
                popupMenu.getMenu().add(Menu.NONE, 5, 0, "3 Days");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()){
                            case 0:
                                postDurationTV.setText("Default");
                                break;

                            case 1:
                                postDurationTV.setText("1 Hour");
                                break;

                            case 2:
                                postDurationTV.setText("6 Hours");
                                break;

                            case 3:
                                postDurationTV.setText("12 Hours");
                                break;

                            case 4:
                                postDurationTV.setText("24 Hours");
                                break;

                            case 5:
                                postDurationTV.setText("3 Days");
                                break;

                            default:
                                Toast.makeText(SharePostActivity.this, "Unknown Selection", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                });
                popupMenu.show();

            }
        });

        shareBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (shareBTN.getText().toString().equals("Record")){
                    if(audioRecorder.checkRecordingPermission()){
                        postCaptionArea.setVisibility(View.GONE);
                        if (!audioRecorder.isRecording()){
                            shareBTN.setText("Stop");
                            audioRecorder.startRecording();
                        }
                    }else{
                        audioRecorder.requestRecordingPermission();
                    }

                }else if (shareBTN.getText().toString().equals("Stop")){

                    shareBTN.setText("Post");
                    audioRecorder.stopRecording();

                }else if (shareBTN.getText().toString().equals("Post")){

                    if (!TextUtils.isEmpty(myCaptionET.getText().toString().trim())){
                        switch (postType) {
                            case "textPost":
                                uploadSharedPost("sharedTextPost");
                                break;

                            case "videoPost":
                                uploadSharedPost("sharedVideoPost");
                                break;

                            case "imagePost":
                                uploadSharedPost("sharedImagePost");
                                break;

                            case "audioPost":
                                uploadSharedPost("sharedTextAudioPost");
                                break;

                            case "audioImagePost":
                                uploadSharedPost("sharedTextAudioImagePost");
                                break;

                            default:
                                Toast.makeText(SharePostActivity.this, "Unknown post type being shared", Toast.LENGTH_SHORT).show();
                        }
                    }else if (audioRecorder.getRecordingFilePath() != null){
                        switch (postType) {
                            case "textPost":
                                uploadSharedAudioPost("sharedAudioTextPost");
                                break;

                            case "videoPost":
                                uploadSharedAudioPost("sharedAudioVideoPost");
                                break;

                            case "imagePost":
                                uploadSharedAudioPost("sharedAudioImagePost");
                                break;

                            case "audioPost":
                                uploadSharedAudioPost("sharedAudioAudioPost");
                                break;

                            case "audioImagePost":
                                uploadSharedAudioPost("sharedAudioAudioImagePost");
                                break;

                            default:
                                Toast.makeText(SharePostActivity.this, "Unknown post type being shared", Toast.LENGTH_SHORT).show();
                        }
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

    }

    private void getSharedPostDescription() {
        postRef.child(sharePostID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel postModel = snapshot.getValue(PostModel.class);

                    if (postModel.getPostID().equals(sharePostID)){
                        getPostDetails(postModel);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPostDetails(PostModel postModel) {
        postType = postModel.getPostType();
        userID = postModel.getUserID();

        switch (postModel.getPostType()){
            case "imagePost":
                picArea.setVisibility(View.VISIBLE);
                hisPostImage.setVisibility(View.VISIBLE);
                try{
                    Picasso.get().load(postModel.getPostImage()).into(hisPostImage);
                    sharedImageLoader.setVisibility(View.GONE);
                }catch (NullPointerException ignored){}
                getUniversalPostDetails(postModel);
                getUserDetails(postModel);

                break;

            case "textPost":
                picArea.setVisibility(View.GONE);
                getUniversalPostDetails(postModel);
                getUserDetails(postModel);
                break;

            case "videoPost":
                try{
                    getUserDetails(postModel);
                    videoArea.setVisibility(View.VISIBLE);
                    videoView.setVideoURI(Uri.parse(postModel.getVideoURL()));

                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.start();
                            sharedVideoLoader.setVisibility(View.GONE);
                            mediaPlayer.setLooping(true);
                            mediaPlayer.setVolume(100f, 100f);

                            float videoRatio = mediaPlayer.getVideoWidth() / (float)mediaPlayer.getVideoHeight();
                            float screenRatio = videoView.getWidth() / (float)videoView.getHeight();
                            float scale  = videoRatio / screenRatio;
                            if (scale >= 1f){
                                videoView.setScaleX(scale);
                            }else {
                                videoView.setScaleY(1f / scale);
                            }
                        }
                    });

                    videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mediaPlayer.start();
                        }
                    });
                }catch (NullPointerException ignored){}
                break;

            case "audioPost":
                mediaPlayerArea.setVisibility(View.VISIBLE);
                hisCaptionTV.setVisibility(View.GONE);

                getUserDetails(postModel);
                mediaPlayer(postModel);
                break;

            case "audioImagePost":
                mediaPlayerArea.setVisibility(View.VISIBLE);
                hisCaptionTV.setVisibility(View.GONE);
                picArea.setVisibility(View.VISIBLE);
                hisPostImage.setVisibility(View.VISIBLE);

                getUserDetails(postModel);
                mediaPlayer(postModel);

                try{
                    Picasso.get().load(postModel.getPostImage()).into(hisPostImage);
                    sharedImageLoader.setVisibility(View.GONE);
                }catch (NullPointerException ignored){}
                getUniversalPostDetails(postModel);

                break;

            default:
                Toast.makeText(this, "Unknown sharing error", Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserDetails(PostModel postModel) {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUSER_ID().equals(postModel.getUserID())){
                        try{
                            Picasso.get().load(user.getImageURL()).into(hisProfilePic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(hisProfilePic);
                        }

                        hisNameTV.setText(user.getUsername());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void mediaPlayer(PostModel postModel) {

        playBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!audioPlayer.isPlaying()){
                    audioPlayer.startPlayingAudio(postModel.getAudioURL());
                }else{
                    audioPlayer.stopPlayingAudio();
                }
            }
        });

    }

    private void uploadSharedAudioPost(String postType) {
        uploadProgress.show();

        StorageReference filePath = audioReference.child("AudioPosts").child(postID + ".3gp");
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

                    HashMap<String, Object> sharedMap = new HashMap<>();

                    sharedMap.put("userID", firebaseUser.getUid());//usersID
                    sharedMap.put("postID", postID);
                    sharedMap.put("postCaption", myCaptionET.getText().toString());
                    sharedMap.put("postImage", "noImage");
                    sharedMap.put("postTime", String.valueOf(System.currentTimeMillis()));
                    sharedMap.put("postPrivacy", privacyProtection);//post security
                    sharedMap.put("postType", postType);//post security
                    sharedMap.put("videoURL", "noVideo");//post security
                    sharedMap.put("sharedPost", sharePostID);
                    sharedMap.put("audioURL", audioLink);

                    if (!myLocationCheckIn.getText().toString().equals("No Location")) {
                        sharedMap.put("latitude", locationServices.getLatitude());
                        sharedMap.put("longitude", locationServices.getLongitude());
                    }

                    postRef.child(postID).setValue(sharedMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    uploadProgress.dismiss();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SharePostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void uploadSharedPost(String sharedTextPost) {
        uploadProgress.show();

        HashMap<String, Object> sharedMap = new HashMap<>();

        sharedMap.put("userID", firebaseUser.getUid());//usersID
        sharedMap.put("postID", postID);
        sharedMap.put("postCaption", myCaptionET.getText().toString());
        sharedMap.put("postImage", "noImage");
        sharedMap.put("postTime", String.valueOf(System.currentTimeMillis()));
        sharedMap.put("postPrivacy", privacyProtection);//post security
        sharedMap.put("postType", sharedTextPost);//post security
        sharedMap.put("videoURL", "noVideo");//post security
        sharedMap.put("sharedPost", sharePostID);

        if (!myLocationCheckIn.getText().toString().equals("No Location")) {
            sharedMap.put("latitude", locationServices.getLatitude());
            sharedMap.put("longitude", locationServices.getLongitude());
        }

        postRef.child(postID).setValue(sharedMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SharePostActivity.this, "Shared Successfully", Toast.LENGTH_SHORT).show();
                        uploadProgress.dismiss();

                        sendShareNotification(myCaptionET.getText().toString());
                        myCaptionET.setText("");

                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SharePostActivity.this, "Sharing Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendShareNotification(String caption) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String notificationID = notificationsRef.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("notificationID", notificationID);
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "Shared your post: " + caption);
        hashMap.put("postid", sharePostID);
        hashMap.put("ispost", true);
        hashMap.put("timeStamp", timeStamp);

        assert notificationID != null;
        notificationsRef.child(userID).child(notificationID).setValue(hashMap);
    }

    private void getUniversalPostDetails(PostModel post) {

        if (post.getPostCaption().equals("")){
            hisCaptionTV.setVisibility(View.GONE);
        }else{
            hisCaptionTV.setVisibility(View.VISIBLE);
            hisCaptionTV.setText(post.getPostCaption());
        }

        try{
            universalFunctions.findAddress(post.getLatitude(), post.getLongitude(), hisLocationTV, myLocationArea);
        }catch (Exception ignored){}

        try{
            hisPostDate.setText(getTimeAgo.getTimeAgo(Long.parseLong(post.getPostTime()), SharePostActivity.this));
        }catch (NumberFormatException ignored){}


    }

    private void getMyDetails() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUSER_ID().equals(firebaseUser.getUid())){
                        try{
                            Picasso.get().load(user.getImageURL()).into(myProfilePic);
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_post_menu, menu);

        menu.findItem(R.id.uploadImageItems).setVisible(false);
        menu.findItem(R.id.uploadVideosItem).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.tagFriendsItem:
                Toast.makeText(this, "You will be able to tag friends", Toast.LENGTH_SHORT).show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

}