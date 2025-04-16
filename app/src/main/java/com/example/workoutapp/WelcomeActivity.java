package com.example.workoutapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WelcomeActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView avatarImageView;
    private EditText weightEditText, ageEditText;
    private RadioGroup sexRadioGroup;
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
        sexRadioGroup = findViewById(R.id.sexRadioGroup);
        finishButton = findViewById(R.id.finishButton);

        setupLoadingDialog();

        avatarImageView.setOnClickListener(v -> openGallery());

        finishButton.setOnClickListener(v -> {
            String weight = weightEditText.getText().toString().trim();
            String age = ageEditText.getText().toString().trim();
            int selectedSexId = sexRadioGroup.getCheckedRadioButtonId();

            if (weight.isEmpty() || age.isEmpty() || selectedSexId == -1) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedSex = findViewById(selectedSexId);
            String sex = selectedSex.getText().toString();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                showLoading();

                if (selectedAvatarUri != null) {
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                            .child("avatars/" + user.getUid() + ".jpg");

                    storageRef.putFile(selectedAvatarUri)
                            .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                                    .addOnSuccessListener(downloadUri -> {
                                        saveUserDataToFirestore(user, age, weight, sex, downloadUri.toString());
                                    })
                                    .addOnFailureListener(e -> {
                                        hideLoading();
                                        Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                                    }))
                            .addOnFailureListener(e -> {
                                hideLoading();
                                Toast.makeText(this, "Failed to upload avatar", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    saveUserDataToFirestore(user, age, weight, sex, null);
                }
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserDataToFirestore(FirebaseUser user, String age, String weight, String sex, @Nullable String avatarUrl) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getDisplayName());
        userData.put("age", age);
        userData.put("weight", weight);
        userData.put("sex", sex);
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

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Avatar"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedAvatarUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedAvatarUri);
                avatarImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
