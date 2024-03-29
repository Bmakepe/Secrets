package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
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
import com.makepe.blackout.GettingStarted.Adapters.TimelineAdapter;
import com.makepe.blackout.GettingStarted.Adapters.VideoItemAdapter;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SavedPostsActivity extends AppCompatActivity {

    private TextView seeMore, savedMediaHeader, savedPostsHead, savedVideosHeader, moreVideos;
    private LinearLayout imagesArea, videosArea, postsArea;

    //for saved posts
    private List<String> savedPostsList;
    private List<PostModel> mediaPostList, postListSaves, savedVideosList;
    private RecyclerView savedPostsRecycler, savedMediaRecycler, savedVideosRecycler;

    //for firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference savedReference, postsReference, videosReference;

    private LinearLayoutManager savedMediaLayout, savedVideoLayout;
    private MediaAdapter mediaAdapter;
    private VideoItemAdapter videoItemAdapter;

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
        savedVideosHeader = findViewById(R.id.savedVideosHeader);
        moreVideos = findViewById(R.id.viewSavedVideos);
        savedMediaRecycler = findViewById(R.id.savedMediaRecycler);
        savedPostsRecycler = findViewById(R.id.savedPostsRecycler);
        savedVideosRecycler = findViewById(R.id.savedVideosRecycler);
        imagesArea = findViewById(R.id.imagesArea);
        videosArea = findViewById(R.id.videosArea);
        postsArea = findViewById(R.id.postsArea);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        savedReference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(firebaseUser.getUid());
        postsReference = FirebaseDatabase.getInstance().getReference("SecretPosts");
        videosReference = FirebaseDatabase.getInstance().getReference("SecretVideos");

        savedPostsList = new ArrayList<>();
        postListSaves = new ArrayList<>();
        mediaPostList = new ArrayList<>();
        savedVideosList = new ArrayList<>();

        LinearSnapHelper snapHelper = new LinearSnapHelper();

        savedMediaRecycler.hasFixedSize();
        savedMediaLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        savedMediaRecycler.setLayoutManager(savedMediaLayout);
        snapHelper.attachToRecyclerView(savedMediaRecycler);
        mediaAdapter = new MediaAdapter(SavedPostsActivity.this, mediaPostList);
        savedMediaRecycler.setAdapter(mediaAdapter);

        savedVideosRecycler.hasFixedSize();
        savedVideoLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        savedVideosRecycler.setLayoutManager(savedVideoLayout);
        snapHelper.attachToRecyclerView(savedVideosRecycler);
        videoItemAdapter = new VideoItemAdapter(savedVideosList, SavedPostsActivity.this);
        savedVideosRecycler.setAdapter(videoItemAdapter);

        savedPostsRecycler.hasFixedSize();
        savedPostsRecycler.setLayoutManager(new LinearLayoutManager(this));

        pd = new ProgressDialog(this);
        pd.setMessage("Loading...");

        getMySavedPosts();

        moreVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!savedVideosList.isEmpty()) {
                    Intent seeMoreIntent = new Intent(SavedPostsActivity.this, InteractionsActivity.class);
                    seeMoreIntent.putExtra("userID", firebaseUser.getUid());
                    seeMoreIntent.putExtra("interactionType", "seeSavedVideos");
                    startActivity(seeMoreIntent);
                }else
                    Toast.makeText(SavedPostsActivity.this, "You have not saved any media", Toast.LENGTH_SHORT).show();
            }
        });

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

                    getSavedMediaPosts();
                    getSavedTextPosts();
                    getSavedVideoPosts();
                }else{

                    savedMediaHeader.setText("Saved Media [" + mediaPostList.size() +  "]");
                    savedPostsHead.setText("Saved Posts [" + postListSaves.size() +  "]");
                    savedVideosHeader.setText("Saved Videos [" + savedVideosList.size() + "]");

                    pd.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSavedMediaPosts() {
        postsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mediaPostList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    for (String id : savedPostsList){
                        assert postModel != null;
                        if (postModel.getPostID().equals(id)){
                            if (postModel.getPostType().equals("imagePost")
                                    || postModel.getPostType().equals("audioImagePost")
                                    || postModel.getPostType().equals("sharedTextImagePost")
                                    || postModel.getPostType().equals("sharedAudioImagePost")
                                    || postModel.getPostType().equals("sharedTextAudioImagePost")
                                    || postModel.getPostType().equals("sharedAudioAudioImagePost"))
                                mediaPostList.add(postModel);
                        }
                    }

                    if (!mediaPostList.isEmpty()){
                        imagesArea.setVisibility(View.VISIBLE);
                        savedMediaHeader.setText("Saved Media [" + mediaPostList.size() +  "]");
                        mediaAdapter.notifyDataSetChanged();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSavedVideoPosts() {
        videosReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                savedVideosList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    for (String id : savedPostsList){
                        assert postModel != null;
                        if (postModel.getPostID().equals(id))
                            if (postModel.getPostType().equals("videoPost")
                                    || postModel.getPostType().equals("sharedAudioVideoPost")
                                    || postModel.getPostType().equals("sharedTextVideoPost")
                                    || postModel.getPostType().equals("audioVideoPost")
                                    || postModel.getPostType().equals("sharedAudioAudioVideoPost")
                                    || postModel.getPostType().equals("sharedTextAudioVideoPost")) {
                                savedVideosList.add(postModel);
                            }
                    }

                    if (!savedVideosList.isEmpty()){
                        videosArea.setVisibility(View.VISIBLE);

                        savedVideosHeader.setText("Saved Videos [" + savedVideosList.size() + "]");
                        videoItemAdapter.notifyDataSetChanged();
                    }

                    pd.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSavedTextPosts() {
        postsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postListSaves.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    for (String id : savedPostsList){
                        assert postModel != null;
                        if (postModel.getPostID().equals(id)) {

                            if (postModel.getPostType().equals("textPost")
                                    || postModel.getPostType().equals("audioPost")
                                    || postModel.getPostType().equals("sharedTextPost")
                                    || postModel.getPostType().equals("sharedAudioTextPost")
                                    || postModel.getPostType().equals("sharedTextAudioPost")
                                    || postModel.getPostType().equals("sharedAudioAudioPost")) {
                                postListSaves.add(postModel);
                            }
                        }
                    }

                    if (!postListSaves.isEmpty()){
                        postsArea.setVisibility(View.VISIBLE);
                        savedPostsHead.setText("Saved Posts [" + postListSaves.size() +  "]");
                        savedPostsRecycler.setAdapter(new TimelineAdapter(SavedPostsActivity.this, postListSaves));
                    }

                }

                pd.dismiss();
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
