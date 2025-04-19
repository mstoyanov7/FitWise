package com.example.workoutapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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

        // Default tab
        loadFragment(new WorkoutsFragment());
        textViewHeader.setText("Recent Workouts");

        // Display user data
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
                    //intent = new Intent(Profile.this, HomeActivity.class);
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

        // Select initial tab
        String selectedTab = getIntent().getStringExtra("selectedTab");
        if ("goals".equalsIgnoreCase(selectedTab)) {
            radioGroupTabs.check(R.id.radioGoals);
        } else {
            radioGroupTabs.check(R.id.radioWorkouts);
        }
    }

    private void fetchAndDisplayUserData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cachedName = prefs.getString("name", null);
        String cachedAvatar = prefs.getString("avatarUrl", null);
        String cachedAge = prefs.getString("age", null);
        String cachedWeight = prefs.getString("weight", null);
        String cachedSex = prefs.getString("sex", null);

        // 🧠 Step 1: Show cached info instantly
        updateUserAfterSignIn(cachedName, cachedAvatar);

        if (cachedAge != null)
            ((TextView) findViewById(R.id.textViewAge)).setText("Age: " + cachedAge);
        if (cachedWeight != null)
            ((TextView) findViewById(R.id.textViewWeight)).setText("Weight: " + cachedWeight + " kg");
        if (cachedSex != null)
            ((TextView) findViewById(R.id.textViewSex)).setText("Sex: " + cachedSex);

        // 🧠 Step 2: Fetch updated info from Firestore in the background
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
                            String freshSex = doc.getString("sex");
                            String freshAvatar = doc.getString("avatarUrl");

                            // Update UI & cache
                            updateUserAfterSignIn(freshName, freshAvatar);
                            if (freshAge != null)
                                ((TextView) findViewById(R.id.textViewAge)).setText("Age: " + freshAge);
                            if (freshWeight != null)
                                ((TextView) findViewById(R.id.textViewWeight)).setText("Weight: " + freshWeight + " kg");
                            if (freshSex != null)
                                ((TextView) findViewById(R.id.textViewSex)).setText("Sex: " + freshSex);

                            prefs.edit()
                                    .putString("name", freshName)
                                    .putString("age", freshAge)
                                    .putString("weight", freshWeight)
                                    .putString("sex", freshSex)
                                    .putString("avatarUrl", freshAvatar)
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
                    .centerCrop()      // ensure the image fills
                    .circleCrop()      // crop into circle
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
