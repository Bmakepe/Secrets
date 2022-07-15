package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
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
import com.makepe.blackout.GettingStarted.InAppActivities.CommentsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.CommentModel;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.NotiModel;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyHolder> {

    private Context mContext;
    private List<NotiModel> mNotifications;
    private DatabaseReference userReference, postReference, storyReference, commentReference;
    private FirebaseUser firebaseUser;

    private GetTimeAgo getTimeAgo;

    public NotificationAdapter(Context mContext, List<NotiModel> mNotifications) {
        this.mContext = mContext;
        this.mNotifications = mNotifications;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        final NotiModel notification = mNotifications.get(position);
        getTimeAgo = new GetTimeAgo();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        postReference = FirebaseDatabase.getInstance().getReference("Posts");
        storyReference = FirebaseDatabase.getInstance().getReference("Story");
        commentReference = FirebaseDatabase.getInstance().getReference("Comments");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getUserInfo(holder, notification);
        getNotificationContent(holder, notification);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(notification.isPost() || notification.isStory()){
                    Intent intent = new Intent(mContext, CommentsActivity.class);
                    intent.putExtra("postID", notification.getPostID());
                    mContext.startActivity(intent);
                }else if (notification.isFollowing()){
                    Intent intent = new Intent(mContext, ViewProfileActivity.class);
                    intent.putExtra("uid", notification.getUserID());
                    mContext.startActivity(intent);
                }
            }
        });
    }

    private void getNotificationContent(MyHolder holder, NotiModel notification) {

        try{//convert timestamp to x days ago
            holder.timeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(notification.getTimeStamp()), mContext));
        }catch (NumberFormatException n){
            Toast.makeText(mContext, "Could not format time", Toast.LENGTH_SHORT).show();
        }//for converting timestamp

        holder.text.setText(notification.getText());


        if(notification.isPost()){

            postReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()){
                        PostModel model = ds.getValue(PostModel.class);

                        assert model != null;
                        if (model.getPostID().equals(notification.getPostID())){
                            if (model.getPostType().equals("imagePost")
                                    || model.getPostType().equals("audioImagePost")){
                                try{
                                    holder.post_image.setVisibility(View.VISIBLE);
                                    Picasso.get().load(model.getPostImage()).into(holder.post_image);
                                }catch (NullPointerException ignored){}
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }else if (notification.isStory()){
            storyReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Story story = ds.getValue(Story.class);

                        if (story.getStoryID().equals(notification.getPostID())){
                            try{
                                holder.post_image.setVisibility(View.VISIBLE);
                                Picasso.get().load(story.getStoryImage()).into(holder.post_image);
                            }catch (NullPointerException ignored){}
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else if (notification.isComment()){
            commentReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()){
                        CommentModel commentModel = ds.getValue(CommentModel.class);

                        assert commentModel != null;
                        if (commentModel.getCommentID().equals(notification.getPostID()))
                            if (commentModel.getCommentType().equals("imageComment")
                                    || commentModel.getCommentType().equals("audioImageComment"))
                                try{
                                    holder.post_image.setVisibility(View.VISIBLE);
                                    Picasso.get().load(commentModel.getCommentImage()).into(holder.post_image);
                                }catch (NullPointerException ignored){}
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            holder.post_image.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        ImageView image_profile, post_image;
        public TextView username, text, timeStamp;

        MyHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.notiProIMG);
            post_image = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.notiUsername);
            text = itemView.findViewById(R.id.notiComment);
            timeStamp = itemView.findViewById(R.id.notiTimeStamp);

        }
    }

    private void getUserInfo(MyHolder holder, NotiModel notification){
        List<ContactsModel> phoneBook = new ArrayList<>();
        ContactsList contactsList = new ContactsList(phoneBook, mContext);
        contactsList.readContacts();
        final List<ContactsModel> phoneContacts = contactsList.getContactsList();

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        User user = ds.getValue(User.class);

                        if (user.getUSER_ID().equals(notification.getUserID())) {

                            try {
                                Picasso.get().load(user.getImageURL()).into(holder.image_profile);
                            } catch (NullPointerException ignored) {
                            }

                            //username.setText(username1);

                            for (ContactsModel contactsModel : phoneContacts) {
                                if (notification.getUserID().equals(firebaseUser.getUid())) {
                                    holder.username.setText("Me");
                                } else if (contactsModel.getNumber().equals(user.getNumber())) {
                                    holder.username.setText(contactsModel.getUsername());
                                }else{
                                    holder.username.setText(user.getUsername());
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
