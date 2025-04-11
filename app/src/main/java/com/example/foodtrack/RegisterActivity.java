package com.example.foodtrack;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final String PREF_KEY = RegisterActivity.class.getPackage().toString();
    private static final String SECRET_KEY = "h7pACEh9MXz2";
    private FirebaseAuth mAuth;

    EditText emailEditText;
    EditText passwordEditText;
    EditText passwordAgainEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String secret_key = getIntent().getStringExtra("SECRET_KEY");
        if(!secret_key.equals(SECRET_KEY)){
            finish();
        }

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordAgainEditText = findViewById(R.id.passwordAgainEditText);

        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");

        emailEditText.setText(email);
        passwordEditText.setText(password);

        mAuth = FirebaseAuth.getInstance();

    }

    public void register(View view) {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String passwordAgain = passwordAgainEditText.getText().toString();

        if(!password.equals(passwordAgain)){
            Log.e(LOG_TAG, "Nem azonos a két jelszó!");
            Toast.makeText(this, "Nem azonos a két jelszó!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!email.equals(email.trim()) || !password.equals(password.trim())){
            Log.e(LOG_TAG, "Az email / jelszó nem keződhet és/vagy végződhet szőközzel!");
            Toast.makeText(this, "Az email / jelszó nem keződhet és/vagy végződhet szőközzel!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Mezők kitöltése kötelező!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){
                Log.d(LOG_TAG, "Sikeres regisztráció.");
                Log.i(LOG_TAG, "Regisztrált email: " + email + ", jelszó: " + password);
                Toast.makeText(RegisterActivity.this, "Sikeres regisztráció! Kérlek jelentkezz be!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Log.d(LOG_TAG, "Sikertelen regisztráció. Hiba: " + task.getException().getMessage());
                Toast.makeText(RegisterActivity.this, "Sikertelen regisztráció.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void cancel(View view) {
        finish();
    }
}