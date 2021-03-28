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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.santalu.maskara.widget.MaskEditText;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    ProgressBar progressBar;
    TextInputLayout tilEmail, tilPassword, tilFullname, tilRepeatPass, tilPhone;
    MaskEditText metPhone;
    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        TextView tvSignIn = findViewById(R.id.tv_sign_in);
        tvSignIn.setOnClickListener(this);

        Button btnSignUp = findViewById(R.id.btn_sign_up);
        btnSignUp.setOnClickListener(this);

        mFirebaseAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progress_bar);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilRepeatPass = findViewById(R.id.tilRepeatPassword);
        tilFullname = findViewById(R.id.tilFullname);
        tilPhone = findViewById(R.id.tilPhone);
        metPhone = findViewById(R.id.metPhone);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tv_sign_in) {

            Intent signInIntent = new Intent(SignUpActivity.this, SignInActivity.class);
            signInIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(signInIntent);

        } else if (view.getId() == R.id.btn_sign_up) {

            String fullname = Objects.requireNonNull(tilFullname.getEditText()).getText().toString().trim();
            String email = Objects.requireNonNull(tilEmail.getEditText()).getText().toString().trim();
            String password = Objects.requireNonNull(tilPassword.getEditText()).getText().toString().trim();
            String repeatPass = Objects.requireNonNull(tilRepeatPass.getEditText()).getText().toString().trim();
            String phonenumber = "+62" + metPhone.getUnMasked();
            confirmSignUp(fullname, email, password, repeatPass, phonenumber);

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private boolean validateField(TextInputLayout textInputLayout, String text) {
        if (text.isEmpty()) {
            textInputLayout.setErrorEnabled(true);
            textInputLayout.setError("Field can't be empty.");
            return false;
        }

        textInputLayout.setErrorEnabled(false);
        return true;
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
            tilPassword.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateRepeatPassword(String repeatPassword, String password) {
        if (repeatPassword.isEmpty()) {
            tilRepeatPass.setErrorEnabled(true);
            tilRepeatPass.setError("Field can't be empty.");
            return false;
        } else if (!repeatPassword.equals(password)) {
            tilRepeatPass.setErrorEnabled(true);
            tilRepeatPass.setError("Doesn't match with password field.");
            return false;
        } else {
            tilRepeatPass.setErrorEnabled(false);
            return true;
        }
    }

    private void confirmSignUp(String fullname, String email, String password, String repeatPassword, String phonenumber) {
        if (!validateField(tilFullname, fullname) |
                !validateField(tilPhone, phonenumber) |
                !validateEmail(email) |
                !validatePassword(password) |
                !validateRepeatPassword(repeatPassword, password)) return;

        tilFullname.setErrorEnabled(false);
        tilEmail.setErrorEnabled(false);
        tilPassword.setErrorEnabled(false);
        tilRepeatPass.setErrorEnabled(false);
        tilPhone.setErrorEnabled(false);

        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

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
                                                Toast.makeText(SignUpActivity.this, "Your account has been register successfully", Toast.LENGTH_LONG).show();
                                                Intent signInIntent = new Intent(SignUpActivity.this, SignInActivity.class);
                                                startActivity(signInIntent);
                                            } else {
                                                Toast.makeText(SignUpActivity.this, "Register failed. Try again.", Toast.LENGTH_LONG).show();
                                            }

                                            progressBar.setVisibility(View.GONE);
                                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        }
                                    });
                        } else {
                            Toast.makeText(SignUpActivity.this, "Register failed. Try again.", Toast.LENGTH_LONG).show();

                            progressBar.setVisibility(View.GONE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }
                    }
                });
    }
}