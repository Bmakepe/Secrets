package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.FirebaseContactsAdapter;
import com.makepe.blackout.GettingStarted.Adapters.UserAdapter;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConnectionsActivity extends AppCompatActivity {

    private ImageView backBTN, searchBTN;
    private RecyclerView connectionsRecycler;
    private TextView connectionsHeader;

    private String userID, interaction;

    private DatabaseReference userReference;
    private FirebaseUser firebaseUser;

    private ArrayList<ContactsModel> userList;
    private ArrayList<String> idList;

    UserAdapter userAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connections);

        backBTN = findViewById(R.id.connectBackBTN);
        searchBTN = findViewById(R.id.connectionsSearchBTN);
        connectionsRecycler = findViewById(R.id.connectionsRecycler);
        connectionsHeader = findViewById(R.id.connectionsHeader);

        Intent intent = getIntent();
        userID = intent.getStringExtra("UserID");
        interaction = intent.getStringExtra("Interaction");

        connectionsHeader.setText(interaction);

        userReference = FirebaseDatabase.getInstance().getReference("Users");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        idList = new ArrayList<>();
        userList = new ArrayList<>();

        connectionsRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        connectionsRecycler.setLayoutManager(layoutManager);

        switch (interaction){
            case "Followers":
                getFollowers();
                break;

            case "Following":
                getFollowing();
                break;

            case "Likes":
                getLikes();
                break;

            case "Views":
                getViewers();
                break;

            default:
                Toast.makeText(this, "Unable to solve request", Toast.LENGTH_SHORT).show();
        }

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getViewers() {
        Toast.makeText(this, "You will be able to " + interaction + " for post " + userID, Toast.LENGTH_SHORT).show();
    }

    private void getLikes() {
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("Likes").child(userID);
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    idList.add(ds.getKey());

                    userReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            userList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()){
                                ContactsModel user = ds.getValue(ContactsModel.class);

                                for (String ID : idList){
                                    if (user.getUSER_ID().equals(ID)){
                                        userList.add(user);
                                    }
                                }
                            }
                            userAdapter = new UserAdapter( ConnectionsActivity.this, userList);
                            connectionsRecycler.setAdapter(userAdapter);
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

    private void getFollowing() {
        final DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(userID).child("following");

        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    idList.add(ds.getKey());

                    userReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            userList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()){
                                ContactsModel user = ds.getValue(ContactsModel.class);

                                for (String ID : idList){
                                    assert user != null;
                                    if (user.getUSER_ID().equals(ID)){
                                        userList.add(user);
                                    }
                                }
                            }

                            userAdapter = new UserAdapter( ConnectionsActivity.this, userList);
                            connectionsRecycler.setAdapter(userAdapter);
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

    private void getFollowers() {
        final DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(userID).child("followers");
        followersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    idList.add(ds.getKey());

                    userReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            userList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()){
                                ContactsModel user = ds.getValue(ContactsModel.class);

                                for (String ID : idList){
                                    if (user.getUSER_ID().equals(ID)){
                                        userList.add(user);
                                    }
                                }
                            }

                            userAdapter = new UserAdapter( ConnectionsActivity.this, userList);
                            connectionsRecycler.setAdapter(userAdapter);
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
}