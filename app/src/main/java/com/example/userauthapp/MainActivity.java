package com.example.userauthapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("remember", "false");
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStarted = findViewById(R.id.btn_started);
        btnStarted.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_started) {
            Intent signInIntent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(signInIntent);

            finish();
        }
    }
}