package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.ChatActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatlistAdapter extends RecyclerView.Adapter<ChatlistAdapter.MyHolder> {

    private Context context;
    private List<User> chatList;
    private HashMap<String, String> lastMessageMap;
    private HashMap<String, String> lastTimeStampMap;

    private GetTimeAgo getTimeAgo;

    private DatabaseReference userRef;

    public ChatlistAdapter(Context context, List<User> chatList) {
        this.context = context;
        this.chatList = chatList;
        lastMessageMap = new HashMap<>();
        lastTimeStampMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       // View view = LayoutInflater.from(context).inflate(R.layout.raw_user_list, parent, false);

        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //get data;
        User user = chatList.get(position);
        getTimeAgo= new GetTimeAgo();
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        final String hisUid = user.getUSER_ID();
        String lastmessage = lastMessageMap.get(hisUid);
        String lastTime = lastTimeStampMap.get(hisUid);

        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        getUserDetails(hisUid, holder);

        //set Data

        if(lastmessage == null || lastmessage.equals("default")){
            holder.chatlistMessage.setVisibility(View.GONE);
        }else{
            holder.chatlistMessage.setVisibility(View.VISIBLE);
            holder.chatlistMessage.setText(lastmessage);

            String postTime = getTimeAgo.getTimeAgo(Long.parseLong(lastTime), context);
            holder.timestamp.setText("-" + postTime);
        }

        //for online status
        if(user.getOnlineStatus().equals("online")){
            //online
            holder.chatlistStatus.setImageResource(R.drawable.online_circle);
            holder.onlineStatus.setText("Online");
        }else{
            //offline
            holder.chatlistStatus.setImageResource(R.drawable.offline_circle);
            try{
                String lastSeen = getTimeAgo.getTimeAgo(Long.parseLong(user.getOnlineStatus()), context);
                holder.onlineStatus.setText("last seen " + lastSeen);
            }catch (NumberFormatException n){
                Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity with that user
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("userid", hisUid);
                context.startActivity(intent);
            }
        });
    }

    private void getUserDetails(String hisUid, final MyHolder holder) {
        List<ContactsModel> phoneBook = new ArrayList<>();
        ContactsList contactsList = new ContactsList(phoneBook, context);
        contactsList.readContacts();
        final List<ContactsModel> phoneNumbers = contactsList.getContactsList();

        userRef.addValueEventListener(new ValueEventListener() {
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

        CircleImageView chatlistStatus, chatlistProPic;// chatlistCover;
        TextView chatListUser, chatlistMessage, typingTV, onlineStatus, timestamp;// unreadTexts;

        MyHolder(@NonNull View itemView) {
            super(itemView);

            chatlistProPic = itemView.findViewById(R.id.chatListPropic);;
            chatListUser = itemView.findViewById(R.id.chatlistUsername);
            chatlistMessage = itemView.findViewById(R.id.chatlistMessage);
            typingTV = itemView.findViewById(R.id.typingTV);
            chatlistStatus = itemView.findViewById(R.id.chatlistStatus);
            onlineStatus = itemView.findViewById(R.id.chatlistOnlineStatus);
            timestamp = itemView.findViewById(R.id.cl_message_timeStamp);
            /*chatlistCover = itemView.findViewById(R.id.chatlistCover);
            unreadTexts = itemView.findViewById(R.id.unreadTexts);*/
        }
    }
}
