package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.MessageAdapter;
import com.makepe.blackout.GettingStarted.Models.Chat;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.Movement;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class  ChatActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView username, onlineStatusTV;
    private ImageButton voiceBTN, attachFiles;
    private RecyclerView chatRecycler;
    private LinearLayout chatNameLayout; 
    private Toolbar messageToolbar;

    private EditText myMessage;
    private GetTimeAgo getTimeAgo;

    private View rootView;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, chatReference, movementReference;

    private List<Chat> mChat;

    private Intent intent;
    private String hisImage, receiverID, textMessage;

    private ContactsList contactsList;
    private List<ContactsModel> phoneBook;

    //for checking if user has seen message or not
    private ValueEventListener seenListener;
    private DatabaseReference userRefForSeen;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_chat);

        messageToolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(messageToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileImage = findViewById(R.id.profileCIV);
        username = findViewById(R.id.hisNameTV);
        chatRecycler = findViewById(R.id.chatRecyclerView);
        chatNameLayout = findViewById(R.id.chatNameLayout);
        onlineStatusTV = findViewById(R.id.statusTV);
        myMessage = findViewById(R.id.messageEt);
        rootView = findViewById(R.id.root_view);
        voiceBTN = findViewById(R.id.voiceBTN);
        attachFiles = findViewById(R.id.attachFiles); 

        intent = getIntent();
        receiverID = intent.getStringExtra("userid");
        getTimeAgo = new GetTimeAgo();

        phoneBook = new ArrayList<>();
        contactsList = new ContactsList(phoneBook, ChatActivity.this);

        chatRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecycler.setLayoutManager(layoutManager);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        chatReference = FirebaseDatabase.getInstance().getReference("Chats");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        movementReference = FirebaseDatabase.getInstance().getReference("Movements");

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

        getUserDetails();
        readMessages();
        seenMessage();
        
        attachFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu popupMenu = new PopupMenu(ChatActivity.this, attachFiles, Gravity.END);
                popupMenu.getMenu().add(Menu.NONE, 0, 0, "Send Images");
                popupMenu.getMenu().add(Menu.NONE, 1, 0, "Send Video");
                popupMenu.getMenu().add(Menu.NONE, 2, 0, "Send Documents");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()){
                            case 0:
                                Toast.makeText(ChatActivity.this, "You will be able to send images", Toast.LENGTH_SHORT).show();
                                break;

                            case 1:
                                Toast.makeText(ChatActivity.this, "You will be able to send videos", Toast.LENGTH_SHORT).show();
                                break;

                            case 2:
                                Toast.makeText(ChatActivity.this, "You will be able to send documents", Toast.LENGTH_SHORT).show();
                                break;

                            default:
                                throw new IllegalStateException("Unexpected value " + menuItem.getItemId());
                        }

                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        voiceBTN.setOnClickListener(v -> {
            textMessage = myMessage.getText().toString();

            if(!TextUtils.isEmpty(textMessage)){
                sendMessage("noAudio");
            }else{
                Toast.makeText(ChatActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
            }
            myMessage.setText("");
        });

        chatNameLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, ViewProfileActivity.class);
            intent.putExtra("uid", receiverID);
            startActivity(intent);
        });
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
                        }else{
                            movementReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()){
                                        Movement movement = ds.getValue(Movement.class);

                                        if (movement.getMovementID().equals(receiverID)){
                                            username.setText(movement.getMovementName());

                                            try{
                                                hisImage = movement.getMovementProPic();
                                                Picasso.get().load(hisImage).into(profileImage);
                                            }catch (NullPointerException e){
                                                Picasso.get().load(R.drawable.default_profile_display_pic).into(profileImage);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    try{
                        if(chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(receiverID)){
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

    private void sendMessage(String choice) {

        String timeStamp = String.valueOf(System.currentTimeMillis());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", firebaseUser.getUid());
        hashMap.put("receiver", receiverID);

        switch (choice){
            case "location":
                Toast.makeText(this, "You will be able to send location", Toast.LENGTH_SHORT).show();
                break;

            case "alert":
                hashMap.put("message", "Haibooo!!!");
                hashMap.put("message_type", "text");
                break;

            case "noAudio":
                hashMap.put("message", textMessage);
                hashMap.put("message_type", "text");
                break;

            default:
                Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();

        }

        hashMap.put("isSeen", false);
        hashMap.put("timeStamp", timeStamp);

        reference.child("Chats").push().setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        myMessage.setText("");
                        myMessage.setHint("Write Your Message");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //create chatlist node/child in firebase database
        final DatabaseReference senderReference = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(firebaseUser.getUid())
                .child(receiverID);
        senderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    senderReference.child("id").setValue(receiverID);
                    senderReference.child("timeStamp").setValue(String.valueOf(System.currentTimeMillis()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference receiverReference = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(receiverID)
                .child(firebaseUser.getUid());
        receiverReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    receiverReference.child("id").setValue(firebaseUser.getUid());
                    senderReference.child("timeStamp").setValue(String.valueOf(System.currentTimeMillis()));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.videoCall:
                Toast.makeText(ChatActivity.this, "Video Call", Toast.LENGTH_SHORT).show();
                break;
            case R.id.voiceCall:
                Toast.makeText(ChatActivity.this, "Voice Call", Toast.LENGTH_SHORT).show();
                break;
            case R.id.profileView:
                userReference.child(receiverID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Intent intent = new Intent(ChatActivity.this, ViewProfileActivity.class);
                            intent.putExtra("uid", receiverID);
                            startActivity(intent);
                        }else{
                            Intent movementIntent = new Intent(ChatActivity.this, MovementDetailsActivity.class);
                            movementIntent.putExtra("movementID", receiverID);
                            startActivity(movementIntent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                break;

            case R.id.sendAlert:
                sendMessage("alert");
                break;
            case R.id.chatSearch:
                Toast.makeText(ChatActivity.this, "Search the chat", Toast.LENGTH_SHORT).show();
                break;
            case R.id.location:
                Toast.makeText(ChatActivity.this, "Send Location", Toast.LENGTH_SHORT).show();
                break;
            case R.id.sendFiles:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_GET_CONTENT);
                shareIntent.setType("*/*");
                startActivity(shareIntent);
                break;

            default:
                Toast.makeText(this, "Unknown Menu Selection", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
