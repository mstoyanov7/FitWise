package com.example.workoutapp;

import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public class CountdownDialogFragment extends DialogFragment {

    private static final String ARG_STAGE_NAMES = "stage_names";
    private static final String ARG_STAGE_TIMES = "stage_times";

    private TextView countdownText, stageLabel, completeMsg;
    private Button pauseBtn;
    private Button nextBtn;
    private ProgressBar progressRing;

    private List<Stage> stages = new ArrayList<>();
    private int currentStageIndex = 0;

    private CountDownTimer timer;
    private boolean isPaused = false;
    private long timeRemaining = 0;
    private long stageDuration = 0;

    public static CountdownDialogFragment newInstance(String[] stageNames, String[] stageDurations) {
        CountdownDialogFragment fragment = new CountdownDialogFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_STAGE_NAMES, stageNames);
        args.putStringArray(ARG_STAGE_TIMES, stageDurations);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.countdown_timer, container, false);

        countdownText = view.findViewById(R.id.text_timer);
        stageLabel = view.findViewById(R.id.text_stage);
        pauseBtn = view.findViewById(R.id.btn_pause);
        nextBtn = view.findViewById(R.id.btn_next);
        completeMsg = view.findViewById(R.id.text_complete_message);
        progressRing = view.findViewById(R.id.progress_ring);

        String[] names = getArguments().getStringArray(ARG_STAGE_NAMES);
        String[] times = getArguments().getStringArray(ARG_STAGE_TIMES);

        if (names != null && times != null) {
            for (int i = 0; i < names.length; i++) {
                String timeStr = times[i].replaceAll("[^0-9]", "");
                long millis = Long.parseLong(timeStr) * 60 * 1000;
                stages.add(new Stage(names[i], millis));
            }
        }

        startStageTimer();

        pauseBtn.setOnClickListener(v -> {
            if (!isPaused) {
                isPaused = true;
                if (timer != null) timer.cancel();
                pauseBtn.setText("Resume");
            } else {
                startStageTimer();
                pauseBtn.setText("Pause");
            }
        });

        nextBtn.setOnClickListener(v -> {
            if (timer != null) timer.cancel();
            currentStageIndex++;

            if (currentStageIndex < stages.size()) {
                startStageTimer();
            } else {
                countdownText.setText("00:00");
                stageLabel.setText("🎉 Workout complete!");

                pauseBtn.setEnabled(false);
                pauseBtn.setAlpha(0.5f);
                nextBtn.setEnabled(false);
                nextBtn.setAlpha(0.5f);

                if (completeMsg != null) {
                    completeMsg.setVisibility(View.VISIBLE);
                }

                Toast.makeText(getContext(), "You crushed it! 💪", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void startStageTimer() {
        isPaused = false;

        Stage current = stages.get(currentStageIndex);
        stageLabel.setText(current.name);
        timeRemaining = current.durationMillis;
        stageDuration = current.durationMillis;

        pauseBtn.setText("Pause");
        completeMsg.setVisibility(View.GONE);

        progressRing.setMax((int) stageDuration);
        progressRing.setProgress((int) stageDuration);

        timer = new CountDownTimer(timeRemaining, 1000) {
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                updateTimerText(millisUntilFinished);
                progressRing.setProgress((int) millisUntilFinished);
            }

            public void onFinish() {
                currentStageIndex++;
                if (currentStageIndex < stages.size()) {
                    startStageTimer();
                } else {
                    countdownText.setText("00:00");
                    stageLabel.setText("🎉 Workout complete!");

                    pauseBtn.setEnabled(false);
                    pauseBtn.setAlpha(0.5f);
                    nextBtn.setEnabled(false);
                    nextBtn.setAlpha(0.5f);

                    if (completeMsg != null) {
                        completeMsg.setVisibility(View.VISIBLE);
                    }

                    Toast.makeText(getContext(), "You crushed it! 💪", Toast.LENGTH_SHORT).show();
                }
            }
        };

        timer.start();
    }

    private void updateTimerText(long millis) {
        int totalSeconds = (int) (millis / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        countdownText.setText(String.format("%02d:%02d", minutes, seconds));
    }

    @Override
    public void onDestroyView() {
        if (timer != null) timer.cancel();
        super.onDestroyView();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    public static class Stage {
        String name;
        long durationMillis;

        public Stage(String name, long durationMillis) {
            this.name = name;
            this.durationMillis = durationMillis;
        }
    }
}
