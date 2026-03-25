package com.example.motorcycletheory.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.motorcycletheory.R;
import com.example.motorcycletheory.databinding.ActivityExamConfirmBinding;
import com.example.motorcycletheory.network.ApiClient;
import com.example.motorcycletheory.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class ExamConfirmActivity extends AppCompatActivity {
    private ActivityExamConfirmBinding binding;
    private SessionManager sessionManager;
    private int generatedExamId = -1;
    private String generatedQuestionsJson = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExamConfirmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        prefillUserInfo();
        binding.btnPay.setOnClickListener(v -> processPayment());
    }

    private void prefillUserInfo() {
        String email = sessionManager.getEmail();
        binding.etEmail.setText(email);

        String name = deriveNameFromEmail(email);
        binding.etFullName.setText(name);
    }

    private String deriveNameFromEmail(String email) {
        if (email == null || !email.contains("@")) return "";
        String prefix = email.substring(0, email.indexOf('@'));
        String[] parts = prefix.split("[._]");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) sb.append(part.substring(1));
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    private boolean validateInput() {
        boolean valid = true;

        binding.tilFullName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);

        String name = String.valueOf(binding.etFullName.getText()).trim();
        String email = String.valueOf(binding.etEmail.getText()).trim();
        String phone = String.valueOf(binding.etPhone.getText()).trim();

        if (name.isEmpty()) {
            binding.tilFullName.setError(getString(R.string.billing_validate_name));
            valid = false;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.billing_validate_email));
            valid = false;
        }

        if (phone.isEmpty()) {
            binding.tilPhone.setError(getString(R.string.billing_validate_phone));
            valid = false;
        }

        return valid;
    }

    private void processPayment() {
        if (!validateInput()) return;

        binding.btnPay.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (binding == null) return;
            binding.progressBar.setVisibility(View.GONE);
            generateExam();
        }, 2000);
    }

    private void generateExam() {
        ApiClient apiClient = ApiClient.getInstance(this);
        String url = apiClient.endpoint("/api/exam/generate");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                response -> {
                    if (binding == null) return;
                    generatedExamId = response.optInt("examId", -1);
                    JSONArray questions = response.optJSONArray("questions");

                    if (generatedExamId <= 0 || questions == null || questions.length() == 0) {
                        Toast.makeText(this, getString(R.string.home_exam_invalid), Toast.LENGTH_SHORT).show();
                        binding.btnPay.setEnabled(true);
                        return;
                    }

                    generatedQuestionsJson = questions.toString();
                    showConfirmation();
                },
                error -> {
                    if (binding == null) return;
                    binding.btnPay.setEnabled(true);
                    String msg = getString(R.string.home_generate_error);
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        msg = getString(R.string.home_session_expired);
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected com.android.volley.Response<JSONObject> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, StandardCharsets.UTF_8);
                    return com.android.volley.Response.success(new JSONObject(jsonString),
                            com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    return com.android.volley.Response.error(new com.android.volley.VolleyError(e));
                }
            }

            @Override
            public Map<String, String> getHeaders() {
                return ApiClient.authHeaders(sessionManager);
            }
        };

        apiClient.getRequestQueue().add(request);
    }

    private void showConfirmation() {
        binding.btnPay.setVisibility(View.GONE);
        binding.cardConfirmation.setVisibility(View.VISIBLE);

        String paymentMethod = getSelectedPaymentMethod();
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        String detail = getString(R.string.billing_success_detail, generatedExamId, paymentMethod, timestamp);
        binding.tvConfirmationDetail.setText(detail);

        binding.btnStartExam.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExamTakingActivity.class);
            intent.putExtra(ExamTakingActivity.EXTRA_EXAM_ID, generatedExamId);
            intent.putExtra(ExamTakingActivity.EXTRA_QUESTIONS_JSON, generatedQuestionsJson);
            startActivity(intent);
            finish();
        });
    }

    private String getSelectedPaymentMethod() {
        int checkedId = binding.rgPaymentMethod.getCheckedRadioButtonId();
        if (checkedId == R.id.rbVnpay) return "VNPay";
        if (checkedId == R.id.rbZalopay) return "ZaloPay";
        if (checkedId == R.id.rbMomo) return "MoMo";
        return "VNPay";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
