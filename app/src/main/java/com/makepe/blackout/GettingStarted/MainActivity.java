package com.makepe.blackout.GettingStarted;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.makepe.blackout.GettingStarted.InAppActivities.CameraActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.CreateGroupActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.MessagesActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.MovementsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.MyContactsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.NewMovementActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.PostActivity;
import com.makepe.blackout.R;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton startFAB, newPostFAB, viewMovementsFAB, openContactsFAB;
    Animation fabOpen, fabClose, rotateForward, rotateBackward;
    boolean isOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavController navController = Navigation.findNavController(this, R.id.fragmentContainerView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        startFAB = findViewById(R.id.startFAB);
        newPostFAB = findViewById(R.id.newPostFAB);
        viewMovementsFAB = findViewById(R.id.viewMovementsFAB);
        openContactsFAB = findViewById(R.id.openContactsFAB);

        //animations
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);

        rotateForward = AnimationUtils.loadAnimation(this, R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(this, R.anim.rotate_backward);

        startFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateFab();
            }
        });

        newPostFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFAB();
                //startActivity(new Intent(MainActivity.this, PostActivity.class));
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
            }
        });

        viewMovementsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFAB();
                startActivity(new Intent(MainActivity.this, MovementsActivity.class));
            }
        });

        openContactsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFAB();
                startActivity(new Intent(MainActivity.this, MyContactsActivity.class));
            }
        });
    }

    private void animateFab(){
        if (isOpen){
            closeFAB();
        }else{
            openFAB();
        }
    }

    private void openFAB() {
        startFAB.startAnimation(rotateBackward);
        newPostFAB.startAnimation(fabOpen);
        viewMovementsFAB.startAnimation(fabOpen);
        openContactsFAB.startAnimation(fabOpen);

        newPostFAB.setClickable(true);
        viewMovementsFAB.setClickable(true);
        openContactsFAB.setClickable(true);

        isOpen = true;
    }

    private void closeFAB() {
        startFAB.startAnimation(rotateForward);
        newPostFAB.startAnimation(fabClose);
        viewMovementsFAB.startAnimation(fabClose);
        openContactsFAB.startAnimation(fabClose);

        newPostFAB.setClickable(false);
        viewMovementsFAB.setClickable(false);
        openContactsFAB.setClickable(false);

        isOpen = false;
    }
}