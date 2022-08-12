package com.makepe.blackout.GettingStarted.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.InAppActivities.NewsDetailsActivity;
import com.makepe.blackout.GettingStarted.Models.NewsHeadlines;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.SelectListener;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private Context context;
    private List<NewsHeadlines> newsHeadlines;
    private SelectListener listener;

    public static final int ITEM_NEWS_WITH_IMAGES = 100;
    public static final int ITEM_NEWS_WITHOUT_IMAGES = 200;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference;

    public NewsAdapter(Context context, List<NewsHeadlines> newsHeadlines) {
        this.context = context;
        this.newsHeadlines = newsHeadlines;
    }

    public NewsAdapter(Context context, List<NewsHeadlines> newsHeadlines, SelectListener listener) {
        this.context = context;
        this.newsHeadlines = newsHeadlines;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case ITEM_NEWS_WITH_IMAGES:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.news_item_layout, parent, false));
            case ITEM_NEWS_WITHOUT_IMAGES:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.news_item_no_images_layout, parent, false));
            default:
                throw new IllegalStateException("Unexpected value " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsHeadlines headlines = newsHeadlines.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        getNewsDetails(holder, headlines);
        getMyDetails(holder);

        holder.likesArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Likes", Toast.LENGTH_SHORT).show();
            }
        });

        holder.shareArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Share", Toast.LENGTH_SHORT).show();
            }
        });

        holder.bookmarkArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Save", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.OnNewsClicked(headlines);
            }
        });
    }

    private void getMyDetails(ViewHolder holder) {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUserID().equals(firebaseUser.getUid()))
                        try{
                            Picasso.get().load(user.getImageURL()).into(holder.myProfilePic);
                        }catch (NullPointerException ignored){}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getNewsDetails(ViewHolder holder, NewsHeadlines headlines) {

        holder.text_title.setText(headlines.getTitle());
        holder.text_source.setText(headlines.getSource().getName());

        try {

            String date = headlines.getPublishedAt();
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
            Date parsedDate = inputFormat.parse(date);
            String formattedDate = outputFormat.format(parsedDate);

            holder.news_timestamp.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (headlines.getUrlToImage() != null)
            try{
                Picasso.get().load(headlines.getUrlToImage()).into(holder.img_headline);
            }catch (IllegalArgumentException ignored){}

        holder.newsLoader.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return newsHeadlines.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView text_title, text_source, news_timestamp;
        public ImageView img_headline;
        public ProgressBar newsLoader;

        public CircleImageView myProfilePic;
        public RelativeLayout likesArea, commentsArea, shareArea, bookmarkArea;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            text_title = itemView.findViewById(R.id.newsHeadline);
            text_source = itemView.findViewById(R.id.newsSourceTV);
            img_headline = itemView.findViewById(R.id.newsImageView);
            newsLoader = itemView.findViewById(R.id.newsLoader);
            news_timestamp = itemView.findViewById(R.id.news_timestamp);

            myProfilePic = itemView.findViewById(R.id.commentAreaProPic);
            likesArea = itemView.findViewById(R.id.likesArea);
            commentsArea = itemView.findViewById(R.id.commentLayout);
            shareArea = itemView.findViewById(R.id.shareArea);
            bookmarkArea = itemView.findViewById(R.id.saveArea);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (newsHeadlines.get(position).getUrlToImage() != null)
            return ITEM_NEWS_WITH_IMAGES;
        else
            return ITEM_NEWS_WITHOUT_IMAGES;
    }
}
