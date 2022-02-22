package com.makepe.blackout.GettingStarted.Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.FirebaseContactsAdapter;
import com.makepe.blackout.GettingStarted.InAppActivities.ChatListActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.HomeConsoleActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.MyContactsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.PostActivity;
import com.makepe.blackout.GettingStarted.Models.Chat;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.ViewPagerAdapter;
import com.makepe.blackout.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeConsoleFragment extends Fragment {

    private FirebaseUser firebaseUser;
    private DatabaseReference userRef, chatRef;

    private CircleImageView homeProPic;

    public HomeConsoleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_console, container, false);

        homeProPic = view.findViewById(R.id.homePic);

        final TabLayout tabLayout = view.findViewById(R.id.home_tab_layout);
        final ViewPager viewPager = view.findViewById(R.id.home_view_pager);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPagerAdapter.addFragment(new HomeFragment(),"Posts");
        viewPagerAdapter.addFragment(new VideosFragment(), "Videos");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        /*chatRef = FirebaseDatabase.getInstance().getReference("Chats");
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
                viewPagerAdapter.addFragment(new HomeFragment(),"Timeline");

                int unread = 0;

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;

                    try{
                        if(chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isSeen()){
                            unread++;
                        }
                    }catch (NullPointerException ignored){}
                }

                if(unread == 0){
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                }else{
                    viewPagerAdapter.addFragment(new ChatsFragment(), " Chats (" + unread + ")");
                }

                viewPager.setAdapter(viewPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        getUserDetails();

        view.findViewById(R.id.newPostBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PostActivity.class));
            }
        });

        view.findViewById(R.id.homeInboxBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ChatListActivity.class));
            }
        });

        return view;
    }

    private void getUserDetails() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUSER_ID().equals(firebaseUser.getUid())){
                        try{
                            Picasso.get().load(user.getImageURL()).into(homeProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(homeProPic);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }


}
