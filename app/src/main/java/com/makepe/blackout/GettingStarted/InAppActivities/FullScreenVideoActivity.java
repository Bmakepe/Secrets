package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class FullScreenVideoActivity extends AppCompatActivity {

    private ViewPager2 fullScreenPager;
    private ProgressBar fullScreenLoader;
    private List<PostModel> videoList;

    private DatabaseReference videoReference, followReference;
    private FirebaseUser firebaseUser;

    private String videoID, reason, userID;

    private List<String> followingList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_video);

        fullScreenPager = findViewById(R.id.fullScreenVideoPager);
        fullScreenLoader = findViewById(R.id.fullScreenVideoLoader);

        Intent intent = getIntent();
        videoID = intent.getStringExtra("videoID");
        reason = intent.getStringExtra("reason");
        userID = intent.getStringExtra("userID");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        videoReference = FirebaseDatabase.getInstance().getReference("Posts");
        followReference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid())
                .child("following");

        videoList = new ArrayList<>();

        switch (reason){
            case "random":
                checkFollowing();
                break;

            case "userVideos":
                getUserVideos();
                break;

            default:
                Toast.makeText(this, "Unknown reason", Toast.LENGTH_SHORT).show();
        }

        findViewById(R.id.fullScreenVideoBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void checkFollowing() {
        followingList = new ArrayList<>();
        followReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    followingList.add(ds.getKey());
                }

                getRandomVideos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserVideos() {
        videoReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                videoList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    try{
                        if (postModel.getUserID().equals(userID)){
                            if (postModel.getPostType().equals("videoPost")) {
                                if (postModel.getPostID().equals(videoID)){
                                    videoList.add(0, postModel);
                                }else {
                                    videoList.add(postModel);
                                }
                            }
                        }
                        fullScreenLoader.setVisibility(View.GONE);
                    }catch (NullPointerException ignored){}
                }
                fullScreenPager.setAdapter(new VideoAdapter(videoList, FullScreenVideoActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getRandomVideos() {
        videoReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                videoList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    try{
                        for (String id : followingList){
                            if (postModel.getUserID().equals(id)){
                                if (postModel.getPostType().equals("videoPost")){
                                    if (postModel.getPostPrivacy().equals("Public")){
                                        if (postModel.getPostID().equals(videoID)){
                                            videoList.add(0, postModel);
                                        }else {
                                            videoList.add(postModel);
                                        }
                                    }
                                }
                            }
                        }

                        fullScreenLoader.setVisibility(View.GONE);
                    }catch (NullPointerException ignored){}
                }
                fullScreenPager.setAdapter(new VideoAdapter(videoList, FullScreenVideoActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}