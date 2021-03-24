package com.example.userauthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    ProgressBar progressBar;
    TextInputLayout tilEmail, tilPassword;
    TextInputEditText tietEmail, tietPassword;
    FirebaseAuth mFirebaseAuth;

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

        progressBar = findViewById(R.id.progress_bar);
        mFirebaseAuth = FirebaseAuth.getInstance();
        tilEmail = findViewById(R.id.tilEmail);
        tietEmail = findViewById(R.id.tietEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tietPassword = findViewById(R.id.tietPassword);
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
            if (validateField(tietEmail, tilEmail) && validateField(tietPassword, tilPassword)) {
                String email = tietEmail.getEditableText().toString().trim();
                String password = tietPassword.getEditableText().toString().trim();

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
}