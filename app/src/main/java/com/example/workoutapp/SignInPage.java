package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class SignInPage extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this,gso);

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


        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        // Navigate to Register Page
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(SignInPage.this, RegisterForm.class);
            startActivity(intent);
        });

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct!=null){
            navigateToWorkoutsPage();
        }
    }

    void signIn(){
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent,1000);
    }

    void navigateToWorkoutsPage(){
        finish();
        Intent intent = new Intent(SignInPage.this, Workouts.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                task.getResult(ApiException.class);
                navigateToWorkoutsPage();
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
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
