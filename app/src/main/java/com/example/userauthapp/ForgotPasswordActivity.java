package com.example.userauthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    TextInputLayout tilEmail;
    ProgressBar progressBar;
    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Button btnForgot = findViewById(R.id.btn_forgot);
        btnForgot.setOnClickListener(this);

        tilEmail = findViewById(R.id.tilEmail);
        progressBar = findViewById(R.id.progress_bar);
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_forgot) {
            String email = Objects.requireNonNull(tilEmail.getEditText()).getText().toString().trim();
            resetPassword(email);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            tilEmail.setErrorEnabled(true);
            tilEmail.setError("Field can't be empty.");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setErrorEnabled(true);
            tilEmail.setError("Please provide valid email.");
            return false;
        } else {
            tilEmail.setErrorEnabled(false);
            return true;
        }
    }

    private void resetPassword(String email) {
        if (!validateEmail(email)) return;

        tilEmail.setErrorEnabled(false);

        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        mFirebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Check your email to reset your password", Toast.LENGTH_LONG).show();
                    Intent signInIntent = new Intent(ForgotPasswordActivity.this, SignInActivity.class);
                    startActivity(signInIntent);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Something wrong happen. Try Again.", Toast.LENGTH_LONG).show();
                }

                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
    }
}