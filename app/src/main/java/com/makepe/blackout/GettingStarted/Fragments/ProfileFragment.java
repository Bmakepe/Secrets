package com.makepe.blackout.GettingStarted.Fragments;


import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makepe.blackout.GettingStarted.Adapters.MediaAdapter;
import com.makepe.blackout.GettingStarted.Adapters.NotificationAdapter;
import com.makepe.blackout.GettingStarted.Adapters.PostAdapter;
import com.makepe.blackout.GettingStarted.InAppActivities.ConnectionsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ContactsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.EditProfileActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenImageActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.NotificationsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.SavedPostsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.SettingsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.MainActivity;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.NotiModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ViewPagerAdapter;
import com.makepe.blackout.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    //view from xml file
    private ImageView avatarIv, coverIv, profileOnlineStatus, optionsMenu, notificationsBTN;

    // ImageView profileMenuIcon;
    private TextView nameTV, Biography, postNo, followersNo, followingNo, locationDetails;
    private RelativeLayout followersListBTN, followingListBTN;

    private ProgressDialog pd;
    private ProgressBar coverLoader, picLoader;
    private String uid;
    private double latitude, longitude;

    //firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef, postReference;

    private int postCount = 0, videoCount = 0;

    //for fragments
    TabLayout profileTabs;
    ViewPager profilePager;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //initiate xml views
        avatarIv = view.findViewById(R.id.profilePicture);
        nameTV = view.findViewById(R.id.profileUsername);
        coverIv = view.findViewById(R.id.coverImage);
        Biography = view.findViewById(R.id.profileBio);
        profileOnlineStatus = view.findViewById(R.id.profileOnlineStatus);
        postNo = view.findViewById(R.id.posts);
        followersNo = view.findViewById(R.id.followers);
        followingNo = view.findViewById(R.id.following);
        optionsMenu = view.findViewById(R.id.options);
        notificationsBTN = view.findViewById(R.id.notificationBTN);
        coverLoader = view.findViewById(R.id.coverPicLoader);
        picLoader = view.findViewById(R.id.proPicLoader);
        followersListBTN = view.findViewById(R.id.followersListBTN);
        followingListBTN  = view.findViewById(R.id.followingListBTN);
        profileTabs = view.findViewById(R.id.profileTabs);
        profilePager  = view.findViewById(R.id.profileViewPager);
        locationDetails  = view.findViewById(R.id.locationDetails);

        //initiate firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        postReference = FirebaseDatabase.getInstance().getReference("Posts");

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        uid = prefs.getString("profileid", "none");

        pd = new ProgressDialog(getActivity());

        checkUserStatus();
        getUserDetails();
        getNrPosts();
        getFollowers();

        //setting up fragments
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.addFragment(new MyPostsFragment(), "Posts [" + postCount + "]");
        viewPagerAdapter.addFragment(new MyVideosFragment(), "Videos [" + videoCount + ']');
        profilePager.setAdapter(viewPagerAdapter);
        profileTabs.setupWithViewPager(profilePager);

        followersListBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent followersIntent = new Intent(getActivity(), ConnectionsActivity.class);
                followersIntent.putExtra("UserID", firebaseUser.getUid());
                followersIntent.putExtra("Interaction", "Followers");
                startActivity(followersIntent);
            }
        });

        followingListBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent followingIntent = new Intent(getActivity(), ConnectionsActivity.class);
                followingIntent.putExtra("UserID", firebaseUser.getUid());
                followingIntent.putExtra("Interaction", "Following");
                startActivity(followingIntent);

            }
        });

        avatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //click profile imageview to change profile / cover pic
                Intent intent1 = new Intent(getActivity(), FullScreenImageActivity.class);
                intent1.putExtra("itemID", firebaseUser.getUid());
                intent1.putExtra("reason", "userImage");
                startActivity(intent1);
            }
        });

        coverIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent1 = new Intent(getActivity(), FullScreenImageActivity.class);
                intent1.putExtra("itemID", firebaseUser.getUid());
                intent1.putExtra("reason", "coverImage");
                startActivity(intent1);
            }
        });

        optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });

        notificationsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), NotificationsActivity.class));
            }
        });

        view.findViewById(R.id.editProfBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), EditProfileActivity.class));
            }
        });

        locationDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String address = "https://maps.google.com/maps?saddr=" + latitude + "," + longitude;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                startActivity(intent);
            }
        });

        return view;
    }

    private void getUserDetails() {
        pd.setMessage("Loading Profile");
        pd.show();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //get firebase data and set to required fields appropriately
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUSER_ID().equals(firebaseUser.getUid())){

                        nameTV.setText(user.getUsername());
                        Biography.setText(user.getBio());

                        try{
                            Picasso.get().load(user.getImageURL()).into(avatarIv);
                            Picasso.get().load(user.getCoverURL()).into(coverIv);
                        }catch (NullPointerException ignored){}

                        if(user.getOnlineStatus().equals("online")){
                            profileOnlineStatus.setVisibility(View.VISIBLE);
                        }

                        try{
                            latitude = user.getLatitude();
                            longitude = user.getLongitude();
                            //find address, country, state, city

                            Geocoder geocoder;
                            List<Address> addresses;
                            geocoder = new Geocoder(getContext(), Locale.getDefault());

                            try{
                                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                String address = addresses.get(0).getAddressLine(0);//complete address
                                locationDetails.setText(address);
                                locationDetails.setVisibility(View.VISIBLE);

                            }catch (Exception e){
                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }catch (NullPointerException ignored){}

                        pd.dismiss();
                        picLoader.setVisibility(View.GONE);
                        coverLoader.setVisibility(View.GONE);

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*private void iniFollowDialog() {
        followDialog = new Dialog(getActivity());
        followDialog.setContentView(R.layout.connections_list);
        followDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        followDialog.getWindow().setLayout(android.widget.Toolbar.LayoutParams.MATCH_PARENT, android.widget.Toolbar.LayoutParams.MATCH_PARENT);
        followDialog.getWindow().getAttributes().gravity = Gravity.BOTTOM;

        //for Connections Pop Up
        ImageView connectBackBTN = followDialog.findViewById(R.id.connectBackBTN);
        TabLayout connectionsTABS = followDialog.findViewById(R.id.tabs);

        connectionsTABS.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        Toast.makeText(getContext(), "Followers", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(getContext(), "Following", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        connectBackBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followDialog.dismiss();
            }
        });

        followDialog.findViewById(R.id.connectionsSearchBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDialog.show();
            }
        });
    }//for displaying both followers and following lists*/

    private void getFollowers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(uid).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followersNo.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });//function to get number of followers

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(uid).child("following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingNo.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });//function to get number of user i follow
    }//function for followers and users i follow

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(getActivity(), optionsMenu, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0,0,"Movements");
        popupMenu.getMenu().add(Menu.NONE, 1,0,"Saved Posts");
        popupMenu.getMenu().add(Menu.NONE, 2,0,"Settings");
        popupMenu.getMenu().add(Menu.NONE, 3,0,"Log Out");


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                switch (item.getItemId()){
                    case 0:
                        Toast.makeText(getActivity(), "you will be able to see and create movements", Toast.LENGTH_SHORT).show();
                        break;

                    case 1:
                        startActivity(new Intent(getActivity(), SavedPostsActivity.class));
                        Toast.makeText(getActivity(), "Saved Posts", Toast.LENGTH_SHORT).show();
                        break;

                    case 2:
                        startActivity(new Intent(getActivity(), SettingsActivity.class));
                        break;

                    case 3:
                        Toast.makeText(getActivity(), "Log out", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        Toast.makeText(getActivity(), "illegal selection", Toast.LENGTH_SHORT).show();

                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void getNrPosts(){
       postReference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               int i = 0;
               for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                   PostModel post = snapshot.getValue(PostModel.class);
                   assert post != null;

                   if (post.getUserID().equals(firebaseUser.getUid())){
                       if (post.getPostType().equals("videoPost"))
                           videoCount++;
                   }

                   if (post.getUserID().equals(firebaseUser.getUid())){
                       if (!post.getPostType().equals("videoPost"))
                           postCount++;
                   }
               }

               i = postCount + videoCount;

               try{
                   postNo.setText(i + " ");
               }catch (NullPointerException ignored){ }

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });
   }

    private void checkUserStatus(){
        //get firebase user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            uid = user.getUid();
        }else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

}
