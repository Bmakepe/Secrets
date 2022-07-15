package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenVideoActivity;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class VideoItemAdapter extends RecyclerView.Adapter<VideoItemAdapter.ViewHolder> {
    private List<PostModel> videoList;
    private Context context;

    DatabaseReference userRef, videoReference;
    UniversalFunctions universalFunctions;

    public VideoItemAdapter(List<PostModel> videoList, Context context) {
        this.videoList = videoList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_item_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        PostModel post = videoList.get(position);
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        videoReference = FirebaseDatabase.getInstance().getReference("SecretPosts");
        universalFunctions = new UniversalFunctions(context);

        universalFunctions.checkVideoViewCount(holder.videoViews, post);

        if (post.getPostType().equals("videoPost") || post.getPostType().equals("audioVideoPost")){
            getVideoOwnerDetails(post, holder);
            getVideoDetails(post, holder);
        }else{
            getSharedVideoPostDetails(post, holder);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Intent videoIntent = new Intent(context, FullScreenVideoActivity.class);
                videoIntent.putExtra("videoID", post.getPostID());
                context.startActivity(videoIntent);*/

            }
        });
    }

    private void getSharedVideoPostDetails(PostModel post, ViewHolder holder) {
        videoReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        PostModel sharedVideo = ds.getValue(PostModel.class);

                        assert sharedVideo != null;
                        if (sharedVideo.getPostID().equals(post.getSharedPost())) {

                            getVideoOwnerDetails(sharedVideo, holder);
                            getVideoDetails(sharedVideo, holder);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getVideoOwnerDetails(PostModel post, ViewHolder holder) {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUSER_ID().equals(post.getUserID()))
                        try{
                            Picasso.get().load(user.getImageURL()).into(holder.videoOwnPic);
                        }catch (NullPointerException ignored){}
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getVideoDetails(PostModel post, ViewHolder holder) {

        holder.exploreVideoView.setVideoURI(Uri.parse(post.getVideoURL()));
        holder.exploreVideoView.setOnPreparedListener(mediaPlayer -> {

            mediaPlayer.setVolume(0f, 0f);
            mediaPlayer.setLooping(true);

        });

        holder.exploreVideoView.start();
        holder.videoLoader.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        VideoView exploreVideoView;
        TextView videoViews;
        CircleImageView videoOwnPic;
        ProgressBar videoLoader;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            exploreVideoView = itemView.findViewById(R.id.exploreVideoView);
            videoViews = itemView.findViewById(R.id.videoViewsCount);
            videoOwnPic = itemView.findViewById(R.id.videoOwnerPic);
            videoLoader = itemView.findViewById(R.id.videoItemLoader);
        }
    }
}
