package com.example.motorcycletheory.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.motorcycletheory.R;
import com.example.motorcycletheory.network.ApiClient;
import com.example.motorcycletheory.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ExamTakingActivity extends AppCompatActivity {
    public static final String EXTRA_EXAM_ID = "extra_exam_id";
    public static final String EXTRA_QUESTIONS_JSON = "extra_questions_json";

    private TextView tvQuestionIndex;
    private TextView tvQuestionContent;
    private RadioGroup rgAnswers;
    private RadioButton rbA;
    private RadioButton rbB;
    private RadioButton rbC;
    private RadioButton rbD;
    private Button btnNextQuestion;

    private int examId;
    private JSONArray questions;
    private int currentIndex = 0;
    private final JSONObject selectedAnswers = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_taking);

        tvQuestionIndex = findViewById(R.id.tvQuestionIndex);
        tvQuestionContent = findViewById(R.id.tvQuestionContent);
        rgAnswers = findViewById(R.id.rgAnswers);
        rbA = findViewById(R.id.rbA);
        rbB = findViewById(R.id.rbB);
        rbC = findViewById(R.id.rbC);
        rbD = findViewById(R.id.rbD);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);

        examId = getIntent().getIntExtra(EXTRA_EXAM_ID, -1);
        String questionsJson = getIntent().getStringExtra(EXTRA_QUESTIONS_JSON);

        if (examId <= 0 || questionsJson == null || questionsJson.isEmpty()) {
            Toast.makeText(this, "Du lieu de thi khong hop le", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            questions = new JSONArray(questionsJson);
        } catch (JSONException e) {
            Toast.makeText(this, "Khong doc duoc cau hoi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnNextQuestion.setOnClickListener(v -> onNextClicked());
        renderQuestion();
    }

    private void renderQuestion() {
        if (currentIndex < 0 || currentIndex >= questions.length()) {
            return;
        }

        JSONObject question = questions.optJSONObject(currentIndex);
        if (question == null) {
            return;
        }

        int questionId = question.optInt("questionId", -1);
        tvQuestionIndex.setText("Cau " + (currentIndex + 1) + "/" + questions.length() + " - ID " + questionId);
        tvQuestionContent.setText(question.optString("content", ""));

        rbA.setText("A. " + question.optString("answerA", ""));
        rbB.setText("B. " + question.optString("answerB", ""));
        rbC.setText("C. " + question.optString("answerC", ""));
        rbD.setText("D. " + question.optString("answerD", ""));

        rgAnswers.clearCheck();
        String savedAnswer = selectedAnswers.optString(String.valueOf(questionId), "");
        if ("A".equals(savedAnswer)) {
            rgAnswers.check(R.id.rbA);
        } else if ("B".equals(savedAnswer)) {
            rgAnswers.check(R.id.rbB);
        } else if ("C".equals(savedAnswer)) {
            rgAnswers.check(R.id.rbC);
        } else if ("D".equals(savedAnswer)) {
            rgAnswers.check(R.id.rbD);
        }

        btnNextQuestion.setText(currentIndex == questions.length() - 1 ? getString(R.string.exam_submit) : getString(R.string.exam_next));
    }

    private void onNextClicked() {
        JSONObject question = questions.optJSONObject(currentIndex);
        if (question == null) {
            return;
        }

        int questionId = question.optInt("questionId", -1);
        String selected = getSelectedOption();
        if (selected.isEmpty()) {
            Toast.makeText(this, "Vui long chon dap an", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            selectedAnswers.put(String.valueOf(questionId), selected);
        } catch (JSONException e) {
            Toast.makeText(this, "Khong luu duoc dap an", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentIndex == questions.length() - 1) {
            submitExam();
            return;
        }

        currentIndex++;
        renderQuestion();
    }

    private String getSelectedOption() {
        int selectedId = rgAnswers.getCheckedRadioButtonId();
        if (selectedId == R.id.rbA) {
            return "A";
        }
        if (selectedId == R.id.rbB) {
            return "B";
        }
        if (selectedId == R.id.rbC) {
            return "C";
        }
        if (selectedId == R.id.rbD) {
            return "D";
        }
        return "";
    }

    private void submitExam() {
        btnNextQuestion.setEnabled(false);

        JSONObject payload = new JSONObject();
        try {
            payload.put("examId", examId);
            payload.put("answers", selectedAnswers);
        } catch (JSONException e) {
            btnNextQuestion.setEnabled(true);
            Toast.makeText(this, "Loi tao request nop bai", Toast.LENGTH_SHORT).show();
            return;
        }

        SessionManager sessionManager = new SessionManager(this);
        ApiClient apiClient = ApiClient.getInstance(this);
        String url = apiClient.endpoint("/api/exam/submit");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                payload,
                response -> {
                    btnNextQuestion.setEnabled(true);
                    Intent intent = new Intent(this, ExamResultActivity.class);
                    intent.putExtra(ExamResultActivity.EXTRA_EXAM_ID, examId);
                    intent.putExtra(ExamResultActivity.EXTRA_SCORE, response.optInt("score", 0));
                    intent.putExtra(ExamResultActivity.EXTRA_TOTAL_QUESTIONS, response.optInt("totalQuestions", questions.length()));
                    intent.putExtra(ExamResultActivity.EXTRA_PASSED, response.optBoolean("passed", false));
                    intent.putExtra(ExamResultActivity.EXTRA_FAILED_IMPORTANT, response.optBoolean("failedByImportantQuestion", false));
                    startActivity(intent);
                    finish();
                },
                error -> {
                    btnNextQuestion.setEnabled(true);
                    String msg = "Nop bai that bai";
                    if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                        msg = "Du lieu nop bai chua hop le";
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
}
