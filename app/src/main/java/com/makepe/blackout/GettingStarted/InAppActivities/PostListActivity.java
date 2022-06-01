package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.PostAdapter;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostListActivity extends AppCompatActivity {

    private RecyclerView postListPager;
    private List<PostModel> postList;

    private DatabaseReference postReference;
    private String postID;
    private Toolbar postListToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

        postListPager = findViewById(R.id.postListRecycler);
        postListToolbar = findViewById(R.id.postListToolbar);
        setSupportActionBar(postListToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        postID = intent.getStringExtra("postID");

        postReference = FirebaseDatabase.getInstance().getReference("Posts");

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
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    assert postModel != null;
                    if (!postModel.getPostType().equals("videoPost")
                            && !postModel.getPostType().equals("sharedVideoPost")
                            && !postModel.getPostType().equals("audioVideoPost")
                            && !postModel.getPostPrivacy().equals("Private")){

                        postList.add(postModel);

                    }

                    Collections.shuffle(postList);

                    for (int i = 0; i < postList.size(); i++){
                        PostModel model = postList.get(i);
                        if (model.getPostID().equals(postID)){
                            postList.remove(model);
                            postList.add(0, model);
                        }
                    }
                }

                postListPager.setAdapter(new PostAdapter(PostListActivity.this, postList));
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