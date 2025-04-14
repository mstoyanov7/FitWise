package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ScannedFoodActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView rvDetails;
    private SimpleAdapter adapter;
    private List<String> nutrientData;
    private List<String> ingredientData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_scanned_food);

        // Initialize views
        tabLayout = findViewById(R.id.tab_layout);
        rvDetails = findViewById(R.id.rv_details);
        rvDetails.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleAdapter(new ArrayList<>());
        rvDetails.setAdapter(adapter);

        View constraint_layout = findViewById(R.id.constraint_layout);
        constraint_layout.setOnClickListener(v -> hideKeyboardAndClearFocus());

        // Initialize the data lists for the tabs
        initializeData();

        // Set up tabs
        tabLayout.addTab(tabLayout.newTab().setText("Nutritional Values"));
        tabLayout.addTab(tabLayout.newTab().setText("Ingredients"));
        updateRecyclerViewData(nutrientData);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    updateRecyclerViewData(nutrientData);
                } else {
                    updateRecyclerViewData(ingredientData);
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void initializeData() {
        Intent intent = getIntent();

        // Original values per 100g
        float caloriesPer100g = parseFloatSafe(intent.getStringExtra("calories"));
        float proteinPer100g = parseFloatSafe(intent.getStringExtra("protein"));
        String carbs = intent.getStringExtra("carbs");
        String fat = intent.getStringExtra("fat");
        String sugars = intent.getStringExtra("sugars");

        // UI elements
        EditText etWeightInput = findViewById(R.id.tv_stat_weight_value);
        final TextView tvCalories = findViewById(R.id.tv_stat_calories_value);
        final TextView tvProtein = findViewById(R.id.tv_stat_protein_value);

        // Default weight in grams
        final float defaultWeight = 100f;
        etWeightInput.setText(String.valueOf((int) defaultWeight));

        // Function to recalculate stats and update TextViews
        final Runnable updateStats = () -> {
            String input = etWeightInput.getText().toString().trim();
            // Ако е празно, показваме placeholders, но не променяме съдържанието на EditText
            if (input.isEmpty()) {
                tvCalories.setText("0");
                tvProtein.setText("0");
                etWeightInput.setText(String.valueOf(0));
            } else {
                float weight = parseFloatSafe(input);
                float adjustedCalories = caloriesPer100g * weight / 100f;
                float adjustedProtein = proteinPer100g * weight / 100f;
                tvCalories.setText(String.format("%.0f kcal", adjustedCalories));
                tvProtein.setText(String.format("%.1f g", adjustedProtein));
            }
        };

        // Извикваме изчислението веднага при инициализация
        updateStats.run();

        etWeightInput.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                String currentText = etWeightInput.getText().toString();
                // Ако полето съдържа само "0" и натиснатият клавиш е цифра, различна от '0'
                if (currentText.equals("0")) {
                    int unicodeChar = event.getUnicodeChar();
                    if (Character.isDigit(unicodeChar) && unicodeChar != '0') {
                        // Заместваме "0" с новия въведен символ
                        String newText = String.valueOf((char)unicodeChar);
                        etWeightInput.setText(newText);
                        etWeightInput.setSelection(newText.length());
                        return true; // Консумираме събитието
                    }
                }
            }
            return false;
        });

        // Добавяме TextWatcher, който след всяка промяна обновява изходните стойности
        etWeightInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateStats.run();
            }
        });

        // Summary и заглавие
        TextView nameView = findViewById(R.id.tv_food_name);
        TextView summaryView = findViewById(R.id.tv_food_summary);
        String name = intent.getStringExtra("name");
        if (nameView != null && summaryView != null) {
            nameView.setText(name);
            summaryView.setText(String.format("%.0f kcal | ~%.0fg | %.1fg protein", caloriesPer100g, defaultWeight, proteinPer100g));
        }

        // Зареждане на изображението
        String imageUrl = intent.getStringExtra("image_url");
        ImageView ivFood = findViewById(R.id.iv_food);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.dinner)
                    .into(ivFood);
        }

        // Данни за RecyclerView (Nutrient list)
        nutrientData = new ArrayList<>();
        nutrientData.add("Calories: " + caloriesPer100g + " kcal");
        nutrientData.add("Carbs: " + carbs + " g");
        nutrientData.add("Fat: " + fat + " g");
        nutrientData.add("Sugar: " + sugars + " g");
        nutrientData.add("Protein: " + proteinPer100g + " g");

        // Данни за Ingredients
        String ingredients = intent.getStringExtra("ingredients");
        ingredientData = new ArrayList<>();
        if (ingredients != null && !ingredients.equals("N/A")) {
            for (String item : ingredients.split(",\\s*")) {
                ingredientData.add(item.trim());
            }
        } else {
            ingredientData.add("No ingredients listed");
        }
    }

    private void hideKeyboardAndClearFocus() {
        View view = getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private float parseFloatSafe(String str) {
        try {
            return Float.parseFloat(str.replace(",", "."));
        } catch (Exception e) {
            return 0f;
        }
    }

    private void updateRecyclerViewData(List<String> data) {
        adapter.updateData(data);
    }

    private static class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.ViewHolder> {
        private List<String> dataList;

        public SimpleAdapter(List<String> data) {
            this.dataList = data;
        }

        public void updateData(List<String> newData) {
            dataList.clear();
            dataList.addAll(newData);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText(dataList.get(position));
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
