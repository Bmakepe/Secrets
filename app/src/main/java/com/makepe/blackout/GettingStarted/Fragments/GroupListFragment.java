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
import com.makepe.blackout.GettingStarted.Adapters.GroupAdapter;
import com.makepe.blackout.GettingStarted.Models.GroupsModel;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.List;

public class GroupListFragment extends Fragment {

    private List<GroupsModel> groupList;
    private RecyclerView groupRecycler;
    private DatabaseReference groupReference;
    private FirebaseUser firebaseUser;

    public GroupListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_list, container, false);

        groupRecycler = view.findViewById(R.id.groupRecycler);

        groupRecycler.hasFixedSize();
        groupRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        groupList = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        groupReference = FirebaseDatabase.getInstance().getReference("SecretGroups");

        getGroupList();

        return view;
    }

    private void getGroupList() {
        groupReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    if (ds.child("Participants").child(firebaseUser.getUid()).exists()){
                        GroupsModel group = ds.getValue(GroupsModel.class);
                        groupList.add(group);
                    }
                }
                groupRecycler.setAdapter(new GroupAdapter(getActivity(), groupList));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}