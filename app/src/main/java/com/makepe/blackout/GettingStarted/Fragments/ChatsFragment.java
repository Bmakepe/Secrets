package com.makepe.blackout.GettingStarted.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.makepe.blackout.GettingStarted.Notifications.Token;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private RecyclerView chatlistRecycler;
    private List<ChatlistModel> chatlist;
    private List<User> userList;

    private DatabaseReference reference;
    private FirebaseUser currentUser;
    private ChatlistAdapter chatlistAdapter;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        chatlistRecycler = view.findViewById(R.id.activeChats);
        chatlistRecycler.setHasFixedSize(true);
        chatlistRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        //chatlistRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        chatlist = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatlist.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ChatlistModel cm = ds.getValue(ChatlistModel.class);
                    chatlist.add(cm);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void loadChats() {
        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    for(ChatlistModel chatList : chatlist){
                        assert user != null;
                        if (user.getUSER_ID().equals(chatList.getId())){
                            userList.add(user);
                        }
                    }
                    chatlistAdapter = new ChatlistAdapter(getContext(), userList);
                    chatlistRecycler.setAdapter(chatlistAdapter);

                    //set Last message
                    for(int i = 0; i < userList.size(); i++){
                        lastMessage(userList.get(i).getUSER_ID());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lastMessage(final String user_id) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage = "default";
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    if(chat == null){
                        continue;
                    }

                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();

                    if(sender == null || receiver == null){
                        continue;
                    }

                    if(chat.getReceiver().equals(currentUser.getUid()) &&
                            chat.getSender().equals(user_id) ||
                            chat.getReceiver().equals(user_id) &&
                                    chat.getSender().equals(currentUser.getUid())){
                        theLastMessage = chat.getMessage();
                    }
                }
                chatlistAdapter.setLastMessageMap(user_id, theLastMessage);
                chatlistAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}