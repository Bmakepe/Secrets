package com.makepe.blackout.GettingStarted.OtherClasses;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
import com.makepe.blackout.GettingStarted.Models.PostModel;
import com.makepe.blackout.R;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class UniversalFunctions {
    private Context context;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference postReference = FirebaseDatabase.getInstance().getReference("Posts");
    DatabaseReference likesReference =  FirebaseDatabase.getInstance().getReference("Likes");
    DatabaseReference commentsReference = FirebaseDatabase.getInstance().getReference("Comments");
    DatabaseReference savesReference = FirebaseDatabase.getInstance().getReference().child("Saves");


    public UniversalFunctions(Context context) {
        this.context = context;
    }

    public UniversalFunctions() {
    }

    public void findAddress(PostModel listItem, TextView postCheckIn) {
        //find address, country, state, city
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context, Locale.getDefault());

            addresses = geocoder.getFromLocation(listItem.getLatitude(), listItem.getLongitude(), 1);

            String address = addresses.get(0).getAddressLine(0);//complete address

            postCheckIn.setText(address);
            postCheckIn.setVisibility(View.VISIBLE);
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

    public void addLikesNotifications(String userid, String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "liked your post");
        hashMap.put("postid", postid);
        hashMap.put("ispost", true);
        hashMap.put("isStory", false);
        hashMap.put("timeStamp", timeStamp);

        reference.push().setValue(hashMap);
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
        DatabaseReference viewsReference = FirebaseDatabase.getInstance().getReference("VideoViews")
                .child(videoPost.getPostID());
        viewsReference.addValueEventListener(new ValueEventListener() {
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

    public void addVideoLikesNotification(String postID, String userID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userID);
        String timeStamp = String.valueOf(System.currentTimeMillis());

        String key = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("notificationID", key);
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "liked your video");
        hashMap.put("postid", postID);
        hashMap.put("ispost", true);
        hashMap.put("timeStamp", timeStamp);

        reference.child(key).setValue(hashMap);
    }

    public void addFollowNotifications(String user_id) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(user_id);
        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);
        hashMap.put("isStory", false);
        hashMap.put("timeStamp", timeStamp);

        reference.push().setValue(hashMap);
    }
}
