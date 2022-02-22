package com.makepe.blackout.GettingStarted.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
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
import androidx.cardview.widget.CardView;
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
import com.makepe.blackout.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private final Context context;
    private final List<Chat> mChats;
    private final String imageurl;

    private MediaPlayer myMediaPlayer;

    private FirebaseUser firebaseUser;

    public MessageAdapter(Context context, List<Chat> mChats, String imageurl) {
        this.context = context;
        this.mChats = mChats;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        switch (viewType){
            case MSG_TYPE_RIGHT:
                view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
                return new MessageAdapter.MyHolder(view);

            case MSG_TYPE_LEFT:
                view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
                return new MessageAdapter.MyHolder(view);

            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        Chat chat = mChats.get(position);
        final String uid = chat.getSender();

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

        holder.itemView.setOnClickListener(v -> showChatOptions(holder.itemView, uid, position));
    }

    private void getChatDetails(Chat chat, MyHolder holder) {

        String textMessage = chat.getMessage();
        String timeStamp = chat.getTimeStamp();
        String voiceNote = chat.getAudio();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
            calendar.setTimeInMillis(Long.parseLong(timeStamp));
            String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
            holder.timeStamp.setText(pTime);
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }

        Picasso.get().load(imageurl).networkPolicy(NetworkPolicy.OFFLINE).into(holder.chatPic, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Exception e) {

                Picasso.get().load(imageurl).into(holder.chatPic);
            }
        });

        try{
            if(textMessage.equals("noMessage")){
                //used for audio messages

                holder.mediaPlayer.setVisibility(View.VISIBLE);
                holder.showMessages.setVisibility(View.GONE);
                holder.playBTN.setTag("Play");

                holder.playBTN.setOnClickListener(view -> {
                    if(holder.playBTN.getTag().equals("Play")){
                        holder.playBTN.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                        holder.playBTN.setTag("Pause");
                        playVoiceNote(voiceNote, holder);
                    }else{
                        holder.playBTN.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                        holder.playBTN.setTag("Play");
                        pause();
                    }
                });

            }else{
                //used for text messages

                holder.mediaPlayer.setVisibility(View.GONE);
                holder.showMessages.setVisibility(View.VISIBLE);

                holder.showMessages.setText(textMessage);

            }
        }catch (NullPointerException ignored){}
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

    private void showChatOptions(final View itemView, String uid, final int position) {
        final PopupMenu popupMenu = new PopupMenu(context, itemView, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0,0,"Reply");
        popupMenu.getMenu().add(Menu.NONE, 1,0,"Copy");
        popupMenu.getMenu().add(Menu.NONE, 2,0,"Forward");
        if(uid.equals(firebaseUser.getUid())){
            popupMenu.getMenu().add(Menu.NONE, 3,0,"Delete");
        }


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
                        Toast.makeText(context, "Delete Chat", Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Delete");
                        builder.setMessage("Are you sure to delete this message");

                        //delete btn
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteMessage(position);
                            }
                        });
                        //cancel delete btn
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss dialog
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + id);
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void deleteMessage(int position) {
        String msgTimeStamp = mChats.get(position).getTimeStamp();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = reference.orderByChild("timeStamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                   if(ds.child("sender").getValue().equals(firebaseUser.getUid())) {
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

        CircleImageView chatPic;
        TextView showMessages, timeStamp;
        ImageView ticks;

        //for voice chat views
        ImageButton playBTN;
        TextView voiceTimeText, voiceTimeDuration;
        SeekBar voiceNoteSeekbar;
        CardView mediaPlayer;

        MyHolder(@NonNull View itemView) {
            super(itemView);

            chatPic = itemView.findViewById(R.id.chatProPic);
            showMessages = itemView.findViewById(R.id.showMessages);
            timeStamp = itemView.findViewById(R.id.chatTimeStamp);
            ticks = itemView.findViewById(R.id.greyTick);

            playBTN = itemView.findViewById(R.id.playVoiceBTN);
            voiceTimeText = itemView.findViewById(R.id.postTimeText);
            voiceTimeDuration = itemView.findViewById(R.id.postTimeDuration);
            voiceNoteSeekbar = itemView.findViewById(R.id.voiceNoteSeekbar);
            mediaPlayer = itemView.findViewById(R.id.voiceCard);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChats.get(position).getSender().equals(firebaseUser.getUid())){

            return MSG_TYPE_RIGHT;

        }else{

            return MSG_TYPE_LEFT;

        }
    }
}
