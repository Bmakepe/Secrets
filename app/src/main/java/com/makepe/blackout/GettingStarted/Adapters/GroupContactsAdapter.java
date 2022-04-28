package com.makepe.blackout.GettingStarted.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenImageActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.StoryActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.GroupsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupContactsAdapter extends RecyclerView.Adapter<GroupContactsAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;

    private String groupID, myGroupRole;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, groupReference;

    private UniversalFunctions universalFunctions;

    public GroupContactsAdapter(Context context, List<User> userList, String groupID, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupID = groupID;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.group_members_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        groupReference = FirebaseDatabase.getInstance().getReference("SecretGroups");
        universalFunctions = new UniversalFunctions(context);
        User user = userList.get(position);

        getContactDetails(user, holder);
        checkGroupAttendance(user, holder);
        universalFunctions.checkActiveStories(holder.contactProPic, user.getUSER_ID());

        holder.contactProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (holder.contactProPic.getTag().equals("storyActive")){

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
                groupReference.child(groupID).child("Participants").child(user.getUSER_ID())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){//user Exists/ is participant

                                    String hisPreviousRole = "" + snapshot.child("groupRole").getValue();

                                    String[] options;

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Choose Option");

                                    if(myGroupRole.equals("creator")){
                                        if(hisPreviousRole.equals("admin")){
                                            options = new String[]{"Remove Admin", "Remove User", "View Profile"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int which) {
                                                    if(which == 0){
                                                        //remove admin clicked
                                                        removeAdmin(user);

                                                    }else if(which == 1){
                                                        //remove user clicked
                                                        removeParticipant(user);

                                                    }else{
                                                        if(!user.getUSER_ID().equals(firebaseUser.getUid())){
                                                            Intent intent = new Intent(context, ViewProfileActivity.class);
                                                            intent.putExtra("uid", user.getUSER_ID());
                                                            context.startActivity(intent);
                                                        }
                                                    }

                                                }
                                            }).show();
                                        }
                                        else if(hisPreviousRole.equals("participant")){
                                            //normal participant
                                            options = new String[]{"Make Admin", "Remove User", "View Profile"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int which) {
                                                    if(which == 0){
                                                        //remove admin clicked
                                                        makeAdmin(user);

                                                    }else if(which == 1){
                                                        //remove user clicked
                                                        removeParticipant(user);

                                                    }else{
                                                        if(!user.getUSER_ID().equals(firebaseUser.getUid())){
                                                            Intent intent = new Intent(context, ViewProfileActivity.class);
                                                            intent.putExtra("uid", user.getUSER_ID());
                                                            context.startActivity(intent);
                                                        }
                                                    }

                                                }
                                            }).show();
                                        }
                                    }
                                    else if(myGroupRole.equals("admin")){
                                        if(hisPreviousRole.equals("creator")){
                                            if(!user.getUSER_ID().equals(firebaseUser.getUid())){
                                                Intent intent = new Intent(context, ViewProfileActivity.class);
                                                intent.putExtra("uid", user.getUSER_ID());
                                                context.startActivity(intent);
                                            }
                                        }
                                        else if(hisPreviousRole.equals("admin")){
                                            options = new String[]{"Remove Admin", "Remove User", "View Profile"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int which) {
                                                    if(which == 0){
                                                        //remove admin clicked
                                                        removeAdmin(user);

                                                    }else if(which == 1){
                                                        //remove user clicked
                                                        removeParticipant(user);

                                                    }else{
                                                        if(!user.getUSER_ID().equals(firebaseUser.getUid())){
                                                            Intent intent = new Intent(context, ViewProfileActivity.class);
                                                            intent.putExtra("uid", user.getUSER_ID());
                                                            context.startActivity(intent);
                                                        }
                                                    }

                                                }
                                            }).show();
                                        }
                                        else if(hisPreviousRole.equals("participant")){
                                            options = new String[]{"Make Admin", "Remove User", "View Profile"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int which) {
                                                    if(which == 0){
                                                        //remove admin clicked
                                                        makeAdmin(user);

                                                    }else if(which == 1){
                                                        //remove user clicked
                                                        removeParticipant(user);

                                                    }else{
                                                        if(!user.getUSER_ID().equals(firebaseUser.getUid())){
                                                            Intent intent = new Intent(context, ViewProfileActivity.class);
                                                            intent.putExtra("uid", user.getUSER_ID());
                                                            context.startActivity(intent);
                                                        }
                                                    }

                                                }
                                            }).show();
                                        }
                                    }else if(myGroupRole.equals("participant")){
                                        if(!user.getUSER_ID().equals(firebaseUser.getUid())){
                                            Intent intent = new Intent(context, ViewProfileActivity.class);
                                            intent.putExtra("uid", user.getUSER_ID());
                                            context.startActivity(intent);
                                        }
                                    }

                                }else{//user doesn't exist/not participant : add
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Add Participant")
                                            .setMessage("Add this user in this group")
                                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    addParticipant(user);
                                                }
                                            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

    }

    private void addParticipant(User user) {
        //setup user data

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("userID", user.getUSER_ID());
        hashMap.put("groupRole", "participant");
        hashMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
        hashMap.put("groupID", groupID);

        groupReference.child(groupID).child("Participants").child(user.getUSER_ID()).setValue(hashMap)
                //ref.push().setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Added Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeAdmin(User user) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("groupRole", "admin");

        groupReference.child(groupID).child("Participants").child(user.getUSER_ID()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, user.getUsername() + " is now an admin", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeParticipant(User user) {
        groupReference.child(groupID).child("Participants").child(user.getUSER_ID()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, user.getUsername() + " has been removed from the group", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeAdmin(User user) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("groupRole", "participant");

        groupReference.child(groupID).child("Participants").child(user.getUSER_ID()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, user.getUsername() + " admin rights revoked", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkGroupAttendance(User user, ViewHolder holder) {
        groupReference.child(groupID).child("Participants").child(user.getUSER_ID())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    GroupsModel groupsModel = snapshot.getValue(GroupsModel.class);

                    assert groupsModel != null;
                    if (!groupsModel.getGroupRole().equals("participant")) {
                        holder.statusTV.setVisibility(View.VISIBLE);
                        holder.statusTV.setText(groupsModel.getGroupRole());
                    }
                }else{

                    holder.statusTV.setVisibility(View.VISIBLE);
                    holder.statusTV.setText("ADD MEMBER");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getContactDetails(User user, ViewHolder holder) {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user1 = ds.getValue(User.class);

                    assert user1 != null;
                    if (user1.getUSER_ID().equals(user.getUSER_ID())){
                        holder.contactName.setText(user1.getUsername());

                        try{
                            Picasso.get().load(user1.getImageURL()).into(holder.contactProPic);
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
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView contactName, statusTV;
        CircleImageView contactProPic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contactName);
            contactProPic = itemView.findViewById(R.id.contactProPic);
            statusTV = itemView.findViewById(R.id.statusTV);

        }
    }
}
