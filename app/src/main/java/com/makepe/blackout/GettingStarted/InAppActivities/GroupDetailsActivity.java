package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makepe.blackout.GettingStarted.Adapters.GroupContactsAdapter;
import com.makepe.blackout.GettingStarted.Models.GroupsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupDetailsActivity extends AppCompatActivity {

    private ImageView groupCoverPic;
    private CircleImageView groupProfilePic;
    private TextView groupDetailsName, sendMessageBTN, leaveGroupBTN, groupDescription,
            groupMembersCount, groupCreatedDate, groupPrivacy, aboutTV;

    private ProgressBar coverLoader, profilePicLoader;
    private String groupID, myGroupRole, groupIcon, groupCoverIcon;

    private DatabaseReference groupReference, userReference;
    private FirebaseUser firebaseUser;

    private List<User> groupMembers;
    private RecyclerView membersRecycler;

    private GetTimeAgo getTimeAgo;

    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        Toolbar groupDetailsToolbar = findViewById(R.id.groupDetailsToolbar);
        setSupportActionBar(groupDetailsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupCoverPic = findViewById(R.id.groupCoverImage);
        groupProfilePic = findViewById(R.id.groupProfilePicture);
        groupDetailsName = findViewById(R.id.groupDetailsName);
        coverLoader = findViewById(R.id.groupCoverPicLoader);
        profilePicLoader = findViewById(R.id.groupProPicLoader);
        sendMessageBTN = findViewById(R.id.groupSendMessageBTN);
        leaveGroupBTN = findViewById(R.id.leaveGroupBTN);
        groupDescription = findViewById(R.id.groupDescription);
        membersRecycler = findViewById(R.id.groupMembersRecycler);
        groupMembersCount = findViewById(R.id.groupMembersCount);
        groupCreatedDate = findViewById(R.id.groupTimeCreated);
        groupPrivacy = findViewById(R.id.groupPrivacy);
        aboutTV = findViewById(R.id.groupAboutTV);

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");

        groupReference = FirebaseDatabase.getInstance().getReference("SecretGroups");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        membersRecycler.hasFixedSize();
        membersRecycler.setLayoutManager(new LinearLayoutManager(this));

        groupMembers = new ArrayList<>();

        getTimeAgo = new GetTimeAgo();

        getGroupDetails();
        getGroupMembers();
        getMyDetails();
        getMembersCount();

        sendMessageBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(GroupDetailsActivity.this, GroupChatActivity.class);
                intent1.putExtra("groupID", groupID);
                startActivity(intent1);
            }
        });

        leaveGroupBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String dialogTitle = "", dialogDescription = "", positiveButtonTitle = "";

                if (myGroupRole.equals("creator")){
                    dialogTitle = "Delete group";
                    dialogDescription = "Are You Sure You Want To Delete Group Permanently?";
                    positiveButtonTitle = "Delete";
                }else{
                    dialogTitle = "Leave Group";
                    dialogDescription = "Are You Sure You Want To Leave Group Permanently?";
                    positiveButtonTitle = "Leave";
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(GroupDetailsActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (myGroupRole.equals("creator")){
                                    deleteGroup();
                                }else{
                                    leaveGroup();
                                }
                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();

                Toast.makeText(GroupDetailsActivity.this, "You will be able to leave the group", Toast.LENGTH_SHORT).show();
            }
        });

        groupProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(GroupDetailsActivity.this, FullScreenImageActivity.class);
                intent1.putExtra("itemID", groupID);
                intent1.putExtra("reason", "groupProPic");
                startActivity(intent1);

            }
        });

        groupCoverPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(GroupDetailsActivity.this, FullScreenImageActivity.class);
                intent1.putExtra("itemID", groupID);
                intent1.putExtra("reason", "groupCoverPic");
                startActivity(intent1);

            }
        });

    }

    private void getMembersCount() {
        groupReference.child(groupID).child("Participants")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                            groupMembersCount.setText("" + snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getMyDetails() {
        groupReference.child(groupID).child("Participants")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            GroupsModel groupsModel = dataSnapshot.getValue(GroupsModel.class);

                            assert groupsModel != null;
                            if (groupsModel.getUserID().equals(firebaseUser.getUid())){
                                myGroupRole = groupsModel.getGroupRole();

                                if (myGroupRole.equals("creator")){
                                    leaveGroupBTN.setText("Delete Group");
                                }else{
                                    leaveGroupBTN.setText("Exit Group");
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getGroupMembers() {
        groupReference.child(groupID).child("Participants")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        groupMembers.clear();

                        for (DataSnapshot ds : snapshot.getChildren()){
                            GroupsModel model = ds.getValue(GroupsModel.class);

                            userReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()){
                                        User user = ds.getValue(User.class);

                                        assert user != null;
                                        assert model != null;
                                        if (user.getUSER_ID().equals(model.getUserID())){
                                            groupMembers.add(user);
                                        }

                                        membersRecycler.setAdapter(new GroupContactsAdapter(GroupDetailsActivity.this, groupMembers, groupID, myGroupRole));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getGroupDetails() {
        groupReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    GroupsModel group = ds.getValue(GroupsModel.class);

                    assert group != null;
                    if (group.getGroupID().equals(groupID)){

                        groupIcon = group.getGroupProPic();
                        groupCoverIcon = group.getGroupCoverPic();

                        groupDetailsName.setText(group.getGroupName());
                        groupDescription.setText(group.getGroupPurpose());
                        groupPrivacy.setText(group.getGroupPrivacy() + " group");
                        aboutTV.setText("About " + group.getGroupName());

                        String timeStamp = getTimeAgo.getTimeAgo(Long.parseLong(group.getTimeStamp()), GroupDetailsActivity.this);
                        groupCreatedDate.setText("Created: " + timeStamp);

                        try{
                            Picasso.get().load(group.getGroupCoverPic()).into(groupCoverPic);
                            Picasso.get().load(group.getGroupProPic()).into(groupProfilePic);

                            coverLoader.setVisibility(View.GONE);
                            profilePicLoader.setVisibility(View.GONE);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(groupCoverPic);
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(groupProfilePic);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void leaveGroup() {
        groupReference.child(groupID).child("Participants").child(firebaseUser.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupDetailsActivity.this, "Group Left Successfully", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(GroupDetailsActivity.this, MainActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteGroup() {
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(groupIcon);
        picRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                StorageReference coverPicRef = FirebaseStorage.getInstance().getReferenceFromUrl(groupCoverIcon);
                coverPicRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        groupReference.child(groupID)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(GroupDetailsActivity.this, "Group Deleted Successfully", Toast.LENGTH_SHORT).show();
                                        //startActivity(new Intent(GroupDetailsActivity.this, MainActivity.class));
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(GroupDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_details_menu, menu);

        if(myGroupRole.equals("participant")){
            menu.findItem(R.id.addGroupMembers).setVisible(false);
            menu.findItem(R.id.editGroup).setVisible(false);
        }

        if (myGroupRole.equals("creator") || myGroupRole.equals("admin"))
            menu.findItem(R.id.reportGroup).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.addGroupMembers:
                Intent manageIntent = new Intent(GroupDetailsActivity.this, AddGroupMembersActivity.class);
                manageIntent.putExtra("groupID", groupID);
                manageIntent.putExtra("groupRole", myGroupRole);
                startActivity(manageIntent);
                return true;
            case R.id.editGroup:
                Intent intent = new Intent(GroupDetailsActivity.this, EditGroupDetailsActivity.class);
                intent.putExtra("groupID", groupID);
                startActivity(intent);
                return true;
            case R.id.reportGroup:
                Intent intent1  = new Intent(GroupDetailsActivity.this, ReportActivity.class);
                intent1.putExtra("reported", groupID);
                startActivity(intent1);
                return true;

        }
        return false;
    }
}