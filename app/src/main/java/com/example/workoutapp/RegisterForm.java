package com.example.workoutapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.*;
import com.google.firebase.auth.*;

public class RegisterForm extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;

    private EditText nameEditText, emailEditText, passwordEditText;
    private Button signUpButton;
    private TextView haveAccountText;
    private ImageButton googleButton;

    private GoogleSignInClient mGoogleSignInClient;

    private boolean passwordVisible = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullscreenUtil.hideSystemUI(this);
        setContentView(R.layout.register_form);


        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpButton = findViewById(R.id.sign_up_button);
        haveAccountText = findViewById(R.id.haveAccountText);
        googleButton = findViewById(R.id.google_button);

        passwordEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    passwordVisible = !passwordVisible;
                    int cursorPos = passwordEditText.getSelectionEnd();
                    passwordEditText.setInputType(
                            passwordVisible ?
                                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                    );
                    passwordEditText.setSelection(cursorPos);
                    return true;
                }
            }
            return false;
        });

        haveAccountText.setText(Html.fromHtml(getString(R.string.have_account), Html.FROM_HTML_MODE_LEGACY));
        haveAccountText.setOnClickListener(v ->
                startActivity(new Intent(this, SignInPage.class))
        );

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
                                UserProfileChangeRequest profileUpdates =
                                        new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name)
                                                .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(updTask -> {
                                            if (updTask.isSuccessful()) {
                                                Toast.makeText(this, "Registered as " + name, Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(this, WelcomeActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(this, "Profile update failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

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
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign-in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        AuthResult result = task.getResult();
                        boolean isNew = result.getAdditionalUserInfo().isNewUser();

                        Intent dest = isNew
                                ? new Intent(RegisterForm.this, WelcomeActivity.class)
                                : new Intent(RegisterForm.this, HomeActivity.class);

                        startActivity(dest);
                        finish();
                    } else {
                        Toast.makeText(this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
