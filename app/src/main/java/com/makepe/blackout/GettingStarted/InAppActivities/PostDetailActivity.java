package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Adapters.CommentsAdapter;
import com.makepe.blackout.GettingStarted.Fragments.ProfileFragment;
import com.makepe.blackout.GettingStarted.Models.CommentModel;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailActivity extends AppCompatActivity {

    private TextView hisUsername, hisPostTimeStamp, postCaption, likesCounter, commentCounter, postCommentBTN;
    private ImageView backBTN, postMenuBTN, postImage, likesBTN, savePostBTN;
    private EditText commentCaption;
    private CircleImageView hisProfilePic, myProfilePic;
    private LinearLayout postLocationArea;
    private CardView postImageArea;
    private ProgressBar imageLoader;
    private RelativeLayout likesArea, shareArea;
    private RecyclerView commentsRecycler;

    //for shared moment area
    private CircleImageView sharedPostProPic;
    private TextView sharedPostUsername, sharedPostDate, sharedPostCheckIn, sharedPostDesc;
    private ProgressBar sharedImageProgressBar, sharedVideoProgressBar;
    private ImageView sharedPostImage, volumeBTN;
    private CardView sharedPost, imageAreaCard, videoAreaCard;
    private VideoView postDetailVideoView;

    private DatabaseReference postReference, userReference, commentsReference, notificationsReference;
    private FirebaseUser firebaseUser;

    private String postID, userID, postImageURL;
    private List<ContactsModel> phoneNumbers;

    private GetTimeAgo timeAgo;
    private UniversalFunctions universalFunctions;

    private List<CommentModel> commentList;
    private CommentsAdapter commentsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        backBTN = findViewById(R.id.postDetail_backBTN);
        hisProfilePic = findViewById(R.id.postDetail_his_moment_image_user);
        hisUsername = findViewById(R.id.postDetail_moment_username);
        hisPostTimeStamp = findViewById(R.id.postDetail_timestamp);
        postMenuBTN = findViewById(R.id.postDetail_postMenuBTN);
        postImageArea = findViewById(R.id.pd_postImageArea);
        imageLoader = findViewById(R.id.pd_progress_load_photo);
        postImage = findViewById(R.id.pd_postImage);
        postCaption = findViewById(R.id.pd_Caption);
        likesArea = findViewById(R.id.pd_likesArea);
        likesBTN = findViewById(R.id.pd_post_likes_btn);
        likesCounter = findViewById(R.id.pd_likesCounter);
        commentCounter = findViewById(R.id.pd_comment_counter);
        shareArea = findViewById(R.id.pd_shareArea);
        savePostBTN = findViewById(R.id.pd_bookmark_btn);
        commentsRecycler = findViewById(R.id.recycler_comments);
        myProfilePic = findViewById(R.id.my_image_profile);
        commentCaption = findViewById(R.id.add_comment);
        postCommentBTN = findViewById(R.id.postCommentBTN);

        //for shared moments area
        sharedPostProPic = findViewById(R.id.shared_moment_image_user);
        sharedPostUsername = findViewById(R.id.shared_moment_username);
        sharedPostDate = findViewById(R.id.shared_moment_postDate);
        sharedPostCheckIn = findViewById(R.id.shared_moment_postCheckIn);
        sharedPostDesc = findViewById(R.id.shared_moment_post_desc);
        sharedImageProgressBar = findViewById(R.id.shared_moment_progress_load_media);
        sharedVideoProgressBar = findViewById(R.id.sharedPostDetailVideoItemLoader);
        sharedPostImage = findViewById(R.id.shared_moment_postImage);
        sharedPost = findViewById(R.id.shared_post_detail_item);
        imageAreaCard = findViewById(R.id.shared_moment_postPicArea);
        videoAreaCard = findViewById(R.id.sharedPostDetailVideoArea);
        postDetailVideoView = findViewById(R.id.sharedPostDetailVideoView);
        volumeBTN = findViewById(R.id.sharedPostDetailItemVolumeBTN);

        Intent intent = getIntent();
        postID = intent.getStringExtra("postID");
        timeAgo = new GetTimeAgo();
        universalFunctions = new UniversalFunctions(this);

        postReference = FirebaseDatabase.getInstance().getReference("Posts");
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        commentsReference = FirebaseDatabase.getInstance().getReference("Comments");
        notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        List<ContactsModel> phoneBook = new ArrayList<>();
        ContactsList contactsList = new ContactsList(phoneBook, this);
        contactsList.readContacts();
        phoneNumbers = contactsList.getContactsList();

        commentList = new ArrayList<>();
        commentsRecycler.setHasFixedSize(true);
        commentsRecycler.setLayoutManager(new LinearLayoutManager(this));

        getMyDetails();
        getPostDetails();
        readComments();
        universalFunctions.isLiked(postID, likesBTN);
        universalFunctions.nrLikes(likesCounter, postID);
        universalFunctions.getCommentsCount(postID, commentCounter);
        universalFunctions.isSaved(postID, savePostBTN);

        likesArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(PostDetailActivity.this, "You will be able to like this post", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PostDetailActivity.this, ConnectionsActivity.class);
                intent.putExtra("UserID", postID);
                intent.putExtra("Interaction", "Likes");
                startActivity(intent);
            }
        });
        
        shareArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(PostDetailActivity.this, SharePostActivity.class);
                shareIntent.putExtra("postID", postID);
                startActivity(shareIntent);
            }
        });

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        postCommentBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(PostDetailActivity.this, "You will be able to post a comment soon", Toast.LENGTH_SHORT).show();
                if (commentCaption.getText().toString().equals("")){
                    Toast.makeText(PostDetailActivity.this, "You Can't Send Empty Comment", Toast.LENGTH_SHORT).show();
                }else{
                    addComment();
                }
            }
        });

        postMenuBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(PostDetailActivity.this, postMenuBTN, Gravity.END);

                if(!userID.equals(firebaseUser.getUid()))
                    popupMenu.getMenu().add(Menu.NONE, 0,0, "View Profile");
                if (userID.equals(firebaseUser.getUid())){
                    popupMenu.getMenu().add(Menu.NONE, 1,0, "Delete Post");
                    popupMenu.getMenu().add(Menu.NONE, 2,0, "Edit Post");
                    popupMenu.getMenu().add(Menu.NONE, 3,0, "Post Views");
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case 0:
                                Intent intent = new Intent(PostDetailActivity.this, ViewProfileActivity.class);
                                intent.putExtra("uid", userID);
                                startActivity(intent);
                                break;

                            case 1:
                                universalFunctions.beginDelete(postID, postImageURL);
                                break;

                            case 2:
                                Toast.makeText(PostDetailActivity.this, "You will be able to edit this post", Toast.LENGTH_SHORT).show();
                                break;

                            case 3:
                                Toast.makeText(PostDetailActivity.this, "You will be able to see who viewed this post", Toast.LENGTH_SHORT).show();
                                Intent intent1 = new Intent(PostDetailActivity.this, ConnectionsActivity.class);
                                intent1.putExtra("UserID", postID);
                                intent1.putExtra("Interaction", "Views");
                                startActivity(intent1);
                                break;

                            default:
                                Toast.makeText(PostDetailActivity.this, "Unknown Selection", Toast.LENGTH_SHORT).show();
                        }

                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent1 = new Intent(PostDetailActivity.this, FullScreenImageActivity.class);
                intent1.putExtra("itemID", postID);
                intent1.putExtra("reason", "postImage");
                startActivity(intent1);
            }
        });

        savePostBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(savePostBTN.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(postID).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(postID).removeValue();
                }
            }
        });

    }

    private void readComments() {
        commentsReference.child(postID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    CommentModel comments = ds.getValue(CommentModel.class);

                    commentList.add(comments);

                }
                commentsAdapter = new CommentsAdapter(PostDetailActivity.this, commentList);
                commentsRecycler.setAdapter(commentsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addComment() {
        final String timeStamp = String.valueOf(System.currentTimeMillis());

        String commentID = commentsReference.push().getKey();

        HashMap<String, Object> commentMap = new HashMap();
        commentMap.put("cID", commentID);
        commentMap.put("comment", commentCaption.getText().toString());
        commentMap.put("timeStamp", timeStamp);
        commentMap.put("userID", firebaseUser.getUid());

        commentsReference.child(postID).child(commentID).setValue(commentMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (userID.equals(firebaseUser.getUid()))
                            sendCommentNotification();
                        commentCaption.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostDetailActivity.this, "Comment Unsuccessful", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void sendCommentNotification() {
        String timeStamp = String.valueOf(System.currentTimeMillis());

        String notificationID = notificationsReference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("notificationID", notificationID);
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "commented: " + commentCaption.getText().toString());
        hashMap.put("postid", postID);
        hashMap.put("ispost", true);
        hashMap.put("timeStamp", timeStamp);

        assert notificationID != null;
        notificationsReference.child(userID).child(notificationID).setValue(hashMap);
    }

    private void getPostDetails() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    assert postModel != null;
                    if (postModel.getPostID().equals(postID)){

                        userID = postModel.getUserID();
                        postImageURL = postModel.getPostImage();
                        postCaption.setText(postModel.getPostCaption());

                        try{
                            if (postModel.getPostPrivacy().equals("Private")){
                                shareArea.setVisibility(View.GONE);
                            }
                        }catch (NullPointerException ignored){}

                        try{
                            universalFunctions.findAddress(postModel, sharedPostCheckIn);
                        }catch (Exception ignored){}

                        try{
                            String timeStamp = timeAgo.getTimeAgo(Long.parseLong(postModel.getPostTime()), PostDetailActivity.this);
                            hisPostTimeStamp.setText(timeStamp);
                        }catch (NumberFormatException ignored){}

                        switch (postModel.getPostType()){
                            case "textPost":
                                postImageArea.setVisibility(View.GONE);
                                imageLoader.setVisibility(View.GONE);
                                break;

                            case "imagePost":
                                try{
                                    Picasso.get().load(postModel.getPostImage()).into(postImage);
                                    postImageArea.setVisibility(View.VISIBLE);
                                    imageLoader.setVisibility(View.GONE);
                                }catch (NullPointerException ignored){}
                                break;

                            case "videoPost":
                                break;

                            case "sharedTextPost":
                                getSharedPostDetails(postModel);
                                break;

                            case "sharedVideoPost":
                                getSharedPostDetails(postModel);
                                break;

                            default:
                                Toast.makeText(PostDetailActivity.this, "Unknown Post Type", Toast.LENGTH_SHORT).show();
                        }

                        userReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dss : snapshot.getChildren()){
                                    User user = dss.getValue(User.class);

                                    assert user != null;
                                    if (user.getUSER_ID().equals(postModel.getUserID())){

                                        try{
                                            Picasso.get().load(user.getImageURL()).into(hisProfilePic);
                                        }catch (NullPointerException e){
                                            Picasso.get().load(R.drawable.default_profile_display_pic).into(hisProfilePic);
                                        }

                                        for (ContactsModel cm : phoneNumbers){
                                            if (firebaseUser.getUid().equals(user.getUSER_ID())){
                                                hisUsername.setText("Me");
                                            }else if (cm.getNumber().equals(user.getNumber())){
                                                hisUsername.setText(cm.getUsername());
                                            }else{
                                                hisUsername.setText(user.getUsername());
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSharedPostDetails(PostModel postModel) {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel post = ds.getValue(PostModel.class);

                    if (post.getPostID().equals(postModel.getSharedPost())){
                        sharedPostDesc.setText(post.getPostCaption());

                        try{
                            universalFunctions.findAddress(post, sharedPostCheckIn);
                        }catch (Exception ignored){}

                        try{
                            String timeStamp = timeAgo.getTimeAgo(Long.parseLong(post.getPostTime()), PostDetailActivity.this);
                            sharedPostDate.setText(timeStamp);
                        }catch (NumberFormatException ignored){}

                        switch (post.getPostType()){
                            case "textPost":
                                sharedPost.setVisibility(View.VISIBLE);
                                imageAreaCard.setVisibility(View.GONE);
                                videoAreaCard.setVisibility(View.GONE);
                                break;

                            case "videoPost":
                                sharedPost.setVisibility(View.VISIBLE);
                                imageAreaCard.setVisibility(View.GONE);
                                videoAreaCard.setVisibility(View.VISIBLE);

                                try{
                                    getVideoDetails(post);
                                }catch (NullPointerException ignored){}
                                break;

                            case "imagePost":
                                sharedPost.setVisibility(View.VISIBLE);
                                imageAreaCard.setVisibility(View.VISIBLE);
                                videoAreaCard.setVisibility(View.GONE);

                                try{
                                    Picasso.get().load(post.getPostImage()).into(sharedPostImage);
                                    sharedImageProgressBar.setVisibility(View.GONE);
                                }catch (NullPointerException ignored){}
                                break;

                            default:
                                Toast.makeText(PostDetailActivity.this, "unidentied post type", Toast.LENGTH_SHORT).show();
                        }


                        userReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dss : snapshot.getChildren()){
                                    User user = dss.getValue(User.class);

                                    assert user != null;
                                    if (user.getUSER_ID().equals(post.getUserID())){

                                        try{
                                            Picasso.get().load(user.getImageURL()).into(sharedPostProPic);
                                        }catch (NullPointerException e){
                                            Picasso.get().load(R.drawable.default_profile_display_pic).into(sharedPostProPic);
                                        }

                                        for (ContactsModel cm : phoneNumbers){
                                            if (firebaseUser.getUid().equals(user.getUSER_ID())){
                                                sharedPostUsername.setText("Me");
                                            }else if (cm.getNumber().equals(user.getNumber())){
                                                sharedPostUsername.setText(cm.getUsername());
                                            }else{
                                                sharedPostUsername.setText(user.getUsername());
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getVideoDetails(PostModel post) {
        postDetailVideoView.setVideoURI(Uri.parse(post.getVideoURL()));
        postDetailVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                sharedVideoProgressBar.setVisibility(View.GONE);
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(0f, 0f);

                volumeBTN.setImageResource(R.drawable.ic_baseline_volume_up_24);

                float videoRatio = mediaPlayer.getVideoWidth() / (float)mediaPlayer.getVideoHeight();
                float screenRatio = postDetailVideoView.getWidth() / (float)postDetailVideoView.getHeight();
                float scale  = videoRatio / screenRatio;
                if (scale >= 1f){
                    postDetailVideoView.setScaleX(scale);
                }else {
                    postDetailVideoView.setScaleY(1f / scale);
                }
            }
        });

        postDetailVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
    }

    private void getMyDetails() {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUSER_ID().equals(firebaseUser.getUid())){
                        try{
                            Picasso.get().load(user.getImageURL()).into(myProfilePic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(myProfilePic);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}