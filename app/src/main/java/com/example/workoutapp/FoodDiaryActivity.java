package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.TextStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FoodDiaryActivity extends AppCompatActivity {

    private static final String[] MEAL_NAMES = {"Breakfast", "Lunch", "Dinner", "Snacks"};

    private EditText inputCalories, inputCarbs, inputFat, inputProtein;
    private TextView remainCalories, remainCarbs, remainFat, remainProtein;
    private LinearLayout diaryContainer;

    private LocalDate currentSelectedDate;
    private final Map<LocalDate, List<List<FoodItem>>> foodLog = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        currentSelectedDate = LocalDate.now();
        setContentView(R.layout.food_diary_activity);
        FullscreenUtil.hideSystemUI(this);

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

        List<LocalDate> week = DateUtils.currentWeek(currentSelectedDate);
        WeekAdapter adapter = new WeekAdapter(week, this::onDaySelected);
        weekRv.setAdapter(adapter);

        adapter.selectDate(currentSelectedDate);
        onDaySelected(currentSelectedDate);
    }

    private void onDaySelected(LocalDate date) {
        currentSelectedDate = date;
        Log.d("FoodDiary", "Selected date: " + date);

        TextView lbl = findViewById(R.id.selectedDateText);
        lbl.setText(
                date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()) + ", " +
                        date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " +
                        date.getDayOfMonth()
        );

        reloadMealsForDate(date);
    }

    private void reloadMealsForDate(LocalDate date) {
        // ensure we have 4 lists for this date
        List<List<FoodItem>> meals = foodLog.get(date);
        if (meals == null) {
            meals = new ArrayList<>(MEAL_NAMES.length);
            for (int i = 0; i < MEAL_NAMES.length; i++) {
                meals.add(new ArrayList<>());
            }
            foodLog.put(date, meals);
        }

        // for each meal section
        for (int i = 0; i < MEAL_NAMES.length; i++) {
            LinearLayout section = (LinearLayout) diaryContainer.getChildAt(i + 2);
            TextView title = section.findViewById(R.id.tvMealTitle);
            title.setText(MEAL_NAMES[i]);

            MaterialButton btnAdd = section.findViewById(R.id.btnAddFood);
            LinearLayout itemsContainer = section.findViewById(R.id.foodItemsContainer);

            itemsContainer.removeAllViews();
            int mealIndex = i;
            btnAdd.setOnClickListener(v -> showAddFoodDialog(itemsContainer, mealIndex));

            // add saved items for this meal
            for (FoodItem f : meals.get(i)) {
                addFoodChip(f, itemsContainer);
            }
        }
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_meals);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_meals) return true;
            Intent intent = null;
            if (id == R.id.nav_workout)  {
                intent = new Intent(this, Workouts.class);
            }
            else if (id == R.id.nav_profile) {
                intent = new Intent(this, Profile.class);
            }
            else if (id == R.id.nav_calendar) {
                intent = new Intent(this, CalendarActivity.class);
            }
            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }

    private void bindNutritionViews() {
        inputCalories = findViewById(R.id.inputCalories);
        inputCarbs = findViewById(R.id.inputCarbs);
        inputFat = findViewById(R.id.inputFat);
        inputProtein = findViewById(R.id.inputProtein);

        remainCalories = findViewById(R.id.remainCalories);
        remainCarbs = findViewById(R.id.remainCarbs);
        remainFat = findViewById(R.id.remainFat);
        remainProtein = findViewById(R.id.remainProtein);

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

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                updateRemaining();
            }
        };
        inputCalories.addTextChangedListener(watcher);
        inputCarbs.addTextChangedListener(watcher);
        inputFat.addTextChangedListener(watcher);
        inputProtein.addTextChangedListener(watcher);
    }

    private void updateRemaining() {
        int cal = parse(inputCalories);
        int carb = parse(inputCarbs);
        int fat = parse(inputFat);
        int prot = parse(inputProtein);

        remainCalories.setText(String.valueOf(cal));
        remainCarbs.setText(String.valueOf(carb));
        remainFat.setText(String.valueOf(fat));
        remainProtein.setText(String.valueOf(prot));
    }

    private int parse(EditText et) {
        try {
            return Integer.parseInt(et.getText().toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showAddFoodDialog(LinearLayout container, int mealIndex) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_food, null);

        EditText etFood = dialogView.findViewById(R.id.etFoodName);
        EditText etCal = dialogView.findViewById(R.id.etCalories);
        EditText etCarbs = dialogView.findViewById(R.id.etCarbs);
        EditText etFat = dialogView.findViewById(R.id.etFat);
        EditText etProt = dialogView.findViewById(R.id.etProtein);

        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnAdd    = dialogView.findViewById(R.id.btnAdd);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String food = etFood.getText().toString().trim();
            if (food.isEmpty()) {
                etFood.setError("Food name is required");
                etFood.requestFocus();
                return;
            }

            int cal = parse(etCal);
            int carb = parse(etCarbs);
            int fat = parse(etFat);
            int prot = parse(etProt);

            // save to the correct meal list for the selected date
            List<List<FoodItem>> meals = foodLog.get(currentSelectedDate);
            FoodItem item = new FoodItem(food, cal, carb, fat, prot);
            meals.get(mealIndex).add(item);

            addFoodChip(item, container);
            updateRemaining();
            dialog.dismiss();
        });
    }

    private void addFoodChip(FoodItem f, LinearLayout container) {
        View chip = getLayoutInflater().inflate(R.layout.item_food_chip, container, false);
        TextView tvName    = chip.findViewById(R.id.tvChipName);
        TextView tvDetails = chip.findViewById(R.id.tvChipDetails);

        tvName   .setText(f.name);
        tvDetails.setText(
                f.calories + "kcal  •  " +
                        f.carbs    + "g Carbs  •  " +
                        f.fat      + "g Fat  •  " +
                        f.protein + "g Protein"
        );

        container.addView(chip, 0);
    }

    // model for a single food entry
    static class FoodItem {
        final String name;
        final int calories, carbs, fat, protein;
        FoodItem(String name, int calories, int carbs, int fat, int protein) {
            this.name = name;
            this.calories = calories;
            this.carbs = carbs;
            this.fat = fat;
            this.protein = protein;
        }
    }
}
