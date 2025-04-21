package com.example.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BarcodeScannerActivity extends AppCompatActivity {

    private static final int SHOW_REQUEST = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullscreenUtil.hideSystemUI(this);

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
                fetchProductData(result.getContents());
            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        } else if (requestCode == SHOW_REQUEST && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void fetchProductData(String barcode) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request req = new Request.Builder()
                    .url("https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json")
                    .build();
            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "HTTP error " + resp.code(), Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                JSONObject json = new JSONObject(resp.body().string());
                if (json.getInt("status") != 1) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                JSONObject prod = json.getJSONObject("product");
                String name        = prod.optString("product_name", "N/A");
                JSONObject nut     = prod.optJSONObject("nutriments");
                String calories    = nut != null ? nut.optString("energy-kcal_100g", "0") : "0";
                String sugars      = nut != null ? nut.optString("sugars_100g", "0") : "0";
                String fat         = nut != null ? nut.optString("fat_100g", "0") : "0";
                String protein     = nut != null ? nut.optString("proteins_100g", "0") : "0";
                String carbs       = nut != null ? nut.optString("carbohydrates_100g", "0") : "0";
                String ingredients = prod.optString("ingredients_text", "N/A");
                String imageUrl    = prod.optString("image_url", "");

                Bundle extras = getIntent().getExtras();

                runOnUiThread(() -> {
                    Intent intent = new Intent(this, ScannedFoodActivity.class);
                    if (extras != null) intent.putExtras(extras);
                    intent.putExtra("name",        name);
                    intent.putExtra("ingredients", ingredients);
                    intent.putExtra("calories",    calories);
                    intent.putExtra("sugars",      sugars);
                    intent.putExtra("fat",         fat);
                    intent.putExtra("protein",     protein);
                    intent.putExtra("carbs",       carbs);
                    intent.putExtra("image_url",   imageUrl);
                    startActivityForResult(intent, SHOW_REQUEST);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        }).start();
    }
}
