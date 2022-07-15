package com.makepe.blackout.GettingStarted.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class MyPostsFragment extends Fragment {

    //for media posts
    private TextView mediaCounter, seeMore;
    private int mediaCount;
    private RecyclerView mediaRecycler;
    private MediaAdapter mediaAdapter;
    private List<PostModel> mediaList;
    private LinearLayout myMediaArea, myPostsArea;

    //for regular posts
    private TextView postsCounter;
    private RecyclerView postRecycler;
    //private PostAdapter timelineAdapter;
    private TimelineAdapter timelineAdapter;
    private List<PostModel> postList;

    //firebase dependencies
    private DatabaseReference userRef, postsRef;
    private FirebaseUser firebaseUser;

    public MyPostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_posts, container, false);

        //for media posts
        mediaCounter = view.findViewById(R.id.MediaNo);
        seeMore = view.findViewById(R.id.viewMyMedia);
        mediaRecycler = view.findViewById(R.id.myMediaRecycler);
        myMediaArea = view.findViewById(R.id.myMediaArea);

        //for regular posts
        postsCounter = view.findViewById(R.id.PostsNo);
        postRecycler = view.findViewById(R.id.myPostsRecycler);
        myPostsArea = view.findViewById(R.id.myPostsArea);

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        postsRef = FirebaseDatabase.getInstance().getReference("SecretPosts");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getMyMedia();
        getMyPosts();

        seeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if(mediaCount != 0) {
                        Intent seeMoreIntent = new Intent(getActivity(), InteractionsActivity.class);
                        seeMoreIntent.putExtra("userID", firebaseUser.getUid());
                        seeMoreIntent.putExtra("interactionType", "seeMoreMedia");
                        startActivity(seeMoreIntent);
                    }else
                        Toast.makeText(getActivity(), "you do not have any media posts", Toast.LENGTH_SHORT).show();
                }catch (NullPointerException ignored){}
            }
        });

        return view;
    }

    private void getMyPosts() {
        postRecycler.setHasFixedSize(true);
        postRecycler.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        postRecycler.setLayoutManager(linearLayoutManager);

        postList = new ArrayList<>();

        timelineAdapter = new TimelineAdapter(getActivity(), postList);
        //timelineAdapter = new PostAdapter(getActivity(), postList);
        postRecycler.setAdapter(timelineAdapter);
        timelineAdapter.notifyDataSetChanged();

        loadPosts();
    }

    private void loadPosts() {
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                int postCounter = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel postModel = snapshot.getValue(PostModel.class);
                    assert postModel != null;

                    if (postModel.getUserID().equals(firebaseUser.getUid()))
                        if (!postModel.getPostType().equals("videoPost")
                                && !postModel.getPostType().equals("sharedVideoPost")
                                && !postModel.getPostType().equals("audioVideoPost")
                                && !postModel.getPostType().equals("sharedAudioTextVideoPost")
                                && !postModel.getPostType().equals("sharedTextAudioVideoPost")
                                && !postModel.getPostType().equals("sharedAudioAudioVideoPost")
                                && !postModel.getPostType().equals("imagePost")
                                && !postModel.getPostType().equals("audioImagePost")){
                            postList.add(postModel);
                            postCounter++;
                        }
                }

                if (postCounter == 0)
                    myPostsArea.setVisibility(View.GONE);

                postsCounter.setText("Posts [" + postCounter + "]");
                Collections.reverse(postList);
                timelineAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMyMedia() {
        mediaRecycler.setHasFixedSize(true);
        mediaRecycler.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false);
        mediaRecycler.setLayoutManager(linearLayoutManager);
        mediaRecycler.setItemAnimator(new DefaultItemAnimator());

        mediaList = new ArrayList<>();

        mediaAdapter = new MediaAdapter(getActivity(), mediaList);
        mediaRecycler.setAdapter(mediaAdapter);

        loadMedia();

    }

    private void loadMedia() {
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mediaList.clear();
                mediaCount = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel modelPost = snapshot.getValue(PostModel.class);
                    assert modelPost != null;

                    if (modelPost.getUserID().equals(firebaseUser.getUid())){
                        if (modelPost.getPostType().equals("imagePost")
                                || modelPost.getPostType().equals("audioImagePost")){
                            mediaList.add(modelPost);
                            mediaCount++;
                        }
                    }
                }

                if (mediaCount == 0)
                    myMediaArea.setVisibility(View.GONE);

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