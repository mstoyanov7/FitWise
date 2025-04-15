package com.example.workoutapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.LinearLayout.LayoutParams;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView tvWorkoutsCount;
    private ImageButton btnAddWorkout;
    private LinearLayout workoutsContainer;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_page);
        FullscreenUtil.hideSystemUI(this);

        calendarView = findViewById(R.id.calendarView);
        btnAddWorkout = findViewById(R.id.btnAddWorkout);
        tvWorkoutsCount = findViewById(R.id.tvWorkoutsCount);
        workoutsContainer = findViewById(R.id.workoutsContainer);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_calendar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Intent intent = null;

                if (id == R.id.nav_meals) {
                    // intent = new Intent(CalendarActivity.this, MealsActivity.class);
                } else if (id == R.id.nav_workout) {
                    intent = new Intent(CalendarActivity.this, Workouts.class);
                } else if (id == R.id.nav_home) {
                    // intent = new Intent(CalendarActivity.this, HomeActivity.class);
                } else if (id == R.id.nav_calendar) {
                    intent = new Intent(CalendarActivity.this, CalendarActivity.class);
                } else if (id == R.id.nav_profile) {
                    intent = new Intent(CalendarActivity.this, Profile.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    return true;
                }
                return false;
            }
        });

        btnAddWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CalendarActivity.this, "Add new workout button clicked", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to the Add Workout screen or open a dialog
            }
        });
    }

    /**
     * Loads workouts for the specified date.
     * For demonstration purposes, this creates a dummy workout card.
     *
     * @param year       Selected year.
     * @param month      Selected month (zero-indexed).
     * @param dayOfMonth Selected day of the month.
     */
    private void loadWorkoutsForDate(int year, int month, int dayOfMonth) {
        // Clear any existing workout views from the container
        workoutsContainer.removeAllViews();

        // For demonstration, always add a single dummy workout.
        // Replace this dummy content with your real data fetching (e.g., from a database or an API).
        String workoutTitle = "Strength Training";
        String workoutStatus = "Upcoming";
        String workoutTime = "05:30PM - 50 min";

        // Create a container for the workout card programmatically
        LinearLayout cardLayout = new LinearLayout(this);
        cardLayout.setOrientation(LinearLayout.VERTICAL);
        cardLayout.setPadding(16, 16, 16, 16);

        // Set layout parameters (adjust margins as needed)
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        cardLayout.setLayoutParams(params);

        // Optionally, set a background to mimic a card (you could also use a CardView via XML)
        cardLayout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        // Create and add a TextView for the workout title
        TextView tvTitle = new TextView(this);
        tvTitle.setText(workoutTitle);
        tvTitle.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        cardLayout.addView(tvTitle);

        // Create and add a TextView for the workout status
        TextView tvStatus = new TextView(this);
        tvStatus.setText(workoutStatus);
        tvStatus.setTextAppearance(this, android.R.style.TextAppearance_Small);
        cardLayout.addView(tvStatus);

        // Create and add a TextView for the workout time
        TextView tvTime = new TextView(this);
        tvTime.setText(workoutTime);
        tvTime.setTextAppearance(this, android.R.style.TextAppearance_Small);
        cardLayout.addView(tvTime);

        // Update the workouts count label if needed
        tvWorkoutsCount.setText("1 Workout");

        // Add the workout card to the container
        workoutsContainer.addView(cardLayout);
    }
}
