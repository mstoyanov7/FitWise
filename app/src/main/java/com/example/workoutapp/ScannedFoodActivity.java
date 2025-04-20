package com.example.workoutapp;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScannedFoodActivity extends AppCompatActivity {

    private static final float INITIAL_WEIGHT = 100f;

    // per‑100g values
    private float per100Cal, per100Carbs, per100Fat, per100Sugar, per100Protein;

    private final List<String> nutrientData   = new ArrayList<>();
    private final List<String> ingredientData = new ArrayList<>();

    private TabLayout    tabLayout;
    private RecyclerView rvDetails;
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_scanned_food);
        FullscreenUtil.hideSystemUI(this);

        // intercept back gesture/button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        // — pull in Intent extras —
        Intent in = getIntent();
        final String selectedDate = in.getStringExtra("selectedDate");
        final int    mealIndex    = in.getIntExtra("mealIndex", -1);

        String name        = in.getStringExtra("name");
        String ingredients = in.getStringExtra("ingredients");
        String calStr      = in.getStringExtra("calories");
        String sugarStr    = in.getStringExtra("sugars");
        String fatStr      = in.getStringExtra("fat");
        String protStr     = in.getStringExtra("protein");
        String carbsStr    = in.getStringExtra("carbs");
        String imageUrl    = in.getStringExtra("image_url");

        // parse per‑100g floats
        per100Cal     = safeFloat(calStr);
        per100Sugar   = safeFloat(sugarStr);
        per100Fat     = safeFloat(fatStr);
        per100Protein = safeFloat(protStr);
        per100Carbs   = safeFloat(carbsStr);

        // — header UI —
        TextView tvName    = findViewById(R.id.tv_food_name);
        TextView tvSummary = findViewById(R.id.tv_food_summary);
        ImageView ivFood   = findViewById(R.id.iv_food);

        tvName.setText(name);
        tvSummary.setText(String.format(
                Locale.getDefault(),
                "%.0f kcal | ~100g | %.1f g protein",
                per100Cal, per100Protein
        ));
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.dinner)
                .into(ivFood);

        // — weight input & live stats —
        EditText etWeightInput = findViewById(R.id.tv_stat_weight_value);
        TextView tvCalories    = findViewById(R.id.tv_stat_calories_value);
        TextView tvProteinOut  = findViewById(R.id.tv_stat_protein_value);

        // initialize to 100g
        etWeightInput.setText(String.valueOf((int) INITIAL_WEIGHT));

        // zero‑default on blur → blank→"0" but keep focus
        etWeightInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && etWeightInput.getText().toString().trim().isEmpty()) {
                etWeightInput.setText("0");
                etWeightInput.requestFocus();
            }
        });

        // build initial lists
        initIngredientData(ingredients);
        initNutrientData(INITIAL_WEIGHT);

        // set initial top stats
        tvCalories  .setText(String.format(Locale.getDefault(),
                "%.0f kcal",  per100Cal     * INITIAL_WEIGHT / 100f));
        tvProteinOut.setText(String.format(Locale.getDefault(),
                "%.1f g",    per100Protein * INITIAL_WEIGHT / 100f));

        // — RecyclerView + Tabs set‑up —
        tabLayout = findViewById(R.id.tab_layout);
        rvDetails = findViewById(R.id.rv_details);
        rvDetails.setLayoutManager(new LinearLayoutManager(this));

        // adapter holds its own snapshot
        adapter = new SimpleAdapter(new ArrayList<>(nutrientData));
        rvDetails.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("Nutritional Values"));
        tabLayout.addTab(tabLayout.newTab().setText("Ingredients"));
        tabLayout.selectTab(tabLayout.getTabAt(0));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    adapter.updateData(nutrientData);
                } else {
                    adapter.updateData(ingredientData);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab t) {}
            @Override public void onTabReselected(TabLayout.Tab t) {}
        });

        // — live‑update on weight change —
        etWeightInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(Editable s) {
                // strip leading zeros
                String txt = s.toString();
                if (txt.length() > 1 && txt.startsWith("0")) {
                    s.replace(0, s.length(), txt.replaceFirst("^0+", ""));
                }
                // compute current weight
                String inStr = s.toString().trim();
                float weight = inStr.isEmpty() ? 0f : safeFloat(inStr);

                // update header stats
                tvCalories  .setText(String.format(Locale.getDefault(),
                        "%.0f kcal",  per100Cal     * weight / 100f));
                tvProteinOut.setText(String.format(Locale.getDefault(),
                        "%.1f g",    per100Protein * weight / 100f));

                // rebuild and refresh
                initNutrientData(weight);
                adapter.updateData(nutrientData);
            }
        });

        // — Scan Again button —
        Button btnNewScan = findViewById(R.id.btn_new_scan);
        btnNewScan.setOnClickListener(v -> {
            startActivity(new Intent(this, BarcodeScannerActivity.class));
            finish();
        });

        // — Add to Diary button —
        Button btnAddDiary = findViewById(R.id.btn_add_diary);
        btnAddDiary.setOnClickListener(v -> {
            String inStr = etWeightInput.getText().toString().trim();
            float weight = inStr.isEmpty() ? 0f : safeFloat(inStr);

            Intent out = new Intent();
            out.putExtra("name",          name);
            out.putExtra("calories",      String.valueOf(per100Cal     * weight / 100f));
            out.putExtra("carbs",         String.valueOf(per100Carbs   * weight / 100f));
            out.putExtra("fat",           String.valueOf(per100Fat     * weight / 100f));
            out.putExtra("protein",       String.valueOf(per100Protein * weight / 100f));
            out.putExtra("selectedDate",  selectedDate);
            out.putExtra("mealIndex",     mealIndex);
            out.putExtra("grams", weight);
            setResult(RESULT_OK, out);
            finish();
        });
    }

    private void initIngredientData(String ingredients) {
        ingredientData.clear();
        if (ingredients != null && !ingredients.equals("N/A")) {
            for (String item : ingredients.split(",\\s*")) {
                ingredientData.add(item.trim());
            }
        } else {
            ingredientData.add("No ingredients listed");
        }
    }

    private void initNutrientData(float weight) {
        nutrientData.clear();
        nutrientData.add(String.format(Locale.getDefault(),
                "Calories: %.0f kcal",   per100Cal     * weight / 100f));
        nutrientData.add(String.format(Locale.getDefault(),
                "Carbs: %.1f g",         per100Carbs   * weight / 100f));
        nutrientData.add(String.format(Locale.getDefault(),
                "Fat: %.1f g",           per100Fat     * weight / 100f));
        nutrientData.add(String.format(Locale.getDefault(),
                "Sugar: %.1f g",         per100Sugar   * weight / 100f));
        nutrientData.add(String.format(Locale.getDefault(),
                "Protein: %.1f g",       per100Protein * weight / 100f));
    }

    private float safeFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (Exception e) {
            return 0f;
        }
    }

    private static class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.VH> {
        private final List<String> list = new ArrayList<>();
        SimpleAdapter(List<String> initial) { list.addAll(initial); }
        void updateData(List<String> data) {
            list.clear(); list.addAll(data);
            notifyDataSetChanged();
        }
        @Override public VH onCreateViewHolder(android.view.ViewGroup p,int vt) {
            View v = LayoutInflater.from(p.getContext())
                    .inflate(android.R.layout.simple_list_item_1, p, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(VH h,int pos) {
            h.text1.setText(list.get(pos));
        }
        @Override public int getItemCount() { return list.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView text1;
            VH(View v) {
                super(v);
                text1 = v.findViewById(android.R.id.text1);
            }
        }
    }
}
