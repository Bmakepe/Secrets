package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.ChatActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenImageActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.StoryActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.ChatList;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.Movement;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatlistAdapter extends RecyclerView.Adapter<ChatlistAdapter.MyHolder> {

    private Context context;
    private List<ChatList> chatList;

    private HashMap<String, String> lastMessageMap;
    private HashMap<String, String> lastTimeStampMap;

    private GetTimeAgo getTimeAgo;
    private UniversalFunctions universalFunctions;

    private DatabaseReference userReference, movementReference;

    public ChatlistAdapter(Context context, List<ChatList> chatList) {
        this.context = context;
        this.chatList = chatList;
        lastMessageMap = new HashMap<>();
        lastTimeStampMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //get data;
        ChatList chats = chatList.get(position);
        getTimeAgo= new GetTimeAgo();
        universalFunctions = new UniversalFunctions(context);
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        movementReference = FirebaseDatabase.getInstance().getReference("Movements");


        String lastmessage = lastMessageMap.get(chats.getId());
        String lastTime = lastTimeStampMap.get(chats.getId());

        getUserDetails(chats.getId(), holder);
        universalFunctions.checkActiveStories(holder.chatlistProPic, chats.getId());

        //set Data

        if(lastmessage == null || lastmessage.equals("default")){
            holder.chatlistMessage.setVisibility(View.GONE);
        }else{
            holder.chatlistMessage.setVisibility(View.VISIBLE);
            holder.chatlistMessage.setText(lastmessage);

            String postTime = getTimeAgo.getTimeAgo(Long.parseLong(lastTime), context);
            holder.timestamp.setText("-" + postTime);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity with that user
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("userid", chats.getId());
                context.startActivity(intent);
            }
        });

        holder.chatlistProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.chatlistProPic.getTag().equals("storyActive")){

                    Intent intent = new Intent(context, StoryActivity.class);
                    intent.putExtra("userid", chats.getId());
                    context.startActivity(intent);
                }else{
                    Intent picIntent = new Intent(context, FullScreenImageActivity.class);
                    picIntent.putExtra("itemID", chats.getId());
                    picIntent.putExtra("reason", "userImage");
                    context.startActivity(picIntent);
                }
            }
        });
    }

    private void getUserDetails(String hisUid, final MyHolder holder) {
        List<ContactsModel> phoneBook = new ArrayList<>();
        ContactsList contactsList = new ContactsList(phoneBook, context);
        contactsList.readContacts();
        final List<ContactsModel> phoneNumbers = contactsList.getContactsList();

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        User user = ds.getValue(User.class);

                        assert user != null;
                        if (user.getUSER_ID().equals(hisUid)) {

                            for (ContactsModel cm : phoneNumbers) {
                                if (cm.getNumber().equals(user.getNumber())) {
                                    holder.chatListUser.setText(cm.getUsername());
                                }else{
                                    holder.chatListUser.setText(user.getUsername());
                                }
                            }

                            try {
                                Picasso.get().load(user.getImageURL()).into(holder.chatlistProPic);
                            } catch (NullPointerException e) {
                                Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.chatlistProPic);
                            }
                        }else{
                            movementReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()){
                                        Movement movement = ds.getValue(Movement.class);

                                        assert movement != null;
                                        if (movement.getMovementID().equals(hisUid)){
                                            holder.chatListUser.setText(movement.getMovementName());

                                            try{
                                                Picasso.get().load(movement.getMovementProPic()).into(holder.chatlistProPic);
                                            }catch (NullPointerException e){
                                                Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.chatlistProPic);
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

    public void setLastMessageMap(String userid, String lastMessage){
        lastMessageMap.put(userid, lastMessage);
    }

    public void setLastTimeStampMap(String userId, String lastTimeStamp){
        lastTimeStampMap.put(userId, lastTimeStamp);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        CircleImageView chatlistProPic;// chatlistCover;
        TextView chatListUser, chatlistMessage, onlineStatus, timestamp;// unreadTexts;

        MyHolder(@NonNull View itemView) {
            super(itemView);

            chatlistProPic = itemView.findViewById(R.id.chatListPropic);;
            chatListUser = itemView.findViewById(R.id.chatlistUsername);
            chatlistMessage = itemView.findViewById(R.id.chatlistMessage);
            onlineStatus = itemView.findViewById(R.id.chatlistLastSeen);
            timestamp = itemView.findViewById(R.id.cl_message_timeStamp);
        }
    }
}
