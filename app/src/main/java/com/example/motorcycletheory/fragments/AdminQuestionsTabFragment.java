package com.example.motorcycletheory.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.motorcycletheory.R;
import com.example.motorcycletheory.adapters.AdminQuestionAdapter;
import com.example.motorcycletheory.databinding.AdminTabQuestionsBinding;
import com.example.motorcycletheory.databinding.DialogQuestionBinding;
import com.example.motorcycletheory.databinding.DialogConfirmDeleteBinding;
import com.example.motorcycletheory.databinding.DialogViewQuestionBinding;
import com.example.motorcycletheory.models.AdminQuestion;
import com.example.motorcycletheory.network.ApiClient;
import com.example.motorcycletheory.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminQuestionsTabFragment extends Fragment implements AdminQuestionAdapter.OnQuestionActionListener {
    private AdminTabQuestionsBinding binding;
    private AdminQuestionAdapter adapter;
    private SessionManager sessionManager;
    private ApiClient apiClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AdminTabQuestionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdminFragment parent = (AdminFragment) getParentFragment();
        if (parent != null) {
            sessionManager = parent.getSessionManager();
            apiClient = parent.getApiClient();
        }

        adapter = new AdminQuestionAdapter();
        adapter.setOnQuestionActionListener(this);
        binding.rvQuestions.setAdapter(adapter);

        loadQuestions();
    }

    private void loadQuestions() {
        String url = apiClient.endpoint("/api/question?page=1&pageSize=50");
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    JSONArray items = response.optJSONArray("items");
                    List<AdminQuestion> questions = parseQuestions(items);
                    if (questions.isEmpty()) {
                        binding.tvEmptyQuestions.setVisibility(View.VISIBLE);
                        binding.rvQuestions.setVisibility(View.GONE);
                    } else {
                        binding.tvEmptyQuestions.setVisibility(View.GONE);
                        binding.rvQuestions.setVisibility(View.VISIBLE);
                        adapter.submitList(questions);
                    }
                },
                error -> Toast.makeText(requireContext(), getString(R.string.admin_questions_fetch_failed), Toast.LENGTH_SHORT).show()
        );
        apiClient.getRequestQueue().add(request);
    }

    private List<AdminQuestion> parseQuestions(@Nullable JSONArray items) {
        List<AdminQuestion> questions = new ArrayList<>();
        if (items == null) return questions;
        
        for (int i = 0; i < items.length(); i++) {
            JSONObject obj = items.optJSONObject(i);
            if (obj != null) {
                questions.add(new AdminQuestion(
                        obj.optInt("questionId", -1),
                        obj.optString("content", ""),
                        obj.optString("answerA", ""),
                        obj.optString("answerB", ""),
                        obj.optString("answerC", ""),
                        obj.optString("answerD", ""),
                        obj.optString("correctAnswer", ""),
                        obj.optInt("categoryId", 0),
                        obj.optBoolean("isImportant", false)
                ));
            }
        }
        return questions;
    }

    public void showCreateQuestionDialog() {
        showQuestionDialog(null);
    }

    private void showQuestionDialog(@Nullable AdminQuestion existingQuestion) {
        DialogQuestionBinding dialogBinding = DialogQuestionBinding.inflate(getLayoutInflater());
        
        boolean isEdit = existingQuestion != null;
        if (isEdit) {
            dialogBinding.tvDialogTitle.setText(R.string.admin_dialog_edit_question);
            dialogBinding.etQuestionContent.setText(existingQuestion.getContent());
            dialogBinding.etAnswerA.setText(existingQuestion.getAnswerA());
            dialogBinding.etAnswerB.setText(existingQuestion.getAnswerB());
            dialogBinding.etAnswerC.setText(existingQuestion.getAnswerC());
            dialogBinding.etAnswerD.setText(existingQuestion.getAnswerD());
            dialogBinding.etCorrectAnswer.setText(existingQuestion.getCorrectAnswer());
            dialogBinding.etCategoryId.setText(String.valueOf(existingQuestion.getCategoryId()));
            dialogBinding.cbIsImportant.setChecked(existingQuestion.isImportant());
        } else {
            dialogBinding.tvDialogTitle.setText(R.string.admin_dialog_create_question);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnSave.setOnClickListener(v -> {
            String content = dialogBinding.etQuestionContent.getText().toString().trim();
            String answerA = dialogBinding.etAnswerA.getText().toString().trim();
            String answerB = dialogBinding.etAnswerB.getText().toString().trim();
            String answerC = dialogBinding.etAnswerC.getText().toString().trim();
            String answerD = dialogBinding.etAnswerD.getText().toString().trim();
            String correctAnswer = dialogBinding.etCorrectAnswer.getText().toString().trim();
            String categoryIdStr = dialogBinding.etCategoryId.getText().toString().trim();
            boolean isImportant = dialogBinding.cbIsImportant.isChecked();

            if (content.isEmpty() || answerA.isEmpty() || answerB.isEmpty() || 
                answerC.isEmpty() || answerD.isEmpty() || correctAnswer.isEmpty() || categoryIdStr.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            int categoryId;
            try {
                categoryId = Integer.parseInt(categoryIdStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Category ID không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject payload = new JSONObject();
                payload.put("content", content);
                payload.put("answerA", answerA);
                payload.put("answerB", answerB);
                payload.put("answerC", answerC);
                payload.put("answerD", answerD);
                payload.put("correctAnswer", correctAnswer);
                payload.put("categoryId", categoryId);
                payload.put("isImportant", isImportant);

                if (isEdit) {
                    updateQuestion(existingQuestion.getQuestionId(), payload);
                } else {
                    createQuestion(payload);
                }
                dialog.dismiss();
            } catch (JSONException e) {
                Toast.makeText(requireContext(), "Lỗi tạo JSON", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void createQuestion(JSONObject payload) {
        String url = apiClient.endpoint("/api/question");
        sendWriteRequest(Request.Method.POST, url, payload, "Tạo câu hỏi thành công");
    }

    private void updateQuestion(int questionId, JSONObject payload) {
        String url = apiClient.endpoint("/api/question/" + questionId);
        sendWriteRequest(Request.Method.PUT, url, payload, "Cập nhật câu hỏi thành công");
    }

    private void deleteQuestion(int questionId) {
        String url = apiClient.endpoint("/api/question/" + questionId);
        sendWriteRequest(Request.Method.DELETE, url, null, "Xóa câu hỏi thành công");
    }

    private void sendWriteRequest(int method, String url, @Nullable JSONObject payload, String successMessage) {
        StringRequest request = new StringRequest(
                method,
                url,
                response -> {
                    Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show();
                    loadQuestions();
                    AdminFragment parent = (AdminFragment) getParentFragment();
                    if (parent != null) {
                        parent.refreshDashboard();
                    }
                },
                error -> {
                    int status = error.networkResponse == null ? -1 : error.networkResponse.statusCode;
                    Toast.makeText(requireContext(), "Lỗi: " + status, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public byte[] getBody() {
                if (payload == null) return null;
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

    @Override
    public void onViewQuestion(AdminQuestion question) {
        DialogViewQuestionBinding dialogBinding = DialogViewQuestionBinding.inflate(getLayoutInflater());
        
        dialogBinding.tvViewQuestionId.setText(String.valueOf(question.getQuestionId()));
        dialogBinding.tvViewContent.setText(question.getContent());
        dialogBinding.tvViewAnswerA.setText("A. " + question.getAnswerA());
        dialogBinding.tvViewAnswerB.setText("B. " + question.getAnswerB());
        dialogBinding.tvViewAnswerC.setText("C. " + question.getAnswerC());
        dialogBinding.tvViewAnswerD.setText("D. " + question.getAnswerD());
        dialogBinding.tvViewCorrectAnswer.setText(question.getCorrectAnswer());
        dialogBinding.tvViewCategoryId.setText(String.valueOf(question.getCategoryId()));
        dialogBinding.tvViewImportant.setVisibility(question.isImportant() ? View.VISIBLE : View.GONE);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onEditQuestion(AdminQuestion question) {
        showQuestionDialog(question);
    }

    @Override
    public void onDeleteQuestion(AdminQuestion question) {
        DialogConfirmDeleteBinding dialogBinding = DialogConfirmDeleteBinding.inflate(getLayoutInflater());
        dialogBinding.tvDeleteMessage.setText("Bạn có chắc chắn muốn xóa câu hỏi #" + question.getQuestionId() + " không?");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnConfirmDelete.setOnClickListener(v -> {
            deleteQuestion(question.getQuestionId());
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
