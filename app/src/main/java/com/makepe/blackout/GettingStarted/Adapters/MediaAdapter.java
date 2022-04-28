package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.makepe.blackout.GettingStarted.InAppActivities.CommentsActivity;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    private UniversalFunctions postAdapter;

    public MediaAdapter() {
    }

    private Context context;
    private List<PostModel> mPosts;

    private FirebaseUser firebaseUser;

    public MediaAdapter(Context context, List<PostModel> mPosts) {
        this.context = context;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.media_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final PostModel modelPost = mPosts.get(position);
        postAdapter = new UniversalFunctions(context);

        Glide.with(context)
                .load(modelPost.getPostImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .error(R.drawable.default_profile_display_pic)
                .into(holder.post_image);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent intent = new Intent(context, PostDetailActivity.class);
                Intent intent = new Intent(context, CommentsActivity.class);
                intent.putExtra("postID", modelPost.getPostID());
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView post_image;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            post_image = itemView.findViewById(R.id.post_image);
        }
    }
}
