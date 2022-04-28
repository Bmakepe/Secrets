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
import com.makepe.blackout.GettingStarted.Adapters.PostAdapter;
import com.makepe.blackout.GettingStarted.InAppActivities.ConnectionsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.InteractionsActivity;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.R;

import org.w3c.dom.Text;

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

    //for regular posts
    private TextView postsCounter;
    private RecyclerView postRecycler;
    private PostAdapter timelineAdapter;
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

        //for regular posts
        postsCounter = view.findViewById(R.id.PostsNo);
        postRecycler = view.findViewById(R.id.myPostsRecycler);

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        postsRef = FirebaseDatabase.getInstance().getReference("Posts");
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

        timelineAdapter = new PostAdapter(getActivity(), postList);
        postRecycler.setAdapter(timelineAdapter);

        loadPosts();
    }

    private void loadPosts() {
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                int i = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel modelPost = snapshot.getValue(PostModel.class);
                    assert modelPost != null;
                    if(modelPost.getPostImage().equals("noImage")
                            && firebaseUser.getUid().equals(modelPost.getUserID())
                            && !modelPost.getPostType().equals("videoPost")
                            && !modelPost.getPostType().equals("sharedVideoPost")){

                        postList.add(modelPost);
                        i++;
                    }

                }
                postsCounter.setText("Posts [" + i + "]");
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
                    if(!modelPost.getPostImage().equals("noImage")
                            && firebaseUser.getUid().equals(modelPost.getUserID())){
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