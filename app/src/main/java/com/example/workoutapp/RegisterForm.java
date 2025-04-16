package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

        // Init UI
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpButton = findViewById(R.id.sign_up_button);
        haveAccountText = findViewById(R.id.haveAccountText);
        googleButton = findViewById(R.id.google_button);

        // Set "Already have account?" text
        haveAccountText.setText(Html.fromHtml(getString(R.string.have_account), Html.FROM_HTML_MODE_LEGACY));
        haveAccountText.setOnClickListener(v -> startActivity(new Intent(this, SignInPage.class)));

        // Register Button
        signUpButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

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
                                                Toast.makeText(this, "Welcome, " + name, Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(this, WelcomeActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Google Sign-In placeholder (optional)
        googleButton.setOnClickListener(v ->
                Toast.makeText(this, "Google Sign-In not implemented", Toast.LENGTH_SHORT).show());
    }
}
