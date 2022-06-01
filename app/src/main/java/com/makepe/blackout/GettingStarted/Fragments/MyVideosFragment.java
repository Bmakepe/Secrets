package com.makepe.blackout.GettingStarted.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.VideoItemAdapter;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyVideosFragment extends Fragment {

    private List<PostModel> videoList;
    private RecyclerView videoRecycler;
    private VideoItemAdapter videoAdapter;

    private FirebaseUser firebaseUser;
    private DatabaseReference postReference;

    public MyVideosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_videos, container, false);

        videoRecycler = view.findViewById(R.id.videoRecycler);

        videoList = new ArrayList<>();

        postReference = FirebaseDatabase.getInstance().getReference("Posts");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        videoRecycler.hasFixedSize();
        videoRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3, LinearLayoutManager.VERTICAL, false));

        getMyVideos();

        return view;
    }

    private void getMyVideos() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                videoList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    assert postModel != null;
                    if (postModel.getUserID().equals(firebaseUser.getUid())){
                        if (postModel.getPostType().equals("videoPost")
                                || postModel.getPostType().equals("sharedVideoPost")
                                || postModel.getPostType().equals("audioVideoPost"))
                            videoList.add(postModel);
                    }
                }
                videoAdapter = new VideoItemAdapter(videoList, getActivity());
                Collections.reverse(videoList);
                videoRecycler.setAdapter(videoAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}