package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.GroupChatAdapter;
import com.makepe.blackout.GettingStarted.Models.GroupChat;
import com.makepe.blackout.GettingStarted.Models.GroupsModel;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {

    private String groupID;
    private Intent intent;

    private CircleImageView groupProPic;
    private TextView groupNameTV;

    private DatabaseReference groupReference, groupChatsReference;
    private FirebaseUser firebaseUser;

    private String myGroupRole, groupMessage;

    private RecyclerView groupChatsRecycler;
    private EditText groupChatET;
    private ImageButton groupSendBTN, attachMediaBTN;

    private List<GroupChat> groupChats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_group_chat);

        Toolbar newGroupToolbar = findViewById(R.id.groupChatToolbar);
        setSupportActionBar(newGroupToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupProPic = findViewById(R.id.groupProPic);
        groupNameTV = findViewById(R.id.groupNameTV);
        groupChatsRecycler = findViewById(R.id.groupRecyclerView);
        groupChatET = findViewById(R.id.groupMessageEt);
        groupSendBTN = findViewById(R.id.groupVoiceBTN);
        attachMediaBTN = findViewById(R.id.groupAttachFiles);

        intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        groupChats = new ArrayList<>();

        groupReference = FirebaseDatabase.getInstance().getReference("SecretGroups");
        groupChatsReference = FirebaseDatabase.getInstance().getReference("GroupChats");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getGroupDetails();
        getMyGroupRole();
        readGroupMessages();

        groupChatET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0){
                    groupSendBTN.setImageResource(R.drawable.ic_send_black_24dp);
                }else{
                    groupSendBTN.setImageResource(R.drawable.ic_mic_black_24dp);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        groupChatsRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        groupChatsRecycler.setLayoutManager(layoutManager);

        groupSendBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupMessage = groupChatET.getText().toString().trim();

                if (TextUtils.isEmpty(groupMessage)){
                    groupChatET.setError("Whats Up?");
                    groupChatET.requestFocus();
                }else{
                    sendGroupMessage();
                }
            }
        });

        attachMediaBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final PopupMenu popupMenu = new PopupMenu(GroupChatActivity.this, attachMediaBTN, Gravity.END);
                popupMenu.getMenu().add(Menu.NONE, 0, 0, "Send Images");
                popupMenu.getMenu().add(Menu.NONE, 1, 0, "Send Video");
                popupMenu.getMenu().add(Menu.NONE, 2, 0, "Send Documents");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()){
                            case 0:
                                Toast.makeText(GroupChatActivity.this, "You will be able to send images", Toast.LENGTH_SHORT).show();
                                break;

                            case 1:
                                Toast.makeText(GroupChatActivity.this, "You will be able to send videos", Toast.LENGTH_SHORT).show();
                                break;

                            case 2:
                                Toast.makeText(GroupChatActivity.this, "You will be able to send documents", Toast.LENGTH_SHORT).show();
                                break;

                            default:
                                throw new IllegalStateException("Unexpected value " + menuItem.getItemId());
                        }

                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        groupNameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupChatActivity.this, GroupDetailsActivity.class);
                intent.putExtra("groupID", groupID);
                startActivity(intent);
            }
        });

    }

    private void readGroupMessages() {
        groupChatsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChats.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    GroupChat chat = ds.getValue(GroupChat.class);

                    try{
                        if(chat.getGroupID().equals(firebaseUser.getUid()) && chat.getSenderID().equals(groupID) ||
                                chat.getGroupID().equals(groupID) && chat.getSenderID().equals(firebaseUser.getUid())){
                            groupChats.add(chat);
                        }
                    }catch (NullPointerException ignored){}

                    groupChatsRecycler.setAdapter(new GroupChatAdapter(GroupChatActivity.this, groupChats));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendGroupMessage() {

        String chatID = groupChatsReference.push().getKey();

        HashMap<String, Object> groupMap = new HashMap<>();
        groupMap.put("groupID", groupID);
        groupMap.put("chatID", chatID);
        groupMap.put("senderID", firebaseUser.getUid());
        groupMap.put("message", groupMessage);
        groupMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        groupMap.put("media", "noMedia");
        groupMap.put("audio", "noAudio");
        groupMap.put("msg_type", "text");

        groupChatsReference.child(chatID).setValue(groupMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        groupChatET.setText("");
                        groupChatET.setHint("Write Your Message");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void getMyGroupRole() {
        groupReference.child(groupID).child("Participants")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            GroupsModel groups = ds.getValue(GroupsModel.class);

                            assert groups != null;
                            if (groups.getUserID().equals(firebaseUser.getUid())){
                                myGroupRole = groups.getGroupRole();
                            }

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

                    if (group.getGroupID().equals(groupID)){
                        groupNameTV.setText(group.getGroupName());

                        try{
                            Picasso.get().load(group.getGroupProPic()).into(groupProPic);
                        }catch (NullPointerException ignored){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
        getMenuInflater().inflate(R.menu.group_chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.groupDetails:
                Intent intent = new Intent(GroupChatActivity.this, GroupDetailsActivity.class);
                intent.putExtra("groupID", groupID);
                startActivity(intent);
                break;

            case R.id.sendGroupAlert:
                groupMessage = "Haibo!!!";
                sendGroupMessage();
                break;

            default:
                Toast.makeText(this, "Unknown Selection", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}