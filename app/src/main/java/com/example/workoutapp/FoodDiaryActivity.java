package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.TextStyle;

import java.util.List;
import java.util.Locale;

public class FoodDiaryActivity extends AppCompatActivity {

    private static final String[] MEAL_NAMES = {"Breakfast","Lunch","Dinner","Snacks"};

    private EditText inputCalories, inputCarbs, inputFat, inputProtein;
    private TextView remainCalories, remainCarbs, remainFat, remainProtein;
    private LinearLayout diaryContainer;          // holds the four meal sections

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        setContentView(R.layout.food_diary_activity);

        diaryContainer = findViewById(R.id.diaryContainer);

        bindNutritionViews();
        setupTextWatchers();
        setupWeekCalendar();
        setupBottomNav();
    }

    private void setupWeekCalendar() {
        RecyclerView weekRv = findViewById(R.id.weekRecyclerView);
        weekRv.setLayoutManager(new GridLayoutManager(this, 7));
        weekRv.setHasFixedSize(true);
        weekRv.setNestedScrollingEnabled(false);

        List<LocalDate> week = DateUtils.currentWeek(LocalDate.now());
        WeekAdapter adapter = new WeekAdapter(week, this::onDaySelected);
        weekRv.setAdapter(adapter);

        adapter.selectDate(LocalDate.now());
        onDaySelected(LocalDate.now());
    }

    private void onDaySelected(LocalDate date) {
        TextView lbl = findViewById(R.id.selectedDateText);
        lbl.setText(
                date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()) + ", " +
                        date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " +
                        date.getDayOfMonth());

        reloadMealsForDate(date);
    }

    private void reloadMealsForDate(LocalDate date) {
        for (int i = 0; i < MEAL_NAMES.length; i++) {
            LinearLayout section = (LinearLayout) diaryContainer.getChildAt(i + 2);
            TextView title = (TextView) section.getChildAt(0);
            title.setText(MEAL_NAMES[i]);

            // TODO: clear existing food chips / views in this section
            // TODO: query your storage for foods on 'date' + MEAL_NAMES[i] and add them
        }
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_meals);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_meals) return true;
            Intent intent = null;
            if (id == R.id.nav_workout) intent = new Intent(this, Workouts.class);
            else if (id == R.id.nav_profile) intent = new Intent(this, Profile.class);
            else if (id == R.id.nav_calendar) intent = new Intent(this, CalendarActivity.class);
            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }

    private void bindNutritionViews() {
        inputCalories = findViewById(R.id.inputCalories);
        inputCarbs    = findViewById(R.id.inputCarbs);
        inputFat      = findViewById(R.id.inputFat);
        inputProtein  = findViewById(R.id.inputProtein);

        remainCalories = findViewById(R.id.remainCalories);
        remainCarbs    = findViewById(R.id.remainCarbs);
        remainFat      = findViewById(R.id.remainFat);
        remainProtein  = findViewById(R.id.remainProtein);

        setZeroDefault(inputCalories);
        setZeroDefault(inputCarbs);
        setZeroDefault(inputFat);
        setZeroDefault(inputProtein);
    }

    private void setZeroDefault(EditText et) {
        et.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && et.getText().toString().trim().isEmpty()) {
                et.setText("0");
            }
        });
    }

    private TextWatcher stripLeadingZerosWatcher(EditText target) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c) {}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                String txt = s.toString();
                if (txt.length() > 1 && txt.startsWith("0")) {
                    String cleaned = txt.replaceFirst("^0+(?!$)", ""); // remove all leading 0s except if only "0"
                    target.removeTextChangedListener(this);
                    target.setText(cleaned);
                    target.setSelection(cleaned.length());
                    target.addTextChangedListener(this);
                }
                updateRemaining();   // keep nutrition totals fresh
            }
        };
    }

    private void setupTextWatchers() {
        inputCalories.addTextChangedListener(stripLeadingZerosWatcher(inputCalories));
        inputCarbs   .addTextChangedListener(stripLeadingZerosWatcher(inputCarbs));
        inputFat     .addTextChangedListener(stripLeadingZerosWatcher(inputFat));
        inputProtein .addTextChangedListener(stripLeadingZerosWatcher(inputProtein));
    }

    private void updateRemaining() {
        int cal  = parse(inputCalories);
        int carb = parse(inputCarbs);
        int fat  = parse(inputFat);
        int prot = parse(inputProtein);

        remainCalories.setText(String.valueOf(cal));
        remainCarbs.setText(String.valueOf(carb));
        remainFat.setText(String.valueOf(fat));
        remainProtein.setText(String.valueOf(prot));
    }

    private int parse(EditText et) {
        try {
            return Integer.parseInt(et.getText().toString().trim());
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }
}
