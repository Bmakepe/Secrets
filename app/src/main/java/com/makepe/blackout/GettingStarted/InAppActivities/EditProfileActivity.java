package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.makepe.blackout.GettingStarted.OtherClasses.UploadFunctions;
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
    private RadioGroup userGenderBTN;
    private RadioButton radioBtnMale, radioBtnFemale;


    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;
    private StorageReference imageStorageReference, coverStorageReference;

    private Uri proPicUri, coverPicUri;
    private StorageTask uploadTask, coverUploadTask;
    private String coverURL, profileURL, username, userBiography,
            userDOB, userGender, choice, genderSelected;

    private ProgressDialog progressDialog;
    private LocationServices locationServices;
    private double latitude, longitude;

    private UploadFunctions uploadFunctions;

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
        userGenderBTN = findViewById(R.id.userGenderBTN);
        radioBtnMale = findViewById(R.id.maleChecked);
        radioBtnFemale = findViewById(R.id.femaleChecked);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        imageStorageReference = FirebaseStorage.getInstance().getReference("user_profile_pictures");
        coverStorageReference = FirebaseStorage.getInstance().getReference("user_cover_images");

        locationServices = new LocationServices(locationET, EditProfileActivity.this);
        uploadFunctions = new UploadFunctions(this);
        progressDialog = new ProgressDialog(this);

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

        userGenderBTN.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.maleChecked:
                        genderSelected = "Male";
                        Toast.makeText(EditProfileActivity.this, genderSelected, Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.femaleChecked:
                        genderSelected = "Female";
                        Toast.makeText(EditProfileActivity.this, genderSelected, Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        Toast.makeText(EditProfileActivity.this, "Please select group privacy", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        getUserDetails();

        locationET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationServices.getMyLocation();
            }
        });

        changePicArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(EditProfileActivity.this);

                choice = "proPic";
            }
        });

        changeCoverPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    dateOfBirthET.setError("Select Your Date Of Birth");
                    dateOfBirthET.requestFocus();
                }else{

                    progressDialog.setMessage("Updating Profile. Please Wait...");
                    progressDialog.show();

                    if (proPicUri != null && coverPicUri != null)
                        updateUserPictureDetails();
                    else if (proPicUri != null)
                        updateUserProfilePicOnly();
                    else if (coverPicUri != null)
                        updateUserCoverPicOnly();
                    else
                        updateUserDetails();

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

    private void updateUserDetails() {
        HashMap<String, Object> credentialsMap = new HashMap<>();

        if (!nameET.getText().toString().equals(username))
            credentialsMap.put("username", nameET.getText().toString());

        if (!biographyET.getText().toString().equals(userBiography))
            credentialsMap.put("biography", biographyET.getText().toString());

        if (!dateOfBirthET.getText().toString().equals(userDOB))
            credentialsMap.put("dateOfBirth", dateOfBirthET.getText().toString());

        if (latitude != locationServices.getLatitude() && longitude != locationServices.getLongitude()
                || !locationET.getText().toString().equals(R.string.location_text)) {
            credentialsMap.put("latitude", locationServices.getLatitude());
            credentialsMap.put("longitude", locationServices.getLongitude());
        }

        if (radioBtnMale.isChecked())
            if (!userGender.equals("Male"))
                credentialsMap.put("gender", "Male");
        else if (radioBtnFemale.isChecked())
            if (!userGender.equals("Female"))
                credentialsMap.put("gender", "Female");

        userRef.child(firebaseUser.getUid()).updateChildren(credentialsMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        finish();
                    }
                });
    }

    private void updateUserCoverPicOnly() {
        final StorageReference coverReference = coverStorageReference.child(coverURL);
        coverReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                final StorageReference coverFileReference = coverStorageReference.child(userRef.push().getKey()
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

                            HashMap<String, Object> credentialsMap = new HashMap<>();

                            if (!nameET.getText().toString().equals(username))
                                credentialsMap.put("username", nameET.getText().toString());

                            if (!biographyET.getText().toString().equals(userBiography))
                                credentialsMap.put("biography", biographyET.getText().toString());

                            if (!dateOfBirthET.getText().toString().equals(userDOB))
                                credentialsMap.put("dateOfBirth", dateOfBirthET.getText().toString());

                            if (latitude != locationServices.getLatitude() && longitude != locationServices.getLongitude()
                                    || !locationET.getText().toString().equals(R.string.location_text)) {
                                credentialsMap.put("latitude", locationServices.getLatitude());
                                credentialsMap.put("longitude", locationServices.getLongitude());
                            }

                            if (radioBtnMale.isChecked())
                                if (!userGender.equals("Male"))
                                    credentialsMap.put("gender", "Male");
                            else if (radioBtnFemale.isChecked())
                                if (!userGender.equals("Female"))
                                    credentialsMap.put("gender", "Female");

                            credentialsMap.put("coverURL", coverDownloadUri.toString());

                            userRef.child(firebaseUser.getUid()).updateChildren(credentialsMap)
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

    private void updateUserProfilePicOnly() {
        final StorageReference picReference = imageStorageReference.child(profileURL);
        picReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                final StorageReference picFileReference = imageStorageReference.child(userRef.push().getKey()
                        + "." + uploadFunctions.getFileExtension(proPicUri));
                uploadTask = picFileReference.putFile(proPicUri);
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

                            HashMap<String, Object> credentialsMap = new HashMap<>();

                            if (!nameET.getText().toString().equals(username))
                                credentialsMap.put("username", nameET.getText().toString());

                            if (!biographyET.getText().toString().equals(userBiography))
                                credentialsMap.put("biography", biographyET.getText().toString());

                            if (!dateOfBirthET.getText().toString().equals(userDOB))
                                credentialsMap.put("dateOfBirth", dateOfBirthET.getText().toString());

                            if (latitude != locationServices.getLatitude() && longitude != locationServices.getLongitude()
                                    || !locationET.getText().toString().equals(R.string.location_text)) {
                                credentialsMap.put("latitude", locationServices.getLatitude());
                                credentialsMap.put("longitude", locationServices.getLongitude());
                            }

                            if (radioBtnMale.isChecked())
                                if (!userGender.equals("Male"))
                                    credentialsMap.put("gender", "Male");
                            else if (radioBtnFemale.isChecked())
                                if (!userGender.equals("Female"))
                                        credentialsMap.put("gender", "Female");

                            credentialsMap.put("imageURL", picDownloadUri.toString());

                            userRef.child(firebaseUser.getUid()).updateChildren(credentialsMap)
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

    private void updateUserPictureDetails() {
        final StorageReference picRef = imageStorageReference.child(profileURL);
        picRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                final StorageReference picFileReference = imageStorageReference.child(userRef.push().getKey()
                        + "." + uploadFunctions.getFileExtension(proPicUri));

                uploadTask = picFileReference.putFile(proPicUri);
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
                            Uri proDownloadUri = task.getResult();

                            final StorageReference coverRef = coverStorageReference.child(coverURL);
                            coverRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    final StorageReference coverFileReference = coverStorageReference.child(userRef.push().getKey()
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

                                                HashMap<String, Object> credentialsMap = new HashMap<>();

                                                if (!nameET.getText().toString().equals(username))
                                                    credentialsMap.put("username", nameET.getText().toString());

                                                if (!biographyET.getText().toString().equals(userBiography))
                                                    credentialsMap.put("biography", biographyET.getText().toString());

                                                if (!dateOfBirthET.getText().toString().equals(userDOB))
                                                    credentialsMap.put("dateOfBirth", dateOfBirthET.getText().toString());

                                                if (latitude != locationServices.getLatitude() && longitude != locationServices.getLongitude()
                                                        || !locationET.getText().toString().equals(R.string.location_text)) {
                                                    credentialsMap.put("latitude", locationServices.getLatitude());
                                                    credentialsMap.put("longitude", locationServices.getLongitude());
                                                }

                                                if (radioBtnMale.isChecked())
                                                    if (!userGender.equals("Male"))
                                                        credentialsMap.put("gender", "Male");
                                                else if (radioBtnFemale.isChecked())
                                                    if (!userGender.equals("Female"))
                                                        credentialsMap.put("gender", "Female");

                                                credentialsMap.put("imageURL", proDownloadUri.toString());
                                                credentialsMap.put("coverURL", coverDownloadUri.toString());

                                                userRef.child(firebaseUser.getUid()).updateChildren(credentialsMap)
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

    private void updateDOB(Calendar myCalendar, EditText dateOfBirthET) {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        dateOfBirthET.setText(sdf.format(myCalendar.getTime()) + "");
    }

    private void getUserDetails() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUserID().equals(firebaseUser.getUid())){

                        username = user.getUsername();
                        userBiography = user.getBiography();
                        userDOB = user.getDateOfBirth();
                        coverURL = user.getCoverURL();
                        profileURL = user.getImageURL();
                        userGender = user.getGender();

                        nameET.setText(username);
                        biographyET.setText(userBiography);
                        dateOfBirthET.setText(userDOB);

                        if (userGender.equalsIgnoreCase("Male"))
                            radioBtnMale.setChecked(true);
                        else if (userGender.equalsIgnoreCase("Female"))
                            radioBtnFemale.setChecked(true);

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