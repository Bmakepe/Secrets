package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class SharePostActivity extends AppCompatActivity implements LocationListener {

    CircleImageView myProfilePic, hisProfilePic;
    TextView shareBTN, checkInBTN, hisNameTV, hisLocationTV, hisCaptionTV, hisPostDate;
    ImageView hisPostImage, recVoiceBTN;
    EditText myCaptionET;
    CardView picArea;
    LinearLayout locationArea;

    //for video sharing
    VideoView videoView;
    CardView videoArea;
    ProgressBar sharedVideoLoader;

    DatabaseReference postRef, userRef, notificationsRef;
    FirebaseUser firebaseUser;

    String sharePostID, sharePostTimeStamp, userID, myCaption, postType;

    GetTimeAgo getTimeAgo;

    UniversalFunctions universalFunctions;

    ProgressDialog uploadProgress;

    //for Location services & privacy settings
    private String[] locationPermission;
    private LocationManager locationManager;
    private double latitude, longitude;
    private RelativeLayout tagLocationBTN, privacyBTN;
    private TextView myLocationCheckIn, audiencePicker;
    private String myLocation, privacyProtection = "Public";

    public static final int LOCATION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_post);

        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        myProfilePic = findViewById(R.id.share_userProPic);
        hisProfilePic = findViewById(R.id.share_image_user);
        shareBTN = findViewById(R.id.postShareBTN);
        checkInBTN = findViewById(R.id.share_tag_location);
        hisNameTV = findViewById(R.id.share_username);
        hisPostDate = findViewById(R.id.share_postDate);
        hisLocationTV = findViewById(R.id.share_postCheckIn);
        hisCaptionTV = findViewById(R.id.share_post_desc);
        hisPostImage = findViewById(R.id.share_postImage);
        myCaptionET = findViewById(R.id.shareCaption);
        picArea = findViewById(R.id.share_postPicArea);
        locationArea = findViewById(R.id.share_postLocationArea);
        myLocationCheckIn = findViewById(R.id.share_locationCheckIn);
        audiencePicker = findViewById(R.id.share_audience_picker);
        videoView = findViewById(R.id.sharePreviewCameraSelectedVideo);
        videoArea = findViewById(R.id.shareVideoCardView);
        sharedVideoLoader = findViewById(R.id.sharedVideoLoader);
        tagLocationBTN = findViewById(R.id.shareTagLocationBTN);
        privacyBTN = findViewById(R.id.sharingPrivacyBTN);
        recVoiceBTN = findViewById(R.id.share_recVoiceBTN);

        Intent intent = getIntent();
        sharePostID = intent.getStringExtra("postID");
        getTimeAgo = new GetTimeAgo();
        uploadProgress = new ProgressDialog(this);
        universalFunctions = new UniversalFunctions(SharePostActivity.this);

        postRef = FirebaseDatabase.getInstance().getReference("Posts");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        notificationsRef = FirebaseDatabase.getInstance().getReference("Notifications");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getMyDetails();
        getSharedPostDetails();

        findViewById(R.id.postProPic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!userID.equals(firebaseUser.getUid())){
                    Intent intent = new Intent(SharePostActivity.this, ViewProfileActivity.class);
                    intent.putExtra("uid", userID);
                    startActivity(intent);
                }
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

        findViewById(R.id.post_share_back_BTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tagLocationBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkLocationPermission()){
                    detectLocation();
                }else{
                    ActivityCompat.requestPermissions(SharePostActivity.this, locationPermission, LOCATION_REQUEST_CODE);
                }
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
                myCaption = myCaptionET.getText().toString().trim();
                uploadProgress.setMessage("Loading...");

                if (TextUtils.isEmpty(myCaption)){
                    myCaptionET.setError("Whats Up???");
                    myCaptionET.requestFocus();
                }else{
                    switch(postType){
                        case "textPost":
                            uploadSharedPost("sharedTextPost", myCaption);
                            break;

                        case "videoPost":
                            uploadSharedPost("sharedVideoPost", myCaption);
                            break;

                        case "imagePost":
                            uploadSharedPost("sharedImagePost", myCaption);
                            break;

                        default:
                            Toast.makeText(SharePostActivity.this, "Unknown post type being shared", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        recVoiceBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SharePostActivity.this, "you will be able to record your voice status", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void uploadSharedPost(String sharedTextPost, String caption) {
        uploadProgress.show();
        final String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> sharedMap = new HashMap<>();
        String postID = postRef.push().getKey();

        sharedMap.put("userID", firebaseUser.getUid());//usersID
        sharedMap.put("postID", postID);
        sharedMap.put("postCaption", caption);
        sharedMap.put("postImage", "noImage");
        sharedMap.put("postTime", timeStamp);
        sharedMap.put("postPrivacy", privacyProtection);//post security
        sharedMap.put("postType", sharedTextPost);//post security
        sharedMap.put("videoURL", "noVideo");//post security
        sharedMap.put("sharedPost", sharePostID);

        if (!TextUtils.isEmpty(myLocationCheckIn.getText().toString())) {
            sharedMap.put("latitude", latitude);
            sharedMap.put("longitude", longitude);
        }

        postRef.child(postID).setValue(sharedMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SharePostActivity.this, "Shared Successfully", Toast.LENGTH_SHORT).show();
                        uploadProgress.dismiss();
                        myCaptionET.setText("");

                        sendShareNotification(caption);

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
        hashMap.put("text", "commented: " + caption);
        hashMap.put("postid", sharePostID);
        hashMap.put("ispost", true);
        hashMap.put("timeStamp", timeStamp);

        assert notificationID != null;
        notificationsRef.child(userID).child(notificationID).setValue(hashMap);
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

            myLocation = address + ", " + city + ", " + state + ", " + country;

            myLocationCheckIn.setText(myLocation);

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

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void getSharedPostDetails() {
        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel post = ds.getValue(PostModel.class);

                    if (post.getPostID().equals(sharePostID)){
                        postType = post.getPostType();
                        userID = post.getUserID();
                        switch (postType){
                            case "imagePost":
                                picArea.setVisibility(View.VISIBLE);
                                hisPostImage.setVisibility(View.VISIBLE);
                                try{
                                    Picasso.get().load(post.getPostImage()).into(hisPostImage);
                                }catch (NullPointerException ignored){}
                                getUniversalPostDetails(post);
                                break;

                            case "textPost":
                                picArea.setVisibility(View.GONE);
                                getUniversalPostDetails(post);
                                break;

                            case "videoPost":
                                try{
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
                                }catch (NullPointerException ignored){}
                                getUniversalPostDetails(post);
                                break;

                            case "sharedTextPost":
                                Toast.makeText(SharePostActivity.this, "You will be sharing a shared post", Toast.LENGTH_SHORT).show();
                                getUniversalPostDetails(post);
                                break;

                            default:
                                Toast.makeText(SharePostActivity.this, "Unknown sharing error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUniversalPostDetails(PostModel post) {

        if (post.getPostCaption().equals("")){
            hisCaptionTV.setVisibility(View.GONE);
        }else{
            hisCaptionTV.setVisibility(View.VISIBLE);
            hisCaptionTV.setText(post.getPostCaption());
        }

        try{
            sharePostTimeStamp = getTimeAgo.getTimeAgo(Long.parseLong(post.getPostTime()), SharePostActivity.this);
            hisPostDate.setText(sharePostTimeStamp);
        }catch (NumberFormatException ignored){}

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUSER_ID().equals(post.getUserID())){
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
}