package com.makepe.blackout.GettingStarted;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.MovementInteractionAdapter;
import com.makepe.blackout.GettingStarted.Adapters.UserInteractionAdapter;
import com.makepe.blackout.GettingStarted.InAppActivities.InteractionsActivity;
import com.makepe.blackout.GettingStarted.Models.Movement;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;

public class TimelineSetupActivity extends AppCompatActivity {

    private TextView moreUsers, moreMovements;
    private RecyclerView movementsRecycler, usersRecycler;

    private ArrayList<Movement> movements;
    private ArrayList<User> userList;

    private DatabaseReference userReference, movementReference;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_setup);

        moreUsers = findViewById(R.id.proSetup_seeMoreBTN);
        moreMovements = findViewById(R.id.proSetup_moreMovements);
        movementsRecycler = findViewById(R.id.proSetup_movementsRecycler);
        usersRecycler = findViewById(R.id.proSetup_usersRecycler);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        movementReference = FirebaseDatabase.getInstance().getReference("Movements");

        movements = new ArrayList<>();
        movementsRecycler.hasFixedSize();
        movementsRecycler.setLayoutManager(new GridLayoutManager(TimelineSetupActivity.this, 3));

        userList = new ArrayList<>();
        usersRecycler.hasFixedSize();
        usersRecycler.setLayoutManager(new GridLayoutManager(TimelineSetupActivity.this, 3));

        getMovements();
        getUsers();

        moreUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent movementIntent = new Intent(TimelineSetupActivity.this, InteractionsActivity.class);
                movementIntent.putExtra("userID", firebaseUser.getUid());
                movementIntent.putExtra("interactionType", "seeMoreUsers");
                startActivity(movementIntent);

            }
        });

        moreMovements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent movementIntent = new Intent(TimelineSetupActivity.this, InteractionsActivity.class);
                movementIntent.putExtra("userID", firebaseUser.getUid());
                movementIntent.putExtra("interactionType", "seeMoreMovements");
                startActivity(movementIntent);
            }
        });

        findViewById(R.id.goToTimeline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TimelineSetupActivity.this, MainActivity.class));
            }
        });

    }

    private void getUsers() {
        userReference.limitToFirst(6).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if(!user.getUSER_ID().equals(firebaseUser.getUid()))
                        userList.add(user);
                }
                Collections.shuffle(userList);
                usersRecycler.setAdapter(new UserInteractionAdapter(userList, TimelineSetupActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMovements() {
        movementReference.limitToFirst(6).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                movements.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Movement movement = ds.getValue(Movement.class);

                    if (!movement.getMovementAdmin().equals(firebaseUser.getUid()))
                        movements.add(movement);
                }
                Collections.shuffle(movements);
                movementsRecycler.setAdapter(new MovementInteractionAdapter(movements, TimelineSetupActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}