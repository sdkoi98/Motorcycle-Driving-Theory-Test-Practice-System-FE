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
import com.android.volley.toolbox.StringRequest;
import com.example.motorcycletheory.R;
import com.example.motorcycletheory.activities.LoginActivity;
import com.example.motorcycletheory.databinding.FragmentProfileBinding;
import com.example.motorcycletheory.network.ApiClient;
import com.example.motorcycletheory.utils.SessionManager;

import java.util.Map;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager sessionManager = new SessionManager(requireContext());
        String email = sessionManager.getEmail();
        String role = sessionManager.getRole();

        String displayName = deriveNameFromEmail(email);
        binding.tvDisplayName.setText(displayName);
        binding.tvRole.setText(getString(R.string.profile_role_label, role));
        binding.tvEmailValue.setText(email);
        binding.tvAvatarInitial.setText(extractInitial(displayName));

        binding.btnLogout.setOnClickListener(v -> callLogout(sessionManager));
    }

    private String deriveNameFromEmail(String email) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return getString(R.string.profile_default_name);
        }
        String prefix = email.substring(0, email.indexOf('@'));
        if (prefix.isEmpty()) {
            return getString(R.string.profile_default_name);
        }
        // Simple formatting: split by . or _ and capitalize first letter of each part.
        String[] parts = prefix.split("[._]");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
            builder.append(' ');
        }
        String name = builder.toString().trim();
        return name.isEmpty() ? prefix : name;
    }

    private String extractInitial(String name) {
        if (name == null || name.isEmpty()) {
            return "?";
        }
        return String.valueOf(Character.toUpperCase(name.charAt(0)));
    }

    private void callLogout(SessionManager sessionManager) {
        ApiClient apiClient = ApiClient.getInstance(requireContext());
        String url = apiClient.endpoint("/api/auth/logout");

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    sessionManager.clear();
                    openLogin();
                },
                error -> {
                    // Even if BE logout fails, clear local session to force re-auth.
                    sessionManager.clear();
                    Toast.makeText(requireContext(), "Đăng xuất trên thiết bị", Toast.LENGTH_SHORT).show();
                    openLogin();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return ApiClient.authHeaders(sessionManager);
            }
        };

        apiClient.getRequestQueue().add(request);
    }

    private void openLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
