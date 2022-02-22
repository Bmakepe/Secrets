package com.makepe.blackout.GettingStarted.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.makepe.blackout.GettingStarted.Adapters.ExploreAdapter;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.Collections;

public class ExploreImagesFragment extends Fragment {

    private ArrayList<String> exploreItems;
    private RecyclerView exploreRecycler;
    private ExploreAdapter exploreAdapter;

    private DatabaseReference postRef, userRef;
    private FirebaseUser firebaseUser;

    private ProgressDialog pd;

    public ExploreImagesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore_images, container, false);

        exploreRecycler = view.findViewById(R.id.exploreRecycler);

        postRef = FirebaseDatabase.getInstance().getReference("Posts");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        exploreItems = new ArrayList<>();
        pd = new ProgressDialog(getContext());
        pd.setMessage("Loading...");
        pd.show();
        exploreItems.clear();

        declareRecycler();
        getAllExploreItems();

        view.findViewById(R.id.exploreFragBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        return view;
    }

    private void getAllExploreItems() {
        getExploreItems();
        getExploreUsers();
        updateRecycler();
    }

    private void declareRecycler() {

        exploreRecycler.hasFixedSize();
        exploreRecycler.setHasFixedSize(true);
        //exploreRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false));

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3,
                GridLayoutManager.VERTICAL, false);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {

                if (position % 12 == 0 || position % 12 ==7){
                    return position == 1 ? 2 : 1;
                }else{
                    return position == 1 ? 1 : 1;
                }
            }
        });
        exploreRecycler.setLayoutManager(layoutManager);

    }

    private void getExploreUsers() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (!user.getUSER_ID().equals(firebaseUser.getUid()))
                        exploreItems.add(user.getUSER_ID());
                }
                updateRecycler();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getExploreItems() {
        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    try{
                        assert postModel != null;
                        if (!postModel.getPostPrivacy().equals("Private")){
                            if (postModel.getPostType().equals("imagePost"))
                                exploreItems.add(postModel.getPostID());
                        }
                    }catch (NullPointerException ignored){}

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateRecycler() {
        Collections.shuffle(exploreItems);
        exploreRecycler.setAdapter(new ExploreAdapter(exploreItems, getContext()));
        pd.dismiss();
    }
}