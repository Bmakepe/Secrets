package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makepe.blackout.GettingStarted.Adapters.ImageAdapter;
import com.makepe.blackout.GettingStarted.Adapters.MessageAdapter;
import com.makepe.blackout.GettingStarted.Models.Chat;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.LocationServices;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class  ChatActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView username, onlineStatusTV;
    private ImageButton voiceBTN, attachFiles;
    private RecyclerView chatRecycler, messageMediaRecycler;
    private LinearLayout chatNameLayout; 
    private Toolbar messageToolbar;

    private EditText myMessage;
    private GetTimeAgo getTimeAgo;

    private View rootView;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, chatReference;
    private StorageReference chatImageReference;

    private List<Chat> mChat;
    private ArrayList<String> mediaUriList, mediaIdList;

    private Intent intent;
    private String hisImage, receiverID, textMessage, chatID;

    private ContactsList contactsList;
    private List<ContactsModel> phoneBook;

    //for checking if user has seen message or not
    private ValueEventListener seenListener;
    private DatabaseReference userRefForSeen;

    public int PICK_IMAGE_INTENT = 100, totalMediaUploaded = 0;
    private ImageAdapter imageAdapter;

    private ProgressDialog messageDialog;

    //for sending my location
    private LocationServices locationServices;


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
        messageMediaRecycler = findViewById(R.id.messageMediaRecycler);

        intent = getIntent();
        receiverID = intent.getStringExtra("userid");
        getTimeAgo = new GetTimeAgo();

        mChat = new ArrayList<>();
        mediaUriList = new ArrayList<>();
        mediaIdList = new ArrayList<>();
        phoneBook = new ArrayList<>();
        contactsList = new ContactsList(phoneBook, ChatActivity.this);

        locationServices = new LocationServices(ChatActivity.this, receiverID);
        messageDialog = new ProgressDialog(this);
        messageDialog.setMessage("Loading");

        chatRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecycler.setLayoutManager(layoutManager);

        messageMediaRecycler.setHasFixedSize(true);
        messageMediaRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(ChatActivity.this, mediaUriList);
        messageMediaRecycler.setAdapter(imageAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        chatReference = FirebaseDatabase.getInstance().getReference("Chats");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        chatImageReference = FirebaseStorage.getInstance().getReference().child("chatImages");

        myMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(charSequence.length() != 0){
                    voiceBTN.setImageResource(R.drawable.ic_send_black_24dp);
                }else{
                    voiceBTN.setImageResource(R.drawable.ic_mic_black_24dp);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        getUserDetails();
        readMessages();
        seenMessage();
        
        attachFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu popupMenu = new PopupMenu(ChatActivity.this, attachFiles, Gravity.END);
                popupMenu.getMenu().add(Menu.NONE, 0, 0, "Send Media");
                popupMenu.getMenu().add(Menu.NONE, 1, 0, "Send Location");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()){
                            case 0:
                                openGallery();
                                break;

                            case 1:
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
            textMessage = myMessage.getText().toString();

            if (TextUtils.isEmpty(textMessage)){
                Toast.makeText(this, "You will be able to record a voice note", Toast.LENGTH_SHORT).show();
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void openGallery() {
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
                        if (user.getUSER_ID().equals(receiverID)){

                            for(ContactsModel cm : myContacts){
                                if(cm.getNumber().equals(user.getNumber())){
                                    username.setText(cm.getUsername());
                                }else{
                                    username.setText(user.getUsername());
                                }
                            }

                            if(user.getOnlineStatus().equals("online")){
                                onlineStatusTV.setText(user.getOnlineStatus());
                                onlineStatusTV.setVisibility(View.VISIBLE);
                            }else{
                                try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                                    String pTime = getTimeAgo.getTimeAgo(Long.parseLong(user.getOnlineStatus()), ChatActivity.this);
                                    onlineStatusTV.setVisibility(View.VISIBLE);
                                    onlineStatusTV.setText("Last seen: " + pTime);
                                }catch (NumberFormatException n){
                                    Toast.makeText(ChatActivity.this, "Could not format time", Toast.LENGTH_SHORT).show();
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
                        if(chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(receiverID)){
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

    public void sendMessage(String choice) {

        chatID = chatReference.push().getKey();

        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("chatID", chatID);
        messageMap.put("sender", firebaseUser.getUid());
        messageMap.put("receiver", receiverID);
        messageMap.put("isSeen", false);
        messageMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));

        switch (choice){

            case "alert":
                messageMap.put("message", "Haibooo!!!");
                messageMap.put("message_type", "text");
                break;

            case "noAudio":
                if (mediaUriList.isEmpty()) {
                    messageMap.put("message", textMessage);
                    messageMap.put("message_type", "text");
                }else{
                    messageMap.put("message", textMessage);
                    messageMap.put("message_type", "mediaTextMessage");
                }
                break;

            default:
                Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();

        }

        updateDatabase(messageMap);
    }

    private void updateDatabase(HashMap<String, Object> messageMap) {

        chatReference.child(chatID).setValue(messageMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        myMessage.setText("");
                        myMessage.setHint("Write Your Message");

                        if (!mediaUriList.isEmpty())
                            uploadImages();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        //create chatlist node/child in firebase database
        final DatabaseReference senderReference = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(firebaseUser.getUid())
                .child(receiverID);
        senderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    senderReference.child("id").setValue(receiverID);
                    senderReference.child("timeStamp").setValue(String.valueOf(System.currentTimeMillis()));
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
                    receiverReference.child("id").setValue(firebaseUser.getUid());
                    senderReference.child("timeStamp").setValue(String.valueOf(System.currentTimeMillis()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadImages() {

        messageDialog.show();
        HashMap<String, Object> imageMap = new HashMap<>();

        for(String mediaUri : mediaUriList){

            String mediaId = chatReference.child("media").push().getKey();
            mediaIdList.add(mediaId);

            final StorageReference filePath = chatImageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(Uri.parse(mediaUri)));

            UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageMap.put("image" + totalMediaUploaded, uri.toString());

                            totalMediaUploaded++;

                            if (totalMediaUploaded == mediaUriList.size()){
                                chatReference.child(chatID).child("images").updateChildren(imageMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                if (!mediaUriList.isEmpty() && !mediaIdList.isEmpty()){
                                                    mediaUriList.clear();
                                                    mediaIdList.clear();
                                                    totalMediaUploaded = 0;
                                                    messageDialog.dismiss();
                                                    imageAdapter.notifyDataSetChanged();
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

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void readMessages(){
        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);

                    assert chat != null;
                    if(chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(receiverID) ||
                            chat.getReceiver().equals(receiverID) && chat.getSender().equals(firebaseUser.getUid())){
                        mChat.add(chat);
                    }
                }
                chatRecycler.setAdapter(new MessageAdapter(ChatActivity.this, mChat, hisImage));
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

        if (resultCode == RESULT_OK){
            if (requestCode == PICK_IMAGE_INTENT){
                if (data.getClipData() == null){
                    mediaUriList.add(data.getData().toString());
                }else{
                    for (int i = 0; i < data.getClipData().getItemCount(); i++){
                        mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                }
                voiceBTN.setImageResource(R.drawable.ic_send_black_24dp);

                imageAdapter.notifyDataSetChanged();
            }
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

            default:
                Toast.makeText(this, "Unknown Menu Selection", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
