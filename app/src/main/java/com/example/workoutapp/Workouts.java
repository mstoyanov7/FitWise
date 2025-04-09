package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Workouts extends AppCompatActivity {
    Button btnCalisthenics, btnFitness, btnRunning, btnYoga;

    FrameLayout cardPullUps, cardDips, cardMuscleUps, cardPistolSquats;

    EditText searchInput;
    ImageButton searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workouts_page);

        // current day
        TextView dateTextView = findViewById(R.id.dateTextView);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        dateTextView.setText(currentDate);

        searchInput = findViewById(R.id.search_input);
        searchButton = findViewById(R.id.search_button);

        btnCalisthenics = findViewById(R.id.btn_calisthenics);
        btnFitness = findViewById(R.id.btn_fitness);
        btnRunning = findViewById(R.id.btn_running);
        btnYoga = findViewById(R.id.btn_yoga);

        cardPullUps = findViewById(R.id.card_pull_ups);
        cardDips = findViewById(R.id.card_dips);
        cardMuscleUps = findViewById(R.id.card_muscle_ups);
        cardPistolSquats = findViewById(R.id.pistol_squats);

        searchButton.setOnClickListener(v -> {
            if (searchInput.getVisibility() == View.GONE) {
                searchInput.setVisibility(View.VISIBLE);
                searchInput.requestFocus();
            } else {
                searchInput.setVisibility(View.GONE);
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();

                btnCalisthenics.setVisibility(query.contains("cal") ? View.VISIBLE : View.GONE);
                btnFitness.setVisibility(query.contains("fit") ? View.VISIBLE : View.GONE);
                btnRunning.setVisibility(query.contains("run") ? View.VISIBLE : View.GONE);
                btnYoga.setVisibility(query.contains("yo") ? View.VISIBLE : View.GONE);
            }
        });

        btnCalisthenics.setOnClickListener(v -> showCategory("calisthenics"));
        btnFitness.setOnClickListener(v -> showCategory("fitness"));
        btnRunning.setOnClickListener(v -> showCategory("running"));
        btnYoga.setOnClickListener(v -> showCategory("yoga"));

        // === Навигация към страници с упражнения ===
        // cardPullUps.setOnClickListener(v -> startActivity(new Intent(this, PullUpsActivity.class)));
        // cardDips.setOnClickListener(v -> startActivity(new Intent(this, DipsActivity.class)));
        // cardMuscleUps.setOnClickListener(v -> startActivity(new Intent(this, MuscleUpsActivity.class)));
        // cardPistolSquats.setOnClickListener(v -> startActivity(new Intent(this, PistolSquatsActivity.class)));

        // Показване на първа категория по подразбиране
        showCategory("calisthenics");
    }

    private void showCategory(String category) {
        switch (category) {
            case "calisthenics":
                cardPullUps.setVisibility(View.VISIBLE);
                cardDips.setVisibility(View.VISIBLE);
                cardMuscleUps.setVisibility(View.GONE);
                cardPistolSquats.setVisibility(View.GONE);
                break;

            case "fitness":
                cardPullUps.setVisibility(View.GONE);
                cardDips.setVisibility(View.GONE);
                cardMuscleUps.setVisibility(View.VISIBLE);
                cardPistolSquats.setVisibility(View.VISIBLE);
                break;

            case "running":
            case "yoga":
                cardPullUps.setVisibility(View.GONE);
                cardDips.setVisibility(View.GONE);
                cardMuscleUps.setVisibility(View.GONE);
                cardPistolSquats.setVisibility(View.GONE);
                break;
        }
    }
}
