package com.example.motorcycletheory.fragments;

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
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.motorcycletheory.R;
import com.example.motorcycletheory.activities.ExamTakingActivity;
import com.example.motorcycletheory.databinding.FragmentHomeBinding;
import com.example.motorcycletheory.network.ApiClient;
import com.example.motorcycletheory.utils.SessionManager;

import org.json.JSONArray;

import java.util.Map;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnGenerateExam.setOnClickListener(v -> callGenerateExam());
    }

    private void callGenerateExam() {
        SessionManager sessionManager = new SessionManager(requireContext());
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(requireContext(), "Ban chua dang nhap", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnGenerateExam.setEnabled(false);
        ApiClient apiClient = ApiClient.getInstance(requireContext());
        String url = apiClient.endpoint("/api/exam/generate");


        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                response -> {
                    binding.btnGenerateExam.setEnabled(true);
                    int examId = response.optInt("examId", -1);
                    JSONArray questions = response.optJSONArray("questions");
                    if (examId <= 0 || questions == null || questions.length() == 0) {
                        Toast.makeText(requireContext(), "De thi khong hop le", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent intent = new Intent(requireContext(), ExamTakingActivity.class);
                    intent.putExtra(ExamTakingActivity.EXTRA_EXAM_ID, examId);
                    intent.putExtra(ExamTakingActivity.EXTRA_QUESTIONS_JSON, questions.toString());
                    startActivity(intent);
                },
                error -> {
                    binding.btnGenerateExam.setEnabled(true);
                    String msg = getString(R.string.home_generate_error);
                    if (error.networkResponse != null) {
                        int code = error.networkResponse.statusCode;
                        String body = new String(error.networkResponse.data != null ? error.networkResponse.data : new byte[0]);
                        if (code == 401) {
                            msg = getString(R.string.home_session_expired);
                        } else if (!body.isEmpty()) {
                            String trimmed = body.trim();
                            if (trimmed.toLowerCase().contains("nullreferenceexception")) {
                                msg = getString(R.string.home_server_error, "NullReferenceException");
                            } else {
                                msg = trimmed;
                            }
                        }
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected com.android.volley.Response<org.json.JSONObject> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, java.nio.charset.StandardCharsets.UTF_8);
                    return com.android.volley.Response.success(new org.json.JSONObject(jsonString), com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
