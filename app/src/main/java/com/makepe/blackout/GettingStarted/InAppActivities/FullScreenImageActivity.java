package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

public class FullScreenImageActivity extends AppCompatActivity {

    ImageView backBTN, fullScreenImage;
    DatabaseReference userRef, postRef;

    String itemID, reason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        backBTN = findViewById(R.id.imageBackBTN);
        fullScreenImage = findViewById(R.id.fullScreenImage);

        Intent intent = getIntent();
        itemID = intent.getStringExtra("itemID");
        reason = intent.getStringExtra("reason");

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        postRef = FirebaseDatabase.getInstance().getReference("Posts");

        switch(reason){
            case "userImage":
                getProfileImage();
                break;

            case "coverImage":
                getCoverImage();
                break;

            case "postImage":
                getPostImage();
                break;

            default:
                Toast.makeText(this, "Illegal statement", Toast.LENGTH_SHORT).show();
        }
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void getPostImage() {
        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    if (postModel.getPostID().equals(itemID))
                        try{
                            Picasso.get().load(postModel.getPostImage()).into(fullScreenImage);
                        }catch (NullPointerException ignored){}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCoverImage() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUSER_ID().equals(itemID))
                        try{
                            Picasso.get().load(user.getCoverURL()).into(fullScreenImage);
                        }catch (NullPointerException ignored){}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getProfileImage() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUSER_ID().equals(itemID))
                        try{
                            Picasso.get().load(user.getImageURL()).into(fullScreenImage);
                        }catch (NullPointerException ignored){}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}