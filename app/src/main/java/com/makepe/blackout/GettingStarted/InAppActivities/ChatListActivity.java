package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.makepe.blackout.GettingStarted.Models.ChatlistModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {
    private RecyclerView chatListRecycler;
    private EditText chatListSearchEd;
    private ProgressBar chatlistLoader;

    private FirebaseUser firebaseUser;
    private DatabaseReference chatListRef, userReference, chatsReference;
    private List<ChatlistModel> chatList;
    private List<User> userList;

    private ChatlistAdapter chatlistAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        chatListRecycler = findViewById(R.id.chatListRecycler);
        chatListSearchEd = findViewById(R.id.chatListSearchEd);
        chatlistLoader = findViewById(R.id.chatlistLoader);

        chatListSearchEd.setHint("Search");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        chatListRef = FirebaseDatabase.getInstance().getReference("ChatList");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        chatsReference = FirebaseDatabase.getInstance().getReference("Chats");
        chatList = new ArrayList<>();
        userList = new ArrayList<>();

        getChatList();

        findViewById(R.id.chatListContactsBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(ChatListActivity.this, MyContactsActivity.class));
            }
        });

        findViewById(R.id.chatListBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getChatList() {
        chatListRef.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        ChatlistModel cm = ds.getValue(ChatlistModel.class);
                        chatList.add(cm);
                    }
                    loadChats();
                }else{
                    chatlistLoader.setVisibility(View.GONE);
                    Toast.makeText(ChatListActivity.this, "You have no active chats", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadChats() {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    for (ChatlistModel chatList : chatList){
                        if (user.getUSER_ID().equals(chatList.getId())){
                            userList.add(user);
                        }
                    }
                    chatlistAdapter = new ChatlistAdapter(ChatListActivity.this, userList);
                    chatListRecycler.setAdapter(chatlistAdapter);

                    //set last message
                    for (int i = 0; i < userList.size(); i++){
                        lastMessage(userList.get(i).getUSER_ID());
                    }

                    chatlistLoader.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String user_id) {
        chatsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "def" + "ault";
                String lastTimeStamp = "default";

                for (DataSnapshot ds : snapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);

                    if (chat == null)
                        continue;

                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();

                    if (sender == null || receiver == null)
                        continue;

                    if(chat.getReceiver().equals(firebaseUser.getUid()) &&
                            chat.getSender().equals(user_id) ||
                            chat.getReceiver().equals(user_id) &&
                                    chat.getSender().equals(firebaseUser.getUid())) {

                        try{
                            if (chat.getMsg_type().equals("image")) {
                                theLastMessage = "Sent a photo";
                            } else {
                                theLastMessage = chat.getMessage();
                            }
                            lastTimeStamp = chat.getTimeStamp();
                        }catch (NullPointerException ignored){}
                    }
                }
                chatlistAdapter.setLastMessageMap(user_id, theLastMessage);
                chatlistAdapter.setLastTimeStampMap(user_id, lastTimeStamp);

                chatlistAdapter.notifyDataSetChanged();

                chatlistLoader.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}