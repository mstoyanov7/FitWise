package com.example.workoutapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class WorkoutPreviewBottomSheet extends BottomSheetDialogFragment {

    private LinearLayout cardContainer;

    public WorkoutPreviewBottomSheet() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_workout_preview, container, false);
        cardContainer = view.findViewById(R.id.card_container);

        addStyledCard(
                "Week 1 Day 1",
                "30:00",
                new String[]{"Warm Up", "Jog and Walk", "Cool Down"},
                new String[]{"5min", "20min", "5min"},
                new int[]{R.drawable.ic_warmup, R.drawable.ic_jogging, R.drawable.ic_cooldown}
        );

        addStyledCard(
                "Week 1 Day 2",
                "35:00",
                new String[]{"Warm Up", "Jog and Walk", "Cool Down"},
                new String[]{"5min", "25min", "5min"},
                new int[]{R.drawable.ic_warmup, R.drawable.ic_jogging, R.drawable.ic_cooldown}
        );

        return view;
    }

    private void addStyledCard(String title, String duration, String[] stages, String[] times, int[] icons) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackgroundResource(R.drawable.running_card);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(dp(280), ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, dp(16), 0);
        card.setLayoutParams(cardParams);

        TextView titleText = new TextView(getContext());
        titleText.setText(title);
        titleText.setTextColor(Color.BLACK);
        titleText.setTextSize(16);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        titleText.setGravity(Gravity.CENTER_HORIZONTAL);
        card.addView(titleText);

        TextView durationText = new TextView(getContext());
        durationText.setText(duration);
        durationText.setTextColor(Color.parseColor("#1976D2"));
        durationText.setTextSize(32);
        durationText.setTypeface(null, android.graphics.Typeface.BOLD);
        durationText.setGravity(Gravity.CENTER_HORIZONTAL);
        durationText.setPadding(0, dp(4), 0, dp(16));
        card.addView(durationText);

        LinearLayout iconRow = new LinearLayout(getContext());
        iconRow.setOrientation(LinearLayout.HORIZONTAL);
        iconRow.setGravity(Gravity.CENTER);
        iconRow.setPadding(0, 0, 0, dp(16));

        for (int i = 0; i < stages.length; i++) {
            LinearLayout stageLayout = new LinearLayout(getContext());
            stageLayout.setOrientation(LinearLayout.VERTICAL);
            stageLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams stageParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            stageParams.setMargins(dp(8), 0, dp(8), 0);
            stageLayout.setLayoutParams(stageParams);

            ImageView icon = new ImageView(getContext());
            icon.setImageResource(icons[i]);
            icon.setLayoutParams(new LinearLayout.LayoutParams(dp(32), dp(32)));
            stageLayout.addView(icon);

            TextView stageLabel = new TextView(getContext());
            stageLabel.setText(stages[i]);
            stageLabel.setTextSize(12);
            stageLabel.setGravity(Gravity.CENTER);
            stageLayout.addView(stageLabel);

            TextView timeLabel = new TextView(getContext());
            timeLabel.setText(times[i]);
            timeLabel.setTextSize(12);
            timeLabel.setGravity(Gravity.CENTER);
            stageLayout.addView(timeLabel);

            iconRow.addView(stageLayout);

            if (i < stages.length - 1) {
                TextView arrow = new TextView(getContext());
                arrow.setText("→");
                arrow.setTextColor(Color.GRAY);
                arrow.setTextSize(18);
                arrow.setPadding(dp(4), 0, dp(4), 0);
                iconRow.addView(arrow);
            }
        }

        card.addView(iconRow);

        Button startBtn = new Button(getContext());
        startBtn.setText("GO TO WORKOUT");
        startBtn.setTextColor(Color.WHITE);
        startBtn.setTextSize(14);
        startBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        startBtn.setBackgroundResource(R.drawable.rounded_button_green);

        startBtn.setOnClickListener(v -> {
            CountdownDialogFragment dialog = CountdownDialogFragment.newInstance(stages, times);
            dialog.show(getParentFragmentManager(), "CountdownPopup");
        });

        card.addView(startBtn);
        cardContainer.addView(card);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
