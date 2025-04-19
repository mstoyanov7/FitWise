package com.example.workoutapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;                // ← correct import
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterForm extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private TextInputEditText nameEditText, emailEditText, passwordEditText;
    private Button signUpButton;
    private TextView haveAccountText;
    private ImageButton googleButton;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_form);
        FullscreenUtil.hideSystemUI(this);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Init UI
        nameEditText     = findViewById(R.id.nameEditText);
        emailEditText    = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpButton     = findViewById(R.id.sign_up_button);
        haveAccountText  = findViewById(R.id.haveAccountText);
        googleButton     = findViewById(R.id.google_button);

        // “Already have account? …”
        haveAccountText.setText(Html.fromHtml(getString(R.string.have_account), Html.FROM_HTML_MODE_LEGACY));
        haveAccountText.setOnClickListener(v ->
                startActivity(new Intent(this, SignInPage.class))
        );

        // Email/password register
        signUpButton.setOnClickListener(v -> {
            String name     = nameEditText.getText().toString().trim();
            String email    = emailEditText.getText().toString().trim();
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
                                // set display name
                                UserProfileChangeRequest profileUpdates =
                                        new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name)
                                                .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(updTask -> {
                                            if (updTask.isSuccessful()) {
                                                Toast.makeText(this,
                                                        "Registered as " + name,
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                                startActivity(
                                                        new Intent(this, WelcomeActivity.class)
                                                );
                                                finish();
                                            } else {
                                                Toast.makeText(this,
                                                        "Profile update failed",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(this,
                                    "Registration Failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });

        // Google Sign-In
        googleButton.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> gsTask =
                    GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = gsTask.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this,
                        "Google sign in failed: " + e.getStatusCode(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                // specify <AuthResult> so onComplete(Task<AuthResult>) matches
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            AuthResult result = task.getResult();
                            boolean isNew = result.getAdditionalUserInfo().isNewUser();

                            if (user != null) {
                                Intent dest = isNew
                                        ? new Intent(RegisterForm.this, WelcomeActivity.class)
                                        : new Intent(RegisterForm.this, Workouts.class);

                                startActivity(dest);
                                finish();
                            }
                        } else {
                            Toast.makeText(RegisterForm.this,
                                    "Authentication Failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });
    }
}
