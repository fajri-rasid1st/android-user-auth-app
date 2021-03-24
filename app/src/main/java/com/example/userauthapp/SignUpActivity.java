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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    TextView tvSignIn;
    Button btnSignUp;
    ProgressBar progressBar;
    TextInputLayout tilEmail, tilPassword, tilFullname, tilRepeatPass, tilPhone;
    TextInputEditText tietEmail, tietPassword, tietFullname, tietRepeatPass, tietPhone;
    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mFirebaseAuth = FirebaseAuth.getInstance();

        tvSignIn = findViewById(R.id.tv_sign_in);
        tvSignIn.setOnClickListener(this);

        btnSignUp = findViewById(R.id.btn_sign_up);
        btnSignUp.setOnClickListener(this);

        progressBar = findViewById(R.id.progress_bar);

        tilEmail = findViewById(R.id.tilEmail);
        tietEmail = findViewById(R.id.tietEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tietPassword = findViewById(R.id.tietPassword);
        tilRepeatPass = findViewById(R.id.tilRepeatPassword);
        tietRepeatPass = findViewById(R.id.tietRepeatPassword);
        tilFullname = findViewById(R.id.tilFullname);
        tietFullname = findViewById(R.id.tietFullname);
        tilPhone = findViewById(R.id.tilPhone);
        tietPhone = findViewById(R.id.tietPhone);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tv_sign_in) {
            Intent signInIntent = new Intent(SignUpActivity.this, SignInActivity.class);
            signInIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(signInIntent);
        } else if (view.getId() == R.id.btn_sign_up) {
            if (
                    validateField(tietFullname, tilFullname) &&
                            validateField(tietEmail, tilEmail) &&
                            validateField(tietPassword, tilPassword) &&
                            validateField(tietRepeatPass, tilRepeatPass) &&
                            validateField(tietPhone, tilPhone)
            ) {
                if (
                        validateEmail(tietEmail, tilEmail) &&
                                validateRepeatPassword(tietRepeatPass, tietPassword, tilRepeatPass)
                ) {
                    String email = tietEmail.getEditableText().toString().trim();
                    String password = tietPassword.getEditableText().toString().trim();
                    String fullname = tietFullname.getEditableText().toString().trim();
                    String phonenumber = tietPhone.getEditableText().toString().trim();

                    progressBar.setVisibility(View.VISIBLE);

                    mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        User user = new User(fullname, email, phonenumber);

                                        FirebaseDatabase.getInstance().getReference("Users")
                                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).setValue(user)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(SignUpActivity.this, "Your account has been register successfully.", Toast.LENGTH_LONG).show();
                                                            Intent signInIntent = new Intent(SignUpActivity.this, SignInActivity.class);
                                                            startActivity(signInIntent);
                                                        } else {
                                                            Toast.makeText(SignUpActivity.this, "Register failed. Bad connection.", Toast.LENGTH_LONG).show();
                                                        }
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(SignUpActivity.this, "Register failed. Bad connection.", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
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

    private boolean validateRepeatPassword(TextInputEditText tiet, TextInputEditText tietPassword, TextInputLayout til) {
        boolean isEquals = tietPassword.getEditableText().toString().trim().equals(tiet.getEditableText().toString().trim());

        if (isEquals) {
            til.setErrorEnabled(false);
        } else {
            til.setErrorEnabled(true);
            til.setError("Does not match with password field.");
        }

        return isEquals;
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
}