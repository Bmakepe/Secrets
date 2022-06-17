package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.makepe.blackout.GettingStarted.Adapters.GroupChatAdapter;
import com.makepe.blackout.GettingStarted.Models.GroupChat;
import com.makepe.blackout.GettingStarted.Models.GroupsModel;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioRecorder;
import com.makepe.blackout.GettingStarted.OtherClasses.LocationServices;
import com.makepe.blackout.GettingStarted.OtherClasses.UploadFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.io.File;
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
    private StorageReference groupImageReference, audioReference;

    private String myGroupRole, groupMessage, chatID, videoURL;

    private RecyclerView groupChatsRecycler;
    private EditText groupChatET;
    private ImageButton groupSendBTN, attachMediaBTN;

    private List<GroupChat> groupChats;
    private ArrayList<String> mediaUriList, mediaIdList;

    public int PICK_IMAGE_INTENT = 100, totalMediaUploaded = 0;
    public static final int PICK_VIDEO_REQUEST = 200;
    private Uri videoURI;

    //for sending my location
    private LocationServices locationServices;

    private UploadFunctions uploadFunctions;

    private ProgressDialog messageDialog;

    //for sending voice notes
    private AudioRecorder audioRecorder;
    private LottieAnimationView lavPlaying;
    private ImageView voicePlayBTN, deleteAudioBTN;
    private TextView seekTimer;
    private RelativeLayout playAudioArea;

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

        //for recording the audio
        playAudioArea = findViewById(R.id.playAudioArea);
        voicePlayBTN = findViewById(R.id.post_playVoiceIcon);
        seekTimer = findViewById(R.id.seekTimer);
        lavPlaying = findViewById(R.id.lav_playing);
        deleteAudioBTN = findViewById(R.id.recordingDeleteBTN);

        intent = getIntent();
        groupID = intent.getStringExtra("groupID");

        groupChats = new ArrayList<>();
        mediaUriList = new ArrayList<>();
        mediaIdList = new ArrayList<>();

        uploadFunctions = new UploadFunctions(GroupChatActivity.this);

        audioRecorder = new AudioRecorder(lavPlaying, GroupChatActivity.this,
                GroupChatActivity.this, playAudioArea, voicePlayBTN, seekTimer);

        messageDialog = new ProgressDialog(this);
        messageDialog.setMessage("Loading...");

        audioReference = FirebaseStorage.getInstance().getReference("group_voice_notes");
        groupReference = FirebaseDatabase.getInstance().getReference("SecretGroups");
        groupChatsReference = FirebaseDatabase.getInstance().getReference("GroupChats");
        groupImageReference = FirebaseStorage.getInstance().getReference().child("groupChatImages");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        locationServices = new LocationServices(GroupChatActivity.this, groupID);

        getGroupDetails();
        getMyGroupRole();
        readGroupMessages();

        groupSendBTN.setTag("notRecording");

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

                if (TextUtils.isEmpty(groupChatET.getText().toString()) && groupSendBTN.getTag().equals("notRecording")){
                    if (audioRecorder.checkRecordingPermission()){
                        groupChatET.setVisibility(View.GONE);
                        groupSendBTN.setImageResource(R.drawable.ic_send_black_24dp);

                        if (!audioRecorder.isRecording){
                            audioRecorder.startRecording();
                            groupSendBTN.setTag("Recording");
                        }

                    }else{
                        audioRecorder.requestRecordingPermission();
                    }
                }else if(groupSendBTN.getTag().equals("Recording")){
                    audioRecorder.stopRecording();

                    if (audioRecorder.getRecordingFilePath() != null)
                        sendGroupAudioMessage();
                }else{
                    sendGroupMessage("text");
                }
            }
        });

        attachMediaBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final PopupMenu popupMenu = new PopupMenu(GroupChatActivity.this, attachMediaBTN, Gravity.END);
                popupMenu.getMenu().add(Menu.NONE, 0, 0, "Send Images");
                popupMenu.getMenu().add(Menu.NONE, 1, 0, "Send Video");
                popupMenu.getMenu().add(Menu.NONE, 2, 0, "Send Location");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()){
                            case 0:
                                openImageGallery();
                                break;

                            case 1:
                                openVideoGallery();
                                break;

                            case 2:
                                locationServices.sendMyGroupLocation();
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

    private void openVideoGallery() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    private void openImageGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryIntent.setAction(galleryIntent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture(s)"), PICK_IMAGE_INTENT);
    }

    private void readGroupMessages() {
        groupChatsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChats.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    GroupChat chat = ds.getValue(GroupChat.class);

                    assert chat != null;
                    if (chat.getGroupID().equals(groupID))
                        groupChats.add(chat);

                }
                groupChatsRecycler.setAdapter(new GroupChatAdapter(GroupChatActivity.this, groupChats));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendGroupMessage(String msg_type) {

        chatID = groupChatsReference.push().getKey();

        HashMap<String, Object> groupMap = new HashMap<>();
        groupMap.put("groupID", groupID);
        groupMap.put("chatID", chatID);
        groupMap.put("senderID", firebaseUser.getUid());
        groupMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        groupMap.put("msg_type", msg_type);

        if (!TextUtils.isEmpty(groupMessage))
            groupMap.put("message", groupMessage);

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

    private void sendGroupAudioMessage() {
        messageDialog.show();

        StorageReference audioPath = audioReference.child(groupID).child(chatID + ".3gp");
        Uri audioUrl = Uri.fromFile(new File(audioRecorder.getRecordingFilePath()));

        StorageTask<UploadTask.TaskSnapshot> audioTask = audioPath.putFile(audioUrl);

        audioTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful())
                    throw task.getException();
                return audioPath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri audioDownloadLink = task.getResult();

                    HashMap<String, Object> audioMap = new HashMap<>();

                    chatID = groupChatsReference.push().getKey();

                    HashMap<String, Object> groupMap = new HashMap<>();
                    groupMap.put("groupID", groupID);
                    groupMap.put("chatID", chatID);
                    groupMap.put("senderID", firebaseUser.getUid());
                    groupMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
                    groupMap.put("msg_type", "audioMessage");
                    audioMap.put("audio", audioDownloadLink.toString());

                    groupChatsReference.child(chatID).setValue(audioMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    groupSendBTN.setTag("notRecording");
                                    messageDialog.dismiss();
                                    groupSendBTN.setImageResource(R.drawable.ic_mic_black_24dp);
                                }
                            });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatActivity.this, "Can not upload your voice note at this moment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImages() {
        messageDialog.show();

        chatID = groupChatsReference.push().getKey();

        HashMap<String, Object> chatMap = new HashMap<>();
        chatMap.put("groupID", groupID);
        chatMap.put("chatID", chatID);
        chatMap.put("senderID", firebaseUser.getUid());
        chatMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        chatMap.put("msg_type", "imageMessage");

        groupChatsReference.child(chatID).setValue(chatMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                HashMap<String, Object> groupImageMap = new HashMap<>();

                for (String mediaUri : mediaUriList){

                    String mediaID = groupChatsReference.child("images").push().getKey();
                    mediaIdList.add(mediaID);

                    final StorageReference filePath = groupImageReference.child(System.currentTimeMillis()
                            + "." + uploadFunctions.getFileExtension(Uri.parse(mediaUri)));

                    UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    groupImageMap.put("image" + totalMediaUploaded, uri.toString());

                                    totalMediaUploaded++;

                                    if (totalMediaUploaded == mediaUriList.size()){
                                        groupChatsReference.child(chatID).child("images").updateChildren(groupImageMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        if (!mediaUriList.isEmpty() && !mediaIdList.isEmpty()){
                                                            mediaUriList.clear();
                                                            mediaIdList.clear();
                                                            totalMediaUploaded = 0;
                                                            messageDialog.dismiss();
                                                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                    }
                                                });
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });

    }

    private void sendVideoMessage() {
        messageDialog.show();

        long systemMillis = System.currentTimeMillis();

        StorageReference videoReference = FirebaseStorage.getInstance().getReference()
                .child("Group_Chat_Videos")
                .child(systemMillis + "." + uploadFunctions.getFileExtension(videoURI));

        UploadTask uploadTask = videoReference.putFile(videoURI);

        uploadTask.addOnProgressListener(taskSnapshot ->{
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            Log.d("PROGRESS", "Upload is " + progress + "% done");
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
            }
        });

        Task<Uri> urlTask = uploadTask.continueWithTask(task ->{
            if (!task.isSuccessful()){
                throw task.getException();
            }
            return videoReference.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri videoDownloadLink = task.getResult();
                videoURL = videoDownloadLink.toString();
                Log.i("downloadTag", videoURL);

                chatID = groupChatsReference.push().getKey();

                HashMap<String, Object> videoMap = new HashMap<>();

                videoMap.put("groupID", groupID);
                videoMap.put("chatID", chatID);
                videoMap.put("senderID", firebaseUser.getUid());
                videoMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
                videoMap.put("videoURL", videoURL);
                videoMap.put("msg_type", "videoMessage");

                groupChatsReference.child(chatID).setValue(videoMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        messageDialog.dismiss();
                        videoURL = "";
                        videoURI = null;
                    }
                });
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_INTENT && resultCode == RESULT_OK){

            if (data.getClipData() == null){
                mediaUriList.add(data.getData().toString());
            }else{
                for (int i = 0; i < data.getClipData().getItemCount(); i++){
                    mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                }
            }

            if (!mediaUriList.isEmpty()) {
                uploadImages();
            }
        }else if (requestCode == PICK_VIDEO_REQUEST && resultCode == -1
                && data != null & data.getData() != null){

            videoURI = data.getData();

            if (videoURI != null)
                sendVideoMessage();
        }
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
                sendGroupMessage("text");
                break;

            case R.id.groupSearchBTN:
                Toast.makeText(this, "search group chats", Toast.LENGTH_SHORT).show();
                break;

        }
        return false;
    }

}