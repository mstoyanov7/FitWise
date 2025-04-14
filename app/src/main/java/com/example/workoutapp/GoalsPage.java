package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class GoalsPage extends AppCompatActivity {

    // Basic data model
    class Goal {
        String title, date;
        boolean isCompleted;

        Goal(String title, String date, boolean isCompleted) {
            this.title = title;
            this.date = date;
            this.isCompleted = isCompleted;
        }

        @Override
        public String toString() {
            return title; // For filtering
        }
    }

    private BottomNavigationView bottomNavigationView;
    private ListView lvGoals;
    private EditText etSearchGoal;
    private List<Goal> goalList = new ArrayList<>();
    private List<Goal> filteredList = new ArrayList<>();
    private ArrayAdapter<Goal> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goals_page);

        // Initialize views
        lvGoals = findViewById(R.id.lvGoals);
        etSearchGoal = findViewById(R.id.etSearchGoal);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Sample data
        goalList.add(new Goal("Do 500 Pull-Ups", "01/06/2023", true));
        goalList.add(new Goal("Drink 100l of Water", "01/06/2023", false));
        goalList.add(new Goal("Run 10km", "01/06/2023", false));
        goalList.add(new Goal("Meditate 10 mins daily", "01/06/2023", false));

        filteredList.addAll(goalList);

        // Inline adapter with custom getView
        adapter = new ArrayAdapter<Goal>(this, R.layout.goal_item, R.id.tvGoalTitle, filteredList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Goal goal = getItem(position);
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.goal_item, parent, false);
                }

                TextView tvTitle = convertView.findViewById(R.id.tvGoalTitle);
                TextView tvDate = convertView.findViewById(R.id.tvGoalDate);
                Button btnAction = convertView.findViewById(R.id.btnGoalAction);

                tvTitle.setText(goal.title);
                tvDate.setText(goal.date);
                btnAction.setText(goal.isCompleted ? "Undo" : "Complete");

                btnAction.setOnClickListener(v -> {
                    goal.isCompleted = !goal.isCompleted;
                    notifyDataSetChanged();
                });

                return convertView;
            }
        };

        lvGoals.setAdapter(adapter);

        // Filter logic
        etSearchGoal.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filteredList.clear();
                String query = s.toString().toLowerCase();
                for (Goal g : goalList) {
                    if (g.title.toLowerCase().contains(query)) {
                        filteredList.add(g);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment selectedFragment = null;
                if (id == R.id.nav_meals) {
                    return true;
                } else if (id == R.id.nav_workout) {
                    startActivity(new Intent(GoalsPage.this, Workouts.class));
                    return true;
                } else if (id == R.id.nav_home) {
                    return true;
                } else if (id == R.id.nav_calendar) {
                    return true;
                } else if (id == R.id.nav_profile) {
                    return true;
                }
                return false;
            }
        });
    }
}
