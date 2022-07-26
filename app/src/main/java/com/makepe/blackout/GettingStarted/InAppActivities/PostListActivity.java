package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.TimelineAdapter;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PostListActivity extends AppCompatActivity {

    private RecyclerView postListPager;
    private List<PostModel> postList;

    private DatabaseReference postReference;
    private String postID;
    private Toolbar postListToolbar;

    public static final String TAG = "NATIVE_AD_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

        postListPager = findViewById(R.id.postListRecycler);
        postListToolbar = findViewById(R.id.postListToolbar);
        setSupportActionBar(postListToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                Log.d(TAG, "onInitializationComplete: " + initializationStatus);
            }
        });

        MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ADD_TEST_DEVICE_ID_HERE", "ADD_TEST_DEVICE_ID_HERE")).build()
        );

        Intent intent = getIntent();
        postID = intent.getStringExtra("postID");

        postReference = FirebaseDatabase.getInstance().getReference("SecretPosts");

        postListPager.hasFixedSize();
        postListPager.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();

        getAllPublicPosts();

    }

    private void getAllPublicPosts() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                int i = 0;
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    assert postModel != null;
                    if (!postModel.getPostType().equals("videoPost")
                            && !postModel.getPostType().equals("sharedVideoPost")
                            && !postModel.getPostType().equals("audioVideoPost")
                            && !postModel.getPostType().equals("sharedAudioTextVideoPost")
                            && !postModel.getPostType().equals("sharedTextAudioVideoPost")
                            && !postModel.getPostType().equals("sharedAudioAudioVideoPost")
                            && !postModel.getPostPrivacy().equals("Private")){
                        postList.add(postModel);
                    }

                    Collections.shuffle(postList);

                    for (int j = 0; j < postList.size(); j++){
                        PostModel model = postList.get(j);
                        if (model.getPostID().equals(postID)){
                            postList.remove(model);
                            postList.add(0, model);
                        }
                    }
                }

                postListPager.setAdapter(new TimelineAdapter(PostListActivity.this, postList));

                //postListPager.setAdapter(new PostAdapter(PostListActivity.this, postList));
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