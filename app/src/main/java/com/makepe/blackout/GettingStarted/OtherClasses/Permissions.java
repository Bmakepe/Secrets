package com.makepe.blackout.GettingStarted.OtherClasses;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class Permissions {
    private Context context;

    private final String[] requiredPermissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.VIBRATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.READ_PHONE_STATE

    };

    public static final int PERMISSION_MULTIPLE_REQUEST = 123;

    public Permissions(Context context) {
        this.context = context;
    }

    public void verifyPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            ActivityCompat.requestPermissions((Activity) context, requiredPermissions, PERMISSION_MULTIPLE_REQUEST);
        }else{
            Toast.makeText(context, "Permissions Checked", Toast.LENGTH_SHORT).show();
        }
    }
}
