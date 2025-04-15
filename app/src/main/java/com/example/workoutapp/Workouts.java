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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class Workouts extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    Button btnCalisthenics, btnFitness, btnRunning, btnYoga;

    FrameLayout cardPullUps, cardDips, cardMuscleUps, cardPistolSquats;

    EditText searchInput;
    ImageButton searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workouts_page);
        FullscreenUtil.hideSystemUI(this);

        TextView noResultsText = findViewById(R.id.noResultsText);

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

                // Скриваме всички карти по подразбиране
                cardPullUps.setVisibility(View.GONE);
                cardDips.setVisibility(View.GONE);
                cardMuscleUps.setVisibility(View.GONE);
                cardPistolSquats.setVisibility(View.GONE);

                int found = 0;

                if ("pull ups".contains(query) || "pullups".contains(query)) {
                    cardPullUps.setVisibility(View.VISIBLE);
                    found++;
                }
                if ("dips".contains(query)) {
                    cardDips.setVisibility(View.VISIBLE);
                    found++;
                }
                if ("muscle ups".contains(query) || "muscleups".contains(query)) {
                    cardMuscleUps.setVisibility(View.VISIBLE);
                    found++;
                }
                if ("pistol squats".contains(query) || "pistols".contains(query)) {
                    cardPistolSquats.setVisibility(View.VISIBLE);
                    found++;
                }

                // Показваме/скриваме текста при нужда
                noResultsText.setVisibility(found == 0 ? View.VISIBLE : View.GONE);
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

        // Setup bottom navigation view
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Optionally set the default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment selectedFragment = null;
                if (id == R.id.nav_meals) {
                    // TODO: Load Meals screen or fragment
                    return true;
                } else if (id == R.id.nav_workout) {
                    // TODO: Load Workout screen or fragment
                    return true;
                } else if (id == R.id.nav_home) {
                    // TODO: Load Home screen or fragment
                    return true;
                } else if (id == R.id.nav_calendar) {
                    // TODO: Load Calendar screen or fragment
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(Workouts.this, Profile.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void showCategory(String category) {
        switch (category) {
            case "calisthenics":
                cardPullUps.setVisibility(View.VISIBLE);
                cardDips.setVisibility(View.VISIBLE);
                cardMuscleUps.setVisibility(View.VISIBLE);
                cardPistolSquats.setVisibility(View.VISIBLE);
                break;

            case "fitness":
                cardPullUps.setVisibility(View.GONE);
                cardDips.setVisibility(View.GONE);
                cardMuscleUps.setVisibility(View.GONE);
                cardPistolSquats.setVisibility(View.GONE);
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
