package com.makepe.blackout.GettingStarted;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.makepe.blackout.GettingStarted.OtherClasses.LocationServices;
import com.makepe.blackout.GettingStarted.OtherClasses.Permissions;
import com.makepe.blackout.GettingStarted.OtherClasses.UploadFunctions;
import com.makepe.blackout.R;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserDetailsActivity extends AppCompatActivity {

    //data views
    private CircleImageView setupProfilePic;
    private TextInputEditText setupUsername, setupBiography;
    private Button continueBTN;
    private CheckBox maleCheck, femaleCheck;
    private ProgressDialog progressDialog;
    private EditText dateOfBirth, locationET;
    private ImageView addCoverPic, coverPic;
    private String choice, boy = "Male", girl = "Female", terms = "Accepted";

    private Uri imageUri, coverPicUri;
    private StorageTask uploadTask, coverUploadTask;
    private FirebaseUser firebaseUser;
    private DatabaseReference userReference;
    private StorageReference imageStorageReference, coverStorageReference;

    private LocationServices locationServices;
    private UploadFunctions uploadFunctions;

    private Permissions permissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        final Calendar myCalendar = Calendar.getInstance();

        setupProfilePic = findViewById(R.id.setupProfilePic);
        setupUsername = findViewById(R.id.setupUsername);
        setupBiography = findViewById(R.id.setupBiography);
        continueBTN = findViewById(R.id.continueBTN);
        maleCheck = findViewById(R.id.checkMale);
        femaleCheck = findViewById(R.id.checkFemale);
        dateOfBirth= findViewById(R.id.dateOfBirthPicker);
        locationET= findViewById(R.id.userLocation);
        addCoverPic= findViewById(R.id.addCoverPicBTN);
        coverPic = findViewById(R.id.regProCoverPic);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                //function for picking the DOB and updating the label to show the DOB picked
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateDOB(myCalendar, dateOfBirth);

            }
        };

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        imageStorageReference = FirebaseStorage.getInstance().getReference("user_profile_pictures");
        coverStorageReference = FirebaseStorage.getInstance().getReference("user_cover_images");
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Setting Up Your Profile");
        locationServices = new LocationServices(locationET, UserDetailsActivity.this);
        uploadFunctions = new UploadFunctions(this);
        permissions = new Permissions(this);
        permissions.verifyPermissions();

        selectGender();
        checkUserStatus();

        continueBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmUserInfo();
            }
        });

        dateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(UserDetailsActivity.this,  date, myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        setupProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(UserDetailsActivity.this);
                choice = "proPic";
            }
        });

        locationET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationServices.getMyLocation();
            }
        });

        addCoverPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(16, 9)
                        .start(UserDetailsActivity.this);
                choice = "coverPic";
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void updateDOB(Calendar myCalendar, EditText dateOfBirth) {
        //function to update the DOB label with the date you picked

        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        dateOfBirth.setText(sdf.format(myCalendar.getTime()) + "");
    }

    private void confirmUserInfo() {

        if(TextUtils.isEmpty(setupUsername.getText().toString())){
            setupUsername.setError("Enter Your Username");
            setupUsername.requestFocus();
        }else if(TextUtils.isEmpty(setupBiography.getText().toString())){
            setupBiography.setError("Tell Us Something About Yourself");
            setupBiography.requestFocus();
        }else if(!maleCheck.isChecked() && !femaleCheck.isChecked()){
            Toast.makeText(UserDetailsActivity.this, "Please Select Your Gender", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(dateOfBirth.getText().toString())){
            dateOfBirth.setError("Please Pick Date of Birth");
            dateOfBirth.requestFocus();
        }else if(imageUri == null || coverPicUri == null){
            Toast.makeText(this, "Please select both the profile and cover pictures", Toast.LENGTH_SHORT).show();
        }else{
            updateUserDetails();
        }

    }

    private void updateUserDetails() {
        progressDialog.show();

        final StorageReference picFileReference = imageStorageReference.child(firebaseUser.getUid()
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

                    final StorageReference coverFileReference = coverStorageReference.child(firebaseUser.getUid()
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

                                credentialsMap.put("userID", firebaseUser.getUid());
                                credentialsMap.put("username", Objects.requireNonNull(setupUsername.getText()).toString());
                                credentialsMap.put("biography", Objects.requireNonNull(setupBiography.getText()).toString());
                                credentialsMap.put("dateOfBirth", dateOfBirth.getText().toString());
                                credentialsMap.put("phoneNumber", firebaseUser.getPhoneNumber());
                                credentialsMap.put("terms", terms);
                                credentialsMap.put("imageURL", picDownloadUri.toString());
                                credentialsMap.put("coverURL", coverDownloadUri.toString());
                                credentialsMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
                                credentialsMap.put("isVerified", false);

                                if(maleCheck.isChecked()){
                                    credentialsMap.put("gender", boy);
                                }else if(femaleCheck.isChecked()){
                                    credentialsMap.put("gender", girl);
                                }

                                if (!locationET.getText().toString().equals(R.string.location_text)) {
                                    credentialsMap.put("latitude", locationServices.getLatitude());
                                    credentialsMap.put("longitude", locationServices.getLongitude());
                                }

                                userReference.child(firebaseUser.getUid()).setValue(credentialsMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                progressDialog.dismiss();
                                                startActivity(new Intent(UserDetailsActivity.this, TimelineSetupActivity.class));
                                                Toast.makeText(UserDetailsActivity.this, "Welcome To Secrets", Toast.LENGTH_SHORT).show();
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

    private void selectGender() {
        maleCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                femaleCheck.setChecked(false);
            }
        });

        femaleCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                maleCheck.setChecked(false);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)  {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            switch (choice){
                case "proPic":

                    imageUri = result.getUri();
                    setupProfilePic.setImageURI(imageUri);
                    break;

                case "coverPic":
                    coverPicUri = result.getUri();
                    coverPic.setImageURI(coverPicUri);
                    break;

                default:
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkUserStatus(){
        if(firebaseUser == null){
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        }
    }

}
