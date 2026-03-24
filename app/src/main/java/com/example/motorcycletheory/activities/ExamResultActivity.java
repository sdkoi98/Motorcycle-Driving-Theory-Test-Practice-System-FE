package com.example.motorcycletheory.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.motorcycletheory.R;
import com.example.motorcycletheory.network.ApiClient;
import com.example.motorcycletheory.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class ExamResultActivity extends AppCompatActivity {
    public static final String EXTRA_EXAM_ID = "extra_exam_id";
    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_TOTAL_QUESTIONS = "extra_total_questions";
    public static final String EXTRA_PASSED = "extra_passed";
    public static final String EXTRA_FAILED_IMPORTANT = "extra_failed_important";

    private TextView tvResultSummary;
    private TextView tvDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_result);

        tvResultSummary = findViewById(R.id.tvResultSummary);
        tvDetail = findViewById(R.id.tvDetail);
        Button btnViewDetail = findViewById(R.id.btnViewDetail);

        Button btnBackToHome = findViewById(R.id.btnBackToHome);

        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int total = getIntent().getIntExtra(EXTRA_TOTAL_QUESTIONS, 0);
        boolean passed = getIntent().getBooleanExtra(EXTRA_PASSED, false);
        boolean failedImportant = getIntent().getBooleanExtra(EXTRA_FAILED_IMPORTANT, false);

        String passStr = passed ? getString(R.string.result_pass) : getString(R.string.result_fail);
        String failedStr = failedImportant ? getString(R.string.result_yes) : getString(R.string.result_no);
        String summary = getString(R.string.result_summary, score, total, passStr, failedStr);
        tvResultSummary.setText(summary);

        btnViewDetail.setOnClickListener(v -> loadExamDetail());

        btnBackToHome.setOnClickListener(v -> {
            finish();
        });
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
                response -> tvDetail.setText(formatDetail(response)),
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
        builder.append("Exam #").append(response.optInt("examId", -1)).append("\n");
        builder.append(getString(R.string.result_summary, 
                response.optInt("score", 0), 
                response.optInt("totalQuestions", 0),
                response.optBoolean("passed", false) ? getString(R.string.result_yes) : getString(R.string.result_no),
                "N/A")).append("\n\n");

        JSONArray questions = response.optJSONArray("questions");
        if (questions == null || questions.length() == 0) {
            builder.append(getString(R.string.result_no_questions));
            return builder.toString();
        }

        int count = Math.min(questions.length(), 8);
        for (int i = 0; i < count; i++) {
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
}
