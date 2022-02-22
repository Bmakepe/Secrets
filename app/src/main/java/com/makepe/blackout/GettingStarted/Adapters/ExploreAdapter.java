package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.PostDetailActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.PostListActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ViewHolder> {
    private ArrayList<String> exploreItems;
    private Context context;

    private DatabaseReference postRef, userRef;

    public ExploreAdapter(ArrayList<String> exploreItems, Context context) {
        this.exploreItems = exploreItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.explore_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        postRef = FirebaseDatabase.getInstance().getReference("Posts");
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        getExploreItemDetails(position, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postRef.child(exploreItems.get(position)).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            //Intent postIntent = new Intent(context, PostDetailActivity.class);
                            Intent postIntent = new Intent(context, PostListActivity.class);
                            postIntent.putExtra("postID", exploreItems.get(position));
                            context.startActivity(postIntent);
                        }else{
                            userRef.child(exploreItems.get(position)).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Intent postIntent = new Intent(context, ViewProfileActivity.class);
                                        postIntent.putExtra("uid", exploreItems.get(position));
                                        context.startActivity(postIntent);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private void getExploreItemDetails(int position, ViewHolder holder) {
        postRef.child(exploreItems.get(position)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel postModel = snapshot.getValue(PostModel.class);
                    try {
                        assert postModel != null;
                        Picasso.get().load(postModel.getPostImage()).into(holder.exploreImage);
                        holder.imageLoader.setVisibility(View.GONE);
                    }catch (NullPointerException ignored){}
                }else{
                    userRef.child(exploreItems.get(position)).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                User user = snapshot.getValue(User.class);

                                try{
                                    assert user != null;
                                    Picasso.get().load(user.getImageURL()).into(holder.exploreImage);
                                    holder.imageLoader.setVisibility(View.GONE);
                                }catch (NullPointerException ignored){}
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return exploreItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView exploreImage;
        ProgressBar imageLoader;
        RelativeLayout exploreImageArea;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            exploreImage = itemView.findViewById(R.id.exploreImage);
            imageLoader = itemView.findViewById(R.id.exploreImageLoader);
            exploreImageArea = itemView.findViewById(R.id.exploreImageArea);

        }
    }
}
