package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedbackActivity extends AppCompatActivity {

    CircleImageView feedbackPic;
    EditText nameET, numberET, messageET;
    TextView submitBTN, rateCounter;

    DatabaseReference userRef, enquiryRef;
    FirebaseUser firebaseUser;

    String name, number, message;

    RatingBar ratingBar;
    float rateValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        feedbackPic = findViewById(R.id.feedbackPic);
        nameET = findViewById(R.id.feedbackName);
        numberET = findViewById(R.id.feedbackNumber);
        messageET = findViewById(R.id.feedbackMessage);
        submitBTN = findViewById(R.id.submitFeedback);
        ratingBar = findViewById(R.id.feedbackRating);
        rateCounter = findViewById(R.id.feedbackRateCounter);

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        enquiryRef = FirebaseDatabase.getInstance().getReference("Enquiries");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getUserDetails();

        submitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = nameET.getText().toString().trim();
                number = numberET.getText().toString().trim();
                message = messageET.getText().toString().trim();

                if (TextUtils.isEmpty(name)){

                    nameET.setError("Enter Your Name Here");
                    nameET.requestFocus();

                }else if (TextUtils.isEmpty(number)){

                    numberET.setError("Enter Your Number Here");
                    numberET.requestFocus();

                }else if (TextUtils.isEmpty(message)){

                    messageET.setError("Write you message here");
                    messageET.requestFocus();

                }else if(rateValue == 0){
                    Toast.makeText(FeedbackActivity.this, "Please rate properly", Toast.LENGTH_SHORT).show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(FeedbackActivity.this);
                    builder.setTitle("Feedback!!!")
                            .setMessage("Thank you for your feedback!! ")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    submitEnquiry(rateValue);
                                }
                            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
                }
            }
        });

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {

                rateValue = ratingBar.getRating();

                if(rateValue <= 1 && rateValue > 0)
                    rateCounter.setText("Very Bad " + rateValue + "/5");
                else if(rateValue <= 2 && rateValue > 1)
                    rateCounter.setText("Bad " + rateValue + "/5");
                else if(rateValue <= 3 && rateValue > 2)
                    rateCounter.setText("Good " + rateValue + "/5");
                else if(rateValue <= 4 && rateValue > 3)
                    rateCounter.setText("Very Good " + rateValue + "/5");
                else if(rateValue <= 5 && rateValue > 4)
                    rateCounter.setText("Excellent " + rateValue + "/5");

            }
        });

        findViewById(R.id.feedbackBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void getUserDetails() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if (user.getUSER_ID().equals(firebaseUser.getUid())){

                        nameET.setText(user.getUsername());
                        numberET.setText(user.getNumber());

                        try{
                            Picasso.get().load(user.getImageURL()).into(feedbackPic);
                        }catch (NullPointerException ignored){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void submitEnquiry(float rateValue) {
        String key = enquiryRef.push().getKey();

        final String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userID", firebaseUser.getUid());
        hashMap.put("publisherId", name);
        hashMap.put("number", number);
        hashMap.put("message", message);
        hashMap.put("rating", rateValue);
        hashMap.put("timestamp", timestamp);
        hashMap.put("reviewID", key);

        enquiryRef.child(key).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(FeedbackActivity.this, "Your enquiry has been submitted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FeedbackActivity.this, "Could not submit your enquiry", Toast.LENGTH_SHORT).show();
            }
        });

    }
}