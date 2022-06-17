package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.ViewStoryAdapter;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class ViewStoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    private ArrayList<Story> stories;
    private ViewPager2 storyPager;

    private String clickedStoryOwner;

    private List<String> followingList;
    private DatabaseReference followReference, storyReference;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_story);

        Toolbar groupDetailsToolbar = findViewById(R.id.viewStoryToolbar);
        setSupportActionBar(groupDetailsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storyPager = findViewById(R.id.storiesPager);

        Intent intent = getIntent();
        clickedStoryOwner = intent.getStringExtra("userid");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        followReference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid()).child("following");
        storyReference = FirebaseDatabase.getInstance().getReference("Story");

        followingList = new ArrayList<>();
        stories = new ArrayList<>();

        checkFollowing();
    }

    private void checkFollowing() {
        followReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    followingList.add(ds.getKey());
                }
                readStories();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readStories() {
        storyReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long timecurrent = System.currentTimeMillis();
                stories.clear();
                stories.add(new Story("", firebaseUser.getUid(),
                        "", "", "", "", "",
                        0, 0, 0, 0));

                for (String id : followingList){
                    int countStory = 0;
                    Story story = null;

                    for (DataSnapshot ds : snapshot.child(id).getChildren()){
                        story = snapshot.getValue(Story.class);

                        if (timecurrent > story.getTimeStart() && timecurrent < story.getTimeEnd())
                            countStory++;
                    }

                    if (countStory > 0)
                        stories.add(story);
                }

                for (int i = 0; i < stories.size(); i++){
                    Story story = stories.get(i);

                    if (story.getUserID().equals(clickedStoryOwner)){
                        stories.remove(story);
                        stories.add(0, story);
                    }
                }

                storyPager.setAdapter(new ViewStoryAdapter(ViewStoryActivity.this, stories));
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

    @Override
    public void onNext() {

    }

    @Override
    public void onPrev() {

    }

    @Override
    public void onComplete() {

    }
}