package com.example.workoutapp;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProductDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        TableLayout table = findViewById(R.id.tableLayout);

        // Вземаме данните от intent
        String name = getIntent().getStringExtra("name");
        String brand = getIntent().getStringExtra("brand");
        String ingredients = getIntent().getStringExtra("ingredients");
        String calories = getIntent().getStringExtra("calories");
        String sugar = getIntent().getStringExtra("sugar");
        String fat = getIntent().getStringExtra("fat");

        addRow(table, "Име", name);
        addRow(table, "Марка", brand);
        addRow(table, "Съставки", ingredients);
        addRow(table, "Калории (100g)", calories);
        addRow(table, "Захари (100g)", sugar);
        addRow(table, "Мазнини (100g)", fat);
    }

    private void addRow(TableLayout table, String label, String value) {
        TableRow row = new TableRow(this);
        TextView key = new TextView(this);
        TextView val = new TextView(this);

        key.setText(label);
        val.setText(value);

        key.setPadding(10, 10, 20, 10);
        val.setPadding(10, 10, 10, 10);

        row.addView(key);
        row.addView(val);

        table.addView(row);
    }
}
