package com.makepe.blackout.GettingStarted.Adapters;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MediaContent;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.makepe.blackout.GettingStarted.InAppActivities.ChatActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.CommentsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenImageActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.PhoneCallActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.PostListActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.SharePostActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.StoryActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.CommentModel;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.Notifications.SendNotifications;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioPlayer;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioRecorder;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;
    private List<PostModel> postList;

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
    public static final int VIEW_TYPE_AD = 1300;

    public static final String TAG = "POST_TAG";

    private FirebaseUser firebaseUser;
    private DatabaseReference postReference, userReference, commentsReference, likesReference;

    private UniversalFunctions universalFunctions;
    private GetTimeAgo getTimeAgo;

    private boolean isShared = false;

    //for comments
    private StorageReference commentAudioReference;
    private SendNotifications notifications;
    private String commentID;
    private EditText commentET;
    private ProgressDialog commentDialog;

    public TimelineAdapter(Context context, List<PostModel> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType){

            case TEXT_POST_ITEM:
                return new HolderPosts(LayoutInflater.from(context).inflate(R.layout.raw_post, parent, false));

            case IMAGE_POST_ITEM:
                return new HolderPosts(LayoutInflater.from(context).inflate(R.layout.raw_image_post, parent, false));

            case SHARED_TEXT_POST_ITEM:
                return new HolderPosts(LayoutInflater.from(context).inflate(R.layout.shared_text_post_item, parent, false));

            case SHARED_IMAGE_POST_ITEM:
                return new HolderPosts(LayoutInflater.from(context).inflate(R.layout.shared_image_post_item, parent, false));

            case AUDIO_POST_ITEM:
                return new HolderPosts(LayoutInflater.from(context).inflate(R.layout.audio_post_item, parent, false));

            case AUDIO_IMAGE_POST_ITEM:
                return new HolderPosts(LayoutInflater.from(context).inflate(R.layout.audio_image_post_item, parent, false));

            case SHARED_AUDIO_IMAGE_POST:
                return new HolderPosts(LayoutInflater.from(context).inflate(R.layout.shared_audio_image_post_item, parent, false));

            case SHARED_AUDIO_TEXT_POST:
                return new HolderPosts(LayoutInflater.from(context).inflate(R.layout.shared_audio_text_item, parent, false));

            case SHARED_TEXT_AUDIO_POST:
                return new HolderPosts(LayoutInflater.from(context).inflate(R.layout.shared_text_audio_post_item, parent, false));

            case SHARED_TEXT_AUDIO_IMAGE_POST:
                return new HolderPosts(LayoutInflater.from(context).inflate(R.layout.shared_text_audio_image_item, parent, false));

            case SHARED_AUDIO_AUDIO_POST:
                return new HolderPosts(LayoutInflater.from(context).inflate(R.layout.shared_audio_audio_post_item, parent, false));

            case SHARED_AUDIO_AUDIO_IMAGE_POST:
                return new HolderPosts(LayoutInflater.from(context).inflate(R.layout.shared_audio_audio_image_item, parent, false));

            case VIEW_TYPE_AD:
                return new HolderNativeAd(LayoutInflater.from(context).inflate(R.layout.native_ad_row_item, parent, false));

            default:
                throw new IllegalStateException("Unexpected value " + viewType);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position) == VIEW_TYPE_AD){
            AdLoader adLoader = new AdLoader.Builder(context, context.getString(R.string.native_ad_test))
                    .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                        @Override
                        public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                            Log.d(TAG, "onNativeAdLoaded: ");

                            HolderNativeAd holderNativeAd = (HolderNativeAd) holder;
                            displayNativeAd(holderNativeAd, nativeAd);
                        }
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdClicked() {
                            super.onAdClicked();
                            Log.d(TAG, "onAdClicked: Ad Clicked");
                        }

                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            Log.d(TAG, "onAdClosed: Ad Closed");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            Log.d(TAG, "onAdFailedToLoad: Ad Failed due to " + loadAdError.getMessage());
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                            Log.d(TAG, "onAdImpression: ");
                        }

                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                        }

                        @Override
                        public void onAdOpened() {
                            super.onAdOpened();
                        }
                    })
                    .withNativeAdOptions(new NativeAdOptions.Builder().build()).build();

            adLoader.loadAd(new AdRequest.Builder().build());
        }else{
            PostModel post = postList.get(position);
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            userReference = FirebaseDatabase.getInstance().getReference("Users");
            postReference = FirebaseDatabase.getInstance().getReference("SecretPosts");
            commentsReference = FirebaseDatabase.getInstance().getReference("Comments");
            likesReference = FirebaseDatabase.getInstance().getReference("Likes");
            commentAudioReference = FirebaseStorage.getInstance().getReference();

            universalFunctions = new UniversalFunctions(context);
            getTimeAgo = new GetTimeAgo();
            notifications = new SendNotifications(context);

            HolderPosts holderPosts = (HolderPosts) holder;

            ((HolderPosts) holder).commentAreaVoiceBTN.setTag("notRecording");

            getAdapterFunctions(post, holderPosts);

            ((HolderPosts) holder).commentAreaMessage.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.length() != 0)
                        ((HolderPosts) holder).commentAreaVoiceBTN.setImageResource(R.drawable.ic_send_black_24dp);
                    else
                        ((HolderPosts) holder).commentAreaVoiceBTN.setImageResource(R.drawable.ic_mic_black_24dp);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            switch (getItemViewType(position)){
                case IMAGE_POST_ITEM:

                    holderPosts.postImageRecyclerView.hasFixedSize();
                    holderPosts.postImageRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                    new LinearSnapHelper().attachToRecyclerView(holderPosts.postImageRecyclerView);

                    displayImagePost(holderPosts, post);
                    break;

                case TEXT_POST_ITEM:
                    displayTextPost(holderPosts, post);
                    break;

                case AUDIO_POST_ITEM:
                    displayAudioPost(holderPosts, post);
                    break;

                case AUDIO_IMAGE_POST_ITEM:

                    holderPosts.postImageRecyclerView.hasFixedSize();
                    holderPosts.postImageRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                    new LinearSnapHelper().attachToRecyclerView(holderPosts.postImageRecyclerView);

                    displayAudioImagePost(holderPosts, post);
                    break;

                case SHARED_TEXT_POST_ITEM:
                    displaySharedTextPost(holderPosts, post);
                    getSharedPostButtons(holderPosts, post);
                    break;

                case SHARED_IMAGE_POST_ITEM:

                    holderPosts.sharedPostImageRecyclerView.hasFixedSize();
                    holderPosts.sharedPostImageRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                    new LinearSnapHelper().attachToRecyclerView(holderPosts.sharedPostImageRecyclerView);

                    displaySharedImagePost(holderPosts, post);
                    getSharedPostButtons(holderPosts, post);
                    break;

                case SHARED_AUDIO_IMAGE_POST:

                    holderPosts.sharedPostImageRecyclerView.hasFixedSize();
                    holderPosts.sharedPostImageRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                    new LinearSnapHelper().attachToRecyclerView(holderPosts.sharedPostImageRecyclerView);

                    displaySharedAudioImageTextPost(holderPosts, post);
                    getSharedPostButtons(holderPosts, post);
                    break;

                case SHARED_AUDIO_TEXT_POST:
                    displaySharedAudioTextPost(holderPosts, post);
                    getSharedPostButtons(holderPosts, post);
                    break;

                case SHARED_TEXT_AUDIO_POST:
                    displaySharedTextAudioPost(holderPosts, post);
                    getSharedPostButtons(holderPosts, post);
                    break;

                case SHARED_TEXT_AUDIO_IMAGE_POST:

                    holderPosts.sharedPostImageRecyclerView.hasFixedSize();
                    holderPosts.sharedPostImageRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                    new LinearSnapHelper().attachToRecyclerView(holderPosts.sharedPostImageRecyclerView);

                    displaySharedTextImageAudioPost(holderPosts, post);
                    getSharedPostButtons(holderPosts, post);
                    break;

                case SHARED_AUDIO_AUDIO_POST:
                    displaySharedAudioAudioPost(holderPosts, post);
                    getSharedPostButtons(holderPosts, post);
                    break;

                case SHARED_AUDIO_AUDIO_IMAGE_POST:

                    holderPosts.sharedPostImageRecyclerView.hasFixedSize();
                    holderPosts.sharedPostImageRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                    new LinearSnapHelper().attachToRecyclerView(holderPosts.sharedPostImageRecyclerView);

                    displaySharedAudioImageAudioPost(holderPosts, post);
                    getSharedPostButtons(holderPosts, post);
                    break;

                default:
                    Toast.makeText(context, "Unknown post type identified " + post.getPostType(), Toast.LENGTH_SHORT).show();
            }
        }

    }


    //--------for retrieving ad details
    private void displayNativeAd(HolderNativeAd holder, NativeAd nativeAd) {
        String headline = nativeAd.getHeadline();
        String body = nativeAd.getBody();
        String callToAction = nativeAd.getCallToAction();
        NativeAd.Image icon = nativeAd.getIcon();
        String price = nativeAd.getPrice();
        String store = nativeAd.getStore();
        String advertiser = nativeAd.getAdvertiser();
        Double starRating = nativeAd.getStarRating();
        MediaContent mediaContent = nativeAd.getMediaContent();

        if (body == null){
            holder.ad_body.setVisibility(View.INVISIBLE);
        }else{
            holder.ad_body.setVisibility(View.VISIBLE);
            holder.ad_body.setText(body);
        }

        if (icon == null){
            holder.ad_app_icon.setVisibility(View.INVISIBLE);
        }else{
            holder.ad_app_icon.setVisibility(View.VISIBLE);
            holder.ad_app_icon.setImageDrawable(icon.getDrawable());
        }

        if (headline == null){
            holder.ad_headline.setVisibility(View.INVISIBLE);
        }else{
            holder.ad_headline.setVisibility(View.VISIBLE);
            holder.ad_headline.setText(headline);
        }

        if (price == null){
            holder.ad_price.setVisibility(View.INVISIBLE);
        }else{
            holder.ad_price.setVisibility(View.VISIBLE);
            holder.ad_price.setText(price);
        }

        if (store == null){
            holder.ad_store.setVisibility(View.INVISIBLE);
        }else{
            holder.ad_store.setVisibility(View.VISIBLE);
            holder.ad_store.setText(store);
        }

        if (advertiser == null){
            holder.ad_advertiser.setVisibility(View.INVISIBLE);
        }else{
            holder.ad_advertiser.setVisibility(View.VISIBLE);
            holder.ad_advertiser.setText(advertiser);
        }

        if (callToAction == null){
            holder.adCallToAction.setVisibility(View.INVISIBLE);
        }else{
            holder.adCallToAction.setVisibility(View.VISIBLE);
            holder.adCallToAction.setText(callToAction);
            holder.nativeAdView.setCallToActionView(holder.adCallToAction);
        }

        if (starRating == null){
            holder.ad_stars.setVisibility(View.INVISIBLE);
        }else{
            holder.ad_stars.setVisibility(View.VISIBLE);
            holder.ad_stars.setRating(starRating.floatValue());
        }

        if (mediaContent == null){
            holder.mediaView.setVisibility(View.INVISIBLE);
        }else{
            holder.mediaView.setVisibility(View.VISIBLE);
            holder.mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.mediaView.setMediaContent(mediaContent);
        }

        holder.nativeAdView.setNativeAd(nativeAd);

    }


    //-----------for retrieving shared posts only
    private void displaySharedTextPost(HolderPosts holder, PostModel post) {

        postReference.child(post.getPostID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel model = snapshot.getValue(PostModel.class);

                    assert model != null;
                    displayTextPost(holder, model);

                    holder.shareCounter.setVisibility(View.GONE);

                    postReference.child(model.getSharedPost()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel sharedPost = snapshot.getValue(PostModel.class);

                                assert sharedPost != null;
                                holder.sharedPostDesc.setText(sharedPost.getPostCaption());
                                isShared = true;

                                getSharedPostOwner(holder, sharedPost);
                                checkSharedTaggedFriends(holder, sharedPost);
                                getUniversalSharedFunctions(holder, sharedPost);
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

    private void displaySharedImagePost(HolderPosts holder, PostModel post) {
        ArrayList<String> imageList = new ArrayList<>();

        postReference.child(post.getPostID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel postModel = snapshot.getValue(PostModel.class);

                    assert postModel != null;
                    displayTextPost(holder, postModel);

                    postReference.child(post.getSharedPost()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel sharedPost = snapshot.getValue(PostModel.class);

                                assert sharedPost != null;
                                holder.sharedPostDesc.setText(sharedPost.getPostCaption());
                                isShared = true;

                                postReference.child(post.getSharedPost()).child("images").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            imageList.clear();
                                            for (DataSnapshot ds : snapshot.getChildren()){
                                                imageList.add(ds.getValue().toString());
                                                holder.sharedProgressBar.setVisibility(View.GONE);
                                            }
                                            holder.sharedPostImageRecyclerView.setAdapter(new PostImageAdapter(context, imageList));
                                            holder.sharedPostImagesCounter.setText("" + snapshot.getChildrenCount());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                getSharedPostOwner(holder, sharedPost);
                                getUniversalSharedFunctions(holder, sharedPost);
                                checkSharedTaggedFriends(holder, sharedPost);
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

        holder.sharedPostDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharedPostIntent = new Intent(context, PostListActivity.class);
                sharedPostIntent.putExtra("postID", post.getSharedPost());
                context.startActivity(sharedPostIntent);
            }
        });
    }

    private void displaySharedAudioTextPost(HolderPosts holder, PostModel post) {

        postReference.child(post.getPostID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel postModel = snapshot.getValue(PostModel.class);

                    assert postModel != null;
                    displayAudioPost(holder, postModel);

                    postReference.child(postModel.getSharedPost()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel sharedPost = snapshot.getValue(PostModel.class);

                                assert sharedPost != null;
                                holder.sharedPostDesc.setText(sharedPost.getPostCaption());
                                isShared = true;

                                getUniversalSharedFunctions(holder, sharedPost);
                                getSharedPostOwner(holder, sharedPost);
                                checkSharedTaggedFriends(holder, sharedPost);
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

    private void displaySharedAudioImageTextPost(HolderPosts holder, PostModel post) {
        ArrayList<String> imageList = new ArrayList<>();

        postReference.child(post.getPostID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel model = snapshot.getValue(PostModel.class);

                    assert model != null;
                    displayAudioPost(holder, model);

                    holder.shareCounter.setVisibility(View.GONE);

                    postReference.child(model.getSharedPost()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel sharedPost = snapshot.getValue(PostModel.class);

                                assert sharedPost != null;
                                holder.sharedPostDesc.setText(sharedPost.getPostCaption());
                                isShared = true;

                                postReference.child(sharedPost.getPostID()).child("images").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            imageList.clear();
                                            for (DataSnapshot ds : snapshot.getChildren()){
                                                imageList.add(ds.getValue().toString());
                                                holder.sharedProgressBar.setVisibility(View.GONE);
                                            }
                                            holder.sharedPostImageRecyclerView.setAdapter(new PostImageAdapter(context, imageList));
                                            holder.sharedPostImagesCounter.setText("" + snapshot.getChildrenCount());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                getSharedPostOwner(holder, sharedPost);
                                getUniversalSharedFunctions(holder, sharedPost);
                                checkSharedTaggedFriends(holder, sharedPost);
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

        holder.sharedPostDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharedPostIntent = new Intent(context, PostListActivity.class);
                sharedPostIntent.putExtra("postID", post.getSharedPost());
                context.startActivity(sharedPostIntent);
            }
        });
    }

    private void displaySharedTextAudioPost(HolderPosts holder, PostModel post) {

        postReference.child(post.getPostID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel model = snapshot.getValue(PostModel.class);

                    assert model != null;
                    displayTextPost(holder, model);

                    holder.shareCounter.setVisibility(View.GONE);

                    postReference.child(model.getSharedPost()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel sharedPost = snapshot.getValue(PostModel.class);

                                assert sharedPost != null;
                                isShared = true;

                                getSharedPostOwner(holder, sharedPost);
                                getSharedAudio(holder, sharedPost);
                                checkSharedTaggedFriends(holder, sharedPost);
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

    private void displaySharedTextImageAudioPost(HolderPosts holder, PostModel post) {
        ArrayList<String> imageList = new ArrayList<>();

        postReference.child(post.getPostID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel model = snapshot.getValue(PostModel.class);

                    assert model != null;
                    displayTextPost(holder, model);

                    holder.shareCounter.setVisibility(View.GONE);
                    isShared = true;

                    postReference.child(model.getSharedPost()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel sharedPost = snapshot.getValue(PostModel.class);

                                assert sharedPost != null;
                                postReference.child(sharedPost.getPostID()).child("images").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            imageList.clear();
                                            for (DataSnapshot ds : snapshot.getChildren()){
                                                imageList.add(ds.getValue().toString());
                                                holder.sharedProgressBar.setVisibility(View.GONE);
                                            }
                                            holder.sharedPostImageRecyclerView.setAdapter(new PostImageAdapter(context, imageList));
                                            holder.sharedPostImagesCounter.setText("" + snapshot.getChildrenCount());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                getSharedPostOwner(holder, sharedPost);
                                getSharedAudio(holder, sharedPost);
                                checkSharedTaggedFriends(holder, sharedPost);
                                getUniversalSharedFunctions(holder, sharedPost);
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

        holder.sharedPostDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharedPostIntent = new Intent(context, PostListActivity.class);
                sharedPostIntent.putExtra("postID", post.getSharedPost());
                context.startActivity(sharedPostIntent);
            }
        });
    }

    private void displaySharedAudioImageAudioPost(HolderPosts holder, PostModel post) {
        ArrayList<String> imageList = new ArrayList<>();

        postReference.child(post.getPostID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel postModel = snapshot.getValue(PostModel.class);

                    assert postModel != null;
                    displayAudioPost(holder, postModel);

                    holder.shareCounter.setVisibility(View.GONE);

                    postReference.child(postModel.getSharedPost()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel sharedPost = snapshot.getValue(PostModel.class);

                                assert sharedPost != null;

                                isShared = true;

                                postReference.child(sharedPost.getPostID()).child("images").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            imageList.clear();
                                            for (DataSnapshot ds : snapshot.getChildren()){
                                                imageList.add(ds.getValue().toString());
                                                holder.sharedProgressBar.setVisibility(View.GONE);
                                            }
                                            holder.sharedPostImageRecyclerView.setAdapter(new PostImageAdapter(context, imageList));
                                            holder.sharedPostImagesCounter.setText("" + snapshot.getChildrenCount());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                getSharedPostOwner(holder, sharedPost);
                                getSharedAudio(holder, sharedPost);
                                checkSharedTaggedFriends(holder, sharedPost);
                                getUniversalSharedFunctions(holder, sharedPost);
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

        holder.sharedPostDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharedPostIntent = new Intent(context, PostListActivity.class);
                sharedPostIntent.putExtra("postID", post.getSharedPost());
                context.startActivity(sharedPostIntent);
            }
        });

    }

    private void displaySharedAudioAudioPost(HolderPosts holder, PostModel post) {

        postReference.child(post.getPostID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel postModel = snapshot.getValue(PostModel.class);

                    assert postModel != null;
                    displayAudioPost(holder, postModel);

                    holder.shareCounter.setVisibility(View.GONE);

                    postReference.child(postModel.getSharedPost()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                PostModel sharedPost = snapshot.getValue(PostModel.class);

                                assert sharedPost != null;

                                isShared = true;
                                getSharedPostOwner(holder, sharedPost);
                                getSharedAudio(holder, sharedPost);
                                getUniversalSharedFunctions(holder, sharedPost);

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
    private void displayImagePost(HolderPosts holder, PostModel post) {
        ArrayList<String> imageList = new ArrayList<>();

        postReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel model = ds.getValue(PostModel.class);

                    assert model != null;
                    if (model.getPostID().equals(post.getPostID())){

                        holder.postCaption.setText(model.getPostCaption());

                        try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                            holder.postTimeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(model.getPostTime()), context));
                        }catch (NumberFormatException n){
                            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                        }//for converting timestamp

                        try{
                            universalFunctions.findAddress(model.getLatitude(), model.getLongitude(), holder.rPostLocation, holder.locationArea);
                        }catch (Exception ignored){}

                        postReference.child(model.getPostID()).child("images").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    imageList.clear();
                                    for (DataSnapshot data : snapshot.getChildren()) {
                                        imageList.add(data.getValue().toString());
                                    }
                                    holder.imageLoader.setVisibility(View.GONE);
                                    holder.postImageRecyclerView.setAdapter(new PostImageAdapter(context, imageList));

                                    holder.postImagesCounter.setText("" + snapshot.getChildrenCount());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        getPostUserDetails(holder, model);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void displayTextPost(HolderPosts holder, PostModel post) {
        postReference.child(post.getPostID()).addListenerForSingleValueEvent(new ValueEventListener() {
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

                    try{
                        universalFunctions.findAddress(model.getLatitude(), model.getLongitude(), holder.rPostLocation, holder.locationArea);
                    }catch (Exception ignored){}

                    getPostUserDetails(holder, post);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayAudioImagePost(HolderPosts holder, PostModel post) {
        ArrayList<String> imageList = new ArrayList<>();

        postReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    PostModel model = ds.getValue(PostModel.class);

                    assert model != null;
                    if (model.getPostID().equals(post.getPostID())){

                        try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                            holder.postTimeStamp.setText(getTimeAgo.getTimeAgo(Long.parseLong(model.getPostTime()), context));
                        }catch (NumberFormatException n){
                            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                        }//for converting timestamp

                        try{
                            universalFunctions.findAddress(model.getLatitude(), model.getLongitude(), holder.rPostLocation, holder.locationArea);
                        }catch (Exception ignored){}

                        postReference.child(model.getPostID()).child("images").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    imageList.clear();
                                    for (DataSnapshot data : snapshot.getChildren()) {
                                        imageList.add(data.getValue().toString());
                                    }
                                    holder.imageLoader.setVisibility(View.GONE);
                                    holder.postImageRecyclerView.setAdapter(new PostImageAdapter(context, imageList));

                                    holder.postImagesCounter.setText("" + snapshot.getChildrenCount());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        getPostUserDetails(holder, model);
                        getNormalPostAudio(holder, model);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayAudioPost(HolderPosts holder, PostModel post) {

        postReference.child(post.getPostID()).addListenerForSingleValueEvent(new ValueEventListener() {
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


    //------------for checking tagged friends for both shared and normal posts
    private void checkTags(HolderPosts holder, PostModel post) {

        postReference.child(post.getPostID()).child("taggedFriends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    if (snapshot.getChildrenCount() == 1)
                        holder.taggedPeopleList.setText("with: "+ snapshot.getChildrenCount() +" friend");
                    else
                        holder.taggedPeopleList.setText("with: "+ snapshot.getChildrenCount() +" friends");

                    holder.tagsArea.setVisibility(View.VISIBLE);

                }else{
                    holder.tagsArea.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkSharedTaggedFriends(HolderPosts holder, PostModel sharedPost) {
        postReference.child(sharedPost.getPostID()).child("taggedFriends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    if (snapshot.getChildrenCount() == 1)
                        holder.sharedPost_taggedPeopleList.setText("with: " + snapshot.getChildrenCount() + " friend");
                    else
                        holder.sharedPost_taggedPeopleList.setText("with: " + snapshot.getChildrenCount() + " friends");

                    holder.sharedPost_taggedPeopleList.setVisibility(View.VISIBLE);
                }else{
                    holder.sharedPost_taggedPeopleList.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //-----------for retrieving user details
    private void getPostUserDetails(HolderPosts holder, PostModel post) {
        userReference.child(post.getUserID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    assert user != null;

                    if (user.getUserID().equals(firebaseUser.getUid()))
                        holder.postUsername.setText("Me");
                    else
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

    private void getSharedPostOwner(HolderPosts holder, PostModel sharedPost) {
        userReference.child(sharedPost.getUserID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    if(user.getUserID().equals(firebaseUser.getUid()))
                        holder.sharedPostUsername.setText("Me");
                    else
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
    private void getAdapterFunctions(PostModel post, HolderPosts holder) {
        iniPicPopUp(context, holder, post);
        universalFunctions.getCommentsCount(post.getPostID(), holder.commentCounter);

        universalFunctions.isLiked(post.getPostID(), holder.postLikeBTN);
        universalFunctions.nrLikes(holder.likeCounter, post.getPostID());
        universalFunctions.isSaved(post.getPostID(), holder.savePostBTN);
        universalFunctions.checkActiveStories(holder.postProPic, post.getUserID());
        universalFunctions.getSharedNumber(post.getPostID(), holder.shareCounter);

        onClickListeners(holder, post);
        getPostComments(holder, post);
        checkTags(holder, post);
        getMyUserDetails(holder);

        /*try{
            if (!post.isCommentsAllowed()){
                holder.commentLayout.setVisibility(View.INVISIBLE);
                holder.commentCounter.setVisibility(View.GONE);
            }
        } catch (NullPointerException ignored){}*/
    }

    private void getMyUserDetails(HolderPosts holder) {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUserID().equals(firebaseUser.getUid()))
                        try {
                            Picasso.get().load(user.getImageURL()).fit().into(holder.commentAreaProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.commentAreaProPic);
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSharedPostButtons(HolderPosts holder, PostModel post) {

        holder.sharedPost_taggedPeopleList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSharedTaggedFriendsList(holder, post);
            }
        });

        holder.sharedPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharedPostIntent = new Intent(context, CommentsActivity.class);
                sharedPostIntent.putExtra("postID", post.getSharedPost());
                context.startActivity(sharedPostIntent);
            }
        });
    }

    private void onClickListeners(HolderPosts holder, PostModel post){

        AudioRecorder audioRecorder = new AudioRecorder(holder.lavPlaying, context,
                (Activity) context, holder.playAudioArea, holder.voicePlayBTN, holder.recorderDurationTV);

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

                showCommentsDialog(post);

                /*universalFunctions.addView(post.getPostID());

                Intent postDetailIntent= new Intent(context, CommentsActivity.class);
                postDetailIntent.putExtra("postID", post.getPostID());
                context.startActivity(postDetailIntent);*/
            }
        });

        holder.postProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.popAddPost.show();
            }
        });//popping up profile picture

        holder.likeCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!holder.likeCounter.getText().toString().equals("0 Likes"))
                    showLikesDialog(post);
                else
                    Toast.makeText(context, "This post has no likes", Toast.LENGTH_SHORT).show();
            }
        });

        holder.likesLayout.setOnClickListener(new View.OnClickListener() {
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

                if (!firebaseUser.getUid().equals(post.getUserID())) {
                    Intent shareIntent = new Intent(context, SharePostActivity.class);
                    if (!isShared) {
                        shareIntent.putExtra("postID", post.getPostID());
                    }else{
                        shareIntent.putExtra("postID", post.getSharedPost());
                    }
                    context.startActivity(shareIntent);
                }else
                    Toast.makeText(context, "You can not share your own post", Toast.LENGTH_SHORT).show();
            }
        });

        holder.shareCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!holder.shareCounter.getText().toString().equals("0 Shares")) {

                    showSharesDialog(post);
                }else{
                    Toast.makeText(context, "This post has no shares", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.tagsArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTaggedFriendsDialog(holder, post);
            }
        });

        holder.commentAreaVoiceBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(holder.commentAreaMessage.getText().toString())
                        && holder.commentAreaVoiceBTN.getTag().equals("notRecording")){

                    if (audioRecorder.checkRecordingPermission()){
                        holder.commentAreaVoiceBTN.setImageResource(R.drawable.ic_baseline_stop_circle_24);

                        if (!audioRecorder.isRecording){
                            audioRecorder.startRecording();
                            holder.commentAreaVoiceBTN.setTag("Recording");
                        }

                    }else{
                        audioRecorder.requestRecordingPermission();
                    }

                }else if (holder.commentAreaVoiceBTN.getTag().equals("Recording")){

                    try {
                        audioRecorder.stopRecording();
                        holder.commentAreaVoiceBTN.setTag("sendAudio");
                        holder.commentAreaVoiceBTN.setImageResource(R.drawable.ic_send_black_24dp);
                    }catch (NullPointerException e){
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }else{
                    commentID = commentsReference.push().getKey();

                    if (!TextUtils.isEmpty(holder.commentAreaMessage.getText().toString()))

                        uploadTextComment(post, holder.commentAreaMessage);

                    else if (audioRecorder.getRecordingFilePath() != null){

                        if (holder.commentAreaVoiceBTN.getTag().equals("sendAudio"))
                            uploadAudioComment(post, audioRecorder);

                        holder.commentAreaVoiceBTN.setImageResource(R.drawable.ic_mic_black_24dp);

                    }
                }
            }
        });

        holder.deleteAudioBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioRecorder.resetRecorder();
                holder.commentAreaVoiceBTN.setImageResource(R.drawable.ic_mic_black_24dp);
                holder.commentAreaVoiceBTN.setTag("notRecording");
            }
        });

        holder.voicePlayBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!audioRecorder.isPlaying()){
                    audioRecorder.startPlayingRecording();
                }else{
                    audioRecorder.stopPlayingAudio();
                }
            }
        });

    }


    //-----------below is for all dialogs
    private void showSharesDialog(PostModel post) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);

        interactionHeader.setText("Shares");

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(context));
        friendsRecycler.setNestedScrollingEnabled(true);
        UserAdapter userAdapter = new UserAdapter(context, idList, "goToProfile");
        friendsRecycler.setAdapter(userAdapter);

        postReference.child(post.getPostID()).child("sharedBy")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    idList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        idList.add(ds.getKey());
                    }
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        bottomSheetDialog.show();
        bottomSheetDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void showLikesDialog(PostModel post) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);

        assert interactionHeader != null;
        interactionHeader.setText("Likes");

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(context));
        friendsRecycler.setNestedScrollingEnabled(true);

        likesReference.child(post.getPostID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    idList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        idList.add(ds.getKey());
                    }
                    friendsRecycler.setAdapter(new UserAdapter(context, idList, "goToProfile"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        bottomSheetDialog.show();
        bottomSheetDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void showTaggedFriendsDialog(HolderPosts holder, PostModel post) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);

        interactionHeader.setText("Tagged Friends");

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(context));
        friendsRecycler.setNestedScrollingEnabled(true);
        UserAdapter userAdapter = new UserAdapter(context, idList, "goToProfile");
        friendsRecycler.setAdapter(userAdapter);

        postReference.child(post.getPostID()).child("taggedFriends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    idList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        idList.add(ds.getKey());
                    }
                    userAdapter.notifyDataSetChanged();

                }else{
                    holder.tagsArea.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        bottomSheetDialog.show();
        bottomSheetDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

    }

    private void showSharedTaggedFriendsList(HolderPosts holder, PostModel post) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);

        interactionHeader.setText("Tagged Friends");

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(context));
        friendsRecycler.setNestedScrollingEnabled(true);
        UserAdapter userAdapter = new UserAdapter(context, idList, "goToProfile");
        friendsRecycler.setAdapter(userAdapter);

        postReference.child(post.getSharedPost()).child("taggedFriends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            idList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()){
                                idList.add(ds.getKey());
                            }
                            userAdapter.notifyDataSetChanged();

                        }else{
                            holder.tagsArea.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        bottomSheetDialog.show();
        bottomSheetDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

    }


    //functions to deal with comments only
    private void showCommentsDialog(PostModel post) {
        final BottomSheetDialog commentSheetDialog = new BottomSheetDialog(context);
        commentSheetDialog.setContentView(R.layout.post_comment_layout);

        ImageView close = commentSheetDialog.findViewById(R.id.closeCommentSheetBTN);
        TextView commentHeader = commentSheetDialog.findViewById(R.id.commentSheetHeader);
        RecyclerView commentSheetRecycler = commentSheetDialog.findViewById(R.id.commentSheetRecycler);
        CircleImageView myProfilePicture = commentSheetDialog.findViewById(R.id.commentSheetProfilePic);
        commentET = commentSheetDialog.findViewById(R.id.commentSheetCaptionET);
        ImageView postCommentBTN = commentSheetDialog.findViewById(R.id.commentSheetPostBTN);

        //for recording audio status
        LottieAnimationView lavPlaying = commentSheetDialog.findViewById(R.id.lav_playing);
        ImageView deleteAudioBTN = commentSheetDialog.findViewById(R.id.recordingDeleteBTN);
        ImageView voicePlayBTN = commentSheetDialog.findViewById(R.id.post_playVoiceIcon);
        TextView seekTimer = commentSheetDialog.findViewById(R.id.seekTimer);
        RelativeLayout playAudioArea = commentSheetDialog.findViewById(R.id.playAudioArea);

        AudioRecorder commentRecorder = new AudioRecorder(lavPlaying, context,
                (Activity) context, playAudioArea, voicePlayBTN, seekTimer);
        postCommentBTN.setTag("notRecording");

        ArrayList<CommentModel> commentList = new ArrayList<>();

        commentSheetRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        commentSheetRecycler.setLayoutManager(layoutManager);
        CommentsAdapter commentsAdapter = new CommentsAdapter(context, commentList);
        commentSheetRecycler.setAdapter(commentsAdapter);

        commentsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int counter = 0;
                    commentList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        CommentModel comments = ds.getValue(CommentModel.class);

                        if (comments.getPostID().equals(post.getPostID())) {
                            commentList.add(comments);
                            counter++;
                        }

                        if (counter == 1)
                            commentHeader.setText(counter + " Comment");
                        else
                            commentHeader.setText(counter + " Comments");
                    }
                    commentsAdapter.notifyDataSetChanged();
                }else
                    commentHeader.setText("0 Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        User user = ds.getValue(User.class);

                        if (user.getUserID().equals(firebaseUser.getUid()))
                            try{
                                Picasso.get().load(user.getImageURL()).into(myProfilePicture);
                            }catch (NullPointerException e){
                                Picasso.get().load(R.drawable.default_profile_display_pic).into(myProfilePicture);
                            }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        commentET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0)
                    postCommentBTN.setImageResource(R.drawable.ic_send_black_24dp);
                else
                    postCommentBTN.setImageResource(R.drawable.ic_mic_black_24dp);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        commentSheetDialog.show();
        commentSheetDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        postCommentBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(commentET.getText().toString())
                        && postCommentBTN.getTag().equals("notRecording")){

                    if (commentRecorder.checkRecordingPermission()){
                        postCommentBTN.setImageResource(R.drawable.ic_baseline_stop_circle_24);

                        if (!commentRecorder.isRecording){
                            commentRecorder.startRecording();
                            postCommentBTN.setTag("Recording");
                        }

                    }else{
                        commentRecorder.requestRecordingPermission();
                    }

                }else if (postCommentBTN.getTag().equals("Recording")){
                    commentRecorder.stopRecording();
                    postCommentBTN.setTag("sendAudio");
                    postCommentBTN.setImageResource(R.drawable.ic_send_black_24dp);

                }else{
                    commentID = commentsReference.push().getKey();

                    if (!TextUtils.isEmpty(commentET.getText().toString()))
                        uploadTextComment(post, commentET);
                    else if (commentRecorder.getRecordingFilePath() != null){
                        if (postCommentBTN.getTag().equals("sendAudio"))
                            uploadAudioComment(post, commentRecorder);
                        postCommentBTN.setImageResource(R.drawable.ic_mic_black_24dp);
                    }

                }
            }
        });

        deleteAudioBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentRecorder.resetRecorder();
                postCommentBTN.setImageResource(R.drawable.ic_mic_black_24dp);
                postCommentBTN.setTag("notRecording");
            }
        });

        voicePlayBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!commentRecorder.isPlaying()){
                    commentRecorder.startPlayingRecording();
                }else{
                    commentRecorder.stopPlayingAudio();
                }
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentSheetDialog.dismiss();
            }
        });
    }

    private void uploadAudioComment(PostModel post, AudioRecorder audioRecorder) {

        commentDialog = new ProgressDialog(context);
        commentDialog.setMessage("loading...");
        commentDialog.setCancelable(false);
        commentDialog.show();

        StorageReference audioPath = commentAudioReference.child("comment_audio_files")
                .child(commentID + ".3gp");
        Uri audioUri = Uri.fromFile(new File(audioRecorder.getRecordingFilePath()));

        StorageTask<UploadTask.TaskSnapshot> audioTask = audioPath.putFile(audioUri);

        audioTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful())
                    throw task.getException();
                return audioPath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri audioDownloadLink = task.getResult();

                    HashMap<String, Object> audioMap = new HashMap<>();

                    audioMap.put("commentID", commentID);
                    audioMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
                    audioMap.put("userID", firebaseUser.getUid());
                    audioMap.put("postID", post.getPostID());
                    audioMap.put("commentType", "audioComment");
                    audioMap.put("audioUrl", audioDownloadLink.toString());

                    commentsReference.child(commentID).setValue(audioMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    resetAudioLayout(post, audioRecorder);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    private void uploadTextComment(PostModel post, EditText commentText) {

        commentDialog = new ProgressDialog(context);
        commentDialog.setMessage("loading...");
        commentDialog.setCancelable(false);
        commentDialog.show();

        HashMap<String, Object> commentMap = new HashMap<>();

        commentMap.put("commentID", commentID);
        commentMap.put("comment", commentText.getText().toString());
        commentMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        commentMap.put("userID", firebaseUser.getUid());
        commentMap.put("postID", post.getPostID());
        commentMap.put("commentType", "textComment");

        commentsReference.child(commentID).setValue(commentMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        resetTextLayout(post, commentText);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetTextLayout(PostModel post, EditText commentText) {

        if (!post.getUserID().equals(firebaseUser.getUid()))
            notifications.addPostCommentNotification(post.getUserID(), post.getPostID(), commentText.getText().toString());

        commentText.setText("");

        commentDialog.dismiss();
    }

    private void resetAudioLayout(PostModel post, AudioRecorder audioRecorder){
        if (!post.getUserID().equals(firebaseUser.getUid()))
            notifications.addPostCommentNotification(post.getUserID(), post.getPostID(), "recorded a comment");

        if (audioRecorder.getRecordingFilePath() != null){
            audioRecorder.resetRecorder();
        }

        commentDialog.dismiss();

    }


    //other post functions
    private void iniPicPopUp(final Context context, final HolderPosts holder, final PostModel postModel) {
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

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUserID().equals(postModel.getUserID())){

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

    private void getSharedAudio(HolderPosts holder, PostModel sharedPost) {

        AudioPlayer audioPlayer = new AudioPlayer(context, holder.shared_playBTN,
                holder.shared_audioCurrentDuration, holder.shared_postTotalTime, holder.sharedAudioSeekbar);

        audioPlayer.init();

        holder.shared_playBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!audioPlayer.mediaPlayer.isPlaying())
                    audioPlayer.startPlayingAudio(sharedPost.getAudioURL());
                else if(audioPlayer.mediaPlayer.isPlaying())
                    audioPlayer.stopPlayingAudio();
            }
        });
    }

    private void getUniversalSharedFunctions(HolderPosts holder, PostModel sharedPost) {

        try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
            holder.sharedPostDate.setText(getTimeAgo.getTimeAgo(Long.parseLong(sharedPost.getPostTime()), context));
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }//for converting timestamp

        try{
            universalFunctions.findAddress(sharedPost.getLatitude(), sharedPost.getLongitude(),  holder.sharedPostCheckIn, holder.sharedLocationArea);
        }catch (Exception ignored){}//for shared post location
    }

    private void getNormalPostAudio(HolderPosts holder, PostModel post){

        AudioPlayer audioPlayer = new AudioPlayer(context, holder.playBTN,
                holder.postAudioCurrentDuration, holder.postTotalTime, holder.audioSeekBar);

        audioPlayer.init();

        holder.playBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!audioPlayer.mediaPlayer.isPlaying())
                    audioPlayer.startPlayingAudio(post.getAudioURL());
                else if(audioPlayer.mediaPlayer.isPlaying())
                    audioPlayer.stopPlayingAudio();
            }
        });
    }

    private void getPostComments(HolderPosts holder, PostModel post){

        List<CommentModel> comments = new ArrayList<>();

        holder.postItemRecyclerView.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setReverseLayout(false);
        holder.postItemRecyclerView.setLayoutManager(layoutManager);
        holder.postItemRecyclerView.setVisibility(View.VISIBLE);
        CommentsAdapter adapterComments = new CommentsAdapter(context, comments);
        holder.postItemRecyclerView.setAdapter(adapterComments);

        commentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    comments.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        CommentModel commentModel = ds.getValue(CommentModel.class);

                        assert commentModel != null;
                        if (commentModel.getPostID().equals(post.getPostID()))
                            comments.add(commentModel);
                    }

                    Collections.reverse(comments);

                    while(comments.size() > 1){
                        comments.remove(1);
                    }

                    adapterComments.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class HolderPosts extends RecyclerView.ViewHolder{
        //-----Views from row post.xml
        //---------------view for post owner details
        public CircleImageView postProPic;
        public TextView postUsername;

        //--------------views for post details
        public TextView postTimeStamp, postCaption, rPostLocation, postImagesCounter, taggedPeopleList;
        public ImageView privacyIcon;
        public RecyclerView postImageRecyclerView;

        //--------------views for post buttons
        public ImageView postLikeBTN, postMenuBTN, savePostBTN;
        public TextView likeCounter, commentCounter, shareCounter;

        //------------other
        public RelativeLayout commentLayout, likesLayout, shareArea, postPicArea;
        public Dialog popAddPost;
        public LinearLayout locationArea, tagsArea;
        public ProgressBar imageLoader;

        //---------------for shared posts
        public CircleImageView sharedPostProPic;
        public TextView sharedPostUsername, sharedPostDate, sharedPostCheckIn, sharedPostDesc, sharedPost_taggedPeopleList, sharedPostImagesCounter;
        public ProgressBar sharedProgressBar;
        public LinearLayout sharedLocationArea, sharedPostDetails;
        public ImageView sharedPostVolumeBTN;
        public RelativeLayout sharedPost;
        public RecyclerView sharedPostImageRecyclerView;

        //--------------for audio post buttons
        public CircleImageView playBTN;
        public SeekBar audioSeekBar;
        public TextView postAudioCurrentDuration, postTotalTime;

        //--------------for shared audio post buttons
        public CircleImageView shared_playBTN;
        public SeekBar sharedAudioSeekbar;
        public TextView shared_audioCurrentDuration, shared_postTotalTime;

        //----------for comments
        public RecyclerView postItemRecyclerView;
        public CircleImageView commentAreaProPic, commentAreaVoiceBTN;
        public EditText commentAreaMessage;

        //for recording voice comments
        private LottieAnimationView lavPlaying;
        private ImageView voicePlayBTN, deleteAudioBTN;
        private TextView recorderDurationTV;
        private RelativeLayout playAudioArea;

        public HolderPosts(@NonNull View itemView) {
            super(itemView);

            postProPic = itemView.findViewById(R.id.postProPic);
            postUsername = itemView.findViewById(R.id.postUsername);
            taggedPeopleList = itemView.findViewById(R.id.taggedPeopleList);

            postTimeStamp = itemView.findViewById(R.id.postTimeStamp);
            postCaption = itemView.findViewById(R.id.postCaption);
            rPostLocation = itemView.findViewById(R.id.rPostLocation);
            postImagesCounter = itemView.findViewById(R.id.postImagesCounter);

            privacyIcon = itemView.findViewById(R.id.privacyIcon);
            postImageRecyclerView = itemView.findViewById(R.id.postImageRecyclerView);
            imageLoader = itemView.findViewById(R.id.post_item_image_loader);

            postLikeBTN = itemView.findViewById(R.id.postLikeBTN);
            postMenuBTN = itemView.findViewById(R.id.postMenuBTN);
            savePostBTN = itemView.findViewById(R.id.savePostBTN);

            likeCounter = itemView.findViewById(R.id.likeCounter);
            commentCounter = itemView.findViewById(R.id.commentCounter);
            shareCounter = itemView.findViewById(R.id.shareCounter);

            commentLayout = itemView.findViewById(R.id.commentLayout);
            likesLayout = itemView.findViewById(R.id.likesArea);
            shareArea = itemView.findViewById(R.id.shareArea);
            postPicArea = itemView.findViewById(R.id.postPicArea);
            locationArea = itemView.findViewById(R.id.post_location_area);
            tagsArea = itemView.findViewById(R.id.tagsArea);

            postItemRecyclerView = itemView.findViewById(R.id.postItemRecyclerView);
            commentAreaProPic = itemView.findViewById(R.id.commentAreaProPic);
            commentAreaVoiceBTN = itemView.findViewById(R.id.commentAreaVoiceBTN);
            commentAreaMessage = itemView.findViewById(R.id.commentAreaMessage);

            //for recording the audio
            playAudioArea = itemView.findViewById(R.id.playAudioArea);
            voicePlayBTN = itemView.findViewById(R.id.post_playVoiceIcon);
            recorderDurationTV = itemView.findViewById(R.id.seekTimer);
            lavPlaying = itemView.findViewById(R.id.lav_playing);
            deleteAudioBTN = itemView.findViewById(R.id.recordingDeleteBTN);

            //for shared posts
            sharedPostProPic = itemView.findViewById(R.id.shared_image_user);
            sharedPostUsername = itemView.findViewById(R.id.shared_username);
            sharedPostDate = itemView.findViewById(R.id.shared_postDate);
            sharedPostCheckIn = itemView.findViewById(R.id.shared_postCheckIn);
            sharedPostDesc = itemView.findViewById(R.id.shared_post_desc);
            sharedProgressBar = itemView.findViewById(R.id.shared_progress_load_media);
            sharedLocationArea = itemView.findViewById(R.id.sharedLocationArea);
            sharedPostImageRecyclerView = itemView.findViewById(R.id.sharedPostImageRecyclerView);
            sharedPost = itemView.findViewById(R.id.shared_post_item);
            sharedPostVolumeBTN = itemView.findViewById(R.id.sharePostItemVolumeBTN);
            sharedPost_taggedPeopleList = itemView.findViewById(R.id.sharedPost_taggedPeopleList);
            sharedPostImagesCounter = itemView.findViewById(R.id.sharedPostImagesCounter);
            sharedPostDetails = itemView.findViewById(R.id.shared_post_details_area);

            //for audio buttons
            playBTN = itemView.findViewById(R.id.postItem_playVoiceIcon);
            audioSeekBar = itemView.findViewById(R.id.postItem_lav_playing);
            postAudioCurrentDuration = itemView.findViewById(R.id.postItemSeekTimer);
            postTotalTime = itemView.findViewById(R.id.postTotalTime);

            //for audio buttons
            shared_playBTN = itemView.findViewById(R.id.shared_postItem_playVoiceIcon);
            sharedAudioSeekbar = itemView.findViewById(R.id.shared_postItem_lav_playing);
            shared_audioCurrentDuration = itemView.findViewById(R.id.shared_postItemSeekTimer);
            shared_postTotalTime = itemView.findViewById(R.id.shared_postTotalTime);
        }

    }

    static class HolderNativeAd extends RecyclerView.ViewHolder{

        private ImageView ad_app_icon;
        private TextView ad_headline, ad_advertiser, ad_body, ad_price, ad_store;
        private RatingBar ad_stars;
        private MediaView mediaView;
        private Button adCallToAction;
        private NativeAdView nativeAdView;

        public HolderNativeAd(@NonNull View itemView) {
            super(itemView);

            ad_app_icon = itemView.findViewById(R.id.ad_app_icon);
            ad_headline = itemView.findViewById(R.id.ad_headline);
            ad_advertiser = itemView.findViewById(R.id.ad_advertiser);
            ad_stars = itemView.findViewById(R.id.ad_stars);
            ad_body = itemView.findViewById(R.id.ad_body);
            mediaView = itemView.findViewById(R.id.media_view);
            ad_price = itemView.findViewById(R.id.ad_price);
            ad_store = itemView.findViewById(R.id.ad_store);
            adCallToAction = itemView.findViewById(R.id.ad_call_to_action);
            nativeAdView = itemView.findViewById(R.id.nativeAdView);

        }
    }

    @Override
    public int getItemViewType(int position) {
        /*if (position != 0 && position % 4 == 0){
            return VIEW_TYPE_AD;
        }else{
            switch (postList.get(position).getPostType()){
                case "textPost":
                    return TEXT_POST_ITEM;

                case "imagePost":
                    return IMAGE_POST_ITEM;

                case "sharedTextPost":
                    return SHARED_TEXT_POST_ITEM;

                case "sharedTextImagePost":
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
                    throw new IllegalStateException("Unexpected value " + postList.get(position).getPostType());
            }
        }*/

        switch (postList.get(position).getPostType()){
            case "textPost":
                return TEXT_POST_ITEM;

            case "imagePost":
                return IMAGE_POST_ITEM;

            case "sharedTextPost":
                return SHARED_TEXT_POST_ITEM;

            case "sharedTextImagePost":
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
                throw new IllegalStateException("Unexpected value " + postList.get(position).getPostType());
        }

    }

}
