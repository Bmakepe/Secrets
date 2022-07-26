package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.ProfileTabAdapter;
import com.makepe.blackout.GettingStarted.Adapters.UserAdapter;
import com.makepe.blackout.GettingStarted.Fragments.UserPostsFragment;
import com.makepe.blackout.GettingStarted.Fragments.UserVideosFragment;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.FollowInteraction;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {

    private String hisUserID;

    private CircleImageView hisProfilePic;
    private ImageView hisCoverPic, hisVerification;
    private TextView hisUsername, hisBiography, followersNo, followingNo, postsNo,
            sendDM, followBTN, hisLocationDetails, aboutTV;
    private LinearLayout otherUserButtons, hisProfileLocationArea;
    private TabLayout hisTabLayout;
    private ViewPager hisViewPager;

    private ProgressBar coverLoader, picLoader;

    //for his media
    private Dialog picDialog;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, followReference, postReference, videoReference;

    private UniversalFunctions universalFunctions;
    private FollowInteraction followInteraction;
    private GetTimeAgo getTimeAgo;

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
        hisVerification = findViewById(R.id.hisVerification);
        hisTabLayout = findViewById(R.id.hisProfileTabs);
        hisViewPager = findViewById(R.id.hisProfileViewPager);

        final Intent intent = getIntent();
        hisUserID = intent.getStringExtra("uid");

        Bundle bundle = new Bundle();
        bundle.putString("hisUserID", hisUserID);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        followReference = FirebaseDatabase.getInstance().getReference("Follow");
        videoReference = FirebaseDatabase.getInstance().getReference("SecretVideos");
        postReference = FirebaseDatabase.getInstance().getReference("SecretPosts");

        UserPostsFragment postsFragment = new UserPostsFragment();
        postsFragment.setArguments(bundle);

        UserVideosFragment videoFragment = new UserVideosFragment();
        videoFragment.setArguments(bundle);

        universalFunctions = new UniversalFunctions(this);
        followInteraction = new FollowInteraction(this);
        getTimeAgo = new GetTimeAgo();

        iniPicPopUp(hisUserID);
        getUserInfo();
        followInteraction.updateFollowing(hisUserID, followBTN);
        followInteraction.checkFollower(hisUserID);
        followInteraction.getFollowingNo(hisUserID, followingNo);
        followInteraction.getFollowersNo(hisUserID, followersNo);

        universalFunctions.getNrPosts(hisUserID, postsNo);
        universalFunctions.checkActiveStories(hisProfilePic, hisUserID);
        setupFragments();

        if(hisUserID.equals(firebaseUser.getUid())){
            otherUserButtons.setVisibility(View.GONE);
        }

        sendDM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!hisUserID.equals(firebaseUser.getUid())){
                    Intent intent1 = new Intent(ViewProfileActivity.this, ChatActivity.class);
                    intent1.putExtra("userid", hisUserID);
                    startActivity(intent1);
                }
            }
        });

        hisProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hisUserID.equals(firebaseUser.getUid()))
                    picDialog.show();
                else {
                    Intent intent1 = new Intent(ViewProfileActivity.this, FullScreenImageActivity.class);
                    intent1.putExtra("itemID", hisUserID);
                    intent1.putExtra("reason", "userImage");
                    startActivity(intent1);
                }
            }
        });

        hisCoverPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ViewProfileActivity.this, FullScreenImageActivity.class);
                intent1.putExtra("itemID", hisUserID);
                intent1.putExtra("reason", "coverImage");
                startActivity(intent1);
            }
        });

        followBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (followInteraction.checkFollowing(hisUserID))
                    followInteraction.unFollowUser(hisUserID);
                else
                    followInteraction.followUser(hisUserID);
            }
        });

        findViewById(R.id.hisFollowersListBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showFollowersDialog();

            }
        });

        findViewById(R.id.hisFollowingListBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFollowingDialog();
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

    private void setupFragments() {
        ProfileTabAdapter viewPagerAdapter = new ProfileTabAdapter(getSupportFragmentManager());
        postReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int postCounter = 0;
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel model = ds.getValue(PostModel.class);

                    assert model != null;
                    if (model.getUserID().equals(hisUserID))
                        postCounter++;
                }
                if (postCounter != 0)
                    viewPagerAdapter.addFragment(new UserPostsFragment(), "Posts [" + postCounter + "]", hisUserID);

                videoReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int videoCounter = 0;
                        for (DataSnapshot ds : snapshot.getChildren()){
                            PostModel postModel = ds.getValue(PostModel.class);

                            assert postModel != null;
                            if (postModel.getUserID().equals(hisUserID))
                                videoCounter++;
                        }
                        if (videoCounter != 0)
                            viewPagerAdapter.addFragment(new UserVideosFragment(), "Videos [" + videoCounter + "]", hisUserID);

                        hisViewPager.setAdapter(viewPagerAdapter);
                        hisTabLayout.setupWithViewPager(hisViewPager);
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
    }

    private void showFollowingDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);

        interactionHeader.setText("Following");

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(this));
        friendsRecycler.setNestedScrollingEnabled(true);

        followReference.child(hisUserID).child("following")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            idList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()){
                                idList.add(ds.getKey());
                            }
                            friendsRecycler.setAdapter(new UserAdapter(ViewProfileActivity.this, idList, "goToProfile"));
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

    private void showFollowersDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);

        interactionHeader.setText("Followers");

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(this));
        friendsRecycler.setNestedScrollingEnabled(true);

        followReference.child(hisUserID).child("followers")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            idList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()){
                                idList.add(ds.getKey());
                            }
                            friendsRecycler.setAdapter(new UserAdapter(ViewProfileActivity.this, idList, "goToProfile"));
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

    private void iniPicPopUp(String hisUserID) {
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
        videoCallBTN.setVisibility(View.GONE);
        universalFunctions.checkActiveStories(proPicPopUp, hisUserID);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUserID().equals(hisUserID)) {
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
                Intent callIntent = new Intent(ViewProfileActivity.this, PhoneCallActivity.class);
                callIntent.putExtra("userID", hisUserID);
                startActivity(callIntent);
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
                                    intent1.putExtra("itemID", hisUserID);
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
                                    intent.putExtra("userid", hisUserID);
                                    startActivity(intent);
                                    dialogInterface.dismiss();
                                }
                            });
                    alertDialog.show();
                }else if (proPicPopUp.getTag().equals("noStories")){
                    Intent intent1 = new Intent(ViewProfileActivity.this, FullScreenImageActivity.class);
                    intent1.putExtra("itemID", hisUserID);
                    intent1.putExtra("reason", "userImage");
                    startActivity(intent1);
                    picDialog.dismiss();
                }
            }
        });

    }

    private void getUserInfo() {

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);

                        assert user != null;
                        if (user.getUserID().equals(hisUserID)){
                            hisUsername.setVisibility(View.VISIBLE);
                            hisBiography.setVisibility(View.VISIBLE);

                            aboutTV.setText("About " + user.getUsername());
                            hisBiography.setText(user.getBiography() + "\n" + "Joined: " + getTimeAgo.getTimeAgo(Long.parseLong(user.getTimeStamp()), ViewProfileActivity.this));

                            if(hisUserID.equals(firebaseUser.getUid())){
                                hisUsername.setText("Me");
                            }else {
                                hisUsername.setText(user.getUsername());
                            }

                            hisVerification.setVisibility(!user.isVerified() ? View.GONE : View.VISIBLE);

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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!hisUserID.equals(firebaseUser.getUid()))
            getMenuInflater().inflate(R.menu.his_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.postNotifications:
                Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
                break;

            case R.id.blockUser:
                Toast.makeText(this, "you will be able to block this user", Toast.LENGTH_SHORT).show();
                break;

            case R.id.report:
                Intent intent1  = new Intent(ViewProfileActivity.this, ReportActivity.class);
                intent1.putExtra("reported", hisUserID);
                startActivity(intent1);
                break;
        }
        return false;
    }
}
