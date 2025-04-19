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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;

import java.util.*;

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

        FirebaseApp.initializeApp(this);

        initializeViews();
        setupCalendar();
        setupAddWorkoutButton();
        showWorkoutListFragment(selectedDate);
        loadWorkoutDataFromFirebase();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_calendar);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;
            int id = item.getItemId();

            if (id == R.id.nav_meals) {
                intent = new Intent(CalendarActivity.this, FoodDiaryActivity.class);
            } else if (id == R.id.nav_workout) {
                intent = new Intent(CalendarActivity.this, Workouts.class);
            } else if (id == R.id.nav_home) {
                intent = new Intent(this, HomeActivity.class);
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

    private void loadWorkoutDataFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("workouts")
                .document(user.getUid())
                .collection("entries")
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        String dateStr = (String) doc.get("date");
                        if (dateStr == null) continue;

                        LocalDate date = LocalDate.parse(dateStr);
                        String name = (String) doc.get("name");
                        String time = (String) doc.get("time");
                        String status = (String) doc.get("status");
                        List<String> exercises = (List<String>) doc.get("exercises");

                        CalendarWorkout workout = new CalendarWorkout(name, time, status, exercises);
                        workoutData.computeIfAbsent(date, k -> new ArrayList<>()).add(workout);
                    }

                    showWorkoutListFragment(selectedDate);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load workouts: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void showAddWorkoutDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_workout_calendar, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        EditText etName = dialogView.findViewById(R.id.etWorkoutName);
        MaterialButton btnDate = dialogView.findViewById(R.id.btnWorkoutDate);
        MaterialButton btnTime = dialogView.findViewById(R.id.btnWorkoutTime);
        TextView addFav = dialogView.findViewById(R.id.btnAddFromFavorites);
        MaterialButton btnAdd = dialogView.findViewById(R.id.btnAddWorkout);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelWorkout);
        RecyclerView exerciseRecycler = dialogView.findViewById(R.id.exerciseRecycler);

        List<String> exerciseData = new ArrayList<>();
        CalendarAddWorkoutsAdapter adapter = new CalendarAddWorkoutsAdapter(this, exerciseData);
        exerciseRecycler.setAdapter(adapter);
        exerciseRecycler.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder from, @NonNull RecyclerView.ViewHolder to) {
                adapter.moveItem(from.getAdapterPosition(), to.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {}
        });
        itemTouchHelper.attachToRecyclerView(exerciseRecycler);

        btnDate.setOnClickListener(v -> showDatePicker(btnDate));
        btnTime.setOnClickListener(v -> showTimePicker(btnTime));
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        addFav.setOnClickListener(v -> showFavoritesDialog(adapter));

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Workout name required", Toast.LENGTH_SHORT).show();
                return;
            }

            LocalDate date = LocalDate.parse(btnDate.getText().toString());
            String time = btnTime.getText().toString() + " - 50 min";
            List<String> finalList = new ArrayList<>(adapter.getExercises());

            CalendarWorkout workout = new CalendarWorkout(name, time, "Upcoming", finalList);
            workoutData.computeIfAbsent(date, k -> new ArrayList<>()).add(workout);
            if (date.equals(selectedDate)) {
                showWorkoutListFragment(date);
            }

            // Save to Firestore under workouts/{uid}/entries
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Map<String, Object> workoutMap = new HashMap<>();
                workoutMap.put("name", name);
                workoutMap.put("time", time);
                workoutMap.put("status", "Upcoming");
                workoutMap.put("exercises", finalList);
                workoutMap.put("date", date.toString());

                FirebaseFirestore.getInstance()
                        .collection("workouts")
                        .document(user.getUid())
                        .collection("entries")
                        .add(workoutMap)
                        .addOnSuccessListener(docRef -> Toast.makeText(this, "Workout saved to cloud", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to save workout: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            Toast.makeText(this, "Workout added!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }

    private void showFavoritesDialog(CalendarAddWorkoutsAdapter adapter) {
        String[] allExercises = {"Push-ups", "Deadlifts", "Pull-ups", "Burpees", "Lunges",
                "Shoulder Press", "Plank", "Mountain Climbers", "Russian Twists",
                "Bicep Curls", "Tricep Dips", "Squats", "Jumping Jacks"};

        boolean[] checkedItems = new boolean[allExercises.length];
        List<String> selected = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Favorite Exercises");
        builder.setMultiChoiceItems(allExercises, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) selected.add(allExercises[which]);
            else selected.remove(allExercises[which]);
        });

        builder.setPositiveButton("Add", (dialog, which) -> {
            adapter.getExercises().addAll(selected);
            adapter.notifyDataSetChanged();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDatePicker(MaterialButton btnDate) {
        LocalDate now = LocalDate.now();
        new DatePickerDialog(this,
                (view, y, m, d) -> btnDate.setText(LocalDate.of(y, m + 1, d).toString()),
                now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth()
        ).show();
    }

    private void showTimePicker(MaterialButton btnTime) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        new TimePickerDialog(this,
                (view, h, min) -> btnTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, min)),
                c.get(java.util.Calendar.HOUR_OF_DAY),
                c.get(java.util.Calendar.MINUTE), true
        ).show();
    }

    public HashMap<LocalDate, List<CalendarWorkout>> getWorkoutData() {
        return workoutData;
    }

    public static class CalendarWorkout {
        public final String name;
        public final String time;
        public String status;
        public final List<String> exerciseList;

        public CalendarWorkout(String name, String time, String status, List<String> exercises) {
            this.name = name;
            this.time = time;
            this.status = status;
            this.exerciseList = exercises;
        }
    }
}
