package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity implements LocationListener {

    private ImageView coverPic, backBTN, changeCoverPic;
    private CircleImageView profilePic;
    private TextView updateTV;
    private EditText nameET, biographyET, locationET;
    private RelativeLayout changePicArea;

    private FirebaseUser firebaseUser;
    private DatabaseReference userRef;
    private StorageReference storageReference;

    private Uri proPicUri, coverPicUri;
    private StorageTask uploadTask, coverUploadTask;
    private String myUri = "", coverUri = "", name, bio;

    private String choice;

    private ProgressDialog progressDialog;

    private String[] locationPermission;
    private LocationManager locationManager;
    private double latitude, longitude;
    public static final int LOCATION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        coverPic = findViewById(R.id.editProCoverPic);
        backBTN = findViewById(R.id.editBackBTN);
        profilePic = findViewById(R.id.editProUserImage);
        updateTV = findViewById(R.id.updateEditProBTN);
        nameET = findViewById(R.id.editProFullName);
        biographyET = findViewById(R.id.editProBiography);
        changeCoverPic = findViewById(R.id.editCoverPic);
        changePicArea = findViewById(R.id.changePicArea);
        locationET = findViewById(R.id.editLocationET);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference("pro_pics");

        progressDialog = new ProgressDialog(this);

        getUserDetails();

        locationET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkLocationPermission()){
                    detectLocation();
                }else{
                    ActivityCompat.requestPermissions(EditProfileActivity.this, locationPermission, LOCATION_REQUEST_CODE);
                }
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
                        .setAspectRatio(21,9)
                        .start(EditProfileActivity.this);

                choice = "coverPic";
            }
        });

        updateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserDetails();
            }
        });

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void detectLocation() {
        Toast.makeText(this, "Please wait... Detecting your location", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void findAddress() {
        //find address, country, state, city

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try{
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0);//complete address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            String myLocation = address + ", " + city + ", " + state + ", " + country;

            locationET.setText(myLocation);

        }catch (Exception e){
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void updateUserDetails() {

        name = nameET.getText().toString().trim();
        bio = biographyET.getText().toString().trim();

        if (TextUtils.isEmpty(name)){
            nameET.setError("Enter Your Name here");
            nameET.requestFocus();
        }else if (TextUtils.isEmpty(bio)){
            biographyET.setError("Tell us more about yourself");
            biographyET.requestFocus();
        }else{
            Toast.makeText(this, "You will be able to update profile details", Toast.LENGTH_SHORT).show();
            uploadCoverPicOnly();
            uploadProfilePicOnly();
            updateUserCredentials();
        }
    }

    private void uploadProfilePicOnly(){
        if (myUri != null){
            final StorageReference picFileReference = storageReference.child(System.currentTimeMillis()
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

                        HashMap<String, Object> picMap = new HashMap<>();
                        picMap.put("ImageURL", myUri);

                        userRef.child(firebaseUser.getUid()).updateChildren(picMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(EditProfileActivity.this, "Successfully Uploaded profile Picture", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditProfileActivity.this, "Could not upload profile picture", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }else{
            Toast.makeText(this, "something went wrong with uploading profile pic", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadCoverPicOnly(){
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

                        userRef.child(firebaseUser.getUid()).updateChildren(coverMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(EditProfileActivity.this, "Successfully updated cover pic", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditProfileActivity.this, "Could not upload cover picture", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }else{
            Toast.makeText(this, "Something went wrong with updating cover picture", Toast.LENGTH_SHORT).show();
        }

    }

    private void updateUserCredentials(){
        HashMap<String, Object> credentialsMap = new HashMap<>();
        credentialsMap.put("Username", name);
        credentialsMap.put("Bio", bio);

        if (!TextUtils.isEmpty(locationET.getText().toString())) {
            credentialsMap.put("latitude", latitude);
            credentialsMap.put("longitude", longitude);
        }

        userRef.child(firebaseUser.getUid()).updateChildren(credentialsMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(EditProfileActivity.this, "User details have been updated", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfileActivity.this, "Updating user details unsuccessful", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserDetails() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUSER_ID().equals(firebaseUser.getUid())){
                        nameET.setText(user.getUsername());
                        biographyET.setText(user.getBio());

                        try{
                            Picasso.get().load(user.getImageURL()).into(profilePic);
                            Picasso.get().load(user.getCoverURL()).into(coverPic);
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
}