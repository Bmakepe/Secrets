package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.HisTabAdapter;
import com.makepe.blackout.GettingStarted.Adapters.MediaAdapter;
import com.makepe.blackout.GettingStarted.Adapters.PostAdapter;
import com.makepe.blackout.GettingStarted.Fragments.HisPostsFragment;
import com.makepe.blackout.GettingStarted.Fragments.HisVideosFragment;
import com.makepe.blackout.GettingStarted.MainActivity;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class ViewProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    String hisid, myID, hisPhoneName;

    CircleImageView hisProfilePic;
    ImageView hisCoverPic, postNotificationBTN, profileBack, onlineStatus;
    TextView hisUsername, hisBiography, followersNo, followingNo, postsNo, sendDM, followBTN, hisLocationDetails;

    private ProgressBar coverLoader, picLoader;

    //for his media
    Dialog picDialog, searchFollowersDialog;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, postReference;

    private int videoCount = 0, postCount = 0;

    TabLayout hisTabLayout;
    ViewPager hisViewPager;

    UniversalFunctions universalFunctions;

    private double latitude, longitude;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        hisProfilePic = findViewById(R.id.hisProfilePicture);
        hisCoverPic = findViewById(R.id.hisCoverImage);
        hisUsername = findViewById(R.id.hisProfileUsername);
        hisBiography = findViewById(R.id.hisProfileBio);
        postNotificationBTN = findViewById(R.id.postNotificationBTN);
        sendDM = findViewById(R.id.sendMessageBTN);
        profileBack = findViewById(R.id.profileBackBTN);
        followBTN = findViewById(R.id.followHimBTN);
        onlineStatus = findViewById(R.id.hisOnlineStatus);
        postsNo = findViewById(R.id.hisPosts);
        followersNo = findViewById(R.id.hisfollowersNo);
        followingNo = findViewById(R.id.hisFollowingNo);
        coverLoader = findViewById(R.id.hisCoverPicLoader);
        picLoader = findViewById(R.id.hisProPicLoader);
        hisTabLayout = findViewById(R.id.hisProfileTabs);
        hisViewPager = findViewById(R.id.hisProfileViewPager);
        hisLocationDetails = findViewById(R.id.hisLocationDetails);

        final Intent intent = getIntent();
        hisid = intent.getStringExtra("uid");

        Bundle bundle = new Bundle();
        bundle.putString("hisUserID", hisid);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        postReference = FirebaseDatabase.getInstance().getReference("Posts");

        HisPostsFragment postsFragment = new HisPostsFragment();
        postsFragment.setArguments(bundle);

        HisVideosFragment videoFragment = new HisVideosFragment();
        videoFragment.setArguments(bundle);

        universalFunctions = new UniversalFunctions(this);

        checkUserStatus();
        iniPicPopUp(hisid);
        getNrPosts();
        getFollowers();
        isFollowing(hisid, followBTN);
        checkFollow();
        getUserInfo(hisid);
        iniSearchFollowers();

        HisTabAdapter viewPagerAdapter = new HisTabAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new HisPostsFragment(), "Posts [" + postCount + "]", hisid);
        viewPagerAdapter.addFragment(new HisVideosFragment(), "Videos [" + videoCount + "]", hisid);
        hisViewPager.setAdapter(viewPagerAdapter);
        hisTabLayout.setupWithViewPager(hisViewPager);

        if(hisid.equals(myID)){
            sendDM.setVisibility(View.GONE);
            postNotificationBTN.setVisibility(View.GONE);
            followBTN.setVisibility(View.GONE);
        }

        postNotificationBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ViewProfileActivity.this, "post notifications will be switched on", Toast.LENGTH_SHORT).show();
            }
        });

        sendDM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(hisid.equals(firebaseUser.getUid())){

                }else{
                    Intent intent1 = new Intent(ViewProfileActivity.this, ChatActivity.class);
                    intent1.putExtra("userid", hisid);
                    startActivity(intent1);
                }
            }
        });

        profileBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        hisProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picDialog.show();
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
                if(followBTN.getTag().equals("Follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(hisid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(hisid)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                    universalFunctions.addFollowNotifications(hisid);
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

    private void iniSearchFollowers() {
        searchFollowersDialog = new Dialog(this);
        searchFollowersDialog.setContentView(R.layout.search_pop_up);
        searchFollowersDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        searchFollowersDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        searchFollowersDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        searchFollowersDialog.findViewById(R.id.searchBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchFollowersDialog.dismiss();
            }
        });
    }

    private void isFollowing(final String userid, final TextView followBTN){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userid).exists()){
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

    private void checkFollow(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(hisid).exists()){
                    followBTN.setTag("Following");
                }else{
                    followBTN.setTag("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getFollowers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(hisid).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followersNo.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(hisid).child("following");
        reference1.addValueEventListener(new ValueEventListener() {
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

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);

        Query query = reference.orderByChild("USER_ID").equalTo(hisid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        String proPic = "" + ds.child("ImageURL").getValue();

                        try{
                            //load pro pic into imageView
                            Picasso.get().load(proPic).placeholder(R.drawable.default_profile_display_pic).into(proPicPopUp);
                        }catch (Exception ignored){

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
                Intent intent1 = new Intent(ViewProfileActivity.this, FullScreenImageActivity.class);
                intent1.putExtra("itemID", hisid);
                intent1.putExtra("reason", "userImage");
                startActivity(intent1);
                picDialog.dismiss();
            }
        });
    }

    private void getUserInfo(final String hisid) {

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

                        if (user.getUSER_ID().equals(hisid)){
                                hisUsername.setVisibility(View.VISIBLE);
                                hisBiography.setVisibility(View.VISIBLE);

                                hisBiography.setText(user.getBio());

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
                                    //find address, country, state, city

                                    Geocoder geocoder;
                                    List<Address> addresses;
                                    geocoder = new Geocoder(ViewProfileActivity.this, Locale.getDefault());

                                    try{
                                        addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                        String address = addresses.get(0).getAddressLine(0);//complete address
                                        hisLocationDetails.setText(address);
                                        hisLocationDetails.setVisibility(View.VISIBLE);

                                    }catch (Exception e){
                                        Toast.makeText(ViewProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }catch (NullPointerException ignored){}


                                try{
                                    if(user.getOnlineStatus().equals("online")){
                                        onlineStatus.setVisibility(View.VISIBLE);
                                    }else{
                                        onlineStatus.setVisibility(View.VISIBLE);
                                        onlineStatus.setImageResource(R.drawable.offline_circle);
                                    }
                                }catch (Exception e){
                                    Toast.makeText(ViewProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
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
                int i = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel post = snapshot.getValue(PostModel.class);
                    if(post.getUserID().equals(hisid)){
                        if (post.getPostType().equals("videoPost"))
                            videoCount++;
                    }
                    if(post.getUserID().equals(hisid)){
                        if (!post.getPostType().equals("videoPost"))
                            postCount++;
                    }
                }
                i = postCount + videoCount;

                postsNo.setText(String.valueOf(i));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void checkUserStatus(){

       try{
           FirebaseUser user = firebaseAuth.getCurrentUser();
           if(user != null){
               myID = user.getUid();
           }else{
               startActivity(new Intent(ViewProfileActivity.this, MainActivity.class));
               finish();
           }
       }catch (NullPointerException e){
           Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
       }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myID);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //set last seen time

        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}
