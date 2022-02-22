package com.makepe.blackout.GettingStarted.OtherClasses;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.database.FirebaseDatabase;
import com.makepe.blackout.GettingStarted.InAppActivities.HomeConsoleActivity;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

@SuppressLint("Registered")
public class BlackOut extends MultiDexApplication {

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);

        MultiDex.install(BlackOut.this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }
}
