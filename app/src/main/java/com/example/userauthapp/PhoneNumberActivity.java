package com.example.userauthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.santalu.maskara.widget.MaskEditText;

public class PhoneNumberActivity extends AppCompatActivity implements View.OnClickListener {
    TextInputLayout tilPhone;
    MaskEditText metPhone;
    ProgressBar progressBar;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        Button btnSendOTP = findViewById(R.id.btn_send_otp);
        btnSendOTP.setOnClickListener(this);

        // database reference
        reference = FirebaseDatabase.getInstance().getReference("Users");

        // initialize some view at phone number layout
        tilPhone = findViewById(R.id.tilPhone);
        metPhone = findViewById(R.id.metPhone);
        progressBar = findViewById(R.id.progress_bar);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_send_otp) {
            String phonenumber = "0" + metPhone.getUnMasked();
            sendOTP(phonenumber);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private boolean validateField(TextInputLayout textInputLayout, String text) {
        if (text.equals("0")) {
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

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isExist = false;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user.getPhoneNumber().equals(phonenumber)) {
                        isExist = true;
                        break;
                    }
                }

                if (isExist) {
                    Intent otpAuthIntent = new Intent(getApplicationContext(), OTPAuthActivity.class);
                    otpAuthIntent.putExtra("phonenumber", phonenumber);
                    startActivity(otpAuthIntent);
                } else {
                    Toast.makeText(PhoneNumberActivity.this, "Phone number not found.", Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PhoneNumberActivity.this, "Oops, something wrong happen.", Toast.LENGTH_SHORT).show();

                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
    }
}