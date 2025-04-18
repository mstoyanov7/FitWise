package com.example.workoutapp;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Workouts extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private GridLayout exercisesGrid;

    Button btnCalisthenics, btnFitness, btnRunning, btnYoga;

    FrameLayout cardPullUps, cardDips, cardMuscleUps, cardPistolSquats;
    FrameLayout cardPullups1, cardPullups2, cardPullups3;

    EditText searchInput;
    ImageButton searchButton;
    TextView noResultsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workouts_page);
        FullscreenUtil.hideSystemUI(this);

        // current day
        TextView dateTextView = findViewById(R.id.dateTextView);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        dateTextView.setText(currentDate);


        noResultsText = findViewById(R.id.noResultsText);
        searchInput = findViewById(R.id.search_input);
        searchButton = findViewById(R.id.search_button);
        btnCalisthenics = findViewById(R.id.btn_calisthenics);
        btnFitness = findViewById(R.id.btn_fitness);
        btnRunning = findViewById(R.id.btn_running);
        btnYoga = findViewById(R.id.btn_yoga);

        // GridLayout and Cards
        exercisesGrid = findViewById(R.id.exercisesGridLayout);

        // Animation on filtering
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.APPEARING);
        layoutTransition.enableTransitionType(LayoutTransition.DISAPPEARING);
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);

        exercisesGrid.setLayoutTransition(layoutTransition);


        cardPullUps = findViewById(R.id.card_pull_ups);
        cardPullups1 = findViewById(R.id.card_pull_ups1);
        cardPullups2 = findViewById(R.id.card_pull_ups2);
        cardPullups3 = findViewById(R.id.card_pull_ups3);
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

        // Handle text searching
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBySearch(s.toString().toLowerCase());
            }
        });

        // Filter button listeners
        btnCalisthenics.setOnClickListener(v -> showCategory("calisthenics"));
        btnFitness.setOnClickListener(v -> showCategory("fitness"));
        btnRunning.setOnClickListener(v -> showCategory("running"));
        btnYoga.setOnClickListener(v -> showCategory("yoga"));

        // Show default category
        showCategory("calisthenics");

        // Setup bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_workout);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Intent intent = null;

                if (id == R.id.nav_meals) {
                    //intent = new Intent(CurrentActivity.this, MealsActivity.class);
                } else if (id == R.id.nav_workout) {
                    intent = new Intent(Workouts.this, Workouts.class);
                } else if (id == R.id.nav_home) {
                    //intent = new Intent(CurrentActivity.this, HomeActivity.class);
                } else if (id == R.id.nav_calendar) {
                    intent = new Intent(Workouts.this, CalendarActivity.class);
                } else if (id == R.id.nav_profile) {
                    intent = new Intent(Workouts.this, Profile.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    // Apply fade in to the incoming activity and fade out from the current one.
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    return true;
                }
                return false;
            }
        });
    }


        private void showCategory(String category) {
        exercisesGrid.removeAllViews();
        noResultsText.setVisibility(View.GONE);

        switch (category) {
            case "calisthenics":
                exercisesGrid.addView(cardPullUps);
                exercisesGrid.addView(cardPullups1);
                exercisesGrid.addView(cardPullups2);
                exercisesGrid.addView(cardPullups3);
                exercisesGrid.addView(cardDips);
                exercisesGrid.addView(cardMuscleUps);
                exercisesGrid.addView(cardPistolSquats);
                break;
            case "fitness":
                exercisesGrid.addView(cardPullups2);
                exercisesGrid.addView(cardPullups3);
                exercisesGrid.addView(cardDips);
                break;
            case "running":
            case "yoga":
                break;
        }

        // If no cards were added, show the "no results" text
        if (exercisesGrid.getChildCount() == 0) {
            noResultsText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Dynamically show only the cards that match the search query,
     * so GridLayout does not leave empty cells.
     */
    private void filterBySearch(String query) {
        exercisesGrid.removeAllViews();
        int found = 0;

        if ("pull ups".contains(query) || "pullups".contains(query)) {
            exercisesGrid.addView(cardPullUps);
            found++;
        }
        if ("dips".contains(query)) {
            exercisesGrid.addView(cardDips);
            found++;
        }
        if ("muscle ups".contains(query) || "muscleups".contains(query)) {
            exercisesGrid.addView(cardMuscleUps);
            found++;
        }
        if ("pistol squats".contains(query) || "pistols".contains(query)) {
            exercisesGrid.addView(cardPistolSquats);
            found++;
        }

        noResultsText.setVisibility(found == 0 ? View.VISIBLE : View.GONE);
    }
}
