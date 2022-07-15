package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.CommentsActivity;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    private Context context;
    private List<PostModel> mPosts;

    private FirebaseUser firebaseUser;
    private DatabaseReference postReference;

    public MediaAdapter(Context context, List<PostModel> mPosts) {
        this.context = context;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.media_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final PostModel modelPost = mPosts.get(position);
        postReference = FirebaseDatabase.getInstance().getReference("SecretPosts");

        if (modelPost.getPostType().equals("imagePost")
                || modelPost.getPostType().equals("audioImagePost")){
            getPostImage(holder, modelPost);
        }else{
            getSharedImagePost(modelPost, holder);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CommentsActivity.class);
                intent.putExtra("postID", modelPost.getPostID());
                context.startActivity(intent);

            }
        });
    }

    private void getPostImage(ViewHolder holder, PostModel modelPost) {

        ArrayList<String> imageURLs = new ArrayList<>();

        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    assert postModel != null;
                    if (postModel.getPostID().equals(modelPost.getPostID())){
                        postReference.child(postModel.getPostID()).child("images").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    if (snapshot.getChildrenCount() == 1){
                                        for (DataSnapshot data : snapshot.getChildren()){
                                            String imageURL = data.getValue().toString();
                                            try{
                                                holder.multipleMediaIcon.setVisibility(View.GONE);
                                                holder.multipleMediaIcon.setImageResource(R.drawable.ic_image_black_24dp);
                                                Picasso.get().load(imageURL).into(holder.post_image);
                                                holder.mediaLoader.setVisibility(View.GONE);
                                            }catch (NullPointerException ignored){}
                                        }
                                    }else{
                                        imageURLs.clear();
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                            imageURLs.add(dataSnapshot.getValue().toString());
                                        }

                                        try{
                                            holder.multipleMediaIcon.setVisibility(View.VISIBLE);
                                            holder.multipleMediaIcon.setImageResource(R.drawable.ic_baseline_collections_24);
                                            Picasso.get().load(imageURLs.get(0)).into(holder.post_image);
                                            holder.mediaLoader.setVisibility(View.GONE);
                                        }catch (NullPointerException ignored){}
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

    private void getSharedImagePost(PostModel modelPost, ViewHolder holder) {

        ArrayList<String> imageURLs = new ArrayList<>();

        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel model = ds.getValue(PostModel.class);

                    assert model != null;
                    if (modelPost.getSharedPost().equals(model.getPostID())){

                        postReference.child(model.getSharedPost()).child("images").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    if (snapshot.getChildrenCount() == 1){
                                        for (DataSnapshot data : snapshot.getChildren()){
                                            String imageURL = data.getValue().toString();
                                            try{
                                                holder.multipleMediaIcon.setVisibility(View.GONE);
                                                holder.multipleMediaIcon.setImageResource(R.drawable.ic_image_black_24dp);
                                                Picasso.get().load(imageURL).into(holder.post_image);
                                                holder.mediaLoader.setVisibility(View.GONE);
                                            }catch (NullPointerException ignored){}
                                        }
                                    }else{
                                        imageURLs.clear();
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                            imageURLs.add(dataSnapshot.getValue().toString());
                                        }

                                        try{
                                            holder.multipleMediaIcon.setVisibility(View.VISIBLE);
                                            holder.multipleMediaIcon.setImageResource(R.drawable.ic_baseline_collections_24);
                                            Picasso.get().load(imageURLs.get(0)).into(holder.post_image);
                                            holder.mediaLoader.setVisibility(View.GONE);
                                        }catch (NullPointerException ignored){}
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

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView post_image, multipleMediaIcon;
        public ProgressBar mediaLoader;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            post_image = itemView.findViewById(R.id.post_image);
            multipleMediaIcon = itemView.findViewById(R.id.mediaMultipleImageIcon);
            mediaLoader = itemView.findViewById(R.id.mediaLoader);
        }
    }


}
