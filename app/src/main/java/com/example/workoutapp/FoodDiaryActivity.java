package com.example.workoutapp;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
        inputCarbs.addTextChangedListener(stripZeroWatcher);
        inputFat.addTextChangedListener(stripZeroWatcher);
        inputProtein.addTextChangedListener(stripZeroWatcher);
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
            if (id == R.id.nav_meals) {
                return true;
            }
            else if (id == R.id.nav_workout) {
                intent = new Intent(this, Workouts.class);
            }
            else if (id == R.id.nav_calendar) {
                intent = new Intent(this, CalendarActivity.class);
            }
            else if (id == R.id.nav_profile) {
                intent = new Intent(this, Profile.class);
            }
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


    @SuppressLint("SetTextI18n")
    private void onDaySelected(LocalDate date) {
        currentSelectedDate = date;
        TextView lbl = findViewById(R.id.selectedDateText);
        lbl.setText(
                date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault())
                        + ", "
                        + date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                        + " " + date.getDayOfMonth()
        );
        reloadMealsForDate(date);
        fetchGoalsThenTotals(date);
    }

    private void reloadMealsForDate(LocalDate date) {
        List<List<FoodItem>> meals = foodLog.get(date);
        if (meals == null) {
            meals = new ArrayList<>();
            for (int i = 0; i < MEAL_NAMES.length; i++) {
                meals.add(new ArrayList<>());
            }
            foodLog.put(date, meals);
        }
        for (int i = 0; i < MEAL_NAMES.length; i++) {
            LinearLayout section = (LinearLayout) diaryContainer.getChildAt(i + 2);
            ((TextView)section.findViewById(R.id.tvMealTitle)).setText(MEAL_NAMES[i]);

            MaterialButton btnAdd = section.findViewById(R.id.btnAddFood);
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
            float cal = safeFloat(calStr), carb = safeFloat(carbStr), fat = safeFloat(fatStr), prot = safeFloat(protStr);
            List<List<FoodItem>> meals = foodLog.get(currentSelectedDate);
            FoodItem item = new FoodItem(name, cal, carb, fat, prot);
            meals.get(mealIndex).add(item);

            saveFoodLogToPrefs();
            addEntryToFirebase(item, mealIndex);
            LinearLayout section = (LinearLayout) diaryContainer.getChildAt(mealIndex + 2);
            addFoodChip(item, section.findViewById(R.id.foodItemsContainer), mealIndex);

            updateRemaining();
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
        MaterialButton btnAdd = dialogView.findViewById(R.id.btnAdd);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(dialogView).create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAdd.setOnClickListener(v -> {
            String food = etFood.getText().toString().trim();
            if (food.isEmpty()) {
                etFood.setError("Food name is required");
                return;
            }
            float cal = parseFloat(etCal), carb = parseFloat(etCarbs), frekFat = parseFloat(etFat), prot = parseFloat(etProt);
            List<List<FoodItem>> meals = foodLog.get(currentSelectedDate);
            FoodItem item = new FoodItem(food, cal, carb, frekFat, prot);
            meals.get(mealIndex).add(item);

            saveFoodLogToPrefs();
            addEntryToFirebase(item, mealIndex);
            addFoodChip(item, container, mealIndex);
            updateRemaining();
            dialog.dismiss();
        });
    }

    @SuppressLint("SetTextI18n")
    private void addFoodChip(FoodItem f, LinearLayout container, int mealIndex) {
        View chip = getLayoutInflater().inflate(R.layout.item_food_chip, container, false);
        TextView tvN = chip.findViewById(R.id.tvChipName);
        TextView tvD = chip.findViewById(R.id.tvChipDetails);
        ImageButton btnR = chip.findViewById(R.id.btnRemove);

        tvN.setText(f.name);
        tvD.setText(fmt(f.calories)+" kcal • "+fmt(f.carbs)+"g Carbs • "+fmt(f.fat)+"g Fat • "+fmt(f.protein)+"g Protein");

        btnR.setOnClickListener(v -> {
            chip.animate().alpha(0f).setDuration(400).withEndAction(() -> {
                container.removeView(chip);
                List<List<FoodItem>> meals = foodLog.get(currentSelectedDate);
                meals.get(mealIndex).remove(f);
                saveFoodLogToPrefs();
                removeEntryFromFirebase(f, mealIndex);
                updateRemaining();
            }).start();
        });
        container.addView(chip,0);
    }

    /**
     * Recalculates and updates "Remaining" based on goals minus eaten,
     * then persists those daily totals.
     */
    private void updateRemaining() {
        float goalCals  = parseFloat(inputCalories);
        float goalCarbs = parseFloat(inputCarbs);
        float goalFat   = parseFloat(inputFat);
        float goalProt  = parseFloat(inputProtein);
        float eatenCals = 0f, eatenCarbs = 0f, eatenFat = 0f, eatenProt = 0f;

        List<List<FoodItem>> meals = foodLog.get(currentSelectedDate);

        if (meals!=null) {
            for(List<FoodItem> m: meals) {
                for(FoodItem it: m) {
                    eatenCals+=it.calories; eatenCarbs+=it.carbs; eatenFat+=it.fat; eatenProt+=it.protein;
                }
            }
        }
        float remCals=goalCals-eatenCals, remCarbs=goalCarbs-eatenCarbs, remFat=goalFat -eatenFat, remProt=goalProt-eatenProt;
        remainCalories.setText(fmt(remCals));
        remainCarbs.setText(fmt(remCarbs));
        remainFat.setText(fmt(remFat));
        remainProtein.setText(fmt(remProt));
        saveDailyTotalsToFirebase(currentSelectedDate, remCals, remCarbs, remFat, remProt);
    }

    private float parseFloat(EditText et) {
        try {
            return Float.parseFloat(et.getText().toString().trim());
        }
        catch(Exception e){
            return 0f;
        }
    }

    private float safeFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch(Exception e){
            return 0f;
        }
    }

    private void loadFoodLogFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> ents = prefs.getStringSet(PREFS_KEY_ENTRIES,new HashSet<>());
        foodLog.clear();
        for(String rec : ents) {
            String[] p = rec.split("\\|",7);
            LocalDate d = LocalDate.parse(p[0]);

            int idx = Integer.parseInt(p[1]);
            float c = Float.parseFloat(p[3]), car = Float.parseFloat(p[4]), f = Float.parseFloat(p[5]), pr=Float.parseFloat(p[6]);
            FoodItem it = new FoodItem(p[2], c, car, f, pr);
            List<List<FoodItem>> m = foodLog.get(d);
            if(m == null){
                m = new ArrayList<>();
                for(int i = 0; i < MEAL_NAMES.length; i++) {
                    m.add(new ArrayList<>());
                }
                foodLog.put(d, m);
            }
            m.get(idx).add(it);
        }
    }

    private void saveFoodLogToPrefs() {
        SharedPreferences prefs=getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        Set<String> ents=new HashSet<>();
        for(Map.Entry<LocalDate, List<List<FoodItem>>> e : foodLog.entrySet()){
            String ds = e.getKey().toString();
            for(int i = 0; i < e.getValue().size(); i++){
                for(FoodItem f : e.getValue().get(i)){
                    ents.add(ds + "|"+ i + "|" + f.name + "|" + f.calories + "|" + f.carbs + "|" + f.fat + "|" + f.protein);
                }
            }
        }
        prefs.edit().putStringSet(PREFS_KEY_ENTRIES, ents).apply();
    }

    private void loadFoodLogFromFirebase() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u==null) return;
        FirebaseFirestore.getInstance().collection("foodDiary").document(u.getUid()).collection("entries")
                .get().addOnSuccessListener(query->{
                    foodLog.clear();
                    for(QueryDocumentSnapshot doc:query){
                        LocalDate d=LocalDate.parse(doc.getString("date"));
                        int idx = doc.getLong("mealIndex").intValue();
                        float c = doc.getDouble("calories").floatValue();
                        float car = doc.getDouble("carbs").floatValue();
                        float f = doc.getDouble("fat").floatValue();
                        float pr = doc.getDouble("protein").floatValue();
                        FoodItem it = new FoodItem(doc.getString("name"), c, car, f, pr);
                        List<List<FoodItem>> m = foodLog.computeIfAbsent(d,dd->{List<List<FoodItem>> mm = new ArrayList<>();
                            for(int i = 0; i < MEAL_NAMES.length; i++) {
                                mm.add(new ArrayList<>());
                            }
                            return mm;
                        });
                        m.get(idx).add(it);
                    }
                    saveFoodLogToPrefs();
                    reloadMealsForDate(currentSelectedDate);
                }).addOnFailureListener(e->Toast.makeText(this,"Failed to sync food diary: "+ e.getMessage(),Toast.LENGTH_LONG).show());
    }

    /**
     * Fetches profile goals, sets inputs, then loads or computes that day's remaining.
     */
    private void fetchGoalsThenTotals(LocalDate date) {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if(u == null) {
            updateRemaining();
            return;
        }
        FirebaseFirestore.getInstance().collection("users").document(u.getUid()).get()
                .addOnSuccessListener(userDoc->{
                    if(!userDoc.exists()) {
                        updateRemaining();
                        return;
                    }

                    Long calsL = userDoc.getLong("calories");
                    Double protD=userDoc.getDouble("protein");
                    Double carbD = userDoc.getDouble("carbs");
                    Double fatD=userDoc.getDouble("fats");

                    float gc = calsL != null ? calsL.floatValue():0f;
                    float gp = protD != null ? protD.floatValue():0f;
                    float gcar = carbD != null ? carbD.floatValue():0f;
                    float gf = fatD != null ? fatD.floatValue():0f;

                    inputCalories.setText(fmt(gc));
                    inputProtein.setText(fmt(gp));
                    inputCarbs.setText(fmt(gcar));
                    inputFat.setText(fmt(gf));

                    FirebaseFirestore.getInstance().collection("foodDiary").document(u.getUid())
                            .collection("dailyTotals").document(date.toString()).get()
                            .addOnSuccessListener(doc->{
                                if(doc.exists()){
                                    float rc = doc.getDouble("remainCalories").floatValue();
                                    float rcar = doc.getDouble("remainCarbs").floatValue();
                                    float rf = doc.getDouble("remainFat").floatValue();
                                    float rpr = doc.getDouble("remainProtein").floatValue();
                                    remainCalories.setText(fmt(rc));
                                    remainCarbs.setText(fmt(rcar));
                                    remainFat.setText(fmt(rf));
                                    remainProtein.setText(fmt(rpr));
                                }
                                else updateRemaining();
                            }).addOnFailureListener(e->{
                                updateRemaining();
                            });
                }).addOnFailureListener(e->{
                    updateRemaining();
                });
    }

    private void saveDailyTotalsToFirebase(LocalDate date, float remCals, float remCarbs, float remFat, float remProt) {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if(u == null) return;
        Map<String,Object>totals = new HashMap<>();
        totals.put("date", date.toString());
        totals.put("remainCalories", remCals);
        totals.put("remainCarbs", remCarbs);
        totals.put("remainFat", remFat);
        totals.put("remainProtein", remProt);
        FirebaseFirestore.getInstance().collection("foodDiary").document(u.getUid())
                .collection("dailyTotals").document(date.toString()).set(totals);
    }

    private void addEntryToFirebase(FoodItem item, int mealIndex){
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if(u==null) return;
        Map<String,Object> m = new HashMap<>();
        m.put("date", currentSelectedDate.toString());
        m.put("mealIndex", mealIndex);
        m.put("name", item.name);
        m.put("calories", item.calories);
        m.put("carbs", item.carbs);
        m.put("fat", item.fat);
        m.put("protein", item.protein);
        FirebaseFirestore.getInstance().collection("foodDiary").document(u.getUid())
                .collection("entries").add(m)
                .addOnFailureListener(e->Toast.makeText(this,"Could not save food to cloud: "+e.getMessage(),Toast.LENGTH_SHORT).show());
    }
    private void removeEntryFromFirebase(FoodItem f, int mealIndex){
        FirebaseUser u=FirebaseAuth.getInstance().getCurrentUser();
        if(u == null) return;
        FirebaseFirestore.getInstance().collection("foodDiary").document(u.getUid())
                .collection("entries").whereEqualTo("date", currentSelectedDate.toString())
                .whereEqualTo("mealIndex", mealIndex).whereEqualTo("name", f.name)
                .whereEqualTo("calories", f.calories).whereEqualTo("carbs", f.carbs)
                .whereEqualTo("fat", f.fat).whereEqualTo("protein", f.protein).get()
                .addOnSuccessListener(q->{
                    for(DocumentSnapshot d:q.getDocuments())d.getReference().delete();
                });
    }

    static class FoodItem {
        final String name;
        final float calories, carbs, fat, protein;
        FoodItem(String name, float calories, float carbs, float fat, float protein){
            this.name = name;
            this.calories = calories;
            this.carbs = carbs;
            this.fat = fat;
            this.protein = protein;
        }
    }
}
