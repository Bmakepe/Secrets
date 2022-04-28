package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.makepe.blackout.GettingStarted.Adapters.PostAdapter;
import com.makepe.blackout.GettingStarted.Models.Movement;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioRecorder;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.LocationServices;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MovementDetailsActivity extends AppCompatActivity {

    private ImageView movementCoverPic, movementPostImage, goToGalleryBTN;
    private CircleImageView movementProPic;
    private ProgressBar coverLoader, proPicLoader, postProgressBar;
    private TextView movementName, movementPurpose, followMovementBTN, sendMessageBTN, aboutTV,
            postCommentBTN, mediaCount, followersCount, locationTV, movementPrivacy, movementTimeCreated;
    private EditText movementCaptionET;
    private LinearLayout locationArea, movementButtonsArea;
    private FloatingActionButton storyBTN;
    private ExtendedFloatingActionButton scrollFAB;
    private TextInputLayout movementCaptionArea;

    private DatabaseReference movementReference, movementPostReference;
    private FirebaseUser firebaseUser;

    private String movementID, movementPostID;

    private Uri imageUri;
    private String myUri = "";
    private StorageTask uploadTask;
    private StorageReference storageReference, audioReference;
    private ProgressDialog uploadDialog;

    private ArrayList<PostModel> movementsList;
    private RecyclerView movementsRecycler;

    private boolean isAdmin = false;

    private GetTimeAgo getTimeAgo;
    private UniversalFunctions universalFunctions;
    private LocationServices locationServices;

    //for recording audio status
    private AudioRecorder audioRecorder;
    private LottieAnimationView lavPlaying;
    private ImageView voicePlayBTN, deleteAudioBTN;
    private TextView seekTimer;
    private RelativeLayout playAudioArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movement_details);

        Toolbar movementToolbar = findViewById(R.id.movementDetailsToolbar);
        setSupportActionBar(movementToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        movementCoverPic = findViewById(R.id.movementCoverImage);
        movementProPic = findViewById(R.id.movementProfilePicture);
        coverLoader = findViewById(R.id.movementCoverPicLoader);
        proPicLoader = findViewById(R.id.movementProPicLoader);
        movementName = findViewById(R.id.movementDetailsName);
        movementPurpose = findViewById(R.id.movementDescription);
        sendMessageBTN = findViewById(R.id.movementSendMessageBTN);
        postCommentBTN = findViewById(R.id.movementPostFAB);
        movementPostImage = findViewById(R.id.movementPostImage);
        goToGalleryBTN = findViewById(R.id.movementGoToGallery);
        movementsRecycler = findViewById(R.id.movementsRecycler);
        movementCaptionET = findViewById(R.id.movementCaptionET);
        mediaCount = findViewById(R.id.movementMediaCount);
        followersCount = findViewById(R.id.movement_followers_count);
        followMovementBTN = findViewById(R.id.followMovementBTN);
        postProgressBar = findViewById(R.id.movementPostProgressBar);
        locationArea = findViewById(R.id.movement_location_area);
        locationTV = findViewById(R.id.movementLocationCheckIn);
        movementButtonsArea = findViewById(R.id.movementButtonsArea);
        movementPrivacy = findViewById(R.id.movementPrivacy);
        movementTimeCreated = findViewById(R.id.movementTimeCreated);
        aboutTV = findViewById(R.id.movementAboutTV);
        storyBTN = findViewById(R.id.newMovementStoryBTN);
        movementCaptionArea = findViewById(R.id.movementCaptionArea);
        scrollFAB = findViewById(R.id.movementScrollToTheTopBTN);

        playAudioArea = findViewById(R.id.playAudioArea);
        voicePlayBTN = findViewById(R.id.post_playVoiceIcon);
        seekTimer = findViewById(R.id.seekTimer);
        lavPlaying = findViewById(R.id.lav_playing);
        deleteAudioBTN = findViewById(R.id.recordingDeleteBTN);

        Intent intent = getIntent();
        movementID = intent.getStringExtra("movementID");

        movementReference = FirebaseDatabase.getInstance().getReference("Movements");
        movementPostReference = FirebaseDatabase.getInstance().getReference("MovementPosts");
        storageReference = FirebaseStorage.getInstance().getReference("MovementPics");
        audioReference = FirebaseStorage.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        uploadDialog = new ProgressDialog(this);
        getTimeAgo = new GetTimeAgo();
        universalFunctions = new UniversalFunctions(this);
        locationServices = new LocationServices(locationTV, MovementDetailsActivity.this);
        audioRecorder = new AudioRecorder(lavPlaying, MovementDetailsActivity.this,
                MovementDetailsActivity.this, playAudioArea, voicePlayBTN, seekTimer);

        movementPostID = movementPostReference.push().getKey();

        movementsList = new ArrayList<>();
        movementsRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        movementsRecycler.setLayoutManager(layoutManager);

        getMovementsDetails();
        getMovementPosts();
        getMediaCount();
        isFollowing();
        getFollowersCount();
        universalFunctions.checkActiveStories(movementProPic, movementID);

        movementCaptionET.addTextChangedListener(new TextWatcher() {
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

        movementsRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                if (dy > 0) { // scrolling down
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollFAB.setVisibility(View.GONE);
                        }
                    }, 2000); // delay of 2 seconds before hiding the fab

                } else if (dy < 0) { // scrolling up

                    scrollFAB.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { // No scrolling
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollFAB.setVisibility(View.GONE);
                        }
                    }, 2000); // delay of 2 seconds before hiding the fab
                }

            }
        });

        scrollFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                movementsRecycler.smoothScrollToPosition(0);
            }
        });

        locationArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationServices.getMyLocation();
            }
        });

        sendMessageBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent messageIntent = new Intent(MovementDetailsActivity.this, ChatActivity.class);
                messageIntent.putExtra("userid", movementID);
                startActivity(messageIntent);
            }
        });

        postCommentBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (postCommentBTN.getText().toString().equals("Record")){

                    if (audioRecorder.checkRecordingPermission()){
                        movementCaptionArea.setVisibility(View.GONE);
                        if (!audioRecorder.isRecording()){
                            postCommentBTN.setText("Stop");
                            audioRecorder.startRecording();
                        }
                    }else{
                        audioRecorder.requestRecordingPermission();
                    }

                }else if (postCommentBTN.getText().toString().trim().equals("Stop")){
                    postCommentBTN.setText("Post");

                    audioRecorder.stopRecording();


                }else if (postCommentBTN.getText().toString().equals("Post")) {
                    uploadDialog.setMessage("Loading...");
                    uploadDialog.show();

                    if (!TextUtils.isEmpty(movementCaptionET.getText().toString())) {

                        postProgressBar.setVisibility(View.VISIBLE);
                        postCommentBTN.setVisibility(View.GONE);

                        if (imageUri != null) {
                            uploadImageMovement();
                        } else {
                            uploadMovementPost("textPost", "noImage");
                        }

                    }else if (audioRecorder.getRecordingFilePath() != null) {

                        uploadAudioPost();

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
                Toast.makeText(MovementDetailsActivity.this, "Recording will be deleted", Toast.LENGTH_SHORT).show();
            }
        });

        movementCoverPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent coverIntent = new Intent(MovementDetailsActivity.this, FullScreenImageActivity.class);
                coverIntent.putExtra("itemID", movementID);
                coverIntent.putExtra("reason", "movementCoverPic");
                startActivity(coverIntent);
            }
        });

        movementProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (movementProPic.getTag().equals("storyActive")){
                    AlertDialog alertDialog = new AlertDialog.Builder(MovementDetailsActivity.this).create();
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Picture",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent proPicIntent = new Intent(MovementDetailsActivity.this, FullScreenImageActivity.class);
                                    proPicIntent.putExtra("itemID", movementID);
                                    proPicIntent.putExtra("reason", "movementProPic");
                                    startActivity(proPicIntent);
                                    dialogInterface.dismiss();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "View Story",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(MovementDetailsActivity.this, StoryActivity.class);
                                    intent.putExtra("userid", movementID);
                                    startActivity(intent);
                                    dialogInterface.dismiss();
                                }
                            });
                    alertDialog.show();
                }else if(movementProPic.getTag().equals("noStories")){
                    Intent proPicIntent = new Intent(MovementDetailsActivity.this, FullScreenImageActivity.class);
                    proPicIntent.putExtra("itemID", movementID);
                    proPicIntent.putExtra("reason", "movementProPic");
                    startActivity(proPicIntent);
                }
            }
        });

        storyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MovementDetailsActivity.this, AddStoryActivity.class);
                intent.putExtra("userid", movementID);
                startActivity(intent);
            }
        });

        goToGalleryBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(MovementDetailsActivity.this);
            }
        });

        followMovementBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(followMovementBTN.getText().toString().equals("Follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(movementID).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(movementID)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                    //universalFunctions.addFollowNotifications(hisid);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(movementID).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(movementID)
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        findViewById(R.id.movementFollowerListBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent followersIntent = new Intent(MovementDetailsActivity.this, ConnectionsActivity.class);
                followersIntent.putExtra("UserID", movementID);
                followersIntent.putExtra("Interaction", "Followers");
                startActivity(followersIntent);
            }
        });

    }

    private void uploadAudioPost() {

        final StorageReference audioPath = audioReference.child("MovementAudioPosts").child(movementPostID + ".3gp");
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
                    String audioLink = audioDownloadLink.toString();

                    if (imageUri != null){

                        final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                                + "." + getFileExtension(imageUri));
                        uploadTask = fileReference.putFile(imageUri);
                        uploadTask.continueWithTask(new Continuation() {
                            @Override
                            public Object then(@NonNull Task task) throws Exception {
                                if(!task.isSuccessful())
                                    throw task.getException();
                                return fileReference.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){

                                    Uri imageDownloadLink = task.getResult();
                                    myUri = imageDownloadLink.toString();

                                    HashMap<String, Object> movementAudioImageMap = new HashMap<>();

                                    movementAudioImageMap.put("userID", firebaseUser.getUid());
                                    movementAudioImageMap.put("postID", movementPostID);
                                    movementAudioImageMap.put("postCaption", movementCaptionET.getText().toString());
                                    movementAudioImageMap.put("postImage", myUri);
                                    movementAudioImageMap.put("postTime", String.valueOf(System.currentTimeMillis()));
                                    movementAudioImageMap.put("postType", "audioImagePost");
                                    movementAudioImageMap.put("movementID", movementID);
                                    movementAudioImageMap.put("audioURL", audioLink);

                                    if (!locationTV.getText().toString().equals("No Location")) {
                                        movementAudioImageMap.put("latitude", locationServices.getLatitude());
                                        movementAudioImageMap.put("longitude", locationServices.getLongitude());
                                    }

                                    assert movementPostID != null;
                                    movementPostReference.child(movementPostID).setValue(movementAudioImageMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    movementPostImage.setImageURI(null);
                                                    movementPostImage.setVisibility(View.GONE);
                                                    postProgressBar.setVisibility(View.GONE);
                                                    postCommentBTN.setVisibility(View.VISIBLE);
                                                    postCommentBTN.setText("Record");
                                                    imageUri = null;

                                                    if (!TextUtils.isEmpty(locationTV.getText().toString())){
                                                        locationTV.setText("No Location");
                                                    }
                                                    uploadDialog.dismiss();
                                                    Toast.makeText(MovementDetailsActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                                }
                            }
                        });
                    }else{
                        HashMap<String, Object> movementAudioImageMap = new HashMap<>();

                        movementAudioImageMap.put("userID", firebaseUser.getUid());
                        movementAudioImageMap.put("postID", movementPostID);
                        movementAudioImageMap.put("postCaption", movementCaptionET.getText().toString());
                        movementAudioImageMap.put("postImage", "");
                        movementAudioImageMap.put("postTime", String.valueOf(System.currentTimeMillis()));
                        movementAudioImageMap.put("postType", "audioPost");
                        movementAudioImageMap.put("movementID", movementID);
                        movementAudioImageMap.put("audioURL", audioLink);

                        if (!locationTV.getText().toString().equals("No Location")) {
                            movementAudioImageMap.put("latitude", locationServices.getLatitude());
                            movementAudioImageMap.put("longitude", locationServices.getLongitude());
                        }

                        assert movementPostID != null;
                        movementPostReference.child(movementPostID).setValue(movementAudioImageMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        movementPostImage.setImageURI(null);
                                        movementPostImage.setVisibility(View.GONE);
                                        postProgressBar.setVisibility(View.GONE);
                                        postCommentBTN.setVisibility(View.VISIBLE);
                                        postCommentBTN.setText("Record");
                                        imageUri = null;

                                        if (!TextUtils.isEmpty(locationTV.getText().toString())){
                                            locationTV.setText("No Location");
                                        }
                                        uploadDialog.dismiss();
                                        Toast.makeText(MovementDetailsActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }

                }
            }
        });
    }

    private void uploadMovementPost(String postType, String postImage) {

        HashMap<String, Object> movementMap = new HashMap<>();

        movementMap.put("userID", firebaseUser.getUid());
        movementMap.put("postID", movementPostID);
        movementMap.put("postCaption", movementCaptionET.getText().toString());
        movementMap.put("postImage", postImage);
        movementMap.put("postTime", String.valueOf(System.currentTimeMillis()));
        movementMap.put("postType", postType);
        movementMap.put("movementID", movementID);

        if (!locationTV.getText().toString().equals("No Location")) {
            movementMap.put("latitude", locationServices.getLatitude());
            movementMap.put("longitude", locationServices.getLongitude());
        }

        assert movementPostID != null;
        movementPostReference.child(movementPostID).setValue(movementMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        uploadDialog.dismiss();
                        movementCaptionET.setText(null);
                        movementPostImage.setImageURI(null);
                        movementPostImage.setVisibility(View.GONE);
                        postProgressBar.setVisibility(View.GONE);
                        postCommentBTN.setVisibility(View.VISIBLE);
                        imageUri = null;

                        if (!TextUtils.isEmpty(locationTV.getText().toString())){
                            locationTV.setText("No Location");
                        }

                        Toast.makeText(MovementDetailsActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MovementDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void uploadImageMovement() {

        final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                + "." + getFileExtension(imageUri));

        uploadTask = fileReference.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    myUri = downloadUri.toString();

                    uploadMovementPost("imagePost", myUri);
                }
            }
        });


    }

    private void getFollowersCount() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(movementID).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followersCount.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isFollowing(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(movementID).exists()){
                    //executes if following user
                    followMovementBTN.setText("Following");
                }else{
                    followMovementBTN.setText("Follow");
                    //executes if you are not following the user
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getMediaCount() {
        movementPostReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel model = ds.getValue(PostModel.class);

                    assert model != null;
                    if (model.getMovementID().equals(movementID))
                        i++;

                }
                mediaCount.setText(i + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMovementPosts() {
        movementPostReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                movementsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    assert postModel != null;
                    if (postModel.getMovementID().equals(movementID))
                        movementsList.add(postModel);

                    //movementsRecycler.setAdapter(new MovementPostAdapter(MovementDetailsActivity.this, movementsList));
                    movementsRecycler.setAdapter(new PostAdapter(MovementDetailsActivity.this, movementsList));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMovementsDetails() {
        movementReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Movement movement = ds.getValue(Movement.class);

                    assert movement != null;
                    if (movement.getMovementID().equals(movementID)){
                        movementName.setText(movement.getMovementName());
                        movementPurpose.setText(movement.getMovementPurpose());
                        movementPrivacy.setText(movement.getMovementPrivacy() + " Movement");
                        aboutTV.setText("About " + movement.getMovementName());

                        movementTimeCreated.setText("Created: " + getTimeAgo.getTimeAgo(Long.parseLong(movement.getMovementTimestamp()), MovementDetailsActivity.this));

                        try{
                            Picasso.get().load(movement.getMovementProPic()).into(movementProPic);
                            Picasso.get().load(movement.getMovementCoverPic()).into(movementCoverPic);
                            coverLoader.setVisibility(View.GONE);
                            proPicLoader.setVisibility(View.GONE);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(movementProPic);
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(movementCoverPic);

                        }

                        if (firebaseUser.getUid().equals(movement.getMovementAdmin())){
                            storyBTN.setVisibility(View.VISIBLE);
                            movementButtonsArea.setVisibility(View.GONE);
                            isAdmin = true;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            movementPostImage.setImageURI(imageUri);
            movementPostImage.setVisibility(View.VISIBLE);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movement_details_menu, menu);

        if (!isAdmin){
            menu.findItem(R.id.editMovement).setVisible(false);
            menu.findItem(R.id.movementInbox).setVisible(false);
            menu.findItem(R.id.shutdownMovement).setVisible(false);
        }

        if (isAdmin){
            menu.findItem(R.id.reportMovement).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.editMovement:
                Toast.makeText(this, "New Movement Campaign", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MovementDetailsActivity.this, EditMovementActivity.class);
                intent.putExtra("movementID", movementID);
                startActivity(intent);
                break;
            case R.id.movementInbox:
                Intent chatIntent = new Intent(MovementDetailsActivity.this, MovementChatsActivity.class);
                chatIntent.putExtra("movementID", movementID);
                startActivity(chatIntent);
                break;
            case R.id.inviteFriends:
                Toast.makeText(this, "You will be able to invite friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.shutdownMovement:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Shutdown Movement")
                        .setMessage("Are you sure you want to shutdown the movement?")
                        .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ProgressDialog shutdownDialog = new ProgressDialog(MovementDetailsActivity.this);
                                shutdownDialog.setMessage("Please Wait... Loading...");
                                shutdownDialog.show();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();

                break;
            case R.id.reportMovement:
                Intent intent1  = new Intent(MovementDetailsActivity.this, ReportActivity.class);
                intent1.putExtra("reported", movementID);
                startActivity(intent1);
                break;

            default:
                Toast.makeText(this, "unknown menu selection", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}