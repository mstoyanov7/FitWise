package com.example.workoutapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TextView dateTextView;
    private TextView selectedDateTextView;
    private TextView finishedWorkouts, caloriesConsumed;
    private ImageView trainingPlanImage, menuImage;
    private BottomNavigationView bottomNavigationView;
    private LocalDate currentSelectedDate;
    private TextView greetingText;

    private ListenerRegistration userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        AndroidThreeTen.init(this);
        currentSelectedDate = LocalDate.now();

        // Initialize views
        greetingText = findViewById(R.id.greetingText);
        dateTextView = findViewById(R.id.dateTextView);
        selectedDateTextView = findViewById(R.id.dateTextView);
        finishedWorkouts = findViewById(R.id.finishedWorkouts);
        caloriesConsumed = findViewById(R.id.caloriesConsumed);
        trainingPlanImage = findViewById(R.id.trainingPlanImage);
        menuImage = findViewById(R.id.menuImage);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        FullscreenUtil.hideSystemUI(this);

        String currentDate = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()).format(LocalDate.now());
        dateTextView.setText(currentDate);

        setupWeekCalendar();
        loadCaloriesForSelectedDate();
        loadCompletedWorkoutsForDate(currentSelectedDate);

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String timeGreeting;
        if (hour >= 5 && hour < 12) {
            timeGreeting = "Добро утро";
        } else if (hour >= 12 && hour < 17) {
            timeGreeting = "Добър ден";
        } else {
            timeGreeting = "Добър вечер";
        }

        // Firestore listener for live updates
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(uid);

            userListener = userRef.addSnapshotListener((snapshot, error) -> {
                if (error != null || snapshot == null || !snapshot.exists()) return;

                // Update greeting with name
                String name = snapshot.getString("name");
                if (name != null && !name.isEmpty()) {
                    greetingText.setText(timeGreeting + ",\n" + name);

                    // Save name to SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    prefs.edit().putString("name", name).apply();
                }

                Long workouts = snapshot.getLong("finishedWorkouts");
                Long calories = snapshot.getLong("caloriesBurnt");

                if (workouts != null) finishedWorkouts.setText(String.valueOf(workouts));
                if (calories != null) caloriesConsumed.setText(String.format(Locale.getDefault(), "%,d", calories));
            });
        }

        // Bottom navigation setup
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;
            int id = item.getItemId();

            if (id == R.id.nav_meals) {
                intent = new Intent(this, FoodDiaryActivity.class);
            } else if (id == R.id.nav_workout) {
                intent = new Intent(this, Workouts.class);
            } else if (id == R.id.nav_home) {
                intent = new Intent(this, HomeActivity.class);
            } else if (id == R.id.nav_calendar) {
                intent = new Intent(this, CalendarActivity.class);
            } else if (id == R.id.nav_profile) {
                intent = new Intent(this, Profile.class);
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            }
            return false;
        });
    }

    private void loadCompletedWorkoutsForDate(LocalDate date) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        String dateStr = date.toString(); // yyyy-MM-dd

        FirebaseFirestore.getInstance()
                .collection("workouts")
                .document(uid)
                .collection("entries")
                .document("completedWorkouts")
                .collection("byDate")
                .document(dateStr)
                .get()
                .addOnSuccessListener(doc -> {
                    Long count = doc.getLong("count");
                    finishedWorkouts.setText(String.valueOf(count != null ? count : 0));
                })
                .addOnFailureListener(e -> finishedWorkouts.setText("0"));
    }

    private void setupWeekCalendar() {
        RecyclerView weekRv = findViewById(R.id.weekRecyclerView);
        weekRv.setLayoutManager(new GridLayoutManager(this, 7));
        weekRv.setHasFixedSize(true);

        List<LocalDate> week = DateUtils.currentWeek(currentSelectedDate);
        WeekAdapter adapter = new WeekAdapter(week, date -> {
            currentSelectedDate = date;
            String formatted = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()).format(date);
            dateTextView.setText(formatted);
            loadCaloriesForSelectedDate();
        });

        weekRv.setAdapter(adapter);
        adapter.selectDate(currentSelectedDate);
    }

    private void loadCaloriesForSelectedDate() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || currentSelectedDate == null) return;

        String selectedDate = currentSelectedDate.toString(); // Format: yyyy-MM-dd

        FirebaseFirestore.getInstance()
                .collection("foodDiary")
                .document(user.getUid())
                .collection("entries")
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnSuccessListener(snapshot -> {
                    int totalCals = 0;
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Number ncal = doc.getDouble("calories") != null
                                ? doc.getDouble("calories")
                                : doc.getLong("calories");
                        if (ncal != null) totalCals += ncal.intValue();
                    }
                    caloriesConsumed.setText(String.format(Locale.getDefault(), "%,d", totalCals));
                })
                .addOnFailureListener(e ->
                        caloriesConsumed.setText("N/A")
                );
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (userListener != null) {
            userListener.remove();
        }
    }
}
