package com.example.workoutapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class CalendarAddWorkoutsAdapter extends RecyclerView.Adapter<CalendarAddWorkoutsAdapter.ViewHolder> {

    private final List<String> exercises;
    private final Context context;

    public CalendarAddWorkoutsAdapter(Context context, List<String> exercises) {
        this.context = context;
        this.exercises = exercises;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExercise;
        ImageView btnDelete;

        public ViewHolder(View view) {
            super(view);
            tvExercise = view.findViewById(R.id.tvExerciseChip);
            btnDelete = view.findViewById(R.id.btnDeleteExercise);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.calendar_item_exerchise_chip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = exercises.get(position);
        holder.tvExercise.setText(name);
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            exercises.remove(pos);
            notifyItemRemoved(pos);
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void moveItem(int from, int to) {
        Collections.swap(exercises, from, to);
        notifyItemMoved(from, to);
    }

    public List<String> getExercises() {
        return exercises;
    }
}
