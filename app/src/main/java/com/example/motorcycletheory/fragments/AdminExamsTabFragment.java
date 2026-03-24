package com.example.motorcycletheory.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.motorcycletheory.R;
import com.example.motorcycletheory.activities.ExamResultActivity;
import com.example.motorcycletheory.adapters.AdminExamAdapter;
import com.example.motorcycletheory.databinding.AdminTabExamsBinding;
import com.example.motorcycletheory.databinding.DialogConfirmDeleteBinding;
import com.example.motorcycletheory.models.AdminExam;
import com.example.motorcycletheory.network.ApiClient;
import com.example.motorcycletheory.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminExamsTabFragment extends Fragment implements AdminExamAdapter.OnExamActionListener {
    private AdminTabExamsBinding binding;
    private AdminExamAdapter adapter;
    private SessionManager sessionManager;
    private ApiClient apiClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AdminTabExamsBinding.inflate(inflater, container, false);
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

        adapter = new AdminExamAdapter();
        adapter.setOnExamActionListener(this);
        binding.rvExams.setAdapter(adapter);

        loadExams();
    }

    private void loadExams() {
        String url = apiClient.endpoint("/api/exam/admin/all-exams");
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    List<AdminExam> exams = parseExams(response);
                    if (exams.isEmpty()) {
                        binding.tvEmptyExams.setVisibility(View.VISIBLE);
                        binding.rvExams.setVisibility(View.GONE);
                    } else {
                        binding.tvEmptyExams.setVisibility(View.GONE);
                        binding.rvExams.setVisibility(View.VISIBLE);
                        adapter.submitList(exams);
                    }
                },
                error -> Toast.makeText(requireContext(), getString(R.string.admin_exams_fetch_failed), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return ApiClient.authHeaders(sessionManager);
            }
        };
        apiClient.getRequestQueue().add(request);
    }

    private List<AdminExam> parseExams(JSONArray response) {
        List<AdminExam> exams = new ArrayList<>();
        for (int i = 0; i < response.length(); i++) {
            JSONObject obj = response.optJSONObject(i);
            if (obj != null) {
                exams.add(new AdminExam(
                        obj.optInt("examId", -1),
                        obj.optInt("userId", -1),
                        obj.optInt("score", 0),
                        obj.optInt("totalQuestions", 0),
                        obj.optBoolean("isPassed", false),
                        obj.optString("examDate", "")
                ));
            }
        }
        return exams;
    }

    @Override
    public void onViewExam(AdminExam exam) {
        Intent intent = new Intent(requireContext(), ExamResultActivity.class);
        intent.putExtra("examId", exam.getExamId());
        startActivity(intent);
    }

    @Override
    public void onDeleteExam(AdminExam exam) {
        DialogConfirmDeleteBinding dialogBinding = DialogConfirmDeleteBinding.inflate(getLayoutInflater());
        dialogBinding.tvDeleteMessage.setText("Bạn có chắc chắn muốn xóa bài thi #" + exam.getExamId() + " không?");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnConfirmDelete.setOnClickListener(v -> {
            deleteExam(exam.getExamId());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteExam(int examId) {
        String url = apiClient.endpoint("/api/exam/" + examId);
        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Toast.makeText(requireContext(), "Xóa bài thi thành công", Toast.LENGTH_SHORT).show();
                    loadExams();
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
            public Map<String, String> getHeaders() {
                return ApiClient.authHeaders(sessionManager);
            }
        };
        apiClient.getRequestQueue().add(request);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
