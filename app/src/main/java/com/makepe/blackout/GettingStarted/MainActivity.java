package com.makepe.blackout.GettingStarted;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hbb20.CountryCodePicker;
import com.makepe.blackout.GettingStarted.InAppActivities.HomeConsoleActivity;
import com.makepe.blackout.R;

public class MainActivity extends AppCompatActivity {

    CountryCodePicker codePicker;
    EditText numberED;
    TextView registerBTN;
    ProgressDialog progressDialog;

    FirebaseUser firebaseUser;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //check if there is a user who is already logged in when the app opens
        if(firebaseUser != null){
            startActivity(new Intent(MainActivity.this, HomeConsoleActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        codePicker = findViewById(R.id.codeTIL);
        numberED = findViewById(R.id.phoneNumberArea);
        registerBTN = findViewById(R.id.registerBTN);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registration in progress...");

        //click register button to get input number and prepare to send OTP by parsing the number to next activity
        registerBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                String mobile;

                mobile = numberED.getText().toString().trim();//get number
                codePicker.registerCarrierNumberEditText(numberED);//add picked country code to number which has been entered
                String number = codePicker.getFullNumberWithPlus();//store number with + apprehended at the beginning of the number

                if(mobile.isEmpty()||mobile.length()< 8){
                    numberED.setError("Enter a valid number");
                    numberED.requestFocus();
                    return;
                }

                progressDialog.dismiss();
                Intent intent = new Intent(MainActivity.this, VerifyPhoneActivity.class);
                intent.putExtra("number", number);//parse number the next activity for processing
                startActivity(intent);

            }
        });
    }

}
