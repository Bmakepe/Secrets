package com.makepe.blackout.GettingStarted.Adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
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
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenImageActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.FullScreenPictureActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.StoryActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.CommentModel;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.Notifications.SendNotifications;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioPlayer;
import com.makepe.blackout.GettingStarted.OtherClasses.AudioRecorder;
import com.makepe.blackout.GettingStarted.OtherClasses.FollowInteraction;
import com.makepe.blackout.GettingStarted.OtherClasses.GetTimeAgo;
import com.makepe.blackout.GettingStarted.OtherClasses.LocationServices;
import com.makepe.blackout.GettingStarted.OtherClasses.UniversalFunctions;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.reflect.KVisibility;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.MyHolder> {

    private Context context;
    private List<CommentModel> commentList;

    private DatabaseReference userReference, likesReference, replyReference;
    private FirebaseUser firebaseUser;

    private GetTimeAgo timeAgo;

    private UniversalFunctions universalFunctions;
    private SendNotifications notifications;
    private FollowInteraction followInteraction;

    public static final int COMMENT_IMAGE_POST = 100;
    public static final int COMMENT_TEXT_POST = 200;
    public static final int COMMENT_AUDIO_POST = 300;
    public static final int COMMENT_AUDIO_IMAGE_POST = 400;

    //for comments
    private StorageReference commentReplyAudioReference;
    private AudioRecorder commentRecorder;
    private String commentID;
    private EditText commentET;
    private ProgressDialog commentDialog;

    public CommentsAdapter(Context context, List<CommentModel> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate comment layout xml
        switch (viewType){
            case COMMENT_IMAGE_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.comment_image_layout, parent, false));

            case COMMENT_TEXT_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.comment_layout, parent, false));

            case COMMENT_AUDIO_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.comment_audio_item, parent, false));

            case COMMENT_AUDIO_IMAGE_POST:
                return new MyHolder(LayoutInflater.from(context).inflate(R.layout.comment_audio_image_item, parent, false));

            default:
                throw new IllegalStateException("Unexpected value " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get the data
        CommentModel comments = commentList.get(position);
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        likesReference = FirebaseDatabase.getInstance().getReference("Likes");
        replyReference = FirebaseDatabase.getInstance().getReference("CommentReply");
        commentReplyAudioReference = FirebaseStorage.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        timeAgo = new GetTimeAgo();
        universalFunctions = new UniversalFunctions(context);
        notifications = new SendNotifications(context);
        followInteraction = new FollowInteraction(context);

        switch (getItemViewType(position)){
            case COMMENT_IMAGE_POST:
                getCommentInfo(comments, holder);
                getImageCommentDetails(comments, holder);
                loadOtherFunctions(comments, holder);
                break;

            case COMMENT_TEXT_POST:
                getCommentInfo(comments, holder);
                getTextCommentDetails(comments, holder);
                loadOtherFunctions(comments, holder);
                break;

            case COMMENT_AUDIO_POST:
                getCommentInfo(comments, holder);
                getAudioCommentDetails(comments, holder);
                loadOtherFunctions(comments, holder);
                break;

            case COMMENT_AUDIO_IMAGE_POST:
                getCommentInfo(comments, holder);
                getAudioCommentDetails(comments, holder);
                loadOtherFunctions(comments, holder);

                try{
                    Picasso.get().load(comments.getCommentImage()).into(holder.commentImage);
                }catch (NullPointerException ignored){}

                holder.commentImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent imageIntent = new Intent(context, FullScreenPictureActivity.class);
                        imageIntent.putExtra("imageURL", comments.getCommentImage());
                        context.startActivity(imageIntent);
                    }
                });
                break;

            default:
                Toast.makeText(context, "Unknown comment post type", Toast.LENGTH_SHORT).show();
        }

    }

    private void getAudioCommentDetails(CommentModel comments, MyHolder holder) {

        AudioPlayer audioPlayer = new AudioPlayer(context, holder.playBTN,
                holder.seekTimer, holder.postTotalTime, holder.audioAnimation);

        audioPlayer.init();

        try{//convert timestamp
            holder.commentTime.setText(timeAgo.getTimeAgo(Long.parseLong(comments.getTimeStamp()), context));
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }

        holder.playBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!audioPlayer.mediaPlayer.isPlaying())
                    audioPlayer.startPlayingAudio(comments.getAudioUrl());
                else if(audioPlayer.mediaPlayer.isPlaying())
                    audioPlayer.stopPlayingAudio();
            }
        });
    }

    private void getTextCommentDetails(CommentModel comments, MyHolder holder) {

        try{//convert timestamp
            holder.commentTime.setText(timeAgo.getTimeAgo(Long.parseLong(comments.getTimeStamp()), context));
            holder.commentCaption.setText(comments.getComment());
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadOtherFunctions(CommentModel comments, MyHolder holder) {

        universalFunctions.isLiked(comments.getCommentID(), holder.likesBTN);
        universalFunctions.nrLikes(holder.likesCounter, comments.getCommentID());

        universalFunctions.checkActiveStories(holder.cProPic, comments.getUserID());

        getCommentReplyCounter(holder, comments);
        getCommentReplies(holder, comments);

        holder.menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuOptions(holder.menuIcon, comments);
            }
        });

        holder.cProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.cProPic.getTag().equals("storyActive")){

                    Intent intent = new Intent(context, StoryActivity.class);
                    intent.putExtra("userid", comments.getUserID());
                    context.startActivity(intent);
                }else{

                    Intent picIntent = new Intent(context, FullScreenImageActivity.class);
                    picIntent.putExtra("itemID", comments.getUserID());
                    picIntent.putExtra("reason", "userImage");
                    context.startActivity(picIntent);
                }
            }
        });

        holder.likesBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.likesBTN.getTag().equals("like")){
                    likesReference.child(comments.getCommentID())
                            .child(firebaseUser.getUid()).setValue(true);
                    if (!firebaseUser.getUid().equals(comments.getUserID()))
                        notifications.addCommentLikesNotification(comments);
                }else{
                    likesReference.child(comments.getCommentID())
                            .child(firebaseUser.getUid()).removeValue();
                    if (!firebaseUser.getUid().equals(comments.getUserID()))
                        notifications.removeCommentLikesNotification(comments);
                }
            }
        });

        holder.likesCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLikesDialog(comments);
            }
        });

        holder.commentReplyArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommentRepliesDialog(comments);
            }
        });
    }


    //for comment replies only
    private void getCommentReplyCounter(MyHolder holder, CommentModel comments) {
        replyReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int counter = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        CommentModel commentModel = ds.getValue(CommentModel.class);

                        assert commentModel != null;
                        if (commentModel.getReplyCommentID().equals(comments.getCommentID()))
                            counter++;

                        if (counter == 1)
                            holder.replyCounter.setText(counter + " Reply");
                        else
                            holder.replyCounter.setText(counter + " Replies");
                    }
                }else{
                    holder.replyCounter.setText("0 Replies");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCommentReplies(MyHolder holder, CommentModel commentModel){
        List<CommentModel> commentReplies = new ArrayList<>();

        holder.commentReplyRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setReverseLayout(false);
        holder.commentReplyRecycler.setLayoutManager(layoutManager);
        holder.commentReplyRecycler.setVisibility(View.VISIBLE);
        CommentReplyAdapter replyAdapter = new CommentReplyAdapter(context, commentReplies);
        holder.commentReplyRecycler.setAdapter(replyAdapter);

        replyReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    commentReplies.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        CommentModel comment = ds.getValue(CommentModel.class);

                        assert comment != null;
                        if (comment.getReplyCommentID().equals(commentModel.getCommentID()))
                            commentReplies.add(comment);
                    }

                    Collections.reverse(commentReplies);

                    while(commentReplies.size() > 1){
                        commentReplies.remove(1);
                    }

                    replyAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showCommentRepliesDialog(CommentModel comments) {
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

        commentRecorder = new AudioRecorder(lavPlaying, context,
                (Activity) context, playAudioArea, voicePlayBTN, seekTimer);
        notifications = new SendNotifications(context);
        postCommentBTN.setTag("notRecording");

        commentDialog = new ProgressDialog(context);

        ArrayList<CommentModel> replyList = new ArrayList<>();

        commentSheetRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        commentSheetRecycler.setLayoutManager(layoutManager);
        CommentReplyAdapter replyAdapter = new CommentReplyAdapter(context, replyList);
        commentSheetRecycler.setAdapter(replyAdapter);

        replyReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    replyList.clear();
                    int counter = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        CommentModel commentModel = ds.getValue(CommentModel.class);

                        if (commentModel.getReplyCommentID().equals(comments.getCommentID())){
                            replyList.add(commentModel);
                            counter++;
                        }

                        if (counter == 1)
                            commentHeader.setText(counter + " Reply");
                        else
                            commentHeader.setText(counter + " Replies");

                        replyAdapter.notifyDataSetChanged();
                    }

                }else
                    commentHeader.setText("0 Replies");
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
                    commentID = replyReference.push().getKey();
                    commentDialog.setMessage("loading...");
                    commentDialog.setCancelable(false);
                    commentDialog.show();

                    if (!TextUtils.isEmpty(commentET.getText().toString()))
                        uploadTextComment(comments);
                    else if (commentRecorder.getRecordingFilePath() != null){
                        if (postCommentBTN.getTag().equals("sendAudio"))
                            uploadAudioComment(comments);
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

    private void uploadAudioComment(CommentModel comments) {
        StorageReference audioPath = commentReplyAudioReference.child("reply_comment_audio_files")
                .child(commentID + ".3gp");
        Uri audioUri = Uri.fromFile(new File(commentRecorder.getRecordingFilePath()));

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
                    audioMap.put("replyCommentID", comments.getCommentID());
                    audioMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
                    audioMap.put("userID", firebaseUser.getUid());
                    audioMap.put("postID", comments.getPostID());
                    audioMap.put("commentType", "audioComment");
                    audioMap.put("audioUrl", audioDownloadLink.toString());

                    replyReference.child(commentID).setValue(audioMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    resetLayout(comments);
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

    private void uploadTextComment(CommentModel comments) {
        HashMap<String, Object> commentMap = new HashMap<>();

        commentMap.put("commentID", commentID);
        commentMap.put("replyCommentID", comments.getCommentID());
        commentMap.put("comment", commentET.getText().toString());
        commentMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        commentMap.put("userID", firebaseUser.getUid());
        commentMap.put("postID", comments.getPostID());
        commentMap.put("commentType", "textComment");

        replyReference.child(commentID).setValue(commentMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        resetLayout(comments);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetLayout(CommentModel comments) {

        if (!comments.getUserID().equals(firebaseUser.getUid()))
            if (TextUtils.isEmpty(commentET.getText().toString()))
                notifications.addPostCommentNotification(comments.getUserID(), comments.getPostID(), "recorded a comment");
            else
                notifications.addPostCommentNotification(comments.getUserID(), comments.getPostID(), commentET.getText().toString());

        if (commentRecorder.getRecordingFilePath() != null){
            commentRecorder.resetRecorder();
        }

        commentET.setText("");

        commentDialog.dismiss();
    }


    //for the rest of the funtions
    private void showLikesDialog(CommentModel post) {
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

        likesReference.child(post.getCommentID()).addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void getImageCommentDetails(CommentModel comments, MyHolder holder) {

        try{
            universalFunctions.findAddress(comments.getLatitude(), comments.getLongitude(), holder.commentLocation, holder.commentLocationArea);
        }catch (Exception ignored){}

        try{
            Picasso.get().load(comments.getCommentImage()).into(holder.commentImage);
        }catch (NullPointerException ignored){}

        try{//convert timestamp
            holder.commentTime.setText(timeAgo.getTimeAgo(Long.parseLong(comments.getTimeStamp()), context));
            holder.commentCaption.setText(comments.getComment());
        }catch (NumberFormatException n){
            Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
        }

        holder.commentImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageIntent = new Intent(context, FullScreenPictureActivity.class);
                imageIntent.putExtra("imageURL", comments.getCommentImage());
                context.startActivity(imageIntent);
            }
        });
    }

    private void showMenuOptions(ImageView menuIcon, CommentModel comments) {
        PopupMenu popupMenu = new PopupMenu(context, menuIcon, Gravity.END);
        
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "View Profile");
        
        if (comments.getUserID().equals(firebaseUser.getUid())) {
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Delete Comment");
        }else if (!comments.getUserID().equals(firebaseUser.getUid())){
            if (followInteraction.checkFollowing(comments.getUserID())){
                userReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            User user = ds.getValue(User.class);

                            assert user != null;
                            if (user.getUserID().equals(comments.getUserID()))
                                popupMenu.getMenu().add(Menu.NONE, 2, 0, "Unfollow " + user.getUsername());
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
                            if (user.getUserID().equals(comments.getUserID()))
                                popupMenu.getMenu().add(Menu.NONE, 2, 0, "Follow " + user.getUsername());
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
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case 0:
                        if (!comments.getUserID().equals(firebaseUser.getUid())) {
                            Intent intent = new Intent(context, ViewProfileActivity.class);
                            intent.putExtra("uid", comments.getUserID());
                            context.startActivity(intent);
                        }
                        break;
                        
                    case 1:
                        Toast.makeText(context, "You will be able to delete", Toast.LENGTH_SHORT).show();
                        break;

                    case 2:
                        if (followInteraction.checkFollowing(comments.getUserID()))
                            followInteraction.unFollowUser(comments.getUserID());
                        else
                            followInteraction.followUser(comments.getUserID());
                        
                    default:
                        Toast.makeText(context, "Unknown Selection", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void getCommentInfo(CommentModel comments, final MyHolder holder) {

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);

                        assert user != null;
                        if (user.getUserID().equals(comments.getUserID())) {
                            if (user.getUserID().equals(firebaseUser.getUid())) {
                                holder.commentOwner.setText("Me");
                            } else{
                                holder.commentOwner.setText(user.getUsername());
                            }

                            try{
                                Picasso.get().load(user.getImageURL()).into(holder.cProPic);
                            }catch (NullPointerException e){
                                Picasso.get().load(R.drawable.default_profile_display_pic).into(holder.cProPic);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        public CircleImageView cProPic;
        public TextView commentCaption, commentOwner, commentTime, likesCounter, commentLocation, replyCounter;
        public ImageView likesBTN, commentImage, menuIcon;
        public LinearLayout commentLocationArea;
        public RelativeLayout commentReplyArea;
        public RecyclerView commentReplyRecycler;

        //--------------for audio post buttons
        public CircleImageView playBTN;
        public SeekBar audioAnimation;
        public TextView seekTimer, postTotalTime;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            cProPic = itemView.findViewById(R.id.comProPic);
            commentCaption = itemView.findViewById(R.id.commentCaption);
            commentOwner = itemView.findViewById(R.id.commentOwner);
            commentTime = itemView.findViewById(R.id.commentTime);
            likesBTN = itemView.findViewById(R.id.likeComBTN);
            commentImage = itemView.findViewById(R.id.commentImage);
            likesCounter = itemView.findViewById(R.id.commentItemLikesCounter);
            menuIcon = itemView.findViewById(R.id.commentMenuIcon);
            commentLocation = itemView.findViewById(R.id.commentLocation);
            commentLocationArea = itemView.findViewById(R.id.commentLocationArea);
            replyCounter = itemView.findViewById(R.id.commentReplyCounter);
            commentReplyArea = itemView.findViewById(R.id.commentReplyArea);
            commentReplyRecycler = itemView.findViewById(R.id.commentReplyRecycler);

            //for audio buttons
            playBTN = itemView.findViewById(R.id.postItem_playVoiceIcon);
            audioAnimation = itemView.findViewById(R.id.postItem_lav_playing);
            seekTimer = itemView.findViewById(R.id.postItemSeekTimer);
            postTotalTime = itemView.findViewById(R.id.postTotalTime);

        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (commentList.get(position).getCommentType()){
            case "textComment":
                return COMMENT_TEXT_POST;

            case "imageComment":
                return COMMENT_IMAGE_POST;

            case "audioImageComment":
                return COMMENT_AUDIO_IMAGE_POST;

            case "audioComment":
                return COMMENT_AUDIO_POST;

            default:
                throw new IllegalStateException("Unexpected value " + commentList.get(position).getCommentType());
        }
    }
}
