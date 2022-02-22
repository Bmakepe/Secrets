package com.makepe.blackout.GettingStarted.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.AddStoryActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.StoryActivity;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.MyHolder> {
    private Context context;
    private List<Story> mStory;

    public StoryAdapter(Context context, List<Story> mStory) {
        this.context = context;
        this.mStory = mStory;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == 0){
            view = LayoutInflater.from(context).inflate(R.layout.add_story_item, parent, false);
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.item_story, parent, false);
        }
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {

        final Story story = mStory.get(position);

        userInfo(holder, position, story.getUserid());

        if(holder.getAdapterPosition() != 0){
            seenStory(holder, story.getUserid());
        }

        if(holder.getAdapterPosition() == 0){
            myStory(holder.addstory_text, holder.story_plus, false);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.getAdapterPosition() == 0){
                    myStory(holder.addstory_text, holder.story_plus, true);
                }else{
                    //TODO: go to story
                    Intent intent = new Intent(context, StoryActivity.class);
                    intent.putExtra("userid", story.getUserid());
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mStory.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView story_photo, story_plus, story_photo_seen, storyCoverPic;
        TextView story_username, addstory_text;

        MyHolder(@NonNull View itemView) {
            super(itemView);

            story_photo = itemView.findViewById(R.id.story_photo);
            story_plus = itemView.findViewById(R.id.story_plus);
            story_photo_seen = itemView.findViewById(R.id.story_photo_seen);
            story_username = itemView.findViewById(R.id.story_username);
            addstory_text = itemView.findViewById(R.id.addstory_text);
            storyCoverPic = itemView.findViewById(R.id.storyBackGround);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return 0;
        }
        return 1;
    }

    private void userInfo(final MyHolder viewHolder, final int pos, String userID){
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = reference.orderByChild("USER_ID").equalTo(userID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    for(DataSnapshot ds : dataSnapshot.getChildren()){

                        final String proPic = "" + ds.child("ImageURL").getValue();
                        String userName = "" + ds.child("Username").getValue();
                        final String coverPic = "" + ds.child("CoverURL").getValue();


                        Picasso.get().load(proPic).networkPolicy(NetworkPolicy.OFFLINE).into(viewHolder.story_photo, new Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(proPic).into(viewHolder.story_photo);
                            }
                        });

                        try{
                            Picasso.get().load(coverPic).networkPolicy(NetworkPolicy.OFFLINE).into(viewHolder.storyCoverPic, new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(coverPic).into(viewHolder.storyCoverPic);
                                }
                            });
                        }catch (IllegalArgumentException ignored){}

                        if(pos != 0){

                            Picasso.get().load(proPic).networkPolicy(NetworkPolicy.OFFLINE).into(viewHolder.story_photo_seen, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {

                                    Picasso.get().load(proPic).into(viewHolder.story_photo_seen);
                                }
                            });
                            viewHolder.story_username.setText(userName);
                        }
                    }
                }else{
                    Toast.makeText(context, "Cannot find relevant data", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void myStory(final TextView textView, final ImageView imageView, final boolean click){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long timecurrent = System.currentTimeMillis();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Story story = snapshot.getValue(Story.class);
                    assert story != null;
                    if(timecurrent > story.getTimestart() && timecurrent < story.getTimeend()){
                        count++;
                    }
                }
                if(click){
                    if(count > 0){
                        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Story",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(context, StoryActivity.class);
                                        intent.putExtra("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        context.startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(context, AddStoryActivity.class);
                                        context.startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }else{
                        Intent intent = new Intent(context, AddStoryActivity.class);
                        context.startActivity(intent);
                    }

                }else{
                    if(count > 0){
                        textView.setText("My Story");
                        imageView.setVisibility(View.GONE);
                    }else{
                        textView.setText("Add Story");
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenStory(final MyHolder viewHolder, String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    int i = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (!snapshot.child("views")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .exists() && System.currentTimeMillis() < snapshot.getValue(Story.class).getTimeend()) {
                            i++;
                        }
                    }

                    if (i > 0) {
                        viewHolder.story_photo.setVisibility(View.VISIBLE);
                        viewHolder.story_photo_seen.setVisibility(View.GONE);
                    } else {
                        viewHolder.story_photo.setVisibility(View.GONE);
                        viewHolder.story_photo_seen.setVisibility(View.VISIBLE);
                    }
                }else{
                    Toast.makeText(context, "Cannot find relevant data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
