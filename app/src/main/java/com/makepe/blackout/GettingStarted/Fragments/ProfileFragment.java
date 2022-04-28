package com.makepe.blackout.GettingStarted.Fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.makepe.blackout.GettingStarted.InAppActivities.ConnectionsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.EditProfileActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenImageActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.MessagesActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.MovementsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.NotificationsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.PostActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.SavedPostsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.SettingsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.StoryActivity;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.GettingStarted.RegisterActivity;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ViewPagerAdapter;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    //view from xml file
    private ImageView coverIv;
    private CircleImageView avatarIv;
    private Toolbar profileToolbar;

    // ImageView profileMenuIcon;
    private TextView nameTV, Biography, postNo, followersNo, followingNo, locationDetails, aboutTV;
    private RelativeLayout followersListBTN, followingListBTN;
    private LinearLayout profileLocationArea;

    private ProgressDialog pd;
    private ProgressBar coverLoader, picLoader;
    private String uid;
    private double latitude, longitude;

    //firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef, postReference, followersReference, followingReference, storyReference;

    private int postCount = 0, videoCount = 0, totalCount = 0;

    //for fragments
    private TabLayout profileTabs;
    private ViewPager profilePager;

    private boolean activeStories = false;

    private UniversalFunctions universalFunctions;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
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
        postNo = view.findViewById(R.id.posts);
        followersNo = view.findViewById(R.id.followers);
        followingNo = view.findViewById(R.id.following);
        profileToolbar = view.findViewById(R.id.profileToolbar);
        coverLoader = view.findViewById(R.id.coverPicLoader);
        picLoader = view.findViewById(R.id.proPicLoader);
        followersListBTN = view.findViewById(R.id.followersListBTN);
        followingListBTN  = view.findViewById(R.id.followingListBTN);
        profileTabs = view.findViewById(R.id.profileTabs);
        profilePager  = view.findViewById(R.id.profileViewPager);
        locationDetails  = view.findViewById(R.id.locationDetails);
        aboutTV  = view.findViewById(R.id.profileAboutTV);
        profileLocationArea  = view.findViewById(R.id.profileLocationArea);

        ((AppCompatActivity)getActivity()).setSupportActionBar(profileToolbar);

        //initiate firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        postReference = FirebaseDatabase.getInstance().getReference("Posts");
        followersReference = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("followers");
        followingReference = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following");
        storyReference = FirebaseDatabase.getInstance().getReference("Story").child(firebaseUser.getUid());

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        uid = prefs.getString("profileid", "none");

        pd = new ProgressDialog(getActivity());

        universalFunctions = new UniversalFunctions(getContext());

        getUserDetails();
        getFollowers();
        getNrPosts();
        universalFunctions.checkActiveStories(avatarIv, firebaseUser.getUid());

        //setting up fragments
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.addFragment(new MyPostsFragment(), "Posts [" + postCount + "]");
        viewPagerAdapter.addFragment(new MyVideosFragment(), "Videos [" + videoCount + "]");
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
                if (avatarIv.getTag().equals("storyActive")){
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Picture",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent1 = new Intent(getActivity(), FullScreenImageActivity.class);
                                    intent1.putExtra("itemID", firebaseUser.getUid());
                                    intent1.putExtra("reason", "userImage");
                                    startActivity(intent1);
                                    dialogInterface.dismiss();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "View Story",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(getActivity(), StoryActivity.class);
                                    intent.putExtra("userid", firebaseUser.getUid());
                                    startActivity(intent);
                                    dialogInterface.dismiss();
                                }
                            });
                    alertDialog.show();

                }else if (avatarIv.getTag().equals("noStories")){
                    Intent intent1 = new Intent(getActivity(), FullScreenImageActivity.class);
                    intent1.putExtra("itemID", firebaseUser.getUid());
                    intent1.putExtra("reason", "userImage");
                    startActivity(intent1);
                }
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

                    assert user != null;
                    if (user.getUSER_ID().equals(firebaseUser.getUid())){

                        nameTV.setText(user.getUsername());
                        Biography.setText(user.getBio());
                        aboutTV.setText("About " + user.getUsername());

                        try{
                            Picasso.get().load(user.getImageURL()).into(avatarIv);
                            Picasso.get().load(user.getCoverURL()).into(coverIv);
                        }catch (NullPointerException ignored){}


                        try{
                            latitude = user.getLatitude();
                            longitude = user.getLongitude();
                            universalFunctions.findAddress(latitude, longitude, locationDetails, profileLocationArea);
                        }catch (Exception ignored){}

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

    private void getFollowers(){

        followersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followersNo.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });//function to get number of followers

        followingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingNo.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }//function for followers and users i follow

    private void getNrPosts(){
       postReference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               int totalCount = 0;
               for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                   PostModel post = snapshot.getValue(PostModel.class);
                   assert post != null;

                   if (post.getUserID().equals(firebaseUser.getUid())
                           && post.getPostType().equals("videoPost"))
                       videoCount++;
                   else if (post.getUserID().equals(firebaseUser.getUid())
                           && !post.getPostType().equals("videoPost"))
                       postCount++;

               }
               totalCount = videoCount + postCount;
               postNo.setText(String.valueOf(totalCount));


           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });
   }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
        /*menu.add(0, 1, 1, menuIconWithText(getResources().getDrawable(R.drawable.ic_baseline_arrow_forward_24), getResources().getString(R.string.saved_posts)));
        menu.add(0, 2, 2, menuIconWithText(getResources().getDrawable(R.drawable.ic_baseline_arrow_forward_24), getResources().getString(R.string.saved_posts)));
        menu.add(0, 3, 3, menuIconWithText(getResources().getDrawable(R.drawable.ic_baseline_arrow_forward_24), getResources().getString(R.string.saved_posts)));*/

        super.onCreateOptionsMenu(menu, inflater);
    }

    private CharSequence menuIconWithText(Drawable drawable, String title) {
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        SpannableString sb = new SpannableString("    " + title);
        ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.inboxBTN:
                startActivity(new Intent(getActivity(), MessagesActivity.class));
                break;
            case R.id.savedPostsItem:
                startActivity(new Intent(getActivity(), SavedPostsActivity.class));
                break;
            case R.id.goToSettings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;
            case R.id.logOut:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), RegisterActivity.class));
                break;
            default:
                Toast.makeText(getContext(), "Unknown Selection", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

}
