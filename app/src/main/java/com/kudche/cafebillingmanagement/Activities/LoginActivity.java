package com.kudche.cafebillingmanagement.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.kudche.cafebillingmanagement.MainActivity;
import com.kudche.cafebillingmanagement.R;
import com.kudche.cafebillingmanagement.Utils.ToastUtils;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUserId, etPassword;
    private Button btnLogin;

    // Hardcoded credentials
    private static final String OWNER_ID = "1111";
    private static final String OWNER_PW = "1111";
    
    private static final String WORKER_ID = "2222";
    private static final String WORKER_PW = "2222";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if already logged in
        SharedPreferences prefs = getSharedPreferences("CafePrefs", MODE_PRIVATE);
        if (prefs.contains("userRole")) {
            startMainActivity();
            return;
        }

        etUserId = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String userId = etUserId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (userId.isEmpty() || password.isEmpty()) {
            ToastUtils.showInfo(this, "Please enter all fields");
            return;
        }

        String role = null;

        if (userId.equals(OWNER_ID) && password.equals(OWNER_PW)) {
            role = "OWNER";
        } else if (userId.equals(WORKER_ID) && password.equals(WORKER_PW)) {
            role = "WORKER";
        }

        if (role != null) {
            // Save role in SharedPreferences
            SharedPreferences.Editor editor = getSharedPreferences("CafePrefs", MODE_PRIVATE).edit();
            editor.putString("userRole", role);
            editor.putString("userId", userId);
            editor.apply();

            ToastUtils.showSuccess(this, "Login Successful as " + role);
            startMainActivity();
        } else {
            ToastUtils.showError(this, "Invalid Credentials");
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}