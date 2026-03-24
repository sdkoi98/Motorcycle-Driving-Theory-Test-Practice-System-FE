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
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.motorcycletheory.R;
import com.example.motorcycletheory.adapters.AdminExamAdapter;
import com.example.motorcycletheory.adapters.AdminQuestionAdapter;
import com.example.motorcycletheory.adapters.AdminUserAdapter;
import com.example.motorcycletheory.databinding.FragmentAdminBinding;
import com.example.motorcycletheory.models.AdminExam;
import com.example.motorcycletheory.models.AdminQuestion;
import com.example.motorcycletheory.models.AdminUser;
import com.example.motorcycletheory.network.ApiClient;
import com.example.motorcycletheory.utils.AdminViewPagerAdapter;
import com.example.motorcycletheory.utils.SessionManager;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminFragment extends Fragment {
    private FragmentAdminBinding binding;
    private SessionManager sessionManager;
    private ApiClient apiClient;

    // Tab fragments
    private AdminUsersTabFragment usersTabFragment;
    private AdminQuestionsTabFragment questionsTabFragment;
    private AdminExamsTabFragment examsTabFragment;

    private int currentTabPosition = 0;

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

        if (!"Admin".equalsIgnoreCase(sessionManager.getRole())) {
            Toast.makeText(requireContext(), getString(R.string.admin_no_permission), Toast.LENGTH_LONG).show();
            return;
        }

        setupViewPager();
        loadDashboardStats();

        binding.fabAdd.setOnClickListener(v -> onFabClicked());
    }

    private void setupViewPager() {
        usersTabFragment = new AdminUsersTabFragment();
        questionsTabFragment = new AdminQuestionsTabFragment();
        examsTabFragment = new AdminExamsTabFragment();

        AdminViewPagerAdapter adapter = new AdminViewPagerAdapter(this);
        adapter.addFragment(usersTabFragment, getString(R.string.admin_tab_users));
        adapter.addFragment(questionsTabFragment, getString(R.string.admin_tab_questions));
        adapter.addFragment(examsTabFragment, getString(R.string.admin_tab_exams));

        binding.viewPager.setAdapter(adapter);
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentTabPosition = position;
            }
        });

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(adapter.getPageTitle(position))
        ).attach();
    }

    private void loadDashboardStats() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String url = apiClient.endpoint("/api/admin/stats");
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    binding.progressBar.setVisibility(View.GONE);
                    updateDashboardUI(response);
                },
                error -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), getString(R.string.admin_stats_failed), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return ApiClient.authHeaders(sessionManager);
            }
        };
        apiClient.getRequestQueue().add(request);
    }

    private void updateDashboardUI(JSONObject stats) {
        binding.tvTotalUsers.setText(String.valueOf(stats.optInt("totalUsers", 0)));
        binding.tvTotalQuestions.setText(String.valueOf(stats.optInt("totalQuestions", 0)));
        binding.tvTotalImportant.setText(String.valueOf(stats.optInt("totalImportantQuestions", 0)));
        binding.tvTotalExams.setText(String.valueOf(stats.optInt("totalExams", 0)));
    }

    private void onFabClicked() {
        switch (currentTabPosition) {
            case 0: // Users tab
                usersTabFragment.showCreateUserDialog();
                break;
            case 1: // Questions tab
                questionsTabFragment.showCreateQuestionDialog();
                break;
            case 2: // Exams tab
                Toast.makeText(requireContext(), "Không thể tạo bài thi từ Admin", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void refreshDashboard() {
        loadDashboardStats();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
