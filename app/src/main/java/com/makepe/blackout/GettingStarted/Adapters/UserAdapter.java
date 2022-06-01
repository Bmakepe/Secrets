package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.makepe.blackout.GettingStarted.InAppActivities.ChatActivity;
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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> userList;
    private String choice;

    private DatabaseReference userRef;
    private FirebaseUser firebaseUser;

    private UniversalFunctions universalFunctions;
    private UniversalNotifications notifications;
    private FollowInteraction followInteraction;

    public UserAdapter(Context context, ArrayList<String> userList, String choice) {
        this.context = context;
        this.userList = userList;
        this.choice = choice;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String userID = userList.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        universalFunctions = new UniversalFunctions(context);
        notifications = new UniversalNotifications(context);
        followInteraction = new FollowInteraction(context);

        if (userID.equals(firebaseUser.getUid())){
            holder.followBTN.setVisibility(View.GONE);
        }

        getUserDetails(userID, holder);
        followInteraction.updateFollowing(userID, holder.followBTN);
        universalFunctions.checkActiveStories(holder.userProPic, userID);

        holder.userProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (holder.userProPic.getTag().equals("storyActive")){
                    Intent intent = new Intent(context, StoryActivity.class);
                    intent.putExtra("userid", userID);
                    context.startActivity(intent);
                }else if (holder.userProPic.getTag().equals("noStories")){
                    Intent picIntent = new Intent(context, FullScreenImageActivity.class);
                    picIntent.putExtra("itemID", userID);
                    picIntent.putExtra("reason", "userImage");
                    context.startActivity(picIntent);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (choice){
                    case "goToProfile":
                        userRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot snap : snapshot.getChildren()){
                                    User user = snap.getValue(User.class);

                                    assert user != null;
                                    if (user.getUSER_ID().equals(userID)) {
                                        if (!user.getUSER_ID().equals(firebaseUser.getUid())) {
                                            Intent intent = new Intent(context, ViewProfileActivity.class);
                                            intent.putExtra("uid", user.getUSER_ID());
                                            context.startActivity(intent);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        break;

                    case "goToChats":
                        Intent messageIntent = new Intent(context, ChatActivity.class);
                        messageIntent.putExtra("userid", userID);
                        context.startActivity(messageIntent);
                        break;

                    default:
                        Toast.makeText(context, "Unknown Selection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.followBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (followInteraction.checkFollowing(userID))
                    followInteraction.unFollowUser(userID);
                else
                    followInteraction.followUser(userID);
            }
        });
    }

    private void checkFollowing(String userID, ViewHolder holder) {
        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference()
                .child("Follow")
                .child(firebaseUser.getUid())
                .child("following");
        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(userID).exists()){
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

    private void getUserDetails(String userID, ViewHolder holder) {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user1 = ds.getValue(User.class);

                    assert user1 != null;
                    if (user1.getUSER_ID().equals(userID)){

                        if (userID.equals(firebaseUser.getUid())){
                            holder.usernameTV.setText("Me");
                        }else{
                            holder.usernameTV.setText(user1.getUsername());
                        }

                        try{
                            Picasso.get().load(user1.getImageURL()).into(holder.userProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.userProPic);
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

        private CircleImageView userProPic;
        private TextView followBTN, usernameTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userProPic = itemView.findViewById(R.id.userItemProPic);
            followBTN = itemView.findViewById(R.id.userItemFollow);
            usernameTV = itemView.findViewById(R.id.userItemContactName);

        }
    }
}
