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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.motorcycletheory.R;
import com.example.motorcycletheory.activities.QuestionDetailActivity;
import com.example.motorcycletheory.adapters.QuestionBankAdapter;
import com.example.motorcycletheory.databinding.FragmentQuestionBankBinding;
import com.example.motorcycletheory.models.AdminQuestion;
import com.example.motorcycletheory.network.ApiClient;
import com.example.motorcycletheory.utils.SessionManager;
import com.example.motorcycletheory.utils.StudyCartManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuestionBankFragment extends Fragment implements QuestionBankAdapter.OnQuestionBankListener {
    private FragmentQuestionBankBinding binding;
    private QuestionBankAdapter adapter;
    private StudyCartManager cartManager;
    private List<AdminQuestion> allQuestions = new ArrayList<>();
    private String currentFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentQuestionBankBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cartManager = new StudyCartManager(requireContext());
        adapter = new QuestionBankAdapter(this, getBookmarkedIds());

        binding.rvQuestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvQuestions.setAdapter(adapter);

        setupFilters();
        loadQuestions();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.updateBookmarks(getBookmarkedIds());
    }

    private void setupFilters() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipImportant)) {
                currentFilter = "important";
            } else if (checkedIds.contains(R.id.chipBookmarked)) {
                currentFilter = "bookmarked";
            } else {
                currentFilter = "all";
            }
            applyFilter();
        });
    }

    private void loadQuestions() {
        SessionManager sessionManager = new SessionManager(requireContext());
        ApiClient apiClient = ApiClient.getInstance(requireContext());
        String url = apiClient.endpoint("/api/question?page=1&pageSize=200");

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvQuestions.setVisibility(View.GONE);
        binding.tvEmpty.setVisibility(View.GONE);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    parseQuestions(response);
                },
                error -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    showEmpty(getString(R.string.question_bank_fetch_error));
                    Toast.makeText(requireContext(), getString(R.string.question_bank_fetch_error), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected com.android.volley.Response<JSONObject> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, StandardCharsets.UTF_8);
                    return com.android.volley.Response.success(new JSONObject(jsonString),
                            com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response));
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

    private void parseQuestions(JSONObject response) {
        allQuestions.clear();
        JSONArray items = response.optJSONArray("items");
        if (items == null) {
            items = response.optJSONArray("questions");
        }
        if (items == null || items.length() == 0) {
            showEmpty(getString(R.string.question_bank_empty));
            return;
        }

        for (int i = 0; i < items.length(); i++) {
            JSONObject obj = items.optJSONObject(i);
            if (obj == null) continue;
            allQuestions.add(new AdminQuestion(
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
        applyFilter();
    }

    private void applyFilter() {
        List<AdminQuestion> filtered;
        switch (currentFilter) {
            case "important":
                filtered = new ArrayList<>();
                for (AdminQuestion q : allQuestions) {
                    if (q.isImportant()) filtered.add(q);
                }
                break;
            case "bookmarked":
                Set<Integer> bookmarked = getBookmarkedIds();
                filtered = new ArrayList<>();
                for (AdminQuestion q : allQuestions) {
                    if (bookmarked.contains(q.getQuestionId())) filtered.add(q);
                }
                break;
            default:
                filtered = new ArrayList<>(allQuestions);
                break;
        }

        if (filtered.isEmpty()) {
            showEmpty(getString(R.string.question_bank_empty));
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvQuestions.setVisibility(View.VISIBLE);
            binding.tvQuestionCount.setText(getString(R.string.question_bank_count, filtered.size()));
            adapter.submitList(filtered);
        }
    }

    private void showEmpty(String message) {
        binding.tvEmpty.setText(message);
        binding.tvEmpty.setVisibility(View.VISIBLE);
        binding.rvQuestions.setVisibility(View.GONE);
        binding.tvQuestionCount.setText("");
    }

    private Set<Integer> getBookmarkedIds() {
        Set<Integer> ids = new HashSet<>();
        for (AdminQuestion q : cartManager.getCartItems()) {
            ids.add(q.getQuestionId());
        }
        return ids;
    }

    @Override
    public void onQuestionClick(AdminQuestion question) {
        Intent intent = new Intent(requireContext(), QuestionDetailActivity.class);
        intent.putExtra(QuestionDetailActivity.EXTRA_QUESTION_ID, question.getQuestionId());
        intent.putExtra(QuestionDetailActivity.EXTRA_CONTENT, question.getContent());
        intent.putExtra(QuestionDetailActivity.EXTRA_ANSWER_A, question.getAnswerA());
        intent.putExtra(QuestionDetailActivity.EXTRA_ANSWER_B, question.getAnswerB());
        intent.putExtra(QuestionDetailActivity.EXTRA_ANSWER_C, question.getAnswerC());
        intent.putExtra(QuestionDetailActivity.EXTRA_ANSWER_D, question.getAnswerD());
        intent.putExtra(QuestionDetailActivity.EXTRA_CORRECT, question.getCorrectAnswer());
        intent.putExtra(QuestionDetailActivity.EXTRA_CATEGORY, question.getCategoryId());
        intent.putExtra(QuestionDetailActivity.EXTRA_IMPORTANT, question.isImportant());
        startActivity(intent);
    }

    @Override
    public void onBookmarkToggle(AdminQuestion question, boolean addToCart) {
        if (addToCart) {
            cartManager.addQuestion(question);
            Toast.makeText(requireContext(), getString(R.string.toast_added_to_cart), Toast.LENGTH_SHORT).show();
        } else {
            cartManager.removeQuestion(question.getQuestionId());
            Toast.makeText(requireContext(), getString(R.string.toast_removed_from_cart), Toast.LENGTH_SHORT).show();
        }
        adapter.updateBookmarks(getBookmarkedIds());
        updateCartBadge();

        if ("bookmarked".equals(currentFilter)) {
            applyFilter();
        }
    }

    private void updateCartBadge() {
        if (getActivity() instanceof com.example.motorcycletheory.activities.MainActivity) {
            ((com.example.motorcycletheory.activities.MainActivity) getActivity()).refreshCartBadge();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
