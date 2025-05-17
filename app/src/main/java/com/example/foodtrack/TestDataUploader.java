package com.example.foodtrack;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class TestDataUploader {

    public static void uploadTestLogs(String userId, FirebaseFirestore db) {

        // Dátum: 2025-05-16 — lunch
        addLogEntry(db, userId, "2025-05-16", "lunch", "1fBbXn9GuJdiPK2TT5WM", 100);
        addLogEntry(db, userId, "2025-05-16", "lunch", "2WwVqkjbLaWFmie3JRam", 70);

        // Dátum: 2025-05-17 — lunch
        addLogEntry(db, userId, "2025-05-17", "lunch", "2oyc8acdLWI4vCzuPh5V", 150);
        addLogEntry(db, userId, "2025-05-17", "lunch", "2sqSlR3vGe5YkcEhWj4q", 180);
    }

    private static void addLogEntry(FirebaseFirestore db, String userId, String date,
                                    String category, String foodId, int amount) {

        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("foodRef", db.collection("foods").document(foodId));
        logEntry.put("amount", amount);

        db.collection("users")
                .document(userId)
                .collection("logs")
                .document(date)
                .collection(category)
                .add(logEntry) // random logId generálás
                .addOnSuccessListener(documentReference ->
                        System.out.println("Sikeres log hozzáadás: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        System.err.println("Hiba a log feltöltéskor: " + e.getMessage()));
    }
}
