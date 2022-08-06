package com.makepe.blackout.GettingStarted.OtherClasses;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import com.makepe.blackout.GettingStarted.Adapters.UserAdapter;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.CommentModel;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.GettingStarted.Notifications.SendNotifications;
import com.makepe.blackout.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class UniversalFunctions {
    private Context context;
    private final SendNotifications sendNotifications = new SendNotifications();

    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    private final DatabaseReference postReference = FirebaseDatabase.getInstance().getReference("SecretPosts");
    private final DatabaseReference videoReference = FirebaseDatabase.getInstance().getReference("SecretVideos");
    private final DatabaseReference likesReference =  FirebaseDatabase.getInstance().getReference("Likes");
    private final DatabaseReference commentsReference = FirebaseDatabase.getInstance().getReference("Comments");
    private final DatabaseReference savesReference = FirebaseDatabase.getInstance().getReference().child("Saves");
    private final DatabaseReference viewsReference = FirebaseDatabase.getInstance().getReference("VideoViews");
    private final DatabaseReference storyReference = FirebaseDatabase.getInstance().getReference("Story");
    private final DatabaseReference postViewsReference = FirebaseDatabase.getInstance().getReference("Views");

    public UniversalFunctions(Context context) {
        this.context = context;
    }

    public UniversalFunctions() {
    }

    public void findAddress(double latitude, double longitude, TextView postCheckIn, LinearLayout locationArea) {
        //find address, country, state, city
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context, Locale.getDefault());

            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0);//complete address

            postCheckIn.setText(address);
            postCheckIn.setVisibility(View.VISIBLE);
            locationArea.setVisibility(View.VISIBLE);
        }catch (Exception ignored){}
    }

    public void isLiked(String postID, ImageView imageView){
        likesReference.child(postID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_favorite_black_24dp);
                    imageView.setTag("liked");
                }else{
                    imageView.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void nrLikes(TextView likes, String postID){
        likesReference.child(postID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() == 1)
                    likes.setText(snapshot.getChildrenCount() + " Like");
                else
                    likes.setText(snapshot.getChildrenCount() + " Likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getCommentsCount(String postID, final TextView commentCountTV) {
        commentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int counter = 0;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        CommentModel commentModel = ds.getValue(CommentModel.class);

                        assert commentModel != null;
                        if (commentModel.getPostID().equals(postID)){
                            counter++;
                        }
                        if (counter == 1)
                            commentCountTV.setText(counter + " Comment");
                        else
                            commentCountTV.setText(counter + " Comments");
                    }
                }else
                    commentCountTV.setText("0 Comments");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void isSaved(final String postID, final ImageView imageView){
        savesReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postID).exists()){
                    imageView.setImageResource(R.drawable.ic_saved);
                    imageView.setTag("saved");
                }else{
                    imageView.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void beginDelete(String postID, String postImage) {
        if(postImage.equals("noImage")){
            deleteWithoutImage(postID);
        }else{
            deleteWithImage(postID, postImage);
        }
    }

    private void deleteWithImage(final String postID, String postImage) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(postImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("postID").equalTo(postID);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }
                                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage(String postID) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("postID").equalTo(postID);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void checkVideoViewCount(TextView videoViews, PostModel videoPost) {
        viewsReference.child(videoPost.getPostID())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    if (snapshot.getChildrenCount() == 1)
                        videoViews.setText(snapshot.getChildrenCount() + " View");
                    else
                        videoViews.setText(snapshot.getChildrenCount() + " Views");
                else
                    videoViews.setText("0 Views");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void checkActiveStories(CircleImageView avatarIv, String userID) {

        storyReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Story story = ds.getValue(Story.class);

                        long currentTime = System.currentTimeMillis();

                        assert story != null;
                        if (currentTime > story.getTimeStart() && currentTime < story.getTimeEnd()){
                            avatarIv.setBorderColor(context.getResources().getColor(R.color.colorGreen));
                            avatarIv.setTag("storyActive");
                        }else{
                            avatarIv.setTag("noStories");
                        }
                    }
                }else {
                    avatarIv.setTag("noStories");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void addView(String postID) {
        postViewsReference.child(postID).child(firebaseUser.getUid()).setValue(true);
    }

    public void seenNumber(String postID, TextView seen_number) {

        postViewsReference.child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() == 1)
                    seen_number.setText(snapshot.getChildrenCount() + " View");
                else
                    seen_number.setText(snapshot.getChildrenCount() + " Views");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getSharedNumber(String postID, TextView sharedPostTV){
        postReference.child(postID).child("sharedBy").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (snapshot.getChildrenCount() == 1)
                        sharedPostTV.setText(snapshot.getChildrenCount() + " Share");
                    else
                        sharedPostTV.setText(snapshot.getChildrenCount() + " Shares");
                }else{
                    sharedPostTV.setText("0 Shares");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void likePost(PostModel postModel){

        likesReference.child(postModel.getPostID())
                .child(firebaseUser.getUid()).setValue(true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if(!postModel.getUserID().equals(firebaseUser.getUid()))
                            sendNotifications.addPostLikeNotification(postModel);
                    }
                });
    }

    public void unlikePost(PostModel model){

        likesReference.child(model.getPostID())
                .child(firebaseUser.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (!model.getUserID().equals(firebaseUser.getUid()))
                            sendNotifications.removePostLikeNotification(model);
                    }
                });
    }

    public void savePost(PostModel model){
        savesReference.child(firebaseUser.getUid())
                .child(model.getPostID()).setValue(true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Saved Post", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void removeSavedPost(PostModel model){
        savesReference.child(firebaseUser.getUid())
                .child(model.getPostID()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Removed from saved posts", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void postMenuOptions(ImageView postMenuBTN, PostModel model) {

        final FollowInteraction followInteraction = new FollowInteraction(context);

        PopupMenu popupMenu = new PopupMenu(context, postMenuBTN, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0, 0, "View Profile");

        if (model.getUserID().equals(firebaseUser.getUid())){
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 2, 0, "Views");
        }else if (!model.getUserID().equals(firebaseUser.getUid())){
            if (followInteraction.checkFollowing(model.getUserID())){
                userReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            User user = ds.getValue(User.class);

                            assert user != null;
                            if (user.getUserID().equals(model.getUserID()))
                                popupMenu.getMenu().add(Menu.NONE, 3, 0, "Unfollow " + user.getUsername());
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
                            if (user.getUserID().equals(model.getUserID()))
                                popupMenu.getMenu().add(Menu.NONE, 3, 0, "Follow " + user.getUsername());

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
                        Intent profileIntent = new Intent(context, ViewProfileActivity.class);
                        profileIntent.putExtra("uid", model.getUserID());
                        context.startActivity(profileIntent);
                        break;

                    case 1:
                        beginDelete(model.getUserID(), model.getVideoURL());
                        break;

                    case 2:

                        openViewsDialog(model);

                        break;

                    case 3:
                        if (followInteraction.checkFollowing(model.getUserID()))
                            followInteraction.unFollowUser(model.getUserID());
                        else
                            followInteraction.followUser(model.getUserID());

                        break;

                    default:
                        Toast.makeText(context, "Unknown video detected", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        popupMenu.show();

    }

    public void getNrPosts(String userID, TextView postsNo){
        postReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int postCount = 0;
                if (dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        PostModel post = snapshot.getValue(PostModel.class);
                        assert post != null;

                        if (post.getUserID().equals(userID)){
                            postCount++;
                        }

                        int finalPostCount = postCount;
                        videoReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                int videoCount = 0;
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    PostModel postModel = ds.getValue(PostModel.class);

                                    if (postModel.getUserID().equals(userID))
                                        videoCount++;
                                }
                                postsNo.setText(String.valueOf(finalPostCount + videoCount));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }else{
                    postsNo.setText(String.valueOf(postCount));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    //-------for displaying dialogs---------//
    public void openViewsDialog(PostModel model) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.tagged_users_layout);

        ImageView close = bottomSheetDialog.findViewById(R.id.taggedCloseSheetBTN);
        RecyclerView friendsRecycler = bottomSheetDialog.findViewById(R.id.taggedFriendsRecycler);
        TextView interactionHeader = bottomSheetDialog.findViewById(R.id.interactionHeader);

        assert interactionHeader != null;
        interactionHeader.setText("Views");

        ArrayList<String> idList = new ArrayList<>();

        friendsRecycler.hasFixedSize();
        friendsRecycler.setLayoutManager(new LinearLayoutManager(context));
        friendsRecycler.setNestedScrollingEnabled(true);
        UserAdapter userAdapter = new UserAdapter(context, idList, "goToProfile");
        friendsRecycler.setAdapter(userAdapter);

        postViewsReference.child(model.getPostID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    idList.add(ds.getKey());
                }
                userAdapter.notifyDataSetChanged();
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
}
