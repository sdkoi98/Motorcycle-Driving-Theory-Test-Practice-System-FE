package com.example.motorcycletheory.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.motorcycletheory.R;
import com.example.motorcycletheory.databinding.ActivityExamResultBinding;
import com.example.motorcycletheory.network.ApiClient;
import com.example.motorcycletheory.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class ExamResultActivity extends AppCompatActivity {
    public static final String EXTRA_EXAM_ID = "extra_exam_id";
    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_TOTAL_QUESTIONS = "extra_total_questions";
    public static final String EXTRA_PASSED = "extra_passed";
    public static final String EXTRA_FAILED_IMPORTANT = "extra_failed_important";

    private ActivityExamResultBinding binding;
    private boolean isDetailVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExamResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        displaySummary();
        setupListeners();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displaySummary() {
        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int total = getIntent().getIntExtra(EXTRA_TOTAL_QUESTIONS, 0);
        boolean passed = getIntent().getBooleanExtra(EXTRA_PASSED, false);
        boolean failedImportant = getIntent().getBooleanExtra(EXTRA_FAILED_IMPORTANT, false);

        String passStr = passed ? getString(R.string.result_pass) : getString(R.string.result_fail);
        String failedStr = failedImportant ? getString(R.string.result_yes) : getString(R.string.result_no);
        String summary = getString(R.string.result_summary, score, total, passStr, failedStr);
        binding.tvResultSummary.setText(summary);
    }

    private void setupListeners() {
        binding.btnViewDetail.setOnClickListener(v -> {
            if (isDetailVisible) {
                // Hide detail
                binding.cardDetail.setVisibility(View.GONE);
                binding.btnViewDetail.setText(R.string.view_exam_detail);
                binding.btnViewDetail.setIcon(getDrawable(android.R.drawable.ic_menu_view));
                isDetailVisible = false;
            } else {
                // Show/Load detail
                loadExamDetail();
            }
        });

        binding.btnBackToHome.setOnClickListener(v -> finish());
    }

    private void loadExamDetail() {
        int examId = getIntent().getIntExtra(EXTRA_EXAM_ID, -1);
        if (examId <= 0) {
            Toast.makeText(this, getString(R.string.result_no_exam_id), Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient apiClient = ApiClient.getInstance(this);
        SessionManager sessionManager = new SessionManager(this);
        String url = apiClient.endpoint("/api/exam/detail/" + examId);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    binding.tvDetail.setText(formatDetail(response));
                    binding.cardDetail.setVisibility(View.VISIBLE);
                    binding.btnViewDetail.setText("Ẩn chi tiết");
                    binding.btnViewDetail.setIcon(getDrawable(android.R.drawable.ic_menu_close_clear_cancel));
                    isDetailVisible = true;
                },
                error -> {
                    String msg = getString(R.string.result_fetch_failed);
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        msg = getString(R.string.result_exam_not_found);
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return ApiClient.authHeaders(sessionManager);
            }
        };

        apiClient.getRequestQueue().add(request);
    }

    private String formatDetail(JSONObject response) {
        StringBuilder builder = new StringBuilder();
        
        JSONArray questions = response.optJSONArray("questions");
        if (questions == null || questions.length() == 0) {
            builder.append(getString(R.string.result_no_questions));
            return builder.toString();
        }

        for (int i = 0; i < questions.length(); i++) {
            JSONObject q = questions.optJSONObject(i);
            if (q == null) {
                continue;
            }
            builder.append(getString(R.string.result_question_format,
                    i + 1,
                    q.optInt("questionId", -1),
                    q.optString("userAnswer", "?"),
                    q.optString("correctAnswer", "?")))
                    .append("\n");
        }

        return builder.toString().trim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
