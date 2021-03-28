package com.example.userauthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.santalu.maskara.widget.MaskEditText;

import java.util.concurrent.TimeUnit;

public class PhoneNumberActivity extends AppCompatActivity {
    private TextInputLayout tilPhone;
    private MaskEditText metPhone;
    private ProgressBar progressBar;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        // firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        // initialize some view at phone number layout
        tilPhone = findViewById(R.id.tilPhone);
        metPhone = findViewById(R.id.metPhone);
        progressBar = findViewById(R.id.progress_bar);

        Button btnSendOTP = findViewById(R.id.btn_send_otp);
        btnSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phonenumber = metPhone.getUnMasked();
                sendOTP("+62" + phonenumber);
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toast.makeText(PhoneNumberActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                Toast.makeText(PhoneNumberActivity.this, "OTP code has been sended", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String phone = "+62" + metPhone.getUnMasked();
                        Intent otpAuthIntent = new Intent(PhoneNumberActivity.this, OTPAuthActivity.class);
                        otpAuthIntent.putExtra("verificationid", s);
                        otpAuthIntent.putExtra("phonenumber", phone);
                        startActivity(otpAuthIntent);
                    }
                }, 3000);
            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private boolean validateField(TextInputLayout textInputLayout, String text) {
        if (text.equals("+62")) {
            textInputLayout.setErrorEnabled(true);
            textInputLayout.setError("Field can't be empty.");
            return false;
        }

        textInputLayout.setErrorEnabled(false);
        return true;
    }

    private void sendOTP(String phonenumber) {
        if (!validateField(tilPhone, phonenumber)) return;

        tilPhone.setErrorEnabled(false);

        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mFirebaseAuth)
                        .setPhoneNumber(phonenumber) // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Time out and unit
                        .setActivity(PhoneNumberActivity.this) // Activity (for callback binding)
                        .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}