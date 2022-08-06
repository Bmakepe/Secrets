package com.makepe.blackout.GettingStarted.Fragments;

import static com.makepe.blackout.GettingStarted.OtherClasses.PaginationListener.PAGE_START;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.MediaAdapter;
import com.makepe.blackout.GettingStarted.Adapters.TimelineAdapter;
import com.makepe.blackout.GettingStarted.InAppActivities.InteractionsActivity;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UserPostsFragment extends Fragment {

    private String hisUserID;

    //for his media posts
    private RecyclerView hisMediaRecycler;
    private List<PostModel> mediaList;
    private MediaAdapter mediaAdapter;
    private int mediaCount;
    private TextView mediaCounter, seeMore;
    private LinearLayout hisMediaArea, hisPostsArea;
    private LinearLayoutManager linearLayoutManager;

    //for his normal posts
    private RecyclerView hisPostsRecycler;
    private List<PostModel> postList;
    private TimelineAdapter timelineAdapter;
    private TextView postCounter;

    //firebase dependencies
    private DatabaseReference postReference;

    private int currentPage = PAGE_START;
    private final boolean isLastPage = false;
    private final int totalPage = 10;
    private boolean isLoading = false;

    public UserPostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_his_posts, container, false);

        hisUserID = getArguments().getString("hisUserID");

        //for media posts
        mediaCounter = view.findViewById(R.id.hisMediaNo);
        seeMore = view.findViewById(R.id.viewHisMedia);
        hisMediaRecycler = view.findViewById(R.id.hisMedia);
        hisMediaArea = view.findViewById(R.id.hisMediaArea);

        //for normal posts
        postCounter = view.findViewById(R.id.hisPostsNo);
        hisPostsRecycler = view.findViewById(R.id.hisPostsRecycler);
        hisPostsArea = view.findViewById(R.id.hisPostsArea);

        postReference = FirebaseDatabase.getInstance().getReference("SecretPosts");

        getMedia();
        getPosts();

        seeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if(mediaCount != 0) {
                        Intent seeMoreIntent = new Intent(getActivity(), InteractionsActivity.class);
                        seeMoreIntent.putExtra("userID", hisUserID);
                        seeMoreIntent.putExtra("interactionType", "seeMoreMedia");
                        startActivity(seeMoreIntent);
                    }else
                        Toast.makeText(getActivity(), "you do not have any media posts", Toast.LENGTH_SHORT).show();
                }catch (NullPointerException ignored){}
            }
        });

        return view;
    }

    private void getPosts() {
        hisPostsRecycler.setHasFixedSize(true);
        hisPostsRecycler.setNestedScrollingEnabled(false);
        hisPostsRecycler.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        hisPostsRecycler.setLayoutManager(linearLayoutManager);

        postList = new ArrayList<>();

        timelineAdapter = new TimelineAdapter(getContext(), postList);
        hisPostsRecycler.setAdapter(timelineAdapter);

        loadHisPosts();
    }

    private void loadHisPosts() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                int i = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel postModel = snapshot.getValue(PostModel.class);
                    assert postModel != null;


                    if (postModel.getUserID().equals(hisUserID))
                        if (!postModel.getPostType().equals("imagePost")
                                && !postModel.getPostType().equals("audioImagePost")) {
                            postList.add(postModel);
                            i++;
                        }
                }

                if (i ==0)
                    hisPostsArea.setVisibility(View.GONE);

                postCounter.setText("Posts [" + i + "]");
                Collections.reverse(postList);
                timelineAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getMedia() {
        hisMediaRecycler.setHasFixedSize(true);
        hisMediaRecycler.setNestedScrollingEnabled(false);
        hisMediaRecycler.hasFixedSize();
        linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false);
        hisMediaRecycler.setLayoutManager(linearLayoutManager);
        hisMediaRecycler.setItemAnimator(new DefaultItemAnimator());

        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(hisMediaRecycler);

        mediaList = new ArrayList<>();

        mediaAdapter = new MediaAdapter(getContext(), mediaList);
        hisMediaRecycler.setAdapter(mediaAdapter);

        loadHisMedia();
    }

    private void loadHisMedia() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mediaList.clear();
                mediaCount = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel modelPost = snapshot.getValue(PostModel.class);
                    assert modelPost != null;

                    if (modelPost.getUserID().equals(hisUserID)){
                        if (modelPost.getPostType().equals("imagePost")
                                || modelPost.getPostType().equals("audioImagePost")){
                            mediaList.add(modelPost);
                            mediaCount++;
                        }
                    }

                }

                if (mediaCount == 0){
                    hisMediaArea.setVisibility(View.GONE);
                }

                mediaCounter.setText("Media [" + mediaCount + "]");
                Collections.reverse(mediaList);
                mediaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}