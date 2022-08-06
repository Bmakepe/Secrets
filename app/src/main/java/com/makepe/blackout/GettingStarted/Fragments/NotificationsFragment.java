package com.makepe.blackout.GettingStarted.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.BundledNotificationAdapter;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;

public class NotificationsFragment extends Fragment {

    private RecyclerView notiRecycler;
    private ProgressBar notificationsLoader;
    private ArrayList<String> bundledNotifications;

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
        notificationsLoader = view.findViewById(R.id.notificationsLoader);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        notificationsReference = FirebaseDatabase.getInstance().getReference("SecretNotifications")
                .child(firebaseUser.getUid());

        notiRecycler.setHasFixedSize(true);
        notiRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        bundledNotifications = new ArrayList<>();

        getMyNotifications();

        return view;

    }

    private void getMyNotifications() {
        notificationsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    bundledNotifications.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){

                        notificationsReference.child(ds.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    for (DataSnapshot data : snapshot.getChildren()){
                                        bundledNotifications.add(data.getKey());
                                    }
                                    //Collections.shuffle(bundledNotifications);
                                    notiRecycler.setAdapter(new BundledNotificationAdapter(getActivity(), bundledNotifications));
                                    notificationsLoader.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                }else{
                    Toast.makeText(getActivity(), "You have no notifications", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}