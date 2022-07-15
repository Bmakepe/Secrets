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
import android.widget.CheckBox;
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
import com.makepe.blackout.GettingStarted.InAppActivities.PosteActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.User;
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
    private DatabaseReference userReference;

    private List<User> firebaseContacts;//for retrieve firebase contacts
    private Context context;
    public ArrayList<User> taggedFriends = new ArrayList<>();

    public FriendsAdapter(List<User> firebaseContacts, Context context) {
        this.firebaseContacts = firebaseContacts;
        this.context = context;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(context).inflate(R.layout.friend_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
        //for retrieving phone contacts
        User contact = firebaseContacts.get(position);
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        getContacts(holder, contact);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!holder.selector.isChecked()){
                    holder.selector.setChecked(true);
                    taggedFriends.add(contact);
                    //posteActivity.taggedUsers.add(contact);
                }else{
                    holder.selector.setChecked(false);
                    taggedFriends.remove(contact);
                    //posteActivity.taggedUsers.remove(contact);
                }
            }
        });

    }

    private void getContacts(MyHolder holder, User contact) {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User contactsModel = ds.getValue(User.class);

                    assert contactsModel != null;
                    if (contactsModel.getUSER_ID().equals(contact.getUSER_ID())){
                        holder.contactName.setText(contactsModel.getUsername());
                        try{
                            Picasso.get().load(contact.getImageURL()).into(holder.contactProPic);
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

    @Override
    public int getItemCount() {
        return firebaseContacts.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        public TextView contactName;
        public CircleImageView contactProPic;
        public CheckBox selector;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contactName);
            contactProPic = itemView.findViewById(R.id.contactProPic);
            selector = itemView.findViewById(R.id.contactSelector);
        }
    }
}
