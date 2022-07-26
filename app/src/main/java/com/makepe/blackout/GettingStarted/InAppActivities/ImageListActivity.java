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
import com.makepe.blackout.GettingStarted.Adapters.TimelineAdapter;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.Notifications.Data;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageListActivity extends AppCompatActivity {

    private RecyclerView imageListRecycler;
    private List<PostModel> imageList;
    private String userID;

    private DatabaseReference postReference, userReference;
    private String selectedPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);

        Toolbar imageListToolBar = findViewById(R.id.imageListToolbar);
        setSupportActionBar(imageListToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageListRecycler = findViewById(R.id.imageListRecycler);

        Intent intent = getIntent();
        selectedPost = intent.getStringExtra("postID");

        postReference = FirebaseDatabase.getInstance().getReference("SecretPosts");
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        imageList = new ArrayList<>();

        imageListRecycler.hasFixedSize();
        imageListRecycler.setLayoutManager(new LinearLayoutManager(this));

        getUserPosts();

    }

    private void getUserPosts() {
        postReference.child(selectedPost).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel model = snapshot.getValue(PostModel.class);
                    userID = model.getUserID();

                    postReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            imageList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()){
                                PostModel postModel = ds.getValue(PostModel.class);

                                if (postModel.getUserID().equals(userID))
                                    if (postModel.getPostType().equals("imagePost")
                                            || postModel.getPostType().equals("audioImagePost"))
                                        imageList.add(postModel);

                                for (int i = 0; i < imageList.size(); i++){
                                    PostModel post = imageList.get(i);
                                    if (post.getPostID().equals(selectedPost)){
                                        imageList.remove(post);
                                        imageList.add(0, post);
                                    }
                                }

                                imageListRecycler.setAdapter(new TimelineAdapter(ImageListActivity.this, imageList));

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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}