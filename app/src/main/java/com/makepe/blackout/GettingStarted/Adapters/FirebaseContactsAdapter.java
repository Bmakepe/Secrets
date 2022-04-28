package com.makepe.blackout.GettingStarted.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.ChatActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenImageActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.StoryActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class FirebaseContactsAdapter extends RecyclerView.Adapter<FirebaseContactsAdapter.MyHolder> {

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference;

    private String fireProPic, status;

    private List<ContactsModel> firebaseContacts;//for retrieving firebase contacts
    private ContactsList contacts; //for contacts list class
    private Context context;

    private UniversalFunctions universalFunctions;

    public FirebaseContactsAdapter(List<ContactsModel> firebaseContacts, Context context) {
        this.firebaseContacts = firebaseContacts;
        this.context = context;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_display_item, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        universalFunctions = new UniversalFunctions(context);
        final ContactsModel user = firebaseContacts.get(position);
        //for retrieving phone contacts
        List<ContactsModel> phoneContacts = new ArrayList<>();
        contacts = new ContactsList(phoneContacts, context);

        final String userid = firebaseContacts.get(position).getUSER_ID();

        matchContacts(firebaseContacts, holder, position);
        isFollowing(userid, holder.contactFollowBTN);
        universalFunctions.checkActiveStories(holder.contactProPic, userid);

        checkOnlineStatus(userid, holder);

        holder.contactProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.contactProPic.getTag().equals("storyActive")){

                    Intent intent = new Intent(context, StoryActivity.class);
                    intent.putExtra("userid", userid);
                    context.startActivity(intent);
                }else{
                    Intent picIntent = new Intent(context, FullScreenImageActivity.class);
                    picIntent.putExtra("itemID", user.getUSER_ID());
                    picIntent.putExtra("reason", "userImage");
                    context.startActivity(picIntent);
                }
            }
        });

        holder.inviteBTN.setVisibility(View.GONE);
        holder.contactFollowBTN.setVisibility(View.VISIBLE);
        if(user.getUSER_ID().equals(firebaseUser.getUid())){
            holder.contactFollowBTN.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewProfileActivity.class);
                intent.putExtra("uid", user.getUSER_ID());
                context.startActivity(intent);
            }
        });

        holder.contactFollowBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.contactFollowBTN.getTag().equals("Follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getUSER_ID()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUSER_ID())
                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                    addNotifications(user.getUSER_ID());
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getUSER_ID()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUSER_ID())
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });

    }

    private void matchContacts(List<ContactsModel> contactsList, MyHolder holder, int position) {
        contacts.readContacts();
        List<ContactsModel> myContacts = contacts.getContactsList();
        String fireNumber = contactsList.get(position).getNumber();

        if(myContacts != null){

          //  for (ContactsModel cm : contactsList) {
                //for retrieving contacts from firebase

                for(ContactsModel contactsModel : myContacts){

                    if(contactsModel.getNumber().equals(fireNumber)){
                        holder.contactName.setText(contactsModel.getUsername());
                        holder.contactNumber.setText(fireNumber);
                        getProPic(contactsList, holder);
                    }else if (contactsModel.getNumber().equals(firebaseUser.getPhoneNumber())){
                        holder.contactName.setText("Me");
                        holder.contactNumber.setText(fireNumber);
                        getMyProPic(holder);
                    }


                }
           // }
        }else{
            Toast.makeText(context, "contact list is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void getMyProPic(MyHolder holder) {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUSER_ID().equals(firebaseUser.getUid())){
                        try{
                            Picasso.get().load(user.getImageURL()).into(holder.contactProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.contactProPic);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getProPic(List<ContactsModel> contactsList, MyHolder holder) {
        for(int i = 0; i < contactsList.size(); i++){
            checkingMatchedData(contactsList.get(i).getNumber(), holder);
        }
    }

    private void checkingMatchedData(final String number, final MyHolder holder) {
        try{
            Query query = userReference.orderByChild("Number").equalTo(holder.contactNumber.getText().toString());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){

                        if(number.equals(holder.contactNumber.getText().toString())){
                            holder.contactFollowBTN.setVisibility(View.VISIBLE);
                            holder.inviteBTN.setVisibility(View.GONE);

                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                fireProPic = "" + ds.child("ImageURL").getValue();
                                status = "" + ds.child("onlineStatus").getValue();
                                Calendar calendar = Calendar.getInstance(Locale.getDefault());

                                Picasso.get().load(fireProPic).networkPolicy(NetworkPolicy.OFFLINE).into(holder.contactProPic, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Picasso.get().load(fireProPic).into(holder.contactProPic);
                                    }
                                });

                                if(status.equals("online")){
                                    holder.onlineStatusTV.setVisibility(View.VISIBLE);
                                    holder.onlineStatusTV.setText("Online");
                                }else{
                                    try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                                        calendar.setTimeInMillis(Long.parseLong(status));
                                        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                                        holder.onlineStatusTV.setVisibility(View.VISIBLE);
                                        holder.onlineStatusTV.setText("Last seen at: " + pTime);
                                    }catch (NumberFormatException n){
                                        Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }else {
                        holder.contactFollowBTN.setVisibility(View.GONE);
                        holder.inviteBTN.setVisibility(View.VISIBLE);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }catch (NumberFormatException ignored){}

    }

    private void isFollowing(final String userid, final ImageView followBTN){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userid).exists()){
                    //executes if following user
                    followBTN.setImageResource(R.drawable.ic_person_black_24dp);
                    followBTN.setTag("Following");
                }else{
                    followBTN.setImageResource(R.drawable.ic_person_add_black_24dp);
                    followBTN.setTag("Follow");
                    //executes if you are not following the user
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkOnlineStatus(String uid, final MyHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = reference.orderByChild("USER_ID").equalTo(uid);
        //display and retrieve current user info
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    String userOnline = "" + ds.child("onlineStatus").getValue();

                    if(userOnline.equals("online")){
                        holder.onlineStatus.setVisibility(View.VISIBLE);
                    }else{
                        holder.onlineStatus.setVisibility(View.VISIBLE);
                        holder.onlineStatus.setImageResource(R.drawable.offline_circle);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNotifications(String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);
        hashMap.put("isStory", false);
        hashMap.put("timeStamp", timeStamp);

        reference.push().setValue(hashMap);
    }

    @Override
    public int getItemCount() {
        if(firebaseContacts == null){
            return 0;
        }
        return firebaseContacts.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        TextView contactName, contactNumber, inviteBTN, onlineStatusTV, lastSeen;
        CircleImageView contactProPic, superProPic;
        ImageView contactFollowBTN, onlineStatus, viewProfile, sendMessage,
                callBTN, fullScreenPic;
        LinearLayout nameLayout;
        Dialog proPicPopUp, fullScreenProPic;

        MyHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contactName);
            contactNumber = itemView.findViewById(R.id.personalNumber);
            contactProPic = itemView.findViewById(R.id.contactProPic);
            contactFollowBTN = itemView.findViewById(R.id.contactFollowBTN);
            nameLayout = itemView.findViewById(R.id.nameLayout);
            onlineStatus = itemView.findViewById(R.id.contactOnlineStatus);
            inviteBTN = itemView.findViewById(R.id.invitationBTN);
            onlineStatusTV = itemView.findViewById(R.id.contactStatus);
        }
    }


}
