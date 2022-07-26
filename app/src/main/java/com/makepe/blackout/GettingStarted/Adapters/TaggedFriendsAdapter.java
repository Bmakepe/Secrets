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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class TaggedFriendsAdapter extends RecyclerView.Adapter<TaggedFriendsAdapter.ViewHolder> {


    private Context context;
    private ArrayList<User> users;

    private DatabaseReference userReference;

    public TaggedFriendsAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.tagged_user_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        getUserDetails(holder, user);

        holder.removeTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                users.remove(user);
                notifyDataSetChanged();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewProfileActivity.class);
                intent.putExtra("uid", user.getUserID());
                context.startActivity(intent);
            }
        });

    }

    private void getUserDetails(ViewHolder holder, User user) {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User myUser = ds.getValue(User.class);

                    if (myUser.getUserID().equals(user.getUserID())){
                        holder.userName.setText(myUser.getUsername());

                        try{
                            Picasso.get().load(myUser.getImageURL()).into(holder.userImage);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.userImage);
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
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView userImage;
        public TextView userName;
        public ImageView removeTag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.taggedUserImage);
            userName = itemView.findViewById(R.id.taggedUsername);
            removeTag = itemView.findViewById(R.id.removeTag);
        }
    }
}
