package com.makepe.blackout.GettingStarted.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.makepe.blackout.GettingStarted.InAppActivities.PosteActivity;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostMediaAdapter extends RecyclerView.Adapter<PostMediaAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> mediaList;
    private CardView imageCardView;

    public PostMediaAdapter(Context context, ArrayList<String> mediaList, CardView imageCardView) {
        this.context = context;
        this.mediaList = mediaList;
        this.imageCardView = imageCardView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.post_image_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Uri imageURL = Uri.parse(mediaList.get(position));
        Picasso.get().load(imageURL).into(holder.picToUpload);

        holder.removeImagesBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaList.remove(position);
                notifyDataSetChanged();

                if (mediaList.isEmpty())
                    imageCardView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView picToUpload, removeImagesBTN;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            picToUpload = itemView.findViewById(R.id.picToUpload);
            removeImagesBTN = itemView.findViewById(R.id.removeImagesBTN);
        }
    }
}
