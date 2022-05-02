package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.HisTabAdapter;
import com.makepe.blackout.GettingStarted.Fragments.HisPostsFragment;
import com.makepe.blackout.GettingStarted.Fragments.HisVideosFragment;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalNotifications;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class ViewProfileActivity extends AppCompatActivity {

    private String hisid, hisPhoneName;

    private CircleImageView hisProfilePic;
    private ImageView hisCoverPic;
    private TextView hisUsername, hisBiography, followersNo, followingNo, postsNo,
            sendDM, followBTN, hisLocationDetails, aboutTV;
    private LinearLayout otherUserButtons, hisProfileLocationArea;

    private ProgressBar coverLoader, picLoader;

    //for his media
    private Dialog picDialog;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, postReference, followersReference, followingReference;

    private int videoCount = 0, postCount = 0, totalCount = 0; 

    private UniversalFunctions universalFunctions;
    private UniversalNotifications notifications;

    private double latitude, longitude;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        Toolbar hisProfileToolbar = findViewById(R.id.hisProfileToolbar);
        setSupportActionBar(hisProfileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        hisProfilePic = findViewById(R.id.hisProfilePicture);
        hisCoverPic = findViewById(R.id.hisCoverImage);
        hisUsername = findViewById(R.id.hisProfileUsername);
        hisBiography = findViewById(R.id.hisProfileBio);
        sendDM = findViewById(R.id.sendMessageBTN);
        followBTN = findViewById(R.id.followHimBTN);
        postsNo = findViewById(R.id.hisPosts);
        followersNo = findViewById(R.id.hisfollowersNo);
        followingNo = findViewById(R.id.hisFollowingNo);
        coverLoader = findViewById(R.id.hisCoverPicLoader);
        picLoader = findViewById(R.id.hisProPicLoader);
        hisLocationDetails = findViewById(R.id.hisLocationDetails);
        otherUserButtons = findViewById(R.id.otherUserButtons);
        aboutTV = findViewById(R.id.hisProfileAboutTV);
        hisProfileLocationArea = findViewById(R.id.hisProfileLocationArea);
        TabLayout hisTabLayout = findViewById(R.id.hisProfileTabs);
        ViewPager hisViewPager = findViewById(R.id.hisProfileViewPager);

        final Intent intent = getIntent();
        hisid = intent.getStringExtra("uid");

        Bundle bundle = new Bundle();
        bundle.putString("hisUserID", hisid);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        postReference = FirebaseDatabase.getInstance().getReference("Posts");
        followersReference = FirebaseDatabase.getInstance().getReference().child("Follow").child(hisid).child("followers");
        followingReference = FirebaseDatabase.getInstance().getReference().child("Follow").child(hisid).child("following");

        HisPostsFragment postsFragment = new HisPostsFragment();
        postsFragment.setArguments(bundle);

        HisVideosFragment videoFragment = new HisVideosFragment();
        videoFragment.setArguments(bundle);

        universalFunctions = new UniversalFunctions(this);
        notifications = new UniversalNotifications(this); 

        iniPicPopUp(hisid);
        getFollowers();
        checkFollowing();
        getUserInfo();
        getNrPosts();
        universalFunctions.checkActiveStories(hisProfilePic, hisid);

        HisTabAdapter viewPagerAdapter = new HisTabAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new HisPostsFragment(), "Posts [" + postCount + "]", hisid);
        viewPagerAdapter.addFragment(new HisVideosFragment(), "Videos [" + videoCount + "]", hisid);
        hisViewPager.setAdapter(viewPagerAdapter);
        hisTabLayout.setupWithViewPager(hisViewPager);

        if(hisid.equals(firebaseUser.getUid())){
            otherUserButtons.setVisibility(View.GONE);
        }

        sendDM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!hisid.equals(firebaseUser.getUid())){
                    Intent intent1 = new Intent(ViewProfileActivity.this, ChatActivity.class);
                    intent1.putExtra("userid", hisid);
                    startActivity(intent1);
                }
            }
        });

        hisProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hisid.equals(firebaseUser.getUid()))
                    picDialog.show();
                else {
                    Intent intent1 = new Intent(ViewProfileActivity.this, FullScreenImageActivity.class);
                    intent1.putExtra("itemID", hisid);
                    intent1.putExtra("reason", "userImage");
                    startActivity(intent1);
                }
            }
        });

        hisCoverPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ViewProfileActivity.this, FullScreenImageActivity.class);
                intent1.putExtra("itemID", hisid);
                intent1.putExtra("reason", "coverImage");
                startActivity(intent1);
            }
        });

        followBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(followBTN.getText().toString().equals("Follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(hisid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(hisid)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                    notifications.addFollowNotifications(hisid);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(hisid).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(hisid)
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        findViewById(R.id.hisFollowersListBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent followersIntent = new Intent(ViewProfileActivity.this, ConnectionsActivity.class);
                followersIntent.putExtra("UserID", hisid);
                followersIntent.putExtra("Interaction", "Followers");
                startActivity(followersIntent);

            }
        });

        findViewById(R.id.hisFollowingListBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent followersIntent = new Intent(ViewProfileActivity.this, ConnectionsActivity.class);
                followersIntent.putExtra("UserID", hisid);
                followersIntent.putExtra("Interaction", "Following");
                startActivity(followersIntent);

            }
        });

        hisLocationDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String address = "https://maps.google.com/maps?saddr=" + latitude + "," + longitude;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                startActivity(intent);
            }
        });

    }

    private void checkFollowing(){
        DatabaseReference referenceFollowing = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        referenceFollowing.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(hisid).exists()){
                    //executes if following user
                    followBTN.setText("Following");
                }else{
                    followBTN.setText("Follow");
                    //executes if you are not following the user
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getFollowers(){

        followersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followersNo.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        followingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingNo.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void iniPicPopUp(String hisid) {
        picDialog = new Dialog(this);
        picDialog.setContentView(R.layout.profile_pic_pop_up_layout);
        picDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        picDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        picDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        final CircleImageView proPicPopUp = picDialog.findViewById(R.id.popUP_ProPic);
        final ImageView videoCallBTN, profileBTN, voiceCallBTN;
        videoCallBTN = picDialog.findViewById(R.id.popUP_SendMessage);
        profileBTN = picDialog.findViewById(R.id.popUP_ViewProfile);
        voiceCallBTN = picDialog.findViewById(R.id.popUP_callUser);

        profileBTN.setVisibility(View.GONE);
        videoCallBTN.setImageResource(R.drawable.ic_video_call_black_24dp);
        universalFunctions.checkActiveStories(proPicPopUp, hisid);

        final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUSER_ID().equals(hisid)) {
                        try {
                            //load pro pic into imageView
                            Picasso.get().load(user.getImageURL()).placeholder(R.drawable.default_profile_display_pic).into(proPicPopUp);
                        } catch (Exception ignored) {

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        voiceCallBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ViewProfileActivity.this, "start call", Toast.LENGTH_SHORT).show();
                picDialog.dismiss();
            }
        });

        videoCallBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ViewProfileActivity.this, "start video call", Toast.LENGTH_SHORT).show();
                picDialog.dismiss();
            }
        });

        proPicPopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (proPicPopUp.getTag().equals("storyActive")){
                    AlertDialog alertDialog = new AlertDialog.Builder(ViewProfileActivity.this).create();
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Picture",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent1 = new Intent(ViewProfileActivity.this, FullScreenImageActivity.class);
                                    intent1.putExtra("itemID", hisid);
                                    intent1.putExtra("reason", "userImage");
                                    startActivity(intent1);
                                    dialogInterface.dismiss();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "View Stories",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(ViewProfileActivity.this, StoryActivity.class);
                                    intent.putExtra("userid", hisid);
                                    startActivity(intent);
                                    dialogInterface.dismiss();
                                }
                            });
                    alertDialog.show();
                }else if (proPicPopUp.getTag().equals("noStories")){
                    Intent intent1 = new Intent(ViewProfileActivity.this, FullScreenImageActivity.class);
                    intent1.putExtra("itemID", hisid);
                    intent1.putExtra("reason", "userImage");
                    startActivity(intent1);
                    picDialog.dismiss();
                }
            }
        });

    }

    private void getUserInfo() {

        List<ContactsModel> phoneBook = new ArrayList<>();
        ContactsList contactsList= new ContactsList(phoneBook, ViewProfileActivity.this);
        contactsList.readContacts();
        final List<ContactsModel> finalContacts = contactsList.getContactsList();

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);

                        assert user != null;
                        if (user.getUSER_ID().equals(hisid)){
                                hisUsername.setVisibility(View.VISIBLE);
                                hisBiography.setVisibility(View.VISIBLE);

                                hisBiography.setText(user.getBio());
                                aboutTV.setText("About " + user.getUsername());

                                for(ContactsModel contactsModel: finalContacts){

                                    if(hisid.equals(FirebaseAuth.getInstance().getCurrentUser())){
                                        hisUsername.setText("Me");
                                    }else if(contactsModel.getNumber().equals(user.getNumber())){
                                        hisPhoneName = contactsModel.getUsername();
                                        hisUsername.setText(hisPhoneName);
                                    }else{
                                        hisUsername.setText(user.getUsername());
                                    }
                                }

                                try{
                                    Picasso.get().load(user.getImageURL()).into(hisProfilePic);
                                    Picasso.get().load(user.getCoverURL()).into(hisCoverPic);
                                    coverLoader.setVisibility(View.GONE);
                                    picLoader.setVisibility(View.GONE);
                                }catch (NullPointerException e){
                                    Picasso.get().load(R.drawable.default_profile_display_pic).into(hisProfilePic);
                                    Picasso.get().load(R.drawable.default_profile_display_pic).into(hisCoverPic);
                                    coverLoader.setVisibility(View.GONE);
                                    picLoader.setVisibility(View.GONE);
                                }

                                try{
                                    latitude = user.getLatitude();
                                    longitude = user.getLongitude();
                                    universalFunctions.findAddress(latitude, longitude, hisLocationDetails, hisProfileLocationArea);
                                }catch (NullPointerException ignored){}
                            }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getNrPosts(){
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalCount = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PostModel post = snapshot.getValue(PostModel.class);
                    assert post != null;

                    if (post.getUserID().equals(hisid) && post.getPostType().equals("videoPost")) {
                        videoCount++;
                    } else if (post.getUserID().equals(hisid) && !post.getPostType().equals("videoPost")){
                        postCount++;
                    }
                }
                totalCount = videoCount + postCount;
                postsNo.setText(String.valueOf(totalCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        if (!hisid.equals(firebaseUser.getUid()))
            getMenuInflater().inflate(R.menu.his_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.postNotifications:
                Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.blockUser:
                Toast.makeText(this, "you will be able to block this user", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.report:
                Intent intent1  = new Intent(ViewProfileActivity.this, ReportActivity.class);
                intent1.putExtra("reported", hisid);
                startActivity(intent1);
                return true;

            default:
                Toast.makeText(this, "Unknown Selection", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
