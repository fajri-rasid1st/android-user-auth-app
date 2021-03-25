package com.example.userauthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    private ProgressBar progressBar;
    private TextInputLayout tilEmail, tilPassword;
    private FirebaseAuth mFirebaseAuth;
    private Toast exitToast;
    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        TextView tvSignUp = findViewById(R.id.tv_sign_up);
        tvSignUp.setOnClickListener(this);

        TextView tvForgotPass = findViewById(R.id.tv_forgot_password);
        tvForgotPass.setOnClickListener(this);

        Button btnSignIn = findViewById(R.id.btn_sign_in);
        btnSignIn.setOnClickListener(this);

        mFirebaseAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progress_bar);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tv_sign_up) {

            Intent signUpIntent = new Intent(SignInActivity.this, SignUpActivity.class);
            signUpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(signUpIntent);

        } else if (view.getId() == R.id.tv_forgot_password) {

            Intent forgotPassIntent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
            forgotPassIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(forgotPassIntent);

        } else if (view.getId() == R.id.btn_sign_in) {

            String email = Objects.requireNonNull(tilEmail.getEditText()).getText().toString().trim();
            String password = Objects.requireNonNull(tilPassword.getEditText()).getText().toString().trim();
            confirmSignIn(email, password);

        }
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            exitToast.cancel();
            super.onBackPressed();
            return;
        } else {
            exitToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
            exitToast.show();
        }

        backPressedTime = System.currentTimeMillis();
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

    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            tilPassword.setErrorEnabled(true);
            tilPassword.setError("Field can't be empty.");
            return false;
        } else if (password.length() < 6) {
            tilPassword.setErrorEnabled(true);
            tilPassword.setError("Password must have at least 6 characters.");
            return false;
        } else {
            tilEmail.setErrorEnabled(false);
            return true;
        }
    }

    private void confirmSignIn(String email, String password) {
        if (!validateEmail(email) | !validatePassword(password)) return;

        progressBar.setVisibility(View.VISIBLE);

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent homeIntent = new Intent(SignInActivity.this, HomeActivity.class);
                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(homeIntent);
                        } else {
                            Toast.makeText(SignInActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_LONG).show();
                        }

                        progressBar.setVisibility(View.GONE);
                    }
                });

    }
}