package com.makepe.blackout.GettingStarted.OtherClasses;

import android.Manifest;
import android.app.Activity;
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

import com.makepe.blackout.GettingStarted.InAppActivities.AddStoryActivity;

import java.util.List;
import java.util.Locale;

public class LocationServices implements LocationListener {

    //for Location services
    private String[] locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    private LocationManager locationManager;
    private double latitude, longitude;
    public static final int LOCATION_REQUEST_CODE = 100;

    private TextView locationTV;
    private EditText locationET;

    private Context context;

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

    public void getMyLocation(){
        if (checkLocationPermission()){
            detectLocation();
        }else{
            ActivityCompat.requestPermissions((Activity) context, locationPermission, LOCATION_REQUEST_CODE);
        }
    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void detectLocation() {
        Toast.makeText(context, "Please wait... Detecting your location", Toast.LENGTH_LONG).show();

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
        //find address, country, state, city

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        try{
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0);//complete address

            locationTV.setText(address);
            locationET.setText(address);

        }catch (Exception ignored){ }

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
}
