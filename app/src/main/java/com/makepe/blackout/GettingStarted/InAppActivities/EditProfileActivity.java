package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.LocationServices;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView coverPic, changeCoverPic;
    private CircleImageView profilePic;
    private Button updateTV;
    private EditText nameET, biographyET, locationET, dateOfBirthET;
    private RelativeLayout changePicArea;
    private Toolbar toolbar;
    private CheckBox maleCheck, femaleCheck;

    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;
    private StorageReference imageStorageReference, coverStorageReference;

    private Uri proPicUri, coverPicUri;
    private StorageTask uploadTask, coverUploadTask;
    private String myUri = "", coverUri = "", coverURL, profileURL,
            username, userBiography, userDOB, userLocation, userGender;

    private String choice;

    private ProgressDialog progressDialog;
    private LocationServices locationServices;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        toolbar = findViewById(R.id.edit_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coverPic = findViewById(R.id.editProCoverPic);
        profilePic = findViewById(R.id.editProUserImage);
        updateTV = findViewById(R.id.updateEditProBTN);
        nameET = findViewById(R.id.editProFullName);
        biographyET = findViewById(R.id.editProBiography);
        changeCoverPic = findViewById(R.id.editCoverPic);
        changePicArea = findViewById(R.id.changePicArea);
        locationET = findViewById(R.id.editLocationET);
        dateOfBirthET = findViewById(R.id.dateOfBirthET);
        maleCheck = findViewById(R.id.editCheckMale);
        femaleCheck = findViewById(R.id.editCheckFemale);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        imageStorageReference = FirebaseStorage.getInstance().getReference("user_profile_pictures");
        coverStorageReference = FirebaseStorage.getInstance().getReference("user_cover_images");

        locationServices = new LocationServices(locationET, EditProfileActivity.this);

        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateDOB(myCalendar, dateOfBirthET);
            }
        };

        progressDialog = new ProgressDialog(this);

        getUserDetails();
        selectGender();

        locationET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationServices.getMyLocation();
            }
        });

        changePicArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(EditProfileActivity.this, "You will be able to change pro pic", Toast.LENGTH_SHORT).show();
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(EditProfileActivity.this);

                choice = "proPic";
            }
        });

        changeCoverPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(EditProfileActivity.this, "You will be able to change cover pic", Toast.LENGTH_SHORT).show();
                CropImage.activity()
                        .setAspectRatio(16,9)
                        .start(EditProfileActivity.this);

                choice = "coverPic";
            }
        });

        updateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(nameET.getText().toString())){
                    nameET.setError("Enter Your Name here");
                    nameET.requestFocus();
                }else if (TextUtils.isEmpty(biographyET.getText().toString())){
                    biographyET.setError("Tell us more about yourself");
                    biographyET.requestFocus();
                }else if (TextUtils.isEmpty(dateOfBirthET.getText().toString())){
                    biographyET.setError("Tell us more about yourself");
                    biographyET.requestFocus();
                }else{
                    updateUserCredentials();
                }
            }
        });

        dateOfBirthET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(EditProfileActivity.this, date, myCalendar.get(Calendar.YEAR)
                        ,myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }

    private void selectGender() {
        maleCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
               femaleCheck.setChecked(false);
            }
        });

        femaleCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                maleCheck.setChecked(false);
            }
        });
    }

    private void updateDOB(Calendar myCalendar, EditText dateOfBirthET) {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        dateOfBirthET.setText(sdf.format(myCalendar.getTime()) + "");
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void updateUserCredentials(){
        progressDialog.setMessage("Updating Profile. Please Wait...");
        progressDialog.show();

        HashMap<String, Object> credentialsMap = new HashMap<>();

        if (!nameET.getText().toString().equals(username))
            credentialsMap.put("Username", nameET.getText().toString());

        if (!biographyET.getText().toString().equals(userBiography))
            credentialsMap.put("Bio", biographyET.getText().toString());

        if (!dateOfBirthET.getText().toString().equals(userDOB))
            credentialsMap.put("DOB", dateOfBirthET.getText().toString());

        if (latitude != locationServices.getLatitude() && longitude != locationServices.getLongitude()
                || !locationET.getText().toString().equals(R.string.location_text)) {
            credentialsMap.put("latitude", locationServices.getLatitude());
            credentialsMap.put("longitude", locationServices.getLongitude());
        }

        if (maleCheck.isChecked())
            if (!userGender.equals("Male"))
                credentialsMap.put("Gender", "Male");
        else if (femaleCheck.isChecked())
            if (!userGender.equals("Female"))
                credentialsMap.put("Gender", "Female");

        userRef.child(firebaseUser.getUid()).updateChildren(credentialsMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (coverUri != null)
                            uploadCoverPicOnly();
                        else if (myUri != null)
                            uploadProfilePicOnly();
                        else{
                            Toast.makeText(EditProfileActivity.this, "User details have been updated", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this, "Updating user details unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadProfilePicOnly(){
        final StorageReference picFileReference = imageStorageReference.child(System.currentTimeMillis()
                + "." + getFileExtension(proPicUri));
        uploadTask = picFileReference.putFile(proPicUri);
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
                    Uri picDownloadUri = task.getResult();
                    myUri = picDownloadUri.toString();

                    StorageReference picRef = FirebaseStorage.getInstance().getReference(profileURL);
                    picRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            HashMap<String, Object> picMap = new HashMap<>();
                            picMap.put("ImageURL", myUri);

                            userRef.child(firebaseUser.getUid()).updateChildren(picMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(EditProfileActivity.this, "Successfully Uploaded profile Picture", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(EditProfileActivity.this, "Could not upload profile picture", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                    });
                }
            }
        });
    }

    private void uploadCoverPicOnly(){
        final StorageReference coverFileReference = coverStorageReference.child(System.currentTimeMillis()
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

                    StorageReference coverRef = FirebaseStorage.getInstance().getReference(coverURL);
                    coverRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            HashMap<String, Object> coverMap = new HashMap<>();
                            coverMap.put("CoverURL", coverUri);

                            userRef.child(firebaseUser.getUid()).updateChildren(coverMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (myUri != null)
                                                uploadProfilePicOnly();
                                            else{
                                                Toast.makeText(EditProfileActivity.this, "Your details have been updated", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                                finish();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(EditProfileActivity.this, "Could not upload cover picture", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                    });
                }
            }
        });

    }

    private void getUserDetails() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUSER_ID().equals(firebaseUser.getUid())){

                        username = user.getUsername();
                        userBiography = user.getBio();
                        userDOB = user.getDOB();
                        coverURL = user.getCoverURL();
                        profileURL = user.getImageURL();
                        userGender = user.getGender();

                        nameET.setText(username);
                        biographyET.setText(userBiography);
                        dateOfBirthET.setText(userDOB);

                        if (userGender.equals("Male"))
                            maleCheck.setChecked(true);
                        else if (userGender.equals("Female"))
                            femaleCheck.setChecked(true);

                        try{
                            Picasso.get().load(profileURL).into(profilePic);
                            Picasso.get().load(coverURL).into(coverPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(coverPic);
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(profilePic);
                        }

                        try{
                            latitude = user.getLatitude();
                            longitude = user.getLongitude();
                            //find address, country, state, city

                            Geocoder geocoder;
                            List<Address> addresses;
                            geocoder = new Geocoder(EditProfileActivity.this, Locale.getDefault());

                            try{
                                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                String address = addresses.get(0).getAddressLine(0);//complete address
                                locationET.setText(address);
                            }catch (Exception e){
                                Toast.makeText(EditProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
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

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            switch (choice){
                case "proPic":
                    proPicUri = result.getUri();
                    profilePic.setImageURI(proPicUri);
                    break;

                case "coverPic":
                    coverPicUri = result.getUri();
                    coverPic.setImageURI(coverPicUri);
                    break;

                default:
                    Toast.makeText(this, "Illegal Choice", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}