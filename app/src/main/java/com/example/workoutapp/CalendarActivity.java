package com.example.workoutapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CalendarActivity extends AppCompatActivity {
    private CalendarView calendarView;
    private TextView tvWorkoutsCount;
    private ImageButton btnAddWorkout;
    private RecyclerView rvWorkouts;
    private CalendarAdapter adapter;
    private BottomNavigationView bottomNavigationView;

    private HashMap<String, List<CalendarWorkout>> workoutData = new HashMap<>();
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_page);
        FullscreenUtil.hideSystemUI(this);

        calendarView = findViewById(R.id.calendarView);
        btnAddWorkout = findViewById(R.id.btnAddWorkout);
        tvWorkoutsCount = findViewById(R.id.tvWorkoutsCount);
        rvWorkouts = findViewById(R.id.rvWorkouts);

        rvWorkouts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CalendarAdapter(new ArrayList<>());
        rvWorkouts.setAdapter(adapter);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_calendar);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;
            int id = item.getItemId();

            if (id == R.id.nav_meals) {
                // intent = new Intent(...);
            } else if (id == R.id.nav_workout) {
                intent = new Intent(CalendarActivity.this, Workouts.class);
            } else if (id == R.id.nav_home) {
                // intent = new Intent(...);
            } else if (id == R.id.nav_profile) {
                intent = new Intent(CalendarActivity.this, Profile.class);
            } else if (id == R.id.nav_calendar) {
                return true; // Don't restart the same activity
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            }
            return false;
        });

        Calendar today = Calendar.getInstance();
        selectedDate = formatDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        seedWorkouts();
        updateWorkoutsForDate(selectedDate);

        calendarView.setOnDateChangeListener((view, year, month, day) -> {
            selectedDate = formatDate(year, month, day);
            updateWorkoutsForDate(selectedDate);
        });

        btnAddWorkout.setOnClickListener(v -> showAddWorkoutDialog());
    }

    private void updateWorkoutsForDate(String date) {
        if (adapter == null || rvWorkouts == null || workoutData == null || tvWorkoutsCount == null) {
            Toast.makeText(this, "UI not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        List<CalendarWorkout> workouts = workoutData.getOrDefault(date, new ArrayList<>());

        adapter.updateWorkouts(workouts);

        int count = workouts.size();
        if (count == 0) {
            tvWorkoutsCount.setText("No Workouts");
        } else {
            tvWorkoutsCount.setText(count + (count > 1 ? " Workouts" : " Workout"));
        }
    }

    private String formatDate(int year, int month, int day) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
    }

    private void seedWorkouts() {
        String demoDate = formatDate(2025, 3, 17); // April 17, 2025
        List<CalendarWorkout> list = new ArrayList<>();
        list.add(new CalendarWorkout("Strength Training", "05:30PM - 50 min", "Upcoming"));
        list.add(new CalendarWorkout("Yoga", "07:00PM - 30 min", "Upcoming"));
        workoutData.put(demoDate, list);
    }

    private void showAddWorkoutDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_workout_calendar, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        EditText etName = dialogView.findViewById(R.id.etWorkoutName);
        MaterialButton btnDate = dialogView.findViewById(R.id.btnWorkoutDate);
        MaterialButton btnTime = dialogView.findViewById(R.id.btnWorkoutTime);
        TextView addFav = dialogView.findViewById(R.id.btnAddFromFavorites);
        MaterialButton btnAdd = dialogView.findViewById(R.id.btnAddWorkout);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelWorkout);

        btnDate.setText(selectedDate);
        addFav.setOnClickListener(v -> Toast.makeText(this, "Add from Favorites clicked", Toast.LENGTH_SHORT).show());

        btnDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, y, m, d) -> btnDate.setText(formatDate(y, m, d)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        btnTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this,
                    (view, h, min) -> btnTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, min)),
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true
            ).show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String date = btnDate.getText().toString();
            String time = btnTime.getText().toString() + " - 50 min";
            if (name.isEmpty()) {
                Toast.makeText(this, "Workout name required", Toast.LENGTH_SHORT).show();
                return;
            }
            CalendarWorkout w = new CalendarWorkout(name, time, "Upcoming");
            workoutData.computeIfAbsent(date, k -> new ArrayList<>()).add(w);
            if (date.equals(selectedDate)) updateWorkoutsForDate(date);
            Toast.makeText(this, "Workout added!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }

    // In-memory workout model for calendar
    public static class CalendarWorkout {
        public final String name, time, status;
        public CalendarWorkout(String n, String t, String s) {
            name = n;
            time = t;
            status = s;
        }
    }
}
