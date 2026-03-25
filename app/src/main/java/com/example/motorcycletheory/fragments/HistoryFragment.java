package com.example.motorcycletheory.fragments;

import android.os.Bundle;
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
import com.example.motorcycletheory.R;
import com.example.motorcycletheory.adapters.HistoryAdapter;
import com.example.motorcycletheory.databinding.FragmentHistoryBinding;
import com.example.motorcycletheory.models.ExamHistory;
import com.example.motorcycletheory.network.ApiClient;
import com.example.motorcycletheory.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    private HistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Setup RecyclerView
        adapter = new HistoryAdapter();
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHistory.setAdapter(adapter);
        
        loadHistory();
    }

    private void loadHistory() {
        SessionManager sessionManager = new SessionManager(requireContext());
        if (!sessionManager.isLoggedIn()) {
            showEmptyState(getString(R.string.history_not_logged_in));
            return;
        }

        ApiClient apiClient = ApiClient.getInstance(requireContext());
        String url = apiClient.endpoint("/api/exam/history");

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (response.length() == 0) {
                        showEmptyState(getString(R.string.history_empty));
                    } else {
                        showHistoryList(response);
                    }
                },
                error -> {
                    String msg = getString(R.string.history_fetch_error);
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        msg = getString(R.string.history_session_expired);
                    }
                    showEmptyState(msg);
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return ApiClient.authHeaders(sessionManager);
            }
        };

        apiClient.getRequestQueue().add(request);
    }

    private void showEmptyState(String message) {
        binding.tvHistoryEmpty.setText(message);
        binding.tvHistoryEmpty.setVisibility(View.VISIBLE);
        binding.rvHistory.setVisibility(View.GONE);
        }

    private void showHistoryList(JSONArray data) {
        binding.tvHistoryEmpty.setVisibility(View.GONE);
        binding.rvHistory.setVisibility(View.VISIBLE);
        
        List<ExamHistory> historyList = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.optJSONObject(i);
            if (item == null) {
                continue;
            }
            int examId = item.optInt("examId", -1);
            int score = item.optInt("score", 0);
            int totalQuestions = item.optInt("totalQuestions", 25);
            boolean isPassed = item.optBoolean("isPassed", false);
            String date = item.optString("completedAt", "");
            int duration = 15; // Mặc định 15 phút, có thể lấy từ API nếu có
            
            historyList.add(new ExamHistory(examId, score, totalQuestions, isPassed, date, duration));
        }
        
        adapter.submitList(historyList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
