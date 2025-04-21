package com.example.workoutapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GoalsFragment extends Fragment {

    private LinearLayout listView;
    private final List<Goal> recentGoals = new ArrayList<>();
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "GoalPrefs";
    private static final String PREFS_KEY = "goals";
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

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

    @Override
    public void onResume() {
        super.onResume();
        loadGoalsFromFirebase();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_goals, container, false);
        listView = view.findViewById(R.id.lvRecentGoals);
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        loadGoalsFromFirebase();

        return view;
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
                    for (QueryDocumentSnapshot doc : query) {
                        doc.getReference().delete();
                    }
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

    private void saveGoalsToPrefs() {
        Set<String> entries = new HashSet<>();
        for (Goal g : recentGoals) {
            entries.add(g.title + "|" + g.startDate + "|" + g.dueDate + "|" + g.isCompleted);
        }
        prefs.edit().putStringSet(PREFS_KEY, entries).apply();
    }

    private void loadGoalsFromFirebase() {
        if (currentUser == null) return;
        db.collection("goals")
                .document(currentUser.getUid())
                .collection("entries")
                .get()
                .addOnSuccessListener(query -> {
                    recentGoals.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        String title = doc.getString("title");
                        String start = doc.getString("startDate");
                        String due = doc.getString("dueDate");
                        boolean isCompleted = Boolean.TRUE.equals(doc.getBoolean("isCompleted"));
                        recentGoals.add(new Goal(title, start, due, isCompleted));
                    }

                    Collections.sort(recentGoals, (g1, g2) -> {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            Date d1 = sdf.parse(g1.startDate);
                            Date d2 = sdf.parse(g2.startDate);
                            return d2.compareTo(d1);
                        } catch (ParseException e) {
                            return 0;
                        }
                    });

                    if (recentGoals.size() > 4) {
                        recentGoals.subList(4, recentGoals.size()).clear();
                    }

                    saveGoalsToPrefs();
                    displayGoals();
                });
    }

    private void displayGoals() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        listView.removeAllViews();

        for (Goal goal : recentGoals) {
            View itemView = inflater.inflate(R.layout.goal_item, listView, false);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 8);
            itemView.setLayoutParams(params);

            TextView tvTitle = itemView.findViewById(R.id.tvGoalTitle);
            TextView tvDate = itemView.findViewById(R.id.tvGoalDate);
            Button btnAction = itemView.findViewById(R.id.btnGoalAction);
            ImageButton btnRemove = itemView.findViewById(R.id.btnRemove);

            tvTitle.setText(goal.title);
            tvDate.setText(goal.getFormattedDate());

            btnAction.setText(goal.isCompleted ? "Undo" : "Complete");
            btnAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(goal.isCompleted ? "#F4F4F5" : "#0BA284")));
            btnAction.setTextColor(Color.parseColor(goal.isCompleted ? "#6B7280" : "#FFFFFF"));

            btnAction.setOnClickListener(v -> {
                goal.isCompleted = !goal.isCompleted;
                updateGoalStatusInFirebase(goal);
                saveGoalsToPrefs();
                displayGoals();
            });

            btnRemove.setOnClickListener(v -> {
                deleteGoalFromFirebase(goal);
                recentGoals.remove(goal);
                saveGoalsToPrefs();
                displayGoals();
            });

            listView.addView(itemView);
        }
    }
}
