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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
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
import com.makepe.blackout.GettingStarted.InAppActivities.SharePostActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.StoryActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.Movement;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioPlayer;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalNotifications;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyHolder> {

    private Context context;
    private List<PostModel> postList;
    private FirebaseUser firebaseUser;
    private DatabaseReference postReference, userReference, movementReference,
            movementPostReference, followingReference;

    private UniversalFunctions universalFunctions;
    private UniversalNotifications universalNotifications;
    private GetTimeAgo getTimeAgo;
    private AudioPlayer audioPlayer;

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

    private boolean isFollowing = false;
    private String followingText;

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
        movementReference = FirebaseDatabase.getInstance().getReference("Movements");
        movementPostReference = FirebaseDatabase.getInstance().getReference("MovementPosts");
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

            default:
                Toast.makeText(context, "Unknown post type identified", Toast.LENGTH_SHORT).show();
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

        audioPlayer = new AudioPlayer(context, holder.playBTN,
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
                }else{
                    //for retrieving normal movement image posts only
                    movementPostReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){

                                PostModel movementPost = snapshot.getValue(PostModel.class);
                                assert movementPost != null;
                                holder.postCaption.setText(movementPost.getPostCaption());
                                try{
                                    Picasso.get().load(movementPost.getPostImage()).into(holder.postImage);
                                    holder.imageLoader.setVisibility(View.GONE);
                                }catch (NullPointerException e){
                                    Picasso.get().load(R.drawable.ic_image_black_24dp).into(holder.postImage);
                                }

                                try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                                    holder.postTimeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(movementPost.getPostTime()), context));
                                }catch (NumberFormatException n){
                                    Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                                }//for converting timestamp
                                getMovementUserDetails(holder, post);
                            }else{
                                Toast.makeText(context, "Could not retrieve post from database", Toast.LENGTH_SHORT).show();
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

                }else{
                    movementPostReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel movementPost = snapshot.getValue(PostModel.class);

                                assert movementPost != null;
                                holder.postCaption.setText(movementPost.getPostCaption());

                                try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                                    holder.postTimeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(movementPost.getPostTime()), context));
                                }catch (NumberFormatException n){
                                    Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                                }//for converting timestamp

                                getMovementUserDetails(holder, post);
                            }else{
                                Toast.makeText(context, "Could not retrieve post from database", Toast.LENGTH_SHORT).show();
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

                }else{
                    movementPostReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel postModel = snapshot.getValue(PostModel.class);

                                assert postModel != null;
                                try{
                                    Picasso.get().load(postModel.getPostImage()).into(holder.postImage);
                                    holder.imageLoader.setVisibility(View.GONE);
                                }catch (NullPointerException e){
                                    Picasso.get().load(R.drawable.ic_image_black_24dp).into(holder.postImage);
                                }

                                try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                                    holder.postTimeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(postModel.getPostTime()), context));
                                }catch (NumberFormatException n){
                                    Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                                }//for converting timestamp

                                try{
                                    universalFunctions.findAddress(postModel.getLatitude(), postModel.getLongitude(), holder.rPostLocation, holder.locationArea);
                                }catch (Exception ignored){}

                                getMovementUserDetails(holder, postModel);
                                getNormalPostAudio(holder, postModel);
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

                }else{
                    movementPostReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel postModel = snapshot.getValue(PostModel.class);

                                assert postModel != null;

                                try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                                    holder.postTimeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(postModel.getPostTime()), context));
                                }catch (NumberFormatException n){
                                    Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                                }//for converting timestamp

                                try{
                                    universalFunctions.findAddress(postModel.getLatitude(), postModel.getLongitude(), holder.rPostLocation, holder.locationArea);
                                }catch (Exception ignored){}

                                getMovementUserDetails(holder, postModel);
                                getNormalPostAudio(holder, postModel);
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

    private void getMovementUserDetails(MyHolder holder, PostModel post) {
        movementReference.child(post.getMovementID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Movement movement = snapshot.getValue(Movement.class);

                    userReference.child(post.getUserID()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                User user = snapshot.getValue(User.class);
                                holder.postUsername.setText(user.getUsername() + " > " + movement.getMovementName());

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
        iniPicPopUp(context, holder, post.getUserID());
        universalFunctions.getCommentsCount(post.getPostID(), holder.commentCounter);
        checkFollowing(post);

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
                        }else{
                            movementPostReference.child(post.getPostID()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){

                                        Intent intent = new Intent(context, FullScreenImageActivity.class);
                                        intent.putExtra("itemID", post.getPostID());
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
                menuOptions(holder.postMenuBTN, post);
            }
        });

        holder.commentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //holder.commentDialog.show();
                Intent postDetailIntent= new Intent(context, CommentsActivity.class);
                postDetailIntent.putExtra("postID", post.getPostID());
                context.startActivity(postDetailIntent);
            }
        });

        holder.postProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.postProPic.getTag().equals("storyActive")){
                    Intent intent = new Intent(context, StoryActivity.class);
                    intent.putExtra("userid", post.getUserID());
                    context.startActivity(intent);
                }else if(holder.postProPic.getTag().equals("noStories")){
                    Intent picIntent = new Intent(context, FullScreenImageActivity.class);
                    picIntent.putExtra("itemID", post.getUserID());
                    picIntent.putExtra("reason", "userImage");
                    context.startActivity(picIntent);
                }
                //holder.popAddPost.show();
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
                    if (!firebaseUser.getUid().equals(post.getUserID()))
                        universalNotifications.addLikesNotifications(post.getUserID(), post.getPostID());
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


    //other post functions
    private void iniPicPopUp(final Context context, final MyHolder holder, final String uid) {
        holder.popAddPost = new Dialog(context);
        holder.popAddPost.setContentView(R.layout.profile_pic_pop_up_layout);
        holder.popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        holder.popAddPost.getWindow().setLayout(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        holder.popAddPost.getWindow().getAttributes().gravity = Gravity.CENTER;

        ImageView viewProfile = holder.popAddPost.findViewById(R.id.popUP_ViewProfile);
        ImageView sendMessage = holder.popAddPost.findViewById(R.id.popUP_SendMessage);
        ImageView superProPic = holder.popAddPost.findViewById(R.id.popUP_ProPic);
        ImageView callBTN = holder.popAddPost.findViewById(R.id.popUP_callUser);
        RelativeLayout popButtonsArea = holder.popAddPost.findViewById(R.id.popButtonsArea);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUSER_ID().equals(firebaseUser.getUid())){

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

        if(uid.equals(firebaseUser.getUid())){
            popButtonsArea.setVisibility(View.GONE);

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

                if(!uid.equals(firebaseUser.getUid())){
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

    private void menuOptions(ImageView postMenuBTN, PostModel post) {
        PopupMenu popupMenu = new PopupMenu(context, postMenuBTN, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0,0,"View Profile");

        if(post.getUserID().equals(firebaseUser.getUid())){
            popupMenu.getMenu().add(Menu.NONE, 1,0,"Delete Post");
            popupMenu.getMenu().add(Menu.NONE, 2,0,"Edit Post");
            popupMenu.getMenu().add(Menu.NONE, 3,0,"Post Views");
        }

        if (!post.getUserID().equals(firebaseUser.getUid())){
            if (isFollowing){
                userReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            User user = ds.getValue(User.class);

                            assert user != null;
                            if (user.getUSER_ID().equals(post.getUserID())){
                                popupMenu.getMenu().add(Menu.NONE, 4, 0, "Unfollow " + user.getUsername());
                            }else{
                                movementReference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot snap : snapshot.getChildren()){
                                            Movement movement = snap.getValue(Movement.class);

                                            assert movement != null;
                                            if (movement.getMovementID().equals(post.getMovementID())){
                                                popupMenu.getMenu().add(Menu.NONE, 4, 0, "Unfollow " + movement.getMovementName());
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
            }else{
                userReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            User user = ds.getValue(User.class);

                            assert user != null;
                            if (user.getUSER_ID().equals(post.getUserID())){
                                popupMenu.getMenu().add(Menu.NONE, 4, 0, "Follow " + user.getUsername());
                            }else{
                                movementReference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot snap : snapshot.getChildren()){
                                            Movement movement = snap.getValue(Movement.class);

                                            assert movement != null;
                                            if (movement.getMovementID().equals(post.getUserID())){
                                                popupMenu.getMenu().add(Menu.NONE, 4, 0, "Follow " + movement.getMovementName());
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
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case 0:
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
                        break;

                    case 1:
                        universalFunctions.beginDelete(post.getUserID(), post.getPostImage());
                        break;

                    case 2:
                        Toast.makeText(context, "Edit Post Details", Toast.LENGTH_SHORT).show();
                        break;

                    case 3:
                        Intent intent = new Intent(context, ConnectionsActivity.class);
                        intent.putExtra("UserID", post.getUserID());
                        intent.putExtra("Interaction", "Views");
                        context.startActivity(intent);
                        break;

                    case 4:
                        Toast.makeText(context, "You will be able to follow/unfollow this account", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        Toast.makeText(context, "Unknown Selection made", Toast.LENGTH_SHORT).show();

                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void checkFollowing(PostModel post) {
        followingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(post.getUserID()).exists()){
                    isFollowing = true;
                }else{
                    isFollowing = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSharedAudio(MyHolder holder, PostModel sharedPost) {

        audioPlayer = new AudioPlayer(context, holder.shared_playBTN,
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

        audioPlayer = new AudioPlayer(context, holder.playBTN,
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

    public class MyHolder extends RecyclerView.ViewHolder {
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
            sharedPostImage = itemView.findViewById(R.id.shared_postImage);
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

            default:
                throw new IllegalStateException("Unexpected value" + postList.get(position).getPostType());
        }
    }
}