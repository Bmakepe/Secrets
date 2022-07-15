package com.makepe.blackout.GettingStarted.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Fragments.ProfileFragment;
import com.makepe.blackout.GettingStarted.InAppActivities.ChatActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.CommentsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ConnectionsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenImageActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.PhoneCallActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.SharePostActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.StoryActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.Notifications.SendNotifications;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioPlayer;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalNotifications;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyHolder> {

    private Context context;
    private List<PostModel> postList;
    private FirebaseUser firebaseUser;
    private DatabaseReference postReference, userReference, followingReference;

    private UniversalFunctions universalFunctions;
    private UniversalNotifications universalNotifications;
    private GetTimeAgo getTimeAgo;

    private boolean isLoaderVisible = false;

    public static final int TEXT_POST_ITEM = 100;
    public static final int IMAGE_POST_ITEM = 200;
    public static final int SHARED_TEXT_POST_ITEM = 300;
    public static final int SHARED_IMAGE_POST_ITEM = 400;
    public static final int AUDIO_POST_ITEM = 500;
    public static final int AUDIO_IMAGE_POST_ITEM = 600;
    public static final int SHARED_AUDIO_TEXT_POST = 700;
    public static final int SHARED_AUDIO_IMAGE_POST = 800;
    public static final int SHARED_TEXT_AUDIO_POST = 900;
    public static final int SHARED_TEXT_AUDIO_IMAGE_POST = 1000;
    public static final int SHARED_AUDIO_AUDIO_IMAGE_POST = 1100;
    public static final int SHARED_AUDIO_AUDIO_POST = 1200;
    public static final int SHARED_TEXT_VIDEO_POST = 1300;
    public static final int SHARED_AUDIO_VIDEO_POST = 1400;
    public static final int SHARED_TEXT_AUDIO_VIDEO_POST = 1500;
    public static final int SHARED_AUDIO_AUDIO_VIDEO_POST = 1600;

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

            case IMAGE_POST_ITEM:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.raw_image_post, parent, false));

            case SHARED_TEXT_POST_ITEM:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.shared_text_post_item, parent, false));

            case SHARED_IMAGE_POST_ITEM:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.shared_image_post_item, parent, false));

            case AUDIO_POST_ITEM:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.audio_post_item, parent, false));

            case AUDIO_IMAGE_POST_ITEM:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.audio_image_post_item, parent, false));

            case SHARED_AUDIO_IMAGE_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.shared_audio_image_post_item, parent, false));

            case SHARED_AUDIO_TEXT_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.shared_audio_text_item, parent, false));

            case SHARED_TEXT_AUDIO_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.shared_text_audio_post_item, parent, false));

            case SHARED_TEXT_AUDIO_IMAGE_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.shared_text_audio_image_item, parent, false));

            case SHARED_AUDIO_AUDIO_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.shared_audio_audio_post_item, parent, false));

            case SHARED_AUDIO_AUDIO_IMAGE_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.shared_audio_audio_image_item, parent, false));

            case SHARED_AUDIO_VIDEO_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.shared_audio_video_item, parent, false));

            case SHARED_TEXT_VIDEO_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.shared_video_item, parent, false));

            case SHARED_TEXT_AUDIO_VIDEO_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.shared_text_audio_video_item, parent, false));

            case SHARED_AUDIO_AUDIO_VIDEO_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.shared_audio_audio_video_item, parent, false));

            default:
                throw new IllegalStateException("Unexpected value" + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {

        PostModel post = postList.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        postReference = FirebaseDatabase.getInstance().getReference("Posts");
        followingReference = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(firebaseUser.getUid()).child("following");
        universalFunctions = new UniversalFunctions(context);
        universalNotifications = new UniversalNotifications(context);
        getTimeAgo = new GetTimeAgo();

        getAdapterFunctions(post, holder);

        switch (getItemViewType(position)){
            case IMAGE_POST_ITEM:
                displayImagePost(holder, post);
                getNormalPostButtons(holder, post);
                break;

            case TEXT_POST_ITEM:
                displayTextPost(holder, post);
                break;

            case AUDIO_POST_ITEM:
                displayAudioPost(holder, post);
                break;

            case AUDIO_IMAGE_POST_ITEM:
                displayAudioImagePost(holder, post);
                getNormalPostButtons(holder, post);
                break;

            case SHARED_TEXT_POST_ITEM:
                displaySharedTextPost(holder, post);
                getSharedPostButtons(holder, post);
                break;

            case SHARED_IMAGE_POST_ITEM:
                displaySharedImagePost(holder, post);
                getSharedPostButtons(holder, post);
                break;

            case SHARED_AUDIO_IMAGE_POST:
                displaySharedAudioImageTextPost(holder, post);
                getSharedPostButtons(holder, post);
                break;

            case SHARED_AUDIO_TEXT_POST:
                displaySharedAudioTextPost(holder, post);
                getSharedPostButtons(holder, post);
                break;

            case SHARED_TEXT_AUDIO_POST:
                displaySharedTextAudioPost(holder, post);
                getSharedPostButtons(holder, post);
                break;

            case SHARED_TEXT_AUDIO_IMAGE_POST:
                displaySharedTextImageAudioPost(holder, post);
                getSharedPostButtons(holder, post);
                break;

            case SHARED_AUDIO_AUDIO_POST:
                displaySharedAudioAudioPost(holder, post);
                getSharedPostButtons(holder, post);
                break;

            case SHARED_AUDIO_AUDIO_IMAGE_POST:
                displayAudioImageAudioPost(holder, post);
                getSharedPostButtons(holder, post);
                break;

            case SHARED_TEXT_VIDEO_POST:
                Toast.makeText(context, "Shared Text Video Post Identified", Toast.LENGTH_SHORT).show();
                break;

            case SHARED_AUDIO_VIDEO_POST:
                Toast.makeText(context, "Shared Audio Text Video Post Identified", Toast.LENGTH_SHORT).show();
                break;

            case SHARED_TEXT_AUDIO_VIDEO_POST:
                Toast.makeText(context, "Shared Text Audio Video Post Identified", Toast.LENGTH_SHORT).show();
                break;

            case SHARED_AUDIO_AUDIO_VIDEO_POST:
                Toast.makeText(context, "Shared Audio Audio Video Post Identified", Toast.LENGTH_SHORT).show();
                break;

            default:
                Toast.makeText(context, "Unknown post type identified " + post.getPostType(), Toast.LENGTH_SHORT).show();
        }

    }

    //-----------for retrieving shared posts only
    private void displaySharedTextPost(MyHolder holder, PostModel post) {

        postReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel model = snapshot.getValue(PostModel.class);

                    assert model != null;
                    displayTextPost(holder, model);

                    postReference.child(model.getSharedPost()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel sharedPost = snapshot.getValue(PostModel.class);

                                assert sharedPost != null;
                                holder.sharedPostDesc.setText(sharedPost.getPostCaption());

                                try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                                    holder.sharedPostDate.setText(getTimeAgo.getTimeAgo(Long.parseLong(sharedPost.getPostTime()), context));
                                }catch (NumberFormatException n){
                                    Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                                }//for converting timestamp

                                try{
                                    universalFunctions.findAddress(sharedPost.getLatitude(), sharedPost.getLongitude(),  holder.sharedPostCheckIn, holder.sharedLocationArea);
                                }catch (Exception ignored){}

                                getSharedPostOwner(holder, sharedPost);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displaySharedImagePost(MyHolder holder, PostModel post) {

        postReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel postModel = snapshot.getValue(PostModel.class);

                    assert postModel != null;
                    displayTextPost(holder, postModel);

                    postReference.child(post.getSharedPost()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel sharedPost = snapshot.getValue(PostModel.class);

                                assert sharedPost != null;
                                holder.sharedPostDesc.setText(sharedPost.getPostCaption());

                                try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                                    holder.sharedPostDate.setText(getTimeAgo.getTimeAgo(Long.parseLong(sharedPost.getPostTime()), context));
                                }catch (NumberFormatException n){
                                    Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                                }//for converting timestamp

                                try{
                                    Picasso.get().load(sharedPost.getPostImage()).into(holder.sharedPostImage);
                                    holder.sharedProgressBar.setVisibility(View.GONE);
                                }catch (NullPointerException e){
                                    Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.sharedPostImage);
                                }

                                getSharedPostOwner(holder, sharedPost);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displaySharedAudioTextPost(MyHolder holder, PostModel post) {

        postReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel postModel = snapshot.getValue(PostModel.class);

                    assert postModel != null;
                    displayAudioPost(holder, postModel);

                    postReference.child(postModel.getSharedPost()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel sharedPost = snapshot.getValue(PostModel.class);

                                assert sharedPost != null;
                                holder.sharedPostDesc.setText(sharedPost.getPostCaption());

                                try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                                    holder.sharedPostDate.setText(getTimeAgo.getTimeAgo(Long.parseLong(sharedPost.getPostTime()), context));
                                }catch (NumberFormatException n){
                                    Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                                }//for converting timestamp

                                try{
                                    universalFunctions.findAddress(sharedPost.getLatitude(), sharedPost.getLongitude(),  holder.sharedPostCheckIn, holder.sharedLocationArea);
                                }catch (Exception ignored){}

                                getSharedPostOwner(holder, sharedPost);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displaySharedAudioImageTextPost(MyHolder holder, PostModel post) {

        postReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel model = snapshot.getValue(PostModel.class);

                    assert model != null;
                    displayAudioPost(holder, model);

                    postReference.child(model.getSharedPost()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel sharedPost = snapshot.getValue(PostModel.class);

                                assert sharedPost != null;
                                holder.sharedPostDesc.setText(sharedPost.getPostCaption());

                                try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                                    holder.sharedPostDate.setText(getTimeAgo.getTimeAgo(Long.parseLong(sharedPost.getPostTime()), context));
                                }catch (NumberFormatException n){
                                    Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                                }//for converting timestamp

                                try{
                                    Picasso.get().load(sharedPost.getPostImage()).into(holder.sharedPostImage);
                                    holder.sharedProgressBar.setVisibility(View.GONE);
                                }catch (NullPointerException e){
                                    Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.sharedPostImage);
                                }

                                getSharedPostOwner(holder, sharedPost);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displaySharedTextAudioPost(MyHolder holder, PostModel post) {

        AudioPlayer audioPlayer = new AudioPlayer(context, holder.playBTN,
                holder.seekTimer, holder.postTotalTime, holder.audioAnimation);

        postReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel model = snapshot.getValue(PostModel.class);

                    assert model != null;
                    displayTextPost(holder, model);

                    postReference.child(model.getSharedPost()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel sharedPost = snapshot.getValue(PostModel.class);

                                assert sharedPost != null;

                                getSharedPostOwner(holder, sharedPost);
                                getSharedAudio(holder, sharedPost);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displaySharedTextImageAudioPost(MyHolder holder, PostModel post) {

        postReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel model = snapshot.getValue(PostModel.class);

                    assert model != null;
                    displayTextPost(holder, model);

                    postReference.child(model.getSharedPost()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel sharedPost = snapshot.getValue(PostModel.class);

                                assert sharedPost != null;

                                try{
                                    Picasso.get().load(sharedPost.getPostImage()).into(holder.sharedPostImage);
                                }catch (NullPointerException e){
                                    Picasso.get().load(R.drawable.ic_image_black_24dp).into(holder.sharedPostImage);
                                }

                                getSharedPostOwner(holder, sharedPost);
                                getSharedAudio(holder, sharedPost);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayAudioImageAudioPost(MyHolder holder, PostModel post) {

        postReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel postModel = snapshot.getValue(PostModel.class);

                    assert postModel != null;
                    displayAudioPost(holder, postModel);

                    postReference.child(postModel.getSharedPost()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel sharedPost = snapshot.getValue(PostModel.class);

                                assert sharedPost != null;

                                getSharedPostOwner(holder, sharedPost);
                                getSharedAudio(holder, sharedPost);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void displaySharedAudioAudioPost(MyHolder holder, PostModel post) {

        postReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel postModel = snapshot.getValue(PostModel.class);

                    assert postModel != null;
                    displayAudioPost(holder, postModel);

                    postReference.child(postModel.getSharedPost()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel sharedPost = snapshot.getValue(PostModel.class);

                                assert sharedPost != null;

                                try{
                                    Picasso.get().load(sharedPost.getPostImage()).into(holder.sharedPostImage);
                                }catch (NullPointerException e) {
                                    Picasso.get().load(R.drawable.ic_image_black_24dp).into(holder.sharedPostImage);
                                }

                                getSharedPostOwner(holder, sharedPost);
                                getSharedAudio(holder, sharedPost);


                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //-----------for retrieving normal posts details
    private void displayImagePost(MyHolder holder, PostModel post) {
        postReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //for retrieving normal image posts only
                if (snapshot.exists()){
                    PostModel model = snapshot.getValue(PostModel.class);
                    assert model != null;
                    holder.postCaption.setText(model.getPostCaption());

                    try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                        holder.postTimeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(model.getPostTime()), context));
                    }catch (NumberFormatException n){
                        Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                    }//for converting timestamp

                    try{
                        Picasso.get().load(model.getPostImage()).into(holder.postImage);
                        holder.imageLoader.setVisibility(View.GONE);
                    }catch (NullPointerException e){
                        Picasso.get().load(R.drawable.ic_image_black_24dp).into(holder.postImage);
                    }

                    getPostUserDetails(holder, post);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayTextPost(MyHolder holder, PostModel post) {
        postReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel model = snapshot.getValue(PostModel.class);

                    assert model != null;
                    holder.postCaption.setText(model.getPostCaption());

                    try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                        holder.postTimeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(model.getPostTime()), context));
                    }catch (NumberFormatException n){
                        Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                    }//for converting timestamp

                    getPostUserDetails(holder, post);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayAudioImagePost(MyHolder holder, PostModel post) {

        postReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel model = snapshot.getValue(PostModel.class);

                    assert model != null;
                    try{
                        Picasso.get().load(model.getPostImage()).into(holder.postImage);
                        holder.imageLoader.setVisibility(View.GONE);
                    }catch (NullPointerException e){
                        Picasso.get().load(R.drawable.ic_image_black_24dp).into(holder.postImage);
                    }

                    try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                        holder.postTimeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(model.getPostTime()), context));
                    }catch (NumberFormatException n){
                        Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                    }//for converting timestamp

                    try{
                        universalFunctions.findAddress(model.getLatitude(), model.getLongitude(), holder.rPostLocation, holder.locationArea);
                    }catch (Exception ignored){}

                    getPostUserDetails(holder, model);
                    getNormalPostAudio(holder, model);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayAudioPost(MyHolder holder, PostModel post) {

        postReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel model = snapshot.getValue(PostModel.class);

                    assert model != null;

                    try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                        holder.postTimeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(model.getPostTime()), context));
                    }catch (NumberFormatException n){
                        Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                    }//for converting timestamp

                    try{
                        universalFunctions.findAddress(model.getLatitude(), model.getLongitude(), holder.rPostLocation, holder.locationArea);
                    }catch (Exception ignored){}

                    getPostUserDetails(holder, model);
                    getNormalPostAudio(holder, model);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    //-----------for retrieving user details
    private void getPostUserDetails(MyHolder holder, PostModel post) {
        userReference.child(post.getUserID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    holder.postUsername.setText(user.getUsername());

                    try{
                        Picasso.get().load(user.getImageURL()).into(holder.postProPic);
                    }catch (NullPointerException e){
                        Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.postProPic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSharedPostOwner(MyHolder holder, PostModel sharedPost) {
        userReference.child(sharedPost.getUserID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = snapshot.getValue(User.class);

                    holder.sharedPostUsername.setText(user.getUsername());

                    try{
                        Picasso.get().load(user.getImageURL()).into(holder.sharedPostProPic);
                    }catch (NullPointerException e){
                        Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.sharedPostProPic);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //-----------button onclick listeners
    private void getAdapterFunctions(PostModel post, MyHolder holder) {
        iniPicPopUp(context, holder, post);
        universalFunctions.getCommentsCount(post.getPostID(), holder.commentCounter);

        universalFunctions.isLiked(post.getPostID(), holder.postLikeBTN);
        universalFunctions.nrLikes(holder.likeCounter, post.getPostID());
        universalFunctions.isSaved(post.getPostID(), holder.savePostBTN);
        universalFunctions.checkActiveStories(holder.postProPic, post.getUserID());

        onClickListeners(holder, post);

        if (firebaseUser.getUid().equals(post.getUserID()))
            holder.shareArea.setVisibility(View.GONE);

        /*try{
            if (post.isCommentsAllowed())
                holder.commentLayout.setVisibility(View.GONE);
        }catch (NullPointerException ignored){}*/
    }

    private void getSharedPostButtons(MyHolder holder, PostModel post) {

        holder.shareArea.setVisibility(View.GONE);

        /*holder.sharedPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postReference.child(post.getSharedPost()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){

                            Intent intent = new Intent(context, FullScreenImageActivity.class);
                            intent.putExtra("itemID", post.getSharedPost());
                            intent.putExtra("reason", "postImage");
                            context.startActivity(intent);
                        }else{
                            movementPostReference.child(post.getSharedPost()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){

                                        Intent intent = new Intent(context, FullScreenImageActivity.class);
                                        intent.putExtra("itemID", post.getSharedPost());
                                        intent.putExtra("postImage", "movementPostPic");
                                        context.startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });*/

        holder.sharedPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharedPostIntent = new Intent(context, CommentsActivity.class);
                sharedPostIntent.putExtra("postID", post.getPostID());
                context.startActivity(sharedPostIntent);
            }
        });
    }

    private void getNormalPostButtons(MyHolder holder, PostModel post){

        holder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){

                            Intent intent = new Intent(context, FullScreenImageActivity.class);
                            intent.putExtra("itemID", post.getPostID());
                            intent.putExtra("reason", "postImage");
                            context.startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private void onClickListeners(MyHolder holder, PostModel post){

        holder.postUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open the profile of post owner
                if(!post.getUserID().equals(firebaseUser.getUid())){
                    Intent intent = new Intent(context, ViewProfileActivity.class);
                    intent.putExtra("uid", post.getUserID());
                    context.startActivity(intent);
                }
            }
        });//for displaying post owner profile

        holder.postMenuBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                universalFunctions.postMenuOptions(holder.postMenuBTN, post);
            }
        });

        holder.commentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                universalFunctions.addView(post.getPostID());

                Intent postDetailIntent= new Intent(context, CommentsActivity.class);
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
                    universalFunctions.likePost(post);
                }else{
                    universalFunctions.unlikePost(post);
                }
            }
        });

        holder.savePostBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.savePostBTN.getTag().equals("save")){
                    universalFunctions.savePost(post);
                }else{
                    universalFunctions.removeSavedPost(post);
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


    //other post functions
    private void iniPicPopUp(final Context context, final MyHolder holder, final PostModel postModel) {
        holder.popAddPost = new Dialog(context);
        holder.popAddPost.setContentView(R.layout.profile_pic_pop_up_layout);
        holder.popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        holder.popAddPost.getWindow().setLayout(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        holder.popAddPost.getWindow().getAttributes().gravity = Gravity.CENTER;

        ImageView viewProfile = holder.popAddPost.findViewById(R.id.popUP_ViewProfile);
        ImageView sendMessage = holder.popAddPost.findViewById(R.id.popUP_SendMessage);
        CircleImageView superProPic = holder.popAddPost.findViewById(R.id.popUP_ProPic);
        ImageView callBTN = holder.popAddPost.findViewById(R.id.popUP_callUser);
        RelativeLayout popButtonsArea = holder.popAddPost.findViewById(R.id.popButtonsArea);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUSER_ID().equals(postModel.getUserID())){

                        try{
                            Picasso.get().load(user.getImageURL()).placeholder(R.drawable.default_profile_display_pic).into(superProPic);
                        }catch (Exception e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(superProPic);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        universalFunctions.checkActiveStories(superProPic, postModel.getUserID());

        if(postModel.getUserID().equals(firebaseUser.getUid())){
            popButtonsArea.setVisibility(View.GONE);

        }

        callBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!postModel.getUserID().equals(firebaseUser.getUid())){
                    Intent callIntent = new Intent(context, PhoneCallActivity.class);
                    callIntent.putExtra("userID", postModel.getUserID());
                    context.startActivity(callIntent);
                    holder.popAddPost.dismiss();
                }
            }
        });

        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!postModel.getUserID().equals(firebaseUser.getUid())){
                    Intent intent = new Intent(context, ViewProfileActivity.class);
                    intent.putExtra("uid", postModel.getUserID());
                    context.startActivity(intent);
                    holder.popAddPost.dismiss();
                }

            }
        });

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent1 = new Intent(context, ChatActivity.class);
                intent1.putExtra("userid", postModel.getUserID());
                context.startActivity(intent1);
                holder.popAddPost.dismiss();
            }
        });

        superProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (holder.postProPic.getTag().equals("storyActive")){
                    Intent intent = new Intent(context, StoryActivity.class);
                    intent.putExtra("userid", postModel.getUserID());
                    context.startActivity(intent);
                    holder.popAddPost.dismiss();
                }else if(holder.postProPic.getTag().equals("noStories")){
                    Intent picIntent = new Intent(context, FullScreenImageActivity.class);
                    picIntent.putExtra("itemID", postModel.getUserID());
                    picIntent.putExtra("reason", "userImage");
                    context.startActivity(picIntent);
                    holder.popAddPost.dismiss();
                }

            }
        });

    }

    private void getSharedAudio(MyHolder holder, PostModel sharedPost) {

        AudioPlayer audioPlayer = new AudioPlayer(context, holder.shared_playBTN,
                holder.shared_seekTimer, holder.shared_postTotalTime, holder.shared_audioAnimation);

        try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
            holder.sharedPostDate.setText(getTimeAgo.getTimeAgo(Long.parseLong(sharedPost.getPostTime()), context));
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }//for converting timestamp

        try{
            universalFunctions.findAddress(sharedPost.getLatitude(), sharedPost.getLongitude(),  holder.sharedPostCheckIn, holder.sharedLocationArea);
        }catch (Exception ignored){}

        holder.shared_playBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!audioPlayer.isPlaying()){
                    audioPlayer.startPlayingAudio(sharedPost.getAudioURL());
                }else{
                    audioPlayer.stopPlayingAudio();
                }
            }
        });
    }

    private void getNormalPostAudio(MyHolder holder, PostModel post){

        AudioPlayer audioPlayer = new AudioPlayer(context, holder.playBTN,
                holder.seekTimer, holder.postTotalTime, holder.audioAnimation);

        holder.playBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!audioPlayer.isPlaying()){
                    audioPlayer.startPlayingAudio(post.getAudioURL());
                }else{
                    audioPlayer.stopPlayingAudio();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        //-----Views from row post.xml
            //---------------view for post owner details
        public CircleImageView postProPic;
        public TextView postUsername;

            //--------------views for post details
        public TextView postTimeStamp, postCaption, taggedUsers, rPostLocation;
        public ImageView privacyIcon, postImage;

            //--------------views for post buttons
        public ImageView postLikeBTN, postMenuBTN, savePostBTN;
        public TextView likeCounter, commentCounter;

            //------------other
        public RelativeLayout commentLayout, likesLayout, shareArea, postPicArea;
        public Dialog popAddPost;
        public LinearLayout locationArea;
        public ProgressBar imageLoader;

            //---------------for shared posts
        public CircleImageView sharedPostProPic;
        public TextView sharedPostUsername, sharedPostDate, sharedPostCheckIn, sharedPostDesc;
        public ProgressBar sharedProgressBar;
        public LinearLayout sharedLocationArea;
        public ImageView sharedPostImage, sharedPostVolumeBTN;
        public RelativeLayout sharedPost;

            //--------------for audio post buttons
        public CircleImageView playBTN;
        public LottieAnimationView audioAnimation;
        public TextView seekTimer, postTotalTime;

            //--------------for shared audio post buttons
        public CircleImageView shared_playBTN;
        public LottieAnimationView shared_audioAnimation;
        public TextView shared_seekTimer, shared_postTotalTime;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            postProPic = itemView.findViewById(R.id.postProPic);
            postUsername = itemView.findViewById(R.id.postUsername);

            postTimeStamp = itemView.findViewById(R.id.postTimeStamp);
            postCaption = itemView.findViewById(R.id.postCaption);
            taggedUsers = itemView.findViewById(R.id.rPostTags);
            rPostLocation = itemView.findViewById(R.id.rPostLocation);

            privacyIcon = itemView.findViewById(R.id.privacyIcon);
            postImage = itemView.findViewById(R.id.rPostImage);
            imageLoader = itemView.findViewById(R.id.post_item_image_loader);

            postLikeBTN = itemView.findViewById(R.id.postLikeBTN);
            postMenuBTN = itemView.findViewById(R.id.postMenuBTN);
            savePostBTN = itemView.findViewById(R.id.savePostBTN);

            likeCounter = itemView.findViewById(R.id.likeCounter);
            commentCounter = itemView.findViewById(R.id.commentCounter);

            commentLayout = itemView.findViewById(R.id.commentLayout);
            likesLayout = itemView.findViewById(R.id.likesArea);
            shareArea = itemView.findViewById(R.id.shareArea);
            postPicArea = itemView.findViewById(R.id.postPicArea);
            locationArea = itemView.findViewById(R.id.post_location_area);

            //for shared posts
            sharedPostProPic = itemView.findViewById(R.id.shared_image_user);
            sharedPostUsername = itemView.findViewById(R.id.shared_username);
            sharedPostDate = itemView.findViewById(R.id.shared_postDate);
            sharedPostCheckIn = itemView.findViewById(R.id.shared_postCheckIn);
            sharedPostDesc = itemView.findViewById(R.id.shared_post_desc);
            sharedProgressBar = itemView.findViewById(R.id.shared_progress_load_media);
            sharedLocationArea = itemView.findViewById(R.id.sharedLocationArea);
            //sharedPostImage = itemView.findViewById(R.id.shared_postImage);
            sharedPost = itemView.findViewById(R.id.shared_post_item);
            sharedPostVolumeBTN = itemView.findViewById(R.id.sharePostItemVolumeBTN);

            //for audio buttons
            playBTN = itemView.findViewById(R.id.postItem_playVoiceIcon);
            audioAnimation = itemView.findViewById(R.id.postItem_lav_playing);
            seekTimer = itemView.findViewById(R.id.postItemSeekTimer);
            postTotalTime = itemView.findViewById(R.id.postTotalTime);

            //for audio buttons
            shared_playBTN = itemView.findViewById(R.id.shared_postItem_playVoiceIcon);
            shared_audioAnimation = itemView.findViewById(R.id.shared_postItem_lav_playing);
            shared_seekTimer = itemView.findViewById(R.id.shared_postItemSeekTimer);
            shared_postTotalTime = itemView.findViewById(R.id.shared_postTotalTime);
        }
    }

    @Override
    public int getItemViewType(int position) {

        switch (postList.get(position).getPostType()){
            case "textPost":
                return TEXT_POST_ITEM;

            case "imagePost":
                return IMAGE_POST_ITEM;

            case "sharedTextPost":
                return SHARED_TEXT_POST_ITEM;

            case "sharedImagePost":
                return SHARED_IMAGE_POST_ITEM;

            case "audioPost":
                return AUDIO_POST_ITEM;

            case "audioImagePost":
                return AUDIO_IMAGE_POST_ITEM;

            case "sharedAudioTextPost":
                return SHARED_AUDIO_TEXT_POST;

            case "sharedAudioImagePost":
                return SHARED_AUDIO_IMAGE_POST;

            case "sharedTextAudioPost":
                return SHARED_TEXT_AUDIO_POST;

            case "sharedTextAudioImagePost":
                return SHARED_TEXT_AUDIO_IMAGE_POST;

            case "sharedAudioAudioPost":
                return SHARED_AUDIO_AUDIO_POST;

            case "sharedAudioAudioImagePost":
                return SHARED_AUDIO_AUDIO_IMAGE_POST;

            //case "sharedTextVideoPost":
            case "sharedVideoPost":
                return SHARED_TEXT_VIDEO_POST;

            case "sharedAudioTextVideoPost":
                return SHARED_AUDIO_VIDEO_POST;

            //case "sharedTextAudioVideoPost":
            case "sharedAudioVideoPost":
                return SHARED_TEXT_AUDIO_VIDEO_POST;

            case "sharedAudioAudioVideoPost":
                return SHARED_AUDIO_AUDIO_VIDEO_POST;

            default:
                throw new IllegalStateException("Unexpected value" + postList.get(position).getPostType());
        }

    }

    public void addAll(List<PostModel> postLists){
        int initSize = postList.size();
        postList.addAll(postLists);
        notifyItemRangeChanged(initSize, postLists.size());
    }

}