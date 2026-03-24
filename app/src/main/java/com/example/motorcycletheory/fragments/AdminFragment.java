package com.example.motorcycletheory.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.motorcycletheory.R;
import com.example.motorcycletheory.databinding.FragmentAdminBinding;
import com.example.motorcycletheory.network.ApiClient;
import com.example.motorcycletheory.utils.SessionManager;
import com.example.motorcycletheory.utils.SimpleTextAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminFragment extends Fragment {
    private FragmentAdminBinding binding;
    private String statsSummary = "";
    private String scoreSummary = "";
    private String usersSummary = "";
    private String extraSummary = "";
    private SessionManager sessionManager;
    private ApiClient apiClient;
    private SimpleTextAdapter listAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        apiClient = ApiClient.getInstance(requireContext());
        listAdapter = new SimpleTextAdapter();
        binding.rvAdminData.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAdminData.setAdapter(listAdapter);

        if (!"Admin".equalsIgnoreCase(sessionManager.getRole())) {
            binding.tvAdminHint.setText(getString(R.string.admin_no_permission));
            disableAdminControls();
            return;
        }

        binding.btnRefreshDashboard.setOnClickListener(v -> loadAdminData());
        binding.btnAdminAllExams.setOnClickListener(v -> loadAllExams());

        binding.btnCreateUser.setOnClickListener(v -> createUser());
        binding.btnGetUserById.setOnClickListener(v -> getUserById());
        binding.btnUpdateUser.setOnClickListener(v -> updateUser());
        binding.btnDeleteUser.setOnClickListener(v -> deleteUser());

        binding.btnLoadQuestions.setOnClickListener(v -> loadQuestionsPage());
        binding.btnGetQuestionById.setOnClickListener(v -> getQuestionById());
        binding.btnLoadImportantQuestions.setOnClickListener(v -> loadImportantQuestions());
        binding.btnCreateQuestion.setOnClickListener(v -> createQuestion());
        binding.btnUpdateQuestion.setOnClickListener(v -> updateQuestion());
        binding.btnDeleteQuestion.setOnClickListener(v -> deleteQuestion());

        loadAdminData();
    }

    private void disableAdminControls() {
        binding.btnRefreshDashboard.setEnabled(false);
        binding.btnAdminAllExams.setEnabled(false);
        binding.btnCreateUser.setEnabled(false);
        binding.btnGetUserById.setEnabled(false);
        binding.btnUpdateUser.setEnabled(false);
        binding.btnDeleteUser.setEnabled(false);
        binding.btnLoadQuestions.setEnabled(false);
        binding.btnGetQuestionById.setEnabled(false);
        binding.btnLoadImportantQuestions.setEnabled(false);
        binding.btnCreateQuestion.setEnabled(false);
        binding.btnUpdateQuestion.setEnabled(false);
        binding.btnDeleteQuestion.setEnabled(false);
        binding.etTargetId.setEnabled(false);
        binding.etPayload.setEnabled(false);
    }

    private void loadAdminData() {
        loadStats(apiClient, sessionManager);
        loadUserScores(apiClient, sessionManager);
        loadUsers(apiClient, sessionManager);
    }

    private void loadStats(ApiClient apiClient, SessionManager sessionManager) {
        String url = apiClient.endpoint("/api/admin/stats");
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    int totalUsers = response.optInt("totalUsers", 0);
                    int totalQuestions = response.optInt("totalQuestions", 0);
                    int totalImportantQuestions = response.optInt("totalImportantQuestions", 0);
                    int totalExams = response.optInt("totalExams", 0);
                    statsSummary = getString(R.string.admin_stats_summary, totalUsers, totalQuestions, totalImportantQuestions, totalExams);
                    renderAdminSummary();
                },
                error -> {
                    statsSummary = getString(R.string.admin_stats_failed);
                    renderAdminSummary();
                    Toast.makeText(requireContext(), statsSummary, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return ApiClient.authHeaders(sessionManager);
            }
        };

        apiClient.getRequestQueue().add(request);
    }

    private void loadUserScores(ApiClient apiClient, SessionManager sessionManager) {
        String url = apiClient.endpoint("/api/admin/user-scores");
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    scoreSummary = formatTopScores(response);
                    renderAdminSummary();
                },
                error -> {
                    scoreSummary = getString(R.string.admin_scores_failed);
                    renderAdminSummary();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return ApiClient.authHeaders(sessionManager);
            }
        };

        apiClient.getRequestQueue().add(request);
    }

    private String formatTopScores(JSONArray response) {
        if (response.length() == 0) {
            return getString(R.string.admin_scores_empty);
        }

        StringBuilder builder = new StringBuilder(getString(R.string.admin_scores_top)).append("\n");
        int count = Math.min(response.length(), 5);
        for (int i = 0; i < count; i++) {
            JSONObject item = response.optJSONObject(i);
            if (item == null) {
                continue;
            }
            String username = item.optString("username", "unknown");
            int score = item.optInt("score", 0);
            builder.append(i + 1).append(". ").append(username).append(" - ").append(score).append("\n");
        }
        return builder.toString().trim();
    }

    private void loadUsers(ApiClient apiClient, SessionManager sessionManager) {
        String url = apiClient.endpoint("/api/admin/users");
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    usersSummary = formatUsers(response);
                    renderAdminSummary();
                },
                error -> {
                    usersSummary = getString(R.string.admin_users_failed);
                    renderAdminSummary();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return ApiClient.authHeaders(sessionManager);
            }
        };
        apiClient.getRequestQueue().add(request);
    }

    private String formatUsers(JSONArray response) {
        if (response.length() == 0) {
            return getString(R.string.admin_users_empty);
        }

        StringBuilder builder = new StringBuilder(getString(R.string.admin_users_top)).append("\n");
        int count = Math.min(response.length(), 5);
        for (int i = 0; i < count; i++) {
            JSONObject item = response.optJSONObject(i);
            if (item == null) {
                continue;
            }
            String username = item.optString("username", "unknown");
            String role = item.optString("role", "User");
            builder.append(i + 1).append(". ").append(username).append(" (" + role + ")\n");
        }
        return builder.toString().trim();
    }

    private void renderAdminSummary() {
        String merged = (statsSummary == null ? "" : statsSummary)
                + "\n\n"
                + (scoreSummary == null ? "" : scoreSummary)
                + "\n\n"
                + (usersSummary == null ? "" : usersSummary)
                + "\n\n"
                + (extraSummary == null ? "" : extraSummary);
        binding.tvAdminHint.setText(merged.trim());
    }

    private Integer getTargetId() {
        String raw = String.valueOf(binding.etTargetId.getText()).trim();
        if (raw.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.admin_enter_id), Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            Toast.makeText(requireContext(), getString(R.string.admin_invalid_id), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Nullable
    private JSONObject getPayload() {
        String raw = String.valueOf(binding.etPayload.getText()).trim();
        if (TextUtils.isEmpty(raw)) {
            Toast.makeText(requireContext(), getString(R.string.admin_enter_payload), Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            return new JSONObject(raw);
        } catch (JSONException e) {
            Toast.makeText(requireContext(), getString(R.string.admin_invalid_payload), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void createUser() {
        JSONObject payload = getPayload();
        if (payload == null) {
            return;
        }
        if (!validateUserPayload(payload, false)) {
            return;
        }
        String url = apiClient.endpoint("/api/admin/users");
        sendWriteRequest(Request.Method.POST, url, payload, "Create user");
    }

    private void getUserById() {
        Integer id = getTargetId();
        if (id == null) {
            return;
        }
        String url = apiClient.endpoint("/api/admin/users/" + id);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    extraSummary = "User #" + id + ":\n" + response.toString();
                    showUserList(response);
                    renderAdminSummary();
                },
                error -> {
                    int status = error.networkResponse == null ? -1 : error.networkResponse.statusCode;
                    extraSummary = getString(R.string.admin_user_fetch_failed, id, status);
                    renderAdminSummary();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return ApiClient.authHeaders(sessionManager);
            }
        };
        apiClient.getRequestQueue().add(request);
    }

    private void updateUser() {
        Integer id = getTargetId();
        JSONObject payload = getPayload();
        if (id == null || payload == null) {
            return;
        }
        if (!validateUserPayload(payload, true)) {
            return;
        }
        String url = apiClient.endpoint("/api/admin/users/" + id);
        sendWriteRequest(Request.Method.PUT, url, payload, "Update user #" + id);
    }

    private void deleteUser() {
        Integer id = getTargetId();
        if (id == null) {
            return;
        }
        String url = apiClient.endpoint("/api/admin/users/" + id);
        sendWriteRequest(Request.Method.DELETE, url, null, "Delete user #" + id);
    }

    private void createQuestion() {
        JSONObject payload = getPayload();
        if (payload == null) {
            return;
        }
        if (!validateQuestionPayload(payload)) {
            return;
        }
        String url = apiClient.endpoint("/api/question");
        sendWriteRequest(Request.Method.POST, url, payload, "Create question");
    }

    private void updateQuestion() {
        Integer id = getTargetId();
        JSONObject payload = getPayload();
        if (id == null || payload == null) {
            return;
        }
        if (!validateQuestionPayload(payload)) {
            return;
        }
        String url = apiClient.endpoint("/api/question/" + id);
        sendWriteRequest(Request.Method.PUT, url, payload, "Update question #" + id);
    }

    private void deleteQuestion() {
        Integer id = getTargetId();
        if (id == null) {
            return;
        }
        String url = apiClient.endpoint("/api/question/" + id);
        sendWriteRequest(Request.Method.DELETE, url, null, "Delete question #" + id);
    }

    private void loadQuestionsPage() {
        String url = apiClient.endpoint("/api/question?page=1&pageSize=10");
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    JSONArray items = response.optJSONArray("items");
                    int count = items == null ? 0 : items.length();
                    StringBuilder builder = new StringBuilder();
                    builder.append("Questions page 1: ").append(count).append(" items\n");
                    if (items != null) {
                        int max = Math.min(count, 5);
                        for (int i = 0; i < max; i++) {
                            JSONObject q = items.optJSONObject(i);
                            if (q == null) {
                                continue;
                            }
                            builder.append("- #")
                                    .append(q.optInt("questionId", -1))
                                    .append(" | ")
                                    .append(q.optString("correctAnswer", "?"))
                                    .append("\n");
                        }
                    }
                    showQuestionPageList(items);
                    extraSummary = builder.toString().trim();
                    renderAdminSummary();
                },
                error -> {
                    extraSummary = getString(R.string.admin_questions_fetch_failed);
                    renderAdminSummary();
                }
        );
        apiClient.getRequestQueue().add(request);
    }

    private void getQuestionById() {
        Integer id = getTargetId();
        if (id == null) {
            return;
        }
        String url = apiClient.endpoint("/api/question/" + id);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    extraSummary = "Question #" + id + ":\n" + response.toString();
                    renderAdminSummary();
                },
                error -> {
                    int status = error.networkResponse == null ? -1 : error.networkResponse.statusCode;
                    extraSummary = getString(R.string.admin_question_fetch_failed, id, status);
                    renderAdminSummary();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return ApiClient.authHeaders(sessionManager);
            }
        };
        apiClient.getRequestQueue().add(request);
    }

    private void loadImportantQuestions() {
        String url = apiClient.endpoint("/api/question/important");
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    StringBuilder builder = new StringBuilder("Important questions: ")
                            .append(response.length())
                            .append("\n");
                    int max = Math.min(response.length(), 5);
                    for (int i = 0; i < max; i++) {
                        JSONObject q = response.optJSONObject(i);
                        if (q == null) {
                            continue;
                        }
                        builder.append("- #")
                                .append(q.optInt("questionId", -1))
                                .append("\n");
                    }
                    showImportantQuestionList(response);
                    extraSummary = builder.toString().trim();
                    renderAdminSummary();
                },
                error -> {
                    extraSummary = getString(R.string.admin_important_fetch_failed);
                    renderAdminSummary();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return ApiClient.authHeaders(sessionManager);
            }
        };
        apiClient.getRequestQueue().add(request);
    }

    private void loadAllExams() {
        String url = apiClient.endpoint("/api/exam/admin/all-exams");
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    StringBuilder builder = new StringBuilder("All exams: ")
                            .append(response.length())
                            .append("\n");
                    int max = Math.min(response.length(), 5);
                    for (int i = 0; i < max; i++) {
                        JSONObject exam = response.optJSONObject(i);
                        if (exam == null) {
                            continue;
                        }
                        builder.append("- #")
                                .append(exam.optInt("examId", -1))
                                .append(" user:")
                                .append(exam.optInt("userId", -1))
                                .append("\n");
                    }
                    showExamList(response);
                    extraSummary = builder.toString().trim();
                    renderAdminSummary();
                },
                error -> {
                    extraSummary = getString(R.string.admin_exams_fetch_failed);
                    renderAdminSummary();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return ApiClient.authHeaders(sessionManager);
            }
        };
        apiClient.getRequestQueue().add(request);
    }

    private void sendWriteRequest(int method, String url, @Nullable JSONObject payload, String actionName) {
        StringRequest request = new StringRequest(
                method,
                url,
                response -> {
                    extraSummary = actionName + " OK: " + response;
                    renderAdminSummary();
                    if (url.contains("/api/admin/users")) {
                        loadUsers(apiClient, sessionManager);
                    }
                },
                error -> {
                    int status = error.networkResponse == null ? -1 : error.networkResponse.statusCode;
                    extraSummary = actionName + " FAIL (status=" + status + ")";
                    renderAdminSummary();
                    Toast.makeText(requireContext(), extraSummary, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public byte[] getBody() {
                if (payload == null) {
                    return null;
                }
                return payload.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() {
                return ApiClient.authHeaders(sessionManager);
            }
        };
        apiClient.getRequestQueue().add(request);
    }

    private boolean validateUserPayload(JSONObject payload, boolean isUpdate) {
        String[] fields = isUpdate
                ? new String[]{"username", "email", "role"}
                : new String[]{"username", "email", "password", "role"};
        for (String f : fields) {
            String value = payload.optString(f, "").trim();
            if (value.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.admin_missing_field, "user", f), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private boolean validateQuestionPayload(JSONObject payload) {
        String[] fields = new String[]{
                "content", "answerA", "answerB", "answerC", "answerD", "correctAnswer", "categoryId"
        };
        for (String f : fields) {
            if (!payload.has(f)) {
                Toast.makeText(requireContext(), getString(R.string.admin_missing_field, "question", f), Toast.LENGTH_SHORT).show();
                return false;
            }
            if (!"categoryId".equals(f)) {
                String value = payload.optString(f, "").trim();
                if (value.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.admin_empty_field, f), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        String correct = payload.optString("correctAnswer", "").trim();
        if (!("A".equals(correct) || "B".equals(correct) || "C".equals(correct) || "D".equals(correct))) {
            Toast.makeText(requireContext(), getString(R.string.admin_correct_answer_invalid), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showUserList(JSONObject user) {
        List<String> data = new ArrayList<>();
        data.add("User #" + user.optInt("userId", -1));
        data.add("username: " + user.optString("username", ""));
        data.add("email: " + user.optString("email", ""));
        data.add("role: " + user.optString("role", ""));
        listAdapter.submitList(data);
    }

    private void showQuestionPageList(@Nullable JSONArray items) {
        List<String> data = new ArrayList<>();
        if (items == null || items.length() == 0) {
            data.add(getString(R.string.admin_no_questions));
            listAdapter.submitList(data);
            return;
        }
        int max = Math.min(items.length(), 20);
        for (int i = 0; i < max; i++) {
            JSONObject q = items.optJSONObject(i);
            if (q == null) {
                continue;
            }
            data.add("#" + q.optInt("questionId", -1)
                    + " | " + q.optString("correctAnswer", "?")
                    + " | important=" + q.optBoolean("isImportant", false));
        }
        listAdapter.submitList(data);
    }

    private void showImportantQuestionList(JSONArray items) {
        List<String> data = new ArrayList<>();
        if (items.length() == 0) {
            data.add(getString(R.string.admin_no_important));
            listAdapter.submitList(data);
            return;
        }
        int max = Math.min(items.length(), 20);
        for (int i = 0; i < max; i++) {
            JSONObject q = items.optJSONObject(i);
            if (q == null) {
                continue;
            }
            data.add("Important #" + q.optInt("questionId", -1)
                    + " | " + q.optString("correctAnswer", "?"));
        }
        listAdapter.submitList(data);
    }

    private void showExamList(JSONArray exams) {
        List<String> data = new ArrayList<>();
        if (exams.length() == 0) {
            data.add(getString(R.string.admin_no_exams));
            listAdapter.submitList(data);
            return;
        }
        int max = Math.min(exams.length(), 20);
        for (int i = 0; i < max; i++) {
            JSONObject exam = exams.optJSONObject(i);
            if (exam == null) {
                continue;
            }
            data.add("Exam #" + exam.optInt("examId", -1)
                    + " | user=" + exam.optInt("userId", -1)
                    + " | totalQ=" + exam.optInt("totalQuestions", 0));
        }
        listAdapter.submitList(data);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
