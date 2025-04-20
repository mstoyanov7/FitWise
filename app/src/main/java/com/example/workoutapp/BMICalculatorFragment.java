package com.example.workoutapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class BMICalculatorFragment extends Fragment {

    private TextView bmiValue, bmiCategory, bmiFeedback;
    private View bmiScale;
    private ImageView bmiMarker;

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
        bmiMarker = view.findViewById(R.id.bmiMarker);

        // Get height and weight from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String weightString = prefs.getString("weight", null);
        String heightString = prefs.getString("height", null);

        double weight = -1;
        double height = -1;

        try {
            if (weightString != null) {
                weight = Double.parseDouble(weightString);
            }
            if (heightString != null) {
                height = Double.parseDouble(heightString);
            }
        } catch (NumberFormatException ignored) {}

        if (height <= 0 || weight <= 0) {
            bmiValue.setText("–");
            bmiCategory.setText("Missing data");
            bmiFeedback.setText("Please set your height and weight in profile.");
            return view;
        }

        calculateAndDisplayBMI(height, weight);
        return view;
    }

    private void calculateAndDisplayBMI(double height, double weight) {
        height = height / 100.0;

        double bmi = weight / (height * height);
        String category;
        String feedback;
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

        // Animate marker along custom scale
        bmiScale.post(() -> {
            int scaleHeight = bmiScale.getHeight();
            int markerHeight = bmiMarker.getHeight();
            float percentage = Math.min(1f, (float) bmi / 50f);
            int availableHeight = scaleHeight - bmiScale.getPaddingTop() - bmiScale.getPaddingBottom();
            int offsetY = (int) (availableHeight * (1 - percentage));
            bmiMarker.setTranslationY(bmiScale.getPaddingTop() + offsetY - (markerHeight / 2f));
        });
    }
}
