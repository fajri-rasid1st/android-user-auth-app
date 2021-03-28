package com.example.userauthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OTPAuthActivity extends AppCompatActivity implements View.OnClickListener {
    private TextInputEditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private TextInputLayout tilOtp1, tilOtp2, tilOtp3, tilOtp4, tilOtp5, tilOtp6;
    private TextView tvPhoneNumber;
    private ProgressBar progressBar;
    private String verificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o_t_p_auth);

        progressBar = findViewById(R.id.progress_bar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        verificationId = getIntent().getStringExtra("verificationid");

        Button btnSubmitOTP = findViewById(R.id.btn_submit_otp);
        btnSubmitOTP.setOnClickListener(this);

        TextView tvResendOtp = findViewById(R.id.tv_resend_otp);
        tvResendOtp.setOnClickListener(this);

        tvPhoneNumber = findViewById(R.id.text_phone_number);
        tvPhoneNumber.setText(getIntent().getStringExtra("phonenumber"));

        // initialize edit text
        etOtp1 = findViewById(R.id.otpEditText1);
        etOtp2 = findViewById(R.id.otpEditText2);
        etOtp3 = findViewById(R.id.otpEditText3);
        etOtp4 = findViewById(R.id.otpEditText4);
        etOtp5 = findViewById(R.id.otpEditText5);
        etOtp6 = findViewById(R.id.otpEditText6);

        // initialize text input layout
        tilOtp1 = findViewById(R.id.otpLayout1);
        tilOtp2 = findViewById(R.id.otpLayout2);
        tilOtp3 = findViewById(R.id.otpLayout3);
        tilOtp4 = findViewById(R.id.otpLayout4);
        tilOtp5 = findViewById(R.id.otpLayout5);
        tilOtp6 = findViewById(R.id.otpLayout6);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toast.makeText(OTPAuthActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toast.makeText(OTPAuthActivity.this, "OTP code has been sended", Toast.LENGTH_SHORT).show();

                verificationId = s;
            }
        };

        setUpOTPInputs();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_submit_otp) {
            String otpInput = Objects.requireNonNull(etOtp1.getText()).toString()
                    + Objects.requireNonNull(etOtp2.getText()).toString()
                    + Objects.requireNonNull(etOtp3.getText()).toString()
                    + Objects.requireNonNull(etOtp4.getText()).toString()
                    + Objects.requireNonNull(etOtp5.getText()).toString()
                    + Objects.requireNonNull(etOtp6.getText()).toString();

            if (!validationField(tilOtp1)
                    | !validationField(tilOtp2)
                    | !validationField(tilOtp3)
                    | !validationField(tilOtp4)
                    | !validationField(tilOtp5)
                    | !validationField(tilOtp6)) return;

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otpInput.trim());
            signInWithPhoneAuthCredential(credential);
        } else if (view.getId() == R.id.tv_resend_otp) {
            sendOTP(getIntent().getStringExtra("phonenumber"));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void sendOTP(String phonenumber) {
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mFirebaseAuth)
                        .setPhoneNumber(phonenumber) // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Time out and unit
                        .setActivity(OTPAuthActivity.this) // Activity (for callback binding)
                        .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String phonenumber = tvPhoneNumber.getText().toString();

                            User user = new User("Anonymous", "none", phonenumber);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(OTPAuthActivity.this, "Register failed. Try again.", Toast.LENGTH_SHORT).show();
                                            }

                                            progressBar.setVisibility(View.GONE);
                                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        }
                                    });

                            Intent homeActivity = new Intent(OTPAuthActivity.this, HomeActivity.class);
                            startActivity(homeActivity);
                            finish();
                        } else {
                            Toast.makeText(OTPAuthActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        progressBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                });
    }

    private boolean validationField(TextInputLayout textInputLayout) {
        if (Objects.requireNonNull(textInputLayout.getEditText()).getText().toString().isEmpty()) {
            textInputLayout.setErrorEnabled(true);
            textInputLayout.setError("*");
            return false;
        } else {
            textInputLayout.setErrorEnabled(false);
            return true;
        }
    }

    private void setUpOTPInputs() {
        etOtp1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()) {
                    etOtp2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        etOtp2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()) {
                    etOtp3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        etOtp3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()) {
                    etOtp4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        etOtp4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()) {
                    etOtp5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        etOtp5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()) {
                    etOtp6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
}