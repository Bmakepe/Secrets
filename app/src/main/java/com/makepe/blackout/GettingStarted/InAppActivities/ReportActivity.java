package com.makepe.blackout.GettingStarted.InAppActivities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.blackout.GettingStarted.Models.GroupsModel;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReportActivity extends AppCompatActivity {

    private EditText myNameET, reportedPersonET, messageET;
    private Button submitReportBTN;
    private CircleImageView reportPic;

    private DatabaseReference userReference, groupReference, reportReference;
    private FirebaseUser firebaseUser;

    private String myName, hisName, reportMessage, reported;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Toolbar movementToolbar = findViewById(R.id.report_toolbar);
        setSupportActionBar(movementToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        reported = intent.getStringExtra("reported");

        myNameET = findViewById(R.id.reportName);
        reportedPersonET = findViewById(R.id.reportedPerson);
        messageET = findViewById(R.id.reportMessage);
        submitReportBTN = findViewById(R.id.submitReportBTN);
        reportPic = findViewById(R.id.reportPic);

        userReference = FirebaseDatabase.getInstance().getReference("Users");
        groupReference = FirebaseDatabase.getInstance().getReference("SecretGroups");
        reportReference = FirebaseDatabase.getInstance().getReference("Reports");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getMyDetails();
        getReportedDetails();

        submitReportBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myName = myNameET.getText().toString();
                hisName = reportedPersonET.getText().toString();
                reportMessage = messageET.getText().toString();

                if (TextUtils.isEmpty(myName)){
                    myNameET.setError("Enter your name");
                    myNameET.requestFocus();
                }else if (TextUtils.isEmpty(hisName)){
                    reportedPersonET.setError("Enter your name");
                    reportedPersonET.requestFocus();
                }else if (TextUtils.isEmpty(reportMessage)){
                    messageET.setError("Enter your name");
                    messageET.requestFocus();
                }else{
                    submitReport();
                }
            }
        });

    }

    private void submitReport() {

        String reportID = reportReference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("reportID", reportID);
        hashMap.put("reporterID", firebaseUser.getUid());
        hashMap.put("reportedID", reported);
        hashMap.put("reportedMessage", reportMessage);

        reportReference.child(reportID).setValue(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ReportActivity.this, "Report Submitted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ReportActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void getReportedDetails() {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUserID().equals(reported)){
                        reportedPersonET.setText(user.getUsername());
                    }else{
                        groupReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    GroupsModel groupsModel = ds.getValue(GroupsModel.class);

                                    assert groupsModel != null;
                                    if (groupsModel.getGroupID().equals(reported)){
                                        reportedPersonET.setText(groupsModel.getGroupName());
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

    private void getMyDetails() {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    assert user != null;
                    if (user.getUserID().equals(firebaseUser.getUid())){
                        myNameET.setText(user.getUsername());

                        try{
                            Picasso.get().load(user.getImageURL()).into(reportPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.default_profile_display_pic).into(reportPic);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}