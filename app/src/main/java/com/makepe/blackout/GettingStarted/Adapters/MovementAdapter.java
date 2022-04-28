package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.MovementDetailsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.NewMovementActivity;
import com.makepe.blackout.GettingStarted.Models.Movement;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MovementAdapter extends RecyclerView.Adapter<MovementAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Movement> movements;

    private DatabaseReference movementRef;
    private UniversalFunctions universalFunctions;

    public MovementAdapter() {
    }

    public MovementAdapter(Context context, ArrayList<Movement> movements) {
        this.context = context;
        this.movements = movements;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.movement_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movement movement = movements.get(position);
        movementRef = FirebaseDatabase.getInstance().getReference("Movements");
        universalFunctions = new UniversalFunctions(context);

        getMovementDetails(movement, holder);
        universalFunctions.checkActiveStories(holder.profilePic, movement.getMovementID());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MovementDetailsActivity.class);
                intent.putExtra("movementID", movement.getMovementID());
                context.startActivity(intent);
            }
        });
    }

    private void getMovementDetails(Movement movement, ViewHolder holder) {
        movementRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Movement myMovement = ds.getValue(Movement.class);

                    assert myMovement != null;
                    if (myMovement.getMovementID().equals(movement.getMovementID())){
                        try{
                            Picasso.get().load(myMovement.getMovementCoverPic()).into(holder.coverPic);
                            Picasso.get().load(myMovement.getMovementProPic()).into(holder.profilePic);
                        }catch (NullPointerException e) {
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.coverPic);
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.profilePic);
                        }
                        holder.movementName.setText(myMovement.getMovementName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return movements.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView coverPic;
        public CircleImageView profilePic;
        public TextView movementName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            coverPic = itemView.findViewById(R.id.movement_item_cover_picture);
            profilePic = itemView.findViewById(R.id.movement_item_profile_picture);
            movementName = itemView.findViewById(R.id.movement_item_name);
        }
    }
}
