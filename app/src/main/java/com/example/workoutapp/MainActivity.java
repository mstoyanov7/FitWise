package com.example.workoutapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        GoogleSignInAccount googleUser = GoogleSignIn.getLastSignedInAccount(this);
        SharedPreferences prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        boolean rememberMe = prefs.getBoolean("remember_me", false);

        if ((firebaseUser != null || googleUser != null) && rememberMe) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        FullscreenUtil.hideSystemUI(this);
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
    }
}
