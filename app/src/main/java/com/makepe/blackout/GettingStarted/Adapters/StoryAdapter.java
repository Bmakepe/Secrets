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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.AddStoryActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.StoryActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewStoryActivity;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.MyHolder> {
    private Context context;
    private List<Story> mStory;

    private DatabaseReference userReference, storyReference;
    private FirebaseUser firebaseUser;

    public StoryAdapter(Context context, List<Story> mStory) {
        this.context = context;
        this.mStory = mStory;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0){
            return new MyHolder(LayoutInflater.from(context).inflate(R.layout.add_story_item, parent, false));
        }else{
            return new MyHolder(LayoutInflater.from(context).inflate(R.layout.item_story, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {

        final Story story = mStory.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        storyReference = FirebaseDatabase.getInstance().getReference("Story");

        userInfo(holder, position, story);

        if(holder.getAdapterPosition() != 0){
            seenStory(holder, story.getUserID());
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
                    //Intent intent = new Intent(context, ViewStoryActivity.class);
                    intent.putExtra("userid", story.getUserID());
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

    private void userInfo(final MyHolder viewHolder, final int pos, Story story) {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUserID().equals(story.getUserID())){

                        viewHolder.story_username.setText(user.getUsername());

                        try{
                            Picasso.get().load(user.getImageURL()).into(viewHolder.story_photo);
                            Picasso.get().load(user.getCoverURL()).into(viewHolder.storyCoverPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(viewHolder.story_photo);
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(viewHolder.storyCoverPic);
                        }

                        if (pos != 0){
                            try{
                                Picasso.get().load(user.getImageURL()).into(viewHolder.story_photo_seen);
                            }catch (NullPointerException e){
                                Picasso.get().load(R.drawable.default_profile_display_pic).into(viewHolder.story_photo_seen);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void myStory(final TextView textView, final ImageView imageView, final boolean click){

        storyReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long timecurrent = System.currentTimeMillis();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Story story = snapshot.getValue(Story.class);
                    assert story != null;
                    if(timecurrent > story.getTimeStart() && timecurrent < story.getTimeEnd()){
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
                                        intent.putExtra("userid", firebaseUser.getUid());
                                        context.startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(context, AddStoryActivity.class);
                                        intent.putExtra("userid", firebaseUser.getUid());
                                        context.startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }else{
                        Intent intent = new Intent(context, AddStoryActivity.class);
                        intent.putExtra("userid", firebaseUser.getUid());
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
        storyReference.child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    int i = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (!snapshot.child("views")
                                .child(firebaseUser.getUid()).exists()
                                && System.currentTimeMillis() < snapshot.getValue(Story.class).getTimeEnd()) {
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
