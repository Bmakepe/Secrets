package com.makepe.blackout.GettingStarted.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.makepe.blackout.GettingStarted.Models.ChatList;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {

    private RecyclerView chatListRecycler;
    private List<ChatList> chatList;
    private ProgressBar chatlistLoader;

    private FirebaseUser firebaseUser;
    private DatabaseReference chatListRef, chatsReference;

    private ChatlistAdapter chatlistAdapter;

    public ChatListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        chatListRecycler = view.findViewById(R.id.chatListRecycler);
        chatlistLoader = view.findViewById(R.id.chatlistLoader);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        chatListRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(firebaseUser.getUid());
        chatsReference = FirebaseDatabase.getInstance().getReference("Chats");

        chatListRecycler.hasFixedSize();
        chatListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        getChatList();

        return view;
    }

    private void getChatList() {
        chatList = new ArrayList<>();
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
                        lastMessage(chatList.get(i).getUserID());
                    }
                    chatlistAdapter = new ChatlistAdapter(getContext(), chatList);
                    chatListRecycler.setAdapter(chatlistAdapter);
                    chatlistAdapter.notifyDataSetChanged();
                }else{
                    chatlistLoader.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "You have no active chats", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String userID) {
        chatsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                String lastTimeStamp = "default";

                for (DataSnapshot ds : snapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);

                    if (chat == null)
                        continue;

                    String sender = chat.getSenderID();
                    String receiver = chat.getReceiverID();

                    if (sender == null || receiver == null)
                        continue;

                    if(chat.getReceiverID().equals(firebaseUser.getUid()) &&
                            chat.getSenderID().equals(userID) ||
                            chat.getReceiverID().equals(userID) &&
                                    chat.getSenderID().equals(firebaseUser.getUid())) {
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
                chatlistAdapter.setLastMessageMap(userID, theLastMessage);
                chatlistAdapter.setLastTimeStampMap(userID, lastTimeStamp);

                chatlistLoader.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}