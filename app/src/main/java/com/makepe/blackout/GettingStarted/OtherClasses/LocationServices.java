package com.makepe.blackout.GettingStarted.OtherClasses;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makepe.blackout.GettingStarted.InAppActivities.AddStoryActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ChatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class LocationServices implements LocationListener {

    //for Location services
    private String[] locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    private LocationManager locationManager;
    private double latitude, longitude;
    public static final int LOCATION_REQUEST_CODE = 100;
    private final DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Chats");
    private final DatabaseReference groupChatReference = FirebaseDatabase.getInstance().getReference("GroupChats");
    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    private TextView locationTV;
    private EditText locationET;

    private Context context;

    private String receiverID;
    private boolean sendMessage = false, locationDetected = false, groupMessage = false;

    private ProgressDialog locationDialog;

    public LocationServices() {
    }

    public LocationServices(TextView locationTV, Context context) {
        this.locationTV = locationTV;
        this.context = context;
    }

    public LocationServices(EditText locationET, Context context) {
        this.locationET = locationET;
        this.context = context;
    }

    public LocationServices(Context context, String receiverID) {
        this.context = context;
        this.receiverID = receiverID;
    }

    public void getMyLocation(){
        if (checkLocationPermission()){
            detectLocation();
        }else{
            ActivityCompat.requestPermissions((Activity) context, locationPermission, LOCATION_REQUEST_CODE);
        }
    }

    public void sendMyLocation(){
        getMyLocation();
        sendMessage = true;
    }

    public void sendMyGroupLocation(){
        getMyLocation();
        groupMessage = true;
    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void detectLocation() {
        locationDialog = new ProgressDialog(context);
        locationDialog.setMessage("Detecting Location, Please wait...");
        locationDialog.show();

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                this);

    }

    private void findAddress() {

        if (!sendMessage && !groupMessage) {
            //find address, country, state, city

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);

                String address = addresses.get(0).getAddressLine(0);//complete address

                locationDetected = true;

                locationDialog.dismiss();

                locationTV.setText(address);
                locationET.setText(address);

            } catch (Exception ignored) {}

        }else if (sendMessage){
            String chatID = chatReference.push().getKey();
            assert firebaseUser != null;

            HashMap<String, Object> messageMap = new HashMap<>();
            messageMap.put("chatID", chatID);
            messageMap.put("sender", firebaseUser.getUid());
            messageMap.put("receiver", receiverID);
            messageMap.put("isSeen", false);
            messageMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
            messageMap.put("message_type", "location");
            messageMap.put("latitude", getLatitude());
            messageMap.put("longitude", getLongitude());

            assert chatID != null;
            chatReference.child(chatID).setValue(messageMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            sendMessage = false;

                            locationDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Unable to share location", Toast.LENGTH_SHORT).show();
                        }
                    });
        }else {
            String chatID = groupChatReference.push().getKey();
            assert firebaseUser != null;

            HashMap<String, Object> groupChatMap = new HashMap<>();

            groupChatMap.put("groupID", receiverID);
            groupChatMap.put("chatID", chatID);
            groupChatMap.put("senderID", firebaseUser.getUid());
            groupChatMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
            groupChatMap.put("msg_type", "location");
            groupChatMap.put("latitude", getLatitude());
            groupChatMap.put("longitude", getLongitude());

            assert chatID != null;
            groupChatReference.child(chatID).setValue(groupChatMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            groupMessage = false;

                            locationDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Unable to share location", Toast.LENGTH_SHORT).show();

                        }
                    });

        }

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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isLocationDetected() {
        return locationDetected;
    }

    public void setLocationDetected(boolean locationDetected) {
        this.locationDetected = locationDetected;
    }
}
