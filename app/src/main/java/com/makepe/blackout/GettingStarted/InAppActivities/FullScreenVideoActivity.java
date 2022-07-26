package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import java.util.List;

public class FullScreenVideoActivity extends AppCompatActivity {

    private ViewPager2 fullScreenPager;
    private ProgressBar fullScreenLoader;
    private List<PostModel> videoList;

    private DatabaseReference videoReference;

    private String videoID, userID;

    private List<String> followingList;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_video);

        fullScreenPager = findViewById(R.id.fullScreenVideoPager);
        fullScreenLoader = findViewById(R.id.fullScreenVideoLoader);
        toolbar = findViewById(R.id.fullScreenVideosToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        videoID = intent.getStringExtra("videoID");

        videoReference = FirebaseDatabase.getInstance().getReference("SecretVideos");

        videoList = new ArrayList<>();

        //getUserVideos();

        getUsersVideos();
    }

    private void getUsersVideos() {
        videoReference.child(videoID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel model = snapshot.getValue(PostModel.class);

                    assert model != null;
                    userID = model.getUserID();

                    videoReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            videoList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()){
                                PostModel postModel = ds.getValue(PostModel.class);

                                if (postModel.getUserID().equals(userID)){
                                    videoList.add(postModel);
                                }

                                for (int i = 0; i < videoList.size(); i++){
                                    PostModel model = videoList.get(i);
                                    if (model.getPostID().equals(videoID)){
                                        videoList.remove(model);
                                        videoList.add(0, model);
                                    }
                                }
                                fullScreenLoader.setVisibility(View.GONE);
                                fullScreenPager.setAdapter(new VideoAdapter(videoList, FullScreenVideoActivity.this));

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getUserVideos() {
        videoReference.child(videoID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                videoList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    if (postModel.getPostID().equals(videoID)){
                        String userID = postModel.getUserID();

                        if (postModel.getUserID().equals(userID)){
                            if (postModel.getPostType().equals("videoPost")
                                    || postModel.getPostType().equals("audioVideoPost")){
                                videoList.add(postModel);
                            }
                        }
                    }

                    for (int i = 0; i < videoList.size(); i++){
                        PostModel model = videoList.get(i);
                        if (model.getPostID().equals(videoID)){
                            videoList.remove(model);
                            videoList.add(0, model);
                        }
                    }
                    fullScreenLoader.setVisibility(View.GONE);
                    Toast.makeText(FullScreenVideoActivity.this, "" + videoList.size() , Toast.LENGTH_SHORT).show();
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
                            assert postModel != null;
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}