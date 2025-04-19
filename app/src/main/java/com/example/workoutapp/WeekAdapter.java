package com.example.workoutapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.TextStyle;

import java.util.List;
import java.util.Locale;

public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.DayVH> {

    public interface OnDayClickListener { void onDayClick(LocalDate date); }

    private final List<LocalDate>   weekDates;
    private final OnDayClickListener listener;
    private int selectedPos = -1;

    public WeekAdapter(List<LocalDate> weekDates, OnDayClickListener listener) {
        this.weekDates = weekDates;
        this.listener  = listener;
    }

    /* ---------- boilerplate ---------- */
    @NonNull @Override
    public DayVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_week_day, parent, false);
        return new DayVH(v);
    }

    @Override public void onBindViewHolder(@NonNull DayVH h, int pos) {
        LocalDate date = weekDates.get(pos);

        h.tvDateNum.setText(String.valueOf(date.getDayOfMonth()));
        DayOfWeek dow = date.getDayOfWeek();
        h.tvDayName.setText(dow.getDisplayName(TextStyle.SHORT, Locale.getDefault())); // Mon…

        h.itemView.setSelected(pos == selectedPos);

        h.itemView.setOnClickListener(v -> {
            int old = selectedPos;
            selectedPos = pos;
            notifyItemChanged(old);
            notifyItemChanged(selectedPos);
            listener.onDayClick(date);
        });
    }

    @Override public int getItemCount() { return weekDates.size(); }

    /* ---------- ViewHolder ---------- */
    static class DayVH extends RecyclerView.ViewHolder {
        final TextView tvDateNum, tvDayName;
        DayVH(@NonNull View itemView) {
            super(itemView);
            tvDateNum = itemView.findViewById(R.id.tvDateNum);
            tvDayName = itemView.findViewById(R.id.tvDayName);
        }
    }

    /** Programmatically marks the supplied date as selected. */
    public void selectDate(LocalDate date) {
        int idx = weekDates.indexOf(date);
        if (idx == -1) return;          // date not in this adapter (shouldn’t happen)
        int old = selectedPos;
        selectedPos = idx;
        notifyItemChanged(old);
        notifyItemChanged(selectedPos);
    }

}
