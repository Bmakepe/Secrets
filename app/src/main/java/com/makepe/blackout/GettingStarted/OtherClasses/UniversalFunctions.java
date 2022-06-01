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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import com.makepe.blackout.GettingStarted.Adapters.VideoFootageAdapter;
import com.makepe.blackout.GettingStarted.Fragments.ProfileFragment;
import com.makepe.blackout.GettingStarted.InAppActivities.ConnectionsActivity;
import com.makepe.blackout.GettingStarted.InAppActivities.ViewProfileActivity;
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.GettingStarted.Models.Story;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class UniversalFunctions {
    private Context context;
    private UniversalNotifications universalNotifications = new UniversalNotifications();

    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    private DatabaseReference postReference = FirebaseDatabase.getInstance().getReference("Posts");
    private DatabaseReference likesReference =  FirebaseDatabase.getInstance().getReference("Likes");
    private DatabaseReference commentsReference = FirebaseDatabase.getInstance().getReference("Comments");
    private DatabaseReference savesReference = FirebaseDatabase.getInstance().getReference().child("Saves");
    private DatabaseReference viewsReference = FirebaseDatabase.getInstance().getReference("VideoViews");
    private DatabaseReference storyReference = FirebaseDatabase.getInstance().getReference("Story");
    private DatabaseReference postViewsReference = FirebaseDatabase.getInstance().getReference("Views");

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

    public void isLiked(String postid, ImageView imageView){
        likesReference.child(postid).addValueEventListener(new ValueEventListener() {
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

    public void nrLikes(TextView likes, String postid){
        likesReference.child(postid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() == 1)
                    likes.setText(snapshot.getChildrenCount() + " like");
                else
                    likes.setText(snapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getCommentsCount(String postID, final TextView commentCountTV) {
        commentsReference.child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 1)
                    commentCountTV.setText(dataSnapshot.getChildrenCount() + " comment");
                else
                    commentCountTV.setText(dataSnapshot.getChildrenCount() +" comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void isSaved(final String postid, final ImageView imageView){
        savesReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postid).exists()){
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

    public void beginDelete(String pId, String postImage) {
        if(postImage.equals("noImage")){
            deleteWithoutImage(pId);
        }else{
            deleteWithImage(pId, postImage);
        }
    }

    private void deleteWithImage(final String pId, String postImage) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(postImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
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

    private void deleteWithoutImage(String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
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
                if (snapshot.getChildrenCount() == 0) {
                    videoViews.setText("Views");
                }else if (snapshot.getChildrenCount() == 1) {
                    videoViews.setText(snapshot.getChildrenCount() + " View");
                }else {
                    videoViews.setText(snapshot.getChildrenCount() + " Views");
                }

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

    public void likePost(PostModel postModel){

        likesReference.child(postModel.getPostID())
                .child(firebaseUser.getUid()).setValue(true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if(postModel.getUserID().equals(firebaseUser.getUid()))
                            universalNotifications.addLikesNotifications(postModel.getPostID(), postModel.getUserID());
                    }
                });
    }

    public void unlikePost(PostModel model){

        likesReference.child(model.getPostID())
                .child(firebaseUser.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Unliked post", Toast.LENGTH_SHORT).show();
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
                            if (user.getUSER_ID().equals(model.getUserID()))
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
                            if (user.getUSER_ID().equals(model.getUserID()))
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
                        Intent intent = new Intent(context, ConnectionsActivity.class);
                        intent.putExtra("UserID", model.getUserID());
                        intent.putExtra("Interaction", "Views");
                        context.startActivity(intent);
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
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalCount = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PostModel post = snapshot.getValue(PostModel.class);
                    assert post != null;

                    if (post.getUserID().equals(userID)){
                        totalCount++;
                    }
                }

                postsNo.setText(String.valueOf(totalCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
