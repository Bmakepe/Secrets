package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;
import com.makepe.blackout.GettingStarted.Fragments.ExploreFragment;
import com.makepe.blackout.GettingStarted.Fragments.HomeConsoleFragment;
import com.makepe.blackout.GettingStarted.Fragments.HomeFragment;
import com.makepe.blackout.GettingStarted.Fragments.ProfileFragment;
import com.makepe.blackout.GettingStarted.Fragments.TimelineFragment;
import com.makepe.blackout.GettingStarted.MainActivity;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;

import java.util.HashMap;

public class HomeConsoleActivity extends AppCompatActivity {
    String uid;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_console);

        checkUserStatus();

        openSelectedFragment();

        SpaceNavigationView spaceNavigationView = findViewById(R.id.space);
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem("HOME", R.drawable.ic_home_black_24dp ));
        spaceNavigationView.addSpaceItem(new SpaceItem("PROFILE", R.drawable.ic_person_pin_black_24dp));

        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                ExploreFragment frag = new ExploreFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content, frag, "");
                transaction.commit();
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                getItemClick(itemName);
            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {
                getItemClick(itemName);
            }
        });

        getPermissions();
    }

    private void getItemClick(String itemName) {

        switch(itemName){
            case "HOME":
                TimelineFragment fragment = new TimelineFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content, fragment, "");
                ft.commit();
                break;

            case "PROFILE":
                ProfileFragment fragment1 = new ProfileFragment();
                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                ft1.replace(R.id.content, fragment1, "");
                ft1.commit();
                break;

            default:
                Toast.makeText(HomeConsoleActivity.this, "Incorrect Selection", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSelectedFragment() {
        TimelineFragment fragment = new TimelineFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content, fragment, "");
        ft.commit();
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
        }
    }

    public void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            /*User is signed in stay on this activity
            set phone number of logged in user
             */
            uid = user.getUid();

        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //set last seen time

        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

}
