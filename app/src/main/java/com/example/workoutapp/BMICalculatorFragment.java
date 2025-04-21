package com.example.workoutapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class BMICalculatorFragment extends Fragment {

    private TextView bmiValue, bmiCategory, bmiFeedback;
    private View bmiScale;
    private View bmiMarkerLine;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bmi_calculator, container, false);
        bmiValue = view.findViewById(R.id.bmiValue);
        bmiCategory = view.findViewById(R.id.bmiCategory);
        bmiFeedback = view.findViewById(R.id.bmiFeedback);
        bmiScale = view.findViewById(R.id.bmiScale);
        bmiMarkerLine = view.findViewById(R.id.bmiMarkerLine);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Avoid double-calling during fragment recreation if not visible
        if (getUserVisibleHint()) {
            fetchUserHeightAndWeight();
        } else {
            // For good measure, fallback if hint is unreliable
            fetchUserHeightAndWeight();
        }
    }

    private void fetchUserHeightAndWeight() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String heightStr = doc.getString("height");
                    String weightStr = doc.getString("weight");

                    double height = parseDoubleSafe(heightStr);
                    double weight = parseDoubleSafe(weightStr);

                    if (height <= 0 || weight <= 0) {
                        bmiValue.setText("–");
                        bmiCategory.setText("Missing data");
                        bmiCategory.setTextColor(Color.GRAY);
                        bmiFeedback.setText("Please set your height and weight in profile.");
                    } else {
                        calculateAndDisplayBMI(height, weight);
                    }
                });
    }

    private double parseDoubleSafe(String input) {
        try {
            return Double.parseDouble(input);
        } catch (Exception e) {
            return -1;
        }
    }

    private void calculateAndDisplayBMI(double height, double weight) {
        height = height / 100.0;
        double bmi = weight / (height * height);

        String category, feedback;
        int color;

        if (bmi < 18.5) {
            category = "Underweight";
            feedback = "Consider gaining some weight.";
            color = Color.parseColor("#03A9F4");
        } else if (bmi < 25) {
            category = "Normal";
            feedback = "You're at a healthy weight!";
            color = Color.parseColor("#00C37A");
        } else if (bmi < 30) {
            category = "Overweight";
            feedback = "Try to stay more active.";
            color = Color.parseColor("#FFC107");
        } else if (bmi < 40) {
            category = "Obese";
            feedback = "You should consider getting guidance.";
            color = Color.parseColor("#FF5722");
        } else {
            category = "Morbidly Obese";
            feedback = "Please consult a health professional.";
            color = Color.parseColor("#D32F2F");
        }

        bmiValue.setText(String.format(Locale.getDefault(), "%.1f", bmi));
        bmiCategory.setText(category);
        bmiCategory.setTextColor(color);
        bmiFeedback.setText(feedback);

        // Animate marker line position on the scale
        bmiScale.post(() -> {
            int scaleHeight = bmiScale.getHeight();
            int markerHeight = bmiMarkerLine.getHeight();

            float minBMI = 10f;
            float maxBMI = 45f;
            float normalized = Math.min(1f, Math.max(0f, (float)(bmi - minBMI) / (maxBMI - minBMI)));

            int availableHeight = scaleHeight - bmiScale.getPaddingTop() - bmiScale.getPaddingBottom();
            float offsetY = availableHeight * (1 - normalized);

            bmiMarkerLine.setTranslationY(bmiScale.getPaddingTop() + offsetY - (markerHeight / 2f));
        });
    }
}
