package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

        postListPager = findViewById(R.id.postListRecycler);

        Intent intent = getIntent();
        postID = intent.getStringExtra("postID");

        postReference = FirebaseDatabase.getInstance().getReference("Posts");

        postListPager.hasFixedSize();
        postListPager.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();

        getPosts();

        findViewById(R.id.postListBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getPosts() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    try{
                        assert postModel != null;
                        if (postModel.getPostID().equals(postID)){
                            postList.add(0, postModel);
                        }else{
                            postList.add(postModel);
                        }
                    }catch (NullPointerException ignored){}

                }

                postListPager.setAdapter(new PostAdapter(PostListActivity.this, postList));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}