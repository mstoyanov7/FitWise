package com.example.workoutapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class GoalsFragment extends Fragment {

    static class Goal {
        String title;
        String date;
        boolean isCompleted;

        Goal(String title, String date, boolean isCompleted) {
            this.title = title;
            this.date = date;
            this.isCompleted = isCompleted;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_goals, container, false);
        ListView listView = view.findViewById(R.id.lvRecentGoals);

        // Simulated goals list — replace with real shared data source if needed
        List<Goal> fullGoals = new ArrayList<>();
        fullGoals.add(new Goal("Do 500 Pull-Ups", "01/06/2023", true));
        fullGoals.add(new Goal("Drink 100l of Water", "01/06/2023", false));
        fullGoals.add(new Goal("Run 10km", "01/06/2023", false));
        fullGoals.add(new Goal("Meditate 10 mins daily", "01/06/2023", false));
        fullGoals.add(new Goal("Stretch for 15 mins", "01/06/2023", true));

        // Only take the last 4
        List<Goal> displayList = fullGoals.subList(Math.max(0, fullGoals.size() - 4), fullGoals.size());

        ArrayAdapter<Goal> adapter = new ArrayAdapter<Goal>(getContext(), R.layout.goal_item, R.id.tvGoalTitle, displayList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                Goal goal = getItem(position);
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.goal_item, parent, false);
                }

                TextView tvTitle = convertView.findViewById(R.id.tvGoalTitle);
                TextView tvDate = convertView.findViewById(R.id.tvGoalDate);
                Button btnAction = convertView.findViewById(R.id.btnGoalAction);

                tvTitle.setText(goal.title);
                tvDate.setText(goal.date);

                if (goal.isCompleted) {
                    btnAction.setText("Undo");
                    btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F4F4F5")));
                    btnAction.setTextColor(android.graphics.Color.parseColor("#6B7280"));
                } else {
                    btnAction.setText("Complete");
                    btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0BA284")));
                    btnAction.setTextColor(android.graphics.Color.WHITE);
                }

                btnAction.setOnClickListener(v -> {
                    goal.isCompleted = !goal.isCompleted;
                    notifyDataSetChanged();
                });

                return convertView;
            }
        };

        listView.setAdapter(adapter);
        return view;
    }
}
