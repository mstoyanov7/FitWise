package com.example.workoutapp;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.textfield.TextInputLayout;

public class RegisterForm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Root layout
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(30, 30, 30, 30);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // Load custom font
        Typeface customFont = ResourcesCompat.getFont(this, R.font.robotobold);

        // Avatar image
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.avatar);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(200));
        imageView.setPadding(30, 30, 30, 30);
        imageView.setLayoutParams(imageParams);
        rootLayout.addView(imageView);

        // Name input
        rootLayout.addView(createTextInputLayout(R.string.name, customFont, "text"));

        // Email input
        rootLayout.addView(createTextInputLayout(R.string.email, customFont, "text"));

        // Password input
        rootLayout.addView(createTextInputLayout(R.string.password, customFont, "textPassword"));

        // Signup button
        Button signupBtn = new Button(this);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(75));
        btnParams.topMargin = dpToPx(15);
        signupBtn.setLayoutParams(btnParams);
        signupBtn.setText(R.string.sign_up);
        signupBtn.setTextSize(22);
        signupBtn.setTextColor(getResources().getColor(android.R.color.white));
        signupBtn.setOnClickListener(v -> btn_Signup_form(v));
        rootLayout.addView(signupBtn);

        setContentView(rootLayout);
    }

    private TextInputLayout createTextInputLayout(int hintResId, Typeface font, String inputType) {
        TextInputLayout inputLayout = new TextInputLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dpToPx(5);
        inputLayout.setLayoutParams(params);

        EditText editText = new EditText(this);
        editText.setHint(hintResId);
        editText.setTextSize(25);
        editText.setTypeface(font);
        editText.setEms(10);

        if ("textPassword".equals(inputType)) {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        }

        inputLayout.addView(editText);
        return inputLayout;
    }

    private int dpToPx(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    // Replace this with your actual signup handler
    public void btn_Signup_form(View view) {
        // Handle signup logic
    }
}
