package com.makepe.blackout.GettingStarted.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.VideoAdapter;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DynamicVideoFragment extends Fragment {

    private String tabTitle;
    private int position;

    private List<String> followingList;
    private List<PostModel> videoList;
    private ArrayList<String> videoCategoryList;
    private ViewPager2 videosPager;
    private ProgressBar videoLoader;

    private VideoAdapter.HolderVideos videos;

    private DatabaseReference videoReference, followReference;
    private FirebaseUser firebaseUser;

    public DynamicVideoFragment() {
        // Required empty public constructor
    }

    public static Fragment addFrag(int position, ArrayList<String> videoTabList) {
        DynamicVideoFragment fragment = new DynamicVideoFragment();
        Bundle args = new Bundle();
        args.putInt("tabPosition", position);
        args.putStringArrayList("tabTitles", videoTabList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dynamic_video, container, false);

        videosPager = view.findViewById(R.id.dynamicVideosPager);
        videoLoader = view.findViewById(R.id.dynamicVideosLoader);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        videoReference = FirebaseDatabase.getInstance().getReference("SecretVideos");
        followReference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid())
                .child("following");

        videos = new VideoAdapter.HolderVideos(view);
        videoList = new ArrayList<>();
        videoCategoryList = new ArrayList<>();

        position = getArguments().getInt("tabPosition", 0);
        videoCategoryList = getArguments().getStringArrayList("tabTitles");

        switch (videoCategoryList.get(position)){
            case "Following":
                checkFollowing();
                break;

            case "Explore":
                getPublicVideos();
                break;

            default:
                Toast.makeText(getContext(), "Unknown video category detected: " + videoCategoryList.get(position), Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void getPublicVideos() {
        videoReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    videoList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        PostModel videos = ds.getValue(PostModel.class);

                        if (!videos.getPostPrivacy().equals("Private"))
                            videoList.add(videos);
                    }
                    Collections.shuffle(videoList);
                    videosPager.setAdapter(new VideoAdapter(videoList, getActivity()));
                    videoLoader.setVisibility(View.GONE);
                }else{
                    Toast.makeText(getContext(), "no Videos", Toast.LENGTH_SHORT).show();
                    videoLoader.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkFollowing() {
        followingList = new ArrayList<>();
        followReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    followingList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        followingList.add(ds.getKey());
                    }
                    loadFollowingVideos();
                }else{
                    Toast.makeText(getContext(), "You have no videos to watch", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadFollowingVideos() {
        videoReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    videoList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        PostModel postModel = ds.getValue(PostModel.class);

                        for (String id : followingList){
                            if (postModel.getUserID().equals(id)){
                                videoList.add(postModel);
                            }
                        }
                        Collections.shuffle(videoList);
                        videosPager.setAdapter(new VideoAdapter(videoList, getActivity()));
                        videoLoader.setVisibility(View.GONE);
                    }
                }else{
                    Toast.makeText(getContext(), "No Videos", Toast.LENGTH_SHORT).show();
                    videoLoader.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (this.isVisible()){
            if (!isVisibleToUser){
                //stop or pause video
                if(videos.postVideoView != null)
                    videos.postVideoView.pause();
                else
                    Toast.makeText(getContext(), "Could not pause video", Toast.LENGTH_SHORT).show();

                Toast.makeText(getContext(), "videos in " + videoCategoryList.get(position) + " must stop or pause", Toast.LENGTH_SHORT).show();
            }else{
                //play video
                if(videos.postVideoView != null)
                    videos.postVideoView.start();
                else
                    Toast.makeText(getContext(), "Could not play video", Toast.LENGTH_SHORT).show();

                Toast.makeText(getContext(), "videos in " + videoCategoryList.get(position) + " must start playing", Toast.LENGTH_SHORT).show();
            }
        }
    }

}