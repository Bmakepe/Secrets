package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.makepe.blackout.GettingStarted.OtherClasses.UploadFunctions;
import com.makepe.blackout.R;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroupActivity extends AppCompatActivity {

    private CircleImageView groupProfilePic;
    private ImageView groupCoverPic, addCoverPic;
    private TextInputEditText setupGroupName, setupGroupPurpose;
    private Button createGroupBTN;
    private RadioGroup groupPrivacyBTN;
    private RadioButton selectedPrivacy;

    private Uri imageUri, coverPicUri;
    private String groupId, groupRole = "creator", privacyLock, choice;
    private StorageTask uploadTask, coverUploadTask;
    private FirebaseUser firebaseUser;
    private DatabaseReference groupReference;
    private StorageReference storageImageReference, storageCoverReference;

    private ProgressDialog progressDialog;
    private UploadFunctions uploadFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        Toolbar newGroupToolbar = findViewById(R.id.newGroupToolbar);
        setSupportActionBar(newGroupToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupProfilePic = findViewById(R.id.groupIcon);
        groupCoverPic = findViewById(R.id.groupCoverPic);
        addCoverPic = findViewById(R.id.addGroupCoverPicBTN);
        setupGroupName = findViewById(R.id.groupName);
        createGroupBTN = findViewById(R.id.addGroupBTN);
        groupPrivacyBTN = findViewById(R.id.groupPrivacyBTN);
        setupGroupPurpose = findViewById(R.id.groupPurpose);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        groupReference = FirebaseDatabase.getInstance().getReference("SecretGroups");
        storageImageReference = FirebaseStorage.getInstance().getReference("group_profile_pictures");
        storageCoverReference = FirebaseStorage.getInstance().getReference("group_cover_pictures");

        groupPrivacyBTN.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.privateGP:
                        privacyLock = "Private";
                        Toast.makeText(CreateGroupActivity.this, privacyLock, Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.publicGP:
                        privacyLock = "Public";
                        Toast.makeText(CreateGroupActivity.this, privacyLock, Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        Toast.makeText(CreateGroupActivity.this, "Please select group privacy", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Group...");
        progressDialog.setCancelable(false);

        groupId = groupReference.push().getKey();

        uploadFunctions = new UploadFunctions(this);

        addCoverPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(16, 9)
                        .start(CreateGroupActivity.this);
                choice = "coverPic";
            }
        });

        groupProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(CreateGroupActivity.this);
                choice = "groupPic";
            }
        });

        createGroupBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(setupGroupName.getText().toString())){
                    setupGroupName.setError("Write Group Name");
                    setupGroupName.requestFocus();
                }else if (TextUtils.isEmpty(setupGroupPurpose.getText().toString())){
                    setupGroupPurpose.setError("Write group Purpose");
                    setupGroupPurpose.requestFocus();
                }else if (!validatePrivacy()){
                    Toast.makeText(CreateGroupActivity.this, "You need to select group privacy", Toast.LENGTH_SHORT).show();
                }else if(imageUri == null || coverPicUri == null){
                    Toast.makeText(CreateGroupActivity.this, "Please select both cover and profile pictures", Toast.LENGTH_SHORT).show();
                }else{
                    progressDialog.show();
                    createGroup();

                }
            }
        });

    }

    private void createGroup() {
        selectedPrivacy = findViewById(groupPrivacyBTN.getCheckedRadioButtonId());
        privacyLock = selectedPrivacy.getText().toString();

        final StorageReference picFileReference = storageImageReference.child(groupId
                + "." + uploadFunctions.getFileExtension(imageUri));

        uploadTask = picFileReference.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful())
                    throw task.getException();
                return picFileReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri picDownloadUri = task.getResult();

                    final StorageReference coverFileReference = storageCoverReference.child(groupId
                            + "." + uploadFunctions.getFileExtension(coverPicUri));

                    coverUploadTask = coverFileReference.putFile(coverPicUri);
                    coverUploadTask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if (!task.isSuccessful())
                                throw task.getException();
                            return coverFileReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                Uri coverDownloadUri = task.getResult();

                                HashMap<String, Object> groupMap = new HashMap<>();

                                groupMap.put("groupID", groupId);
                                groupMap.put("groupAdmin", firebaseUser.getUid());
                                groupMap.put("groupName", setupGroupName.getText().toString());
                                groupMap.put("groupPurpose", setupGroupPurpose.getText().toString());
                                groupMap.put("groupRole", groupRole);
                                groupMap.put("groupPrivacy", selectedPrivacy.getText().toString());
                                groupMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
                                groupMap.put("groupCoverPic", coverDownloadUri.toString());
                                groupMap.put("groupProPic", picDownloadUri.toString());

                                groupReference.child(groupId).setValue(groupMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                loadMyParticipation();
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
            }
        });
    }

    private void loadMyParticipation() {
        HashMap<String, Object> participationMap = new HashMap<>();
        participationMap.put("userID", firebaseUser.getUid());
        participationMap.put("groupRole", groupRole);
        participationMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
        participationMap.put("groupID", groupId);

        groupReference.child(groupId).child("Participants")
                .child(firebaseUser.getUid()).setValue(participationMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(CreateGroupActivity.this, GroupChatActivity.class);
                        intent.putExtra("groupID", groupId);
                        startActivity(intent);
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateGroupActivity.this, "failed to update participation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validatePrivacy() {
        if(groupPrivacyBTN.getCheckedRadioButtonId() == -1){
            Toast.makeText(this, "Please select privacy", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            switch (choice){
                case "groupPic":
                    imageUri = result.getUri();
                    groupProfilePic.setImageURI(imageUri);
                    break;

                case "coverPic":
                    coverPicUri = result.getUri();
                    groupCoverPic.setImageURI(coverPicUri);
                    break;

                default:
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}