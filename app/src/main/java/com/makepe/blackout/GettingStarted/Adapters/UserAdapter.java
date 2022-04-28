package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.ChatActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenImageActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.StoryActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.Movement;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalNotifications;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ContactsModel> userList;

    private List<ContactsModel> phoneContacts;
    private ContactsList contacts;

    private DatabaseReference userRef, movementReference;
    private FirebaseUser firebaseUser;

    private UniversalFunctions universalFunctions;
    private UniversalNotifications notifications;

    private String choice;

    public UserAdapter(Context context, ArrayList<ContactsModel> userList, String choice) {
        this.context = context;
        this.userList = userList;
        this.choice = choice;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactsModel user = userList.get(position);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        movementReference = FirebaseDatabase.getInstance().getReference("Movements");
        universalFunctions = new UniversalFunctions(context);
        notifications = new UniversalNotifications(context);

        phoneContacts = new ArrayList<>();
        contacts = new ContactsList(phoneContacts, context);

        if (user.getUSER_ID().equals(firebaseUser.getUid())){
            holder.followBTN.setVisibility(View.GONE);
        }

        getUserDetails(user, holder);
        checkFollowing(user, holder);
        universalFunctions.checkActiveStories(holder.userProPic, user.getUSER_ID());

        holder.userProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (holder.userProPic.getTag().equals("storyActive")){
                    Intent intent = new Intent(context, StoryActivity.class);
                    intent.putExtra("userid", user.getUSER_ID());
                    context.startActivity(intent);
                }else{
                    Intent picIntent = new Intent(context, FullScreenImageActivity.class);
                    picIntent.putExtra("itemID", user.getUSER_ID());
                    picIntent.putExtra("reason", "userImage");
                    context.startActivity(picIntent);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (choice){
                    case "goToProfile":
                        if (!user.getUSER_ID().equals(firebaseUser.getUid())) {
                            Intent intent = new Intent(context, ViewProfileActivity.class);
                            intent.putExtra("uid", user.getUSER_ID());
                            context.startActivity(intent);
                        }
                        break;

                    case "goToChats":
                        Intent messageIntent = new Intent(context, ChatActivity.class);
                        messageIntent.putExtra("userid", user.getUSER_ID());
                        context.startActivity(messageIntent);
                        break;

                    default:
                        Toast.makeText(context, "Unknown Selection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.followBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.followBTN.getText().toString().equals("Follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getUSER_ID()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUSER_ID())
                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                    notifications.addFollowNotifications(user.getUSER_ID());
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getUSER_ID()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUSER_ID())
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });
    }

    private void checkFollowing(ContactsModel user, ViewHolder holder) {
        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(user.getUSER_ID()).exists()){
                    holder.followBTN.setText("Following");
                }else{
                    holder.followBTN.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserDetails(ContactsModel user, ViewHolder holder) {
        contacts.readContacts();
        List<ContactsModel> myContacts = contacts.getContactsList();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user1 = ds.getValue(User.class);

                    assert user1 != null;
                    if (user1.getUSER_ID().equals(user.getUSER_ID())){

                        if (myContacts != null){
                            for (ContactsModel contactsModel : myContacts){
                                if (contactsModel.getNumber().equals(firebaseUser.getPhoneNumber())){
                                    holder.usernameTV.setText("Me");
                                }else if (contactsModel.getNumber().equals(user1.getNumber())){
                                    holder.usernameTV.setText(contactsModel.getUsername());
                                }else{
                                    holder.usernameTV.setText(user1.getUsername());
                                }
                            }
                        }

                        try{
                            Picasso.get().load(user1.getImageURL()).into(holder.userProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.userProPic);
                        }
                    }else{
                        movementReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot snap : snapshot.getChildren()){
                                    Movement movement = snap.getValue(Movement.class);

                                    if(movement.getMovementID().equals(user.getUSER_ID())){
                                        holder.usernameTV.setText(movement.getMovementName());

                                        try{
                                            Picasso.get().load(movement.getMovementProPic()).into(holder.userProPic);
                                        }catch (NullPointerException e){
                                            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.userProPic);
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView userProPic;
        private TextView followBTN, usernameTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userProPic = itemView.findViewById(R.id.userItemProPic);
            followBTN = itemView.findViewById(R.id.userItemFollow);
            usernameTV = itemView.findViewById(R.id.userItemContactName);

        }
    }
}
