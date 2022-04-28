package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import com.makepe.blackout.GettingStarted.Adapters.UserListAdapter;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConnectionsActivity extends AppCompatActivity {

    private RecyclerView connectionsRecycler;
    private Toolbar connectionsToolbar;

    private String userID, interaction;

    private ArrayList<String> idList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connections);

        connectionsRecycler = findViewById(R.id.connectionsRecycler);
        connectionsToolbar = findViewById(R.id.connectionsToolbar);

        Intent intent = getIntent();
        userID = intent.getStringExtra("UserID");
        interaction = intent.getStringExtra("Interaction");

        setSupportActionBar(connectionsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        idList = new ArrayList<>();

        connectionsRecycler.hasFixedSize();
        connectionsRecycler.setLayoutManager(new LinearLayoutManager(this));

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
    }

    private void getViewers() {
        final DatabaseReference viewsReference = FirebaseDatabase.getInstance().getReference("Views").child(userID);
        viewsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    idList.add(ds.getKey());
                    connectionsToolbar.setTitle("Views");
                }
                connectionsRecycler.setAdapter(new UserListAdapter( ConnectionsActivity.this, idList, "goToProfile"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getLikes() {
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference("Likes").child(userID);
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    idList.add(ds.getKey());
                    connectionsToolbar.setTitle("Likes");
                }
                connectionsRecycler.setAdapter(new UserListAdapter( ConnectionsActivity.this, idList, "goToProfile"));
                //getUserDetails();
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
                    connectionsToolbar.setTitle("Following");
                }
                connectionsRecycler.setAdapter(new UserListAdapter( ConnectionsActivity.this, idList, "goToProfile"));
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
                    connectionsToolbar.setTitle("Followers");

                }
                connectionsRecycler.setAdapter(new UserListAdapter( ConnectionsActivity.this, idList, "goToProfile"));
                //getUserDetails();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contacts_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.contactSearch:
                Toast.makeText(this, "Search Contacts", Toast.LENGTH_SHORT).show();

            default:
                Toast.makeText(this, "unknown menu selection", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}