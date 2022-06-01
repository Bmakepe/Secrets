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
import com.makepe.blackout.GettingStarted.InAppActivities.StoryActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.FollowInteraction;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalNotifications;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInteractionAdapter extends RecyclerView.Adapter<UserInteractionAdapter.ViewHolder>{

    private ArrayList<User> userList;
    private Context context;

    private DatabaseReference userRef;
    private FirebaseUser firebaseUser;

    private UniversalFunctions universalFunctions;
    private UniversalNotifications notifications;
    private FollowInteraction followInteraction;

    public UserInteractionAdapter(ArrayList<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_interaction_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        universalFunctions = new UniversalFunctions(context);
        notifications = new UniversalNotifications(context);
        followInteraction = new FollowInteraction(context);

        getUserDetails(user, holder);
        checkFollow(user, holder);
        universalFunctions.checkActiveStories(holder.userProPic, user.getUSER_ID());

        holder.followBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (followInteraction.checkFollowing(user.getUSER_ID()))
                    followInteraction.unFollowUser(user.getUSER_ID());
                else
                    followInteraction.followUser(user.getUSER_ID());
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewProfileActivity.class);
                intent.putExtra("uid", user.getUSER_ID());
                context.startActivity(intent);
            }
        });

        holder.userProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.userProPic.getTag().equals("storyActive")){
                    Intent storyIntent = new Intent(context, StoryActivity.class);
                    storyIntent.putExtra("userid", user.getUSER_ID());
                    context.startActivity(storyIntent);
                } else if (holder.userProPic.getTag().equals("noStories")) {
                    Intent imageIntent = new Intent(context, FullScreenImageActivity.class);
                    imageIntent.putExtra("itemID", user.getUSER_ID());
                    imageIntent.putExtra("reason", "userImage");
                    context.startActivity(imageIntent);
                }
            }
        });

        holder.userCoverPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent coverIntent = new Intent(context, FullScreenImageActivity.class);
                coverIntent.putExtra("itemID", user.getUSER_ID());
                coverIntent.putExtra("reason", "coverImage");
                context.startActivity(coverIntent);
            }
        });

    }

    private void checkFollow(User user, ViewHolder holder) {
        DatabaseReference followRef = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");

        followRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(user.getUSER_ID()).exists()){
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

    private void getUserDetails(User user, ViewHolder holder) {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User myUser = ds.getValue(User.class);

                    if (myUser.getUSER_ID().equals(user.getUSER_ID())){
                        holder.userName.setText(myUser.getUsername());

                        try{
                            Picasso.get().load(myUser.getImageURL()).into(holder.userProPic);
                            Picasso.get().load(myUser.getCoverURL()).into(holder.userCoverPic);
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
        return userList.size();
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
