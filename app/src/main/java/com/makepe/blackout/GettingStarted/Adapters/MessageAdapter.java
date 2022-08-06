package com.makepe.blackout.GettingStarted.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.StoryActivity;
import com.makepe.blackout.GettingStarted.Models.Chat;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioPlayer;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.LocationServices;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private static final int MSG_MEDIA_RIGHT = 2;
    private static final int MSG_MEDIA_LEFT = 3;
    private static final int MSG_TYPE_LOCATION_LEFT = 4;
    private static final int MSG_TYPE_LOCATION_RIGHT = 5;
    private static final int MSG_TYPE_AUDIO_LEFT = 6;
    private static final int MSG_TYPE_AUDIO_RIGHT = 7;
    private static final int MSG_TYPE_VIDEO_LEFT = 8;
    private static final int MSG_TYPE_VIDEO_RIGHT = 9;
    public static final int MSG_TYPE_STORY_LEFT = 10;
    public static final int MSG_TYPE_STORY_RIGHT = 11;
    public static final int MSG_TYPE_AUDIO_STORY_RIGHT = 12;
    public static final int MSG_TYPE_AUDIO_STORY_LEFT = 13;

    private final Context context;
    private final List<Chat> mChats;

    private LinearSnapHelper snapHelper;
    private UniversalFunctions universalFunctions;

    private FirebaseUser firebaseUser;
    private DatabaseReference chatReference, userReference, storyReference;

    private GetTimeAgo getTimeAgo;

    private boolean videoIsPlaying = false;

    public MessageAdapter(Context context, List<Chat> mChats) {
        this.context = context;
        this.mChats = mChats;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType){
            case MSG_TYPE_RIGHT:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false));

            case MSG_TYPE_LEFT:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false));

            case MSG_MEDIA_RIGHT:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.chat_media_right_item, parent, false));

            case MSG_MEDIA_LEFT:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.chat_media_left_item, parent, false));

            case MSG_TYPE_LOCATION_RIGHT:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.chat_item_location_right, parent, false));

            case MSG_TYPE_LOCATION_LEFT:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.chat_item_location_left, parent, false));

            case MSG_TYPE_AUDIO_RIGHT:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.msg_item_audio_right, parent, false));

            case MSG_TYPE_AUDIO_LEFT:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.msg_item_audio_left, parent, false));

            case MSG_TYPE_VIDEO_RIGHT:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.msg_item_video_right, parent, false));

            case MSG_TYPE_VIDEO_LEFT:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.msg_item_video_left, parent, false));

            case MSG_TYPE_STORY_LEFT:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.msg_item_story_text_left, parent, false));

            case MSG_TYPE_STORY_RIGHT:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.msg_item_story_text_right, parent, false));

            case MSG_TYPE_AUDIO_STORY_RIGHT:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.msg_item_story_audio_right, parent, false));

            case MSG_TYPE_AUDIO_STORY_LEFT:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.msg_item_story_audio_left, parent, false));

            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        chatReference = FirebaseDatabase.getInstance().getReference("Chats");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        storyReference = FirebaseDatabase.getInstance().getReference("Story");
        Chat chat = mChats.get(position);
        getTimeAgo = new GetTimeAgo();
        universalFunctions = new UniversalFunctions(context);
        snapHelper = new LinearSnapHelper();

        getChatDetails(chat, holder);

        if (!chat.getSenderID().equals(firebaseUser.getUid()))
            getHisDetails(chat, holder);

        try{
            if(position == mChats.size()-1){
                if(mChats.get(position).isSeen()){
                    holder.ticks.setImageResource(R.drawable.ic_done_all_black_24dp);
                }else{
                    holder.ticks.setImageResource(R.drawable.ic_done_black_24dp);
                }
            }
        }catch (NullPointerException ignored){}

        if (chat.getMessage_type().equals("location")){
            holder.viewLocationBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String address = "https://maps.google.com/maps?saddr=" + chat.getLatitude() + "," + chat.getLongitude();
                    Intent locationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                    context.startActivity(locationIntent);
                }
            });
        }

        //holder.itemView.setOnClickListener(v -> showChatOptions(holder.itemView, chat, position));
    }

    private void getHisDetails(Chat chat, MyHolder holder) {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUserID().equals(chat.getSenderID()))
                        try{
                            Picasso.get().load(user.getImageURL()).into(holder.chatPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.chatPic);
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getChatDetails(Chat chat, MyHolder holder) {

        try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
            holder.timeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(chat.getTimeStamp()), context));
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }

        switch (chat.getMessage_type()){
            case "text":
                holder.showMessages.setText(chat.getMessage());
                break;

            case "imageMessage":

                holder.mediaRecycler.hasFixedSize();
                holder.mediaRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                snapHelper.attachToRecyclerView(holder.mediaRecycler);

                getMediaList(chat, holder);

                holder.messageMediaSeeMoreBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "You will be able to view all the media in this message", Toast.LENGTH_SHORT).show();
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
                getMessageVideoDetails(chat, holder);
                break;

            case "storyTextMessage":
                holder.showMessages.setText(chat.getMessage());
                getStoryMessageDetails(chat, holder);
                break;

            case "storyAudioMessage":
                getAudioMessageDetails(chat, holder);
                getStoryMessageDetails(chat, holder);
                break;

            default:
                Toast.makeText(context, "unknown message type detected: " + chat.getMessage_type(), Toast.LENGTH_SHORT).show();
        }

    }

    private void getStoryMessageDetails(Chat chat, MyHolder holder) {
        storyReference.child(chat.getReceiverID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Story story = ds.getValue(Story.class);

                        assert story != null;
                        if (story.getStoryID().equals(chat.getStoryID()))
                            try{
                                Picasso.get().load(story.getImageURL()).into(holder.storyImage);
                                holder.messageStoryImageLoader.setVisibility(View.GONE);
                            }catch (NullPointerException ignored){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMessageVideoDetails(Chat chat, MyHolder holder) {

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

    private void getAudioMessageDetails(Chat chat, MyHolder holder) {

        AudioPlayer audioPlayer = new AudioPlayer(context, holder.playBTN,
                holder.seekTimer, holder.postTotalTime, holder.audioAnimation);

        audioPlayer.init();

        try{//convert timestamp
            holder.timeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(chat.getTimeStamp()), context));
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }

        holder.playBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!audioPlayer.mediaPlayer.isPlaying())
                    audioPlayer.startPlayingAudio(chat.getAudioURL());
                else if(audioPlayer.mediaPlayer.isPlaying())
                    audioPlayer.stopPlayingAudio();
            }
        });

    }

    private void getMediaList(Chat chat, MyHolder holder) {

        ArrayList<String> mediaList = new ArrayList<>();

        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Chat myChat = ds.getValue(Chat.class);

                        assert myChat != null;
                        if (myChat.getChatID().equals(chat.getChatID())){

                            chatReference.child(myChat.getChatID()).child("images")
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

    private void showChatOptions(final View itemView, Chat chat, final int position) {
        final PopupMenu popupMenu = new PopupMenu(context, itemView, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0,0,"Reply");
        popupMenu.getMenu().add(Menu.NONE, 1,0,"Copy");
        popupMenu.getMenu().add(Menu.NONE, 2,0,"Forward");
        if(chat.getSenderID().equals(firebaseUser.getUid())){
            popupMenu.getMenu().add(Menu.NONE, 3,0,"Delete");
        }

        if(chat.getMessage_type().equals("location"))
            popupMenu.getMenu().add(Menu.NONE, 4, 0, "View Location");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                switch (id){
                    case 0:
                        Toast.makeText(context, "Reply Chat", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(context, "Copy Chat", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(context, "Forward Chat", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Delete");
                        builder.setMessage("Are you sure to delete this message");

                        //delete btn
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch(chat.getMessage_type()){
                                    case "text":
                                    case "location":
                                        deleteMessage(position);
                                        break;
                                    case"mediaTextMessage":
                                        deleteMediaMessage(position);
                                        break;
                                }
                            }
                        });
                        //cancel delete btn
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss dialog
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                        break;

                    case 4:
                        String address = "https://maps.google.com/maps?saddr=" + chat.getLatitude() + "," + chat.getLongitude();
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                        context.startActivity(intent);
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + id);
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void deleteMediaMessage(int position) {
        Toast.makeText(context, "You will be able to delete the media message", Toast.LENGTH_SHORT).show();
    }

    private void deleteMessage(int position) {
        String msgTimeStamp = mChats.get(position).getTimeStamp();
        Query query = chatReference.orderByChild("timeStamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    assert chat != null;
                    if(chat.getSenderID().equals(firebaseUser.getUid())) {
                       ds.getRef().removeValue();
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
        return mChats.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        //for text chat views
        public CircleImageView chatPic;
        public TextView showMessages, timeStamp, messageMediaSeeMoreBTN, viewLocationBTN;
        public ImageView ticks, storyImage;

        public RecyclerView mediaRecycler;
        public LinearLayout locationTextArea;

        public ProgressBar messageStoryImageLoader;

        //--------------for audio post buttons
        public CircleImageView playBTN;
        public SeekBar audioAnimation;
        public TextView seekTimer, postTotalTime;

        //------------for videos
        public VideoView messagesVideoView;
        public ImageView messageVideoPlayBTN;
        public ProgressBar messageVideoLoader;

        MyHolder(@NonNull View itemView) {
            super(itemView);

            chatPic = itemView.findViewById(R.id.chatProPic);
            showMessages = itemView.findViewById(R.id.showMessages);
            timeStamp = itemView.findViewById(R.id.chatTimeStamp);
            ticks = itemView.findViewById(R.id.greyTick);
            mediaRecycler = itemView.findViewById(R.id.chat_media_recycler);
            viewLocationBTN = itemView.findViewById(R.id.viewLocationBTN);
            locationTextArea = itemView.findViewById(R.id.locationTextArea);
            messageMediaSeeMoreBTN = itemView.findViewById(R.id.messageMediaSeeMoreBTN);
            storyImage = itemView.findViewById(R.id.dmStoryImage);
            messageStoryImageLoader = itemView.findViewById(R.id.messageStoryImageLoader);

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
        if(mChats.get(position).getSenderID().equals(firebaseUser.getUid())){

            switch (mChats.get(position).getMessage_type()) {
                case "text":
                    return MSG_TYPE_RIGHT;
                case "imageMessage":
                    return MSG_MEDIA_RIGHT;
                case "location":
                    return MSG_TYPE_LOCATION_RIGHT;
                case "audioMessage":
                    return MSG_TYPE_AUDIO_RIGHT;
                case "videoMessage":
                    return MSG_TYPE_VIDEO_RIGHT;
                case "storyAudioMessage":
                    return MSG_TYPE_AUDIO_STORY_RIGHT;
                case "storyTextMessage":
                    return MSG_TYPE_STORY_RIGHT;
            }

        }else{

            switch (mChats.get(position).getMessage_type()) {
                case "text":
                    return MSG_TYPE_LEFT;
                case "imageMessage":
                    return MSG_MEDIA_LEFT;
                case "location":
                    return MSG_TYPE_LOCATION_LEFT;
                case "audioMessage":
                    return MSG_TYPE_AUDIO_LEFT;
                case "videoMessage":
                    return MSG_TYPE_VIDEO_LEFT;
                case "storyAudioMessage":
                    return MSG_TYPE_AUDIO_STORY_LEFT;
                case "storyTextMessage":
                    return MSG_TYPE_STORY_LEFT;
            }

        }
        return position;
    }
}
