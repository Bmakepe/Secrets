package com.makepe.blackout.GettingStarted.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.makepe.blackout.GettingStarted.Adapters.PostAdapter;
import com.makepe.blackout.GettingStarted.Adapters.StoryAdapter;
import com.makepe.blackout.GettingStarted.InAppActivities.CameraActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.MessagesActivity;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private ProgressBar timelineLoader;

    private ExtendedFloatingActionButton scrollFAB;

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

        homeProPic = view.findViewById(R.id.homePic);
        postRecycler = view.findViewById(R.id.postRecycler);
        storyRecycler = view.findViewById(R.id.viewStoryRecycler);
        homeToolbar = view.findViewById(R.id.timelineToolbar);
        timelineLoader = view.findViewById(R.id.timelineLoader);
        scrollFAB = view.findViewById(R.id.scrollToTheTopBTN);

        ((AppCompatActivity)getActivity()).setSupportActionBar(homeToolbar);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        storyReference = FirebaseDatabase.getInstance().getReference("Story");
        postReference = FirebaseDatabase.getInstance().getReference("Posts");
        followReference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid()).child("following");

        storyList = new ArrayList<>();
        postList = new ArrayList<>();

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

        getUserDetails();
        checkFollowing();

        storyAdapter = new StoryAdapter(getContext(), storyList);
        storyRecycler.setAdapter(storyAdapter);

        postRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                if (dy > 0) { // scrolling down

                    scrollFAB.setVisibility(View.GONE);

                } else if (dy < 0) { // scrolling up

                    scrollFAB.setVisibility(View.VISIBLE);
                }
            }

        });

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

        followReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }
                readAppropriatePosts();
                readStory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readStory() {
        storyReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long timecurrent = System.currentTimeMillis();
                storyList.clear();
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
                Collections.shuffle(storyList);
                storyAdapter.notifyDataSetChanged();
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
                            if (!postModel.getPostType().equals("videoPost")
                                    && !postModel.getPostType().equals("sharedVideoPost")
                                    && !postModel.getPostType().equals("audioVideoPost"))
                                postList.add(postModel);
                    }

                    if (postModel.getUserID().equals(firebaseUser.getUid()))
                        if (!postModel.getPostType().equals("videoPost")
                                && !postModel.getPostType().equals("sharedVideoPost")
                                && !postModel.getPostType().equals("audioVideoPost"))
                            postList.add(postModel);

                }

                Collections.shuffle(postList);
                postRecycler.setAdapter(new PostAdapter(getActivity(), postList));
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
                    if (user.getUSER_ID().equals(firebaseUser.getUid())){
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
                startActivity(new Intent(getActivity(), CameraActivity.class));
                break;

            default:
                Toast.makeText(getContext(), "Unknown Selection", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}