package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Models.NewsHeadlines;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewsDetailsActivity extends AppCompatActivity {

    private NewsHeadlines headlines;
    private TextView text_title, text_author, text_timestamp, text_detail, text_content, text_source;
    private ImageView img_news;
    private CircleImageView newsDetailProPic;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);

        Toolbar newsDetailsToolbar = findViewById(R.id.news_details_toolbar);
        setSupportActionBar(newsDetailsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        text_title = findViewById(R.id.news_detail_title);
        text_author = findViewById(R.id.news_detail_author);
        text_source = findViewById(R.id.news_detail_source);
        text_timestamp = findViewById(R.id.news_detail_timestamp);
        text_detail = findViewById(R.id.text_detail_detail);
        text_content = findViewById(R.id.text_detail_content);
        img_news = findViewById(R.id.news_detail_image);
        newsDetailProPic = findViewById(R.id.newsDetailProPic);

        headlines = (NewsHeadlines) getIntent().getSerializableExtra("data");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        text_title.setText(headlines.getTitle());
        text_author.setText(headlines.getAuthor());
        text_source.setText(headlines.getSource().getName());
        text_detail.setText(headlines.getDescription());
        text_content.setText(headlines.getContent());

        try {

            String date = headlines.getPublishedAt();
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
            Date parsedDate = inputFormat.parse(date);
            String formattedDate = outputFormat.format(parsedDate);

            text_timestamp.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (headlines.getUrlToImage() != null)
            try{
                Picasso.get().load(headlines.getUrlToImage()).into(img_news);
            }catch (NullPointerException ignored){}

        getMyUserDetails();
    }

    private void getMyUserDetails() {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUserID().equals(firebaseUser.getUid()))
                        try{
                            Picasso.get().load(user.getImageURL()).into(newsDetailProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(newsDetailProPic);
                        }
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