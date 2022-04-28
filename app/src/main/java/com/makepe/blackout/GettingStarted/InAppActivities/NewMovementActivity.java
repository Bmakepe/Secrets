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
import com.makepe.blackout.R;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewMovementActivity extends AppCompatActivity {

    private ImageView newMovementCoverPic, newMovementCoverBTN;
    private CircleImageView newMovementProPic;
    private Button createMovementBTN;
    private TextInputEditText movementNameTV, movementPurposeTV;
    private RadioGroup groupPrivacyBTN;
    private RadioButton selectedPrivacy;

    private Uri imageUri, coverPicUri;
    private String myUri = "", coverUri = "", movementID, privacyLock, choice, movementName, movementPurpose;
    private StorageTask uploadTask, coverUploadTask;
    private FirebaseUser firebaseUser;
    private DatabaseReference movementReference;
    private StorageReference storageReference;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_movement);

        Toolbar movementToolbar = findViewById(R.id.newMovementToolbar);
        setSupportActionBar(movementToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newMovementCoverPic = findViewById(R.id.newMovementCoverPic);
        newMovementCoverBTN = findViewById(R.id.newMovementCoverPicBTN);
        newMovementProPic = findViewById(R.id.newMovementIcon);
        createMovementBTN = findViewById(R.id.newMovementBTN);
        movementNameTV = findViewById(R.id.newMovementName);
        movementPurposeTV = findViewById(R.id.newMovementPurpose);
        groupPrivacyBTN = findViewById(R.id.newMovementPrivacyBTN);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        movementReference = FirebaseDatabase.getInstance().getReference("Movements");
        storageReference = FirebaseStorage.getInstance().getReference("MovementPictures");

        groupPrivacyBTN.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.privateGP:
                        privacyLock = "Private";
                        Toast.makeText(NewMovementActivity.this, privacyLock, Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.publicGP:
                        privacyLock = "Public";
                        Toast.makeText(NewMovementActivity.this, privacyLock, Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        Toast.makeText(NewMovementActivity.this, "Please select group privacy", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Movement...");

        newMovementCoverBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(16, 9)
                        .start(NewMovementActivity.this);
                choice = "coverPic";

            }
        });

        newMovementProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(NewMovementActivity.this);
                choice = "movementPic";

            }
        });

        createMovementBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(NewMovementActivity.this, "You will be able to create a movement", Toast.LENGTH_SHORT).show();

                movementName = movementNameTV.getText().toString();
                movementPurpose = movementPurposeTV.getText().toString();

                if (TextUtils.isEmpty(movementName)){
                    movementNameTV.setError("Write Group Name");
                    movementNameTV.requestFocus();

                }else if (TextUtils.isEmpty(movementPurpose)){
                    movementPurposeTV.setError("Write Group purpose");
                    movementPurposeTV.requestFocus();

                }else if (!validatePrivacy()){
                    Toast.makeText(NewMovementActivity.this, "You need to select group privacy", Toast.LENGTH_SHORT).show();

                }else{
                    progressDialog.show();
                    uploadMovementCredentials();

                }
            }
        });

    }

    private void uploadMovementCredentials() {
        movementID = movementReference.push().getKey();
        selectedPrivacy = findViewById(groupPrivacyBTN.getCheckedRadioButtonId());
        privacyLock = selectedPrivacy.getText().toString();

        HashMap<String, Object> movementMap = new HashMap<>();
        movementMap.put("movementID", movementID);
        movementMap.put("movementAdmin", firebaseUser.getUid());
        movementMap.put("movementName", movementName);
        movementMap.put("movementPurpose", movementPurpose);
        movementMap.put("movementPrivacy", privacyLock);
        movementMap.put("movementTimestamp", String.valueOf(System.currentTimeMillis()));

        movementReference.child(movementID).setValue(movementMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        uploadMovementCoverPic();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewMovementActivity.this, "Error Creating a new movement for you", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void uploadMovementCoverPic() {
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

                        HashMap<String, Object> coverMap = new HashMap<>();
                        coverMap.put("movementCoverPic", coverUri);

                        movementReference.child(movementID).updateChildren(coverMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        uploadMovementProPic();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(NewMovementActivity.this, "Failed to upload group profile pic", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
            });
        }
    }

    private void uploadMovementProPic() {
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

                        HashMap<String, Object> picMap = new HashMap<>();
                        picMap.put("movementProPic", myUri);

                        movementReference.child(movementID).updateChildren(picMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Intent intent = new Intent(NewMovementActivity.this, MovementDetailsActivity.class);
                                        intent.putExtra("movementID", movementID);
                                        startActivity(intent);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(NewMovementActivity.this, "Error Uploading Group Picture", Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                }
            });
        }
    }

    private boolean validatePrivacy() {
        if(groupPrivacyBTN.getCheckedRadioButtonId() == -1){
            Toast.makeText(this, "Please select privacy", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
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
                case "movementPic":
                    imageUri = result.getUri();
                    newMovementProPic.setImageURI(imageUri);
                    break;

                case "coverPic":
                    coverPicUri = result.getUri();
                    newMovementCoverPic.setImageURI(coverPicUri);
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