package com.example.foodtrack;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodtrack.model.UserProfile;
import com.example.foodtrack.util.DecimalDigitsInputFilter;
import com.example.foodtrack.util.DefaultMenu;
import com.example.foodtrack.util.NutritionCalculator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private static final String LOG_TAG = ProfileActivity.class.getName();
    private static final String PREF_KEY = ProfileActivity.class.getPackage().toString();
    private FirebaseUser user;
    String userId;
    private FirebaseFirestore db;
    private CheckBox customCheckBox;
    private TextView sexTextView, goalTextView, activityTextView, defaultCalculationInfoTextView, customCalculationInfoTextView;
    private EditText ageEditText, heightEditText, weightEditText;
    private Spinner activitySpinner;
    private RadioGroup sexGroup, goalGroup;
    private String[] activityKeys, activityDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if(user != null){
            Log.d(LOG_TAG, "Hitelesített felhasználó!");
        } else {
            Log.d(LOG_TAG, "Nem hitelesített felhasználó!");
            finish();
        }

        userId = user.getUid();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        View rootView = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        customCheckBox = findViewById(R.id.customCheckBox);

        defaultCalculationInfoTextView = findViewById(R.id.defaultCalculationInfoTextView);
        customCalculationInfoTextView = findViewById(R.id.customCalculationInfoTextView);

        DecimalDigitsInputFilter decimalFilter = new DecimalDigitsInputFilter(3, 2);

        sexTextView = findViewById(R.id.sexTextView);
        sexGroup = findViewById(R.id.sexGroup);
        sexGroup.check(R.id.maleRadioButton);
        ageEditText = findViewById(R.id.ageEditText);
        heightEditText = findViewById(R.id.heightEditText);
        heightEditText.setFilters(new InputFilter[] { decimalFilter });
        weightEditText = findViewById(R.id.weightEditText);
        weightEditText.setFilters(new InputFilter[] { decimalFilter });
        goalTextView = findViewById(R.id.goalTextView);
        goalGroup = findViewById(R.id.goalGroup);
        goalGroup.check(R.id.maintainRadioButton);

        activityTextView = findViewById(R.id.activityTextView);
        activityKeys = getResources().getStringArray(R.array.activity_keys);
        activityDisplay = getResources().getStringArray(R.array.activity_display);

        activitySpinner = findViewById(R.id.activitySpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, activityDisplay);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activitySpinner.setAdapter(adapter);
        activitySpinner.setSelection(2);

        loadUserProfileFromFirebase();

        customCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleInputs(isChecked));
    }

    private void toggleInputs(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        int reverseVisibility = show ? View.GONE : View.VISIBLE;
        defaultCalculationInfoTextView.setVisibility(reverseVisibility);
        customCalculationInfoTextView.setVisibility(visibility);
        sexTextView.setVisibility(visibility);
        sexGroup.setVisibility(visibility);
        ageEditText.setVisibility(visibility);
        heightEditText.setVisibility(visibility);
        weightEditText.setVisibility(visibility);
        goalTextView.setVisibility(visibility);
        goalGroup.setVisibility(visibility);
        activityTextView.setVisibility(visibility);
        activitySpinner.setVisibility(visibility);
    }

    public void saveProfile(View view) {
        boolean isCustom = customCheckBox.isChecked();

        int age, calories, protein, carbs, fat;
        String sex, goal, activity;
        double height, weight;

        UserProfile profile;

        if (isCustom) {
            // Input validáció - üres mezők
            if (ageEditText.getText().toString().trim().isEmpty()
                    || heightEditText.getText().toString().trim().isEmpty()
                    || weightEditText.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Kérlek tölts ki minden mezőt!", Toast.LENGTH_SHORT).show();
                return;
            }

            int checkedSexId = sexGroup.getCheckedRadioButtonId();
            RadioButton sexRadioButton = sexGroup.findViewById(checkedSexId);
            sex = sexRadioButton.getTag().toString();

            age = Integer.parseInt(ageEditText.getText().toString());
            height = Double.parseDouble(heightEditText.getText().toString());
            weight = Double.parseDouble(weightEditText.getText().toString());

            int checkedGoalId = goalGroup.getCheckedRadioButtonId();
            RadioButton goalRadioButton = goalGroup.findViewById(checkedGoalId);
            goal = goalRadioButton.getTag().toString();

            int selectedActivityIndex = activitySpinner.getSelectedItemPosition();
            activity = activityKeys[selectedActivityIndex];

            // Input validáció - limitek
            if (age < 1 || age > 100) {
                Toast.makeText(this, "Az életkor 1 és 100 év között lehet!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (height < 1 || height > 300) {
                Toast.makeText(this, "A magasság 1cm és 300cm között lehet!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (weight < 1 || weight > 300) {
                Toast.makeText(this, "A súly 1kg és 300kg között lehet!", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Integer> result = NutritionCalculator.calculateDaily(sex, age, height, weight, goal, activity);
            calories = result.get("daily_calories") != null ? result.get("daily_calories") : 0;
            protein = result.get("daily_protein") != null ? result.get("daily_protein") : 0;
            carbs = result.get("daily_carbs") != null ? result.get("daily_carbs") : 0;
            fat = result.get("daily_fats") != null ? result.get("daily_fats") : 0;
            profile = UserProfile.createCustomProfile(sex, age, height, weight, goal, activity, calories, protein, carbs, fat);
        } else {
            profile = UserProfile.createDefaultProfile();
        }

        // Profile update
        db.collection("users").document(userId)
                .collection("userprofile").document("profile").set(profile)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil mentve", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        loadUserProfileFromFirebase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    // Menü
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (DefaultMenu.handleMenuSelection(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, LOG_TAG + "onDestroy");
    }

    public void deleteUser(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Megerősítés")
                .setMessage("Biztosan törölni szeretnéd a fiókot?")
                .setPositiveButton("Igen", (dialog, which) -> {
                    deleteUserFromFirebase();
                })
                .setNegativeButton("Nem", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void deleteUserFromFirebase() {
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> {
                    user.delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Felhasználó fiók törölve!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Fiók törlése sikertelen!", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Adatok törlése sikertelen!", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserProfileFromFirebase() {
        db.collection("users").document(userId)
                .collection("userprofile").document("profile").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                        if (profile != null) {
                            customCheckBox.setChecked(profile.isCustom);
                            toggleInputs(profile.isCustom);

                            if (profile.isCustom) {
                                // beállítjuk az értékeket
                                if (profile.sex != null && profile.sex.equalsIgnoreCase("male")) {
                                    sexGroup.check(R.id.maleRadioButton);
                                } else {
                                    sexGroup.check(R.id.femaleRadioButton);
                                }

                                ageEditText.setText(String.valueOf(profile.age));
                                heightEditText.setText(String.valueOf(profile.height));
                                weightEditText.setText(String.valueOf(profile.weight));

                                switch (profile.goal.toLowerCase()) {
                                    case "lose_weight":
                                        goalGroup.check(R.id.loseRadioButton);
                                        break;
                                    case "gain_weight":
                                        goalGroup.check(R.id.gainRadioButton);
                                        break;
                                    default:
                                        goalGroup.check(R.id.maintainRadioButton);
                                        break;
                                }

                                int index = 2; // default
                                for (int i = 0; i < activityKeys.length; i++) {
                                    if (activityKeys[i].equals(profile.activityLevel)) {
                                        index = i;
                                        break;
                                    }
                                }
                                activitySpinner.setSelection(index);
                            } else {
                                sexGroup.check(R.id.maleRadioButton);
                                ageEditText.setText("");
                                heightEditText.setText("");
                                weightEditText.setText("");
                                goalGroup.check(R.id.maintainRadioButton);
                                activitySpinner.setSelection(2);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba a profil betöltésekor!", Toast.LENGTH_SHORT).show();
                });
    }

    @SuppressWarnings("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Visszalépés letiltva – csak a saját gomb engedélyezett
        // super.onBackPressed(); // Nem hívjuk meg szándékosan!
    }

}