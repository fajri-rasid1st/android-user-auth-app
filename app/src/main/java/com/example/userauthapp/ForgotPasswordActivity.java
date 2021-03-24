package com.example.userauthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    TextInputLayout tilEmail;
    TextInputEditText tietEmail;
    ProgressBar progressBar;
    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Button btnForgot = findViewById(R.id.btn_forgot);
        btnForgot.setOnClickListener(this);

        tilEmail = findViewById(R.id.tilEmail);
        tietEmail = findViewById(R.id.tietEmail);

        progressBar = findViewById(R.id.progress_bar);

        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_forgot) {
            if (validateField(tietEmail, tilEmail) && validateEmail(tietEmail, tilEmail)) {
                resetPassword();
            }
        }
    }

    private boolean validateField(TextInputEditText tiet, TextInputLayout til) {
        boolean isEmpty = tiet.getEditableText().toString().trim().isEmpty();

        if (isEmpty) {
            til.setErrorEnabled(true);
            til.setError("Field can't be empty.");
        } else {
            til.setErrorEnabled(false);
        }

        return !isEmpty;
    }

    private boolean validateEmail(TextInputEditText tiet, TextInputLayout til) {
        if (!Patterns.EMAIL_ADDRESS.matcher(tiet.getEditableText().toString().trim()).matches()) {
            til.setErrorEnabled(true);
            til.setError("Please provide valid email.");
            return false;
        } else {
            til.setErrorEnabled(false);
            return true;
        }
    }

    private void resetPassword() {
        String email = tietEmail.getEditableText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        mFirebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Check your email to reset your password.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Something wrong happen. Try Again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}