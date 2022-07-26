package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
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
import com.makepe.blackout.GettingStarted.Models.GroupsModel;
import com.makepe.blackout.GettingStarted.OtherClasses.UploadFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditGroupDetailsActivity extends AppCompatActivity {

    private ImageView editGroupCoverPic, editCoverPicBTN;
    private CircleImageView editGroupProPic;
    private TextInputEditText groupNameTV, groupPurposeTV;
    private Button updateBTN;

    private Uri imageUri, coverPicUri;
    private String groupID, choice, groupCoverURL, groupPicURL, groupName, groupPurpose;
    private StorageTask uploadTask, coverUploadTask;
    private DatabaseReference groupReference;
    private StorageReference proPicStorageReference, coverPicStorageReference;

    private ProgressDialog progressDialog;
    private UploadFunctions uploadFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group_details);

        Toolbar groupDetailsToolbar = findViewById(R.id.editGroupToolbar);
        setSupportActionBar(groupDetailsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editGroupCoverPic = findViewById(R.id.editGroupCoverPic);
        editCoverPicBTN = findViewById(R.id.editGroupCoverPicBTN);
        editGroupProPic = findViewById(R.id.editGroupIcon);
        groupNameTV = findViewById(R.id.editGroupName);
        updateBTN = findViewById(R.id.updateGroupBTN);
        groupPurposeTV = findViewById(R.id.editGroupPurpose);

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Group Details. Please Wait...");

        groupReference = FirebaseDatabase.getInstance().getReference("SecretGroups");
        proPicStorageReference = FirebaseStorage.getInstance().getReference("group_profile_pictures");
        coverPicStorageReference = FirebaseStorage.getInstance().getReference("group_cover_pictures");

        uploadFunctions = new UploadFunctions(this);

        getGroupDetails();

        editCoverPicBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(16, 9)
                        .start(EditGroupDetailsActivity.this);
                choice = "coverPic";
            }
        });

        editGroupProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(EditGroupDetailsActivity.this);
                choice = "groupPic";
            }
        });

        updateBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(groupNameTV.getText().toString())){
                    groupNameTV.setError("Write Group Name");
                    groupNameTV.requestFocus();
                }else if (TextUtils.isEmpty(groupPurposeTV.getText().toString())){
                    groupPurposeTV.setError("Write Group purpose");
                    groupPurposeTV.requestFocus();
                }else{

                    progressDialog.show();

                    if (imageUri != null && coverPicUri != null)
                        updateGroupPictures();
                    else if(imageUri != null)
                        updateGroupProfilePicOnly();
                    else if(coverPicUri != null)
                        updateGroupCoverPicOnly();
                    else
                        updateGroupCredentials();

                    //updateCredentials();
                }
            }
        });

    }

    private void updateGroupCredentials() {

        HashMap<String, Object> updateCredentialsMap = new HashMap<>();

        if (!groupNameTV.getText().toString().equals(groupName))
            updateCredentialsMap.put("groupName", groupNameTV.getText().toString());

        if (!groupPurposeTV.getText().toString().equals(groupPurpose))
            updateCredentialsMap.put("groupPurpose", groupPurposeTV.getText().toString());

        groupReference.child(groupID).updateChildren(updateCredentialsMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        finish();
                    }
                });
    }

    private void updateGroupCoverPicOnly() {
        final StorageReference oldPicReference = coverPicStorageReference.child(groupCoverURL);
        oldPicReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                final StorageReference coverFileReference = coverPicStorageReference.child(groupReference.push().getKey()
                        + "." + uploadFunctions.getFileExtension(coverPicUri));

                coverUploadTask = coverFileReference.putFile(coverPicUri);
                coverUploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful())
                            throw task.getException();
                        return coverFileReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri coverDownloadUri = task.getResult();

                            HashMap<String, Object> updateCredentialsMap = new HashMap<>();

                            if (!groupNameTV.getText().toString().equals(groupName))
                                updateCredentialsMap.put("groupName", groupNameTV.getText().toString());

                            if (!groupPurposeTV.getText().toString().equals(groupPurpose))
                                updateCredentialsMap.put("groupPurpose", groupPurposeTV.getText().toString());

                            updateCredentialsMap.put("groupCoverPic", coverDownloadUri.toString());

                            groupReference.child(groupID).updateChildren(updateCredentialsMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressDialog.dismiss();
                                            finish();
                                        }
                                    });

                        }
                    }
                });
            }
        });
    }

    private void updateGroupProfilePicOnly() {
        final StorageReference oldPicReference = proPicStorageReference.child(groupPicURL);
        oldPicReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                final StorageReference picFileReference = proPicStorageReference.child(groupReference.push().getKey()
                        + "." + uploadFunctions.getFileExtension(imageUri));
                uploadTask = picFileReference.putFile(imageUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful())
                            throw task.getException();
                        return picFileReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri picDownloadUri = task.getResult();

                            HashMap<String, Object> updateCredentialsMap = new HashMap<>();

                            if (!groupNameTV.getText().toString().equals(groupName))
                                updateCredentialsMap.put("groupName", groupNameTV.getText().toString());

                            if (!groupPurposeTV.getText().toString().equals(groupPurpose))
                                updateCredentialsMap.put("groupPurpose", groupPurposeTV.getText().toString());

                            updateCredentialsMap.put("groupProPic", picDownloadUri.toString());

                            groupReference.child(groupID).updateChildren(updateCredentialsMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressDialog.dismiss();
                                            finish();
                                        }
                                    });

                        }
                    }
                });
            }
        });

    }

    private void updateGroupPictures() {
        final StorageReference oldPicReference = proPicStorageReference.child(groupPicURL);
        oldPicReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                final StorageReference picFileReference = proPicStorageReference.child(groupReference.push().getKey()
                        + "." + uploadFunctions.getFileExtension(imageUri));
                uploadTask = picFileReference.putFile(imageUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful())
                            throw task.getException();
                        return picFileReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri picDownloadURI = task.getResult();

                            final StorageReference coverRef = coverPicStorageReference.child(groupCoverURL);
                            coverRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    final StorageReference coverFileReference = coverPicStorageReference.child(groupReference.push().getKey()
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

                                                HashMap<String, Object> updateCredentialsMap = new HashMap<>();

                                                if (!groupNameTV.getText().toString().equals(groupName))
                                                    updateCredentialsMap.put("groupName", groupNameTV.getText().toString());

                                                if (!groupPurposeTV.getText().toString().equals(groupPurpose))
                                                    updateCredentialsMap.put("groupPurpose", groupPurposeTV.getText().toString());

                                                updateCredentialsMap.put("groupProPic", picDownloadURI.toString());
                                                updateCredentialsMap.put("groupCoverPic", coverDownloadUri.toString());

                                                groupReference.child(groupID).updateChildren(updateCredentialsMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                progressDialog.dismiss();
                                                                finish();
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
        });
    }

    private void getGroupDetails() {
        groupReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    GroupsModel groups = ds.getValue(GroupsModel.class);

                    assert groups != null;
                    if (groups.getGroupID().equals(groupID)){
                        groupName = groups.getGroupName();
                        groupPurpose = groups.getGroupPurpose();

                        groupNameTV.setText(groupName);
                        groupPurposeTV.setText(groupPurpose);

                        groupCoverURL = groups.getGroupCoverPic();
                        groupPicURL = groups.getGroupProPic();

                        try{
                            Picasso.get().load(groupPicURL).into(editGroupProPic);
                            Picasso.get().load(groupCoverURL).into(editGroupCoverPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(editGroupProPic);
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(editGroupCoverPic);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            switch (choice){
                case "groupPic":
                    imageUri = result.getUri();
                    editGroupProPic.setImageURI(imageUri);
                    break;

                case "coverPic":
                    coverPicUri = result.getUri();
                    editGroupCoverPic.setImageURI(coverPicUri);
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