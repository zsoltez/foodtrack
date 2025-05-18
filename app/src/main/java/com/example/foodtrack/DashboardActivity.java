package com.example.foodtrack;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodtrack.model.UserProfile;
import com.example.foodtrack.service.AlarmUtils;
import com.example.foodtrack.service.NotificationHelper;
import com.example.foodtrack.util.DefaultMenu;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class DashboardActivity extends AppCompatActivity {
    private static final String LOG_TAG = DashboardActivity.class.getName();
    private static final String PREF_KEY = DashboardActivity.class.getPackage().toString();
    private static final String SECRET_KEY = "h7pACEh9MXz2";
    private FirebaseUser user;
    private String userId;
    private FirebaseFirestore db;
    private boolean isFromMain = false;
    private LocalDate selectedDate;
    private TextView dateText;
    private ImageButton prevDayButton, nextDayButton;
    private View proteinCard, carbsCard, fatCard;
    private TextView proteinValue, carbsValue, fatValue;
    private TextView proteinText, carbsText, fatText;
    private ProgressBar proteinBar, carbsBar, fatBar;
    private View breakfastCard, lunchCard, dinnerCard, snackCard;
    private TextView breakfastCalValue, lunchCalValue, dinnerCalValue, snackCalValue, totalCaloriesValue;
    private TextView breakfastText, lunchText, dinnerText, snackText;
    private PieChart calorieDonutChart;
    private NotificationHelper mNotificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if(user != null){
            Log.d(LOG_TAG, "Hitelesített felhasználó!");
        } else {
            Log.d(LOG_TAG, "Nem hitelesített felhasználó!");
            finish();
        }

        isFromMain = getIntent().getBooleanExtra("from_main", false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        userId = user.getUid();
        selectedDate = LocalDate.now();

        if (savedInstanceState != null) {
            String savedDate = savedInstanceState.getString("selectedDate");
            if (savedDate != null) {
                selectedDate = LocalDate.parse(savedDate);
            }
        }

        setupViews();
        setupListeners();
        loadLogsForDate(selectedDate);

        // Értesítés engedélykérése
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        // Napi értesítés beállítása
        mNotificationHelper = new NotificationHelper(this);
        AlarmUtils.scheduleDailyAlarm(this);

        // Etelek feltoltese - ne futtasd
//        new TestFoodDatabaseUploader(this, db);

        // Teszt adatok feltoltese a jelenlegi userhez
//        new TestDataUploader(userId, db, LocalDate.now());

    }

    private void setupViews() {
        dateText = findViewById(R.id.currentDateText);
        prevDayButton = findViewById(R.id.prevDayButton);
        nextDayButton = findViewById(R.id.nextDayButton);

        proteinCard = findViewById(R.id.proteinProgressCard);
        carbsCard = findViewById(R.id.carbsProgressCard);
        fatCard = findViewById(R.id.fatProgressCard);

        proteinText = proteinCard.findViewById(R.id.nutritionText);
        proteinText.setText(getString(R.string.protein));
        proteinValue = proteinCard.findViewById(R.id.nutritionValueText);
        proteinBar = proteinCard.findViewById(R.id.nutritionBar);

        carbsText = carbsCard.findViewById(R.id.nutritionText);
        carbsText.setText(getString(R.string.carb));
        carbsValue = carbsCard.findViewById(R.id.nutritionValueText);
        carbsBar = carbsCard.findViewById(R.id.nutritionBar);

        fatText = fatCard.findViewById(R.id.nutritionText);
        fatText.setText(getString(R.string.fat));
        fatValue = fatCard.findViewById(R.id.nutritionValueText);
        fatBar = fatCard.findViewById(R.id.nutritionBar);

        breakfastCard = findViewById(R.id.breakfastCard);
        breakfastText = breakfastCard.findViewById(R.id.mealNameText);
        breakfastText.setText(getString(R.string.breakfast));
        breakfastCalValue = breakfastCard.findViewById(R.id.mealCaloriesText);

        lunchCard = findViewById(R.id.lunchCard);
        lunchText = lunchCard.findViewById(R.id.mealNameText);
        lunchText.setText(getString(R.string.lunch));
        lunchCalValue = lunchCard.findViewById(R.id.mealCaloriesText);

        dinnerCard = findViewById(R.id.dinnerCard);
        dinnerText = dinnerCard.findViewById(R.id.mealNameText);
        dinnerText.setText(getString(R.string.dinner));
        dinnerCalValue = dinnerCard.findViewById(R.id.mealCaloriesText);

        snackCard = findViewById(R.id.snackCard);
        snackText = snackCard.findViewById(R.id.mealNameText);
        snackText.setText(getString(R.string.snack));
        snackCalValue = snackCard.findViewById(R.id.mealCaloriesText);

        totalCaloriesValue = findViewById(R.id.totalCaloriesText);
        calorieDonutChart = findViewById(R.id.calorieDonutChart);

        updateDateText();
    }

    private void setupListeners() {
        prevDayButton.setOnClickListener(v -> {
            selectedDate = selectedDate.minusDays(1);
            updateDateText();
            loadLogsForDate(selectedDate);
        });

        nextDayButton.setOnClickListener(v -> {
            selectedDate = selectedDate.plusDays(1);
            updateDateText();
            loadLogsForDate(selectedDate);
        });

        breakfastCard.setOnClickListener(v -> openMealLogActivity("breakfast"));
        lunchCard.setOnClickListener(v -> openMealLogActivity("lunch"));
        dinnerCard.setOnClickListener(v -> openMealLogActivity("dinner"));
        snackCard.setOnClickListener(v -> openMealLogActivity("snack"));
    }

    private void openMealLogActivity(String category) {
        Intent intent = new Intent(this, MealLogActivity.class);
        intent.putExtra("mealType", category);
        intent.putExtra("date", selectedDate.toString());
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivityForResult(intent, 101);
    }

    private void updateDateText() {
        dateText.setText(selectedDate.toString());
    }

    private void loadLogsForDate(LocalDate date) {
        db.collection("users")
                .document(userId)
                .collection("logs")
                .whereEqualTo("date", date.toString())
                .get()
                .addOnSuccessListener(logSnapshots -> {
                    if (logSnapshots.isEmpty()) {
                        loadUserProfileAndUpdateUI(0, 0, 0, 0,
                                0, 0, 0, 0);
                        return;
                    }

                    List<Task<DocumentSnapshot>> foodTasks = new ArrayList<>();
                    List<Integer> amounts = new ArrayList<>();
                    List<String> categories = new ArrayList<>();

                    for (DocumentSnapshot logDoc : logSnapshots) {
                        DocumentReference foodRef = logDoc.getDocumentReference("foodRef");
                        Double amountNumber = logDoc.getDouble("amount");
                        int amount = amountNumber != null ? amountNumber.intValue() : 0;
                        String category = logDoc.getString("category");

                        if (foodRef != null) {
                            foodTasks.add(foodRef.get());
                            amounts.add(amount);
                            categories.add(category);
                        }
                    }

                    Tasks.whenAllSuccess(foodTasks)
                            .addOnSuccessListener(results -> {
                                double totalCalories = 0.0;
                                double totalProtein = 0.0;
                                double totalCarbs = 0.0;
                                double totalFat = 0.0;
                                double totalCalorieBreakfast = 0.0;
                                double totalCalorieLunch = 0.0;
                                double totalCalorieDinner = 0.0;
                                double totalCalorieSnack = 0.0;

                                for (int i = 0; i < results.size(); i++) {
                                    DocumentSnapshot foodDoc = (DocumentSnapshot) results.get(i);
                                    int amount = amounts.get(i);
                                    String category = categories.get(i);

                                    double caloriesPer100 = getDoubleSafe(foodDoc, "calories");
                                    double proteinPer100 = getDoubleSafe(foodDoc, "protein");
                                    double carbsPer100 = getDoubleSafe(foodDoc, "carbs");
                                    double fatPer100 = getDoubleSafe(foodDoc, "fat");

                                    totalCalories += (caloriesPer100 * amount / 100.0);
                                    totalProtein += (proteinPer100 * amount / 100.0);
                                    totalCarbs += (carbsPer100 * amount / 100.0);
                                    totalFat += (fatPer100 * amount / 100.0);

                                    switch (category) {
                                        case "breakfast":
                                            totalCalorieBreakfast += (caloriesPer100 * amount / 100.0);
                                            break;
                                        case "lunch":
                                            totalCalorieLunch += (caloriesPer100 * amount / 100.0);
                                            break;
                                        case "dinner":
                                            totalCalorieDinner += (caloriesPer100 * amount / 100.0);
                                            break;
                                        default:
                                            totalCalorieSnack += (caloriesPer100 * amount / 100.0);
                                            break;
                                    }
                                }

                                loadUserProfileAndUpdateUI((int) Math.round(totalCalories),
                                        (int) Math.round(totalProtein),
                                        (int) Math.round(totalCarbs),
                                        (int) Math.round(totalFat),
                                        (int) Math.round(totalCalorieBreakfast),
                                        (int) Math.round(totalCalorieLunch),
                                        (int) Math.round(totalCalorieDinner),
                                        (int) Math.round(totalCalorieSnack));
                            })
                            .addOnFailureListener(e -> {
                                Log.e(LOG_TAG, "Hiba az étel adatok lekérésekor: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Hiba a logok lekérésekor: " + e.getMessage());
                });
    }

    private double getDoubleSafe(DocumentSnapshot doc, String key) {
        Double value = doc.getDouble(key);
        return value != null ? value : 0.0;
    }

    private void loadUserProfileAndUpdateUI(int calories, int protein, int carbs, int fat,
                                            int calorieBreakfast, int calorieLunch, int calorieDinner, int calorieSnack) {
        db.collection("users").document(userId)
                .collection("userprofile").document("profile")
                .get()
                .addOnSuccessListener(doc -> {
                    UserProfile profile = doc.toObject(UserProfile.class);
                    if (profile != null) {
                        updateUI(
                                calories, protein, carbs, fat,
                                calorieBreakfast, calorieLunch, calorieDinner, calorieSnack,
                                profile.calories, profile.protein, profile.carbs, profile.fat
                        );
                    } else {
                        Log.e(LOG_TAG, "Hiba! Nem található a profil.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Hiba a profil betöltésekor: " + e.getMessage());
                });
    }

    private void updateUI(int calories, int protein, int carbs, int fat,
                          int calorieBreakfast, int calorieLunch, int calorieDinner, int calorieSnack,
                          int goalCalories, int goalProtein, int goalCarbs, int goalFat) {

        renderPieChart(calories, goalCalories);

        String proteinText = getString(R.string.nutrition_display, protein, goalProtein);
        proteinValue.setText(proteinText);
        proteinBar.setProgress((int)(protein * 100 / goalProtein));

        String carbsText = getString(R.string.nutrition_display, carbs, goalCarbs);
        carbsValue.setText(carbsText);
        carbsBar.setProgress((int)(carbs * 100 / goalCarbs));

        String fatText = getString(R.string.nutrition_display, fat, goalFat);
        fatValue.setText(fatText);
        fatBar.setProgress((int)(fat * 100 / goalFat));

        String breakfastText = getString(R.string.mealCal_display, calorieBreakfast);
        breakfastCalValue.setText(breakfastText);

        String lunchText = getString(R.string.mealCal_display, calorieLunch);
        lunchCalValue.setText(lunchText);

        String dinnerText = getString(R.string.mealCal_display, calorieDinner);
        dinnerCalValue.setText(dinnerText);

        String snackText = getString(R.string.mealCal_display, calorieSnack);
        snackCalValue.setText(snackText);

        notificationForReachingLimit(calories, goalCalories);
    }

    private void notificationForReachingLimit(int calories, int goalCalories) {
        Log.i(LOG_TAG, "Calories: " + calories + " GoalCalories: " + goalCalories);
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        LocalDate today = LocalDate.now();
        String todayString = today.toString();
        String todayKey = "cal_goal_notified_" + todayString;

        if (!selectedDate.equals(today)) {
            return;
        }

        // Töröljük az összes korábbi napi kulcsot, ami "cal_goal_notified_"-tel kezdődik
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith("cal_goal_notified_") && !key.equals(todayKey)) {
                editor.remove(key);
            }
        }

        if (prefs.contains(todayKey) && selectedDate.equals(today) && calories < goalCalories) {
            editor.remove(todayKey);
            editor.apply();
        }

        // Értesítés csak ha még nem volt ma
        if (!prefs.contains(todayKey)) {
            if (calories >= goalCalories && calories < goalCalories + 100) {
                mNotificationHelper.calorieLimitNotification("Gratulálunk! Elérted a napi kalóriacélod!");
                editor.putString(todayKey, "reached").apply();
            } else if (calories >= goalCalories + 100) {
                mNotificationHelper.calorieLimitNotification("Vigyázz! Túllépted a napi kalóriakereted!");
                editor.putString(todayKey, "reached").apply();
            }
        }
    }

    private void renderPieChart(int calories, int goalCalories) {

        String calorieText = getString(R.string.totalCal_display, calories, goalCalories);
        totalCaloriesValue.setText(calorieText);

        int remainingCalories = goalCalories - calories;
        int remainingCaloriesMax = Math.max(goalCalories - calories, 0);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(calories, "Elfogyasztott"));
        entries.add(new PieEntry(remainingCaloriesMax, "Hátralévő"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        if (remainingCalories < 0) {
            dataSet.setColors(Color.rgb(244, 67, 54), Color.rgb(200, 200, 200));
        } else {
            dataSet.setColors(Color.rgb(76, 175, 80), Color.rgb(200, 200, 200));
        }
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        calorieDonutChart.setData(pieData);
        calorieDonutChart.setUsePercentValues(false);
        calorieDonutChart.setDrawEntryLabels(false);
        calorieDonutChart.setDescription(null);
        calorieDonutChart.getLegend().setEnabled(false);
        calorieDonutChart.setHoleRadius(75f);
        calorieDonutChart.setTransparentCircleRadius(80f);
        calorieDonutChart.setTouchEnabled(false);
        calorieDonutChart.setRotationEnabled(false);
        calorieDonutChart.setHighlightPerTapEnabled(false);
        calorieDonutChart.invalidate();
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

    @SuppressWarnings("MissingSuperCall")
    @Override
    public void onBackPressed() {
//        if (!isFromMain) {
//            super.onBackPressed();
//        }
//        Számndékosan tiltva, csak a menügombok működjenek
    }

    // Ha volt módosítás a MealLogActivityben
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == MealLogActivity.RESULT_OK) {
            Log.i(LOG_TAG, " - onActivityResult");
            loadLogsForDate(selectedDate);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, LOG_TAG + " - onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, LOG_TAG + " - onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, LOG_TAG + " - onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, LOG_TAG + " - onPause");
    }

    // pl: képernyő forgatásnál megőrzi a választott dátumot
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(LOG_TAG, LOG_TAG + " - onSaveInstanceState");
        outState.putString("selectedDate", selectedDate.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, LOG_TAG + " - onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, LOG_TAG + " - onRestart");
    }


// TODO - Heti lista exportálása
//
//    public void exportButtonPressed(View view) {
//        LocalDate today = LocalDate.now();
//        LocalDate oneWeekAgo = today.minusDays(6); // mai nap + 6 nap vissza
//
//        db.collection("users").document(userId).collection("logs")
//                .whereGreaterThanOrEqualTo("date", oneWeekAgo.toString())
//                .whereLessThanOrEqualTo("date", today.toString())
//                .get()
//                .addOnSuccessListener(logs -> {
//                    StringBuilder csvBuilder = new StringBuilder();
//                    csvBuilder.append("Dátum,Kategória,Név,Márka,Mennyiség (g),Kalória (kcal),Fehérje (g),Szénhidrát (g),Zsír (g)\n");
//
//                    List<Task<DocumentSnapshot>> foodTasks = new ArrayList<>();
//                    List<DocumentSnapshot> logDocs = new ArrayList<>();
//
//                    for (DocumentSnapshot log : logs) {
//                        DocumentReference foodRef = log.getDocumentReference("foodRef");
//                        if (foodRef != null) {
//                            foodTasks.add(foodRef.get());
//                            logDocs.add(log);
//                        }
//                    }
//
//                    Tasks.whenAllSuccess(foodTasks).addOnSuccessListener(results -> {
//                        for (int i = 0; i < results.size(); i++) {
//                            DocumentSnapshot foodDoc = (DocumentSnapshot) results.get(i);
//                            DocumentSnapshot log = logDocs.get(i);
//
//                            String date = log.getString("date");
//                            String category = log.getString("category");
//                            double amount = log.getDouble("amount") != null ? log.getDouble("amount") : 0.0;
//
//                            String name = foodDoc.getString("name");
//                            String brand = foodDoc.getString("brand");
//                            double caloriesPer100 = foodDoc.getDouble("calories") != null ? foodDoc.getDouble("calories") : 0.0;
//                            double proteinPer100 = foodDoc.getDouble("protein") != null ? foodDoc.getDouble("protein") : 0.0;
//                            double carbsPer100 = foodDoc.getDouble("carbs") != null ? foodDoc.getDouble("carbs") : 0.0;
//                            double fatPer100 = foodDoc.getDouble("fat") != null ? foodDoc.getDouble("fat") : 0.0;
//
//                            int calories = (int) Math.round(caloriesPer100 * amount / 100.0);
//                            int protein = (int) Math.round(proteinPer100 * amount / 100.0);
//                            int carbs = (int) Math.round(carbsPer100 * amount / 100.0);
//                            int fats = (int) Math.round(fatPer100 * amount / 100.0);
//
//                            csvBuilder.append(String.format(
//                                    "%s,%s,%.0f,%s,%s,%d,%d,%d,%d\n",
//                                    date, category, amount, name, brand, calories, protein, carbs, fats
//                            ));
//                        }
//
//                        // Fileba írás
//                        try {
//                            String fileName = "foodtrack_export.csv";
//                            File file = new File(getExternalFilesDir(null), fileName);
//                            FileWriter writer = new FileWriter(file);
//                            writer.write(csvBuilder.toString());
//                            writer.close();
//                            Log.i(LOG_TAG, "Export sikeres: " + file.getAbsolutePath());
//                            Toast.makeText(this, "Sikeres exportálás:\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
//                        } catch (Exception e) {
//                            Log.e(LOG_TAG, "Hiba exportálás közben: " + e.getMessage());
//                        }
//                    });
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Hiba az exportálás során, próbálja újra!", Toast.LENGTH_SHORT).show();
//                    Log.e(LOG_TAG, "Hiba exportálás közben: " + e.getMessage());
//                });
//    }
}