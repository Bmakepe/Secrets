package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.makepe.blackout.GettingStarted.Adapters.MovementInteractionAdapter;
import com.makepe.blackout.GettingStarted.Adapters.UserInteractionAdapter;
import com.makepe.blackout.GettingStarted.Models.Movement;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InteractionsActivity extends AppCompatActivity {

    private RecyclerView connectionsRecycler;
    private Toolbar connectionsToolbar;

    private String userID, interactionType;

    //for all Media concerns
    private List<PostModel> mediaList;
    private List<String> savedList;

    //for movements & Users
    private ArrayList<Movement> movementsList;
    private ArrayList<User> userList;

    private DatabaseReference postReference, userReference, movementPostReference,
            savedMediaRef, movementReference;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connections);

        connectionsRecycler = findViewById(R.id.connectionsRecycler);
        connectionsToolbar = findViewById(R.id.connectionsToolbar);
        setSupportActionBar(connectionsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        interactionType = intent.getStringExtra("interactionType");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        postReference = FirebaseDatabase.getInstance().getReference("Posts");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        movementPostReference = FirebaseDatabase.getInstance().getReference("Movements");
        movementPostReference = FirebaseDatabase.getInstance().getReference("MovementPosts");
        savedMediaRef = FirebaseDatabase.getInstance().getReference("Saves")
                .child(firebaseUser.getUid());

        connectionsRecycler.hasFixedSize();
        connectionsRecycler.setLayoutManager(new GridLayoutManager(InteractionsActivity.this, 3));

        switch (interactionType){
            case "seeMoreMedia":
                prepareMedia();
                break;

            case "seeSavedMedia":
                getSavedMedia();
                break;

            case "seeMoreMovements":
                getAllMovements();
                break;

            case "seeMoreUsers":
                getAllUsers();
                break;

            default:
                Toast.makeText(this, "Illegal Selection", Toast.LENGTH_SHORT).show();
        }

    }

    private void getAllUsers() {
        userList = new ArrayList<>();
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (!user.getUSER_ID().equals(firebaseUser.getUid())){
                        userList.add(user);
                    }
                }
                connectionsRecycler.setAdapter(new UserInteractionAdapter(userList, InteractionsActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAllMovements() {
        movementsList = new ArrayList<>();
        movementReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                movementsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Movement movement = ds.getValue(Movement.class);

                    if (!movement.getMovementAdmin().equals(firebaseUser.getUid())){
                        movementsList.add(movement);
                    }
                }
                connectionsRecycler.setAdapter(new MovementInteractionAdapter(movementsList, InteractionsActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSavedMedia() {

        savedList = new ArrayList<>();
        savedMediaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                savedList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        savedList.add(ds.getKey());
                    }

                    getSavedPosts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSavedPosts() {
        mediaList = new ArrayList<>();

        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mediaList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    PostModel post = ds.getValue(PostModel.class);

                    for (String id : savedList) {
                        assert post != null;
                        if (post.getPostID().equals(id)
                                &&post.getPostType().equals("imagePost")) {
                            mediaList.add(post);
                        }
                    }
                }
                getSavedMovements();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSavedMovements() {

        movementPostReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()){
                    PostModel postModel = snap.getValue(PostModel.class);

                    for (String id : savedList){
                        assert postModel != null;
                        if (postModel.getPostID().equals(id)
                                && postModel.getPostType().equals("imagePost")){
                            mediaList.add(postModel);
                        }
                    }
                }
                connectionsRecycler.setAdapter(new MediaAdapter(InteractionsActivity.this, mediaList));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void prepareMedia() {
        mediaList = new ArrayList<>();

        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mediaList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel modelPost = snapshot.getValue(PostModel.class);
                    assert modelPost != null;
                    if(!modelPost.getPostImage().equals("noImage")
                            && modelPost.getUserID().equals(userID)){
                        mediaList.add(modelPost);
                    }

                }

                Collections.reverse(mediaList);
                connectionsRecycler.setAdapter(new MediaAdapter(InteractionsActivity.this, mediaList));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(InteractionsActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUSER_ID().equals(userID)){
                        connectionsToolbar.setTitle(user.getUsername() + " Media");
                    }
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