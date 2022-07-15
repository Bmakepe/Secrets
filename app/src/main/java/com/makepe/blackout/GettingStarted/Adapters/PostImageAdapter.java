package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenPictureActivity;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostImageAdapter extends RecyclerView.Adapter<PostImageAdapter.ViewHolder>{
    
    private Context context;
    private ArrayList<String> imageList;

    public PostImageAdapter(Context context, ArrayList<String> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.post_image_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageURL = imageList.get(position);
        Picasso.get().load(Uri.parse(imageURL)).into(holder.post_item_item);
        holder.post_item_pic_loader.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FullScreenPictureActivity.class);
                intent.putExtra("imageURL", imageURL);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView post_item_item;
        public ProgressBar post_item_pic_loader;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            post_item_item = itemView.findViewById(R.id.post_item_item);
            post_item_pic_loader = itemView.findViewById(R.id.post_item_pic_loader);

        }
    }
}
