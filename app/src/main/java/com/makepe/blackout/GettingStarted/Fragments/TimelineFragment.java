package com.makepe.blackout.GettingStarted.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.PostAdapter;
import com.makepe.blackout.GettingStarted.Adapters.StoryAdapter;
import com.makepe.blackout.GettingStarted.InAppActivities.ChatListActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.PostActivity;
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

    //for stories
    private RecyclerView storyRecycler;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;

    //for followers
    private List<String> followingList;

    //for posts
    private RecyclerView postRecycler;
    private ArrayList<PostModel> postList;
    private PostAdapter adapterPost;


    public TimelineFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        homeProPic = view.findViewById(R.id.homePic);
        postRecycler = view.findViewById(R.id.postRecycler);
        storyRecycler = view.findViewById(R.id.viewStoryRecycler);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        storyReference = FirebaseDatabase.getInstance().getReference("Story");
        postReference = FirebaseDatabase.getInstance().getReference("Posts");
        followReference = FirebaseDatabase.getInstance().getReference("Follow").child(firebaseUser.getUid()).child("following");

        storyList = new ArrayList<>();
        postList = new ArrayList<>();

        getUserDetails();
        prepareRecyclerViews();
        checkFollowing();
        readStory();
        readVideoPosts();
        readImagePosts();
        readSharedPosts();
        readTextPosts();
        getMyPosts();

        adapterPost = new PostAdapter(getActivity(), postList);
        Collections.shuffle(postList);
        postRecycler.setAdapter(adapterPost);

        storyAdapter = new StoryAdapter(getContext(), storyList);
        storyRecycler.setAdapter(storyAdapter);

        view.findViewById(R.id.newPostBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PostActivity.class));
            }
        });

        view.findViewById(R.id.homeInboxBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ChatListActivity.class));
            }
        });

        return view;
    }

    private void getMyPosts() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    if (postModel.getUserID().equals(firebaseUser.getUid()))
                        postList.add(postModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readTextPosts() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    for (String ID : followingList){
                        if (postModel.getUserID().equals(ID) && postModel.getPostType().equals("textPost"))
                            postList.add(postModel);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readSharedPosts() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    for (String ID : followingList){
                        if (postModel.getUserID().equals(ID)){
                            if ( postModel.getPostType().equals("sharedTextPost")
                                    ||postModel.getPostType().equals("sharedImagePost")
                                    ||postModel.getPostType().equals("sharedVideoPost")){
                                postList.add(postModel);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readImagePosts() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    for (String ID : followingList){
                        if (postModel.getPostType().equals("imagePost") && postModel.getUserID().equals(ID))
                            postList.add(postModel);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void prepareRecyclerViews() {

        postRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(false);
        layoutManager.setReverseLayout(true);
        postRecycler.setLayoutManager(layoutManager);


        storyRecycler.setHasFixedSize(true);
        storyRecycler.setNestedScrollingEnabled(false);
        storyRecycler.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
    }

    private void readVideoPosts() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    for (String id : followingList){
                        if (postModel.getPostType().equals("videoPost") && postModel.getUserID().equals(id))
                            postList.add(postModel);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readStory() {
        storyReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long timecurrent = System.currentTimeMillis();
                storyList.clear();
                storyList.add(new Story("", 0, 0,"",
                        FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                        FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(),
                        ""));

                for(String id : followingList){

                    int countStory = 0;
                    Story story = null;

                    for(DataSnapshot snapshot : dataSnapshot.child(id).getChildren()){
                        story = snapshot.getValue(Story.class);
                        if(timecurrent>story.getTimestart() && timecurrent < story.getTimeend()){
                            countStory++;
                        }
                    }
                    if(countStory > 0){
                        storyList.add(story);
                    }
                }

                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

                //loadPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserDetails() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);

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
}