package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.NotificationAdapter;
import com.makepe.blackout.GettingStarted.Models.NotiModel;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    TextView mediaHeader;
    RecyclerView notiRecycler;
    List<NotiModel> notificationList;
    NotificationAdapter notificationAdapter;

    FirebaseUser firebaseUser;
    DatabaseReference notificationsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_sample_layout);

        mediaHeader = findViewById(R.id.recyclerHeading);
        notiRecycler = findViewById(R.id.universalRecycler);

        mediaHeader.setText("Notifications");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications");

        notificationList = new ArrayList<>();

        notiRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        notiRecycler.setLayoutManager(linearLayoutManager);

        notificationAdapter = new NotificationAdapter(NotificationsActivity.this, notificationList);
        notiRecycler.setAdapter(notificationAdapter);

        getNotifications();

        findViewById(R.id.recyclerBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getNotifications() {
        notificationsReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    NotiModel notiModel = snapshot.getValue(NotiModel.class);
                    notificationList.add(notiModel);
                }

                Collections.reverse(notificationList);
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}