package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.MediaAdapter;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InteractionsActivity extends AppCompatActivity {

    private ImageView backBTN, searchBTN;
    private RecyclerView connectionsRecycler;
    private TextView connectionsHeader;

    String userID, interactionType;

    //for all Media concerns
    private List<PostModel> mediaList;
    private MediaAdapter mediaAdapter;

    DatabaseReference postReference, userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connections);

        backBTN = findViewById(R.id.connectBackBTN);
        searchBTN = findViewById(R.id.connectionsSearchBTN);
        connectionsRecycler = findViewById(R.id.connectionsRecycler);
        connectionsHeader = findViewById(R.id.connectionsHeader);
        searchBTN.setVisibility(View.GONE);

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        interactionType = intent.getStringExtra("interactionType");

        postReference = FirebaseDatabase.getInstance().getReference("Posts");
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        switch (interactionType){
            case "seeMoreMedia":
                prepareMedia();
                break;

            default:
                Toast.makeText(this, "Illegal Selection", Toast.LENGTH_SHORT).show();
        }

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void prepareMedia() {

        connectionsRecycler.setHasFixedSize(true);
        connectionsRecycler.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(InteractionsActivity.this, 3);
        connectionsRecycler.setLayoutManager(linearLayoutManager);

        mediaList = new ArrayList<>();

        mediaAdapter = new MediaAdapter(InteractionsActivity.this, mediaList);
        connectionsRecycler.setAdapter(mediaAdapter);

        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mediaList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel modelPost = snapshot.getValue(PostModel.class);
                    if(!modelPost.getPostImage().equals("noImage") && userID.equals(modelPost.getUserID())){
                        mediaList.add(modelPost);
                    }

                }

                Collections.reverse(mediaList);
                mediaAdapter.notifyDataSetChanged();
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
                        connectionsHeader.setText(user.getUsername() + " Media");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}