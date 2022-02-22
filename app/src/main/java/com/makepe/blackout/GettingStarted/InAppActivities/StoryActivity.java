package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.CommentsAdapter;
import com.makepe.blackout.GettingStarted.Models.CommentModel;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    int counter = 0;
    long pressTime = 0L;
    long limit = 500L;

    StoriesProgressView storiesProgressView;
    ImageView storyPhoto, commentBTN;
    CircleImageView storyProPic;
    TextView storyUsername, storyTimeStamp, storyCap;

    List<String> images;
    List<String> storyids;
    String userID, storyID;

    FirebaseUser firebaseUser;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;

                case MotionEvent.ACTION_UP:
                    Long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;

                case MotionEvent.AXIS_SCROLL:
            }
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        storiesProgressView = findViewById(R.id.stories);
        storyProPic = findViewById(R.id.storyProPic);
        storyPhoto = findViewById(R.id.storyImage);
        storyUsername = findViewById(R.id.storyUsername);
        storyTimeStamp = findViewById(R.id.storyTimeStamp);
        storyCap = findViewById(R.id.storyCap);
        commentBTN = findViewById(R.id.storyCommentBTN);

        userID = getIntent().getStringExtra("userid");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getStories(userID);
        displayUsersDetails(userID);

        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });

        reverse.setOnTouchListener(onTouchListener);

        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });

        skip.setOnTouchListener(onTouchListener);

        findViewById(R.id.viewStoryBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        commentBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                storiesProgressView.pause();
            }
        });

    }

    private void displayUsersDetails(final String userID) {
        List<ContactsModel> phoneBook = new ArrayList<>();
        ContactsList contactsList = new ContactsList(phoneBook, StoryActivity.this);
        contactsList.readContacts();
        final List<ContactsModel> phoneContacts = contactsList.getContactsList();

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);

        Query query = reference.orderByChild("USER_ID").equalTo(userID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String name = "" + snapshot.child("Username").getValue();
                    String proPic = "" + snapshot.child("ImageURL").getValue();
                    String number = "" + snapshot.child("Number").getValue();

                    for(ContactsModel contactsModel : phoneContacts){
                        if(userID.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            storyUsername.setText("Me");
                        }else if(contactsModel.getNumber().equals(number)){
                            storyUsername.setText(contactsModel.getUsername());
                        }else{
                            storyUsername.setText(name);
                        }
                    }

                    try{
                        Picasso.get().load(proPic).placeholder(R.drawable.default_profile_display_pic).into(storyProPic);
                    }catch (NullPointerException ignored){

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNext() {
        Glide.with(getApplicationContext()).load(images.get(++counter)).into(storyPhoto);
    }

    @Override
    public void onPrev() {
        if((counter - 1) < 0 ) return;

        Glide.with(getApplicationContext()).load(images.get(--counter)).into(storyPhoto);
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    private void getStories(String userID){
        images = new ArrayList<>();
        storyids = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userID);
        reference.keepSynced(true);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                images.clear();
                storyids.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Story story = snapshot.getValue(Story.class);
                    long timecurrent = System.currentTimeMillis();
                    String sTimeStamp = "" + snapshot.child("storyTimeStamp").getValue();
                    String storyCaption = "" + snapshot.child("storyCaption").getValue();
                    storyID = "" + snapshot.child("storyid").getValue();

                    if(storyCaption.equals("")){
                        storyCap.setVisibility(View.GONE);
                    }else{
                        storyCap.setText(storyCaption);
                    }

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());

                    try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                        calendar.setTimeInMillis(Long.parseLong(sTimeStamp));
                        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                        storyTimeStamp.setText(pTime);
                    }catch (NumberFormatException n){

                    }//for converting timestamp

                    if(timecurrent > story.getTimestart() && timecurrent < story.getTimeend()){
                        images.add(story.getPostImage());
                        storyids.add(story.getStoryid());
                    }
                }
                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(7500L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(counter);

                Glide.with(getApplicationContext()).load(images.get(counter))
                        .into(storyPhoto);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
