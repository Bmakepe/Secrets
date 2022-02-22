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
import android.widget.RadioButton;
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
import com.makepe.blackout.GettingStarted.InAppActivities.PostActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.MyHolder> {
    private DatabaseReference reference;

    private String fireProPic, contactNumber, contactName;
    private ContactsList contacts; //for contacts list class
    private List<ContactsModel> firebaseContacts;//for retrieve firebase contacts
    private Context context;

    public FriendsAdapter(List<ContactsModel> firebaseContacts, Context context) {
        this.firebaseContacts = firebaseContacts;
        this.context = context;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_list_item, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
        //for retrieving phone contacts
        List<ContactsModel> phoneContacts = new ArrayList<>();
        contacts = new ContactsList(phoneContacts, context);

        final String userid = firebaseContacts.get(position).getUSER_ID();

        matchContacts(firebaseContacts, holder, position);

        checkOnlineStatus(userid, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(!holder.selector.isChecked()) {
                    Toast.makeText(context, contactName + " has been selected", Toast.LENGTH_SHORT).show();
                    holder.selector.toggle();
                }else{

                }*/
                holder.selector.toggle();
                Toast.makeText(context, contactName + " has selected. His Number is " + contactNumber, Toast.LENGTH_SHORT).show();
                Intent tags = new Intent(context, PostActivity.class);
                tags.putExtra("taggedPeople", contactNumber);
                context.startActivity(tags);
            }
        });

    }

    private void matchContacts(List<ContactsModel> contactsList, MyHolder holder, int position) {
        contacts.readContacts();
        List<ContactsModel> myContacts = contacts.getContactsList();
        String fireNumber = contactsList.get(position).getNumber();

        if(myContacts != null){

            for(ContactsModel contactsModel : myContacts){

                if(contactsModel.getNumber().equals(fireNumber)){
                    contactNumber = contactsModel.getNumber();
                    contactName = contactsModel.getUsername();
                    holder.contactName.setText(contactName);
                    getProPic(contactsList, holder);
                }

            }
        }else{
            Toast.makeText(context, "contact list is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void getProPic(List<ContactsModel> contactsList, MyHolder holder) {

        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.keepSynced(true);
        for(int i = 0; i < contactsList.size(); i++){
            checkingMatchedData(contactsList.get(i).getNumber(), holder);
        }
    }

    private void checkingMatchedData(final String number, final MyHolder holder) {
        try{
            Query query = reference.orderByChild("Number").equalTo(contactNumber);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        if(number.equals(contactNumber)){

                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                fireProPic = "" + ds.child("ImageURL").getValue();

                                Picasso.get().load(fireProPic).networkPolicy(NetworkPolicy.OFFLINE).into(holder.contactProPic, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Picasso.get().load(fireProPic).into(holder.contactProPic);
                                    }
                                });
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }catch (NumberFormatException ignored){}

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
                        holder.onlineStatus.setImageResource(R.drawable.online_circle);
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

    @Override
    public int getItemCount() {
        if(firebaseContacts == null){
            return 0;
        }
        return firebaseContacts.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        TextView contactName;
        CircleImageView contactProPic;
        ImageView onlineStatus, doneTagBTN;
        RadioButton selector;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contactName);
            contactProPic = itemView.findViewById(R.id.contactProPic);
            onlineStatus = itemView.findViewById(R.id.contactOnlineStatus);
            selector = itemView.findViewById(R.id.contactSelector);
            doneTagBTN = itemView.findViewById(R.id.doneTagBTN);
        }
    }
}
