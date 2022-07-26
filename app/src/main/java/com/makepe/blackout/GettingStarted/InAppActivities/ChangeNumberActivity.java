package com.makepe.blackout.GettingStarted.InAppActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ChangeNumberActivity extends AppCompatActivity {

    private Button verifyOTP;
    private ImageView propic;
    private EditText currentNumber, newPhoneNumber;
    private FirebaseUser user;
    private CountryCodePicker oldCodePicker, newCodePicker;
    private String mVerificationId;
    private FirebaseAuth mAuth;
    private PinView otpED;

    private String oldNumber, newNumber, newPhoneContact;
    private DatabaseReference userRef;

    private ProgressDialog progressDialog;

    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_number);

        Toolbar changeNumberToolbar = findViewById(R.id.changeNumberToolbar);
        setSupportActionBar(changeNumberToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        verifyOTP = findViewById(R.id.verifyOTPBtn);
        currentNumber = findViewById(R.id.oldNumber);
        newPhoneNumber = findViewById(R.id.new_phone_Number);
        oldCodePicker = findViewById(R.id.oldNumberCode);
        newCodePicker = findViewById(R.id.new_code_Picker);
        otpED = findViewById(R.id.otp_edtText);
        propic = findViewById(R.id.numberPropic);

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        user = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        getUserDetails();

        verifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (verifyOTP.getText().toString().trim().equals("Update")) {

                    progressDialog.setMessage("Loading...");

                    oldNumber = currentNumber.getText().toString().trim();
                    newNumber = newPhoneNumber.getText().toString().trim();

                    oldCodePicker.registerCarrierNumberEditText(currentNumber);
                    String oldPhoneNumber = oldCodePicker.getFullNumberWithPlus();

                    newCodePicker.registerCarrierNumberEditText(newPhoneNumber);
                    newPhoneContact = newCodePicker.getFullNumberWithPlus();

                    if (oldPhoneNumber.equals(user.getPhoneNumber())) {

                        if (newNumber.isEmpty() || newNumber.length() < 8) {
                            newPhoneNumber.setError("Enter a valid number");
                            newPhoneNumber.requestFocus();
                        } else {

                            progressDialog.show();
                            sendOTP(newPhoneContact);
                            Toast.makeText(ChangeNumberActivity.this, "Numbers Match Correctly, a code will be sent to " + newPhoneContact, Toast.LENGTH_SHORT).show();

                        }

                    } else {

                        currentNumber.setError("Numbers do not match");
                        currentNumber.requestFocus();
                    }
                }else{
                    String code = otpED.getText().toString().trim();

                    if(code.isEmpty() || code.length() < 6){
                        otpED.setError("Enter a valid code");
                        otpED.requestFocus();
                        return;
                    }else{
                        progressDialog.setMessage("Verifying, Please Wait...");
                        progressDialog.show();
                        verifyVerificationCode(code);
                    }
                }

            }

        });

        findViewById(R.id.resendOTP).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//if OTP has not been send, click to generate another code
                Toast.makeText(ChangeNumberActivity.this, "Your OTP will be sent again", Toast.LENGTH_SHORT).show();
                resendOTP(newPhoneContact, mResendToken);//function to generate another OTP
            }
        });
    }

    private void resendOTP(String newPhoneContact, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                newPhoneContact,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                token
        );
    }

    private void verifyVerificationCode(String code) {
        //function to verify the code that has been send
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        //mAuth.signInWithCredential(credential)
        mAuth.getCurrentUser().updatePhoneNumber(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){//executes when the verification code is correct
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("phone", newPhoneContact);

                            assert user != null;
                            ref.child(user.getUid()).updateChildren(hashMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(ChangeNumberActivity.this, "Phone Number Changed Successfully", Toast.LENGTH_SHORT).show();
                                            otpED.setText(null);
                                            currentNumber.setText(null);
                                            newPhoneNumber.setText(null);
                                            progressDialog.dismiss();
                                            finish();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ChangeNumberActivity.this, "Could not change your phone number", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }else{
                            String message = "Something is wrong, we will fix it soon...";

                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                message = "Invalid code entered";
                            }

                            Snackbar snackbar = Snackbar.make(findViewById(R.id.parent), message, Snackbar.LENGTH_LONG);
                            snackbar.setAction("Dismiss", new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {

                                }
                            });
                            snackbar.show();
                        }
                    }
                });
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            //get verification automatically as the sms comes in and verify
            String code = phoneAuthCredential.getSmsCode();

            if(code != null){
                otpED.setText(code);
                verifyVerificationCode(code);
            }

        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

            //toast error message if number can not be verified
            Toast.makeText(ChangeNumberActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            //this is for resending the generated verification code
            mVerificationId = s;
            mResendToken = forceResendingToken;

        }
    };

    private void sendOTP(String newPhoneContact) {
        //function to send the verification code
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                newPhoneContact,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        );

        verifyOTP.setText("Verify");
        progressDialog.dismiss();
    }

    private void getUserDetails() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    User myUser = ds.getValue(User.class);
                    assert myUser != null;
                    if (myUser.getUserID().equals(user.getUid())) {
                        try {
                            Picasso.get().load(myUser.getImageURL()).into(propic);
                        } catch (NullPointerException ignored) {}
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