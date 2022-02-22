package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyContactsActivity extends AppCompatActivity {

    ArrayList<ContactsModel> userList;
    UserAdapter userAdapter;
    RecyclerView contactsRecycler;

    FirebaseUser firebaseUser;
    DatabaseReference userReference, followingReference;
    private ArrayList<String> idList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_contacts);

        contactsRecycler = findViewById(R.id.myContactsRecycler);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter( MyContactsActivity.this, userList);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        followingReference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid()).child("following");

        contactsRecycler.setHasFixedSize(true);
        contactsRecycler.setNestedScrollingEnabled(false);
        contactsRecycler.hasFixedSize();
        contactsRecycler.setLayoutManager(new LinearLayoutManager(this));
        contactsRecycler.setAdapter(userAdapter);

        idList = new ArrayList<>();

        getMyContacts();

        findViewById(R.id.fBaseBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.createGroupBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MyContactsActivity.this, "You will be able to create a new group", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.chatContactsSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MyContactsActivity.this, "You will be able to search this list", Toast.LENGTH_SHORT).show();
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

                    userReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            userList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()){
                                ContactsModel user = ds.getValue(ContactsModel.class);

                                for (String id : idList){
                                    assert user != null;
                                    if (user.getUSER_ID().equals(id))
                                        userList.add(user);
                                }
                            }
                            contactsRecycler.setAdapter(new UserAdapter(MyContactsActivity.this, userList));
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