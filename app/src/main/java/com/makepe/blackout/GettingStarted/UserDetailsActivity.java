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
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.makepe.blackout.GettingStarted.InAppActivities.HomeConsoleActivity;
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
    CircleImageView setupProfilePic;
    TextInputEditText setupUsername, setupBiography;
    TextView continueBTN;
    CheckBox maleCheck, femaleCheck, termsCheck;
    ProgressDialog progressDialog;
    EditText dateOfBirth;
    String uid;

    Uri imageUri;
    String myUri = "";
    StorageTask uploadTask;

    private FirebaseAuth mAuth;
    FirebaseUser firebaseUser;

    StorageReference storageReference;

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
        termsCheck = findViewById(R.id.terms_conditions);
        dateOfBirth= findViewById(R.id.dateOfBirthPicker);

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

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("pro_pics");

        progressDialog = new ProgressDialog(this);

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
                new DatePickerDialog(UserDetailsActivity.this,  date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        setupProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(UserDetailsActivity.this);
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void updateDOB(Calendar myCalendar, EditText dateOfBirth) {
        //function to update the DOB label with the date you picked

        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        dateOfBirth.setText("Date of Birth: " + sdf.format(myCalendar.getTime()));
    }

    private void confirmUserInfo() {
        final FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        final String Name = Objects.requireNonNull(setupUsername.getText()).toString();
        final String Bio = Objects.requireNonNull(setupBiography.getText()).toString();
        final String boy = "Male", girl = "Female", terms = "Accepted";
        final String DOB = dateOfBirth.getText().toString();
        final boolean maleChecked = maleCheck.isChecked();
        final boolean femaleChecked = femaleCheck.isChecked();
        final boolean acceptTerms = termsCheck.isChecked();

        if(Name.isEmpty()){
            setupUsername.setError("Enter Your Username");
            setupUsername.requestFocus();
        }else if(Bio.isEmpty()){
            setupBiography.setError("Tell Us Something About Yourself");
            setupBiography.requestFocus();
        }else if(!maleChecked && !femaleChecked){
            Toast.makeText(UserDetailsActivity.this, "Please Select Your Gender", Toast.LENGTH_SHORT).show();
        }else if(!acceptTerms) {
            Toast.makeText(UserDetailsActivity.this, "Please Accept The Terms", Toast.LENGTH_SHORT).show();
        }else if(DOB.isEmpty()){
                Toast.makeText(UserDetailsActivity.this, "Please Pick Date of Birth", Toast.LENGTH_SHORT).show();
            }else{
            progressDialog.setMessage("Setting Up Your Profile");
            progressDialog.show();

            if(imageUri != null){
                final StorageReference filereference = storageReference.child(System.currentTimeMillis()
                + "." +getFileExtension(imageUri));

                uploadTask = filereference.putFile(imageUri);

                uploadTask.continueWithTask(new Continuation(){
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filereference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadUri = task.getResult();
                            myUri = downloadUri.toString();

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

                            HashMap<Object, String> hashMap = new HashMap<>();

                            hashMap.put("USER_ID", uid);
                            hashMap.put("Username", Name);
                            hashMap.put("Bio", Bio);
                            hashMap.put("ImageURL", myUri);
                            hashMap.put("CoverURL", "");
                            hashMap.put("DOB", DOB);
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("Number", user.getPhoneNumber());

                            if(maleChecked){
                                hashMap.put("Gender", boy);
                            }else if(femaleChecked){
                                hashMap.put("Gender", girl);
                            }

                            if(acceptTerms){
                                hashMap.put("Terms", terms);
                            }

                            reference.child(uid).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    startActivity(new Intent(UserDetailsActivity.this, HomeConsoleActivity.class));
                                    progressDialog.dismiss();
                                }
                            });

                        }else{
                            Toast.makeText(UserDetailsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                Toast.makeText(this, "Please Select Profile Pic", Toast.LENGTH_SHORT).show();
            }
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
            imageUri = result.getUri();

            setupProfilePic.setImageURI(imageUri);
        }else{
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            /*User is signed in stay on this activity
            set phone number of logged in user
             */
            uid = user.getUid();

        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
