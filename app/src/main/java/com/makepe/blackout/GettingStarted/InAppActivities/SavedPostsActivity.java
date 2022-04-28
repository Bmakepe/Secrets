package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
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
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.List;

public class SavedPostsActivity extends AppCompatActivity {

    private TextView seeMore, savedMediaHeader, savedPostsHead;

    //for saved posts
    private List<String> savedPostsList;
    private List<PostModel> mediaPostList;
    private List<PostModel> postListSaves;
    private RecyclerView savedPostsRecycler;
    private RecyclerView savedMediaRecycler;

    //for firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference savedReference, postsReference, movementPostReference;

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_posts);

        Toolbar savedPostsToolbar = findViewById(R.id.savedPostsToolbar);
        setSupportActionBar(savedPostsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        seeMore = findViewById(R.id.viewSavedMedia);
        savedMediaHeader = findViewById(R.id.savedMediaHeading);
        savedPostsHead = findViewById(R.id.savedPostsHead);
        savedMediaRecycler = findViewById(R.id.savedMediaRecycler);
        savedPostsRecycler = findViewById(R.id.savedPostsRecycler);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        savedReference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(firebaseUser.getUid());
        postsReference = FirebaseDatabase.getInstance().getReference("Posts");
        movementPostReference = FirebaseDatabase.getInstance().getReference("MovementPosts");

        savedMediaRecycler.hasFixedSize();
        savedMediaRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        savedMediaRecycler.setItemAnimator(new DefaultItemAnimator());

        savedPostsRecycler.hasFixedSize();
        savedPostsRecycler.setLayoutManager(new LinearLayoutManager(this));

        savedPostsList = new ArrayList<>();
        postListSaves = new ArrayList<>();
        mediaPostList = new ArrayList<>();

        pd = new ProgressDialog(this);
        pd.setMessage("Loading...");

        getMySavedPosts();

        seeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent seeMoreIntent = new Intent(SavedPostsActivity.this, InteractionsActivity.class);
                seeMoreIntent.putExtra("userID", firebaseUser.getUid());
                seeMoreIntent.putExtra("interactionType", "seeSavedMedia");
                startActivity(seeMoreIntent);
            }
        });

    }

    private void getMySavedPosts() {
        pd.show();

        savedReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    savedPostsList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        savedPostsList.add(ds.getKey());
                    }

                    getSavedTextPosts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSavedTextPosts() {
        postsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postListSaves.clear();
                mediaPostList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    for (String id : savedPostsList){
                        assert postModel != null;
                        if (postModel.getPostID().equals(id)) {

                            if (postModel.getPostType().equals("textPost")) {
                                postListSaves.add(postModel);
                            }else if (postModel.getPostType().equals("imagePost")){
                                mediaPostList.add(postModel);
                            }

                        }
                    }
                }
                getSavedMovementPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void getSavedMovementPosts() {

        movementPostReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel pm = ds.getValue(PostModel.class);

                    for (String id : savedPostsList){
                        assert pm != null;
                        if (pm.getPostID().equals(id)){
                            if (pm.getPostType().equals("textPost")){
                                postListSaves.add(pm);
                            }else if (pm.getPostType().equals("imagePost")){
                                mediaPostList.add(pm);
                            }
                        }

                        savedPostsRecycler.setAdapter(new PostAdapter(SavedPostsActivity.this, postListSaves));
                        savedMediaRecycler.setAdapter(new MediaAdapter(SavedPostsActivity.this, mediaPostList));

                    }
                    savedMediaHeader.setText("Saved Media [" + mediaPostList.size() +  "]");
                    savedPostsHead.setText("Saved Posts [" + postListSaves.size() +  "]");

                    pd.dismiss();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

/*
    //--------------------start of function for media saved posts
    private void getMedia() {

        mediaPostList = new ArrayList<>();

        savedMediaAdapter = new MediaAdapter(SavedPostsActivity.this, mediaPostList);
        savedMediaRecycler.setAdapter(savedMediaAdapter);

        mediaSavedPosts();
    }

    private void mediaSavedPosts() {
        mediaSaves = new ArrayList<>();

        savedReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    mediaSaves.add(ds.getKey());
                }

                readMediaSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMediaSaves() {
        postsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mediaPostList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel post = snapshot.getValue(PostModel.class);

                    for(String id : mediaSaves){
                        assert post != null;
                        if(post.getPostType().equals("imagePost")
                                || post.getPostType().equals("sharedImagePost")
                                && post.getPostID().equals(id)){
                            mediaPostList.add(post);
                        }
                    }

                    movementPostReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                PostModel model = ds.getValue(PostModel.class);

                                for (String id : mediaSaves){
                                    assert model != null;
                                    assert post != null;
                                    if(post.getPostType().equals("imagePost")
                                            || post.getPostType().equals("sharedImagePost")
                                            && model.getPostID().equals(id)){
                                        mediaPostList.add(post);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                savedMediaNo.setText("[" + mediaPostList.size() + "]");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //----------------------end of function for saved media posts


    //---------------------start of functions for saved posts with no media

    private void getPosts() {
        savedPostsRecycler.setHasFixedSize(true);
        savedPostsRecycler.setNestedScrollingEnabled(false);
        savedPostsRecycler.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        savedPostsRecycler.setLayoutManager(linearLayoutManager);

        postListSaves = new ArrayList<>();

        postAdapter = new PostAdapter(SavedPostsActivity.this, postListSaves);
        savedPostsRecycler.setAdapter(postAdapter);

        getSavedPosts();
    }

    private void getSavedPosts() {
        postSaves = new ArrayList<>();

        savedReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    postSaves.add(ds.getKey());
                }
                readPostSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readPostSaves() {
        postsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postListSaves.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel post = snapshot.getValue(PostModel.class);

                    for(String id : postSaves){
                        assert post != null;
                        if(post.getPostID().equals(id)
                                && post.getPostType().equals("textPost")
                                || post.getPostType().equals("sharedTextPost")){
                            postListSaves.add(post);
                        }
                    }
                    movementPostReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                PostModel postModel = ds.getValue(PostModel.class);

                                for (String id : postSaves){
                                    assert postModel != null;
                                    if(postModel.getPostID().equals(id)
                                            && postModel.getPostType().equals("textPost")
                                            || postModel.getPostType().equals("sharedTextPost")){
                                        postListSaves.add(postModel);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                savedPostsNo.setText("[" + postListSaves.size() + "]");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //-------------------end of function for saved posts with no media
    */

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
