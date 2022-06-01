package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Fragments.PodcastFragment;
import com.makepe.blackout.GettingStarted.Fragments.PhotoFragment;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ViewPagerAdapter;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class CameraActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView cameraProPic;
    private DatabaseReference userReference;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        toolbar = findViewById(R.id.cameraToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cameraProPic = findViewById(R.id.cameraProPic);

        TabLayout cameraTabLayout = findViewById(R.id.cameraTabLayout);
        ViewPager cameraViewPager = findViewById(R.id.cameraViewPager);

        userReference = FirebaseDatabase.getInstance().getReference("Users");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new PhotoFragment(), "Media");
        viewPagerAdapter.addFragment(new PodcastFragment(), "Podcast");
        cameraViewPager.setAdapter(viewPagerAdapter);
        cameraTabLayout.setupWithViewPager(cameraViewPager);

        getUserDetails();

    }

    private void getUserDetails() {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUSER_ID().equals(firebaseUser.getUid()))
                        try{
                            Picasso.get().load(user.getImageURL()).into(cameraProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(cameraProPic);
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
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.camera_menu, menu);
        //menu.add(0, 1, 1, menuIconWithText(getResources().getString(R.string.skip), getResources().getDrawable(R.drawable.ic_baseline_arrow_forward_24)));

        return true;
    }

    private CharSequence menuIconWithText(String title, Drawable drawable) {
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        SpannableString sb = new SpannableString(title + "    " );
        ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.skipSearch:
                startActivity(new Intent(CameraActivity.this, PostActivity.class));
                return true;

            default:
                Toast.makeText(this, "Unknown Selection", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}