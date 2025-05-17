package com.example.foodtrack;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private static final String SECRET_KEY = "h7pACEh9MXz2";
    private final Handler handler = new Handler();
    private Runnable scaleLogoRunnable;
    private FirebaseAuth mAuth;

    EditText emailEditText;
    EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Logo animáció
        ImageView logoImageView = findViewById(R.id.logoImageView);
        Animation scaleLogoAnim = AnimationUtils.loadAnimation(this, R.anim.logo_scale);
        scaleLogoRunnable = () -> {
            logoImageView.startAnimation(scaleLogoAnim);
//            handler.postDelayed(scaleLogoRunnable, 5000); // újra fusson 5 mp múlva
        };

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        mAuth = FirebaseAuth.getInstance();

    }

    public void login(View view) {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Mezők kitöltése kötelező!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){
                Log.d(LOG_TAG, "Sikeres bejelentkezés! Bejelentkezett: " + email + ", jelszó: " + password);
                startLogin();
            } else {
                Log.d(LOG_TAG, "Sikertelen bejelentkezés! " + task.getException().getMessage());
                Toast.makeText(MainActivity.this, "Sikertelen bejelentkezés!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("email", emailEditText.getText().toString());
        intent.putExtra("password", passwordEditText.getText().toString());
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RegisterActivity.RESULT_OK) {
            emailEditText.setText("");
            passwordEditText.setText("");
        }
    }

    private void startLogin(){
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("from_main", true);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, LOG_TAG + "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, LOG_TAG + "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, LOG_TAG + "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, LOG_TAG + "onPause");
        handler.removeCallbacks(scaleLogoRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, LOG_TAG + "onResume");
        handler.postDelayed(scaleLogoRunnable, 1000);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, LOG_TAG + "onRestart");
    }

}