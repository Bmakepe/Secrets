package com.makepe.blackout.GettingStarted.Adapters;

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
import com.makepe.blackout.GettingStarted.InAppActivities.ConnectionsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.CommentModel;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyHolder> {

    private Context context;
    private List<CommentModel> commentList;

    private DatabaseReference userReference, commentsRef;
    private FirebaseUser firebaseUser;

    GetTimeAgo timeAgo;

    UniversalFunctions universalFunctions;

    public CommentsAdapter() {
    }

    public CommentsAdapter(Context context, List<CommentModel> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate comment layout xml
        View view = LayoutInflater.from(context).inflate(R.layout.comment_layout, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get the data
        CommentModel comments = commentList.get(position);
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        commentsRef = FirebaseDatabase.getInstance().getReference("Comments");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        timeAgo = new GetTimeAgo();
        universalFunctions = new UniversalFunctions(context);

        //universalFunctions.isLiked(comments.getCommentID(), holder.likesBTN);
        //universalFunctions.nrLikes(holder.likesCounter, comments.getCommentID());

        try{//convert timestamp
            String pTime = timeAgo.getTimeAgo(Long.parseLong(comments.getTimeStamp()), context);
            holder.commentTime.setText(pTime);
            holder.commentCaption.setText(comments.getComment());
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }

        getCommentInfo(comments, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!comments.getUserID().equals(firebaseUser.getUid())) {
                    Intent intent = new Intent(context, ViewProfileActivity.class);
                    intent.putExtra("uid", comments.getUserID());
                    context.startActivity(intent);
                }

            }
        });

        holder.likesBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "you will be able to like this comment", Toast.LENGTH_SHORT).show();
                /*if(holder.likesBTN.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(comments.getCommentID())
                            .child(firebaseUser.getUid()).setValue(true);
                    //universalFunctions.addLikesNotifications(post.getUserID(), post.getPostID());
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(comments.getCommentID())
                            .child(firebaseUser.getUid()).removeValue();
                }*/
            }
        });

        holder.likesCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ConnectionsActivity.class);
                intent.putExtra("UserID", comments.getCommentID());
                intent.putExtra("Interaction", "Likes");
                context.startActivity(intent);
            }
        });

        if(comments.getUserID().equals(firebaseUser.getUid())){
            holder.deleteComBTN.setVisibility(View.VISIBLE);
            holder.deleteComBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //deleteComment(comments.getCommentID());
                    Toast.makeText(context, "You will be able to delete your comment", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void getCommentInfo(CommentModel comments, final MyHolder holder) {
        List<ContactsModel> phoneBook = new ArrayList<>();
        ContactsList contactsList = new ContactsList(phoneBook, context);
        contactsList.readContacts();
        final List<ContactsModel> phoneContacts = contactsList.getContactsList();

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);

                        assert user != null;
                        if (user.getUSER_ID().equals(comments.getUserID())) {

                            for (ContactsModel contactsModel : phoneContacts) {
                                if (user.getUSER_ID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    holder.commentOwner.setText("Me");
                                } else if (contactsModel.getNumber().equals(user.getNumber())) {
                                    holder.commentOwner.setText(contactsModel.getUsername());
                                }else{
                                    holder.commentOwner.setText(user.getUsername());
                                }
                            }

                            Picasso.get().load(user.getImageURL()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.cProPic, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {

                                    Picasso.get().load(user.getImageURL()).placeholder(R.drawable.default_profile_display_pic).into(holder.cProPic);
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        CircleImageView cProPic;
        TextView commentCaption, commentOwner, commentTime, likesCounter;
        ImageView deleteComBTN, likesBTN;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            cProPic = itemView.findViewById(R.id.comProPic);
            commentCaption = itemView.findViewById(R.id.commentCaption);
            commentOwner = itemView.findViewById(R.id.commentOwner);
            commentTime = itemView.findViewById(R.id.commentTime);
            deleteComBTN = itemView.findViewById(R.id.deleteComBTN);
            likesBTN = itemView.findViewById(R.id.likeComBTN);
            likesCounter = itemView.findViewById(R.id.comLikesCounter);

        }
    }
}
