package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private List<ContactsModel> contactsList;
    private Context context;

    private FirebaseUser user;
    private DatabaseReference reference;

    String fireName, fireNumber, fireID, fireProPic;

    public ContactsAdapter(List<ContactsModel> contactsList, Context context) {
        this.contactsList = contactsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_display_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        user = FirebaseAuth.getInstance().getCurrentUser();

        final String name = contactsList.get(position).getUsername();
        final String number = contactsList.get(position).getNumber();
        final String userid = contactsList.get(position).getUSER_ID();

        holder.contactName.setText(name);
        holder.contactNumber.setText(number);

        checkOnlineStatus(userid, holder);

        holder.nameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(fireNumber.equals(number)){
                        if(fireID.equals(user.getUid()))
                            Toast.makeText(context, "can not chat to yourself", Toast.LENGTH_SHORT).show();
                        else{
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("userid", fireID);
                            intent.putExtra("name", name);
                            context.startActivity(intent);
                        }
                    }else{
                        Toast.makeText(context, "invite " + name + " to join", Toast.LENGTH_SHORT).show();
                    }
                }catch(NullPointerException ignored){}

            }
        });

        holder.inviteBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, name + " has been invited to join", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getAllUserList(List<ContactsModel> contactsList, ViewHolder holder) {

        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.keepSynced(true);
        for(int i = 0; i < contactsList.size(); i++){
            checkingMatchedData(contactsList.get(i).getNumber(), holder);
        }
    }

    private void checkingMatchedData(final String number, final ViewHolder holder) {
        try{
            Query query = reference.orderByChild("Number").equalTo(holder.contactNumber.getText().toString());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        if(number.equals(holder.contactNumber.getText().toString())){
                            holder.contactFollowBTN.setVisibility(View.VISIBLE);
                            holder.inviteBTN.setVisibility(View.GONE);

                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                fireID = "" + ds.child("USER_ID").getValue();
                                fireName = "" + ds.child("Username").getValue();
                                fireNumber = "" + ds.child("Number").getValue();
                                fireProPic = "" + ds.child("ImageURL").getValue();

                                try{
                                    Picasso.get().load(fireProPic).into(holder.contactProPic);
                                }catch (NullPointerException e){
                                    Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.contactProPic);
                                }
                            }
                        }

                    }else {
                        holder.contactFollowBTN.setVisibility(View.GONE);
                        holder.inviteBTN.setVisibility(View.VISIBLE);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }catch (NumberFormatException e){

        }

    }

    @Override
    public int getItemCount() {
        if(contactsList == null){
            return 0;
        }
        return contactsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView contactName, contactNumber, inviteBTN;
        CircleImageView contactProPic;
        ImageView contactFollowBTN, onlineStatus;
        LinearLayout nameLayout;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contactName);
            contactNumber = itemView.findViewById(R.id.personalNumber);
            contactProPic = itemView.findViewById(R.id.contactProPic);
            contactFollowBTN = itemView.findViewById(R.id.contactFollowBTN);
            nameLayout = itemView.findViewById(R.id.nameLayout);
            onlineStatus = itemView.findViewById(R.id.contactOnlineStatus);
            inviteBTN = itemView.findViewById(R.id.invitationBTN);
        }
    }


    private void checkOnlineStatus(String uid, final ViewHolder holder) {
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
                        holder.onlineStatus.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
