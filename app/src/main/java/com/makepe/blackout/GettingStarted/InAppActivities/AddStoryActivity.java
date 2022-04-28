package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.makepe.blackout.GettingStarted.MainActivity;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.Movement;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioRecorder;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.LocationServices;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddStoryActivity extends AppCompatActivity {

    private Uri mImageUri;
    private String myUri = "", userID, storyID;
    private StorageTask storageTask;
    private StorageReference storageReference, audioReference;
    private DatabaseReference userReference, storyReference, movementReference;

    private CircleImageView storyProPic;
    private ImageView storyPic;
    private TextView storyUser, storyFAB, locationTV;
    private EditText storyCaption;
    private RelativeLayout locationArea;
    private TextInputLayout storyCaptionSection;
    private LocationServices locationServices;
    private ProgressDialog pd;

    //for recording audio status
    private AudioRecorder audioRecorder;
    private LottieAnimationView lavPlaying;
    private ImageView voicePlayBTN, deleteAudioBTN;
    private TextView seekTimer;
    private RelativeLayout playAudioArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        Toolbar movementToolbar = findViewById(R.id.add_story_toolbar);
        setSupportActionBar(movementToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storyProPic = findViewById(R.id.addStoryProPic);
        storyPic = findViewById(R.id.storyPic);
        storyCaption = findViewById(R.id.storyCaptionET);
        storyUser = findViewById(R.id.addStoryUsername);
        storyFAB = findViewById(R.id.postFAB);
        locationTV = findViewById(R.id.locationCheckIn);
        locationArea = findViewById(R.id.postLocationBTN);
        storyCaptionSection = findViewById(R.id.storyCaptionSection);

        playAudioArea = findViewById(R.id.playAudioArea);
        voicePlayBTN = findViewById(R.id.post_playVoiceIcon);
        seekTimer = findViewById(R.id.seekTimer);
        lavPlaying = findViewById(R.id.lav_playing);
        deleteAudioBTN = findViewById(R.id.recordingDeleteBTN);

        userID = getIntent().getStringExtra("userid");

        storageReference = FirebaseStorage.getInstance().getReference("Story");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        storyReference = FirebaseDatabase.getInstance().getReference("Story");
        movementReference = FirebaseDatabase.getInstance().getReference("Movements");
        audioReference = FirebaseStorage.getInstance().getReference();

        locationServices = new LocationServices(locationTV, AddStoryActivity.this);
        audioRecorder = new AudioRecorder(lavPlaying, AddStoryActivity.this,
                AddStoryActivity.this, playAudioArea, voicePlayBTN, seekTimer);


        storyID = storyReference.push().getKey();

        getUserDetails();

        CropImage.activity()
                .setAspectRatio(9,16)
                .start(AddStoryActivity.this);

        storyCaption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0)
                    storyFAB.setText("Post");
                else
                    storyFAB.setText("Record");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        storyFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (storyFAB.getText().toString().trim().equals("Record")) {
                    if (audioRecorder.checkRecordingPermission()){
                        storyCaptionSection.setVisibility(View.GONE);
                        if (!audioRecorder.isRecording()){
                            storyFAB.setText("Stop");
                            audioRecorder.startRecording();
                        }
                    }else {
                        audioRecorder.requestRecordingPermission();
                    }
                }else if (storyFAB.getText().toString().trim().equals("Stop")){

                    storyFAB.setText("Post");
                    audioRecorder.stopRecording();

                }else if (storyFAB.getText().toString().trim().equals("Post")){

                    pd = new ProgressDialog(AddStoryActivity.this);
                    pd.setMessage("Updating Story");

                    if (!TextUtils.isEmpty(storyCaption.getText().toString()))
                        publishStory();
                    else if (audioRecorder.getRecordingFilePath() != null)
                        publishAudioStory();
                }
            }
        });

        voicePlayBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!audioRecorder.isPlaying()){
                    audioRecorder.startPlayingRecording();
                }else{
                    audioRecorder.stopPlayingAudio();
                }
            }
        });

        deleteAudioBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //audioRecorder.deleteRecording();
                Toast.makeText(AddStoryActivity.this, "Recording will be deleted", Toast.LENGTH_SHORT).show();
            }
        });

        locationArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationServices.getMyLocation();
            }
        });

    }

    private void publishAudioStory() {
        pd.show();

        StorageReference audioPath = audioReference.child("StatusAudios").child(storyID + ".3gp");
        Uri audioUri = Uri.fromFile(new File(audioRecorder.getRecordingFilePath()));

        StorageTask<UploadTask.TaskSnapshot> audioTask = audioPath.putFile(audioUri);

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
                    String audioLink = audioDownloadLink.toString();

                    if (mImageUri != null){
                        final StorageReference imageReference = storageReference.child(System.currentTimeMillis()
                                + "." + getFileExtension(mImageUri));

                        storageTask = imageReference.putFile(mImageUri);
                        storageTask.continueWithTask(new Continuation() {
                            @Override
                            public Object then(@NonNull Task task) throws Exception {
                                if (!task.isSuccessful())
                                    throw task.getException();
                                return imageReference.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){
                                    Uri downloadUri = task.getResult();
                                    myUri  = downloadUri.toString();

                                    long timeEnd = System.currentTimeMillis() + 86400000; //1 day

                                    HashMap<String, Object> hashMap = new HashMap<>();

                                    hashMap.put("storyImage", myUri);
                                    hashMap.put("timeStart", ServerValue.TIMESTAMP);
                                    hashMap.put("timeEnd", timeEnd);
                                    hashMap.put("storyID", storyID);
                                    hashMap.put("userID", userID);
                                    hashMap.put("storyTimeStamp", String.valueOf(System.currentTimeMillis()));
                                    hashMap.put("storyCaption", storyCaption.getText().toString());
                                    hashMap.put("storyType", "audioStory");
                                    hashMap.put("storyAudioUrl", audioLink);

                                    if (!locationTV.getText().toString().equals("No Location")) {
                                        hashMap.put("latitude", locationServices.getLatitude());
                                        hashMap.put("longitude", locationServices.getLongitude());
                                    }

                                    storyReference.child(userID).child(storyID).setValue(hashMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    storyFAB.setVisibility(View.VISIBLE);
                                                    pd.dismiss();

                                                    finish();
                                                }
                                            });
                                }
                            }
                        });
                    }
                }
            }
        });

    }

    private void getUserDetails() {
        List<ContactsModel> phoneBook = new ArrayList<>();
        ContactsList contactsList = new ContactsList(phoneBook, AddStoryActivity.this);
        contactsList.readContacts();
        final List<ContactsModel> myContacts = contactsList.getContactsList();

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUSER_ID().equals(userID)){

                        storyUser.setText(user.getUsername());

                        try{
                            Picasso.get().load(user.getImageURL()).into(storyProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(storyProPic);
                        }
                    }else{
                        movementReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot data : snapshot.getChildren()){
                                    Movement movement = data.getValue(Movement.class);

                                    if (movement.getMovementID().equals(userID)){
                                        storyUser.setText(movement.getMovementName());

                                        try{
                                            Picasso.get().load(movement.getMovementProPic()).into(storyProPic);
                                        }catch (NullPointerException e){
                                            Picasso.get().load(R.drawable.default_profile_display_pic).into(storyProPic);
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
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void publishStory(){
        pd.show();

        if(mImageUri != null){
            final StorageReference imageReference = storageReference.child(System.currentTimeMillis()
            + "." + getFileExtension(mImageUri) );

            storageTask = imageReference.putFile(mImageUri);
            storageTask.continueWithTask(new Continuation(){
                @Override
                public Task<Uri> then(@NonNull Task task) throws Exception{
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUri = downloadUri.toString();

                        long timeEnd = System.currentTimeMillis() + 86400000; //1 day

                        HashMap<String, Object> hashMap = new HashMap<>();

                        hashMap.put("storyImage", myUri);
                        hashMap.put("timeStart", ServerValue.TIMESTAMP);
                        hashMap.put("timeEnd", timeEnd);
                        hashMap.put("storyID", storyID);
                        hashMap.put("userID", userID);
                        hashMap.put("storyTimeStamp", String.valueOf(System.currentTimeMillis()));
                        hashMap.put("storyCaption", storyCaption.getText().toString());
                        hashMap.put("storyType", "textStory");
                        hashMap.put("storyAudioUrl", "");

                        if (!locationTV.getText().toString().equals("No Location")) {
                            hashMap.put("latitude", locationServices.getLatitude());
                            hashMap.put("longitude", locationServices.getLongitude());
                        }

                        storyReference.child(userID).child(storyID).setValue(hashMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        storyFAB.setVisibility(View.VISIBLE);
                                        pd.dismiss();

                                        finish();
                                    }
                                });
                    }else{
                        Toast.makeText(AddStoryActivity.this, "failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddStoryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();

            storyPic.setImageURI(mImageUri);
        }else {
            Toast.makeText(this, "Something gone wrong", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddStoryActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == audioRecorder.REQUEST_AUDIO_PERMISSION){
            if (grantResults.length > 0){
                boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if (permissionToRecord){
                    Toast.makeText(this, "Recording permission granted", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "Recording permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_post_menu, menu);

        menu.findItem(R.id.uploadImageItems).setVisible(false);
        menu.findItem(R.id.uploadVideosItem).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.tagFriendsItem:
                Toast.makeText(this, "You will be able to tag friends", Toast.LENGTH_SHORT).show();
                break;

            default:
                Toast.makeText(this, "unknown menu selection", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

}
