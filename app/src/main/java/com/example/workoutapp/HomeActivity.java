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

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private TextView dateTextView;
    private TextView selectedDateTextView;
    private TextView finishedWorkouts, caloriesBurnt, minutesSpent;
    private ImageView trainingPlanImage, menuImage;
    private BottomNavigationView bottomNavigationView;

    private LocalDate currentSelectedDate = LocalDate.now();

    private void setupWeekCalendar() {
        RecyclerView weekRv = findViewById(R.id.weekRecyclerView);
        weekRv.setLayoutManager(new GridLayoutManager(this, 7));
        weekRv.setHasFixedSize(true);

        List<LocalDate> week = DateUtils.currentWeek(currentSelectedDate);

        WeekAdapter adapter = new WeekAdapter(week, date -> {
            currentSelectedDate = date;
            String formatted = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()).format(date);
            dateTextView.setText(formatted);
        });

        weekRv.setAdapter(adapter);
        adapter.selectDate(currentSelectedDate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);


        TextView greetingText = findViewById(R.id.greetingText);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = prefs.getString("name", "");

        // Determine greeting based on time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String timeGreeting;

        if (hour >= 5 && hour < 12) {
            timeGreeting = "Добро утро";
        } else if (hour >= 12 && hour < 17) {
            timeGreeting = "Добър ден";
        }
        else {
            timeGreeting = "Добър вечер";
        }

        // Set dynamic greeting with name
        String greeting = timeGreeting + ",\n" + name;
        greetingText.setText(greeting);

        // Setup calendar
        setupWeekCalendar();

        // Initialize views
        dateTextView = findViewById(R.id.dateTextView);
        selectedDateTextView = findViewById(R.id.dateTextView); // Assuming this is where selected date goes
        finishedWorkouts = findViewById(R.id.finishedWorkouts);
        caloriesBurnt = findViewById(R.id.caloriesBurnt);
        minutesSpent = findViewById(R.id.minutesSpent);
        trainingPlanImage = findViewById(R.id.trainingPlanImage);
        menuImage = findViewById(R.id.menuImage);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set current date
        String currentDate = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()).format(LocalDate.now());
        dateTextView.setText(currentDate);

        // Set example activity stats
        finishedWorkouts.setText("3");
        caloriesBurnt.setText("2,354");
        minutesSpent.setText("134");

        // Bottom navigation logic
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

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
}
