package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterForm extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextInputEditText nameEditText, emailEditText, passwordEditText;
    private Button signUpButton;
    private TextView haveAccountText;
    private ImageButton googleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_form);
        FullscreenUtil.hideSystemUI(this);

        mAuth = FirebaseAuth.getInstance();

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

                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    if (user != null) {
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name)
                                                .build();

                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(updateTask -> {
                                                    if (updateTask.isSuccessful()) {
                                                        FirebaseUser updatedUser = FirebaseAuth.getInstance().getCurrentUser();

                                                        Toast.makeText(RegisterForm.this, "Welcome, " + updatedUser.getDisplayName(), Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(RegisterForm.this, SignInPage.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(RegisterForm.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(RegisterForm.this, "User is null", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(RegisterForm.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    Toast.makeText(RegisterForm.this, "All fields are required", Toast.LENGTH_SHORT).show();
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
