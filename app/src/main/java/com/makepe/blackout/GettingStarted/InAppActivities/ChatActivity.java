package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makepe.blackout.GettingStarted.Adapters.MessageAdapter;
import com.makepe.blackout.GettingStarted.Fragments.APIService;
import com.makepe.blackout.GettingStarted.MainActivity;
import com.makepe.blackout.GettingStarted.Models.Chat;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.Notifications.Client;
import com.makepe.blackout.GettingStarted.Notifications.Data;
import com.makepe.blackout.GettingStarted.Notifications.MyResponse;
import com.makepe.blackout.GettingStarted.Notifications.Sender;
import com.makepe.blackout.GettingStarted.Notifications.Token;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.RecordingClasses.ViewProxy;
import com.makepe.blackout.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Response;

public class  ChatActivity extends AppCompatActivity {

    CircleImageView profileImage;
    TextView username, onlineStatusTV;
    ImageView chatback, callBTN, videoBTN, chatMenu;
    ImageButton voiceBTN;
    RecyclerView chatRecycler;
    LinearLayout chatNameLayout;
    RelativeLayout messageArea;

    EditText myMessage;
    GetTimeAgo getTimeAgo;

    View rootView;

    FirebaseUser firebaseUser;
    DatabaseReference userReference, chatReference;

    List<Chat> mChat;

    Intent intent;
    String hisImage, receiverID;

    private ContactsList contactsList;
    private List<ContactsModel> phoneBook;

    //for checking if user has seen message or not
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        profileImage = findViewById(R.id.profileCIV);
        username = findViewById(R.id.hisNameTV);
        chatback = findViewById(R.id.chatBackBTN);
        chatRecycler = findViewById(R.id.chatRecyclerView);
        callBTN = findViewById(R.id.callBTN);
        videoBTN = findViewById(R.id.videoBTN);
        chatNameLayout = findViewById(R.id.chatNameLayout);
        onlineStatusTV = findViewById(R.id.statusTV);
        chatMenu = findViewById(R.id.chatMenu);
        myMessage = findViewById(R.id.messageEt);
        rootView = findViewById(R.id.root_view);
        voiceBTN = findViewById(R.id.voiceBTN);
        messageArea = findViewById(R.id.messageArea);

        intent = getIntent();
        receiverID = intent.getStringExtra("userid");

        myMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(charSequence.length() != 0){
                    voiceBTN.setImageResource(R.drawable.ic_send_black_24dp);
                }else{
                    voiceBTN.setImageResource(R.drawable.ic_mic_black_24dp);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        getTimeAgo = new GetTimeAgo();

        phoneBook = new ArrayList<>();
        contactsList = new ContactsList(phoneBook, ChatActivity.this);

        chatRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        chatRecycler.setLayoutManager(layoutManager);

        chatback.setOnClickListener(v -> finish());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        chatReference = FirebaseDatabase.getInstance().getReference("Chats");
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        getUserDetails();
        readMessages();

        voiceBTN.setOnClickListener(v -> {
            String msg = myMessage.getText().toString();

            if(!TextUtils.isEmpty(msg)){
                sendMessage(msg);
            }else{
                Toast.makeText(ChatActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
            }
            myMessage.setText("");
        });

        callBTN.setOnClickListener(v -> Toast.makeText(ChatActivity.this, "Voice Call", Toast.LENGTH_SHORT).show());

        videoBTN.setOnClickListener(v -> Toast.makeText(ChatActivity.this, "Video Call", Toast.LENGTH_SHORT).show());

        chatNameLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, ViewProfileActivity.class);
            intent.putExtra("uid", receiverID);
            startActivity(intent);
        });

        chatMenu.setOnClickListener(v -> showMoreOptions(receiverID));
    }

    private void getUserDetails() {
        contactsList.readContacts();
        final List<ContactsModel> myContacts = contactsList.getContactsList();

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);

                        assert user != null;
                        if (user.getUSER_ID().equals(receiverID)){

                            for(ContactsModel cm : myContacts){
                                if(cm.getNumber().equals(user.getNumber())){
                                    username.setText(cm.getUsername());
                                }else{
                                    username.setText(user.getUsername());
                                }
                            }

                            if(user.getOnlineStatus().equals("online")){
                                onlineStatusTV.setText(user.getOnlineStatus());
                                onlineStatusTV.setVisibility(View.VISIBLE);
                            }else{
                                try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                                    String pTime = getTimeAgo.getTimeAgo(Long.parseLong(user.getOnlineStatus()), ChatActivity.this);
                                    onlineStatusTV.setVisibility(View.VISIBLE);
                                    onlineStatusTV.setText("Last seen: " + pTime);
                                }catch (NumberFormatException n){
                                    Toast.makeText(ChatActivity.this, "Could not format time", Toast.LENGTH_SHORT).show();
                                }
                            }

                            try{
                                hisImage = user.getImageURL();
                                Picasso.get().load(hisImage).into(profileImage);
                            }catch (NullPointerException e){
                                Picasso.get().load(R.drawable.default_profile_display_pic).into(profileImage);
                            }
                            seenMessage(firebaseUser.getUid(), receiverID);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showMoreOptions(final String hisUID) {
        PopupMenu popupMenu = new PopupMenu(ChatActivity.this, chatMenu, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE, 0,0,"View Profile");
        popupMenu.getMenu().add(Menu.NONE, 1,0,"Send Alert");
        popupMenu.getMenu().add(Menu.NONE, 2,0,"Search");
        popupMenu.getMenu().add(Menu.NONE, 3,0,"Send Files");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == 0){
                    Intent intent = new Intent(ChatActivity.this, ViewProfileActivity.class);
                    intent.putExtra("uid", hisUID);
                    startActivity(intent);
                }else if(id == 1){
                    Toast.makeText(ChatActivity.this, "Ping", Toast.LENGTH_SHORT).show();
                    sendMessage("Ayoooo");
                }else if (id == 2){
                    Toast.makeText(ChatActivity.this, "Search the chat", Toast.LENGTH_SHORT).show();
                }else if(id == 3){
                    Toast.makeText(ChatActivity.this, "Send Media to your friend", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void seenMessage(final String uid, final String userid) {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        userRefForSeen.keepSynced(true);
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    try{
                        if(chat.getReceiver().equals(uid) && chat.getSender().equals(userid)){
                            HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                            hasSeenHashMap.put("isSeen", true);
                            ds.getRef().updateChildren(hasSeenHashMap);
                        }
                    }catch (NullPointerException ignored){}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String message) {

        String timeStamp = String.valueOf(System.currentTimeMillis());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseUser.getUid());
        hashMap.put("receiver", receiverID);
        hashMap.put("message", message);
        hashMap.put("audio", "noAudio");
        hashMap.put("isSeen", false);
        hashMap.put("timeStamp", timeStamp);

        reference.child("Chats").push().setValue(hashMap);

        final String msg = message;

        /*reference = FirebaseDatabase.getInstance().getReference("").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(hisUID, user.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        //create chatlist node/child in firebase database
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(firebaseUser.getUid())
                .child(receiverID);
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(receiverID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(receiverID)
                .child(firebaseUser.getUid());
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef1.child("id").setValue(firebaseUser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages(){
        mChat = new ArrayList<>();
        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);

                    assert chat != null;
                    if(chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(receiverID) ||
                            chat.getReceiver().equals(receiverID) && chat.getSender().equals(firebaseUser.getUid())){
                        mChat.add(chat);
                    }
                }
                chatRecycler.setAdapter(new MessageAdapter(ChatActivity.this, mChat, hisImage));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
