package com.makepe.blackout.GettingStarted.Adapters;

import android.app.AlertDialog;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Models.Chat;
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

    private final Context context;
    private final List<Chat> mChats;
    private final String imageurl;

    private MediaPlayer myMediaPlayer;
    private UniversalFunctions universalFunctions;

    private FirebaseUser firebaseUser;
    private DatabaseReference chatReference;

    private GetTimeAgo getTimeAgo;
    private ArrayList<String> mediaList;

    public MessageAdapter(Context context, List<Chat> mChats, String imageurl) {
        this.context = context;
        this.mChats = mChats;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType){
            case MSG_TYPE_RIGHT:
                return new MessageAdapter.MyHolder(LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false));

            case MSG_TYPE_LEFT:
                return new MessageAdapter.MyHolder(LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false));

            case MSG_MEDIA_RIGHT:
                return new MessageAdapter.MyHolder(LayoutInflater.from(context).inflate(R.layout.chat_media_right_item, parent, false));

            case MSG_MEDIA_LEFT:
                return new MessageAdapter.MyHolder(LayoutInflater.from(context).inflate(R.layout.chat_media_left_item, parent, false));

            case MSG_TYPE_LOCATION_RIGHT:
                return new MessageAdapter.MyHolder(LayoutInflater.from(context).inflate(R.layout.chat_item_location_right, parent, false));

            case MSG_TYPE_LOCATION_LEFT:
                return new MessageAdapter.MyHolder(LayoutInflater.from(context).inflate(R.layout.chat_item_location_left, parent, false));

            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        chatReference = FirebaseDatabase.getInstance().getReference("Chats");
        Chat chat = mChats.get(position);
        getTimeAgo = new GetTimeAgo();
        universalFunctions = new UniversalFunctions(context);
        mediaList = new ArrayList<>();

        getChatDetails(chat, holder);

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


        holder.itemView.setOnClickListener(v -> showChatOptions(holder.itemView, chat, position));
    }

    private void getChatDetails(Chat chat, MyHolder holder) {

        String textMessage = chat.getMessage();
        String voiceNote = chat.getAudio();

        try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
            holder.timeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(chat.getTimeStamp()), context));
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }

        try{
            Picasso.get().load(imageurl).into(holder.chatPic);
        }catch (NullPointerException e){
            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.chatPic);
        }

        switch (chat.getMessage_type()){
            case "text":
                holder.showMessages.setVisibility(View.VISIBLE);
                holder.showMessages.setText(textMessage);
                break;

            case "mediaTextMessage":

                holder.mediaRecycler.hasFixedSize();
                holder.mediaRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

                getMediaList(chat, holder);
                break;

            case "location":
                universalFunctions.findAddress(chat.getLatitude(), chat.getLongitude(), holder.messageLocationTV, holder.locationTextArea);


            default:
                Toast.makeText(context, "unknown message type detected", Toast.LENGTH_SHORT).show();
        }

    }

    private void getMediaList(Chat chat, MyHolder holder) {
        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    mediaList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Chat myChat = ds.getValue(Chat.class);

                        assert myChat != null;
                        if (myChat.getChatID().equals(chat.getChatID())){
                            holder.showMessages.setText(myChat.getMessage());

                            chatReference.child(myChat.getChatID()).child("images")
                                    .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
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

    private void pause() {
        if(myMediaPlayer != null){
            if(myMediaPlayer.isPlaying()){
                myMediaPlayer.pause();
            }
        }
    }

    private void playVoiceNote(String voiceStatus, MyHolder holder) {
        if(myMediaPlayer == null){
            myMediaPlayer = new MediaPlayer();
            setAudioUrlToMediaPlayer(voiceStatus);
        }else{
            if(myMediaPlayer.isPlaying()){
                myMediaPlayer.pause();
            }else{
                setAudioUrlToMediaPlayer(voiceStatus);
            }
        }

        myMediaPlayer.setOnPreparedListener(mp -> {
            if(myMediaPlayer != null){
                myMediaPlayer.start();
                String totalTime = String.valueOf(myMediaPlayer.getDuration());
                holder.voiceTimeText.setText(totalTime);
                holder.voiceNoteSeekbar.setProgress(0);
                holder.voiceNoteSeekbar.setMax(Integer.parseInt(totalTime));
                holder.voiceNoteSeekbar.setClickable(true);

                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        holder.voiceNoteSeekbar.setProgress(myMediaPlayer.getCurrentPosition());
                    }
                }, 0 , 200);

                holder.voiceNoteSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser){
                            myMediaPlayer.seekTo(progress);
                        }
                        final long mMinutes = (progress/1000) / 60;//converting into minutes
                        final int mSeconds = ((progress/1000) % 60);//converting to seconds
                        holder.voiceTimeText.setText(mMinutes + ":" + mSeconds);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int CurrentLevel = holder.voiceNoteSeekbar.getProgress();
                        if(CurrentLevel < 30)
                            CurrentLevel = 30;
                        holder.voiceNoteSeekbar.setProgress(CurrentLevel);
                    }
                });

                myMediaPlayer.setOnCompletionListener(mp1 -> {
                    final Handler handler;
                    handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            holder.playBTN.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                            myMediaPlayer.stop();
                        }
                    }, 200);
                });
            }
        });
    }

    private void setAudioUrlToMediaPlayer(String voiceStatus) {
        if(voiceStatus != null){
            Log.d("audio file", "available" + voiceStatus);
            try{
                Log.d("setAudioUrl", ""+voiceStatus);
                myMediaPlayer.reset();
                myMediaPlayer.setDataSource(voiceStatus);
                myMediaPlayer.prepare();
                myMediaPlayer.setVolume(1.0f, 1.0f);
                myMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Log.d("Audio", "available" + voiceStatus);
        }
    }

    private void showChatOptions(final View itemView, Chat chat, final int position) {
        final PopupMenu popupMenu = new PopupMenu(context, itemView, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0,0,"Reply");
        popupMenu.getMenu().add(Menu.NONE, 1,0,"Copy");
        popupMenu.getMenu().add(Menu.NONE, 2,0,"Forward");
        if(chat.getSender().equals(firebaseUser.getUid())){
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
                    if(chat.getSender().equals(firebaseUser.getUid())) {
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
        private CircleImageView chatPic;
        private TextView showMessages, timeStamp, viewLocationBTN, messageLocationTV;
        private ImageView ticks;

        private RecyclerView mediaRecycler;
        private LinearLayout locationTextArea;

        //for voice chat views
        private ImageButton playBTN;
        private TextView voiceTimeText, voiceTimeDuration;
        private SeekBar voiceNoteSeekbar;

        MyHolder(@NonNull View itemView) {
            super(itemView);

            chatPic = itemView.findViewById(R.id.chatProPic);
            showMessages = itemView.findViewById(R.id.showMessages);
            timeStamp = itemView.findViewById(R.id.chatTimeStamp);
            ticks = itemView.findViewById(R.id.greyTick);
            mediaRecycler = itemView.findViewById(R.id.chat_media_recycler);
            viewLocationBTN = itemView.findViewById(R.id.viewLocationBTN);
            messageLocationTV = itemView.findViewById(R.id.messageLocationText);
            locationTextArea = itemView.findViewById(R.id.locationTextArea);

            playBTN = itemView.findViewById(R.id.playVoiceBTN);
            voiceTimeText = itemView.findViewById(R.id.postTimeText);
            voiceTimeDuration = itemView.findViewById(R.id.postTimeDuration);
            voiceNoteSeekbar = itemView.findViewById(R.id.voiceNoteSeekbar);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChats.get(position).getSender().equals(firebaseUser.getUid())){

            if (mChats.get(position).getMessage_type().equals("text"))
                return MSG_TYPE_RIGHT;
            else if (mChats.get(position).getMessage_type().equals("mediaTextMessage"))
                return MSG_MEDIA_RIGHT;
            else if (mChats.get(position).getMessage_type().equals("location"))
                return MSG_TYPE_LOCATION_RIGHT;

        }else{

            if (mChats.get(position).getMessage_type().equals("text"))
                return MSG_TYPE_LEFT;
            else if (mChats.get(position).getMessage_type().equals("mediaTextMessage"))
                return MSG_MEDIA_LEFT;
            else if (mChats.get(position).getMessage_type().equals("location"))
                return MSG_TYPE_LOCATION_LEFT;

        }
        return position;
    }
}
