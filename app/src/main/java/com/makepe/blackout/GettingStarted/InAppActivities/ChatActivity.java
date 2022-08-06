package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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
import com.makepe.blackout.GettingStarted.Adapters.MessageAdapter;
import com.makepe.blackout.GettingStarted.Models.Chat;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioRecorder;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.LocationServices;
import com.makepe.blackout.GettingStarted.OtherClasses.UploadFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView username, onlineStatusTV;
    private ImageButton voiceBTN, attachFiles;
    private RecyclerView chatRecycler;
    private LinearLayout chatNameLayout; 
    private Toolbar messageToolbar;

    private EditText myMessage;
    private GetTimeAgo getTimeAgo;

    private View rootView;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, chatReference;
    private StorageReference chatImageReference, audioReference;

    private List<Chat> mChat;
    private ArrayList<String> mediaUriList, mediaIdList;

    private Intent intent;
    private String hisImage, receiverID, chatID;

    private ContactsList contactsList;
    private List<ContactsModel> phoneBook;

    //for checking if user has seen message or not
    private ValueEventListener seenListener;
    private DatabaseReference userRefForSeen;

    public int PICK_IMAGE_INTENT = 100, totalMediaUploaded = 0;
    public static final int PICK_VIDEO_REQUEST = 200;

    private ProgressDialog messageDialog;

    //for sending my location
    private LocationServices locationServices;

    private UploadFunctions uploadFunctions;

    //for sending video messages
    private Uri videoURI;
    private String videoURL;

    //for sending voice notes
    private AudioRecorder audioRecorder;
    private LottieAnimationView lavPlaying;
    private ImageView voicePlayBTN, deleteAudioBTN;
    private TextView seekTimer;
    private RelativeLayout playAudioArea;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_chat);

        messageToolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(messageToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileImage = findViewById(R.id.profileCIV);
        username = findViewById(R.id.hisNameTV);
        chatRecycler = findViewById(R.id.chatRecyclerView);
        chatNameLayout = findViewById(R.id.chatNameLayout);
        onlineStatusTV = findViewById(R.id.statusTV);
        myMessage = findViewById(R.id.messageEt);
        rootView = findViewById(R.id.root_view);
        voiceBTN = findViewById(R.id.voiceBTN);
        attachFiles = findViewById(R.id.attachFiles);

        //for recording the audio
        playAudioArea = findViewById(R.id.playAudioArea);
        voicePlayBTN = findViewById(R.id.post_playVoiceIcon);
        seekTimer = findViewById(R.id.seekTimer);
        lavPlaying = findViewById(R.id.lav_playing);
        deleteAudioBTN = findViewById(R.id.recordingDeleteBTN);

        intent = getIntent();
        receiverID = intent.getStringExtra("userid");
        getTimeAgo = new GetTimeAgo();
        uploadFunctions = new UploadFunctions(ChatActivity.this);

        mChat = new ArrayList<>();
        mediaUriList = new ArrayList<>();
        mediaIdList = new ArrayList<>();

        audioRecorder = new AudioRecorder(lavPlaying, ChatActivity.this,
                ChatActivity.this, playAudioArea, voicePlayBTN, seekTimer);

        phoneBook = new ArrayList<>();
        contactsList = new ContactsList(phoneBook, ChatActivity.this);

        locationServices = new LocationServices(ChatActivity.this, receiverID);
        messageDialog = new ProgressDialog(this);
        messageDialog.setMessage("Loading...");

        chatRecycler.hasFixedSize();
        chatRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecycler.setLayoutManager(layoutManager);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        chatReference = FirebaseDatabase.getInstance().getReference("Chats");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        chatImageReference = FirebaseStorage.getInstance().getReference().child("chatImages");
        audioReference = FirebaseStorage.getInstance().getReference("voice_notes");

        voiceBTN.setTag("notRecording");

        myMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(charSequence.length() != 0){
                    voiceBTN.setImageResource(R.drawable.ic_send_black_24dp);
                }else{
                    voiceBTN.setImageResource(R.drawable.ic_mic_black_24dp);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        getUserDetails();
        readMessages();
        seenMessage();
        
        attachFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu popupMenu = new PopupMenu(ChatActivity.this, attachFiles, Gravity.END);
                popupMenu.getMenu().add(Menu.NONE, 0, 0, "Send Images");
                popupMenu.getMenu().add(Menu.NONE, 1, 0, "Send Videos");
                popupMenu.getMenu().add(Menu.NONE, 2, 0, "Send Location");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()){
                            case 0:
                                openImagesGallery();
                                break;

                            case 1:
                                openVideoGallery();
                                break;

                            case 2:
                                locationServices.sendMyLocation();
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

        voiceBTN.setOnClickListener(v -> {

            if (TextUtils.isEmpty(myMessage.getText().toString())
                    && voiceBTN.getTag().equals("notRecording")){

                if (audioRecorder.checkRecordingPermission()){
                    myMessage.setVisibility(View.GONE);
                    voiceBTN.setImageResource(R.drawable.ic_send_black_24dp);

                    if (!audioRecorder.isRecording){
                        audioRecorder.startRecording();
                        voiceBTN.setTag("Recording");
                    }

                }else{
                    audioRecorder.requestRecordingPermission();
                }

            }else if (voiceBTN.getTag().equals("Recording")){
                audioRecorder.stopRecording();

                if (audioRecorder.getRecordingFilePath() != null)
                    sendAudioMessage();

            }else{
                sendMessage("noAudio");
            }

        });

        chatNameLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, ViewProfileActivity.class);
            intent.putExtra("uid", receiverID);
            startActivity(intent);
        });

    }

    private void openVideoGallery() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void openImagesGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryIntent.setAction(galleryIntent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture(s)"), PICK_IMAGE_INTENT);
    }

    private void getUserDetails() {
        contactsList.readContacts();
        final List<ContactsModel> myContacts = contactsList.getContactsList();

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);

                        assert user != null;
                        if (user.getUserID().equals(receiverID)){

                            for(ContactsModel cm : myContacts){
                                if(cm.getPhoneNumber().equals(user.getPhoneNumber())){
                                    username.setText(cm.getUsername());
                                }else{
                                    username.setText(user.getUsername());
                                }
                            }

                            try{
                                hisImage = user.getImageURL();
                                Picasso.get().load(hisImage).into(profileImage);
                            }catch (NullPointerException e){
                                Picasso.get().load(R.drawable.default_profile_display_pic).into(profileImage);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    try{
                        if(chat.getReceiverID().equals(firebaseUser.getUid()) && chat.getSenderID().equals(receiverID)){
                            HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                            hasSeenHashMap.put("isSeen", true);
                            ds.getRef().updateChildren(hasSeenHashMap);
                        }
                    }catch (NullPointerException ignored){}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String choice) {

        chatID = chatReference.push().getKey();

        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("chatID", chatID);
        messageMap.put("senderID", firebaseUser.getUid());
        messageMap.put("receiverID", receiverID);
        messageMap.put("isSeen", false);
        messageMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));

        switch (choice){

            case "alert":
                messageMap.put("message", "Haibooo!!!");
                messageMap.put("message_type", "text");
                break;

            case "noAudio":
                messageMap.put("message", myMessage.getText().toString());
                messageMap.put("message_type", "text");
                break;

            default:
                Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();

        }

        chatReference.child(chatID).setValue(messageMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        myMessage.setText("");
                        myMessage.setHint("Write Your Message");

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        updateChatList();
    }

    private void sendAudioMessage() {
        messageDialog.show();
        messageDialog.setCancelable(false);
        chatID = chatReference.push().getKey();

        StorageReference audioPath = audioReference.child(firebaseUser.getUid()).child(chatID + ".3gp");
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

                    HashMap<String, Object> messageMap = new HashMap<>();

                    messageMap.put("chatID", chatID);
                    messageMap.put("senderID", firebaseUser.getUid());
                    messageMap.put("receiverID", receiverID);
                    messageMap.put("isSeen", false);
                    messageMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
                    messageMap.put("audioURL", audioDownloadLink.toString());
                    messageMap.put("message_type", "audioMessage");

                    chatReference.child(chatID).setValue(messageMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (audioRecorder.getRecordingFilePath() != null){
                                        audioRecorder.resetRecorder();
                                        myMessage.setVisibility(View.VISIBLE);
                                    }

                                    voiceBTN.setTag("notRecording");
                                    messageDialog.dismiss();
                                    voiceBTN.setImageResource(R.drawable.ic_mic_black_24dp);

                                }
                            });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this, "Can not upload your voice note at this moment", Toast.LENGTH_SHORT).show();
            }
        });

        updateChatList();
    }

    private void updateChatList() {
        //create chatlist node/child in firebase database

        final DatabaseReference senderReference = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(firebaseUser.getUid())
                .child(receiverID);
        senderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    senderReference.child("userID").setValue(receiverID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference receiverReference = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(receiverID)
                .child(firebaseUser.getUid());
        receiverReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    receiverReference.child("userID").setValue(firebaseUser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadImages() {

        messageDialog.show();
        messageDialog.setCancelable(false);

        chatID = chatReference.push().getKey();

        HashMap<String, Object> imageMap = new HashMap<>();

        imageMap.put("chatID", chatID);
        imageMap.put("senderID", firebaseUser.getUid());
        imageMap.put("receiverID", receiverID);
        imageMap.put("isSeen", false);
        imageMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        imageMap.put("message_type", "imageMessage");

        chatReference.child(chatID).setValue(imageMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                HashMap<String, Object> mediaMap = new HashMap<>();

                for(String mediaUri : mediaUriList){

                    String mediaId = chatReference.child("images").push().getKey();
                    mediaIdList.add(mediaId);

                    final StorageReference filePath = chatImageReference.child(mediaId
                            + "." + uploadFunctions.getFileExtension(Uri.parse(mediaUri)));

                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

                    UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    mediaMap.put("image" + totalMediaUploaded, uri.toString());

                                    totalMediaUploaded++;

                                    if (totalMediaUploaded == mediaUriList.size()){
                                        chatReference.child(chatID).child("images").updateChildren(mediaMap)
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

        updateChatList();

    }

    private void sendVideoMessage() {
        messageDialog.show();
        messageDialog.setCancelable(false);

        long systemMillis = System.currentTimeMillis();

        StorageReference videoReference = FirebaseStorage.getInstance().getReference()
                .child("Chat_Videos")
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
                Toast.makeText(ChatActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
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

                chatID = chatReference.push().getKey();

                HashMap<String, Object> videoMap = new HashMap<>();

                videoMap.put("chatID", chatID);
                videoMap.put("senderID", firebaseUser.getUid());
                videoMap.put("receiverID", receiverID);
                videoMap.put("isSeen", false);
                videoMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
                videoMap.put("message_type", "videoMessage");
                videoMap.put("videoURL", videoURL);

                chatReference.child(chatID).setValue(videoMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                messageDialog.dismiss();
                                videoURI = null;
                                videoURL = "";
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        updateChatList();
    }

    private void readMessages(){
        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);

                    assert chat != null;
                    if(chat.getReceiverID().equals(firebaseUser.getUid()) && chat.getSenderID().equals(receiverID) ||
                            chat.getReceiverID().equals(receiverID) && chat.getSenderID().equals(firebaseUser.getUid())){
                        mChat.add(chat);
                    }
                }
                chatRecycler.setAdapter(new MessageAdapter(ChatActivity.this, mChat));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

            /*Bitmap bmp = null;

            try{
                for (int i = 0; i < mediaUriList.size(); i++){
                    bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(mediaUriList.get(i)));
                }
            }catch (IOException e){
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            byte[] filesInBytes = baos.toByteArray();*/

            if(mediaUriList != null)
                uploadImages();

        }else if (requestCode == PICK_VIDEO_REQUEST && resultCode == -1
                && data != null & data.getData() != null){

            videoURI = data.getData();

            if (videoURI != null)
                sendVideoMessage();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        menu.findItem(R.id.videoCall).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.videoCall:
                Toast.makeText(ChatActivity.this, "Video Call", Toast.LENGTH_SHORT).show();
                break;
            case R.id.voiceCall:
                Intent phoneCallIntent = new Intent(ChatActivity.this, PhoneCallActivity.class);
                phoneCallIntent.putExtra("userID", receiverID);
                startActivity(phoneCallIntent);
                break;
            case R.id.profileView:
                userReference.child(receiverID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Intent intent = new Intent(ChatActivity.this, ViewProfileActivity.class);
                            intent.putExtra("uid", receiverID);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                break;

            case R.id.sendAlert:
                sendMessage("alert");
                break;

            case R.id.chatSearch:
                Toast.makeText(ChatActivity.this, "Search the chat", Toast.LENGTH_SHORT).show();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
