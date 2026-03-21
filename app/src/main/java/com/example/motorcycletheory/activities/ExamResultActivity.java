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

        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int total = getIntent().getIntExtra(EXTRA_TOTAL_QUESTIONS, 0);
        boolean passed = getIntent().getBooleanExtra(EXTRA_PASSED, false);
        boolean failedImportant = getIntent().getBooleanExtra(EXTRA_FAILED_IMPORTANT, false);

        String summary = "Diem: " + score + "/" + total
                + "\nKet qua: " + (passed ? "PASS" : "FAIL")
                + "\nLiet cau diem liet: " + (failedImportant ? "Co" : "Khong");
        tvResultSummary.setText(summary);

        btnViewDetail.setOnClickListener(v -> loadExamDetail());
    }

    private void loadExamDetail() {
        int examId = getIntent().getIntExtra(EXTRA_EXAM_ID, -1);
        if (examId <= 0) {
            Toast.makeText(this, "Khong co examId", Toast.LENGTH_SHORT).show();
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
                    String msg = "Khong lay duoc chi tiet bai thi";
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        msg = "Khong tim thay bai thi";
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
        builder.append("Diem: ").append(response.optInt("score", 0)).append("\n");
        builder.append("Passed: ").append(response.optBoolean("passed", false) ? "Yes" : "No").append("\n\n");

        JSONArray questions = response.optJSONArray("questions");
        if (questions == null || questions.length() == 0) {
            builder.append("Khong co chi tiet cau hoi");
            return builder.toString();
        }

        int count = Math.min(questions.length(), 8);
        for (int i = 0; i < count; i++) {
            JSONObject q = questions.optJSONObject(i);
            if (q == null) {
                continue;
            }
            builder.append(i + 1)
                    .append(") Q")
                    .append(q.optInt("questionId", -1))
                    .append(" - Ban chon ")
                    .append(q.optString("userAnswer", "?"))
                    .append(" / Dung ")
                    .append(q.optString("correctAnswer", "?"))
                    .append("\n");
        }

        return builder.toString().trim();
    }
}
