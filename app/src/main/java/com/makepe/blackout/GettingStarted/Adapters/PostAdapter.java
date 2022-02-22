package com.makepe.blackout.GettingStarted.Adapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makepe.blackout.GettingStarted.Fragments.ProfileFragment;
import com.makepe.blackout.GettingStarted.InAppActivities.ChatActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ConnectionsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenImageActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenVideoActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.PostDetailActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.SharePostActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.CommentModel;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyHolder> {

    private Context context;
    private List<PostModel> postList;
    private FirebaseUser firebaseUser;
    private DatabaseReference postReference, userReference;

    private UniversalFunctions universalFunctions;
    private GetTimeAgo getTimeAgo;

    public static final int TEXT_POST_ITEM = 100;
    public static final int IMAGE_POST_ITEM = 200;
    public static final int SHARED_TEXT_POST_ITEM = 300;
    public static final int VIDEO_POST_ITEM = 400;
    public static final int SHARED_VIDEO_POST_ITEM = 500;
    public static final int SHARED_IMAGE_POST_ITEM = 600;

    public PostAdapter(Context context, List<PostModel> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType){

            case TEXT_POST_ITEM:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.raw_post, parent, false));

            case VIDEO_POST_ITEM:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.raw_video_post, parent, false));

            case IMAGE_POST_ITEM:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.raw_image_post, parent, false));

            case SHARED_TEXT_POST_ITEM:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.shared_text_post_item, parent, false));

            case SHARED_IMAGE_POST_ITEM:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.shared_image_post_item, parent, false));

            case SHARED_VIDEO_POST_ITEM:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.shared_video_post_item, parent, false));

            default:
                throw new IllegalStateException("Unexpected value" + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {

        userReference = FirebaseDatabase.getInstance().getReference("Users");
        postReference = FirebaseDatabase.getInstance().getReference("Posts");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        PostModel post = postList.get(position);
        universalFunctions = new UniversalFunctions(context);
        getTimeAgo = new GetTimeAgo();

        switch (getItemViewType(position)){
            case IMAGE_POST_ITEM:
                displayImagePost(holder, post);
                break;

            case VIDEO_POST_ITEM:
                displayVideoPost(holder, post);
                break;

            case TEXT_POST_ITEM:
                displayTextPost(holder, post);
                break;

            case SHARED_TEXT_POST_ITEM:
                displayTextPost(holder, post);
                getSharedTextPost(holder, post);
                break;

            case SHARED_IMAGE_POST_ITEM:
                displayTextPost(holder, post);
                getSharedImagePost(holder, post);
                break;
                
            case SHARED_VIDEO_POST_ITEM:
                displayTextPost(holder, post);
                getSharedVideoPost(holder, post);
                break;

            default:
                Toast.makeText(context, "Unknown post type identified", Toast.LENGTH_SHORT).show();
        }

    }

    //----sharing functions
    private void getSharedVideoPost(MyHolder holder, PostModel post) {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    if (postModel.getPostID().equals(post.getSharedPost())){
                        getSharedPostOwner(holder, postModel);
                        holder.sharedPostDesc.setText(postModel.getPostCaption());

                        try{
                            universalFunctions.findAddress(postModel, holder.sharedPostCheckIn);
                        }catch (Exception ignored){}

                        try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                            String timeStamp = getTimeAgo.getTimeAgo(Long.parseLong(postModel.getPostTime()), context);
                            holder.sharedPostDate.setText(timeStamp);
                        }catch (NumberFormatException n){
                            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                        }//for converting timestamp

                        try{
                            holder.sharedPostVideoView.setVideoURI(Uri.parse(postModel.getVideoURL()));
                            holder.sharedPostVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mediaPlayer) {
                                    mediaPlayer.start();
                                    holder.sharedProgressBar.setVisibility(View.GONE);
                                    mediaPlayer.setLooping(true);
                                    mediaPlayer.setVolume(0f, 0f);

                                    holder.sharedPostVolumeBTN.setImageResource(R.drawable.ic_baseline_volume_up_24);

                                    float videoRatio = mediaPlayer.getVideoWidth() / (float)mediaPlayer.getVideoHeight();
                                    float screenRatio = holder.sharedPostVideoView.getWidth() / (float)holder.sharedPostVideoView.getHeight();
                                    float scale  = videoRatio / screenRatio;
                                    if (scale >= 1f){
                                        holder.sharedPostVideoView.setScaleX(scale);
                                    }else {
                                        holder.sharedPostVideoView.setScaleY(1f / scale);
                                    }
                                }
                            });

                            holder.sharedPostVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    mediaPlayer.start();
                                }
                            });
                        }catch (NullPointerException ignored){}

                        holder.sharedPostVideoView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent videoIntent = new Intent(context, FullScreenVideoActivity.class);
                                videoIntent.putExtra("videoID", postModel.getPostID());
                                videoIntent.putExtra("reason", "random");
                                videoIntent.putExtra("userID", post.getUserID());
                                context.startActivity(videoIntent);
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

    private void getSharedImagePost(MyHolder holder, PostModel post) {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    if (postModel.getPostID().equals(post.getSharedPost())){
                        holder.sharedPostDesc.setText(postModel.getPostCaption());

                        try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                            String timeStamp = getTimeAgo.getTimeAgo(Long.parseLong(postModel.getPostTime()), context);
                            holder.sharedPostDate.setText(timeStamp);
                        }catch (NumberFormatException n){
                            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                        }//for converting timestamp

                        try{
                            Picasso.get().load(postModel.getPostImage()).into(holder.sharedPostImage);
                            holder.sharedProgressBar.setVisibility(View.GONE);
                        }catch (NullPointerException ignored){}

                        getSharedPostOwner(holder, postModel);

                        try{
                            universalFunctions.findAddress(postModel, holder.sharedPostCheckIn);
                        }catch (Exception ignored){}

                        holder.sharedPostImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent imageIntent = new Intent(context, FullScreenImageActivity.class);
                                imageIntent.putExtra("itemID", postModel.getPostID());
                                imageIntent.putExtra("reason", "postImage");
                                context.startActivity(imageIntent);
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

    private void getSharedTextPost(MyHolder holder, PostModel post) {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel postModel = ds.getValue(PostModel.class);

                    if (postModel.getPostID().equals(post.getSharedPost())){
                        holder.sharedPostDesc.setText(postModel.getPostCaption());

                        try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                            String timeStamp = getTimeAgo.getTimeAgo(Long.parseLong(post.getPostTime()), context);
                            holder.sharedPostDate.setText(timeStamp);
                        }catch (NumberFormatException n){
                            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                        }//for converting timestamp

                         getSharedPostOwner(holder, postModel);

                        try{
                            universalFunctions.findAddress(postModel, holder.sharedPostCheckIn);
                        }catch (Exception ignored){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSharedPostOwner(MyHolder holder, PostModel post) {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUSER_ID().equals(post.getUserID())){
                        try{
                            Picasso.get().load(user.getImageURL()).into(holder.sharedPostProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.sharedPostProPic);
                        }

                        holder.sharedPostUsername.setText(user.getUsername());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //normal post functions
    private void displayTextPost(MyHolder holder, PostModel post) {

        getPostDetails(post, holder);
        checkOnlineStatus(post.getUserID(), holder);
        iniPicPopUp(context, holder, post.getUserID());
        universalFunctions.getCommentsCount(post.getPostID(), holder.commentCounter);
        updateUserInfo(holder, post.getUserID());

        universalFunctions.isLiked(post.getPostID(), holder.postLikeBTN);
        universalFunctions.nrLikes(holder.likeCounter, post.getPostID());
        universalFunctions.isSaved(post.getPostID(), holder.savePostBTN);

        universalOnClickListeners(post, holder);

    }

    private void displayImagePost(MyHolder holder, PostModel post) {
        getImagePostDetails(post, holder);
        checkOnlineStatus(post.getUserID(), holder);
        iniPicPopUp(context, holder, post.getUserID());
        universalFunctions.getCommentsCount(post.getPostID(), holder.commentCounter);
        updateUserInfo(holder, post.getUserID());

        universalFunctions.isLiked(post.getPostID(), holder.postLikeBTN);
        universalFunctions.nrLikes(holder.likeCounter, post.getPostID());
        universalFunctions.isSaved(post.getPostID(), holder.savePostBTN);

        universalOnClickListeners(post, holder);

        holder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(context, FullScreenImageActivity.class);
                intent1.putExtra("itemID", post.getPostID());
                intent1.putExtra("reason", "postImage");
                context.startActivity(intent1);
            }
        });
    }

    private void getImagePostDetails(PostModel post, MyHolder holder) {

        holder.postCaption.setText(post.getPostCaption());

        try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
            String timeStamp = getTimeAgo.getTimeAgo(Long.parseLong(post.getPostTime()), context);
            holder.postTimeStamp.setText(timeStamp);
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }//for converting timestamp

        try{
            Picasso.get().load(post.getPostImage()).into(holder.postImage);
        }catch (Exception e){
            Toast.makeText(context, "could not upload post pic", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayVideoPost(MyHolder holder, PostModel post) {

        getVideoPostDetails(post, holder);
        checkOnlineStatus(post.getUserID(), holder);
        iniPicPopUp(context, holder, post.getUserID());
        universalFunctions.getCommentsCount(post.getPostID(), holder.commentCounter);
        updateUserInfo(holder, post.getUserID());

        universalFunctions.isLiked(post.getPostID(), holder.postLikeBTN);
        universalFunctions.nrLikes(holder.likeCounter, post.getPostID());
        universalFunctions.isSaved(post.getPostID(), holder.savePostBTN);

        universalOnClickListeners(post, holder);

        holder.postVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent videoIntent = new Intent(context, FullScreenVideoActivity.class);
                videoIntent.putExtra("videoID", post.getPostID());
                videoIntent.putExtra("reason", "random");
                videoIntent.putExtra("userID", post.getUserID());
                context.startActivity(videoIntent);
            }
        });

    }

    private void getVideoPostDetails(PostModel post, MyHolder holder) {
        holder.postCaption.setText(post.getPostCaption());

        try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
            String timeStamp = getTimeAgo.getTimeAgo(Long.parseLong(post.getPostTime()), context);
            holder.postTimeStamp.setText(timeStamp);
        }catch (NumberFormatException ignored){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }//for converting timestamp

        try{
            holder.postVideoView.setVideoURI(Uri.parse(post.getVideoURL()));
            holder.postVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    holder.videoLoader.setVisibility(View.GONE);
                    mediaPlayer.setLooping(true);
                    mediaPlayer.setVolume(0f, 0f);

                    holder.volumeBTN.setImageResource(R.drawable.ic_baseline_volume_up_24);

                    float videoRatio = mediaPlayer.getVideoWidth() / (float)mediaPlayer.getVideoHeight();
                    float screenRatio = holder.postVideoView.getWidth() / (float)holder.postVideoView.getHeight();
                    float scale  = videoRatio / screenRatio;
                    if (scale >= 1f){
                        holder.postVideoView.setScaleX(scale);
                    }else {
                        holder.postVideoView.setScaleY(1f / scale);
                    }
                }
            });

            holder.postVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
        }catch (NullPointerException ignored){}

    }


    //post retrieval functions
    private void universalOnClickListeners(PostModel post, MyHolder holder) {

        holder.postUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open the profile of post owner
                if(post.getUserID().equals(firebaseUser.getUid())){
                    FragmentManager fm = ((AppCompatActivity)context).getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    fragmentTransaction.replace(R.id.content,  new ProfileFragment());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }else{
                    Intent intent = new Intent(context, ViewProfileActivity.class);
                    intent.putExtra("uid", post.getUserID());
                    context.startActivity(intent);

                }
            }
        });//for displaying post owner profile

        holder.postMenuBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "menu clicked", Toast.LENGTH_SHORT).show();
                //showMoreOptions(holder.postMenuBTN, uid, pId, PostImage);
                menuOptions(holder.postMenuBTN, post.getUserID(), post.getPostID(), post.getPostImage());
            }
        });

        holder.commentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //holder.commentDialog.show();
                Intent postDetailIntent= new Intent(context, PostDetailActivity.class);
                postDetailIntent.putExtra("postID", post.getPostID());
                context.startActivity(postDetailIntent);
            }
        });

        holder.postProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.popAddPost.show();
            }
        });//popping up profile picture

        holder.likesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ConnectionsActivity.class);
                intent.putExtra("UserID", post.getPostID());
                intent.putExtra("Interaction", "Likes");
                context.startActivity(intent);
            }
        });

        holder.postLikeBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.postLikeBTN.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostID())
                            .child(firebaseUser.getUid()).setValue(true);
                    universalFunctions.addLikesNotifications(post.getUserID(), post.getPostID());
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostID())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.savePostBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.savePostBTN.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostID()).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostID()).removeValue();
                }
            }
        });

        holder.shareArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(context, SharePostActivity.class);
                shareIntent.putExtra("postID", post.getPostID());
                context.startActivity(shareIntent);
            }
        });

    }

    private void getPostDetails(PostModel post, final MyHolder holder) {

        holder.postCaption.setText(post.getPostCaption());

        try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
            String timeStamp = getTimeAgo.getTimeAgo(Long.parseLong(post.getPostTime()), context);
            holder.postTimeStamp.setText(timeStamp);
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }//for converting timestamp

        try{
            if (post.getPostPrivacy().equals("Private")){
                holder.shareArea.setVisibility(View.GONE);
            }
        }catch (NullPointerException ignored){}

        try{
            universalFunctions.findAddress(post, holder.rPostLocation);
        }catch (Exception ignored){}

        if (post.getUserID().equals(firebaseUser.getUid()))
            holder.shareArea.setVisibility(View.GONE);

        /*try{
            if(PostImage.equals("noImage")){
                if(VoiceStatus.equals("noVoice")){
                    //handle no image, no voice - Text Only
                    holder.postImage.setVisibility(View.GONE);
                    holder.postCaption.setVisibility(View.VISIBLE);
                }else{
                    //handle no image, active voice
                    holder.playStatusBTN.setTag("Play");
                    holder.playStatusBTN.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(holder.playStatusBTN.getTag().equals("Play")){
                                //for playing status with no image
                                holder.playStatusBTN.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                                holder.playStatusBTN.setTag("Pause");
                                playVoiceStatus(VoiceStatus, holder);
                            }else{
                                //for pausing status
                                holder.playStatusBTN.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                                holder.playStatusBTN.setTag("Play");
                                pause();
                            }

                        }
                    });
                }
            }else {
                if(!VoiceStatus.equals("noVoice")){
                    //handle image, active voice
                    holder.postImage.setVisibility(View.VISIBLE);
                    holder.mediaPlayer.setVisibility(View.VISIBLE);

                    try{
                        Picasso.get().load(PostImage).networkPolicy(NetworkPolicy.OFFLINE).into(holder.postImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(PostImage).into(holder.postImage);
                            }
                        });
                    }catch (Exception e){
                        Toast.makeText(context, "could not upload post pic", Toast.LENGTH_SHORT).show();
                    }

                    holder.playStatusBTN.setTag("Play");
                    holder.playStatusBTN.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(holder.playStatusBTN.getTag().equals("Play")){
                                //for playing status with an image
                                holder.playStatusBTN.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                                holder.playStatusBTN.setTag("Pause");
                                playVoiceStatus(VoiceStatus, holder);
                            }else{
                                //for pausing status with an image
                                holder.playStatusBTN.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                                holder.playStatusBTN.setTag("Play");
                                pause();
                            }

                        }
                    });

                }else{
                    //handle image, no voice
                    holder.postImage.setVisibility(View.VISIBLE);
                    holder.postCaption.setVisibility(View.VISIBLE);

                    try{
                        Picasso.get().load(PostImage).into(holder.postImage);
                    }catch (Exception e){
                        Toast.makeText(context, "could not upload post pic", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }catch (NullPointerException ignored){ }*/
    }

    private void updateUserInfo(final MyHolder holder, final String uid) {
        List<ContactsModel> phoneBook = new ArrayList<>();
        ContactsList contactsList = new ContactsList(phoneBook, context);
        contactsList.readContacts();
        final List<ContactsModel> phoneNumbers = contactsList.getContactsList();

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        Query query = ref.orderByChild("USER_ID").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        String name = "" + ds.child("Username").getValue();
                        String number = "" + ds.child("Number").getValue();
                        final String proPic = "" + ds.child("ImageURL").getValue();

                        for(ContactsModel cm : phoneNumbers){

                            if(firebaseUser.getUid().equals(uid)){
                                holder.postUsername.setText("Me");
                            }else if(cm.getNumber().equals(number)){
                                holder.postUsername.setText(cm.getUsername());
                            }
                        }

                        try{
                            Picasso.get().load(proPic).placeholder(R.drawable.default_profile_display_pic).networkPolicy(NetworkPolicy.OFFLINE).into(holder.postProPic, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.postProPic);
                                }
                            });
                        }catch (NullPointerException ignored){

                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    //other post functions
    private void iniPicPopUp(final Context context, final MyHolder holder, final String uid) {
        holder.popAddPost = new Dialog(context);
        holder.popAddPost.setContentView(R.layout.profile_pic_pop_up_layout);
        holder.popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        holder.popAddPost.getWindow().setLayout(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        holder.popAddPost.getWindow().getAttributes().gravity = Gravity.CENTER;

        ImageView viewProfile = holder.popAddPost.findViewById(R.id.popUP_ViewProfile);
        ImageView sendMessage = holder.popAddPost.findViewById(R.id.popUP_SendMessage);
        final ImageView superProPic = holder.popAddPost.findViewById(R.id.popUP_ProPic);
        ImageView callBTN = holder.popAddPost.findViewById(R.id.popUP_callUser);
        final TextView lastSeen = holder.popAddPost.findViewById(R.id.popUpLastSeen);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = reference.orderByChild("USER_ID").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String image = "" + ds.child("ImageURL").getValue();
                    String timeStamp = "" + ds.child("onlineStatus").getValue();
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());

                    if(timeStamp.equals("online")){
                        lastSeen.setText(timeStamp);
                        lastSeen.setVisibility(View.VISIBLE);
                    }else{
                        try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                            calendar.setTimeInMillis(Long.parseLong(timeStamp));
                            String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                            lastSeen.setVisibility(View.VISIBLE);
                            lastSeen.setText("Last seen at: " + pTime);
                        }catch (NumberFormatException n){
                            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                        }
                    }

                    try{
                        Picasso.get().load(image).placeholder(R.drawable.default_profile_display_pic).into(superProPic);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.default_profile_display_pic).into(superProPic);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();
        if(uid.equals(user.getUid())){
            sendMessage.setVisibility(View.GONE);
            callBTN.setVisibility(View.GONE);

        }
        callBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "start phone call", Toast.LENGTH_SHORT).show();
                holder.popAddPost.dismiss();
            }
        });

        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(uid.equals(user.getUid())){

                    FragmentManager fm = ((AppCompatActivity)context).getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    fragmentTransaction.replace(R.id.content,  new ProfileFragment());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    holder.popAddPost.dismiss();

                }else{
                    Intent intent = new Intent(context, ViewProfileActivity.class);
                    intent.putExtra("uid", uid);
                    context.startActivity(intent);
                    holder.popAddPost.dismiss();
                }

            }
        });

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent1 = new Intent(context, ChatActivity.class);
                intent1.putExtra("userid", uid);
                context.startActivity(intent1);
                holder.popAddPost.dismiss();
            }
        });

        superProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(context, FullScreenImageActivity.class);
                intent1.putExtra("itemID", uid);
                intent1.putExtra("reason", "userImage");
                context.startActivity(intent1);
                Toast.makeText(context, "you will be able to view users story", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkOnlineStatus(String uid, final MyHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = reference.orderByChild("USER_ID").equalTo(uid);
        //display and retrieve current user info
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    String userOnline = "" + ds.child("onlineStatus").getValue();

                    if(userOnline.equals("online")){
                        holder.onlineStatus.setVisibility(View.VISIBLE);
                    }else{
                        holder.onlineStatus.setVisibility(View.VISIBLE);
                        holder.onlineStatus.setImageResource(R.drawable.offline_circle);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void menuOptions(ImageView menuIcon, final String uid, final String pId, final String postImage) {
        PopupMenu popupMenu = new PopupMenu(context, menuIcon, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0,0,"View Profile");
        if(uid.equals(firebaseUser.getUid())){
            popupMenu.getMenu().add(Menu.NONE, 1,0,"Delete Post");
            popupMenu.getMenu().add(Menu.NONE, 2,0,"Edit Post");
            popupMenu.getMenu().add(Menu.NONE, 3,0,"Post Views");

        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if(id == 0){
                    if(uid.equals(firebaseUser.getUid())){
                        FragmentManager fm = ((AppCompatActivity)context).getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fm.beginTransaction();
                        fragmentTransaction.replace(R.id.content,  new ProfileFragment());
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    }else{
                        Intent intent = new Intent(context, ViewProfileActivity.class);
                        intent.putExtra("uid", uid);
                        context.startActivity(intent);
                    }
                }else if(id == 1){
                    universalFunctions.beginDelete(pId, postImage);
                }else if(id == 2){
                    Toast.makeText(context, "Edit Post Details", Toast.LENGTH_SHORT).show();
                }else if (id == 3){
                    Intent intent = new Intent(context, ConnectionsActivity.class);
                    intent.putExtra("UserID", pId);
                    intent.putExtra("Interaction", "Views");
                    context.startActivity(intent);
                }

                return false;
            }
        });
        popupMenu.show();
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        //-----Views from row post.xml
            //---------------view for post owner details
        public CircleImageView postProPic;
        public TextView postUsername;
        public ImageView onlineStatus;

            //--------------views for post details
        public TextView postTimeStamp, postCaption, taggedUsers, rPostLocation;
        public ImageView privacyIcon, postImage;

            //--------------views for post buttons
        public ImageView postLikeBTN, postMenuBTN, savePostBTN;
        public TextView likeCounter, commentCounter;

            //------------other
        public RelativeLayout commentLayout, likesLayout, shareArea;
        public Dialog popAddPost;
        public CardView postPicArea;

        //----------------for videos
        public VideoView postVideoView;
        public ProgressBar videoLoader;
        public ImageView volumeBTN;

        //---------------for shared posts
        public CircleImageView sharedPostProPic;
        public TextView sharedPostUsername, sharedPostDate, sharedPostCheckIn, sharedPostDesc;
        public ProgressBar sharedProgressBar;
        public LinearLayout sharedLocationArea;
        public ImageView sharedPostImage, sharedPostVolumeBTN;
        public CardView sharedPost;
        public VideoView sharedPostVideoView;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            postProPic = itemView.findViewById(R.id.postProPic);
            postUsername = itemView.findViewById(R.id.postUsername);
            onlineStatus = itemView.findViewById(R.id.onlineStatus);

            postTimeStamp = itemView.findViewById(R.id.postTimeStamp);
            postCaption = itemView.findViewById(R.id.postCaption);
            taggedUsers = itemView.findViewById(R.id.rPostTags);
            rPostLocation = itemView.findViewById(R.id.rPostLocation);

            privacyIcon = itemView.findViewById(R.id.privacyIcon);
            postImage = itemView.findViewById(R.id.rPostImage);

            postLikeBTN = itemView.findViewById(R.id.postLikeBTN);
            postMenuBTN = itemView.findViewById(R.id.postMenuBTN);
            savePostBTN = itemView.findViewById(R.id.savePostBTN);

            likeCounter = itemView.findViewById(R.id.likeCounter);
            commentCounter = itemView.findViewById(R.id.commentCounter);

            commentLayout = itemView.findViewById(R.id.commentLayout);
            likesLayout = itemView.findViewById(R.id.likesArea);
            shareArea = itemView.findViewById(R.id.shareArea);
            postPicArea = itemView.findViewById(R.id.postPicArea);

            postVideoView = itemView.findViewById(R.id.timelineVideoView);
            videoLoader = itemView.findViewById(R.id.videoItemLoader);
            volumeBTN = itemView.findViewById(R.id.postItemVolumeBTN);

            //for shared posts
            sharedPostProPic = itemView.findViewById(R.id.shared_image_user);
            sharedPostUsername = itemView.findViewById(R.id.shared_username);
            sharedPostDate = itemView.findViewById(R.id.shared_postDate);
            sharedPostCheckIn = itemView.findViewById(R.id.shared_postCheckIn);
            sharedPostDesc = itemView.findViewById(R.id.shared_post_desc);
            sharedProgressBar = itemView.findViewById(R.id.shared_progress_load_media);
            sharedLocationArea = itemView.findViewById(R.id.sharedLocationArea);
            sharedPostImage = itemView.findViewById(R.id.shared_postImage);
            sharedPost = itemView.findViewById(R.id.shared_post_item);
            sharedPostVideoView = itemView.findViewById(R.id.sharedTimelineVideoView);
            sharedPostVolumeBTN = itemView.findViewById(R.id.sharePostItemVolumeBTN);
        }
    }

    @Override
    public int getItemViewType(int position) {

        switch (postList.get(position).getPostType()){
            case "textPost":
                return TEXT_POST_ITEM;

            case "imagePost":
                return IMAGE_POST_ITEM;

            case "videoPost":
                return VIDEO_POST_ITEM;

            case "sharedTextPost":
                return SHARED_TEXT_POST_ITEM;

            case "sharedImagePost":
                return SHARED_IMAGE_POST_ITEM;

            case "sharedVideoPost":
                return SHARED_VIDEO_POST_ITEM;

            default:
                throw new IllegalStateException("Unexpected value" + postList.get(position).getPostType());
        }
    }
}