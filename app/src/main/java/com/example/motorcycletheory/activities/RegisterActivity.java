package com.example.motorcycletheory.activities;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.motorcycletheory.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;
import com.example.motorcycletheory.network.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout tilUsername;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnCreateAccount;
    private View pbRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        MaterialToolbar toolbar = findViewById(R.id.toolbarRegister);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilRegisterEmail);
        tilPassword = findViewById(R.id.tilRegisterPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        pbRegister = findViewById(R.id.pbRegister);

        btnCreateAccount.setOnClickListener(v -> doSignup());
    }

    private void doSignup() {
        String username = String.valueOf(etUsername.getText()).trim();
        String email = String.valueOf(etEmail.getText()).trim();
        String password = String.valueOf(etPassword.getText()).trim();
        String confirmPassword = String.valueOf(etConfirmPassword.getText()).trim();

        if (!validateRegisterInput(username, email, password, confirmPassword)) {
            return;
        }

        setLoading(true);

        JSONObject payload = new JSONObject();
        try {
            payload.put("username", username);
            payload.put("email", email);
            payload.put("password", password);
        } catch (JSONException e) {
            setLoading(false);
            Toast.makeText(this, getString(R.string.error_unexpected), Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient apiClient = ApiClient.getInstance(this);
        String url = apiClient.endpoint("/api/auth/signup");

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    setLoading(false);
                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    setLoading(false);
                    int status = error.networkResponse == null ? -1 : error.networkResponse.statusCode;
                    byte[] data = error.networkResponse == null ? null : error.networkResponse.data;
                    String msg = mapRegisterError(status, data);
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public byte[] getBody() {
                return payload.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        apiClient.getRequestQueue().add(request);
    }

    private boolean validateRegisterInput(String username, String email, String password, String confirmPassword) {
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        boolean valid = true;
        if (username.isEmpty()) {
            tilUsername.setError(getString(R.string.error_required_username));
            valid = false;
        } else if (username.length() < 3) {
            tilUsername.setError(getString(R.string.error_username_min));
            valid = false;
        }

        if (email.isEmpty()) {
            tilEmail.setError(getString(R.string.error_required_email));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        }

        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_required_password));
            valid = false;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_min));
            valid = false;
        }

        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError(getString(R.string.error_required_confirm_password));
            valid = false;
        } else if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError(getString(R.string.error_password_not_match));
            valid = false;
        }

        return valid;
    }

    private void setLoading(boolean loading) {
        btnCreateAccount.setEnabled(!loading);
        pbRegister.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String mapRegisterError(int statusCode, byte[] data) {
        if (statusCode == -1) {
            return getString(R.string.error_network);
        }
        String body = "";
        if (data != null && data.length > 0) {
            body = new String(data, StandardCharsets.UTF_8).trim();
        }
        if (statusCode == 400) {
            return body.isEmpty() ? "Đăng ký thất bại: dữ liệu không hợp lệ" : "Đăng ký thất bại: " + stripQuotes(body);
        }
        if (!body.isEmpty()) {
            return "Đăng ký thất bại: " + stripQuotes(body);
        }
        return getString(R.string.error_unexpected);
    }

    private String stripQuotes(String text) {
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }
}
