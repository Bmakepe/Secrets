package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenImageActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.MovementDetailsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.StoryActivity;
import com.makepe.blackout.GettingStarted.Models.Movement;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalNotifications;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MovementInteractionAdapter extends RecyclerView.Adapter<MovementInteractionAdapter.ViewHolder> {

    private ArrayList<Movement> movements;
    private Context context;

    private DatabaseReference movementReference;
    private FirebaseUser firebaseUser;

    private UniversalFunctions universalFunctions;
    private UniversalNotifications notifications;

    public MovementInteractionAdapter(ArrayList<Movement> movements, Context context) {
        this.movements = movements;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_interaction_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movement movement = movements.get(position);
        movementReference = FirebaseDatabase.getInstance().getReference("Movements");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        universalFunctions = new UniversalFunctions(context);
        notifications = new UniversalNotifications(context);

        getMovementDetails(movement, holder);
        checkFollow(movement, holder);
        universalFunctions.checkActiveStories(holder.userProPic, movement.getMovementID());

        holder.followBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.followBTN.getText().equals("Follow")){

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(movement.getMovementID()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(movement.getMovementID())
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                    Toast.makeText(context, "Following", Toast.LENGTH_SHORT).show();
                    notifications.addFollowNotifications(movement.getMovementID());
                }else{

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(movement.getMovementID()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(movement.getMovementID())
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent movementIntent = new Intent(context, MovementDetailsActivity.class);
                movementIntent.putExtra("movementID", movement.getMovementID());
                context.startActivity(movementIntent);
            }
        });

        holder.userProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.userProPic.getTag().equals("storyActive")){
                    Intent storyIntent = new Intent(context, StoryActivity.class);
                    storyIntent.putExtra("userid", movement.getMovementID());
                    context.startActivity(storyIntent);
                } else if (holder.userProPic.getTag().equals("noStories")) {
                    Intent imageIntent = new Intent(context, FullScreenImageActivity.class);
                    imageIntent.putExtra("itemID", movement.getMovementID());
                    imageIntent.putExtra("reason", "userImage");
                    context.startActivity(imageIntent);
                }
            }
        });

        holder.userCoverPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent coverIntent = new Intent(context, FullScreenImageActivity.class);
                coverIntent.putExtra("itemID", movement.getMovementID());
                coverIntent.putExtra("reason", "coverImage");
                context.startActivity(coverIntent);
            }
        });

    }

    private void checkFollow(Movement movement, ViewHolder holder) {
        DatabaseReference followRef = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");

        followRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(movement.getMovementID()).exists()){
                    holder.followBTN.setText("Following");
                }else{
                    holder.followBTN.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMovementDetails(Movement movement, ViewHolder holder) {
        movementReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Movement myMovement = ds.getValue(Movement.class);

                    if (myMovement.getMovementID().equals(movement.getMovementID())){

                        holder.userName.setText(myMovement.getMovementName());

                        try{
                            Picasso.get().load(myMovement.getMovementProPic()).into(holder.userProPic);
                            Picasso.get().load(myMovement.getMovementCoverPic()).into(holder.userCoverPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.userProPic);
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.userCoverPic);
                        }
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

        public TextView userName, followBTN;
        public ImageView userCoverPic;
        public CircleImageView userProPic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.interactionUserUsername);
            followBTN = itemView.findViewById(R.id.interactionFollowBTN);
            userProPic = itemView.findViewById(R.id.interactionUserProfilePic);
            userCoverPic = itemView.findViewById(R.id.interactionUserCoverImage);
        }
    }
}
