package com.makepe.blackout.GettingStarted;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
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
    private String choice, Name, Bio, boy = "Male", girl = "Female", terms = "Accepted", DOB;
    private boolean maleChecked, femaleChecked;

    private Uri imageUri, coverPicUri;
    private String myUri = "", coverUri = "";
    private StorageTask uploadTask, coverUploadTask;
    private FirebaseUser firebaseUser;
    private DatabaseReference userReference;
    private StorageReference storageReference;

    private LocationServices locationServices;

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
        storageReference = FirebaseStorage.getInstance().getReference("pro_pics");
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        progressDialog = new ProgressDialog(this);
        locationServices = new LocationServices(locationET, UserDetailsActivity.this);

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
                        .setAspectRatio(21, 9)
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
        Name = Objects.requireNonNull(setupUsername.getText()).toString();
        Bio = Objects.requireNonNull(setupBiography.getText()).toString();
        DOB = dateOfBirth.getText().toString();
        maleChecked = maleCheck.isChecked();
        femaleChecked = femaleCheck.isChecked();

        if(Name.isEmpty()){
            setupUsername.setError("Enter Your Username");
            setupUsername.requestFocus();
        }else if(Bio.isEmpty()){
            setupBiography.setError("Tell Us Something About Yourself");
            setupBiography.requestFocus();
        }else if(!maleChecked && !femaleChecked){
            Toast.makeText(UserDetailsActivity.this, "Please Select Your Gender", Toast.LENGTH_SHORT).show();
        }else if(DOB.isEmpty()){
                Toast.makeText(UserDetailsActivity.this, "Please Pick Date of Birth", Toast.LENGTH_SHORT).show();
        }else if(imageUri == null || coverPicUri == null){
            Toast.makeText(this, "Please select pic", Toast.LENGTH_SHORT).show();
        }else{
            progressDialog.setMessage("Setting Up Your Profile");
            progressDialog.show();

            uploadUserCredentials();
        }
    }

    private void uploadUserCredentials() {
        HashMap<String, Object> credentialsMap = new HashMap<>();

        credentialsMap.put("USER_ID", firebaseUser.getUid());
        credentialsMap.put("Username", setupUsername.getText().toString());
        credentialsMap.put("Bio", setupBiography.getText().toString());
        credentialsMap.put("DOB", DOB);
        credentialsMap.put("onlineStatus", "online");
        credentialsMap.put("Number", firebaseUser.getPhoneNumber());
        credentialsMap.put("Terms", terms);

        if(maleChecked){
            credentialsMap.put("Gender", boy);
        }else if(femaleChecked){
            credentialsMap.put("Gender", girl);
        }

        if (!TextUtils.isEmpty(locationET.getText().toString())) {
            credentialsMap.put("latitude", locationServices.getLatitude());
            credentialsMap.put("longitude", locationServices.getLongitude());
        }

        userReference.child(firebaseUser.getUid()).setValue(credentialsMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        uploadProfilePicOnly();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UserDetailsActivity.this, "Updating user details unsuccessful", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadCoverPicOnly() {
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
                        coverMap.put("CoverURL", coverUri);

                        userReference.child(firebaseUser.getUid()).updateChildren(coverMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        startActivity(new Intent(UserDetailsActivity.this, TimelineSetupActivity.class));
                                        progressDialog.dismiss();
                                        Toast.makeText(UserDetailsActivity.this, "Welcome!!! :)", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UserDetailsActivity.this, "Could not upload cover picture", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }else{
            Toast.makeText(this, "Something went wrong with updating cover picture", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfilePicOnly() {
        if (myUri != null){
            final StorageReference picFileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));
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
                        Uri picDownloadUri = task.getResult();
                        myUri = picDownloadUri.toString();

                        HashMap<String, Object> picMap = new HashMap<>();
                        picMap.put("ImageURL", myUri);

                        userReference.child(firebaseUser.getUid()).updateChildren(picMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        uploadCoverPicOnly();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UserDetailsActivity.this, "Could not upload profile picture", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }else{
            Toast.makeText(this, "something went wrong with uploading profile pic", Toast.LENGTH_SHORT).show();
        }
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

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(contentResolver.getType(uri));
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
