package com.example.userauthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {
    private ProgressBar progressBar;
    private TextInputLayout tilEmail, tilPassword;
    private GoogleSignInClient mGoogleSignInClient;
    private Toast exitToast;
    private long backPressedTime;
    private final static int RC_SIGN_IN = 123;
    private FirebaseAuth mFirebaseAuth;

//    @Override
//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
//
//        if (currentUser != null) {
//            Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
//            startActivity(homeIntent);
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // initialize firebase, progress bar, email, and password
        mFirebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progress_bar);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);

        // create request for google sign in method
        createRequest();

        // initialize text view sign up
        TextView tvSignUp = findViewById(R.id.tv_sign_up);
        tvSignUp.setOnClickListener(this);

        // initialize text view forgot password
        TextView tvForgotPass = findViewById(R.id.tv_forgot_password);
        tvForgotPass.setOnClickListener(this);

        // initialize sign in button
        Button btnSignIn = findViewById(R.id.btn_sign_in);
        btnSignIn.setOnClickListener(this);

        // initialize sign in google button
        Button btnSignInGoogle = findViewById(R.id.btn_sign_in_google);
        btnSignInGoogle.setOnClickListener(this);

        // initialize sign in OTP phone number
        Button btnSignInPhone = findViewById(R.id.btn_sign_in_phone_number);
        btnSignInPhone.setOnClickListener(this);

        // 'remember me' fuctionality
        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);

        String checkBox = preferences.getString("remember", "");

        if (checkBox.equals("true")) {
            Intent homeIntent = new Intent(SignInActivity.this, HomeActivity.class);
            startActivity(homeIntent);
        }

        CheckBox rememberMe = findViewById(R.id.remember_me);

        rememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);

                if (compoundButton.isChecked()) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "true");
                    editor.apply();
                } else {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
                }
            }
        });
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

        } else if (view.getId() == R.id.btn_sign_in_phone_number) {

            Toast.makeText(this, "This feature is under development.", Toast.LENGTH_SHORT).show();

        } else if (view.getId() == R.id.btn_sign_in_google) signIn();
    }

    @Override
    public void onBackPressed() {
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

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

        tilEmail.setErrorEnabled(false);
        tilPassword.setErrorEnabled(false);

        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent homeIntent = new Intent(SignInActivity.this, HomeActivity.class);
                            startActivity(homeIntent);
                        } else {
                            Toast.makeText(SignInActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                        }

                        progressBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                });
    }

    private void createRequest() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                assert account != null;
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();

                            String fullname = currentUser != null ? currentUser.getDisplayName() : null;
                            String email = currentUser != null ? currentUser.getEmail() : null;
                            String phonenumber = currentUser != null ? currentUser.getPhoneNumber() : null;

                            User user = new User(fullname, email, phonenumber);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(SignInActivity.this, "Register failed. Try again.", Toast.LENGTH_SHORT).show();
                                            }

                                            progressBar.setVisibility(View.GONE);
                                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        }
                                    });

                            Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
                            startActivity(homeIntent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignInActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();

                            progressBar.setVisibility(View.GONE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }
                    }
                });
    }
}