package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.TextStyle;

import java.util.List;
import java.util.Locale;

public class FoodDiaryActivity extends AppCompatActivity {

    /* nutrition inputs / outputs */
    private EditText inputCalories, inputCarbs, inputFat, inputProtein;
    private TextView remainCalories, remainCarbs, remainFat, remainProtein;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* initialise ThreeTenABP just for this screen */
        AndroidThreeTen.init(this);

        setContentView(R.layout.food_diary_activity);   // make sure XML name matches

        bindNutritionViews();
        setupTextWatchers();
        setupWeekCalendar();
        setupBottomNav();
    }

    /* ---------- WEEK CALENDAR ---------- */
    private void setupWeekCalendar() {
        RecyclerView weekRv = findViewById(R.id.weekRecyclerView);

        /* ⭐ 7‑column grid instead of horizontal list  ⭐ */
        weekRv.setLayoutManager(new GridLayoutManager(this, 7));
        weekRv.setHasFixedSize(true);          // still fine
        weekRv.setNestedScrollingEnabled(false);   // no scrolling

        List<LocalDate> week = DateUtils.currentWeek(LocalDate.now());

        WeekAdapter adapter = new WeekAdapter(week, this::onDaySelected);
        weekRv.setAdapter(adapter);

        /* optional: pre‑select today */
        adapter.selectDate(LocalDate.now());
        onDaySelected(LocalDate.now());
    }

    private void onDaySelected(LocalDate date) {
        TextView lbl = findViewById(R.id.selectedDateText);
        lbl.setText(
                date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()) + ", " +
                        date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " +
                        date.getDayOfMonth());
        // TODO: reload meals for 'date'
    }

    /* ---------- BOTTOM NAV ---------- */
    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_meals);   // this tab

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_meals) return true;   // already here

            Intent intent = null;
            if (id == R.id.nav_workout)  intent = new Intent(this, Workouts.class);
            else if (id == R.id.nav_profile) intent = new Intent(this, Profile.class);
            else if (id == R.id.nav_calendar) intent = new Intent(this, CalendarActivity.class);
            // add others as needed

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }

    /* ---------- NUTRITION AREA ---------- */
    private void bindNutritionViews() {
        inputCalories = findViewById(R.id.inputCalories);
        inputCarbs    = findViewById(R.id.inputCarbs);
        inputFat      = findViewById(R.id.inputFat);
        inputProtein  = findViewById(R.id.inputProtein);

        remainCalories = findViewById(R.id.remainCalories);
        remainCarbs    = findViewById(R.id.remainCarbs);
        remainFat      = findViewById(R.id.remainFat);
        remainProtein  = findViewById(R.id.remainProtein);
    }

    private void setupTextWatchers() {
        TextWatcher w = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void onTextChanged (CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(Editable s){ updateRemaining(); }
        };
        inputCalories.addTextChangedListener(w);
        inputCarbs   .addTextChangedListener(w);
        inputFat     .addTextChangedListener(w);
        inputProtein .addTextChangedListener(w);
    }

    private void updateRemaining() {
        int cal = parse(inputCalories);
        int carb= parse(inputCarbs);
        int fat = parse(inputFat);
        int pro = parse(inputProtein);

        remainCalories.setText(String.valueOf(cal));
        remainCarbs   .setText(String.valueOf(carb));
        remainFat     .setText(String.valueOf(fat));
        remainProtein .setText(String.valueOf(pro));
    }

    private int parse(EditText et) {
        try { return Integer.parseInt(et.getText().toString().trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}
