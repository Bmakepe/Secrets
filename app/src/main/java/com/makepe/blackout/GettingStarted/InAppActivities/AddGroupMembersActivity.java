package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.GroupContactsAdapter;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AddGroupMembersActivity extends AppCompatActivity {

    private RecyclerView membersRecycler;
    private List<String> followingList;
    private List<User> userList;

    private String groupID, groupRole;

    private DatabaseReference groupReference, followingReference, userReference;
    private FirebaseUser firebaseUser;

    private ProgressDialog membersDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_members);

        Toolbar groupDetailsToolbar = findViewById(R.id.addGroupMembersToolbar);
        setSupportActionBar(groupDetailsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        membersRecycler = findViewById(R.id.addMembersRecycler);

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        groupRole = intent.getStringExtra("groupRole");

        membersDialog = new ProgressDialog(this);
        membersDialog.setMessage("Loading...");

        followingList = new ArrayList<>();
        userList = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        groupReference = FirebaseDatabase.getInstance().getReference("SecretGroups");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        followingReference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid()).child("following");

        membersRecycler.hasFixedSize();
        membersRecycler.setLayoutManager(new LinearLayoutManager(this));

        checkFollowing();

    }

    private void checkFollowing() {
        followingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    followingList.add(ds.getKey());
                }
                userReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot data : snapshot.getChildren()){
                            User user = data.getValue(User.class);

                            for (String ID : followingList){
                                assert user != null;
                                if (user.getUserID().equals(ID)){
                                    userList.add(user);
                                }
                            }
                        }

                        Collections.sort(userList, new Comparator<User>() {
                            @Override
                            public int compare(User user, User t1) {
                                return user.getUsername().compareTo(t1.getUsername());
                            }
                        });

                        membersRecycler.setAdapter(new GroupContactsAdapter(AddGroupMembersActivity.this, userList, groupID, groupRole));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.contactSearch:
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }
}