package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.explore_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String exploreItemID = exploreItems.get(position);
        postRef = FirebaseDatabase.getInstance().getReference("Posts");

        getExploreItemDetails(exploreItemID, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postRef.child(exploreItemID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Intent postIntent = new Intent(context, PostListActivity.class);
                            postIntent.putExtra("postID", exploreItemID);
                            context.startActivity(postIntent);
                        }else{
                            userRef = FirebaseDatabase.getInstance().getReference("Users").child(exploreItemID);
                            userRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Intent postIntent = new Intent(context, ViewProfileActivity.class);
                                        postIntent.putExtra("uid", exploreItemID);
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

    private void getExploreItemDetails(String exploreItemID, ViewHolder holder) {
        postRef.child(exploreItemID).addValueEventListener(new ValueEventListener() {
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
                    userRef = FirebaseDatabase.getInstance().getReference("Users").child(exploreItemID);
                    userRef.addValueEventListener(new ValueEventListener() {
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
