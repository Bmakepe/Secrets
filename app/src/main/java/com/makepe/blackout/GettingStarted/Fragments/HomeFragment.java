package com.makepe.blackout.GettingStarted.Fragments;


import android.app.ProgressDialog;
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
import com.makepe.blackout.GettingStarted.Adapters.StoryAdapter;
import com.makepe.blackout.GettingStarted.Adapters.PostAdapter;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    //for stories
    private RecyclerView storyRecycler;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;

    //for posts
    private RecyclerView postRecycler;
    private ArrayList<PostModel> postList;
    private PostAdapter adapterPost;

    //for followers
    private List<String> followingList;

    private FirebaseUser firebaseUser;
    private DatabaseReference postReference, followReference, storyReference;

    private ProgressDialog homeLoader;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        postRecycler = view.findViewById(R.id.postRecycler);

        homeLoader = new ProgressDialog(getContext());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storyReference = FirebaseDatabase.getInstance().getReference("Story");
        postReference = FirebaseDatabase.getInstance().getReference("Posts");
        followReference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid())
                .child("following");

        postRecycler.setHasFixedSize(true);
        postRecycler.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postRecycler.setLayoutManager(layoutManager);

        storyRecycler = view.findViewById(R.id.viewStoryRecycler);
        storyRecycler.setHasFixedSize(true);
        storyRecycler.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        storyRecycler.setLayoutManager(linearLayoutManager);

        storyList = new ArrayList<>();
        postList = new ArrayList<>();

        checkFollowing();
        readStory();

        storyAdapter = new StoryAdapter(getContext(), storyList);
        storyRecycler.setAdapter(storyAdapter);

        return view;
    }

    private void checkFollowing(){
        homeLoader.setMessage("Loading...");
        homeLoader.show();
        followingList = new ArrayList<>();

        followReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }

                loadPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPosts() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    PostModel modelPost = ds.getValue(PostModel.class);

                   // if(!modelPost.getPostType().equals("videoPost")){

                        for(String id : followingList){
                            assert modelPost != null;
                            if(modelPost.getUserID().equals(id)) {
                                if (!modelPost.getPostType().equals("videoPost")
                                || !modelPost.getPostType().equals("sharedVideoPost"))
                                    postList.add(modelPost);
                            }
                        }

                        if(modelPost.getUserID().equals(firebaseUser.getUid())){
                            postList.add(modelPost);
                        }
                   // }

                    //Collections.shuffle(postList);
                    adapterPost = new PostAdapter(getActivity(), postList);
                    postRecycler.setAdapter(adapterPost);
                    homeLoader.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readStory(){
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

}
