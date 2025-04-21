package com.example.workoutapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.ChangeBounds;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.*;
import android.transition.TransitionManager;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class Workouts extends AppCompatActivity {

    private EditText searchInput;
    private BottomNavigationView bottomNavigationView;
    private LinearLayout favoriteContainer, exercisesContainer;
    private List<Exercise> allExercises = new ArrayList<>();
    private List<String> favorites = new ArrayList<>();

    private MaterialButton btnAll, btnStrength, btnCardio, btnCalisthenics, btnYoga, btnRunning;
    private String currentCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullscreenUtil.hideSystemUI(this);
        setContentView(R.layout.workouts_page);

        loadFavoritesFromFirebase();

        searchInput = findViewById(R.id.search_input);
        favoriteContainer = findViewById(R.id.favorite_container);
        exercisesContainer = findViewById(R.id.exercises_container);

        btnAll = findViewById(R.id.btn_all);
        btnStrength = findViewById(R.id.btn_strength);
        btnCardio = findViewById(R.id.btn_cardio);
        btnCalisthenics = findViewById(R.id.btn_calisthenics);
        btnYoga = findViewById(R.id.btn_yoga);
        btnRunning = findViewById(R.id.btn_running);

        selectedCategoryButton = btnAll;
        btnAll.setChecked(true);
        renderExercises("All");

        allExercises.add(new Exercise("Push-up", "Upper", "Strength"));
        allExercises.add(new Exercise("Plank", "Core", "Strength"));
        allExercises.add(new Exercise("Bodyweight Squat", "Lower", "Calisthenics"));
        allExercises.add(new Exercise("Bench Press", "Upper Body", "Strength"));
        allExercises.add(new Exercise("Deadlift", "Lower Body", "Strength"));
        allExercises.add(new Exercise("Pull-up", "Upper Body", "Calisthenics"));
        allExercises.add(new Exercise("Lunges", "Lower Body", "Calisthenics"));
        allExercises.add(new Exercise("Light Jogging", "Cardio", "Running"));
        allExercises.add(new Exercise("Sprints", "High Intensity", "Running"));
        allExercises.add(new Exercise("Long Distance Run", "Endurance", "Running"));
        allExercises.add(new Exercise("Treadmill Run", "Indoor", "Running"));
        allExercises.add(new Exercise("Interval Running", "Interval Training", "Running"));
        allExercises.add(new Exercise("Hill Sprints", "Power", "Running"));

        renderExercises("All");

        ImageView dropdownIcon = findViewById(R.id.favorite_toggle_arrow);
        RelativeLayout favoriteToggle = findViewById(R.id.favorites_toggle);
        favoriteContainer.setVisibility(View.GONE);
        dropdownIcon.setRotation(0f);

        favoriteToggle.setOnClickListener(v -> {
            TransitionSet transitionSet = new TransitionSet()
                    .addTransition(new ChangeBounds())
                    .setDuration(180)
                    .setInterpolator(new AccelerateDecelerateInterpolator());

            TransitionManager.beginDelayedTransition((ViewGroup) favoriteContainer.getParent(), transitionSet);

            if (favoriteContainer.getVisibility() == View.GONE) {
                favoriteContainer.setVisibility(View.VISIBLE);
                dropdownIcon.animate().rotation(180f).setDuration(180).start();
            } else {
                favoriteContainer.setVisibility(View.GONE);
                dropdownIcon.animate().rotation(0f).setDuration(180).start();
            }
        });


        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();
                if (query.isEmpty()) {
                    renderExercises(currentCategory);
                    return;
                }
                exercisesContainer.removeAllViews();
                for (Exercise ex : allExercises) {
                    boolean matchesCategory = currentCategory.equals("All") || ex.category.equalsIgnoreCase(currentCategory);
                    boolean matchesQuery = ex.title.toLowerCase().contains(query);

                    if (matchesCategory && matchesQuery) {
                        View view = createExerciseView(ex, false);
                        exercisesContainer.addView(view);
                    }
                }

            }
        });

        btnAll.setOnClickListener(v -> updateSelectedCategory(btnAll, "All"));
        btnStrength.setOnClickListener(v -> updateSelectedCategory(btnStrength, "Strength"));
        btnCardio.setOnClickListener(v -> updateSelectedCategory(btnCardio, "Cardio"));
        btnCalisthenics.setOnClickListener(v -> updateSelectedCategory(btnCalisthenics, "Calisthenics"));
        btnYoga.setOnClickListener(v -> updateSelectedCategory(btnYoga, "Yoga"));
        btnRunning.setOnClickListener(v -> updateSelectedCategory(btnRunning, "Running"));

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_workout);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;
            int id = item.getItemId();

            if (id == R.id.nav_meals) {
                intent = new Intent(Workouts.this, FoodDiaryActivity.class);
            } else if (id == R.id.nav_workout) {
                intent = new Intent(Workouts.this, Workouts.class);
            } else if (id == R.id.nav_home) {
                intent = new Intent(Workouts.this, HomeActivity.class);
            } else if (id == R.id.nav_profile) {
                intent = new Intent(Workouts.this, Profile.class);
            } else if (id == R.id.nav_calendar) {
                intent = new Intent(Workouts.this, CalendarActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            }
            return false;
        });
    }

    private void loadFavoritesFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("favorites")
                .document(user.getUid())
                .collection("entries")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    favorites.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        favorites.add(doc.getId());
                    }
                    renderExercises(currentCategory);
                });
    }


    private void renderExercises(String category) {
        currentCategory = category;
        exercisesContainer.removeAllViews();
        favoriteContainer.removeAllViews();

        for (Exercise ex : allExercises) {
            if (category.equals("All") || ex.category.equalsIgnoreCase(category)) {
                View view = createExerciseView(ex, false);
                exercisesContainer.addView(view);
            }
        }

        renderFavorites(category);
    }

    private void renderFavorites(String category) {
        favoriteContainer.removeAllViews();
        boolean hasFavorites = false;

        for (String favTitle : favorites) {
            for (Exercise ex : allExercises) {
                if (ex.title.equals(favTitle) &&
                        (category.equals("All") || ex.category.equalsIgnoreCase(category))) {
                    View view = createExerciseView(ex, true);
                    favoriteContainer.addView(view);
                    hasFavorites = true;
                }
            }
        }

        if (!hasFavorites) {
            TextView msg = new TextView(this);
            msg.setText("There are no favorite exercises yet.");
            msg.setTextColor(Color.GRAY);
            msg.setPadding(32, 32, 32, 32);
            msg.setTextSize(14f);
            favoriteContainer.addView(msg);
        }
    }

    private View createExerciseView(Exercise ex, boolean isFavoriteSection) {
        LinearLayout cardLayout = new LinearLayout(this);
        cardLayout.setOrientation(LinearLayout.HORIZONTAL);
        cardLayout.setPadding(24, 24, 24, 24);
        cardLayout.setBackgroundResource(R.drawable.exercise_card_bg);
        cardLayout.setElevation(3f);

        if (!isFavoriteSection) {
            ImageView leftIcon = new ImageView(this);
            leftIcon.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
            leftIcon.setImageResource(getIconForExercise(ex.title, false));
            leftIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams iconParams = (LinearLayout.LayoutParams) leftIcon.getLayoutParams();
            iconParams.setMargins(0, 0, 24, 0);
            leftIcon.setLayoutParams(iconParams);
            cardLayout.addView(leftIcon);
        }

        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(this);
        title.setText(ex.title);
        title.setTextSize(16f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.BLACK);

        TextView subtitle = new TextView(this);
        subtitle.setText(ex.type);
        subtitle.setTextSize(14f);
        subtitle.setTextColor(Color.parseColor("#888888"));

        textContainer.addView(title);
        textContainer.addView(subtitle);

        ImageView rightIcon = new ImageView(this);
        int iconSize = isFavoriteSection ? 70 : 48;
        rightIcon.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));

        if (isFavoriteSection) {
            rightIcon.setImageResource(getIconForExercise(ex.title, true));
        } else {
            rightIcon.setImageResource(favorites.contains(ex.title) ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);

            rightIcon.setOnClickListener(v -> {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(this, "You must be signed in to favorite", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String uid = user.getUid();
                String exerciseName = ex.title;

                if (favorites.contains(exerciseName)) {
                    db.collection("favorites").document(uid)
                            .collection("entries").document(exerciseName)
                            .delete()
                            .addOnSuccessListener(unused -> {
                                favorites.remove(exerciseName);
                                renderExercises(currentCategory);
                            });
                } else {
                    db.collection("favorites").document(uid)
                            .collection("entries").document(exerciseName)
                            .set(new Exercise(ex.title, ex.type, ex.category))
                            .addOnSuccessListener(unused -> {
                                favorites.add(exerciseName);
                                renderExercises(currentCategory);
                            });
                }
            });
        }

        cardLayout.addView(textContainer);
        cardLayout.addView(rightIcon);

        cardLayout.setOnClickListener(v -> {
            if (ex.title.equalsIgnoreCase("Light Jogging")) {
                WorkoutPreviewBottomSheet bottomSheet = new WorkoutPreviewBottomSheet();
                bottomSheet.show(getSupportFragmentManager(), "WorkoutPreview");
            }
        });

        FrameLayout wrapper = new FrameLayout(this);
        LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        wrapperParams.setMargins(0, 0, 0, 30);
        wrapper.setLayoutParams(wrapperParams);
        wrapper.addView(cardLayout);

        return wrapper;
    }

    private int getIconForExercise(String title, boolean isFavorite) {
        title = title.toLowerCase();

        if (title.contains("push")) return isFavorite ? R.drawable.ic_pushup_fav : R.drawable.ic_pushup;
        if (title.contains("plank")) return isFavorite ? R.drawable.ic_plank_fav : R.drawable.ic_plank;
        if (title.contains("squat")) return isFavorite ? R.drawable.ic_squat_fav : R.drawable.ic_squat;
        if (title.contains("bench")) return isFavorite ? R.drawable.ic_benchpress_fav : R.drawable.ic_benchpress;
        if (title.contains("deadlift")) return isFavorite ? R.drawable.ic_deadlift_fav : R.drawable.ic_deadlift;
        if (title.contains("pull")) return isFavorite ? R.drawable.ic_biceps : R.drawable.ic_pullup;
        if (title.contains("lunge")) return isFavorite ? R.drawable.ic_squat_fav : R.drawable.ic_lunge;

        if (title.contains("light jogging")) return isFavorite ? R.drawable.ic_jogging_fav : R.drawable.ic_jogging;
        if (title.contains("sprint")) return isFavorite ? R.drawable.ic_jogging_fav : R.drawable.ic_jogging;
        if (title.contains("long distance")) return isFavorite ? R.drawable.ic_jogging_fav : R.drawable.ic_jogging;
        if (title.contains("treadmill")) return isFavorite ? R.drawable.ic_jogging_fav : R.drawable.ic_jogging;
        if (title.contains("interval")) return isFavorite ? R.drawable.ic_jogging_fav : R.drawable.ic_jogging;
        if (title.contains("hill")) return isFavorite ? R.drawable.ic_jogging_fav : R.drawable.ic_jogging;

        return R.drawable.ic_biceps;
    }

    private MaterialButton selectedCategoryButton = null;

    private void updateSelectedCategory(MaterialButton clickedButton, String category) {
        if (selectedCategoryButton == clickedButton) {
            clickedButton.setChecked(false);
            selectedCategoryButton = null;
            currentCategory = "All";
            renderExercises("All");
        } else {
            if (selectedCategoryButton != null) {
                selectedCategoryButton.setChecked(false);
            }
            
            clickedButton.setChecked(true);
            selectedCategoryButton = clickedButton;
            currentCategory = category;
            renderExercises(category);
        }
    }



    public static class Exercise {
        public String title;
        public String type;
        public String category;

        public Exercise() {}

        public Exercise(String title, String type, String category) {
            this.title = title;
            this.type = type;
            this.category = category;
        }
    }
}
