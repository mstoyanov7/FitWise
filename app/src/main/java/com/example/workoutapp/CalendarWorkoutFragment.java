package com.example.workoutapp;

import android.graphics.Color;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CalendarWorkoutFragment extends Fragment {

    private static final String ARG_DATE = "selected_date";

    private LocalDate selectedDate;
    private LinearLayout exerciseContainer;
    private TextView exerciseCountLabel;

    public static CalendarWorkoutFragment newInstance(LocalDate date) {
        CalendarWorkoutFragment fragment = new CalendarWorkoutFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar_workout, container, false);

        exerciseContainer = root.findViewById(R.id.exerciseContainer);
        exerciseCountLabel = root.findViewById(R.id.exerciseCountLabel);

        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_DATE)) {
            try {
                selectedDate = LocalDate.parse(args.getString(ARG_DATE));
            } catch (Exception e) {
                selectedDate = LocalDate.now();
            }
        } else {
            selectedDate = LocalDate.now();
        }

        populateWorkouts();
        return root;
    }

    private void populateWorkouts() {
        if (exerciseContainer == null) return;
        exerciseContainer.removeAllViews();

        CalendarActivity activity = null;
        if (getActivity() instanceof CalendarActivity) {
            activity = (CalendarActivity) requireActivity();
        }
        if (activity == null) return;

        HashMap<LocalDate, List<CalendarActivity.CalendarWorkout>> dataMap = activity.getWorkoutData();
        List<CalendarActivity.CalendarWorkout> workouts = dataMap.getOrDefault(selectedDate, new ArrayList<>());

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (CalendarActivity.CalendarWorkout w : new ArrayList<>(workouts)) {
            View item = inflater.inflate(R.layout.calendar_item_workout, exerciseContainer, false);

            TextView tvTitle = item.findViewById(R.id.tvWorkoutTitle);
            TextView tvStatus = item.findViewById(R.id.tvWorkoutStatus);
            TextView tvTime = item.findViewById(R.id.tvWorkoutTime);
            ImageView imgDropdown = item.findViewById(R.id.imgDropdown);
            LinearLayout expandableLayout = item.findViewById(R.id.expandableLayout);
            LinearLayout exerciseList = item.findViewById(R.id.exerciseList);
            MaterialButton btnComplete = item.findViewById(R.id.btnCompleteWorkout);
            ImageView btnDelete = item.findViewById(R.id.btnDeleteWorkout);

            tvTitle.setText(w.name);
            tvTime.setText(w.time);

            // set initial status color + label
            boolean isCompleted = w.status.equalsIgnoreCase("Completed");
            if (isCompleted) {
                btnComplete.setText("Undo");
                btnComplete.setBackgroundColor(Color.parseColor("#F4F4F5"));
                btnComplete.setTextColor(Color.parseColor("#898989"));
                tvStatus.setText(" • Completed");
                tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            } else {
                btnComplete.setText("Complete");
                btnComplete.setBackgroundColor(Color.parseColor("#0BA284"));
                btnComplete.setTextColor(Color.WHITE);
                tvStatus.setText(" • Upcoming");
                tvStatus.setTextColor(Color.parseColor("#6B7280"));
            }

            // toggle complete/undo
            btnComplete.setOnClickListener(v -> {
                boolean nowCompleted = btnComplete.getText().toString().equalsIgnoreCase("Complete");
                if (nowCompleted) {
                    // Mark as completed
                    w.status = "Completed";
                    btnComplete.setText("Undo");
                    btnComplete.setBackgroundColor(Color.parseColor("#F4F4F5"));
                    btnComplete.setTextColor(Color.parseColor("#898989"));
                    tvStatus.setText(" • Completed");
                    tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
                } else {
                    // Undo completion
                    w.status = "Upcoming";
                    btnComplete.setText("Complete");
                    btnComplete.setBackgroundColor(Color.parseColor("#0BA284"));
                    btnComplete.setTextColor(Color.WHITE);
                    tvStatus.setText(" • Upcoming");
                    tvStatus.setTextColor(Color.parseColor("#6B7280"));
                }
            });

            // handle dropdown expand/collapse
            imgDropdown.setOnClickListener(v -> {
                if (expandableLayout.getVisibility() == View.VISIBLE) {
                    // collapse instantly
                    expandableLayout.setVisibility(View.GONE);
                    imgDropdown.animate().rotation(0f).setDuration(200).start();
                } else {
                    // animate expand
                    TransitionManager.beginDelayedTransition((ViewGroup) expandableLayout.getParent(), new AutoTransition());
                    expandableLayout.setVisibility(View.VISIBLE);
                    imgDropdown.animate().rotation(180f).setDuration(200).start();
                }
            });

            // populate exercises
            exerciseList.removeAllViews();
            for (String ex : w.exerciseList) {
                TextView chip = new TextView(requireContext());
                chip.setText(ex);
                chip.setTextColor(Color.parseColor("#374151"));
                chip.setTextSize(14);
                chip.setBackgroundResource(R.drawable.exercise_chip_background);
                chip.setPadding(24, 12, 24, 12);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 12);
                chip.setLayoutParams(params);
                exerciseList.addView(chip);
            }

            btnDelete.setOnClickListener(v -> {
                workouts.remove(w);
                dataMap.put(selectedDate, workouts);
                populateWorkouts();
            });

            exerciseContainer.addView(item);
        }

        int count = workouts.size();
        String label = count == 0 ? "No Workouts" : count + (count > 1 ? " Workouts" : " Workout");
        if (exerciseCountLabel != null) {
            exerciseCountLabel.setText(label);
        }
    }
}
