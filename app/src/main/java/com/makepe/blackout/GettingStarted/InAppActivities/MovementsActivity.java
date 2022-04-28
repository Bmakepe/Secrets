package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.MovementAdapter;
import com.makepe.blackout.GettingStarted.Models.Movement;
import com.makepe.blackout.R;

import java.util.ArrayList;

public class MovementsActivity extends AppCompatActivity {

    private RecyclerView movementRecycler;
    private ArrayList<Movement> movements;

    private DatabaseReference movementReference;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movements);

        Toolbar movementToolbar = findViewById(R.id.movementToolbar);
        setSupportActionBar(movementToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        movementRecycler = findViewById(R.id.movementRecycler);

        movementReference = FirebaseDatabase.getInstance().getReference("Movements");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        movements = new ArrayList<>();

        movementRecycler.hasFixedSize();
        movementRecycler.setLayoutManager(new GridLayoutManager(this, 3));

        getMovements();

    }

    private void getMovements() {
        movementReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                movements.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    Movement movement = ds.getValue(Movement.class);

                    assert movement != null;
                    if (movement.getMovementPrivacy().equals("Public")
                            || movement.getMovementAdmin().equals(firebaseUser.getUid()))
                        movements.add(movement);
                }
                movementRecycler.setAdapter(new MovementAdapter(MovementsActivity.this, movements));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movement_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.newMovement:
                Toast.makeText(this, "New Movement Campaign", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MovementsActivity.this, NewMovementActivity.class));
                break;

            case R.id.app_bar_search:
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                break;

            default:
                Toast.makeText(this, "unknown menu selection", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}