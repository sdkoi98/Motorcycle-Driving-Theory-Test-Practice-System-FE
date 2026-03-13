package com.example.motorcycletheory.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.motorcycletheory.databinding.ActivityLoginBinding;
import com.example.motorcycletheory.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            openMain();
            return;
        }

        binding.btnLogin.setOnClickListener(v -> {
            String email = String.valueOf(binding.etEmail.getText()).trim();
            String password = String.valueOf(binding.etPassword.getText()).trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Nhap email va mat khau", Toast.LENGTH_SHORT).show();
                return;
            }

            // Scaffold login flow: replace with /api/auth/login call in next step.
            sessionManager.saveSession(email, "TEMP_TOKEN");
            openMain();
        });
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
