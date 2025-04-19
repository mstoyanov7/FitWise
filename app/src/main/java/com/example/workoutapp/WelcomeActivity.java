package com.example.workoutapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class WelcomeActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView avatarImageView;
    private EditText weightEditText, ageEditText, goalWeightEditText;
    private RadioGroup sexRadioGroup;
    private Spinner activityLevelSpinner, weeklyGoalSpinner;
    private Button finishButton;

    private Uri selectedAvatarUri;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.welcome_page);
        FullscreenUtil.hideSystemUI(this);

        avatarImageView = findViewById(R.id.avatarImageView);
        weightEditText = findViewById(R.id.weightEditText);
        ageEditText = findViewById(R.id.ageEditText);
        goalWeightEditText = findViewById(R.id.goalWeightEditText);
        sexRadioGroup = findViewById(R.id.sexRadioGroup);
        activityLevelSpinner = findViewById(R.id.activityLevelSpinner);
        weeklyGoalSpinner = findViewById(R.id.weeklyGoalSpinner);
        finishButton = findViewById(R.id.finishButton);

        setupLoadingDialog();

        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Not very active", "Lightly active", "Active", "Very active"});
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activityLevelSpinner.setAdapter(activityAdapter);

        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{
                        "Lose 1 kg per week",
                        "Lose 0.75 kg per week",
                        "Lose 0.5 kg per week",
                        "Lose 0.25 kg per week",
                        "Maintain my current",
                        "Gain 0.25 kg per week",
                        "Gain 0.5 kg per week"
                });
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weeklyGoalSpinner.setAdapter(goalAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            selectedAvatarUri = user.getPhotoUrl();
            Glide.with(this)
                    .load(selectedAvatarUri)
                    .circleCrop()
                    .into(avatarImageView);
        }

        avatarImageView.setOnClickListener(v -> openGallery());
        finishButton.setOnClickListener(v -> handleFinish());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Avatar"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                selectedAvatarUri = uri;
                Glide.with(this)
                        .load(selectedAvatarUri)
                        .centerCrop()
                        .circleCrop()
                        .into(avatarImageView);
            }
        }
    }

    private void handleFinish() {
        String weight = weightEditText.getText().toString().trim();
        String age = ageEditText.getText().toString().trim();
        String goalWeight = goalWeightEditText.getText().toString().trim();
        String activityLevel = activityLevelSpinner.getSelectedItem().toString();
        String weeklyGoal = weeklyGoalSpinner.getSelectedItem().toString();
        int selectedSexId = sexRadioGroup.getCheckedRadioButtonId();

        if (weight.isEmpty() || age.isEmpty() || goalWeight.isEmpty() || selectedSexId == -1) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedSex = findViewById(selectedSexId);
        String sex = selectedSex.getText().toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        int calories = calculateCalories(
                sex,
                Integer.parseInt(age),
                Double.parseDouble(weight),
                Double.parseDouble(goalWeight),
                activityLevel,
                weeklyGoal
        );

        double protein = Double.parseDouble(weight) * 1.8; // g per kg
        double fats = (calories * 0.25) / 9.0; // 25% of cals / 9 kcal/g
        double carbs = (calories - (protein * 4 + fats * 9)) / 4.0; // remaining cals / 4 kcal/g

        showLoading();

        if (selectedAvatarUri != null) {
            if ("content".equals(selectedAvatarUri.getScheme())) {
                uploadAndSave(user, age, weight, sex, selectedAvatarUri, goalWeight, activityLevel, weeklyGoal, calories, protein, carbs, fats);
            } else {
                saveUserDataToFirestore(user, age, weight, sex, selectedAvatarUri.toString(), goalWeight, activityLevel, weeklyGoal, calories, protein, carbs, fats);
            }
        } else {
            saveUserDataToFirestore(user, age, weight, sex, null, goalWeight, activityLevel, weeklyGoal, calories, protein, carbs, fats);
        }
    }

    private void uploadAndSave(FirebaseUser user, String age, String weight, String sex, Uri fileUri,
                               String goalWeight, String activityLevel, String weeklyGoal, int calories,
                               double protein, double carbs, double fats) {
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference().child("avatars/" + user.getUid() + ".jpg");
        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage()
                        .getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> saveUserDataToFirestore(user, age, weight, sex,
                                downloadUri.toString(), goalWeight, activityLevel, weeklyGoal, calories, protein, carbs, fats))
                        .addOnFailureListener(e -> {
                            hideLoading();
                            Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(this, "Failed to upload avatar", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserDataToFirestore(FirebaseUser user, String age, String weight, String sex, String avatarUrl,
                                         String goalWeight, String activityLevel, String weeklyGoal, int calories,
                                         double protein, double carbs, double fats) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getDisplayName());
        userData.put("age", age);
        userData.put("weight", weight);
        userData.put("sex", sex);
        userData.put("goalWeight", goalWeight);
        userData.put("activityLevel", activityLevel);
        userData.put("weeklyGoal", weeklyGoal);
        userData.put("calories", calories);
        userData.put("protein", (int) protein);
        userData.put("carbs", (int) carbs);
        userData.put("fats", (int) fats);
        if (avatarUrl != null) {
            userData.put("avatarUrl", avatarUrl);
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("name", user.getDisplayName())
                            .putString("age", age)
                            .putString("weight", weight)
                            .putString("sex", sex)
                            .putString("goal", goalWeight)
                            .putString("activity", activityLevel)
                            .putString("weeklyChange", weeklyGoal)
                            .putInt("calories", calories)
                            .putInt("protein", (int) protein)
                            .putInt("carbs", (int) carbs)
                            .putInt("fats", (int) fats)
                            .putString("avatarUrl", avatarUrl)
                            .putBoolean("avatarLoaded", true)
                            .apply();

                    hideLoading();
                    Toast.makeText(this, "Welcome profile saved", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, Workouts.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Log.e("FirestoreError", "Failed to save profile", e);
                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    public static int calculateCalories(String sex, int age, double weight, double goalWeight, String activityLevel, String weeklyGoal) {
        double bmr;
        if (sex.equalsIgnoreCase("Male")) {
            bmr = 10 * weight + 6.25 * 170 - 5 * age + 5;
        } else {
            bmr = 10 * weight + 6.25 * 170 - 5 * age - 161;
        }

        double multiplier;
        switch (activityLevel) {
            case "Lightly active":
                multiplier = 1.375;
                break;
            case "Active":
                multiplier = 1.55;
                break;
            case "Very active":
                multiplier = 1.725;
                break;
            default:
                multiplier = 1.2;
        }

        double goalAdjustment;
        switch (weeklyGoal) {
            case "Lose 1 kg per week":
                goalAdjustment = -1000;
                break;
            case "Lose 0.75 kg per week":
                goalAdjustment = -750;
                break;
            case "Lose 0.5 kg per week":
                goalAdjustment = -500;
                break;
            case "Lose 0.25 kg per week":
                goalAdjustment = -250;
                break;
            case "Gain 0.25 kg per week":
                goalAdjustment = 250;
                break;
            case "Gain 0.5 kg per week":
                goalAdjustment = 500;
                break;
            default:
                goalAdjustment = 0;
        }

        return (int) ((bmr * multiplier) + goalAdjustment);
    }

    private void setupLoadingDialog() {
        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);
        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    private void showLoading() {
        if (!loadingDialog.isShowing()) loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog.isShowing()) loadingDialog.dismiss();
    }
}
