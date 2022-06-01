package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Models.CommentModel;
import com.makepe.blackout.GettingStarted.Models.GroupsModel;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

public class FullScreenImageActivity extends AppCompatActivity {

    private ImageView fullScreenImage;
    private Toolbar toolbar;
    private DatabaseReference userRef, postRef, groupRef, commentReference;

    private String itemID, reason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        fullScreenImage = findViewById(R.id.fullScreenImage);
        toolbar = findViewById(R.id.fullScreenImageToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        itemID = intent.getStringExtra("itemID");
        reason = intent.getStringExtra("reason");

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        postRef = FirebaseDatabase.getInstance().getReference("Posts");
        groupRef = FirebaseDatabase.getInstance().getReference("SecretGroups");
        commentReference = FirebaseDatabase.getInstance().getReference("Comments");

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

            case "groupProPic":
                getGroupProPic();
                break;

            case "groupCoverPic":
                getGroupCoverPic();
                break;

            case "commentImage":
                getCommentImage();
                break;

            default:
                Toast.makeText(this, "Illegal statement", Toast.LENGTH_SHORT).show();
        }

    }

    private void getCommentImage() {
        commentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){

                    if (ds.child(itemID).exists()){
                        CommentModel model = ds.getValue(CommentModel.class);
                        try{
                            assert model != null;
                            Picasso.get().load(model.getCommentImage()).into(fullScreenImage);
                        }catch (NullPointerException ignored){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getGroupCoverPic() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    GroupsModel group = ds.getValue(GroupsModel.class);

                    assert group != null;
                    if (group.getGroupID().equals(itemID)){
                        try{
                            Picasso.get().load(group.getGroupCoverPic()).into(fullScreenImage);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(fullScreenImage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getGroupProPic() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    GroupsModel group = ds.getValue(GroupsModel.class);

                    assert group != null;
                    if (group.getGroupID().equals(itemID)){
                        try{
                            Picasso.get().load(group.getGroupProPic()).into(fullScreenImage);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(fullScreenImage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}