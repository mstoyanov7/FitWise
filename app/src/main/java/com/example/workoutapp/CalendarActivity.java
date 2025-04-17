package com.example.workoutapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private ImageButton btnAddWorkout;
    private BottomNavigationView bottomNavigationView;

    private final HashMap<LocalDate, List<CalendarWorkout>> workoutData = new HashMap<>();
    private LocalDate selectedDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidThreeTen.init(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_page);
        FullscreenUtil.hideSystemUI(this);

        initializeViews();
        setupCalendar();
        setupAddWorkoutButton();
        seedWorkouts();
        showWorkoutListFragment(selectedDate);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_calendar);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;
            int id = item.getItemId();

            if (id == R.id.nav_meals) {
                // intent = new Intent(this, MealsActivity.class);
            } else if (id == R.id.nav_workout) {
                intent = new Intent(CalendarActivity.this, Workouts.class);
            } else if (id == R.id.nav_home) {
                // intent = new Intent(this, HomeActivity.class);
            } else if (id == R.id.nav_profile) {
                intent = new Intent(CalendarActivity.this, Profile.class);
            } else if (id == R.id.nav_calendar) {
                return true;
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            }
            return false;
        });
    }

    private void initializeViews() {
        calendarView = findViewById(R.id.calendarView);
        btnAddWorkout = findViewById(R.id.btnAddWorkout);
        selectedDate = LocalDate.now();
    }

    private void setupCalendar() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
            showWorkoutListFragment(selectedDate);
        });
    }

    private void setupAddWorkoutButton() {
        btnAddWorkout.setOnClickListener(v -> showAddWorkoutDialog());
    }

    private void showWorkoutListFragment(LocalDate date) {
        CalendarWorkoutFragment fragment = CalendarWorkoutFragment.newInstance(date);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.calendarWorkoutFragment, fragment);
        ft.commit();
    }

    private void seedWorkouts() {
        LocalDate demoDate = LocalDate.of(2025, 4, 17);
        List<CalendarWorkout> demoList = new ArrayList<>();
        demoList.add(new CalendarWorkout("Strength Training", "05:30PM - 50 min", "Upcoming"));
        demoList.add(new CalendarWorkout("Yoga", "07:00PM - 30 min", "Upcoming"));
        workoutData.put(demoDate, demoList);
    }

    private void showAddWorkoutDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_workout_calendar, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        Objects.requireNonNull(dialog.getWindow())
                .setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        EditText etName     = dialogView.findViewById(R.id.etWorkoutName);
        MaterialButton btnDate = dialogView.findViewById(R.id.btnWorkoutDate);
        MaterialButton btnTime = dialogView.findViewById(R.id.btnWorkoutTime);
        TextView addFav        = dialogView.findViewById(R.id.btnAddFromFavorites);
        MaterialButton btnAdd   = dialogView.findViewById(R.id.btnAddWorkout);
        MaterialButton btnCancel= dialogView.findViewById(R.id.btnCancelWorkout);

        btnDate.setText(selectedDate.toString());
        btnDate.setOnClickListener(v -> showDatePicker(btnDate));
        btnTime.setOnClickListener(v -> showTimePicker(btnTime));
        addFav.setOnClickListener(v ->
                Toast.makeText(this, "Add from Favorites clicked", Toast.LENGTH_SHORT).show());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAdd.setOnClickListener(v -> addWorkout(etName, btnDate, btnTime, dialog));
    }

    private void showDatePicker(MaterialButton btnDate) {
        LocalDate now = LocalDate.now();
        new DatePickerDialog(this,
                (view, y, m, d) -> btnDate.setText(
                        LocalDate.of(y, m + 1, d).toString()),
                now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth()
        ).show();
    }

    private void showTimePicker(MaterialButton btnTime) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        new TimePickerDialog(this,
                (view, h, min) -> btnTime.setText(
                        String.format(Locale.getDefault(), "%02d:%02d", h, min)),
                c.get(java.util.Calendar.HOUR_OF_DAY),
                c.get(java.util.Calendar.MINUTE), true
        ).show();
    }

    private void addWorkout(EditText etName,
                            MaterialButton btnDate,
                            MaterialButton btnTime,
                            AlertDialog dialog) {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Workout name required", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate date = LocalDate.parse(btnDate.getText().toString());
        String time = btnTime.getText().toString() + " - 50 min";

        CalendarWorkout workout = new CalendarWorkout(name, time, "Upcoming");
        workoutData.computeIfAbsent(date, k -> new ArrayList<>()).add(workout);
        if (date.equals(selectedDate)) {
            showWorkoutListFragment(date);
        }
        Toast.makeText(this, "Workout added!", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    /**
     * Provide access to the workout data map for fragments.
     */
    public HashMap<LocalDate, List<CalendarWorkout>> getWorkoutData() {
        return workoutData;
    }

    /**
     * Simple in-memory model for a calendar workout.
     */
    public static class CalendarWorkout {
        public final String name;
        public final String time;
        public final String status;

        public CalendarWorkout(String name, String time, String status) {
            this.name   = name;
            this.time   = time;
            this.status = status;
        }
    }
}
