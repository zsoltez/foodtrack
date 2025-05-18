package com.example.foodtrack;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodtrack.model.MealLogAdapter;
import com.example.foodtrack.model.MealLogItem;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MealLogActivity extends AppCompatActivity {
    private static final String LOG_TAG = MealLogActivity.class.getName();
    private static final String PREF_KEY = MealLogActivity.class.getPackage().toString();
    private static final String SECRET_KEY = "h7pACEh9MXz2";
    private FirebaseUser user;
    private String userId;
    private FirebaseFirestore db;
    private String date;
    private String mealType;
    private RecyclerView logRecyclerView;
    private MealLogAdapter adapter;
    private List<MealLogItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_meal_log);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if(user != null){
            Log.d(LOG_TAG, "Hitelesített felhasználó!");
        } else {
            Log.d(LOG_TAG, "Nem hitelesített felhasználó!");
            finish();
        }

        String secret_key = getIntent().getStringExtra("SECRET_KEY");
        if(!Objects.equals(secret_key, SECRET_KEY)){
            finish();
        }

        userId = user.getUid();

        date = getIntent().getStringExtra("date");
        mealType = getIntent().getStringExtra("mealType");

        logRecyclerView = findViewById(R.id.mealLogRecyclerView);
        logRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        items = new ArrayList<>();
        adapter = new MealLogAdapter(this, items);
        logRecyclerView.setAdapter(adapter);

        loadLogs();

        View rootView = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private void loadLogs() {
        db.collection("users")
                .document(userId)
                .collection("logs")
                .whereEqualTo("date", date)
                .whereEqualTo("category", mealType)
                .get()
                .addOnSuccessListener(logSnapshots -> {
                    items.clear();

                    if (logSnapshots.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    List<Task<DocumentSnapshot>> mealTasks = new ArrayList<>();
                    List<Double> amounts = new ArrayList<>();
                    List<String> logIds = new ArrayList<>();

                    for (DocumentSnapshot logDoc : logSnapshots) {
                        DocumentReference foodRef = logDoc.getDocumentReference("foodRef");
                        Double amount = logDoc.getDouble("amount");

                        if (foodRef != null && amount != null) {
                            mealTasks.add(foodRef.get());
                            amounts.add(amount);
                            logIds.add(logDoc.getId());
                        }
                    }

                    Tasks.whenAllSuccess(mealTasks)
                            .addOnSuccessListener(results -> {

                                for (int i = 0; i < results.size(); i++) {
                                    DocumentSnapshot foodDoc = (DocumentSnapshot) results.get(i);
                                    Double amount = amounts.get(i);
                                    String logId = logIds.get(i);

                                    String name = foodDoc.getString("name");
                                    String brand = foodDoc.getString("brand");
                                    double caloriesPer100 = getDoubleSafe(foodDoc, "calories");
                                    double proteinPer100 = getDoubleSafe(foodDoc, "protein");
                                    double carbsPer100 = getDoubleSafe(foodDoc, "carbs");
                                    double fatPer100 = getDoubleSafe(foodDoc, "fat");

                                    int calories = (int) Math.round(caloriesPer100 * amount / 100.0);
                                    int protein = (int) Math.round(proteinPer100 * amount / 100.0);
                                    int carbs = (int) Math.round(carbsPer100 * amount / 100.0);
                                    int fats = (int) Math.round(fatPer100 * amount / 100.0);

                                    items.add(new MealLogItem(logId, name, brand, amount.intValue(), calories, protein, carbs, fats));
                                }

                                adapter.notifyDataSetChanged();
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

    public void backButtonPressed(View view) {
        finish();
    }

    public void deleteLogItemButtonPressed(String logId) {
        int indexToRemove = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).logId.equals(logId)) {
                indexToRemove = i;
                break;
            }
        }

        if (indexToRemove != -1) {
            final int finalIndexToRemove = indexToRemove;
            View viewToAnimate = logRecyclerView.findViewHolderForAdapterPosition(finalIndexToRemove).itemView;

            viewToAnimate.animate()
                    .translationX(-viewToAnimate.getWidth())
                    .alpha(0)
                    .setDuration(300)
                    .withEndAction(() -> {
                        db.collection("users")
                                .document(userId)
                                .collection("logs")
                                .document(logId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    items.remove(finalIndexToRemove);
                                    adapter.notifyItemRemoved(finalIndexToRemove);
                                    setResult(RESULT_OK);
                                    Log.i(LOG_TAG, "Sikeres törlés ID: " + logId + " index: " + finalIndexToRemove);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(LOG_TAG, "Hiba a log törlésekor: " + e.getMessage());
                                });
                    })
                    .start();
        }
    }

    public void modifyLogItemButtonPressed(String logId) {
        Intent intent = new Intent(this, ItemLogActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        intent.putExtra("logId", logId);
        intent.putExtra("date", date);
        intent.putExtra("mealType", mealType);
        startActivityForResult(intent, 102);
    }

    public void addLogItemButtonPressed(View view) {
        Intent intent = new Intent(this, ItemLogActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        intent.putExtra("date", date);
        intent.putExtra("mealType", mealType);
        startActivityForResult(intent, 102);
    }

    // Lista frissítése ha volt item módosítás
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 102 && resultCode == ItemLogActivity.RESULT_OK) {
            loadLogs();
            setResult(RESULT_OK);
        }
    }

}