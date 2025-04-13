package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class RegisterForm extends AppCompatActivity {

    private TextInputEditText nameEditText, emailEditText, passwordEditText;
    private Button signUpButton;
    private TextView haveAccountText;
    private ImageButton googleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_form);

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpButton = findViewById(R.id.sign_up_button);
        haveAccountText = findViewById(R.id.haveAccountText);
        googleButton = findViewById(R.id.google_button);

        // Set styled text for "Already have an account? Sign in"
        if (haveAccountText != null) {
            haveAccountText.setText(Html.fromHtml(getString(R.string.have_account), Html.FROM_HTML_MODE_LEGACY));
            haveAccountText.setOnClickListener(v -> {
                startActivity(new Intent(RegisterForm.this, SignInPage.class));
            });
        }

        // Handle Sign Up button click
        if (signUpButton != null) {
            signUpButton.setOnClickListener(v -> {
                String name = nameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // You can validate here
                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    // Submit or proceed to next screen
                    // e.g. startActivity(new Intent(this, HomeActivity.class));
                } else {
                    // Show error or Toast
                }
            });
        }

        // Handle Google button click
        googleButton.setOnClickListener(v -> {
            // TODO: Add Google sign-in logic or just simulate for now
            // Example:
            // Toast.makeText(this, "Google Sign In Clicked", Toast.LENGTH_SHORT).show();
        });
    }
}
