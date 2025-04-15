package com.example.workoutapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.ColorStateList;
import android.graphics.Color;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
        FullscreenUtil.hideSystemUI(this);

        // Initialize views
        lvGoals = findViewById(R.id.lvGoals);
        etSearchGoal = findViewById(R.id.etSearchGoal);

        lvGoals.setDivider(null);
        // Sample data
        goalList.add(new Goal("Do 500 Pull-Ups", "01/06/2026", true));
        goalList.add(new Goal("Drink 100l of Water", "01/06/2024", false));
        goalList.add(new Goal("Run 10km", "01/06/2023", false));
        goalList.add(new Goal("Meditate 10 mins daily", "01/06/2023", false));
        goalList.add(new Goal("Do 500 Pull-Ups", "01/06/2023", true));
        goalList.add(new Goal("Drink 100l of Water", "01/06/2023", false));
        goalList.add(new Goal("Run 10km", "01/06/2023", false));
        goalList.add(new Goal("Meditate 10 mins daily", "01/06/2023", false));
        goalList.add(new Goal("Do 500 Pull-Ups", "01/06/2023", true));
        goalList.add(new Goal("Drink 100l of Water", "01/06/2025", false));
        goalList.add(new Goal("Run 10km", "01/06/2023", false));
        goalList.add(new Goal("Meditate 10 mins daily", "01/06/2023", false));
        filteredList.addAll(goalList);

        adapter = new ArrayAdapter<Goal>(this, R.layout.goal_item, R.id.tvGoalTitle, filteredList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Goal goal = getItem(position);
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.goal_item, parent, false);
                }
                TextView tvTitle = convertView.findViewById(R.id.tvGoalTitle);
                TextView tvDate = convertView.findViewById(R.id.tvGoalDate);
                MaterialButton btnAction = convertView.findViewById(R.id.btnGoalAction);
                tvTitle.setText(goal.title);
                tvDate.setText(goal.date);

                if (goal.isCompleted) {

                    btnAction.setText("Undo");
                    btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F4F4F5")));
                    btnAction.setTextColor(Color.parseColor("#6B7280"));
                } else {
                    btnAction.setText("Complete");
                    btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0BA284")));
                    btnAction.setTextColor(Color.parseColor("#FFFFFF"));
                }

                btnAction.setOnClickListener(v -> {
                    goal.isCompleted = !goal.isCompleted;
                    notifyDataSetChanged();
                });

                return convertView;
            }
        };

        lvGoals.setAdapter(adapter);
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

        ImageButton buttonBack = findViewById(R.id.btnBack);
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(GoalsPage.this, Profile.class);
            intent.putExtra("selectedTab", "goals");
            startActivity(intent);
            finish();
        });

        ImageButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> showAddGoalDialog());

    }

    private void showAddGoalDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_goal, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        EditText etGoalName = dialogView.findViewById(R.id.etGoalName);
        Button btnStartDate = dialogView.findViewById(R.id.btnStartDate);
        Button btnDueDate = dialogView.findViewById(R.id.btnDueDate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelGoal);
        Button btnAddGoal = dialogView.findViewById(R.id.btnAddGoal);

        final Calendar calendarStart = Calendar.getInstance();
        final Calendar calendarDue = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        btnStartDate.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        calendarStart.set(year, month, dayOfMonth);
                        btnStartDate.setText(sdf.format(calendarStart.getTime()));
                    },
                    calendarStart.get(Calendar.YEAR),
                    calendarStart.get(Calendar.MONTH),
                    calendarStart.get(Calendar.DAY_OF_MONTH)
            );
            picker.show();
        });

        btnDueDate.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        calendarDue.set(year, month, dayOfMonth);
                        btnDueDate.setText(sdf.format(calendarDue.getTime()));
                    },
                    calendarDue.get(Calendar.YEAR),
                    calendarDue.get(Calendar.MONTH),
                    calendarDue.get(Calendar.DAY_OF_MONTH)
            );
            picker.show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAddGoal.setOnClickListener(v -> {
            String name = etGoalName.getText().toString().trim();
            String startDate = btnStartDate.getText().toString().trim();
            String dueDate = btnDueDate.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a goal name", Toast.LENGTH_SHORT).show();
            } else {
                // Add the goal (can store both dates in the Goal object if needed)
                goalList.add(new Goal(name, startDate, false));
                filteredList.clear();
                filteredList.addAll(goalList);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }

}
