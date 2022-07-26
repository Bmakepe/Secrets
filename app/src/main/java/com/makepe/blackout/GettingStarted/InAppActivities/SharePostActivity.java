package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.makepe.blackout.GettingStarted.Adapters.PostImageAdapter;
import com.makepe.blackout.GettingStarted.Adapters.TaggedFriendsAdapter;
import com.makepe.blackout.GettingStarted.Adapters.UserAdapter;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.Notifications.SendNotifications;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioPlayer;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioRecorder;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.LocationServices;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SharePostActivity extends AppCompatActivity{

    //for normal post views
    private CircleImageView myProfilePic;
    private EditText myCaptionET;
    private Toolbar toolbar;
    private Switch commentSwitch;

    //for shared post views
    private CircleImageView hisProfilePic;
    private TextView hisNameTV, hisLocationTV, hisCaptionTV, hisPostDate, sharedPostImagesCounter, hisTaggedUsers;
    private RelativeLayout picArea;
    private LinearLayout hisLocationArea;
    private ProgressBar sharedImageLoader;
    private RecyclerView sharedImagePostRecycler;

    //for other views
    private TextView shareBTN, tagFriendsBTN;
    private RelativeLayout privacyBTN;
    private LinearLayout myLocationArea;
    private TextInputLayout postCaptionArea;
    private RecyclerView taggedFriendsRecycler;

    //for video sharing
    private VideoView videoView;
    private RelativeLayout videoArea;
    private ProgressBar sharedVideoLoader;

    private DatabaseReference postRef, userRef, followingReference, videoReference;
    private FirebaseUser firebaseUser;
    private StorageReference audioReference;

    private String sharePostID, postType, postID;

    private GetTimeAgo getTimeAgo;
    private UniversalFunctions universalFunctions;
    private ArrayList<User> taggedUsers = new ArrayList<>();
    private TaggedFriendsAdapter taggedFriendsAdapter;

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
    private PostModel post;

    //for Location services & privacy settings
    private LocationServices locationServices;
    private TextView myLocationCheckIn, audiencePicker;
    private String privacyProtection = "Public";

    private SendNotifications sendNotifications;

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
        commentSwitch = findViewById(R.id.commentSwitch);

        //for shared post views
        hisProfilePic = findViewById(R.id.share_image_user);
        hisNameTV = findViewById(R.id.share_username);
        hisLocationTV = findViewById(R.id.share_postCheckIn);
        hisCaptionTV = findViewById(R.id.share_post_desc);
        hisPostDate = findViewById(R.id.share_postDate);
        hisTaggedUsers = findViewById(R.id.shared_taggedPeopleList);
        picArea = findViewById(R.id.share_postPicArea);
        hisLocationArea = findViewById(R.id.shared_postLocationArea);
        sharedImageLoader = findViewById(R.id.progress_load_media);
        sharedImagePostRecycler = findViewById(R.id.sharedImagePostRecycler);
        sharedPostImagesCounter = findViewById(R.id.sharedPostImagesCounter);

        //for other views
        shareBTN = findViewById(R.id.postFAB);
        myLocationArea = findViewById(R.id.myLocationArea);
        privacyBTN = findViewById(R.id.privacySettingsBTN);
        myLocationCheckIn = findViewById(R.id.locationCheckIn);
        audiencePicker = findViewById(R.id.audience_picker);
        postCaptionArea = findViewById(R.id.textMessage);
        tagFriendsBTN = findViewById(R.id.tagFriendsBTN);
        taggedFriendsRecycler = findViewById(R.id.taggedFriendsRecycler);

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

        sendNotifications = new SendNotifications(this);
        getTimeAgo = new GetTimeAgo();
        uploadProgress = new ProgressDialog(this);
        uploadProgress.setMessage("Loading... Please wait...");
        universalFunctions = new UniversalFunctions(SharePostActivity.this);
        locationServices = new LocationServices(myLocationCheckIn, SharePostActivity.this);
        audioRecorder = new AudioRecorder(lavPlaying, SharePostActivity.this,
                SharePostActivity.this, playAudioArea, voicePlayBTN, seekTimer);

        audioPlayer = new AudioPlayer(SharePostActivity.this, playBTN,
                audioSeekTimer, postTotalTime, audioAnimation);

        postRef = FirebaseDatabase.getInstance().getReference("SecretPosts");
        videoReference = FirebaseDatabase.getInstance().getReference("SecretVideos");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        audioReference = FirebaseStorage.getInstance().getReference();
        followingReference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid()).child("following");

        taggedFriendsRecycler.hasFixedSize();
        taggedFriendsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        taggedFriendsAdapter = new TaggedFriendsAdapter(SharePostActivity.this, taggedUsers);
        taggedFriendsRecycler.setAdapter(taggedFriendsAdapter);

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
                                uploadSharedPost("sharedTextVideoPost");
                                break;

                            case "imagePost":
                                uploadSharedPost("sharedTextImagePost");
                                break;

                            case "audioPost":
                                uploadSharedPost("sharedTextAudioPost");
                                break;

                            case "audioImagePost":
                                uploadSharedPost("sharedTextAudioImagePost");
                                break;

                            case "audioVideoPost":
                                uploadSharedPost("sharedTextAudioVideoPost");

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

                            case "audioVideoPost":
                                uploadSharedAudioPost("sharedAudioAudioVideoPost");
                                break;

                            default:
                                Toast.makeText(SharePostActivity.this, "Unknown post type being shared: " + postType, Toast.LENGTH_SHORT).show();
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

        tagFriendsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
            }
        });

        hisTaggedUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTagsBottomSheetDialog();
            }
        });

    }

    private void showTagsBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SharePostActivity.this);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(SharePostActivity.this));
        friendsRecycler.setNestedScrollingEnabled(true);
        UserAdapter userAdapter = new UserAdapter(SharePostActivity.this, idList, "goToProfile");
        friendsRecycler.setAdapter(userAdapter);

        postRef.child(postID).child("taggedFriends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
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

        bottomSheetDialog.show();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void showBottomSheetDialog() {
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
        FriendsAdapter friendsAdapter = new FriendsAdapter(userList, SharePostActivity.this);
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

    private void getSharedPostDescription() {
        postRef.child(sharePostID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    post = snapshot.getValue(PostModel.class);

                    assert post != null;
                    if (post.getPostID().equals(sharePostID)){

                        postType = post.getPostType();

                        getUniversalPostDetails(post);
                        getUserDetails(post);
                        checkPostTags(post);

                        switch (post.getPostType()){

                            case "audioPost":
                                hisCaptionTV.setVisibility(View.GONE);
                                mediaPlayerArea.setVisibility(View.VISIBLE);

                                mediaPlayer(post);
                                break;

                            case "audioImagePost":
                                hisCaptionTV.setVisibility(View.GONE);
                                mediaPlayerArea.setVisibility(View.VISIBLE);

                                mediaPlayer(post);
                                getPostImages(post);

                                break;

                            case "textPost":
                                hisCaptionTV.setVisibility(View.VISIBLE);
                                hisCaptionTV.setText(post.getPostCaption());
                                picArea.setVisibility(View.GONE);

                                break;

                            case "imagePost":
                                hisCaptionTV.setVisibility(View.VISIBLE);
                                hisCaptionTV.setText(post.getPostCaption());

                                getPostImages(post);

                                break;

                            default:
                                Toast.makeText(SharePostActivity.this, "Unidentified Post Type: " + post.getPostType(), Toast.LENGTH_SHORT).show();
                        }
                    }

                }else{
                    videoReference.child(sharePostID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                post = snapshot.getValue(PostModel.class);

                                assert post != null;
                                if (post.getPostID().equals(sharePostID)){

                                    postType = post.getPostType();

                                    getUniversalPostDetails(post);
                                    getUserDetails(post);
                                    checkVideoPostTags(post);

                                    switch (post.getPostType()){

                                        case "videoPost":
                                            hisCaptionTV.setVisibility(View.VISIBLE);
                                            hisCaptionTV.setText(post.getPostCaption());

                                            getVideoDetails(post);
                                            break;

                                        case "audioVideoPost":
                                            hisCaptionTV.setVisibility(View.GONE);
                                            mediaPlayerArea.setVisibility(View.VISIBLE);

                                            getVideoDetails(post);
                                            mediaPlayer(post);
                                            break;

                                        default:
                                            Toast.makeText(SharePostActivity.this, "Unknown post type identified: " + post.getPostType(), Toast.LENGTH_SHORT).show();

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

    private void checkVideoPostTags(PostModel post) {
        videoReference.child(post.getPostID()).child("taggedFriends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (snapshot.getChildrenCount() == 1)
                                hisTaggedUsers.setText("with: " + snapshot.getChildrenCount() + " friend");
                            else
                                hisTaggedUsers.setText("with: " + snapshot.getChildrenCount() + " friends");

                            hisTaggedUsers.setVisibility(View.VISIBLE);
                        }else{
                            hisTaggedUsers.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkPostTags(PostModel post) {
        postRef.child(post.getPostID()).child("taggedFriends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (snapshot.getChildrenCount() == 1)
                        hisTaggedUsers.setText("with: " + snapshot.getChildrenCount() + " friend");
                    else
                        hisTaggedUsers.setText("with: " + snapshot.getChildrenCount() + " friends");

                    hisTaggedUsers.setVisibility(View.VISIBLE);
                }else{
                    hisTaggedUsers.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getVideoDetails(PostModel post) {
        videoArea.setVisibility(View.VISIBLE);
        videoView.setVideoURI(Uri.parse(post.getVideoURL()));

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
    }

    private void getPostImages(PostModel post) {
        ArrayList<String> imageList = new ArrayList<>();

        sharedImagePostRecycler.hasFixedSize();
        sharedImagePostRecycler.setLayoutManager(new LinearLayoutManager(SharePostActivity.this, LinearLayoutManager.HORIZONTAL, false));

        postRef.child(post.getPostID()).child("images").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    imageList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        imageList.add(ds.getValue().toString());
                    }
                    sharedImagePostRecycler.setAdapter(new PostImageAdapter(SharePostActivity.this, imageList));
                    sharedPostImagesCounter.setText("" + snapshot.getChildrenCount());
                    picArea.setVisibility(View.VISIBLE);
                    sharedImageLoader.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserDetails(PostModel postModel) {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUserID().equals(postModel.getUserID())){
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

    private void getUniversalPostDetails(PostModel post) {

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
                    if (user.getUserID().equals(firebaseUser.getUid())){
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
                    sharedMap.put("postTime", String.valueOf(System.currentTimeMillis()));
                    sharedMap.put("postPrivacy", privacyProtection);//post security
                    sharedMap.put("postType", postType);//post security
                    sharedMap.put("sharedPost", sharePostID);
                    sharedMap.put("audioURL", audioLink);

                    if (!myLocationCheckIn.getText().toString().equals("No Location")) {
                        sharedMap.put("latitude", locationServices.getLatitude());
                        sharedMap.put("longitude", locationServices.getLongitude());
                    }

                    if (commentSwitch.isChecked())
                        sharedMap.put("commentsAllowed", false);
                    else
                        sharedMap.put("commentsAllowed", true);

                    postRef.child(postID).setValue(sharedMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    checkSharedPostTags();

                                    postRef.child(sharePostID).child("sharedBy").child(firebaseUser.getUid()).setValue(true)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                    Toast.makeText(SharePostActivity.this, "Shared Successfully", Toast.LENGTH_SHORT).show();
                                                    uploadProgress.dismiss();

                                                    sendNotifications.addPostShareNotification(post, myCaptionET.getText().toString());

                                                    finish();
                                                }
                                            });
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
        sharedMap.put("postTime", String.valueOf(System.currentTimeMillis()));
        sharedMap.put("postPrivacy", privacyProtection);//post security
        sharedMap.put("postType", sharedTextPost);//post security
        sharedMap.put("sharedPost", sharePostID);

        if (!myLocationCheckIn.getText().toString().equals("No Location")) {
            sharedMap.put("latitude", locationServices.getLatitude());
            sharedMap.put("longitude", locationServices.getLongitude());
        }

        if (commentSwitch.isChecked())
            sharedMap.put("commentsAllowed", false);
        else
            sharedMap.put("commentsAllowed", true);

        postRef.child(postID).setValue(sharedMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        checkSharedPostTags();

                        postRef.child(sharePostID).child("sharedBy").child(firebaseUser.getUid()).setValue(true)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                Toast.makeText(SharePostActivity.this, "Shared Successfully", Toast.LENGTH_SHORT).show();
                                                uploadProgress.dismiss();

                                                sendNotifications.addPostShareNotification(post, myCaptionET.getText().toString());
                                                myCaptionET.setText("");

                                                finish();
                                            }
                                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SharePostActivity.this, "Sharing Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkSharedPostTags() {
        if (!taggedUsers.isEmpty()) {
            for (int i = 0; i < taggedUsers.size(); i++) {
                postRef.child(postID).child("taggedFriends").child(taggedUsers.get(i).getUserID()).setValue(true);
                sendNotifications.addTaggedUserNotification(taggedUsers.get(i), postID);
            }
            taggedUsers.clear();
        }
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