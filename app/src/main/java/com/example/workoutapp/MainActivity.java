package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Button openRegisterButton = findViewById(R.id.openRegisterButton);
        Button openLoginButton = findViewById(R.id.openLoginButton);

        openRegisterButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterForm.class);
            startActivity(intent);
        });

        openLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignInPage.class);
            startActivity(intent);
        });

        findViewById(R.id.btnScan).setOnClickListener(v -> {
            startActivity(new Intent(this, BarcodeScannerActivity.class));
        });
    }
}