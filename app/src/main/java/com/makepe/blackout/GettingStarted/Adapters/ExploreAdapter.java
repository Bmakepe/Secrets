package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ViewHolder> {

    private final ArrayList<String> exploreItems;
    private final Context context;

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
        postRef = FirebaseDatabase.getInstance().getReference("SecretPosts");
        userRef = FirebaseDatabase.getInstance().getReference("Users");

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
                            userRef.child(exploreItemID).addValueEventListener(new ValueEventListener() {
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

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        PostModel model = ds.getValue(PostModel.class);

                        assert model != null;
                        if (model.getPostID().equals(exploreItemID)) {
                            getPostItem(holder, model);
                        } else {
                            getUserItem(holder, exploreItemID);
                        }
                    }
                }else{
                    getUserItem(holder, exploreItemID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserItem(ViewHolder holder, String exploreItemID) {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        User user = ds.getValue(User.class);

                        assert user != null;
                        if (user.getUserID().equals(exploreItemID)){
                            holder.exploreItemOwner.setText(user.getUsername());

                            try{
                                Picasso.get().load(user.getImageURL()).into(holder.exploreImage);
                                holder.imageLoader.setVisibility(View.GONE);
                                holder.multipleImageIcon.setImageResource(R.drawable.ic_baseline_person_24);
                            }catch (NullPointerException ignored){}
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPostItem(ViewHolder holder, PostModel model) {

        ArrayList<String> imageURL = new ArrayList<>();

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel myPost = ds.getValue(PostModel.class);

                    assert myPost != null;
                    if (myPost.getPostID().equals(model.getPostID())){

                        postRef.child(myPost.getPostID()).child("images").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    if (snapshot.getChildrenCount() == 1){
                                        for (DataSnapshot ds : snapshot.getChildren()){
                                            String imageURL = ds.getValue().toString();
                                            try{
                                                holder.multipleImageIcon.setImageResource(R.drawable.ic_image_black_24dp);
                                                Picasso.get().load(imageURL).into(holder.exploreImage);
                                                holder.imageLoader.setVisibility(View.GONE);
                                            }catch (NullPointerException ignored){}

                                            getPostOwner(myPost, holder);

                                        }
                                    }else{
                                        imageURL.clear();
                                        for (DataSnapshot ds : snapshot.getChildren()){
                                            imageURL.add(ds.getValue().toString());
                                        }

                                        try{
                                            holder.multipleImageIcon.setImageResource(R.drawable.ic_baseline_collections_24);
                                            Picasso.get().load(imageURL.get(0)).into(holder.exploreImage);
                                            holder.imageLoader.setVisibility(View.GONE);
                                        }catch (NullPointerException ignored){}

                                        getPostOwner(myPost, holder);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPostOwner(PostModel myPost, ViewHolder holder) {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()){
                    User user = data.getValue(User.class);

                    if (user.getUserID().equals(myPost.getUserID())){
                        holder.exploreItemOwner.setText(user.getUsername());
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
        return exploreItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView exploreImage, multipleImageIcon;
        public ProgressBar imageLoader;
        public RelativeLayout exploreImageArea;
        public TextView exploreItemOwner;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            exploreImage = itemView.findViewById(R.id.exploreImage);
            multipleImageIcon = itemView.findViewById(R.id.multipleImageIcon);
            imageLoader = itemView.findViewById(R.id.exploreImageLoader);
            exploreImageArea = itemView.findViewById(R.id.exploreImageArea);
            exploreItemOwner = itemView.findViewById(R.id.exploreItemOwner);

        }
    }
}
