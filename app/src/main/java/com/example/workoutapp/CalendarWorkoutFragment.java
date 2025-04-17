package com.example.workoutapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

            tvTitle.setText(w.name);
            tvStatus.setText(w.status);
            tvTime.setText(w.time);

            // Optional: make the whole card clickable
            item.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Clicked: " + w.name, Toast.LENGTH_SHORT).show()
            );

            // Optional: If you add a dropdown icon to the layout
            ImageView imgDropdown = item.findViewById(R.id.imgDropdown);
            if (imgDropdown != null) {
                imgDropdown.setOnClickListener(v ->
                        Toast.makeText(getContext(), "Show more for: " + w.name, Toast.LENGTH_SHORT).show()
                );
            }

            exerciseContainer.addView(item);
        }

        int count = workouts.size();
        String label = count == 0 ? "No Workouts" : count + (count > 1 ? " Workouts" : " Workout");
        if (exerciseCountLabel != null) {
            exerciseCountLabel.setText(label);
        }
    }
}
