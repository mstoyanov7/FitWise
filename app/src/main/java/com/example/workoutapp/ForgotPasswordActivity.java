package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText resetEmailEditText;
    private Button submitButton;
    private TextView backToLogin;
    private ImageButton googleSignInButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullscreenUtil.hideSystemUI(this);
        setContentView(R.layout.forgotten_password_page);

        mAuth = FirebaseAuth.getInstance();

        resetEmailEditText = findViewById(R.id.resetEmailEditText);
        submitButton = findViewById(R.id.submitButton);
        backToLogin = findViewById(R.id.back_to_login);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        // Send reset email
        submitButton.setOnClickListener(v -> {
            String email = resetEmailEditText.getText().toString().trim();

            if (email.isEmpty()) {
                resetEmailEditText.setError("Email is required");
                resetEmailEditText.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                resetEmailEditText.setError("Enter a valid email");
                resetEmailEditText.requestFocus();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, SignInPage.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Go back to login
        backToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, SignInPage.class));
            finish();
        });

        // Optional: Google sign-in shortcut
        googleSignInButton.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterForm.class)); // or a dedicated Google login activity
            finish();
        });
    }
}
