package com.makepe.blackout.GettingStarted.Adapters;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.CommentsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.NotiModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyHolder> {

    private Context mContext;
    private List<NotiModel> mNotifications;
    private DatabaseReference userReference;
    private FirebaseUser firebaseUser;

    private GetTimeAgo getTimeAgo;

    public NotificationAdapter(Context mContext, List<NotiModel> mNotifications) {
        this.mContext = mContext;
        this.mNotifications = mNotifications;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);
        return new NotificationAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        final NotiModel notification = mNotifications.get(position);
        getTimeAgo = new GetTimeAgo();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        holder.text.setText(notification.getText());

        getUserInfo(holder, notification);

        try{//convert timestamp to x days ago
            holder.timeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(notification.getTimeStamp()), mContext));
        }catch (NumberFormatException n){
            Toast.makeText(mContext, "Could not format time", Toast.LENGTH_SHORT).show();
        }//for converting timestamp

        if(notification.isIspost()){
            holder.post_image.setVisibility(View.VISIBLE);
            getPostImage(holder.post_image, notification.getPostid());
        }else{
            holder.post_image.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(notification.isIspost() || notification.isStory()){
                    Toast.makeText(mContext, "Post Notification", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(mContext, CommentsActivity.class);
                    intent.putExtra("postID", notification.getPostid());
                    mContext.startActivity(intent);
                }else{
                    Intent intent = new Intent(mContext, ViewProfileActivity.class);
                    intent.putExtra("uid", notification.getUserid());
                    mContext.startActivity(intent);
                }
            }
        });
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

                        if (user.getUSER_ID().equals(notification.getUserid())) {

                            try {
                                Picasso.get().load(user.getImageURL()).into(holder.image_profile);
                            } catch (NullPointerException ignored) {
                            }

                            //username.setText(username1);

                            for (ContactsModel contactsModel : phoneContacts) {
                                if (notification.getUserid().equals(firebaseUser.getUid())) {
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

    private void getPostImage(final ImageView imageView, String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                PostModel post = dataSnapshot.getValue(PostModel.class);
                try{
                    Picasso.get().load(post.getPostImage()).into(imageView);
                }catch (NullPointerException ignore){}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
