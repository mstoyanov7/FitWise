package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class SignInPage extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        // Initialize views
        TextInputLayout emailLayout = findViewById(R.id.emailLayout);
        TextInputLayout passwordLayout = findViewById(R.id.passwordLayout);
        emailEditText = emailLayout.getEditText();
        passwordEditText = passwordLayout.getEditText();
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);

        Button loginButton = findViewById(R.id.loginButton);
        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);
        Button googleSignInButton = findViewById(R.id.googleSignInButton);
        TextView registerText = findViewById(R.id.registerText);

        // Handle Login Button Click
        loginButton.setOnClickListener(v -> handleLogin());

        // Handle Forgot Password Click
        forgotPasswordText.setOnClickListener(v -> {
            Toast.makeText(SignInPage.this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show();
            // Implement Forgot Password Logic
        });

        // Handle Google Sign-In Click
        googleSignInButton.setOnClickListener(v -> {
            Toast.makeText(SignInPage.this, "Google Sign-In Clicked", Toast.LENGTH_SHORT).show();
            // Implement Google Sign-In Logic
        });

        // Navigate to Register Page
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(SignInPage.this, RegisterForm.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mock authentication logic (Replace with real authentication)
        if (email.equals("test@example.com") && password.equals("password123")) {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            // Navigate to Home or Dashboard Activity
            Intent intent = new Intent(SignInPage.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }
}
