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

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
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
    private String myUri = "", coverUri = "", groupID, choice, groupName, groupPurpose, groupCoverURL, groupPicURL;
    private StorageTask uploadTask, coverUploadTask;
    private DatabaseReference groupReference;
    private StorageReference storageReference;

    private ProgressDialog progressDialog;

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
        progressDialog.setMessage("Updating Group...");

        groupReference = FirebaseDatabase.getInstance().getReference("SecretGroups");
        storageReference = FirebaseStorage.getInstance().getReference("GroupPictures");

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
                groupPurpose = groupPurposeTV.getText().toString();
                groupName = groupNameTV.getText().toString();

                if (TextUtils.isEmpty(groupName)){
                    groupNameTV.setError("Write Group Name");
                    groupNameTV.requestFocus();
                }else if (TextUtils.isEmpty(groupPurpose)){
                    groupPurposeTV.setError("Write Group purpose");
                    groupPurposeTV.requestFocus();
                }else{
                    progressDialog.show();
                    updateCredentials();
                }
            }
        });

    }

    private void updateCredentials() {
        HashMap<String, Object> updateCredentialsMap = new HashMap<>();
        updateCredentialsMap.put("groupName", groupName);
        updateCredentialsMap.put("groupPurpose", groupPurpose);

        groupReference.child(groupID).updateChildren(updateCredentialsMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateProfilePic();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditGroupDetailsActivity.this, "Error updating credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfilePic() {
        if (myUri != null){
            final StorageReference picFileReference = storageReference.child(System.currentTimeMillis()
                    + "." +getFileExtension(imageUri));
            uploadTask = picFileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return picFileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri picDownLoadUri = task.getResult();
                        myUri = picDownLoadUri.toString();

                        StorageReference picRef = FirebaseStorage.getInstance().getReference(groupPicURL);
                        picRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                HashMap<String, Object> picMap = new HashMap<>();
                                picMap.put("groupProPic", myUri);

                                groupReference.child(groupID).updateChildren(picMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                updateCoverPicOnly();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditGroupDetailsActivity.this, "Error Uploading Group Picture", Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        });

                    }
                }
            });
        }else{
            updateCoverPicOnly();
        }
    }

    private void updateCoverPicOnly() {
        if (coverUri != null){
            final StorageReference coverFileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(coverPicUri));
            coverUploadTask = coverFileReference.putFile(coverPicUri);
            coverUploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return coverFileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri coverDownloadUri = task.getResult();
                        coverUri = coverDownloadUri.toString();

                        StorageReference coverRef = FirebaseStorage.getInstance().getReference(groupCoverURL);
                        coverRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                HashMap<String, Object> coverMap = new HashMap<>();
                                coverMap.put("groupCoverPic", coverUri);

                                groupReference.child(groupID).updateChildren(coverMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(EditGroupDetailsActivity.this, "Successfully Updated Group", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditGroupDetailsActivity.this, "Failed to upload group profile pic", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        });

                    }
                }
            });
        }else {
            Toast.makeText(EditGroupDetailsActivity.this, "Successfully Updated Group", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void getGroupDetails() {
        groupReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    GroupsModel groups = ds.getValue(GroupsModel.class);

                    assert groups != null;
                    if (groups.getGroupID().equals(groupID)){
                        groupNameTV.setText(groups.getGroupName());
                        groupPurposeTV.setText(groups.getGroupPurpose());

                        groupCoverURL = groups.getGroupCoverPic();
                        groupPicURL = groups.getGroupProPic();

                        try{
                            Picasso.get().load(groups.getGroupProPic()).into(editGroupProPic);
                            Picasso.get().load(groups.getGroupCoverPic()).into(editGroupCoverPic);
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

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(contentResolver.getType(uri));
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