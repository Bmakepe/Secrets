package com.makepe.blackout.GettingStarted.Fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.StoryAdapter;
import com.makepe.blackout.GettingStarted.Adapters.TimelineAdapter;
import com.makepe.blackout.GettingStarted.InAppActivities.MessagesActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.PostActivity;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.Permissions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class TimelineFragment extends Fragment {

    private FirebaseUser firebaseUser;
    private DatabaseReference userRef, followReference, storyReference, postReference;

    private CircleImageView homeProPic;
    private Toolbar homeToolbar;

    //for stories
    private RecyclerView storyRecycler;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;

    //for followers
    private List<String> followingList;

    //for posts
    private RecyclerView postRecycler;
    private ArrayList<PostModel> postList;
    private TimelineAdapter postAdapter;

    private ProgressBar timelineLoader;
    private Permissions permissions;

    private ExtendedFloatingActionButton scrollFAB;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    public TimelineFragment() {
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
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        ButterKnife.bind(getActivity());

        homeProPic = view.findViewById(R.id.homePic);
        postRecycler = view.findViewById(R.id.postRecycler);
        storyRecycler = view.findViewById(R.id.viewStoryRecycler);
        homeToolbar = view.findViewById(R.id.timelineToolbar);
        timelineLoader = view.findViewById(R.id.timelineLoader);
        scrollFAB = view.findViewById(R.id.scrollToTheTopBTN);
        //refreshLayout = view.findViewById(R.id.timelineRefresher);

        ((AppCompatActivity)getActivity()).setSupportActionBar(homeToolbar);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        storyReference = FirebaseDatabase.getInstance().getReference("Story");
        postReference = FirebaseDatabase.getInstance().getReference("SecretPosts");
        followReference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid()).child("following");

        storyList = new ArrayList<>();
        postList = new ArrayList<>();
        permissions = new Permissions(getContext());
        permissions.verifyPermissions();

        //for post recycler view
        postRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(false);
        layoutManager.setReverseLayout(true);
        postRecycler.setLayoutManager(layoutManager);

        //for stories recycler view
        storyRecycler.setHasFixedSize(true);
        storyRecycler.setNestedScrollingEnabled(false);
        storyRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        storyAdapter = new StoryAdapter(getContext(), storyList);
        storyRecycler.setAdapter(storyAdapter);

        getUserDetails();
        checkFollowing();

        scrollFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postRecycler.scrollToPosition(0);
            }
        });

        return view;
    }

    private void checkFollowing() {
        followingList = new ArrayList<>();

        followReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }
                readStory();
                readAppropriatePosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }

    private void readStory() {
        storyReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long timecurrent = System.currentTimeMillis();
                storyList.clear();
                //storyList.add(new Story());
                storyList.add(new Story("", firebaseUser.getUid(),
                        "", "", "", "", "",
                        0, 0, 0, 0));

                for(String id : followingList){

                    int countStory = 0;
                    Story story = null;

                    for(DataSnapshot snapshot : dataSnapshot.child(id).getChildren()){
                        story = snapshot.getValue(Story.class);
                        if(timecurrent>story.getTimeStart() && timecurrent < story.getTimeEnd()){
                            countStory++;
                        }
                    }

                    if(countStory > 0){
                        storyList.add(story);
                    }
                }
                //Collections.shuffle(storyList);
                storyAdapter.notifyDataSetChanged();

                //readAppropriatePosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readAppropriatePosts() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    assert postModel != null;
                    for (String ID : followingList){
                        if (postModel.getUserID().equals(ID))
                            postList.add(postModel);

                    }

                    if (postModel.getUserID().equals(firebaseUser.getUid()))
                        postList.add(postModel);

                }

                Collections.shuffle(postList);
                postAdapter = new TimelineAdapter(getActivity(), postList);
                postRecycler.setAdapter(postAdapter);
                timelineLoader.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void getUserDetails() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUserID().equals(firebaseUser.getUid())){
                        try{
                            Picasso.get().load(user.getImageURL()).into(homeProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(homeProPic);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.timeline_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.inbox:
                startActivity(new Intent(getActivity(), MessagesActivity.class));
                break;
            case R.id.newPostBTN:
                startActivity(new Intent(getActivity(), PostActivity.class));

                //throw new RuntimeException(("Test Crash"));

                break;

            default:
                Toast.makeText(getContext(), "Unknown Selection", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPermission();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkPermission();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;

        //request camera permission if it has not been granted
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_PERMISSION_REQUEST_CODE:
                    if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(getContext(), "Permission has been granted", Toast.LENGTH_SHORT).show();

                    }else
                        Toast.makeText(getContext(), "Permission is not granted", Toast.LENGTH_SHORT).show();
                    break;
        }
    }

}