package com.example.workoutapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.WorkoutViewHolder> {

    private final List<CalendarActivity.CalendarWorkout> workouts;

    public CalendarAdapter(List<CalendarActivity.CalendarWorkout> workouts) {
        this.workouts = workouts;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_calendar, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        CalendarActivity.CalendarWorkout workout = workouts.get(position);
        holder.bind(workout);
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public void updateWorkouts(List<CalendarActivity.CalendarWorkout> newWorkouts) {
        workouts.clear();
        workouts.addAll(newWorkouts);
//        notifyDataSetChanged();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvStatus;
        private final TextView tvTime;

        WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle  = itemView.findViewById(R.id.tvWorkoutTitle);
            tvStatus = itemView.findViewById(R.id.tvWorkoutStatus);
            tvTime   = itemView.findViewById(R.id.tvWorkoutTime);
        }

        void bind(CalendarActivity.CalendarWorkout workout) {
            tvTitle.setText(workout.name);
            tvStatus.setText(workout.status);
            tvTime.setText(workout.time);
        }
    }
}
