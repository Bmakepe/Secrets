package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.UserAdapter;
import com.makepe.blackout.R;

import java.util.ArrayList;

public class MyContactsActivity extends AppCompatActivity {

    private RecyclerView contactsRecycler;

    private FirebaseUser firebaseUser;
    private DatabaseReference followingReference;
    private ArrayList<String> idList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_contacts);

        contactsRecycler = findViewById(R.id.myContactsRecycler);
        Toolbar contactListToolbar = findViewById(R.id.contactListToolbar);
        setSupportActionBar(contactListToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        followingReference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid()).child("following");

        contactsRecycler.setHasFixedSize(true);
        contactsRecycler.setNestedScrollingEnabled(false);
        contactsRecycler.hasFixedSize();
        contactsRecycler.setLayoutManager(new LinearLayoutManager(this));

        idList = new ArrayList<>();

        getMyContacts();

        findViewById(R.id.createGroupBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MyContactsActivity.this, CreateGroupActivity.class));
            }
        });

    }

    private void getMyContacts() {
        followingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    idList.add(ds.getKey());
                }
                contactsRecycler.setAdapter(new UserAdapter( MyContactsActivity.this, idList, "goToChats"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}