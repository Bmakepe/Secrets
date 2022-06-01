package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.makepe.blackout.GettingStarted.OtherClasses.AudioRecorder;
import com.makepe.blackout.GettingStarted.OtherClasses.LocationServices;
import com.makepe.blackout.GettingStarted.RegisterActivity;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity{

    private CircleImageView postActivityProPic;

    private FirebaseUser firebaseUser;
    private DatabaseReference userRef, postRef;
    private StorageReference storageReference, audioReference;

    private ImageView picToUpload, editImageBTN;
    private TextView postBTN, postDurationTV;
    private EditText captionArea;
    private String name, uid, caption, postID, timeStamp;
    private CardView imageCardView, videoCardView;
    private TextInputLayout postCaptionArea;
    private RelativeLayout postDurationArea;
    private Switch commentSwitch;

    private ProgressBar postProgress;
    private ProgressDialog uploadDialog;

    private Dialog friendListDialog, searchDialog;
    private Toolbar postToolbar;

    private Uri imageUri;
    private String myUri = "";
    private StorageTask uploadTask;

    public static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    public static final int STORAGE_REQUEST_CODE = 100;
    public static final int IMAGE_PICK_GALLERY_CODE = 200;
    private static final int PICK_VIDEO_REQUEST = 300;

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

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postToolbar = findViewById(R.id.postToolbar);
        setSupportActionBar(postToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postActivityProPic = findViewById(R.id.addPostProPic);
        postBTN = findViewById(R.id.postFAB);
        postProgress = findViewById(R.id.postProgress);
        tagLocation = findViewById(R.id.locationCheckIn);
        myLocationAreaBTN = findViewById(R.id.postLocationBTN);
        picToUpload = findViewById(R.id.picToUpload);
        captionArea = findViewById(R.id.addPostArea);
        imageCardView = findViewById(R.id.imageCardView);
        videoView = findViewById(R.id.previewCameraSelectedVideo);
        videoArea = findViewById(R.id.videoArea);
        videoCardView = findViewById(R.id.videoCardView);
        privacyBTN = findViewById(R.id.privacySettingsBTN);
        audiencePicker = findViewById(R.id.audience_picker);
        postCaptionArea = findViewById(R.id.postCaptionArea);
        deleteAudioBTN = findViewById(R.id.recordingDeleteBTN);
        commentSwitch = findViewById(R.id.commentSwitch);
        postDurationArea = findViewById(R.id.postDurationArea);
        postDurationTV = findViewById(R.id.postDurationTV);
        editImageBTN = findViewById(R.id.editImageBTN);

        playAudioArea = findViewById(R.id.playAudioArea);
        voicePlayBTN = findViewById(R.id.post_playVoiceIcon);
        seekTimer = findViewById(R.id.seekTimer);
        lavPlaying = findViewById(R.id.lav_playing);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        postRef = FirebaseDatabase.getInstance().getReference("Posts");
        storageReference = FirebaseStorage.getInstance().getReference("ImagePosts");
        audioReference = FirebaseStorage.getInstance().getReference();

        uploadDialog = new ProgressDialog(this);
        locationServices = new LocationServices(tagLocation, PostActivity.this);
        audioRecorder = new AudioRecorder(lavPlaying, PostActivity.this,
                PostActivity.this, playAudioArea, voicePlayBTN, seekTimer);

        postID = postRef.push().getKey();
        timeStamp = String.valueOf(System.currentTimeMillis());

        getUserDetails();
        checkUserStatus();
        iniFriendList();
        iniSearchDialog();

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

        postBTN.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {

                if (postBTN.getText().toString().trim().equals("Record")){

                    if(audioRecorder.checkRecordingPermission()){
                        postCaptionArea.setVisibility(View.GONE);
                        if (!audioRecorder.isRecording()){
                            postBTN.setText("Stop");
                            audioRecorder.startRecording();
                        }
                    }else{
                        audioRecorder.requestRecordingPermission();
                    }

                }else if (postBTN.getText().toString().trim().equals("Stop")){

                    postBTN.setText("Post");
                    audioRecorder.stopRecording();

                }else if (postBTN.getText().toString().trim().equals("Post")){
                    //Button for posting to the database
                    caption = captionArea.getText().toString().trim();
                    uploadDialog.setMessage("Loading...");

                    if (!TextUtils.isEmpty(captionArea.getText().toString().trim())){
                        uploadDialog.show();
                        if (imageUri != null)
                            uploadImagePost();
                        else if (videoURI != null)
                            uploadVideoPost();
                        else
                            uploadTextPost();

                    }else if (audioRecorder.getRecordingFilePath() != null){
                        if (imageUri != null)
                            uploadAudioImagePost();
                        else if (videoURI != null)
                            uploadVideoAudioPost();
                        else
                            uploadAudioPost();
                    }

                }
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
                popupMenu.getMenu().add(Menu.NONE, 2, 0, "For Me");

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

                            case 2:
                                privacyProtection = "For Me";
                                audiencePicker.setText("For Me");
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

        postDurationArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(PostActivity.this, postDurationTV, Gravity.END);
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
                Toast.makeText(PostActivity.this, "Recording will be deleted", Toast.LENGTH_SHORT).show();
            }
        });

        editImageBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editImageIntent = new Intent(PostActivity.this, EditImageActivity.class);
                //editImageIntent.putExtra("imageUri", imageUri);
                editImageIntent.setData(imageUri);
                startActivity(editImageIntent);
            }
        });

    }

    private void uploadVideoAudioPost() {
        uploadDialog.show();

        long systemMillis = System.currentTimeMillis();

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("videos")
                .child(systemMillis + "." + getFileExtension(videoURI));

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

                            HashMap<String, Object> audioMap = new HashMap<>();

                            //put the post info
                            audioMap.put("userID", firebaseUser.getUid());//usersID
                            audioMap.put("postID", postID); //the id of the post is the time at which the post has been added
                            audioMap.put("postCaption", ""); // the post caption
                            audioMap.put("postImage", "noImage"); //the post image which has been send to firebase storage and only the uri is stored
                            audioMap.put("postTime", timeStamp);// the time at which the post has been posted
                            audioMap.put("postType", "audioVideoPost");
                            audioMap.put("videoURL", mediaUrl);
                            audioMap.put("postPrivacy", privacyProtection);
                            audioMap.put("audioURL", audioLink);

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

                                            if (!tagLocation.getText().toString().equals("No Location")) {
                                                tagLocation.setText("No Location");
                                            }

                                            audioRecorder.resetRecorder();

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

    private void uploadAudioImagePost() {
        uploadDialog.show();

        final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                + "." + getFileExtension(imageUri));

        uploadTask = fileReference.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful())
                    throw task.getException();
                return fileReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri imageDownloadLink = task.getResult();
                    myUri = imageDownloadLink.toString();

                    final StorageReference audioPath = audioReference.child("AudioPosts").child(postID + ".3gp");
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

                                HashMap<String, Object> audioImageMap = new HashMap<>();

                                //put the post info
                                audioImageMap.put("userID", firebaseUser.getUid());//usersID
                                audioImageMap.put("postID", postID); //the id of the post is the time at which the post has been added
                                audioImageMap.put("postCaption", ""); // the post caption
                                audioImageMap.put("postImage", myUri); //the post image which has been send to firebase storage and only the uri is stored
                                audioImageMap.put("postTime", timeStamp);// the time at which the post has been posted
                                audioImageMap.put("postType", "audioImagePost");
                                audioImageMap.put("videoURL", "noVideo");
                                audioImageMap.put("postPrivacy", privacyProtection);
                                audioImageMap.put("audioURL", audioLink);

                                if (!tagLocation.getText().toString().equals("No Location")) {
                                    audioImageMap.put("latitude", locationServices.getLatitude());
                                    audioImageMap.put("longitude", locationServices.getLongitude());
                                }

                                if (commentSwitch.isChecked())
                                    audioImageMap.put("commentsAllowed", false);
                                else
                                    audioImageMap.put("commentsAllowed", true);

                                postRef.child(postID).setValue(audioImageMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                uploadDialog.dismiss();

                                                if (!tagLocation.getText().toString().equals("No Location")) {
                                                    tagLocation.setText("No Location");
                                                }

                                                audioRecorder.resetRecorder();

                                                captionArea.setText("");
                                                picToUpload.setImageURI(null);
                                                imageUri = null;

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
                    });

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadAudioPost() {
        uploadDialog.show();
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

                    HashMap<String, Object> audioMap = new HashMap<>();

                    //put the post info
                    audioMap.put("userID", firebaseUser.getUid());//usersID
                    audioMap.put("postID", postID); //the id of the post is the time at which the post has been added
                    audioMap.put("postCaption", ""); // the post caption
                    audioMap.put("postImage", "noImage"); //the post image which has been send to firebase storage and only the uri is stored
                    audioMap.put("postTime", timeStamp);// the time at which the post has been posted
                    audioMap.put("postType", "audioPost");
                    audioMap.put("videoURL", "noVideo");
                    audioMap.put("postPrivacy", privacyProtection);
                    audioMap.put("audioURL", audioLink);

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

                                    if (!tagLocation.getText().toString().equals("No Location")) {
                                        tagLocation.setText("No Location");
                                    }

                                    audioRecorder.resetRecorder();

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

    private void uploadTextPost() {

        HashMap<String, Object> textMap = new HashMap<>();

        textMap.put("userID", firebaseUser.getUid());//usersID
        textMap.put("postID", postID);
        textMap.put("postCaption", caption);
        textMap.put("postImage", "noImage");
        textMap.put("postTime", timeStamp);
        textMap.put("postPrivacy", privacyProtection);//post security
        textMap.put("postType", "textPost");//post security
        textMap.put("videoURL", "noVideo");//post security
        textMap.put("audioURL", "noAudio");

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
                    public void onSuccess(Void aVoid) {
                        postProgress.setVisibility(View.GONE);
                        captionArea.setText("");
                        uploadDialog.dismiss();

                        if (!tagLocation.getText().toString().equals("No Location")) {
                            tagLocation.setText("No Location");
                        }

                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostActivity.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void uploadVideoPost() {

        long systemMillis = System.currentTimeMillis();

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("videos")
                .child(systemMillis + "." + getFileExtension(videoURI));

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

                HashMap<String, Object> videoMap = new HashMap<>();

                //put the post info
                videoMap.put("userID", firebaseUser.getUid());//usersID
                videoMap.put("postID", postID); //the id of the post is the time at which the post has been added
                videoMap.put("postCaption", caption); // the post caption
                videoMap.put("postImage", "noImage"); //the post image which has been send to firebase storage and only the uri is stored
                videoMap.put("postTime", timeStamp);// the time at which the post has been posted
                videoMap.put("postPrivacy", privacyProtection);
                videoMap.put("postType", "videoPost");
                videoMap.put("videoURL", mediaUrl);
                videoMap.put("audioURL", "noAudio");

                if (!tagLocation.getText().toString().equals("No Location")) {
                    videoMap.put("latitude", locationServices.getLatitude());
                    videoMap.put("longitude", locationServices.getLongitude());
                }

                if (commentSwitch.isChecked())
                    videoMap.put("commentsAllowed", false);
                else
                    videoMap.put("commentsAllowed", true);

                postRef.child(postID).setValue(videoMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                captionArea.setText("");
                                videoView.setVideoURI(null);
                                Toast.makeText(PostActivity.this, "Video Posted Successfully", Toast.LENGTH_SHORT).show();
                                uploadDialog.dismiss();



                                if (!tagLocation.getText().toString().equals("No Location")) {
                                    tagLocation.setText("No Location");
                                }

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

    private void uploadImagePost() {

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

                    HashMap<String, Object> imageMap = new HashMap<>();

                    //put the post info
                    imageMap.put("userID", firebaseUser.getUid());//usersID
                    imageMap.put("postID", postID); //the id of the post is the time at which the post has been added
                    imageMap.put("postCaption", caption); // the post caption
                    imageMap.put("postImage", myUri); //the post image which has been send to firebase storage and only the uri is stored
                    imageMap.put("postTime", timeStamp);// the time at which the post has been posted
                    imageMap.put("postType", "imagePost");
                    imageMap.put("videoURL", "noVideo");
                    imageMap.put("postPrivacy", privacyProtection);
                    imageMap.put("audioURL", "noAudio");

                    if (!tagLocation.getText().toString().equals("No Location")) {
                        imageMap.put("latitude", locationServices.getLatitude());
                        imageMap.put("longitude", locationServices.getLongitude());
                    }

                    if (commentSwitch.isChecked())
                        imageMap.put("commentsAllowed", false);
                    else
                        imageMap.put("commentsAllowed", true);

                    postRef.child(postID).setValue(imageMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    postProgress.setVisibility(View.GONE);
                                    uploadDialog.dismiss();

                                    if (!tagLocation.getText().toString().equals("No Location")) {
                                        tagLocation.setText("No Location");
                                    }

                                    captionArea.setText("");
                                    picToUpload.setImageURI(null);
                                    imageUri = null;

                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PostActivity.this, "Could not upload image post", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    private void getUserDetails() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUSER_ID().equals(firebaseUser.getUid())){
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

    private void iniSearchDialog() {
        searchDialog = new Dialog(this);
        searchDialog.setContentView(R.layout.search_pop_up);
        searchDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        searchDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        searchDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        searchDialog.findViewById(R.id.searchBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDialog.dismiss();
            }
        });
    }

    private void iniFriendList() {
        friendListDialog = new Dialog(this);
        friendListDialog.setContentView(R.layout.friend_list);
        friendListDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        friendListDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        friendListDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        List<ContactsModel> userList = new ArrayList<>();

        RecyclerView friendListRecycler = friendListDialog.findViewById(R.id.friendListRecycler);
        ImageView doneTagBTN = friendListDialog.findViewById(R.id.doneTagBTN);
        friendListDialog.findViewById(R.id.friendListSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDialog.show();
            }
        });

        getFriendList(userList, friendListRecycler);

        friendListDialog.findViewById(R.id.f_ListBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendListDialog.dismiss();
            }
        });

        doneTagBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendListDialog.dismiss();
                Toast.makeText(PostActivity.this, "Thank you for your tags", Toast.LENGTH_SHORT).show();

            }
        });
    }//friend list pop up

    private void getFriendList(final List<ContactsModel> userList, RecyclerView friendListRecycler) {
        FriendsAdapter userAdapter;
        friendListRecycler.setHasFixedSize(true);
        friendListRecycler.setNestedScrollingEnabled(false);
        friendListRecycler.hasFixedSize();
        friendListRecycler.setLayoutManager(new LinearLayoutManager(this));

        userList.clear();

        userAdapter = new FriendsAdapter(userList, PostActivity.this);
        friendListRecycler.setAdapter(userAdapter);

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ContactsModel user = ds.getValue(ContactsModel.class);

                    assert user != null;
                    assert firebaseUser != null;
                    if (!user.getUSER_ID().equals(firebaseUser.getUid())) {
                        userList.add(user);
                    }

                    Collections.sort(userList, new Comparator<ContactsModel>() {
                        @Override
                        public int compare(ContactsModel o1, ContactsModel o2) {
                            return o1.getUsername().compareTo(o2.getUsername());
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)  {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            assert result != null;
            imageUri = result.getUri();

            if (imageUri != null){
                if (videoURI != null){
                    videoURI = null;

                    videoCardView.setVisibility(View.GONE);
                }

            }

            imageCardView.setVisibility(View.VISIBLE);
            picToUpload.setImageURI(imageUri);

        }else if (requestCode == PICK_VIDEO_REQUEST && resultCode == -1 && data != null & data.getData() != null){
            videoURI = data.getData();

            if (videoURI != null){
                if (imageUri != null){
                    imageUri = null;

                    imageCardView.setVisibility(View.GONE);
                }
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

    public void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            /*User is signed in stay on this activity
            set phone number of logged in user
             */
            uid = user.getUid();
            name = user.getDisplayName();

        } else {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_post_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.uploadImageItems:
                //used to access the gallery and camera options
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(PostActivity.this);
                break;
            case R.id.uploadVideosItem:
                chooseVideo();
                break;
            case R.id.tagFriendsItem:
                friendListDialog.show();
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
    } //below is the handler for recording voice status

}
