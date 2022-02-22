package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

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

    TextView seeMore, savedMediaNo, savedPostsNo, savedPostsHeading;

    //for media saved posts
    private List<String> mediaSaves, popUpSaves;
    private List<PostModel> mediaPostList, mediaPopUpList;
    RecyclerView savedMediaRecycler, mediaPopRecycler;
    MediaAdapter savedMediaAdapter, mediaPopAdapter;
    Dialog mediaDialog;

    //for other saved posts
    private List<String> postSaves;
    private List<PostModel> postListSaves;
    RecyclerView savedPostsRecycler;
    PostAdapter postAdapter;

    //for firebase
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_posts);

        seeMore = findViewById(R.id.viewSavedMedia);
        savedMediaNo = findViewById(R.id.savedMediaNo);
        savedPostsNo = findViewById(R.id.savedPostsNo);
        savedMediaRecycler = findViewById(R.id.savedMediaRecycler);
        savedPostsRecycler = findViewById(R.id.savedPostsRecycler);
        savedPostsHeading = findViewById(R.id.savedPostsHeading);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getMedia();
        getPosts();
        iniMediaDialog();

        seeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaDialog.show();
            }
        });

        findViewById(R.id.savedPostsBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    //------------------ start of functions for media pop up

    private void iniMediaDialog() {
        mediaDialog = new Dialog(this);
        mediaDialog.setContentView(R.layout.recycler_sample_layout);
        mediaDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mediaDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        mediaDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        mediaPopRecycler = mediaDialog.findViewById(R.id.universalRecycler);
        TextView savedPostsHeading = mediaDialog.findViewById(R.id.recyclerHeading);
        mediaDialog.findViewById(R.id.recyclerBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaDialog.dismiss();
            }
        });

        savedPostsHeading.setText("My Saved Media");

        getPopMedia(mediaPopRecycler);
    }

    private void getPopMedia(RecyclerView mediaPopRecycler) {
        mediaPopRecycler.setHasFixedSize(true);
        mediaPopRecycler.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(this, 3);
        mediaPopRecycler.setLayoutManager(linearLayoutManager);

        mediaPopUpList = new ArrayList<>();

        mediaPopAdapter = new MediaAdapter(SavedPostsActivity.this, mediaPopUpList);
        mediaPopRecycler.setAdapter(mediaPopAdapter);

        showAllMediaSaves();

    }

    private void showAllMediaSaves() {
        popUpSaves = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    popUpSaves.add(ds.getKey());
                }

                readAllMediaSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readAllMediaSaves() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mediaPopUpList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel post = snapshot.getValue(PostModel.class);

                    for(String id : popUpSaves){
                        if(!post.getPostImage().equals("noImage") && post.getPostID().equals(id)){
                            mediaPopUpList.add(post);
                        }
                    }
                }
                mediaPopAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //-----------------------------pop up functions finish here


    //--------------------start of function for media saved posts
    private void getMedia() {
        savedMediaRecycler.setHasFixedSize(true);
        savedMediaRecycler.setNestedScrollingEnabled(false);
        savedMediaRecycler.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        savedMediaRecycler.setLayoutManager(linearLayoutManager);
        savedMediaRecycler.setItemAnimator(new DefaultItemAnimator());

        mediaPostList = new ArrayList<>();

        savedMediaAdapter = new MediaAdapter(SavedPostsActivity.this, mediaPostList);
        savedMediaRecycler.setAdapter(savedMediaAdapter);

        mediaSavedPosts();
    }

    private void mediaSavedPosts() {
        mediaSaves = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mediaPostList.clear();
                int i = 0;

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel post = snapshot.getValue(PostModel.class);

                    for(String id : mediaSaves){
                        if(!post.getPostImage().equals("noImage") && post.getPostID().equals(id)){
                            mediaPostList.add(post);
                            i++;
                        }
                    }
                }
                savedMediaNo.setText("[" + i + "]");
                savedMediaAdapter.notifyDataSetChanged();
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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postListSaves.clear();
                int i = 0;

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel post = snapshot.getValue(PostModel.class);

                    for(String id : postSaves){
                        if(post.getPostImage().equals("noImage") && post.getPostID().equals(id)){
                            postListSaves.add(post);
                            i++;
                        }
                    }
                }
                savedPostsNo.setText("[" + i + "]");
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //-------------------end of function for saved posts with no media

}
