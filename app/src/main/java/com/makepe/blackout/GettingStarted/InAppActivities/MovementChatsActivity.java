package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.ChatlistAdapter;
import com.makepe.blackout.GettingStarted.Models.Chat;
import com.makepe.blackout.GettingStarted.Models.ChatList;
import com.makepe.blackout.GettingStarted.Models.Movement;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MovementChatsActivity extends AppCompatActivity {

    private TextView movementName;
    private CircleImageView movementPic;
    private RecyclerView chatsListRecycler;
    private List<ChatList> chatList;

    private DatabaseReference chatListRef, chatsReference, movementReference;

    private ChatlistAdapter chatlistAdapter;

    private String movementID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movement_chats);

        Toolbar movementToolbar = findViewById(R.id.movementChatsToolbar);
        setSupportActionBar(movementToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        chatsListRecycler = findViewById(R.id.movementChatsRecycler);
        movementName = findViewById(R.id.movementChatsUsername);
        movementPic = findViewById(R.id.movementChatProPic);

        movementID = getIntent().getStringExtra("movementID");

        chatListRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(movementID);
        chatsReference = FirebaseDatabase.getInstance().getReference("Chats");
        movementReference = FirebaseDatabase.getInstance().getReference("Movements");

        chatsListRecycler.hasFixedSize();
        chatsListRecycler.setLayoutManager(new LinearLayoutManager(this));

        chatList = new ArrayList<>();

        getChatlist();
        getMovementDetails();

    }

    private void getMovementDetails() {
        movementReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Movement movement = ds.getValue(Movement.class);

                    assert movement != null;
                    if (movement.getMovementID().equals(movementID)){
                        movementName.setText(movement.getMovementName() + " Inbox");

                        try{
                            Picasso.get().load(movement.getMovementProPic()).into(movementPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(movementPic);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getChatlist() {
        chatListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        ChatList cm = ds.getValue(ChatList.class);
                        chatList.add(cm);
                    }

                    //set last message
                    for (int i = 0; i < chatList.size(); i++){
                        lastMessage(chatList.get(i).getId());
                    }
                    chatlistAdapter = new ChatlistAdapter(MovementChatsActivity.this, chatList);
                    chatsListRecycler.setAdapter(chatlistAdapter);

                }else{
                    Toast.makeText(MovementChatsActivity.this, "You have no active chats", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String id) {
        chatsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                String lastTimeStamp = "default";

                for (DataSnapshot ds : snapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);

                    if (chat == null)
                        continue;

                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();

                    if (sender == null || receiver == null)
                        continue;

                    if(chat.getReceiver().equals(movementID) &&
                            chat.getSender().equals(id) ||
                            chat.getReceiver().equals(id) &&
                                    chat.getSender().equals(movementID)) {
                        theLastMessage = chat.getMessage();
                        lastTimeStamp = chat.getTimeStamp();

                        /*try{
                            if (chat.getMsg_type().equals("image")) {
                                theLastMessage = "Sent a photo";
                            } else {
                                theLastMessage = chat.getMessage();
                            }
                            lastTimeStamp = chat.getTimeStamp();
                        }catch (NullPointerException ignored){}*/
                    }
                }
                chatlistAdapter.setLastMessageMap(id, theLastMessage);
                chatlistAdapter.setLastTimeStampMap(id, lastTimeStamp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}