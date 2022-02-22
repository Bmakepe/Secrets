package com.makepe.blackout.GettingStarted.OtherClasses;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;

import java.util.ArrayList;
import java.util.List;

public class FirebaseContacts {

    private List<ContactsModel> fireContacts = new ArrayList<>();

    public FirebaseContacts(List<ContactsModel> fireContacts) {
        this.fireContacts = fireContacts;
    }

    public FirebaseContacts() {
    }

    public void readContacts(){
        fireContacts.clear();
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fireContacts.clear();

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ContactsModel user = ds.getValue(ContactsModel.class);

                    assert user != null;
                    assert firebaseUser != null;
                    if (!user.getUSER_ID().equals(firebaseUser.getUid())) {
                        fireContacts.add(user);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
