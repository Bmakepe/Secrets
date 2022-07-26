package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makepe.blackout.GettingStarted.Models.NotiModel;
import com.makepe.blackout.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationBundleAdapter extends RecyclerView.Adapter<NotificationBundleAdapter.ViewHolder> {

    private Context context;
    private ArrayList<NotiModel> notifications;

    private DatabaseReference notificationsReference;
    private FirebaseUser firebaseUser;

    public static final int IS_POST_LIKE_NOTIFICATION = 100;
    public static final int IS_POST_COMMENT_NOTIFICATION = 200;
    public static final int IS_STORY_LIKE_NOTIFICATION = 300;
    public static final int IS_STORY_COMMENT_NOTIFICATION = 400;
    public static final int IS_COMMENT_LIKE_NOTIFICATION = 500;
    public static final int IS_FOLLOWING_NOTIFICATION = 600;
    public static final int IS_SHARED_NOTIFICATION = 700;

    public NotificationBundleAdapter(Context context, ArrayList<NotiModel> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType){

            case IS_POST_LIKE_NOTIFICATION:
            case IS_STORY_LIKE_NOTIFICATION:
            case IS_COMMENT_LIKE_NOTIFICATION:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.post_like_notification_item, parent, false));

            case IS_POST_COMMENT_NOTIFICATION:
            case IS_STORY_COMMENT_NOTIFICATION:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.post_comment_notification_item, parent, false));

            case IS_FOLLOWING_NOTIFICATION:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.user_follow_notification_item, parent, false));

            case IS_SHARED_NOTIFICATION:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.post_shared_notification_item, parent, false));

            default:
                throw new IllegalStateException("Unexpected value" + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotiModel notification = notifications.get(position);
        notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView picNo1, picNo2, picNo3, picNo4, picNo5;
        public TextView notificationMessage;
        public ImageView post_picture;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            picNo1 = itemView.findViewById(R.id.notiProIMG1);
            picNo2 = itemView.findViewById(R.id.notiProIMG2);
            picNo3 = itemView.findViewById(R.id.notiProIMG3);
            picNo4 = itemView.findViewById(R.id.notiProIMG4);
            picNo5 = itemView.findViewById(R.id.notiProIMG5);
            notificationMessage = itemView.findViewById(R.id.notiMessage);
            post_picture = itemView.findViewById(R.id.notification_post_image);

        }
    }
}
