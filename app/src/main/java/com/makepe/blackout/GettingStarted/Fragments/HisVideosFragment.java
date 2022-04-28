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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.VideoItemAdapter;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class HisVideosFragment extends Fragment {

    private String hisUserID;

    private RecyclerView videosRecycler;
    private List<PostModel> videoList;
    private VideoItemAdapter videoAdapter;

    private DatabaseReference videoReference;


    public HisVideosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_his_videos, container, false);

        hisUserID = getArguments().getString("hisUserID");

        videosRecycler = view.findViewById(R.id.hisVideosRecycler);

        videoReference = FirebaseDatabase.getInstance().getReference("Posts");

        videoList = new ArrayList<>();

        videosRecycler.hasFixedSize();
        videosRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3, LinearLayoutManager.VERTICAL, false));

        getHisVideos();

        return view;
    }

    private void getHisVideos() {
        videoReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                videoList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel videos = ds.getValue(PostModel.class);

                    if (videos.getUserID().equals(hisUserID)){
                        if (videos.getPostType().equals("videoPost")
                                || videos.getPostType().equals("sharedVideoPost")){
                            videoList.add(videos);
                        }
                    }
                    videoAdapter = new VideoItemAdapter(videoList, getActivity());
                    Collections.reverse(videoList);
                    videosRecycler.setAdapter(videoAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}