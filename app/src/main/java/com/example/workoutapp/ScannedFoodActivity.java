package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        // Setup RecyclerView with a linear layout
        rvDetails.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleAdapter(new ArrayList<>());
        rvDetails.setAdapter(adapter);

        // Initialize the data lists for the tabs
        initializeData();

        // Set up tabs in the TabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Nutritional Values"));
        tabLayout.addTab(tabLayout.newTab().setText("Ingredients"));

        // Set default selection to Nutritional Values
        updateRecyclerViewData(nutrientData);

        // Add tab selection listener to switch data on tab change
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    updateRecyclerViewData(nutrientData);
                } else {
                    updateRecyclerViewData(ingredientData);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // No action needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // No action needed
            }
        });
    }

    // Initialize dummy data for the RecyclerView
    private void initializeData() {
        Intent intent = getIntent();

        // Nutrients
        String calories = intent.getStringExtra("calories");
        String carbs = intent.getStringExtra("carbs");
        String fat = intent.getStringExtra("fat");
        String sugars = intent.getStringExtra("sugars");
        String protein = intent.getStringExtra("protein");

        nutrientData = new ArrayList<>();
        nutrientData.add("Calories: " + calories + " kcal");
        nutrientData.add("Carbs: " + carbs + " g");
        nutrientData.add("Fat: " + fat + " g");
        nutrientData.add("Sugar: " + sugars + " g");
        nutrientData.add("Protein: " + protein + " g");

        TextView tvCalories = findViewById(R.id.tv_stat_calories_value);
        TextView tvWeight = findViewById(R.id.tv_stat_weight_value);
        TextView tvProtein = findViewById(R.id.tv_stat_protein_value);

        if (tvCalories != null) tvCalories.setText(calories + " kcal");
        if (tvWeight != null) tvWeight.setText("100g"); // Може да се направи динамично при нужда
        if (tvProtein != null) tvProtein.setText(protein + " g");

        // Ingredients
        String ingredients = intent.getStringExtra("ingredients");
        ingredientData = new ArrayList<>();
        if (ingredients != null && !ingredients.equals("N/A")) {
            for (String item : ingredients.split(",\\s*")) {
                ingredientData.add(item.trim());
            }
        } else {
            ingredientData.add("No ingredients listed");
        }

        // Update static UI like title and summary (optional)
        String imageUrl = intent.getStringExtra("image_url");
        ImageView ivFood = findViewById(R.id.iv_food);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.dinner) // текущият placeholder
                    .into(ivFood);
        }


        TextView nameView = findViewById(R.id.tv_food_name);
        TextView summaryView = findViewById(R.id.tv_food_summary);
        if (nameView != null && summaryView != null) {
            nameView.setText(intent.getStringExtra("name"));
            summaryView.setText(calories + " kcal | ~100g | " + protein + "g protein");
        }
    }

    // Update the RecyclerView with a new data set.
    private void updateRecyclerViewData(List<String> data) {
        adapter.updateData(data);
    }

    // Simple RecyclerView.Adapter implementation
    private static class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.ViewHolder> {
        private List<String> dataList;

        public SimpleAdapter(List<String> data) {
            this.dataList = data;
        }

        // Update the adapter data and refresh the view
        public void updateData(List<String> newData) {
            dataList.clear();
            dataList.addAll(newData);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Inflate a built-in simple list item layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String item = dataList.get(position);
            holder.textView.setText(item);
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        // ViewHolder class holding reference to the list item's TextView.
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
