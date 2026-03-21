package com.example.motorcycletheory.fragments;

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
import com.example.motorcycletheory.R;
import com.example.motorcycletheory.databinding.FragmentHistoryBinding;
import com.example.motorcycletheory.network.ApiClient;
import com.example.motorcycletheory.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;

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
        loadHistory();
    }

    private void loadHistory() {
        SessionManager sessionManager = new SessionManager(requireContext());
        if (!sessionManager.isLoggedIn()) {
            binding.tvHistoryHint.setText(getString(R.string.history_not_logged_in));
            return;
        }

        ApiClient apiClient = ApiClient.getInstance(requireContext());
        String url = apiClient.endpoint("/api/exam/history");

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> binding.tvHistoryHint.setText(formatHistory(response)),
                error -> {
                    String msg = getString(R.string.history_fetch_error);
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        msg = getString(R.string.history_session_expired);
                    }
                    binding.tvHistoryHint.setText(msg);
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

    private String formatHistory(JSONArray data) {
        if (data.length() == 0) {
            return getString(R.string.history_empty);
        }

        StringBuilder builder = new StringBuilder();
        builder.append(getString(R.string.history_total_format, data.length())).append("\n\n");
        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.optJSONObject(i);
            if (item == null) {
                continue;
            }
            int examId = item.optInt("examId", -1);
            int score = item.optInt("score", 0);
            boolean isPassed = item.optBoolean("isPassed", false);
            String status = isPassed ? "Đậu" : "Trượt";
            builder.append(getString(R.string.history_item_format, examId, score, status)).append("\n");
        }
        return builder.toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
