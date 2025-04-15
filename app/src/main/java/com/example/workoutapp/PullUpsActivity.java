package com.example.workoutapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PullUpsActivity extends AppCompatActivity {

    private ImageButton searchButton;
    private EditText searchInput;
    private LinearLayout workoutListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_ups); // замени с точния layout файл

        searchButton = findViewById(R.id.search_button);
        searchInput = findViewById(R.id.search_input);
        workoutListContainer = findViewById(R.id.workout_list_container);

        // Показване/скриване на полето за търсене
        searchButton.setOnClickListener(v -> {
            if (searchInput.getVisibility() == View.GONE) {
                searchInput.setVisibility(View.VISIBLE);
                searchInput.requestFocus();
            } else {
                searchInput.setVisibility(View.GONE);
            }
        });

        // Търсене в реално време
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();

                for (int i = 0; i < workoutListContainer.getChildCount(); i++) {
                    View workoutCard = workoutListContainer.getChildAt(i);

                    // Извличаме заглавието от всяка карта
                    TextView titleView = workoutCard.findViewById(R.id.workout_title_1);
                    if (titleView == null) {
                        titleView = workoutCard.findViewById(R.id.workout_title_2);
                    }

                    if (titleView != null) {
                        String titleText = titleView.getText().toString().toLowerCase();

                        if (titleText.contains(query)) {
                            workoutCard.setVisibility(View.VISIBLE);
                        } else {
                            workoutCard.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
    }
}

