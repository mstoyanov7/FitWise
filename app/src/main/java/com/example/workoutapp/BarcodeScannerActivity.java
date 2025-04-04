package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;d
import okhttp3.Response;

public class BarcodeScannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start scanning immediately
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(MyCaptureActivity.class);
        integrator.setOrientationLocked(false);
        integrator.setPrompt("Scan a food product barcode");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String barcode = result.getContents();
                fetchProductData(barcode);
            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void fetchProductData(String barcode) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String url = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    JSONObject json = new JSONObject(responseData);
                    int status = json.getInt("status");
                    if (status == 1) {
                        JSONObject product = json.getJSONObject("product");

                        // Extract desired fields
                        String name = product.optString("product_name", "N/A");
                        String brand = product.optString("brands", "N/A");
                        String ingredients = product.optString("ingredients_text", "N/A");
                        JSONObject nutriments = product.optJSONObject("nutriments");

                        String calories = nutriments != null ? nutriments.optString("energy-kcal_100g", "N/A") : "N/A";
                        String sugars = nutriments != null ? nutriments.optString("sugars_100g", "N/A") : "N/A";
                        String fat = nutriments != null ? nutriments.optString("fat_100g", "N/A") : "N/A";

                        // Start new activity and pass data
                        runOnUiThread(() -> {
                            Intent intent = new Intent(BarcodeScannerActivity.this, ProductDetailsActivity.class);
                            intent.putExtra("name", name);
                            intent.putExtra("brand", brand);
                            intent.putExtra("ingredients", ingredients);
                            intent.putExtra("calories", calories);
                            intent.putExtra("sugar", sugars);
                            intent.putExtra("fat", fat);
                            startActivity(intent);
                            finish(); // Optional: close scanner after showing details
                        });

                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                        });
                    }

                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "HTTP error: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
