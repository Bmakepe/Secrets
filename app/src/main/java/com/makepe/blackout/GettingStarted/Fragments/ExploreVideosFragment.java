package com.makepe.blackout.GettingStarted.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

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

public class ExploreVideosFragment extends Fragment {

    private List<PostModel> videoList;
    private ViewPager2 videoPager;
    private ProgressBar videoLoader;

    private DatabaseReference videoReference;

    public ExploreVideosFragment() {
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
        View view = inflater.inflate(R.layout.fragment_explore_videos, container, false);

        videoPager = view.findViewById(R.id.videoPager);
        videoLoader = view.findViewById(R.id.videoPostLoader);

        videoReference = FirebaseDatabase.getInstance().getReference("Posts");
        videoList = new ArrayList<>();

        getVideos();

        return view;
    }

    private void getVideos() {
        videoReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                videoList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel videos = ds.getValue(PostModel.class);

                    assert videos != null;
                    if (videos.getPostType().equals("videoPost")
                            ||videos.getPostType().equals("audioVideoPost")
                            ||videos.getPostType().equals("sharedVideoPost"))
                        videoList.add(videos);

                    Collections.shuffle(videoList);
                    videoPager.setAdapter(new VideoFootageAdapter(videoList, getActivity()));

                }
                videoLoader.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}