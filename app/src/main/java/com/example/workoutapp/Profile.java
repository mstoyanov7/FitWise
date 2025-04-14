package com.example.workoutapp;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Profile extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private RadioGroup radioGroupTabs;
    private TextView textViewHeader;
    private Button buttonViewAll;
    private String userName;
    private Uri userPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(acct != null) {
            userName = acct.getDisplayName();
            userPhotoUri = acct.getPhotoUrl();
            updateUserAfterSignIn(userName, userPhotoUri);
        }
        else if (firebaseUser != null) {
            userName = firebaseUser.getDisplayName();
            updateUserAfterSignIn(userName, userPhotoUri);
        }

        FullscreenUtil.hideSystemUI(this);

        radioGroupTabs = findViewById(R.id.radioGroupTabs);
        textViewHeader = findViewById(R.id.textViewHeader);
        buttonViewAll = findViewById(R.id.buttonViewAll);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        loadFragment(new WorkoutsFragment());
        textViewHeader.setText("Recent Workouts");

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
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainerProfileTabs, new Fragment())
                        .commit();
            }
        });

        buttonViewAll.setOnClickListener(v -> {
            String currentTab = textViewHeader.getText().toString();
            if (currentTab.contains("Goals")) {
                Intent intent = new Intent(Profile.this, GoalsPage.class);
                //intent.putExtra("tabName", currentTab);
                startActivity(intent);
            } else if (currentTab.contains("Meals")) {
                // Example: open meals page
                Toast.makeText(this, "Open MealsPageActivity (not implemented)", Toast.LENGTH_SHORT).show();
            } else if (currentTab.contains("Workouts")) {
                // Example: open workouts page
                Toast.makeText(this, "Open WorkoutsPageActivity (not implemented)", Toast.LENGTH_SHORT).show();
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment selectedFragment = null;
                if (id == R.id.nav_meals) {
                    return true;
                } else if (id == R.id.nav_workout) {
                    startActivity(new Intent(Profile.this, Workouts.class));
                    return true;
                } else if (id == R.id.nav_home) {
                    return true;
                } else if (id == R.id.nav_calendar) {
                    return true;
                } else if (id == R.id.nav_profile) {
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

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerProfileTabs, fragment)
                .commit();
    }

    public void updateUserAfterSignIn(String userName, Uri photoUri) {
        TextView textViewName = findViewById(R.id.textViewName);
        ImageView imageViewPhoto = findViewById(R.id.imageViewProfilePic);

        if (userName != null) {
            textViewName.setText(userName);
        }

        if (photoUri != null) {
            Glide.with(this)
                    .load(photoUri)
                    .into(imageViewPhoto);
        }
        else {
            imageViewPhoto.setImageResource(R.drawable.avatar);
        }

    }
}
