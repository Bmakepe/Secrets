package com.makepe.blackout.GettingStarted.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.MediaAdapter;
import com.makepe.blackout.GettingStarted.Adapters.PostAdapter;
import com.makepe.blackout.GettingStarted.InAppActivities.InteractionsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HisPostsFragment extends Fragment {

    private String hisUserID;

    //for his media posts
    private RecyclerView hisMediaRecycler;
    private List<PostModel> mediaList;
    private MediaAdapter mediaAdapter;
    private int mediaCount;
    private TextView mediaCounter, seeMore;

    //for his normal posts
    private RecyclerView hisPostsRecycler;
    private List<PostModel> postList;
    private PostAdapter timelineAdapter;
    private TextView postCounter;

    //firebase dependencies
    private DatabaseReference postReference, userRef;

    public HisPostsFragment() {
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

        //for normal posts
        postCounter = view.findViewById(R.id.hisPostsNo);
        hisPostsRecycler = view.findViewById(R.id.hisPostsRecycler);

        postReference = FirebaseDatabase.getInstance().getReference("Posts");
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        getHisMedia();
        getHisPosts();

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

    private void getHisPosts() {
        hisPostsRecycler.setHasFixedSize(true);
        hisPostsRecycler.setNestedScrollingEnabled(false);
        hisPostsRecycler.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        hisPostsRecycler.setLayoutManager(linearLayoutManager);

        postList = new ArrayList<>();

        timelineAdapter = new PostAdapter(getContext(), postList);
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
                    PostModel modelPost = snapshot.getValue(PostModel.class);
                    assert modelPost != null;
                    if(modelPost.getPostImage().equals("noImage")
                            && hisUserID.equals(modelPost.getUserID())
                            && !modelPost.getPostType().equals("videoPost")
                            && !modelPost.getPostType().equals("sharedVideoPost")
                            && !modelPost.getPostType().equals("audioVideoPost")){
                        postList.add(modelPost);
                        i++;
                    }

                }
                postCounter.setText("Posts [" + i + "]");
                Collections.reverse(postList);
                timelineAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getHisMedia() {
        hisMediaRecycler.setHasFixedSize(true);
        hisMediaRecycler.setNestedScrollingEnabled(false);
        hisMediaRecycler.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false);
        hisMediaRecycler.setLayoutManager(linearLayoutManager);
        hisMediaRecycler.setItemAnimator(new DefaultItemAnimator());

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
                    if(modelPost.getPostType().equals("imagePost")
                            && hisUserID.equals(modelPost.getUserID())){
                        mediaList.add(modelPost);
                        mediaCount++;
                    }

                }
                mediaCounter.setText("Media [" + mediaCount + "]");
                Collections.reverse(mediaList);
                mediaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}