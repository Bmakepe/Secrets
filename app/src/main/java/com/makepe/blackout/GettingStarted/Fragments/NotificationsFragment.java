package com.makepe.blackout.GettingStarted.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.NotificationAdapter;
import com.makepe.blackout.GettingStarted.InAppActivities.NotificationsActivity;
import com.makepe.blackout.GettingStarted.Models.NotiModel;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView notiRecycler;
    private List<NotiModel> notificationList;
    private NotificationAdapter notificationAdapter;

    private FirebaseUser firebaseUser;
    private DatabaseReference notificationsReference;

    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        notiRecycler = view.findViewById(R.id.notificationsRecycler);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications");

        notificationList = new ArrayList<>();

        notiRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        notiRecycler.setLayoutManager(linearLayoutManager);

        notificationAdapter = new NotificationAdapter(getActivity(), notificationList);
        notiRecycler.setAdapter(notificationAdapter);

        getNotifications();

        return view;
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