package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

public class FullScreenPictureActivity extends AppCompatActivity {
    private ImageView fullScreenImage;
    private Toolbar toolbar;

    private String imageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        fullScreenImage = findViewById(R.id.fullScreenImage);
        toolbar = findViewById(R.id.fullScreenImageToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent imageIntent = getIntent();
        imageURL = imageIntent.getStringExtra("imageURL");

        try{
            Picasso.get().load(imageURL).fit().into(fullScreenImage);
        }catch (NullPointerException ignored){}
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}