package com.makepe.blackout.GettingStarted.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.Chat;
import com.makepe.blackout.GettingStarted.Models.GroupChat;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioPlayer;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
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
    public static final int MSG_TYPE_VIDEO_RIGHT = 6;
    public static final int MSG_TYPE_VIDEO_LEFT = 7;
    private static final int MSG_TYPE_LOCATION_LEFT = 8;
    private static final int MSG_TYPE_LOCATION_RIGHT = 9;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, groupChatsReference;

    private GetTimeAgo getTimeAgo;

    private AudioPlayer audioPlayer;
    private UniversalFunctions universalFunctions;

    private boolean videoIsPlaying = false;

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
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_media_right_item, parent, false));

            case MSG_TYPE_IMAGE_LEFT:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_media_left_item, parent, false));

            case MSG_TYPE_VIDEO_RIGHT:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.msg_item_video_right, parent, false));

            case MSG_TYPE_VIDEO_LEFT:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.msg_item_video_left, parent, false));

            case MSG_TYPE_LOCATION_RIGHT:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_item_location_right, parent, false));

            case MSG_TYPE_LOCATION_LEFT:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_item_location_left, parent, false));

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
        universalFunctions = new UniversalFunctions(context);

        getGroupChats(chat, holder);
        getUserInfo(chat, holder);

        holder.chatUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!chat.getSenderID().equals(firebaseUser.getUid())){
                    Intent profileIntent = new Intent(context, ViewProfileActivity.class);
                    profileIntent.putExtra("uid", chat.getSenderID());
                    context.startActivity(profileIntent);
                }
            }
        });

        /*
        holder.itemView.setOnClickListener(view -> {

            showChatOptions(holder.showMessages, chat, position);
        });*/
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

        try{
            holder.timeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(chat.getTimeStamp()), context));
        }catch (NumberFormatException e){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }

        switch(chat.getMsg_type()){
            case "text":
                holder.showMessages.setText(chat.getMessage());
                break;

            case "imageMessage":

                holder.mediaRecycler.hasFixedSize();
                holder.mediaRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

                getMediaList(chat, holder);

                holder.messageMediaSeeMoreBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "You will be able to view all the media here", Toast.LENGTH_SHORT).show();
                    }
                });
                break;

            case "location":
                universalFunctions.findAddress(chat.getLatitude(), chat.getLongitude(), holder.showMessages, holder.locationTextArea);
                break;

            case "audioMessage":
                getAudioMessageDetails(chat, holder);
                break;

            case "videoMessage":
                getGroupVideoMessage(chat, holder);
                break;

            default:
                Toast.makeText(context, "Unknown message type identified " + chat.getMsg_type(), Toast.LENGTH_SHORT).show();
        }

    }

    private void getGroupVideoMessage(GroupChat chat, ViewHolder holder) {

        holder.messagesVideoView.setVideoURI(Uri.parse(chat.getVideoURL()));
        holder.messagesVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                holder.messageVideoLoader.setVisibility(View.GONE);

                mediaPlayer.setVolume(0f, 0f);
                mediaPlayer.setLooping(false);

            }
        });

        holder.messageVideoPlayBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoIsPlaying){
                    holder.messagesVideoView.pause();
                    videoIsPlaying = false;
                    holder.messageVideoPlayBTN.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                }else{
                    holder.messagesVideoView.start();
                    videoIsPlaying = true;
                    holder.messageVideoPlayBTN.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                }
            }
        });
    }

    private void getAudioMessageDetails(GroupChat chat, ViewHolder holder) {

        audioPlayer = new AudioPlayer(context, holder.playBTN,
                holder.seekTimer, holder.postTotalTime, holder.audioAnimation);

        try{//convert timestamp
            holder.timeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(chat.getTimeStamp()), context));
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }

        holder.playBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!audioPlayer.isPlaying()){
                    audioPlayer.startPlayingAudio(chat.getAudio());
                }else{
                    audioPlayer.stopPlayingAudio();
                }
            }
        });

    }

    private void getMediaList(GroupChat chat, ViewHolder holder) {

        ArrayList<String> mediaList = new ArrayList<>();
        groupChatsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        GroupChat myChat = ds.getValue(GroupChat.class);

                        assert myChat != null;
                        if (myChat.getChatID().equals(chat.getChatID())){

                            groupChatsReference.child(myChat.getChatID()).child("images")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()){
                                                mediaList.clear();
                                                for (DataSnapshot ds : snapshot.getChildren()){
                                                    mediaList.add(ds.getValue().toString());
                                                }
                                                holder.mediaRecycler.setAdapter(new ChatMediaAdapter(context, mediaList));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserInfo(GroupChat chat, ViewHolder holder) {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
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

        private CircleImageView chatPic;
        private TextView showMessages, timeStamp, chatUsername, messageMediaSeeMoreBTN;
        private ImageView ticks;

        private RecyclerView mediaRecycler;
        private LinearLayout locationTextArea;

        //--------------for audio post buttons
        public CircleImageView playBTN;
        public LottieAnimationView audioAnimation;
        public TextView seekTimer, postTotalTime;

        //------------for videos
        public VideoView messagesVideoView;
        public ImageView messageVideoPlayBTN;
        public ProgressBar messageVideoLoader;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            chatPic = itemView.findViewById(R.id.chatProPic);
            showMessages = itemView.findViewById(R.id.showMessages);
            timeStamp = itemView.findViewById(R.id.chatTimeStamp);
            ticks = itemView.findViewById(R.id.greyTick);
            chatUsername = itemView.findViewById(R.id.chatUsername);
            locationTextArea = itemView.findViewById(R.id.locationTextArea);
            messageMediaSeeMoreBTN = itemView.findViewById(R.id.messageMediaSeeMoreBTN);
            mediaRecycler = itemView.findViewById(R.id.chat_media_recycler);

            //for audio buttons
            playBTN = itemView.findViewById(R.id.postItem_playVoiceIcon);
            audioAnimation = itemView.findViewById(R.id.postItem_lav_playing);
            seekTimer = itemView.findViewById(R.id.postItemSeekTimer);
            postTotalTime = itemView.findViewById(R.id.postTotalTime);

            messagesVideoView = itemView.findViewById(R.id.messagesVideoView);
            messageVideoPlayBTN = itemView.findViewById(R.id.messageVideoPlayBTN);
            messageVideoLoader = itemView.findViewById(R.id.messageVideoLoader);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(groupChats.get(position).getSenderID().equals(firebaseUser.getUid())){
            switch (groupChats.get(position).getMsg_type()) {
                case "audioMessage":
                    return MSG_TYPE_AUDIO_RIGHT;
                case "imageMessage":
                    return MSG_TYPE_IMAGE_RIGHT;
                case "text":
                    return MSG_TYPE_RIGHT;
                case "videoMessage":
                    return MSG_TYPE_VIDEO_RIGHT;
                case "location":
                    return MSG_TYPE_LOCATION_RIGHT;
            }
        }else {
            switch (groupChats.get(position).getMsg_type()) {
                case "audioMessage":
                    return MSG_TYPE_AUDIO_LEFT;
                case "imageMessage":
                    return MSG_TYPE_IMAGE_LEFT;
                case "text":
                    return MSG_TYPE_LEFT;
                case "videoMessage":
                    return MSG_TYPE_VIDEO_LEFT;
                case "location":
                    return MSG_TYPE_LOCATION_LEFT;
            }
        }

        return position;
    }
}
