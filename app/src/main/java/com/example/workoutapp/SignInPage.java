package com.example.workoutapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.*;
import com.google.firebase.auth.*;

public class SignInPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;

    private static final int RC_SIGN_IN = 1000;
    private GoogleSignInClient gsc;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        FullscreenUtil.hideSystemUI(this);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);

        emailEditText = findViewById(R.id.emailLayout);
        passwordEditText = findViewById(R.id.passwordLayout);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);

        // Eye icon toggle for password
        passwordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) return false;

            int end = 2; // index of drawableEnd
            if (passwordEditText.getCompoundDrawables()[end] == null) return false;

            int drawableWidth = passwordEditText.getCompoundDrawables()[end].getBounds().width();
            float touchX = event.getRawX();
            float fieldRight = passwordEditText.getRight();

            if (touchX >= (fieldRight - drawableWidth)) {
                int currentInputType = passwordEditText.getInputType();
                boolean isVisible = (currentInputType == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));

                passwordEditText.setInputType(isVisible
                        ? (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                        : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));

                passwordEditText.setSelection(passwordEditText.length());
                return true;
            }
            return false;
        });

        Button loginButton = findViewById(R.id.loginButton);
        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);
        TextView registerText = findViewById(R.id.registerText);
        ImageButton googleSignInButton = findViewById(R.id.googleSignInButton);

        loginButton.setOnClickListener(v -> handleLogin());

        forgotPasswordText.setOnClickListener(v ->
                startActivity(new Intent(SignInPage.this, ForgotPasswordActivity.class)));

        registerText.setOnClickListener(v ->
                startActivity(new Intent(SignInPage.this, RegisterForm.class)));

        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        // Auto login if user is already remembered
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        GoogleSignInAccount googleUser = GoogleSignIn.getLastSignedInAccount(this);
        SharedPreferences prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        boolean rememberMe = prefs.getBoolean("remember_me", false);

        if ((firebaseUser != null || googleUser != null) && rememberMe) {
            navigateToHomePage();
        }
    }

    private void handleLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Save remember me preference
                        if (rememberMeCheckBox.isChecked()) {
                            SharedPreferences prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
                            prefs.edit().putBoolean("remember_me", true).apply();
                        }

                        Toast.makeText(SignInPage.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        navigateToHomePage();
                    } else {
                        Toast.makeText(SignInPage.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Save remember me preference
                        SharedPreferences prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
                        prefs.edit().putBoolean("remember_me", true).apply();

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();

                        Intent intent = new Intent(SignInPage.this, isNew ? WelcomeActivity.class : Workouts.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SignInPage.this, "Firebase login failed: " + task.getException(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Google sign in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToHomePage() {
        Intent intent = new Intent(SignInPage.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
