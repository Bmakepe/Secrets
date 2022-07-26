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

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, followReference;

    public ContactsAdapter(List<ContactsModel> contactsList, Context context) {
        this.contactsList = contactsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.contact_display_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        ContactsModel contactsModel = contactsList.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        followReference = FirebaseDatabase.getInstance().getReference("Follow");

        getContactDetails(holder, contactsModel);

        holder.inviteBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, contactsModel.getUsername() + " will be invited to join", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getContactDetails(ViewHolder holder, ContactsModel contactsModel) {

        holder.contactName.setText(contactsModel.getUsername());
        holder.contactNumber.setText(contactsModel.getPhoneNumber());

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ContactsModel contacts = ds.getValue(ContactsModel.class);

                    assert contacts != null;
                    if (contacts.getPhoneNumber().equals(contactsModel.getPhoneNumber())){

                        try{
                            Picasso.get().load(contacts.getImageURL()).into(holder.contactProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.contactProPic);
                        }

                        followReference.child(contacts.getPhoneNumber()).child("following")
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            if (snapshot.child(firebaseUser.getUid()).exists()){
                                                holder.inviteBTN.setText("Following");
                                            }else{
                                                holder.inviteBTN.setText("Follow");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }else{
                        holder.inviteBTN.setText("INVITE");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contactName);
            contactNumber = itemView.findViewById(R.id.personalNumber);
            contactProPic = itemView.findViewById(R.id.contactProPic);
            inviteBTN = itemView.findViewById(R.id.invitationBTN);
        }
    }
}
