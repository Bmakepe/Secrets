package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
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
import com.makepe.blackout.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class BundledNotificationAdapter extends RecyclerView.Adapter<BundledNotificationAdapter.ViewHolder> {
    private Context context;
    private ArrayList<String> notificationBundles;

    public static final int LIKE_NOTIFICATION_ITEM = 100;
    public static final int FOLLOW_NOTIFICATION_ITEM = 200;
    public static final int COMMENT_NOTIFICATION_ITEM = 300;

    boolean isLikeNotification = false, isFollowNotification = false, isCommentNotification = false, isTagNotification = false;

    private DatabaseReference notificationReference = FirebaseDatabase.getInstance().getReference("SecretNotifications");
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    public BundledNotificationAdapter(Context context, ArrayList<String> notificationBundles) {
        this.context = context;
        this.notificationBundles = notificationBundles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (isLikeNotification)
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.post_like_notification_item, parent, false));
        else if (isCommentNotification)
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.user_follow_notification_item, parent, false));
        else if (isFollowNotification)
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.post_comment_notification_item, parent, false));
        else if (isTagNotification)
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.post_tag_notification_item, parent, false));
        else
            //throw new IllegalStateException("Error loading notification types: " + viewType);
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.native_ad_row_item, parent, false));

        /*switch (viewType) {
            case LIKE_NOTIFICATION_ITEM:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.post_like_notification_item, parent, false));
            case FOLLOW_NOTIFICATION_ITEM:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.user_follow_notification_item, parent, false));
            case COMMENT_NOTIFICATION_ITEM:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.post_comment_notification_item, parent, false));
            default:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.native_ad_row_item, parent, false));
                //throw new IllegalStateException("Error loading notification types: " + viewType);
        }*/
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return notificationBundles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView itemProfilePic, firstLiker, secondLiker, thirdLiker, forthLiker, fifthLiker;
        public TextView username, timestamp, message;
        public ImageView postImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemProfilePic = itemView.findViewById(R.id.notiProIMG);
            firstLiker = itemView.findViewById(R.id.notiProIMG1);
            secondLiker = itemView.findViewById(R.id.notiProIMG2);
            thirdLiker = itemView.findViewById(R.id.notiProIMG3);
            forthLiker = itemView.findViewById(R.id.notiProIMG4);
            fifthLiker = itemView.findViewById(R.id.notiProIMG5);

            username = itemView.findViewById(R.id.notiUsername);
            timestamp = itemView.findViewById(R.id.notiTimeStamp);
            message = itemView.findViewById(R.id.notiMessage);
            postImage = itemView.findViewById(R.id.notification_post_image);

        }
    }

    @Override
    public int getItemViewType(int position) {

        notificationReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){

                        notificationReference.child(firebaseUser.getUid()).child(ds.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    for (DataSnapshot data : snapshot.getChildren()){
                                        if (notificationBundles.get(position).equals(data.getKey())){
                                            //Toast.makeText(context, "You will get your view types " + ds.getKey(), Toast.LENGTH_SHORT).show();

                                            if (ds.getKey().equals("CommentNotifications")){
                                                isCommentNotification = true;
                                                Toast.makeText(context, "comment notification exists", Toast.LENGTH_SHORT).show();
                                            }
                                            else if (ds.getKey().equals("FollowNotifications")){
                                                isFollowNotification = true;
                                                Toast.makeText(context, "follow notification exists", Toast.LENGTH_SHORT).show();
                                            }
                                            else if (ds.getKey().equals("LikesNotifications")){
                                                isLikeNotification = true;
                                                Toast.makeText(context, "likes notification exists", Toast.LENGTH_SHORT).show();
                                            }else if (ds.getKey().equals("TagNotifications")){
                                                isTagNotification = true;
                                                Toast.makeText(context, "Tag Notifications exist", Toast.LENGTH_SHORT).show();
                                            }

                                            /*if (ds.getKey().equals("CommentNotifications"))
                                                return COMMENT_NOTIFICATION_ITEM;
                                            else if (ds.getKey().equals("FollowNotifications"))
                                                return FOLLOW_NOTIFICATION_ITEM;
                                            else if (ds.getKey().equals("LikesNotifications"))
                                                return LIKE_NOTIFICATION_ITEM;*/

                                        }
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

        //return itemViewSelected;
        return super.getItemViewType(position);
    }
}
