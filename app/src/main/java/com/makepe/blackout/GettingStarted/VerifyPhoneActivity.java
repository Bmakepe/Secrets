package com.makepe.blackout.GettingStarted;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
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
import com.makepe.blackout.GettingStarted.Models.User;
import com.makepe.blackout.R;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VerifyPhoneActivity extends AppCompatActivity {

    private String mVerificationId;
    private EditText editTextCode;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView resendOTP, changeNum, subHeading, countDownTimerTV;
    private Button verifyBTN;
    private ProgressDialog verifyDialog;
    private DatabaseReference userReference;
    private FirebaseUser firebaseUser;

    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private long duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        editTextCode = findViewById(R.id.verificationCode);
        progressBar = findViewById(R.id.verifyLoader);
        changeNum = findViewById(R.id.change_number);
        resendOTP = findViewById(R.id.resendOTP);
        subHeading = findViewById(R.id.subHeading);
        verifyBTN = findViewById(R.id.verifyBTN);
        countDownTimerTV = findViewById(R.id.countDownTimer);

        Intent intent = getIntent();
        final String number = intent.getExtras().getString("number");//retrieve number from previous activity

        userReference = FirebaseDatabase.getInstance().getReference("Users");
        
        duration = TimeUnit.MINUTES.toMillis(2);
        
        createTimer();

        verifyDialog = new ProgressDialog(this);
        subHeading.setText("We are automatically detecting a verification SMS send to your mobile number " + number);
        mAuth = FirebaseAuth.getInstance();
        sendVerificationCode(number);//send the verification code to number which has been entered

        verifyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editTextCode.getText().toString().trim();//get verification code from edit text

                if(code.isEmpty()||code.length() < 6){
                    editTextCode.setError("Enter valid code");
                    editTextCode.requestFocus();
                    return;
                }
                verifyBTN.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                verifyVerificationCode(code);// verify verification code
            }
        });

        resendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//if OTP has not been send, click to generate another code
                Toast.makeText(VerifyPhoneActivity.this, "Your OTP will be sent again", Toast.LENGTH_SHORT).show();
                resendOTP(number, mResendToken);//function to generate another OTP
            }
        });

        changeNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VerifyPhoneActivity.this, RegisterActivity.class));
                finish();
            }
        });
    }

    private void createTimer() {
        CountDownTimer countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long l) {
                String sDuration = String.format(Locale.ENGLISH, "%02d:%02d"
                        , TimeUnit.MILLISECONDS.toMinutes(l)
                        , TimeUnit.MILLISECONDS.toSeconds(l) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l)));
                countDownTimerTV.setText(sDuration);
            }

            @Override
            public void onFinish() {

            }
        };
        countDownTimer.start();
    }

    private void resendOTP(String number, PhoneAuthProvider.ForceResendingToken token) {//resend OTP function
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                token
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            //get verification automatically as the sms comes in and verify
            String code = phoneAuthCredential.getSmsCode();

            if(code != null){
                editTextCode.setText(code);
                verifyVerificationCode(code);
            }

        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

            if (e instanceof FirebaseAuthInvalidCredentialsException){
                Toast.makeText(VerifyPhoneActivity.this, "Invalid Request", Toast.LENGTH_SHORT).show();
            }else if (e instanceof FirebaseTooManyRequestsException){
                Toast.makeText(VerifyPhoneActivity.this, "The sms quota for the project has been exceeded", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onCodeSent(@NonNull String verificationID, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(verificationID, forceResendingToken);

            //this is for resending the generated verification code
            mVerificationId = verificationID;
            mResendToken = forceResendingToken;

        }
    };

    private void verifyVerificationCode(String code) {
        //function to verify the code that has been send
        verifyDialog.setMessage("Verifying...Please Wait...");
        verifyDialog.setCancelable(false);
        verifyDialog.show();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyPhoneActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){//executes when the verification code is correct

                            userReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                    assert firebaseUser != null;
                                    if (snapshot.child(firebaseUser.getUid()).exists()){
                                        Intent intent = new Intent(VerifyPhoneActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }else{
                                        Intent detailsIntent = new Intent(VerifyPhoneActivity.this, UserDetailsActivity.class);
                                        detailsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(detailsIntent);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

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

    private void sendVerificationCode(String number) {

        //function to send the verification code
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        );
    }
}
