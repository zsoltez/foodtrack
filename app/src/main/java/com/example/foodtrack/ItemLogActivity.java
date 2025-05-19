package com.example.foodtrack;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodtrack.util.DecimalDigitsInputFilter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ItemLogActivity extends AppCompatActivity {
    private static final String LOG_TAG = ItemLogActivity.class.getName();
    private static final String PREF_KEY = ItemLogActivity.class.getPackage().toString();
    private static final String SECRET_KEY = "h7pACEh9MXz2";
    private FirebaseUser user;
    private String userId;
    private FirebaseFirestore db;
    private String logId;
    private String date;
    private String mealType;
    private Spinner foodSpinner;
    private EditText amountEditText;
    private Button saveButton;
    private List<DocumentSnapshot> foodList;
    private DocumentReference selectedFoodRef;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_log);

        user = FirebaseAuth.getInstance().getCurrentUser();

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

        db = FirebaseFirestore.getInstance();

        logId = getIntent().getStringExtra("logId");
        date = getIntent().getStringExtra("date");
        mealType = getIntent().getStringExtra("mealType");

        foodList = new ArrayList<>();

        DecimalDigitsInputFilter decimalFilter = new DecimalDigitsInputFilter(4, 0);
        amountEditText = findViewById(R.id.amountEditText);
        amountEditText.setFilters(new InputFilter[] { decimalFilter });

        foodSpinner = findViewById(R.id.foodSpinner);
        saveButton = findViewById(R.id.saveButton);

        loadFoods();

        View rootView = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private void loadFoods() {
        db.collection("foods")
                .orderBy("name")
                .get().addOnSuccessListener(query -> {
            foodList.clear();
            foodList.addAll(query.getDocuments());

            List<String> foodNames = new ArrayList<>();
            for (DocumentSnapshot doc : foodList) {
                foodNames.add(doc.getString("name") + " - " + doc.getString("brand"));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, foodNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            foodSpinner.setAdapter(adapter);
            foodSpinner.setSelection(0);

            if (logId != null) {
                loadExistingLog();
            }
        });
    }

    private void loadExistingLog() {
        db.collection("users").document(userId)
                .collection("logs").document(logId)
                .get().addOnSuccessListener(logDoc -> {
                    DocumentReference foodRef = logDoc.getDocumentReference("foodRef");
                    Double amount = logDoc.getDouble("amount");

                    if (amount != null) amountEditText.setText(String.valueOf(amount.intValue()));

                    for (int i = 0; i < foodList.size(); i++) {
                        if (foodList.get(i).getReference().equals(foodRef)) {
                            foodSpinner.setSelection(i);
                            break;
                        }
                    }
                });
    }

    public void saveItemButtonPressed(View view) {
        int selectedPosition = foodSpinner.getSelectedItemPosition();
        if (selectedPosition == -1) return;

        selectedFoodRef = foodList.get(selectedPosition).getReference();
        String amountStr = amountEditText.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Mennyiség kitöltése kötelező!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        Map<String, Object> logData = new HashMap<>();
        logData.put("foodRef", selectedFoodRef);
        logData.put("amount", amount);
        logData.put("category", mealType);
        logData.put("date", date);

        CollectionReference logsRef = db.collection("users").document(userId).collection("logs");

        if (logId != null) {
            logsRef.document(logId).set(logData)
                    .addOnSuccessListener(aVoid -> {
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Hiba a mentés során, próbálja újra!" , Toast.LENGTH_SHORT).show();
                        Log.e(LOG_TAG, "Hiba a mentés során: " + e.getMessage());
                    });
        } else {
            logsRef.add(logData)
                    .addOnSuccessListener(documentReference -> {
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Hiba a mentés során, próbálja újra!" , Toast.LENGTH_SHORT).show();
                        Log.e(LOG_TAG, "Hiba a mentés során: " + e.getMessage());
                    });
        }
    }

    public void backButtonPressed(View view) {
        finish();
    }

    public void scanBarcodeButtonPressed(View view) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startBarcodeScanner();
        }
    }

    private void startBarcodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Irányítsd a kamerát a vonalkódra");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBarcodeScanner(); // újraindítjuk a szkennelést
            } else {
                Toast.makeText(this, "Kameraengedély szükséges a vonalkódolvasáshoz!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Megszakítottad a beolvasást", Toast.LENGTH_SHORT).show();
            } else {
                String barcode = result.getContents();
                searchProductByBarcode(barcode);
            }
        }
    }

    private void searchProductByBarcode(String barcode) {
        db.collection("foods")
                .whereEqualTo("barcode", barcode)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot foundFood = query.getDocuments().get(0);
                        DocumentReference selectedFoodRef = foundFood.getReference();

                        for (int i = 0; i < foodList.size(); i++) {
                            if (foodList.get(i).getReference().equals(selectedFoodRef)) {
                                foodSpinner.setSelection(i);
                                Toast.makeText(this, "Termék megtalálva: " + foundFood.getString("name"), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    } else {
                        Toast.makeText(this, "A termék nem található az adatbázisban!", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba történt a keresés során!", Toast.LENGTH_SHORT).show();
                    Log.e(LOG_TAG, "Firestore hiba: " + e.getMessage());
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.i(LOG_TAG, "logId: " + logId);
//        Log.i(LOG_TAG, "date: " + date);
//        Log.i(LOG_TAG, "mealType: " + mealType);
//        Log.i(LOG_TAG, "amountEditText: " + amountEditText.getText().toString());
    }
}