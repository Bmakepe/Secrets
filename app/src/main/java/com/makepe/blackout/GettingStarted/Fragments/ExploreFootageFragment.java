package com.makepe.blackout.GettingStarted.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.VideoFootageAdapter;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExploreFootageFragment extends Fragment {

    private List<PostModel> videoList;
    private ViewPager2 videoPager;
    private ProgressBar videoLoader;

    private FirebaseUser firebaseUser;
    private DatabaseReference postReference, followReference;

    private List<String> followingList;

    public ExploreFootageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore_footage, container, false);

        videoPager = view.findViewById(R.id.followingVideosPager);
        videoLoader = view.findViewById(R.id.followingVideosLoader);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        postReference = FirebaseDatabase.getInstance().getReference("Posts");
        followReference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid())
                .child("following");

        videoList = new ArrayList<>();
        followingList = new ArrayList<>();

        checkFollowing();

        return view;
    }

    private void checkFollowing() {
        followReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    followingList.add(ds.getKey());
                }
                loadVideos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadVideos() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                videoList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    for (String id : followingList){
                        assert postModel != null;
                        if (postModel.getUserID().equals(id)){
                            if (postModel.getPostType().equals("videoPost")
                                        || postModel.getPostType().equals("sharedVideoPost")){
                                videoList.add(postModel);
                            }
                        }
                    }
                    Collections.shuffle(videoList);
                    videoPager.setAdapter(new VideoFootageAdapter(videoList, getActivity()));
                    videoLoader.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}