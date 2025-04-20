package com.example.workoutapp;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FoodDiaryActivity extends AppCompatActivity {

    private static final String PREFS_NAME        = "FoodDiaryPrefs";
    private static final String PREFS_KEY_ENTRIES = "food_entries";

    private static final int SCAN_REQUEST = 1001;
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

        // load persisted entries into memory
        loadFoodLogFromPrefs();
        FirebaseApp.initializeApp(this);
        loadFoodLogFromFirebase();

        setContentView(R.layout.food_diary_activity);
        FullscreenUtil.hideSystemUI(this);

        diaryContainer = findViewById(R.id.diaryContainer);

        bindNutritionViews();
        setupTextWatchers();
        setupWeekCalendar();
        setupBottomNav();
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

    private void setupTextWatchers() {
        TextWatcher stripZeroWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                String txt = s.toString();
                if (txt.length() > 1 && txt.startsWith("0")) {
                    s.replace(0, s.length(), txt.replaceFirst("^0+", ""));
                }
                updateRemaining();
            }
        };
        inputCalories.addTextChangedListener(stripZeroWatcher);
        inputCarbs   .addTextChangedListener(stripZeroWatcher);
        inputFat     .addTextChangedListener(stripZeroWatcher);
        inputProtein .addTextChangedListener(stripZeroWatcher);
    }

    private void setupWeekCalendar() {
        RecyclerView weekRv = findViewById(R.id.weekRecyclerView);
        weekRv.setLayoutManager(new GridLayoutManager(this, 7));
        weekRv.setNestedScrollingEnabled(false);

        List<LocalDate> week = DateUtils.currentWeek(currentSelectedDate);
        WeekAdapter adapter = new WeekAdapter(week, this::onDaySelected);
        weekRv.setAdapter(adapter);

        adapter.selectDate(currentSelectedDate);
        onDaySelected(currentSelectedDate);
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_meals);
        nav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            if (id == R.id.nav_meals) return true;
            else if (id == R.id.nav_workout)  intent = new Intent(this, Workouts.class);
            else if (id == R.id.nav_calendar) intent = new Intent(this, CalendarActivity.class);
            else if (id == R.id.nav_profile)  intent = new Intent(this, Profile.class);
            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
            return true;
        });
    }

    private String fmt(float v) {
        return (v == (long) v)
                ? String.valueOf((long) v)
                : String.format(Locale.getDefault(), "%.1f", v);
    }

    private void onDaySelected(LocalDate date) {
        currentSelectedDate = date;
        TextView lbl = findViewById(R.id.selectedDateText);
        lbl.setText(
                date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault())
                        + ", " + date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                        + " " + date.getDayOfMonth()
        );
        reloadMealsForDate(date);
    }

    private void reloadMealsForDate(LocalDate date) {
        List<List<FoodItem>> meals = foodLog.get(date);
        if (meals == null) {
            meals = new ArrayList<>();
            for (int i = 0; i < MEAL_NAMES.length; i++) meals.add(new ArrayList<>());
            foodLog.put(date, meals);
        }

        for (int i = 0; i < MEAL_NAMES.length; i++) {
            LinearLayout section = (LinearLayout) diaryContainer.getChildAt(i + 2);
            ((TextView)section.findViewById(R.id.tvMealTitle)).setText(MEAL_NAMES[i]);

            MaterialButton btnAdd  = section.findViewById(R.id.btnAddFood);
            MaterialButton btnScan = section.findViewById(R.id.btnScanFood);
            LinearLayout itemsContainer = section.findViewById(R.id.foodItemsContainer);

            itemsContainer.removeAllViews();
            final int mealIndex = i;

            btnAdd.setOnClickListener(v -> showAddFoodDialog(itemsContainer, mealIndex));
            btnScan.setOnClickListener(v -> {
                Intent scanIntent = new Intent(this, BarcodeScannerActivity.class);
                scanIntent.putExtra("selectedDate", date.toString());
                scanIntent.putExtra("mealIndex", mealIndex);
                startActivityForResult(scanIntent, SCAN_REQUEST);
            });

            for (FoodItem f : meals.get(i)) {
                addFoodChip(f, itemsContainer, i);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_REQUEST && resultCode == RESULT_OK && data != null) {
            String name    = data.getStringExtra("name");
            String calStr  = data.getStringExtra("calories");
            String carbStr = data.getStringExtra("carbs");
            String fatStr  = data.getStringExtra("fat");
            String protStr = data.getStringExtra("protein");
            int mealIndex  = data.getIntExtra("mealIndex", -1);

            float cal  = safeFloat(calStr),
                    carb = safeFloat(carbStr),
                    fat  = safeFloat(fatStr),
                    prot = safeFloat(protStr);

            List<List<FoodItem>> meals = foodLog.get(currentSelectedDate);
            FoodItem item = new FoodItem(name, cal, carb, fat, prot);
            meals.get(mealIndex).add(item);
            saveFoodLogToPrefs();  // persist addition
            addEntryToFirebase(item, mealIndex);

            LinearLayout section = (LinearLayout) diaryContainer.getChildAt(mealIndex + 2);
            LinearLayout itemsContainer = section.findViewById(R.id.foodItemsContainer);
            addFoodChip(item, itemsContainer, mealIndex);
            updateRemaining();
        }
    }

    private void showAddFoodDialog(LinearLayout container, int mealIndex) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_food, null);

        EditText etFood  = dialogView.findViewById(R.id.etFoodName);
        EditText etCal   = dialogView.findViewById(R.id.etCalories);
        EditText etCarbs = dialogView.findViewById(R.id.etCarbs);
        EditText etFat   = dialogView.findViewById(R.id.etFat);
        EditText etProt  = dialogView.findViewById(R.id.etProtein);

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

            float cal  = parseFloat(etCal),
                    carb = parseFloat(etCarbs),
                    fat  = parseFloat(etFat),
                    prot = parseFloat(etProt);

            List<List<FoodItem>> meals = foodLog.get(currentSelectedDate);
            FoodItem item = new FoodItem(food, cal, carb, fat, prot);
            meals.get(mealIndex).add(item);
            saveFoodLogToPrefs();  // persist addition
            addEntryToFirebase(item, mealIndex);


            addFoodChip(item, container, mealIndex);
            updateRemaining();
            dialog.dismiss();
        });
    }

    private void addFoodChip(FoodItem f, LinearLayout container, int mealIndex) {
        View chip = getLayoutInflater().inflate(R.layout.item_food_chip, container, false);
        TextView tvName    = chip.findViewById(R.id.tvChipName);
        TextView tvDetails = chip.findViewById(R.id.tvChipDetails);
        ImageButton btnRemove = chip.findViewById(R.id.btnRemove);

        tvName.setText(f.name);
        tvDetails.setText(
                fmt(f.calories) + " kcal  •  " +
                        fmt(f.carbs)    + "g Carbs  •  " +
                        fmt(f.fat)      + "g Fat  •  " +
                        fmt(f.protein) + "g Protein"
        );

        btnRemove.setOnClickListener(v -> {
            chip.animate()
                    .alpha(0f)
                    .setDuration(400)
                    .withEndAction(() -> {
                        container.removeView(chip);
                        List<List<FoodItem>> meals = foodLog.get(currentSelectedDate);
                        if (meals != null) meals.get(mealIndex).remove(f);
                        saveFoodLogToPrefs();  // persist removal
                        removeEntryFromFirebase(f, mealIndex);
                        updateRemaining();
                    });
        });

        container.addView(chip, 0);
    }

    private void updateRemaining() {
        float cals  = parseFloat(inputCalories);
        float carbs = parseFloat(inputCarbs);
        float fat   = parseFloat(inputFat);
        float prot  = parseFloat(inputProtein);

        remainCalories.setText(fmt(cals));
        remainCarbs   .setText(fmt(carbs));
        remainFat     .setText(fmt(fat));
        remainProtein .setText(fmt(prot));
    }

    private float parseFloat(EditText et) {
        try {
            return Float.parseFloat(et.getText().toString().trim());
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    private float safeFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (Exception e) {
            return 0f;
        }
    }

    private void loadFoodLogFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> entries = prefs.getStringSet(PREFS_KEY_ENTRIES, new HashSet<>());
        foodLog.clear();
        for (String entry : entries) {
            // entry format: date|mealIndex|name|cal|carbs|fat|protein
            String[] p = entry.split("\\|", 7);
            LocalDate date       = LocalDate.parse(p[0]);
            int mealIndex        = Integer.parseInt(p[1]);
            String name          = p[2];
            float calories       = Float.parseFloat(p[3]);
            float carbs          = Float.parseFloat(p[4]);
            float fat            = Float.parseFloat(p[5]);
            float protein        = Float.parseFloat(p[6]);
            FoodItem item        = new FoodItem(name, calories, carbs, fat, protein);
            List<List<FoodItem>> meals = foodLog.get(date);
            if (meals == null) {
                meals = new ArrayList<>();
                for (int i = 0; i < MEAL_NAMES.length; i++) meals.add(new ArrayList<>());
                foodLog.put(date, meals);
            }
            meals.get(mealIndex).add(item);
        }
    }

    private void saveFoodLogToPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> entries = new HashSet<>();
        for (Map.Entry<LocalDate, List<List<FoodItem>>> e : foodLog.entrySet()) {
            String dateStr = e.getKey().toString();
            List<List<FoodItem>> meals = e.getValue();
            for (int i = 0; i < meals.size(); i++) {
                for (FoodItem f : meals.get(i)) {
                    String rec = dateStr + "|" + i + "|" + f.name + "|"
                            + f.calories + "|" + f.carbs + "|" + f.fat + "|" + f.protein;
                    entries.add(rec);
                }
            }
        }
        prefs.edit()
                .putStringSet(PREFS_KEY_ENTRIES, entries)
                .apply();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View focus = getCurrentFocus();
            if (focus instanceof EditText) {
                Rect outRect = new Rect();
                focus.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)ev.getRawX(), (int)ev.getRawY())) {
                    EditText et = (EditText) focus;
                    focus.clearFocus();
                    if (et.getText().toString().trim().isEmpty()) et.setText("0");
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void addEntryToFirebase(FoodItem item, int mealIndex) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> m = new HashMap<>();
        m.put("date", currentSelectedDate.toString());
        m.put("mealIndex", mealIndex);
        m.put("name", item.name);
        m.put("calories", item.calories);
        m.put("carbs", item.carbs);
        m.put("fat", item.fat);
        m.put("protein", item.protein);

        FirebaseFirestore.getInstance()
                .collection("foodDiary")
                .document(user.getUid())
                .collection("entries")
                .add(m)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Could not save food to cloud: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void removeEntryFromFirebase(FoodItem f, int mealIndex) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("foodDiary")
                .document(user.getUid())
                .collection("entries")
                .whereEqualTo("date", currentSelectedDate.toString())
                .whereEqualTo("mealIndex", mealIndex)
                .whereEqualTo("name", f.name)
                .whereEqualTo("calories", f.calories)
                .whereEqualTo("carbs", f.carbs)
                .whereEqualTo("fat", f.fat)
                .whereEqualTo("protein", f.protein)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments())
                        doc.getReference().delete();
                });
    }


    private void loadFoodLogFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("foodDiary")
                .document(user.getUid())
                .collection("entries")
                .get()
                .addOnSuccessListener(query -> {
                    foodLog.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        String dateStr = doc.getString("date");
                        int mealIndex = doc.getLong("mealIndex").intValue();
                        String name    = doc.getString("name");
                        float cal      = doc.getDouble("calories").floatValue();
                        float carb     = doc.getDouble("carbs").floatValue();
                        float fat      = doc.getDouble("fat").floatValue();
                        float prot     = doc.getDouble("protein").floatValue();

                        LocalDate date = LocalDate.parse(dateStr);
                        FoodItem item  = new FoodItem(name, cal, carb, fat, prot);
                        List<List<FoodItem>> meals = foodLog.computeIfAbsent(date, d -> {
                            List<List<FoodItem>> m = new ArrayList<>();
                            for (int i = 0; i < MEAL_NAMES.length; i++) m.add(new ArrayList<>());
                            return m;
                        });
                        meals.get(mealIndex).add(item);
                    }
                    saveFoodLogToPrefs();
                    reloadMealsForDate(currentSelectedDate);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to sync food diary: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }


    static class FoodItem {
        final String name;
        final float calories, carbs, fat, protein;
        FoodItem(String name, float calories, float carbs, float fat, float protein) {
            this.name = name;
            this.calories = calories;
            this.carbs = carbs;
            this.fat = fat;
            this.protein = protein;
        }
    }
}
