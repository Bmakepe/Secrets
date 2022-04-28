package com.makepe.blackout.GettingStarted.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
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
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.GroupChat;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.ViewHolder> {

    private Context context;
    private List<GroupChat> groupChats;

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_AUDIO_LEFT = 2;
    public static final int MSG_TYPE_AUDIO_RIGHT = 3;
    public static final int MSG_TYPE_IMAGE_RIGHT = 4;
    public static final int MSG_TYPE_IMAGE_LEFT = 5;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, groupChatsReference;

    private GetTimeAgo getTimeAgo;

    public GroupChatAdapter(Context context, List<GroupChat> groupChats) {
        this.context = context;
        this.groupChats = groupChats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType) {
            case MSG_TYPE_RIGHT:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false));

            case MSG_TYPE_LEFT:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false));

            case MSG_TYPE_AUDIO_LEFT:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.msg_item_audio_left, parent, false));

            case MSG_TYPE_AUDIO_RIGHT:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.msg_item_audio_right, parent, false));

            case MSG_TYPE_IMAGE_RIGHT:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.msg_item_media_right, parent, false));

            case MSG_TYPE_IMAGE_LEFT:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.msg_item_media_left, parent, false));

            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        groupChatsReference = FirebaseDatabase.getInstance().getReference("GroupChats");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        getTimeAgo = new GetTimeAgo();
        GroupChat chat = groupChats.get(position);

        getGroupChats(chat, holder);
        getUserInfo(chat, holder);

        holder.chatUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chat.getSenderID().equals(firebaseUser.getUid())){
                    Toast.makeText(context, "You cannot view this profile", Toast.LENGTH_SHORT).show();
                }else{
                    Intent profileIntent = new Intent(context, ViewProfileActivity.class);
                    profileIntent.putExtra("uid", chat.getSenderID());
                    context.startActivity(profileIntent);
                }
            }
        });

        holder.showMessages.setOnClickListener(view -> {
            showChatOptions(holder.showMessages, chat, position);
        });
    }

    private void showChatOptions(TextView showMessages, GroupChat chat, int position) {
        final PopupMenu popupMenu = new PopupMenu(context, showMessages, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Reply");
        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Copy");
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "Forward");

        if (chat.getSenderID().equals(firebaseUser.getUid()))
            popupMenu.getMenu().add(Menu.NONE, 3, 0, "Delete");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                switch (menuItem.getItemId()){
                    case 0:
                        Toast.makeText(context, "You will be able to quote " + chat.getMessage(), Toast.LENGTH_SHORT).show();
                        break;

                    case 1:
                        Toast.makeText(context, "You will be able to copy " + chat.getMessage() + " to the clipboard", Toast.LENGTH_SHORT).show();
                        break;

                    case 2:
                        Toast.makeText(context, "You will be able to forward " + chat.getMessage(), Toast.LENGTH_SHORT).show();
                        break;

                    case 3:
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Delete");
                        builder.setMessage("Are you sure to delete this message?");

                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteMessage(position);
                                Toast.makeText(context, chat.getMessage() + " will be deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.create().show();
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + menuItem.getItemId());
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void deleteMessage(int position) {
        String msgTimeStamp = groupChats.get(position).getTimeStamp();
        Query query = groupChatsReference.orderByChild("timeStamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    if (ds.child("senderID").getValue().equals(firebaseUser.getUid()))
                        ds.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getGroupChats(GroupChat chat, ViewHolder holder) {
        String audioType = "audio", imgType = "image";

        if (chat.getMsg_type().equals(imgType)){

            try{
                Picasso.get().load(chat.getMedia()).into(holder.msgImage);
            }catch (NullPointerException ignored){}

        }else{
            try{
                if (!chat.getMessage().isEmpty()){
                    holder.showMessages.setVisibility(View.VISIBLE);
                    holder.showMessages.setText(chat.getMessage());
                }
            }catch (NullPointerException ignored){}
        }

        holder.timeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(chat.getTimeStamp()), context));
    }

    private void getUserInfo(GroupChat chat, ViewHolder holder) {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUSER_ID().equals(chat.getSenderID())){
                        holder.chatUsername.setVisibility(View.VISIBLE);
                        holder.chatUsername.setText(user.getUsername());

                        try {
                            Picasso.get().load(user.getImageURL()).into(holder.chatPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.chatPic);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return groupChats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView chatPic;
        TextView showMessages, timeStamp, chatUsername;
        ImageView ticks, msgImage;

        ImageButton playBTN;
        TextView voiceTimeText, voiceTimeDuration;
        SeekBar voiceNoteSeekbar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            chatPic = itemView.findViewById(R.id.chatProPic);
            showMessages = itemView.findViewById(R.id.showMessages);
            timeStamp = itemView.findViewById(R.id.chatTimeStamp);
            ticks = itemView.findViewById(R.id.greyTick);
            chatUsername = itemView.findViewById(R.id.chatUsername);


            playBTN = itemView.findViewById(R.id.playVoiceBTN);
            voiceTimeText = itemView.findViewById(R.id.postTimeText);
            voiceTimeDuration = itemView.findViewById(R.id.postTimeDuration);
            voiceNoteSeekbar = itemView.findViewById(R.id.voiceNoteSeekbar);
            msgImage = itemView.findViewById(R.id.chatImage);
        }
    }

    @Override
    public int getItemViewType(int position) {

        try{
            if(groupChats.get(position).getSenderID().equals(firebaseUser.getUid())){
                if(groupChats.get(position).getMsg_type().equals("audio")){
                    return MSG_TYPE_AUDIO_RIGHT;
                }else if(groupChats.get(position).getMsg_type().equals("image")){
                    return MSG_TYPE_IMAGE_RIGHT;
                }else {
                    return MSG_TYPE_RIGHT;
                }
            }else {
                if(groupChats.get(position).getMsg_type().equals("audio")){
                    return MSG_TYPE_AUDIO_LEFT;
                }else if(groupChats.get(position).getMsg_type().equals("image")){
                    return MSG_TYPE_IMAGE_LEFT;
                }else {
                    return MSG_TYPE_LEFT;
                }
            }
        }catch (NullPointerException ignored){}

        return 0;
    }
}
