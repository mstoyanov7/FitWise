package com.example.workoutapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.ColorStateList;
import android.graphics.Color;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GoalsPage extends AppCompatActivity {

    private ListView lvGoals;
    private EditText etSearchGoal;
    private List<Goal> goalList = new ArrayList<>();
    private List<Goal> filteredList = new ArrayList<>();
    private ArrayAdapter<Goal> adapter;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "GoalPrefs";
    private static final String PREFS_KEY = "goals";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goals_page);
        FullscreenUtil.hideSystemUI(this);

        lvGoals = findViewById(R.id.lvGoals);
        etSearchGoal = findViewById(R.id.etSearchGoal);
        lvGoals.setDivider(null);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

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
                ImageButton btnRemove = convertView.findViewById(R.id.btnRemove);

                tvTitle.setText(goal.title);
                tvDate.setText(goal.getFormattedDate());

                if (goal.isCompleted) {
                    btnAction.setText("Undo");
                    btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F4F4F5")));
                    btnAction.setTextColor(Color.parseColor("#6B7280"));
                } else {
                    btnAction.setText("Complete");
                    btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0BA284")));
                    btnAction.setTextColor(Color.WHITE);
                }

                btnAction.setOnClickListener(v -> {
                    goal.isCompleted = !goal.isCompleted;
                    updateGoalStatusInFirebase(goal);
                    saveGoalsToPrefs();
                    notifyDataSetChanged();
                });

                btnRemove.setOnClickListener(v -> {
                    deleteGoalFromFirebase(goal);
                    goalList.remove(goal);
                    filteredList.remove(goal);
                    saveGoalsToPrefs();
                    notifyDataSetChanged();
                });

                convertView.setOnClickListener(v -> showEditGoalDialog(goal));

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

        loadGoalsFromPrefs();
        loadGoalsFromFirebase();
    }

    private void showAddGoalDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_goal, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        EditText etGoalName = dialogView.findViewById(R.id.etGoalName);
        MaterialButton btnStartDate = dialogView.findViewById(R.id.btnStartDate);
        MaterialButton btnDueDate = dialogView.findViewById(R.id.btnDueDate);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelGoal);
        MaterialButton btnAddGoal = dialogView.findViewById(R.id.btnAddGoal);

        btnStartDate.setText("Pick a date");
        btnDueDate.setText("Pick a date");

        final Calendar calendarStart = Calendar.getInstance();
        final Calendar calendarDue = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        btnStartDate.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendarStart.set(year, month, dayOfMonth);
                btnStartDate.setText(sdf.format(calendarStart.getTime()));
            }, calendarStart.get(Calendar.YEAR), calendarStart.get(Calendar.MONTH), calendarStart.get(Calendar.DAY_OF_MONTH));
            picker.show();
        });

        btnDueDate.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendarDue.set(year, month, dayOfMonth);
                btnDueDate.setText(sdf.format(calendarDue.getTime()));
            }, calendarDue.get(Calendar.YEAR), calendarDue.get(Calendar.MONTH), calendarDue.get(Calendar.DAY_OF_MONTH));
            picker.show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAddGoal.setOnClickListener(v -> {
            String name = etGoalName.getText().toString().trim();
            String startDate = btnStartDate.getText().toString().trim();
            String dueDate = btnDueDate.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a goal name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (startDate.equals("Pick a date")) {
                Toast.makeText(this, "Please select a start date", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dueDate.equals("Pick a date")) {
                Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Date start = sdf.parse(startDate);
                Date due = sdf.parse(dueDate);
                if (due.before(start)) {
                    Toast.makeText(this, "Due date cannot be before the start date", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Goal newGoal = new Goal(name, startDate, dueDate, false);
            saveGoalToFirebase(newGoal);
            dialog.dismiss();
        });
    }


    private void showEditGoalDialog(Goal goal) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_goal, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Edit Existing Goal");
        EditText etGoalName = dialogView.findViewById(R.id.etGoalName);
        MaterialButton btnStartDate = dialogView.findViewById(R.id.btnStartDate);
        MaterialButton btnDueDate = dialogView.findViewById(R.id.btnDueDate);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelGoal);
        MaterialButton btnAddGoal = dialogView.findViewById(R.id.btnAddGoal);

        etGoalName.setText(goal.title);
        btnStartDate.setText(goal.startDate != null ? goal.startDate : "Pick a date");
        btnDueDate.setText(goal.dueDate != null ? goal.dueDate : "Pick a date");
        btnAddGoal.setText("Update Goal");

        final Calendar calendarStart = Calendar.getInstance();
        final Calendar calendarDue = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        btnStartDate.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendarStart.set(year, month, dayOfMonth);
                btnStartDate.setText(sdf.format(calendarStart.getTime()));
            }, calendarStart.get(Calendar.YEAR), calendarStart.get(Calendar.MONTH), calendarStart.get(Calendar.DAY_OF_MONTH));
            picker.show();
        });

        btnDueDate.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendarDue.set(year, month, dayOfMonth);
                btnDueDate.setText(sdf.format(calendarDue.getTime()));
            }, calendarDue.get(Calendar.YEAR), calendarDue.get(Calendar.MONTH), calendarDue.get(Calendar.DAY_OF_MONTH));
            picker.show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAddGoal.setOnClickListener(v -> {
            String newTitle = etGoalName.getText().toString().trim();
            String newStart = btnStartDate.getText().toString().trim();
            String newDue = btnDueDate.getText().toString().trim();

            if (newTitle.isEmpty()) {
                Toast.makeText(this, "Please enter a goal name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newStart.equals("Pick a date")) {
                Toast.makeText(this, "Please select a start date", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newDue.equals("Pick a date")) {
                Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Date start = sdf.parse(newStart);
                Date due = sdf.parse(newDue);
                if (due.before(start)) {
                    Toast.makeText(this, "Due date cannot be before the start date", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            goal.title = newTitle;
            goal.startDate = newStart;
            goal.dueDate = newDue;
            updateGoalInFirebase(goal, newTitle, newStart, newDue);
            saveGoalsToPrefs();
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });
    }

    private void updateGoalStatusInFirebase(Goal goal) {
        if (currentUser == null) return;
        db.collection("goals")
                .document(currentUser.getUid())
                .collection("entries")
                .whereEqualTo("title", goal.title)
                .whereEqualTo("startDate", goal.startDate)
                .whereEqualTo("dueDate", goal.dueDate)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        doc.getReference().update("isCompleted", goal.isCompleted);
                    }
                });
    }

    private void updateGoalInFirebase(Goal goal, String newTitle, String newStart, String newDue) {
        if (currentUser == null) return;
        db.collection("goals")
                .document(currentUser.getUid())
                .collection("entries")
                .whereEqualTo("title", goal.title)
                .whereEqualTo("startDate", goal.startDate)
                .whereEqualTo("dueDate", goal.dueDate)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        doc.getReference().update("title", newTitle, "startDate", newStart, "dueDate", newDue);
                    }
                });
    }

    private void deleteGoalFromFirebase(Goal goal) {
        if (currentUser == null) return;
        db.collection("goals")
                .document(currentUser.getUid())
                .collection("entries")
                .whereEqualTo("title", goal.title)
                .whereEqualTo("startDate", goal.startDate)
                .whereEqualTo("dueDate", goal.dueDate)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        doc.getReference().delete();
                    }
                });
    }

    private void saveGoalsToPrefs() {
        Set<String> entries = new HashSet<>();
        for (Goal g : goalList) {
            entries.add(g.title + "|" + g.startDate + "|" + g.dueDate + "|" + g.isCompleted);
        }
        prefs.edit().putStringSet(PREFS_KEY, entries).apply();
    }

    private void loadGoalsFromPrefs() {
        Set<String> entries = prefs.getStringSet(PREFS_KEY, new HashSet<>());
        goalList.clear();
        for (String s : entries) {
            String[] p = s.split("\\|");
            if (p.length == 4) {
                String title = p[0];
                String start = p[1];
                String due = p[2];
                boolean isCompleted = Boolean.parseBoolean(p[3]);
                goalList.add(new Goal(title, start, due, isCompleted));
            }
        }
        filteredList.clear();
        filteredList.addAll(goalList);
        adapter.notifyDataSetChanged();
    }

    private void loadGoalsFromFirebase() {
        if (currentUser == null) return;
        db.collection("goals")
                .document(currentUser.getUid())
                .collection("entries")
                .get()
                .addOnSuccessListener(query -> {
                    goalList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        String title = doc.getString("title");
                        String start = doc.getString("startDate");
                        String due = doc.getString("dueDate");
                        boolean isCompleted = Boolean.TRUE.equals(doc.getBoolean("isCompleted"));
                        goalList.add(new Goal(title, start, due, isCompleted));
                    }
                    filteredList.clear();
                    filteredList.addAll(goalList);
                    saveGoalsToPrefs();
                    adapter.notifyDataSetChanged();
                });
    }

    private void saveGoalToFirebase(Goal goal) {
        if (currentUser == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("title", goal.title);
        data.put("startDate", goal.startDate);
        data.put("dueDate", goal.dueDate);
        data.put("isCompleted", goal.isCompleted);

        db.collection("goals")
                .document(currentUser.getUid())
                .collection("entries")
                .add(data)
                .addOnSuccessListener(docRef -> loadGoalsFromFirebase())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add goal", Toast.LENGTH_SHORT).show());
    }

    static class Goal {
        String title, startDate, dueDate;
        boolean isCompleted;

        Goal(String title, String startDate, String dueDate, boolean isCompleted) {
            this.title = title;
            this.startDate = startDate;
            this.dueDate = dueDate;
            this.isCompleted = isCompleted;
        }

        String getFormattedDate() {
            return startDate + " - " + dueDate;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
