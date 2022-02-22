package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.makepe.blackout.GettingStarted.Models.ContactsModel;
import com.makepe.blackout.GettingStarted.OtherClasses.ContactsList;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddStoryActivity extends AppCompatActivity {

    private Uri mImageUri;
    String myUri = "";
    private StorageTask storageTask;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    FirebaseUser user;

    String myName, userProPic;

    CircleImageView storyProPic;
    ImageView storyPic;
    TextView storyUser;
    EditText storyCaption;
    FloatingActionButton storyFAB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        storyProPic = findViewById(R.id.addStoryProPic);
        storyPic = findViewById(R.id.storyPic);
        storyCaption = findViewById(R.id.storyCaption);
        storyUser = findViewById(R.id.addStoryUsername);
        storyFAB = findViewById(R.id.postStoryFAB);

        storageReference = FirebaseStorage.getInstance().getReference("Story");
        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        databaseReference.keepSynced(true);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    myName = "" + snapshot.child("Username").getValue();
                    userProPic = "" + snapshot.child("ImageURL").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        getUserDetails();

        CropImage.activity()
                .setAspectRatio(9,16)
                .start(AddStoryActivity.this);

        storyFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishStory();
            }
        });
        findViewById(R.id.storyBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getUserDetails() {
        List<ContactsModel> phoneBook = new ArrayList<>();
        ContactsList contactsList = new ContactsList(phoneBook, AddStoryActivity.this);
        contactsList.readContacts();
        final List<ContactsModel> myContacts = contactsList.getContactsList();

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = reference.orderByChild("USER_ID").equalTo(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        String number = "" + snapshot.child("Number").getValue();
                        String proPic = "" + snapshot.child("ImageURL").getValue();

                        for(ContactsModel contactsModel : myContacts){
                            if(contactsModel.getNumber().equals(number)){
                                storyUser.setText(contactsModel.getUsername());
                            }
                        }

                        try{
                            Picasso.get().load(proPic).into(storyProPic);
                        }catch (NullPointerException ignored){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void publishStory(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Updating Story");
        pd.show();

        if(mImageUri != null){
            final StorageReference imageReference = storageReference.child(System.currentTimeMillis()
            + "." + getFileExtension(mImageUri) );
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            storageTask = imageReference.putFile(mImageUri);
            storageTask.continueWithTask(new Continuation(){
                @Override
                public Task<Uri> then(@NonNull Task task) throws Exception{
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUri = downloadUri.toString();

                        String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                                .child(myid);

                        String storyid = reference.push().getKey();
                        long timeend = System.currentTimeMillis() + 86400000; //1 day
                        String timeStamp = String.valueOf(System.currentTimeMillis());

                        HashMap<String, Object> hashMap = new HashMap<>();

                        hashMap.put("PostImage", myUri);
                        hashMap.put("timestart", ServerValue.TIMESTAMP);
                        hashMap.put("timeend", timeend);
                        hashMap.put("storyid", storyid);
                        hashMap.put("userid", myid);
                        assert user != null;
                        hashMap.put("userNum", user.getPhoneNumber());
                        hashMap.put("storyTimeStamp", timeStamp);
                        hashMap.put("storyCaption", storyCaption.getText().toString());

                        reference.child(storyid).setValue(hashMap);
                        pd.dismiss();

                        finish();
                    }else{
                        Toast.makeText(AddStoryActivity.this, "failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddStoryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();

            storyPic.setImageURI(mImageUri);
        }else {
            Toast.makeText(this, "Something gone wrong", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddStoryActivity.this, HomeConsoleActivity.class));
            finish();
        }
    }
}
