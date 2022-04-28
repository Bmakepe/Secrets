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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.makepe.blackout.GettingStarted.Models.Movement;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditMovementActivity extends AppCompatActivity {

    private ImageView movementCoverPic, movementCoverEditBTN;
    private CircleImageView movementProPic;
    private TextInputEditText movementNameTV, movementPurposeTV;
    private Button updateBTN;

    private String movementID;
    private DatabaseReference movementReference;

    private Uri imageUri, coverPicUri;
    private String myUri = "", coverUri = "", choice, movementName, movementPurpose, movementCoverURL, movementPicURL;
    private StorageTask uploadTask, coverUploadTask;
    private StorageReference storageReference;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_movement);

        Toolbar movementToolbar = findViewById(R.id.editMovementToolbar);
        setSupportActionBar(movementToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        movementCoverPic = findViewById(R.id.editMovementCoverPic);
        movementCoverEditBTN = findViewById(R.id.editMovementCoverPicBTN);
        movementProPic = findViewById(R.id.editMovementIcon);
        movementNameTV = findViewById(R.id.editMovementName);
        movementPurposeTV = findViewById(R.id.editMovementPurpose);
        updateBTN = findViewById(R.id.updateMovementBTN);

        Intent intent = getIntent();
        movementID = intent.getStringExtra("movementID");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Movement...");

        movementReference = FirebaseDatabase.getInstance().getReference("Movements");
        storageReference = FirebaseStorage.getInstance().getReference("MovementPictures");

        getMovementDetails();

        movementCoverEditBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(16, 9)
                        .start(EditMovementActivity.this);
                choice = "coverPic";

            }
        });

        movementProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(EditMovementActivity.this);
                choice = "movementPic";

            }
        });

        updateBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                movementName = movementNameTV.getText().toString();
                movementPurpose = movementPurposeTV.getText().toString();

                if (TextUtils.isEmpty(movementName)){
                    movementNameTV.setError("Write Group Name");
                    movementNameTV.requestFocus();
                }else if (TextUtils.isEmpty(movementPurpose)){
                    movementPurposeTV.setError("Write Group purpose");
                    movementPurposeTV.requestFocus();
                }else{
                    progressDialog.show();
                    updateCredentials();
                }

            }
        });

    }

    private void updateCredentials() {
        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put("movementName", movementName);
        updateMap.put("movementPurpose", movementPurpose);

        movementReference.child(movementID).updateChildren(updateMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateProfilePic();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditMovementActivity.this, "Error updating credentials", Toast.LENGTH_SHORT).show();
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

                        StorageReference picRef = FirebaseStorage.getInstance().getReference(movementPicURL);
                        picRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                HashMap<String, Object> picMap = new HashMap<>();
                                picMap.put("movementProPic", myUri);

                                movementReference.child(movementID).updateChildren(picMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                updateCoverPicOnly();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditMovementActivity.this, "Error Uploading Group Picture", Toast.LENGTH_SHORT).show();

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

                        StorageReference coverRef = FirebaseStorage.getInstance().getReference(movementCoverURL);
                        coverRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                HashMap<String, Object> coverMap = new HashMap<>();
                                coverMap.put("movementCoverPic", coverUri);

                                movementReference.child(movementID).updateChildren(coverMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(EditMovementActivity.this, "Successfully Updated Movement", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditMovementActivity.this, "Failed to upload group profile pic", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });

                    }
                }
            });
        }else {
            Toast.makeText(EditMovementActivity.this, "Successfully Updated Movement", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void getMovementDetails() {
        movementReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    Movement movement = ds.getValue(Movement.class);

                    if (movement.getMovementID().equals(movementID)) {

                        movementNameTV.setText(movement.getMovementName());
                        movementPurposeTV.setText(movement.getMovementPurpose());

                        movementCoverURL = movement.getMovementCoverPic();
                        movementPicURL = movement.getMovementProPic();

                        try{
                            Picasso.get().load(movement.getMovementCoverPic()).into(movementCoverPic);
                            Picasso.get().load(movement.getMovementProPic()).into(movementProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(movementCoverPic);
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(movementProPic);

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
                case "movementPic":
                    imageUri = result.getUri();
                    movementProPic.setImageURI(imageUri);
                    break;

                case "coverPic":
                    coverPicUri = result.getUri();
                    movementCoverPic.setImageURI(coverPicUri);
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