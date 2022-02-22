package com.makepe.blackout.GettingStarted.Fragments;

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
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.List;

public class VideosFragment extends Fragment {

    //for posts
    private RecyclerView postRecycler;
    private ArrayList<PostModel> postList;
    private PostAdapter adapterPost;

    //for followers
    private List<String> followingList;

    private FirebaseUser firebaseUser;
    private DatabaseReference postReference, followReference;

    public VideosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_videos, container, false);

        postRecycler = view.findViewById(R.id.timeLineRecyclerview);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
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

        postList = new ArrayList<>();

        checkFollowing();

        return view;
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
                            if (modelPost.getPostType().equals("videoPost")
                                    || modelPost.getPostType().equals("sharedVideoPost")){
                                postList.add(modelPost);
                            }
                        }
                    }

                    if(modelPost.getUserID().equals(firebaseUser.getUid())){
                        postList.add(modelPost);
                    }
                    // }

                    //Collections.shuffle(postList);
                    adapterPost = new PostAdapter(getActivity(), postList);
                    postRecycler.setAdapter(adapterPost);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}