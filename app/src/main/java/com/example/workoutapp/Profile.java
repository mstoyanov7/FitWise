package com.example.workoutapp;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
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
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private RadioGroup radioGroupTabs;
    private TextView textViewHeader;
    private Button buttonViewAll;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String PREF_IMAGE_LOADED = "avatarLoaded";

    private boolean isFlipped = false;
    private View frontSide;
    private View backSide;

    private View nutritionSide;

    private TextView textViewAgeBack, textViewWeightBack, textViewSexBack, textViewMacros;
    private TextView textViewHeightBack;
    private TextView textViewActivityBack, textViewGoalBack, textViewWeeklyChangeBack;
    private TextView textViewCaloriesBack;
    private int currentSide = 0; // 0 = front, 1 = back, 2 = nutrition

    private Dialog editDialog;
    private Spinner spinnerActivityLevel, spinnerWeeklyGoal;
    private EditText editGoalWeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        FullscreenUtil.hideSystemUI(this);

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

        textViewAgeBack = findViewById(R.id.textViewAge);
        textViewWeightBack = findViewById(R.id.textViewWeight);
        textViewHeightBack = findViewById(R.id.textViewHeight);
        textViewSexBack = findViewById(R.id.textViewSex);
        textViewActivityBack = findViewById(R.id.textViewActivityLevel);
        textViewGoalBack = findViewById(R.id.textViewGoalWeight);
        textViewWeeklyChangeBack = findViewById(R.id.textViewWeeklyGoal);
        textViewCaloriesBack = findViewById(R.id.textViewCalories);
        textViewMacros = findViewById(R.id.textViewMacros);

        buttonFlip.setOnClickListener(v -> toggleCard());

        backSide.setOnClickListener(v -> showEditDialog());

        loadFragment(new WorkoutsFragment());
        textViewHeader.setText("Recent Workouts");

        fetchAndDisplayUserData();

        radioGroupTabs.setOnCheckedChangeListener((group, checkedId) -> {
            Fragment selectedFragment = null;
            String headerText = "";

            if (checkedId == R.id.radioWorkouts) {
                selectedFragment = new WorkoutsFragment();
                headerText = "Recent Workouts";
                buttonViewAll.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.radioMeals) {
                selectedFragment = new MealsFragment();
                headerText = "Recent Meals";
                buttonViewAll.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.radioGoals) {
                selectedFragment = new GoalsFragment();
                headerText = "Recent Goals";
                buttonViewAll.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.radioProgress) {
                selectedFragment = new ProgressFragment();
                headerText = "Progress";
                buttonViewAll.setVisibility(View.GONE);
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
            } else if (currentTab.contains("Meals")) {
                Toast.makeText(this, "Open MealsPageActivity (not implemented)", Toast.LENGTH_SHORT).show();
            } else if (currentTab.contains("Workouts")) {
                Toast.makeText(this, "Open WorkoutsPageActivity (not implemented)", Toast.LENGTH_SHORT).show();
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
            radioGroupTabs.check(R.id.radioWorkouts);
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

    private void saveEditedValues(String activity, String goalWeight, String weeklyGoal) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String sex = prefs.getString("sex", "Male");
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
                            "fats", fats)
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

    private void showEditDialog() {
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
        if (cachedWeight != null) textViewWeightBack.setText("Weight: " + cachedWeight + " kg");
        if (cachedHeight != null) textViewHeightBack.setText("Height: " + cachedHeight + " cm");
        if (cachedSex != null) textViewSexBack.setText("Sex: " + cachedSex);
        if (cachedActivity != null) textViewActivityBack.setText("Activity: " + cachedActivity);
        if (cachedGoal != null) textViewGoalBack.setText("Goal: " + cachedGoal + " kg");
        if (cachedWeeklyChange != null) textViewWeeklyChangeBack.setText("Weekly: " + cachedWeeklyChange);
        if (cachedCalories != -1) textViewCaloriesBack.setText("Target calories: " + cachedCalories);

        if (cachedProtein != -1f || cachedCarbs != -1f || cachedFats != -1f) {
            textViewMacros.setText("Macros: P: " + cachedProtein + "g, C: " + cachedCarbs + "g, F: " + cachedFats + "g");
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
                            if (freshWeight != null) textViewWeightBack.setText("Weight: " + freshWeight + " kg");
                            if (freshHeight != null) textViewHeightBack.setText("Height: " + freshHeight + " cm");
                            if (freshSex != null) textViewSexBack.setText("Sex: " + freshSex);
                            if (freshActivity != null) textViewActivityBack.setText("Activity: " + freshActivity);
                            if (freshGoal != null) textViewGoalBack.setText("Goal: " + freshGoal + " kg");
                            if (freshWeeklyChange != null) textViewWeeklyChangeBack.setText("Weekly: " + freshWeeklyChange);
                            if (freshCalories != null) textViewCaloriesBack.setText("Target calories: " + freshCalories.intValue());
                            if (freshProtein != null && freshCarbs != null && freshFats != null) {
                                textViewMacros.setText("Protein: " + freshProtein.intValue() + "g Carbs: " + freshCarbs.intValue() + "g Fats: " + freshFats.intValue() + "g");
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
                .placeholder(avatarLoaded ? R.drawable.avatar : R.drawable.loading_avatar)
                .error(R.drawable.avatar);

        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .apply(options)
                    .centerCrop()
                    .circleCrop()
                    .into(imageViewPhoto);
            if (!avatarLoaded) prefs.edit().putBoolean(PREF_IMAGE_LOADED, true).apply();
        } else {
            imageViewPhoto.setImageResource(R.drawable.avatar);
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
}