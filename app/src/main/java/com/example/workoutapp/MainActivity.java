package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Button signInButton = findViewById(R.id.signInButton);
        TextView signUpText = findViewById(R.id.signUpText);

        signInButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignInPage.class);
            startActivity(intent);
        });

        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterForm.class);
            startActivity(intent);
        });

        findViewById(R.id.btnScan).setOnClickListener(v -> {
            startActivity(new Intent(this, BarcodeScannerActivity.class));
        });
    }
}