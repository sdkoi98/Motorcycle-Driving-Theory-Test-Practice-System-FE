package com.example.motorcycletheory.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.motorcycletheory.R;
import com.example.motorcycletheory.databinding.ActivityLoginBinding;
import com.example.motorcycletheory.network.ApiClient;
import com.example.motorcycletheory.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

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
            if (!validateLoginInput(email, password)) {
                return;
            }

            doLogin(email, password);
        });

        binding.btnSignup.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private boolean validateLoginInput(String email, String password) {
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        boolean valid = true;
        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.error_required_email));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError(getString(R.string.error_required_password));
            valid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.error_password_min));
            valid = false;
        }
        return valid;
    }

    private void doLogin(String email, String password) {
        setLoading(true);

        JSONObject payload = new JSONObject();
        try {
            payload.put("email", email);
            payload.put("password", password);
        } catch (JSONException e) {
            setLoading(false);
            Toast.makeText(this, getString(R.string.error_unexpected), Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient apiClient = ApiClient.getInstance(this);
        String url = apiClient.endpoint("/api/auth/login");
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                payload,
                response -> {
                    setLoading(false);
                    String token = response.optString("token", "");
                    String responseEmail = response.optString("email", email);
                    String role = response.optString("role", "User");

                    if (token.isEmpty()) {
                        Toast.makeText(this, getString(R.string.error_unexpected), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sessionManager.saveSession(responseEmail, token, role);
                    openMain();
                },
                error -> {
                    setLoading(false);
                    String message = mapAuthError(error.networkResponse == null ? -1 : error.networkResponse.statusCode, error.networkResponse == null ? null : error.networkResponse.data);
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
        );

        apiClient.getRequestQueue().add(request);
    }

    private void setLoading(boolean loading) {
        binding.btnLogin.setEnabled(!loading);
        binding.btnSignup.setEnabled(!loading);
        binding.pbLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String mapAuthError(int statusCode, byte[] data) {
        if (statusCode == -1) {
            return getString(R.string.error_network);
        }
        String body = "";
        if (data != null && data.length > 0) {
            body = new String(data, StandardCharsets.UTF_8).trim();
        }
        if (statusCode == 401) {
            if (!body.isEmpty()) {
                return getString(R.string.login_failed_prefix, stripQuotes(body));
            }
            return getString(R.string.login_failed_credentials);
        }
        if (!body.isEmpty()) {
            return getString(R.string.login_failed_prefix, stripQuotes(body));
        }
        return getString(R.string.error_unexpected);
    }

    private String stripQuotes(String text) {
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
