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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
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
        int completedCount = 0;

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

            boolean isCompleted = w.status.equalsIgnoreCase("Completed");
            if (isCompleted) {
                completedCount++;
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

            btnComplete.setOnClickListener(v -> {
                boolean nowCompleted = btnComplete.getText().toString().equalsIgnoreCase("Complete");
                String newStatus = nowCompleted ? "Completed" : "Upcoming";
                String previousStatus = w.status;
                w.status = newStatus;

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String uid = user.getUid();
                    String date = selectedDate.toString();


                    db.collection("workouts").document(uid)
                            .collection("entries")
                            .whereEqualTo("name", w.name)
                            .whereEqualTo("time", w.time)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                for (var doc : snapshot.getDocuments()) {
                                    doc.getReference().update("status", newStatus);
                                }

                                populateWorkouts();

                                DocumentReference totalRef = db.collection("workouts")
                                        .document(uid)
                                        .collection("entries")
                                        .document("completedWorkouts");

                                totalRef.get().addOnSuccessListener(doc -> {
                                    Long totalCount = doc.getLong("count");
                                    if (totalCount == null) totalCount = 0L;

                                    long newTotal = totalCount;
                                    if (nowCompleted && !previousStatus.equalsIgnoreCase("Completed")) {
                                        newTotal++;
                                    } else if (!nowCompleted && previousStatus.equalsIgnoreCase("Completed")) {
                                        newTotal = Math.max(0, newTotal - 1);
                                    }

                                    totalRef.set(Collections.singletonMap("count", newTotal));
                                });

                                DocumentReference dayRef = db.collection("workouts")
                                        .document(uid)
                                        .collection("entries")
                                        .document("completedWorkouts")
                                        .collection("byDate")
                                        .document(date);

                                dayRef.get().addOnSuccessListener(doc -> {
                                    Long current = doc.getLong("count");
                                    if (current == null) current = 0L;

                                    long newCount = current;
                                    if (nowCompleted && !previousStatus.equalsIgnoreCase("Completed")) {
                                        newCount++;
                                    } else if (!nowCompleted && previousStatus.equalsIgnoreCase("Completed")) {
                                        newCount = Math.max(0, newCount - 1);
                                    }

                                    dayRef.set(Collections.singletonMap("count", newCount));
                                });
                            });
                }
            });

            imgDropdown.setOnClickListener(v -> {
                if (expandableLayout.getVisibility() == View.VISIBLE) {
                    expandableLayout.setVisibility(View.GONE);
                    imgDropdown.animate().rotation(0f).setDuration(200).start();
                } else {
                    TransitionManager.beginDelayedTransition((ViewGroup) expandableLayout.getParent(), new AutoTransition());
                    expandableLayout.setVisibility(View.VISIBLE);
                    imgDropdown.animate().rotation(180f).setDuration(200).start();
                }
            });

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
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String uid = user.getUid();
                    String today = LocalDate.now().toString();

                    db.collection("workouts")
                            .document(uid)
                            .collection("entries")
                            .whereEqualTo("name", w.name)
                            .whereEqualTo("time", w.time)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                boolean wasCompleted = w.status.equalsIgnoreCase("Completed");

                                for (var doc : querySnapshot.getDocuments()) {
                                    doc.getReference().delete();
                                }

                                if (wasCompleted) {
                                    DocumentReference totalRef = db.collection("workouts")
                                            .document(uid)
                                            .collection("entries")
                                            .document("completedWorkouts");

                                    totalRef.get().addOnSuccessListener(doc -> {
                                        Long count = doc.getLong("count");
                                        if (count == null) count = 0L;
                                        long newCount = Math.max(0, count - 1);
                                        totalRef.set(Collections.singletonMap("count", newCount));
                                    });

                                    DocumentReference dayRef = db.collection("workouts")
                                            .document(uid)
                                            .collection("entries")
                                            .document("completedWorkouts")
                                            .collection("byDate")
                                            .document(today);

                                    dayRef.get().addOnSuccessListener(doc -> {
                                        Long count = doc.getLong("count");
                                        if (count == null) count = 0L;
                                        long newCount = Math.max(0, count - 1);
                                        dayRef.set(Collections.singletonMap("count", newCount));
                                    });
                                }

                                workouts.remove(w);
                                dataMap.put(selectedDate, workouts);
                                populateWorkouts();
                            });
                } else {
                    workouts.remove(w);
                    dataMap.put(selectedDate, workouts);
                    populateWorkouts();
                }
            });

            exerciseContainer.addView(item);
        }

        int total = workouts.size();
        String label = total == 0 ? "No Workouts" :
                completedCount + "/" + total + " Completed";
        if (exerciseCountLabel != null) {
            exerciseCountLabel.setText(label);
        }
    }

}
