package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.makepe.blackout.GettingStarted.Adapters.FriendsAdapter;
import com.makepe.blackout.GettingStarted.MainActivity;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity implements LocationListener {

    CircleImageView postActivityProPic;

    FirebaseUser firebaseUser;
    DatabaseReference userRef, postRef;
    StorageReference storageReference;

    private ImageView picToUpload, goToCam, tagFriends, backBTN, voiceShareIcon, statusPlayBTN;
    private TextView postBTN, recVoiceBTN;
    private EditText captionArea;
    private String name, uid, caption;
    private CardView imageCardView, videoCardView;

    private boolean isPlaying = false;

    ProgressBar postProgress;
    ProgressDialog uploadDialog;

    private Dialog friendListDialog, searchDialog;

    Uri imageUri;
    String myUri = "";
    StorageTask uploadTask;

    public static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    public static final int STORAGE_REQUEST_CODE = 100;
    public static final int IMAGE_PICK_GALLERY_CODE = 200;
    public static final int LOCATION_REQUEST_CODE = 300;
    private static final int PICK_VIDEO_REQUEST = 400;

    //for recording audio status
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String AudioSavePath = null;

    //for video gallery
    private Uri videoURI;
    VideoView videoView;
    ConstraintLayout videoArea;
    String mediaUrl;

    //for Location services
    private String[] locationPermission;
    private LocationManager locationManager;
    private double latitude, longitude;
    private RelativeLayout myLocationAreaBTN, privacyBTN;
    private TextView tagLocation, audiencePicker;
    private String privacyProtection = "Public";


    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        postActivityProPic = findViewById(R.id.addPostProPic);
        postBTN = findViewById(R.id.postFAB);
        postProgress = findViewById(R.id.postProgress);
        goToCam = findViewById(R.id.goToCamera);
        tagLocation = findViewById(R.id.locationCheckIn);
        myLocationAreaBTN = findViewById(R.id.postLocationBTN);
        tagFriends = findViewById(R.id.tagFriends);
        picToUpload = findViewById(R.id.picToUpload);
        backBTN = findViewById(R.id.backBTN);
        captionArea = findViewById(R.id.addPostArea);
        imageCardView = findViewById(R.id.imageCardView);
        videoView = findViewById(R.id.previewCameraSelectedVideo);
        videoArea = findViewById(R.id.videoArea);
        videoCardView = findViewById(R.id.videoCardView);
        privacyBTN = findViewById(R.id.privacySettingsBTN);
        audiencePicker = findViewById(R.id.audience_picker);
        recVoiceBTN = findViewById(R.id.recVoiceBTN);
        voiceShareIcon = findViewById(R.id.share_recVoiceIcon);
        statusPlayBTN = findViewById(R.id.share_playVoiceIcon);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        postRef = FirebaseDatabase.getInstance().getReference("Posts");
        storageReference = FirebaseStorage.getInstance().getReference("Post_Pics");

        uploadDialog = new ProgressDialog(this);

        getUserDetails();
        checkUserStatus();
        iniFriendList();
        iniSearchDialog();
        //getTags();

        postBTN.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                //Button for posting to the database
                caption = captionArea.getText().toString().trim();
                uploadDialog.setMessage("Loading...");

                if(TextUtils.isEmpty(caption)){
                    captionArea.setError("Whats Up???");
                    captionArea.requestFocus();
                }else if(imageUri != null){
                    uploadImagePost(caption);
                }else if(videoURI != null){
                    uploadVideoPost(caption);
                }else{
                    uploadTextPost(caption);
                }
            }
        });

        goToCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //used to access the gallery and camera options
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(PostActivity.this);

            }
        });

        tagLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkLocationPermission()){
                    detectLocation();
                }else{
                    ActivityCompat.requestPermissions(PostActivity.this, locationPermission, LOCATION_REQUEST_CODE);
                }
            }
        });

        tagFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PostActivity.this, "Friends list will popup", Toast.LENGTH_SHORT).show();
                friendListDialog.show();
            }
        });

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.goToVideos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseVideo();
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

        findViewById(R.id.recButtonArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recVoiceBTN.getText().toString().equals("Rec")){

                    if (checkAudioPermissions()){

                        Toast.makeText(PostActivity.this, "Recording has began", Toast.LENGTH_SHORT).show();

                        recVoiceBTN.setText("Stop");
                        voiceShareIcon.setImageResource(R.drawable.ic_baseline_stop_circle_24);
                        findViewById(R.id.recordingAlert).setVisibility(View.VISIBLE);
                        findViewById(R.id.postCaptionArea).setVisibility(View.GONE);

                    }else{
                        ActivityCompat.requestPermissions(PostActivity.this, new String[]{
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }, 1);
                    }

                }else if (recVoiceBTN.getText().toString().equals("Stop")){

                    Toast.makeText(PostActivity.this, "Recording has stopped", Toast.LENGTH_SHORT).show();
                    recVoiceBTN.setText("Del");
                    voiceShareIcon.setImageResource(R.drawable.ic_delete_black_24dp);
                    findViewById(R.id.playAudioArea).setVisibility(View.VISIBLE);
                    findViewById(R.id.postCaptionArea).setVisibility(View.GONE);
                    findViewById(R.id.recordingAlert).setVisibility(View.GONE);

                }else if (recVoiceBTN.getText().toString().equals("Del")){

                    Toast.makeText(PostActivity.this, "you will be able to delete the recording and listen to it", Toast.LENGTH_SHORT).show();

                    recVoiceBTN.setText("Rec");
                    findViewById(R.id.playAudioArea).setVisibility(View.GONE);
                    findViewById(R.id.postCaptionArea).setVisibility(View.VISIBLE);
                    voiceShareIcon.setImageResource(R.drawable.ic_baseline_fiber_manual_record_24);

                }
            }
        });



    }

    private boolean checkAudioPermissions(){
        int first = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO);
        int second = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return first == PackageManager.PERMISSION_GRANTED  &&
                second == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void detectLocation() {
        Toast.makeText(this, "Please wait... Detecting your location", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    private void findAddress() {
        //find address, country, state, city

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try{
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0);//complete address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            String myLocation = address + ", " + city + ", " + state + ", " + country;

            tagLocation.setText(myLocation);

        }catch (Exception e){
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }


    private void uploadTextPost(String caption) {
        uploadDialog.show();
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String postID = postRef.push().getKey();

        HashMap<String, Object> textMap = new HashMap<>();

        textMap.put("userID", firebaseUser.getUid());//usersID
        textMap.put("postID", postID);
        textMap.put("postCaption", caption);
        textMap.put("postImage", "noImage");
        textMap.put("postTime", timeStamp);
        textMap.put("postPrivacy", privacyProtection);//post security
        textMap.put("postType", "textPost");//post security
        textMap.put("videoURL", "noVideo");//post security

        if (!TextUtils.isEmpty(tagLocation.getText().toString())) {
            textMap.put("latitude", latitude);
            textMap.put("longitude", longitude);
        }

        postRef.child(postID).setValue(textMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        postProgress.setVisibility(View.GONE);
                        captionArea.setText("");
                        uploadDialog.dismiss();

                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostActivity.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void uploadVideoPost(String caption) {
        uploadDialog.show();
        final String timeStamp = String.valueOf(System.currentTimeMillis());

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

                String postID = postRef.push().getKey();
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

                if (!TextUtils.isEmpty(tagLocation.getText().toString())) {
                    videoMap.put("latitude", latitude);
                    videoMap.put("longitude", longitude);
                }

                postRef.child(postID).setValue(videoMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                captionArea.setText("");
                                videoView.setVideoURI(null);
                                Toast.makeText(PostActivity.this, "Video Posted Successfully", Toast.LENGTH_SHORT).show();
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

    private void uploadImagePost(String caption) {
        uploadDialog.show();
        final String timeStamp = String.valueOf(System.currentTimeMillis());

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

                    String postID = postRef.push().getKey();

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

                    if (!TextUtils.isEmpty(tagLocation.getText().toString())) {
                        imageMap.put("latitude", latitude);
                        imageMap.put("longitude", longitude);
                    }

                    postRef.child(postID).setValue(imageMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    postProgress.setVisibility(View.GONE);
                                    uploadDialog.dismiss();

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

    /*private void getTags() {
        final Intent tags = getIntent();
        tagged = tags.getStringExtra("taggedPeople");

        try{
            if(tagged.isEmpty()){
                taggedPeople.setVisibility(View.GONE);
            }else{
                taggedPeople.setVisibility(View.VISIBLE);
                taggedPeople.setText(tagged);
            }
        }catch (NullPointerException ignored){}
    }*/

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

            imageCardView.setVisibility(View.VISIBLE);
            picToUpload.setImageURI(imageUri);

        }else if (requestCode == PICK_VIDEO_REQUEST && resultCode == -1 && data != null & data.getData() != null){
            videoURI = data.getData();

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
    }

    @Override
    protected void onStart() {
        checkOnlineStatus();
        //executes when the activity is launched to check if the user is still logged in properly
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        checkOnlineStatus();
        super.onResume();
        checkUserStatus();
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
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", "online");
        dbRef.updateChildren(hashMap);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    } //below is the handler for recording voice status

    private void uploadData(final String caption, String uri, final String privacyLock) {//for post image name, post id, post timestamp

        final String timeStamp = String.valueOf(System.currentTimeMillis());

        if(!uri.equals("noImage")){
            //posts with images - noImage is not parsed into the function

            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation(){
                @Override
                public Object then(@NonNull Task task) throws Exception{
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUri = downloadUri.toString();

                        String postID = postRef.push().getKey();

                        //url is received upload post to firebase database

                        HashMap<Object, String> hashMap = new HashMap<>();

                        //put the post info
                        hashMap.put("userID", firebaseUser.getUid());//usersID
                        hashMap.put("postID", postID); //the id of the post is the time at which the post has been added
                        hashMap.put("postCaption", caption); // the post caption
                        hashMap.put("postImage", myUri); //the post image which has been send to firebase storage and only the uri is stored
                        hashMap.put("postTime", timeStamp);// the time at which the post has been posted
                        hashMap.put("postPrivacy", privacyLock);//post security
                        hashMap.put("postType", "imagePost");

                        //put data in this reference
                        postRef.child(postID).setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @SuppressLint("RestrictedApi")
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //executes when the post has successfully been posted to firebase database
                                        postBTN.setVisibility(View.VISIBLE);
                                        postProgress.setVisibility(View.INVISIBLE);

                                        //reset view after posting
                                        captionArea.setText("");
                                        picToUpload.setImageURI(null);
                                        imageUri = null;
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(PostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else{
            //executes when there is not image added to the post
            HashMap<Object, String> hashMap = new HashMap<>();

            String postID = postRef.push().getKey();

            hashMap.put("userID", firebaseUser.getUid());//usersID
            hashMap.put("postID", postID);
            hashMap.put("postCaption", caption);
            hashMap.put("postImage", "noImage");
            hashMap.put("postTime", timeStamp);
            hashMap.put("postPrivacy", privacyLock);//post security
            hashMap.put("postType", "textPost");//post security

            postRef.child(postID).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @SuppressLint("RestrictedApi")
                        @Override
                        public void onSuccess(Void aVoid) {
                            //executes when the post with no image has been posted successfully to firebase
                            postBTN.setVisibility(View.VISIBLE);
                            postProgress.setVisibility(View.INVISIBLE);

                            //reset view after posting
                            captionArea.setText("");
                            picToUpload.setImageURI(null);
                            imageUri = null;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

}
