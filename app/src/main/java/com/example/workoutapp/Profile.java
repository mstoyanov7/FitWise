package com.example.workoutapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.QuerySnapshot;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.GridLabelRenderer;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Profile extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private RadioGroup radioGroupTabs;
    private TextView textViewHeader;
    private Button buttonViewAll;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String PREF_IMAGE_LOADED = "avatarLoaded";
    private View frontSide;
    private View backSide;

    private View nutritionSide;

    private TextView textViewAgeBack, textViewWeightBack, textViewSexBack, textViewMacros;
    private TextView textViewHeightBack;
    private TextView textViewActivityBack, textViewGoalBack, textViewWeeklyChangeBack;
    private TextView textViewCaloriesBack;

    private TextView textViewCaloriesConsumed, textViewForecastResult;
    private int currentSide = 0; // 0 = front, 1 = back, 2 = nutrition

    private Dialog editDialog;
    private Spinner spinnerActivityLevel, spinnerWeeklyGoal;
    private EditText editGoalWeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullscreenUtil.hideSystemUI(this);
        setContentView(R.layout.profile_page);

        radioGroupTabs = findViewById(R.id.radioGroupTabs);
        textViewHeader = findViewById(R.id.textViewHeader);
        buttonViewAll = findViewById(R.id.buttonViewAll);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        ImageButton buttonLogout = findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        frontSide = findViewById(R.id.frontSide);
        backSide = findViewById(R.id.backSide);
        nutritionSide = findViewById(R.id.nutritionSide);

        ImageButton buttonFlip = findViewById(R.id.buttonFlip);

        textViewAgeBack = findViewById(R.id.textViewHeight);
        textViewWeightBack = findViewById(R.id.textViewWeight);
        textViewHeightBack = findViewById(R.id.textViewHeight);
        textViewSexBack = findViewById(R.id.textViewSex);
        textViewActivityBack = findViewById(R.id.textViewActivityLevel);
        textViewGoalBack = findViewById(R.id.textViewGoalWeight);
        textViewWeeklyChangeBack = findViewById(R.id.textViewWeeklyGoal);
        textViewCaloriesBack = findViewById(R.id.textViewCalories);
        textViewMacros = findViewById(R.id.textViewMacros);
        textViewCaloriesConsumed = findViewById(R.id.textViewCaloriesConsumed);
        textViewForecastResult = findViewById(R.id.textViewForecastResult);

        buttonFlip.setOnClickListener(v -> toggleCard());

        ImageButton buttonEdit = findViewById((R.id.buttonEditProfile));
        buttonEdit.setOnClickListener(v -> {
            switch (currentSide) {
                case 0: // frontSide
                    showEditProfileDialog();
                    break;
                case 1: // backSide
                    showEditGoalsDialog();
                    break;
              case 2: // nutritionSide
                  showEditNutritionDialog();
                  break;
            }
        });

        loadFragment(new WorkoutsFragment());
        textViewHeader.setText("Recent Workouts");

        fetchAndDisplayUserData();

        radioGroupTabs.setOnCheckedChangeListener((group, checkedId) -> {
            Fragment selectedFragment = null;
            String headerText = "";

            if (checkedId == R.id.radioBMI) {
                selectedFragment = new BMICalculatorFragment();
                headerText = "BMI Calculator";
                buttonViewAll.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioGoals) {
                selectedFragment = new GoalsFragment();
                headerText = "Recent Goals";
                buttonViewAll.setVisibility(View.VISIBLE);
            }

            textViewHeader.setText(headerText);
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
        });

        buttonViewAll.setOnClickListener(v -> {
            String currentTab = textViewHeader.getText().toString();
            if (currentTab.contains("Goals")) {
                startActivity(new Intent(Profile.this, GoalsPage.class));
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Intent intent = null;

                if (id == R.id.nav_meals) {
                    intent = new Intent(Profile.this, FoodDiaryActivity.class);
                } else if (id == R.id.nav_workout) {
                    intent = new Intent(Profile.this, Workouts.class);
                } else if (id == R.id.nav_home) {
                    intent = new Intent(Profile.this, HomeActivity.class);
                } else if (id == R.id.nav_calendar) {
                    intent = new Intent(Profile.this, CalendarActivity.class);
                } else if (id == R.id.nav_profile) {
                    intent = new Intent(Profile.this, Profile.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    // Apply fade in to the incoming activity and fade out from the current one.
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    return true;
                }
                return false;
            }
        });

        String selectedTab = getIntent().getStringExtra("selectedTab");
        if ("goals".equalsIgnoreCase(selectedTab)) {
            radioGroupTabs.check(R.id.radioGoals);
        } else {
            radioGroupTabs.check(R.id.radioBMI);
        }
    }

    private void toggleCard() {
        View[] sides = {frontSide, backSide, nutritionSide};

        sides[currentSide].animate().rotationX(90).setDuration(200).withEndAction(() -> {
            sides[currentSide].setVisibility(View.GONE);
            currentSide = (currentSide + 1) % sides.length;
            sides[currentSide].setVisibility(View.VISIBLE);
            sides[currentSide].setRotationX(-90);
            sides[currentSide].animate().rotationX(0).setDuration(200).start();
        }).start();
    }

    private float getFloatSafe(SharedPreferences prefs, String key, float defaultValue) {
        try {
            Object value = prefs.getAll().get(key);
            if (value instanceof Float) return (Float) value;
            if (value instanceof Integer) return ((Integer) value).floatValue();
            if (value instanceof Double) return ((Double) value).floatValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAndDrawLast7DaysCalories();
    }

    private void calculateForecast(int averageCals, int sufficientCals)
    {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        int targetCals = prefs.getInt("calories", -1);
        String activity = prefs.getString("weeklyChange", "Maintain my current");

        // Step 1: Map weeklyChange to expected weight change per week
        double expectedKgPerWeek = 0.0;
        switch (activity) {
            case "Lose 1 kg per week":
                expectedKgPerWeek = -1.0;
                break;
            case "Lose 0.75 kg per week":
                expectedKgPerWeek = -0.75;
                break;
            case "Lose 0.5 kg per week":
                expectedKgPerWeek = -0.5;
                break;
            case "Lose 0.25 kg per week":
                expectedKgPerWeek = -0.25;
                break;
            case "Gain 0.25 kg per week":
                expectedKgPerWeek = 0.25;
                break;
            case "Gain 0.5 kg per week":
                expectedKgPerWeek = 0.5;
                break;
            default:
                expectedKgPerWeek = 0.0; // Maintain
        }

        double kcalPerKg = 7700.0;
        double forecast = ((averageCals - targetCals) * sufficientCals / kcalPerKg) + expectedKgPerWeek;

        String formattedForecast = String.format("%.2f", Math.abs(forecast));

        if (forecast < -0.01) {
            textViewForecastResult.setText("Expected loss of " + formattedForecast + " kg");
        }
        else if (forecast > 0.01){
            textViewForecastResult.setText("Expected gain of " + formattedForecast + " kg");
        }
        else{
            textViewForecastResult.setText("Expected maintenance");
        }
    }

    private int calculateAverageCal(List<Integer> dayTotals) {
        int total = 0;
        int nonZeroDays = 0;

        for (int cals : dayTotals) {
            if (cals > 0) {
                total += cals;
                nonZeroDays++;
            }
        }

        int average = 0;
        if (nonZeroDays > 0) {
            average = total / nonZeroDays;
        }

        if (average > 0) {
            textViewCaloriesConsumed.setText(average + " kcal");
        } else {
            textViewCaloriesConsumed.setText("No data");
        }

        return average;
    }

    private void loadAndDrawLast7DaysCalories() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Build the last-7-days list oldest→newest
        LocalDate today = LocalDate.now();
        List<String> last7 = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            last7.add(today.minusDays(i).toString());
        }
        // Initialize sums to zero
        Map<String,Integer> sums = new LinkedHashMap<>();
        for (String d : last7) sums.put(d, 0);

        FirebaseFirestore.getInstance()
                .collection("foodDiary")
                .document(user.getUid())
                .collection("entries")
                .whereIn("date", last7)
                .get()
                .addOnSuccessListener((QuerySnapshot qs) -> {
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        String date = doc.getString("date");
                        Number ncal = doc.getDouble("calories") != null
                                ? doc.getDouble("calories")
                                : doc.getLong("calories");
                        if (date != null && ncal != null && sums.containsKey(date)) {
                            sums.put(date, sums.get(date) + ncal.intValue());
                        }
                    }
                    // Turn into chronologically ordered List<Integer>
                    List<Integer> dayTotals = new ArrayList<>();
                    for (String d : last7) dayTotals.add(sums.get(d));

                    int averageCals = calculateAverageCal(dayTotals);

                    int sufficientCals = 0;
                    for (int val : dayTotals) {
                        if (val > 0) sufficientCals++;
                    }

                    if (sufficientCals > 0){
                        calculateForecast(averageCals, sufficientCals);
                    }

                    drawWeeklyCaloriesChart(dayTotals);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Couldn't load last week’s calories: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void drawWeeklyCaloriesChart(List<Integer> last7DaysCals) {
        GraphView graph = findViewById(R.id.weeklyCaloriesGraph);

        DataPoint[] calPoints = new DataPoint[7];
        for (int i = 0; i < 7; i++) {
            calPoints[i] = new DataPoint(i, last7DaysCals.get(i));
        }
        LineGraphSeries<DataPoint> calSeries = new LineGraphSeries<>(calPoints);
        calSeries.setColor(Color.parseColor("#0BA284"));
        calSeries.setDrawDataPoints(true);
        calSeries.setDataPointsRadius(8);
        calSeries.setThickness(6);

        graph.removeAllSeries();
        graph.addSeries(calSeries);

        GridLabelRenderer grid = graph.getGridLabelRenderer();
        grid.setGridStyle(GridLabelRenderer.GridStyle.BOTH);

        grid.setGridColor(Color.parseColor("#E0E0E0"));
        grid.setHorizontalLabelsColor(Color.parseColor("#4C494F"));
        grid.setVerticalLabelsColor(Color.parseColor("#4C494F"));

        grid.setNumHorizontalLabels(5);
        grid.setNumVerticalLabels(5);

        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        grid.setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);

        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(6);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);
    }

    private void saveEditedValues(String activity, String goalWeight, String weeklyGoal) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String sex = prefs.getString("sex", "Male");
        String height = prefs.getString("height", "170");
        int age = Integer.parseInt(prefs.getString("age", "21"));
        double weight = Double.parseDouble(prefs.getString("weight", "160"));

        int calories = WelcomeActivity.calculateCalories(sex, age, weight, Double.parseDouble(goalWeight), activity, weeklyGoal);
        float protein = (float) (calories * 0.3 / 4);
        float carbs = (float) (calories * 0.4 / 4);
        float fats = (float) (calories * 0.3 / 9);

        // Update UI
        textViewActivityBack.setText("Activity: " + activity);
        textViewGoalBack.setText("Goal: " + goalWeight + " kg");
        textViewWeeklyChangeBack.setText("Weekly: " + weeklyGoal);
        textViewCaloriesBack.setText("Target calories: " + calories);
        textViewMacros.setText("Protein: " + protein + "g • Carbs: " + carbs + "g • Fats: " + fats + "g");

        // Save to Firestore
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .update("activityLevel", activity,
                            "goalWeight", goalWeight,
                            "weeklyGoal", weeklyGoal,
                            "calories", calories,
                            "protein", protein,
                            "carbs", carbs,
                            "fats", fats,
                            "weight", String.valueOf(weight),
                            "height", height,
                            "sex", sex)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    });
        }

        // Save to SharedPreferences
        prefs.edit()
                .putString("activity", activity)
                .putString("goal", goalWeight)
                .putString("weeklyChange", weeklyGoal)
                .putInt("calories", calories)
                .putFloat("protein", protein)
                .putFloat("carbs", carbs)
                .putFloat("fats", fats)
                .apply();
    }


    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText editHeight = dialogView.findViewById(R.id.editTextHeight);
        EditText editWeight = dialogView.findViewById(R.id.editTextWeight);
        RadioGroup radioGroupSex = dialogView.findViewById(R.id.radioGroupSex);
        Button buttonSave = dialogView.findViewById(R.id.buttonSave);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentHeight = prefs.getString("height", "");
        String currentWeight = prefs.getString("weight", "");
        String currentSex = prefs.getString("sex", "Male");

        editHeight.setText(currentHeight);
        editWeight.setText(currentWeight);

        if ("Male".equalsIgnoreCase(currentSex)) {
            radioGroupSex.check(R.id.radioMale);
        } else {
            radioGroupSex.check(R.id.radioFemale);
        }

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonSave.setOnClickListener(v -> {
            String newHeight = editHeight.getText().toString().trim();
            String newWeight = editWeight.getText().toString().trim();
            int selectedSexId = radioGroupSex.getCheckedRadioButtonId();

            if (newHeight.isEmpty() || newWeight.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedSex = selectedSexId == R.id.radioFemale ? "Female" : "Male";

            // Save height, weight, and sex to SharedPreferences
            prefs.edit()
                    .putString("height", newHeight)
                    .putString("weight", newWeight)
                    .putString("sex", selectedSex)
                    .apply();

            // Update UI
            textViewHeightBack.setText(newHeight + " cm");
            textViewWeightBack.setText(newWeight + " kg");
            textViewSexBack.setText(selectedSex);

            // Get the latest values to pass to saveEditedValues()
            String activity = prefs.getString("activity", "Not very active");
            String goalWeight = prefs.getString("goal", newWeight); // fallback to new weight
            String weeklyGoal = prefs.getString("weeklyChange", "Maintain my current");

            // Now reuse the logic for calories + macros + Firestore update
            saveEditedValues(activity, goalWeight, weeklyGoal);
            // Notify BMI calculator to refresh
            reloadBMICalculatorIfVisible();
            dialog.dismiss();
        });

        dialog.show();
    }


    private void showEditGoalsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_goals, null);
        editDialog = new Dialog(this);
        editDialog.setContentView(dialogView);
        editDialog.setCancelable(true);
        editDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        spinnerActivityLevel = dialogView.findViewById(R.id.spinnerActivityLevel);
        spinnerWeeklyGoal = dialogView.findViewById(R.id.spinnerWeeklyGoal);
        editGoalWeight = dialogView.findViewById(R.id.editTextGoalWeight);

        Button buttonSave = dialogView.findViewById(R.id.buttonSave);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        // Load existing values from current view
        editGoalWeight.setText(textViewGoalBack.getText().toString().replaceAll("[^0-9.]", ""));

        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Not very active", "Lightly active", "Active", "Very active"});
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivityLevel.setAdapter(activityAdapter);

        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{
                        "Lose 1 kg per week", "Lose 0.75 kg per week", "Lose 0.5 kg per week", "Lose 0.25 kg per week",
                        "Maintain my current", "Gain 0.25 kg per week", "Gain 0.5 kg per week"});
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeeklyGoal.setAdapter(goalAdapter);

        // Pre-select the current values
        String currentActivity = textViewActivityBack.getText().toString().replace("Activity: ", "");
        String currentGoal = textViewWeeklyChangeBack.getText().toString().replace("Weekly: ", "");

        spinnerActivityLevel.setSelection(activityAdapter.getPosition(currentActivity));
        spinnerWeeklyGoal.setSelection(goalAdapter.getPosition(currentGoal));

        buttonCancel.setOnClickListener(v -> editDialog.dismiss());

        buttonSave.setOnClickListener(v -> {
            String newActivity = spinnerActivityLevel.getSelectedItem().toString();
            String newWeeklyGoal = spinnerWeeklyGoal.getSelectedItem().toString();
            String newGoalWeight = editGoalWeight.getText().toString().trim();

            if (newGoalWeight.isEmpty()) {
                Toast.makeText(this, "Goal weight is required", Toast.LENGTH_SHORT).show();
                return;
            }

            saveEditedValues(newActivity, newGoalWeight, newWeeklyGoal);
            editDialog.dismiss();
        });

        editDialog.show();
    }

    private void showEditNutritionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_macros, null);
        Dialog editDialog = new Dialog(this);
        editDialog.setContentView(dialogView);
        editDialog.setCancelable(true);
        if (editDialog.getWindow() != null) {
            editDialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            editDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // UI refs
        TextView tvTotal      = dialogView.findViewById(R.id.tvMacroCalories);
        SeekBar seekP         = dialogView.findViewById(R.id.sliderProtein);
        SeekBar seekC         = dialogView.findViewById(R.id.sliderCarbs);
        SeekBar seekF         = dialogView.findViewById(R.id.sliderFats);
        TextView tvProteinPct = dialogView.findViewById(R.id.tvProteinPercent);
        TextView tvCarbsPct   = dialogView.findViewById(R.id.tvCarbsPercent);
        TextView tvFatsPct    = dialogView.findViewById(R.id.tvFatsPercent);
        TextView tvWarning    = dialogView.findViewById(R.id.tvMacroWarning);
        Button  btnSave       = dialogView.findViewById(R.id.buttonSave);
        Button  btnCancel     = dialogView.findViewById(R.id.buttonCancel);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int totalCals = prefs.getInt("calories", 0);
        float p = getFloatSafe(prefs, "protein%", 30f);
        float c = getFloatSafe(prefs, "carbs%",   40f);
        float f = getFloatSafe(prefs, "fats%",    30f);
        if (Math.abs(p + c + f - 100f) > 0.5f) {
            p = 30; c = 40; f = 30;
        }

        // init
        tvTotal.setText("Total: " + totalCals + " kcal");
        seekP.setMax(100); seekC.setMax(100); seekF.setMax(100);
        seekP.setProgress((int)p);
        seekC.setProgress((int)c);
        seekF.setProgress((int)f);
        tvProteinPct.setText((int)p + "%");
        tvCarbsPct.setText((int)c + "%");
        tvFatsPct.setText((int)f + "%");

        boolean ok0 = (seekP.getProgress() + seekC.getProgress() + seekF.getProgress() == 100);
        tvWarning.setVisibility(ok0 ? View.GONE : View.VISIBLE);
        btnSave.setEnabled(ok0);

        final int STEP = 5;
        SeekBar.OnSeekBarChangeListener snapAndCheck = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int raw, boolean fromUser) {
                if (!fromUser) return;
                int snapped = Math.round(raw/(float)STEP)*STEP;
                snapped = Math.max(0, Math.min(100, snapped));
                sb.setProgress(snapped);

                int np = seekP.getProgress();
                int nc = seekC.getProgress();
                int nf = seekF.getProgress();
                tvProteinPct.setText(np + "%");
                tvCarbsPct.setText(nc + "%");
                tvFatsPct.setText(nf + "%");

                boolean ok = (np + nc + nf == 100);
                tvWarning.setVisibility(ok ? View.GONE : View.VISIBLE);
                btnSave.setEnabled(ok);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        };

        seekP.setOnSeekBarChangeListener(snapAndCheck);
        seekC.setOnSeekBarChangeListener(snapAndCheck);
        seekF.setOnSeekBarChangeListener(snapAndCheck);

        btnCancel.setOnClickListener(v -> editDialog.dismiss());

        btnSave.setOnClickListener(v -> {
            int newP = seekP.getProgress();
            int newC = seekC.getProgress();
            int newF = seekF.getProgress();

            // Calculate actual grams
            // protein & carbs = 4 kcal/g, fats = 9 kcal/g
            int gramsP = Math.round(totalCals * (newP/100f) / 4f);
            int gramsC = Math.round(totalCals * (newC/100f) / 4f);
            int gramsF = Math.round(totalCals * (newF/100f) / 9f);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.getUid())
                        .update(
                                "protein%", newP,
                                "carbs%",   newC,
                                "fats%",    newF,
                                "protein",  gramsP,
                                "carbs",    gramsC,
                                "fats",     gramsF
                        )
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(this, "Nutrition updated", Toast.LENGTH_SHORT).show()
                        );
            }

            // Save locally
            prefs.edit()
                    .putFloat("protein%", newP)
                    .putFloat("carbs%",   newC)
                    .putFloat("fats%",    newF)
                    .putInt("protein",   gramsP)
                    .putInt("carbs",     gramsC)
                    .putInt("fats",      gramsF)
                    .apply();

            textViewMacros.setText("Protein: " + (int) gramsP + "g • Carbs: " + (int) gramsC + "g • Fats: " + (int) gramsF + "g");

            editDialog.dismiss();
        });

        editDialog.show();
    }
    private void fetchAndDisplayUserData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cachedName = prefs.getString("name", null);
        String cachedAvatar = prefs.getString("avatarUrl", null);
        String cachedAge = prefs.getString("age", null);
        String cachedWeight = prefs.getString("weight", null);
        String cachedHeight = prefs.getString("height", null);
        String cachedSex = prefs.getString("sex", null);
        String cachedActivity = prefs.getString("activityLevel", null);
        String cachedGoal = prefs.getString("goalWeight", null);
        String cachedWeeklyChange = prefs.getString("weeklyChange", null);
        int cachedCalories = prefs.getInt("calories", -1);
        float cachedProtein = getFloatSafe(prefs, "protein", -1f);
        float cachedCarbs = getFloatSafe(prefs, "carbs", -1f);
        float cachedFats = getFloatSafe(prefs, "fats", -1f);

        updateUserAfterSignIn(cachedName, cachedAvatar);

        if (cachedAge != null) textViewAgeBack.setText("Age: " + cachedAge);
        if (cachedWeight != null) textViewWeightBack.setText(cachedWeight + " kg");
        if (cachedHeight != null) textViewHeightBack.setText(cachedHeight + " cm");
        if (cachedSex != null) textViewSexBack.setText(cachedSex);
        if (cachedActivity != null) textViewActivityBack.setText("Activity: " + cachedActivity);
        if (cachedGoal != null) textViewGoalBack.setText("Goal: " + cachedGoal + " kg");
        if (cachedWeeklyChange != null) textViewWeeklyChangeBack.setText("Weekly: " + cachedWeeklyChange);
        if (cachedCalories != -1) textViewCaloriesBack.setText("Target calories: " + cachedCalories);

        if (cachedProtein != -1f || cachedCarbs != -1f || cachedFats != -1f) {
            textViewMacros.setText("Protein: " + cachedProtein + "g • Carbs: " + cachedCarbs + "g • Fats: " + cachedFats + "g");
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String freshName = doc.getString("name");
                            String freshAge = doc.getString("age");
                            String freshWeight = doc.getString("weight");
                            String freshHeight = doc.getString("height");
                            String freshSex = doc.getString("sex");
                            String freshAvatar = doc.getString("avatarUrl");
                            String freshActivity = doc.getString("activityLevel");
                            String freshGoal = doc.getString("goalWeight");
                            String freshWeeklyChange = doc.getString("weeklyGoal");
                            Long freshCalories = doc.getLong("calories");
                            Double freshProtein = doc.getDouble("protein");
                            Double freshCarbs = doc.getDouble("carbs");
                            Double freshFats = doc.getDouble("fats");

                            updateUserAfterSignIn(freshName, freshAvatar);

                            if (freshAge != null) textViewAgeBack.setText("Age: " + freshAge);
                            if (freshWeight != null) textViewWeightBack.setText(freshWeight + " kg");
                            if (freshHeight != null) textViewHeightBack.setText(freshHeight + " cm");
                            if (freshSex != null) textViewSexBack.setText(freshSex);
                            if (freshActivity != null) textViewActivityBack.setText("Activity: " + freshActivity);
                            if (freshGoal != null) textViewGoalBack.setText("Goal: " + freshGoal + " kg");
                            if (freshWeeklyChange != null) textViewWeeklyChangeBack.setText("Weekly: " + freshWeeklyChange);
                            if (freshCalories != null) textViewCaloriesBack.setText("Target calories: " + freshCalories.intValue());
                            if (freshProtein != null && freshCarbs != null && freshFats != null) {
                                textViewMacros.setText("Protein: " + freshProtein.intValue() + "g • Carbs: " + freshCarbs.intValue() + "g • Fats: " + freshFats.intValue() + "g");
                            }

                            prefs.edit()
                                    .putString("name", freshName)
                                    .putString("age", freshAge)
                                    .putString("weight", freshWeight)
                                    .putString("height", freshHeight)
                                    .putString("sex", freshSex)
                                    .putString("avatarUrl", freshAvatar)
                                    .putString("activity", freshActivity)
                                    .putString("goal", freshGoal)
                                    .putString("weeklyChange", freshWeeklyChange)
                                    .putInt("calories", freshCalories != null ? freshCalories.intValue() : -1)
                                    .putFloat("protein", freshProtein != null ? freshProtein.floatValue() : -1f)
                                    .putFloat("carbs", freshCarbs != null ? freshCarbs.floatValue() : -1f)
                                    .putFloat("fats", freshFats != null ? freshFats.floatValue() : -1f)
                                    .putBoolean(PREF_IMAGE_LOADED, true)
                                    .apply();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to refresh profile", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateUserAfterSignIn(String name, String photoUrl) {
        TextView textViewName = findViewById(R.id.textViewName);
        ImageView imageViewPhoto = findViewById(R.id.imageViewProfilePic);

        textViewName.setText(name != null ? name : "");

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean avatarLoaded = prefs.getBoolean(PREF_IMAGE_LOADED, false);

        RequestOptions options = new RequestOptions()
                .placeholder(avatarLoaded ? R.drawable.avatar3 : R.drawable.loading_avatar)
                .error(R.drawable.avatar3);

        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .apply(options)
                    .centerCrop()
                    .circleCrop()
                    .into(imageViewPhoto);
            if (!avatarLoaded) prefs.edit().putBoolean(PREF_IMAGE_LOADED, true).apply();
        } else {
            imageViewPhoto.setImageResource(R.drawable.avatar3);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerProfileTabs, fragment)
                .commit();
    }

    private void showLogoutConfirmationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_logout_confirmation, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirmLogout);

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonConfirm.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().clear().apply();

            FirebaseAuth.getInstance().signOut();
            GoogleSignIn.getClient(this,
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .build()
            ).signOut();

            Toast.makeText(Profile.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Profile.this, SignInPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            dialog.dismiss();
        });

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void reloadBMICalculatorIfVisible() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerProfileTabs);
        if (currentFragment instanceof BMICalculatorFragment) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainerProfileTabs, new BMICalculatorFragment())
                    .commit();
        }
    }


}