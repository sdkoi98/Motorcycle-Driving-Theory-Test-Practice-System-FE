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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.motorcycletheory.R;
import com.example.motorcycletheory.adapters.AdminUserAdapter;
import com.example.motorcycletheory.databinding.AdminTabUsersBinding;
import com.example.motorcycletheory.databinding.DialogUserBinding;
import com.example.motorcycletheory.databinding.DialogConfirmDeleteBinding;
import com.example.motorcycletheory.models.AdminUser;
import com.example.motorcycletheory.network.ApiClient;
import com.example.motorcycletheory.utils.SessionManager;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminUsersTabFragment extends Fragment implements AdminUserAdapter.OnUserActionListener {
    private AdminTabUsersBinding binding;
    private AdminUserAdapter adapter;
    private SessionManager sessionManager;
    private ApiClient apiClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AdminTabUsersBinding.inflate(inflater, container, false);
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

        adapter = new AdminUserAdapter();
        adapter.setOnUserActionListener(this);
        binding.rvUsers.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        String url = apiClient.endpoint("/api/admin/users");
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    List<AdminUser> users = parseUsers(response);
                    if (users.isEmpty()) {
                        binding.tvEmptyUsers.setVisibility(View.VISIBLE);
                        binding.rvUsers.setVisibility(View.GONE);
                    } else {
                        binding.tvEmptyUsers.setVisibility(View.GONE);
                        binding.rvUsers.setVisibility(View.VISIBLE);
                        adapter.submitList(users);
                    }
                },
                error -> Toast.makeText(requireContext(), getString(R.string.admin_users_failed), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return ApiClient.authHeaders(sessionManager);
            }
        };
        apiClient.getRequestQueue().add(request);
    }

    private List<AdminUser> parseUsers(JSONArray response) {
        List<AdminUser> users = new ArrayList<>();
        for (int i = 0; i < response.length(); i++) {
            JSONObject obj = response.optJSONObject(i);
            if (obj != null) {
                users.add(new AdminUser(
                        obj.optInt("userId", -1),
                        obj.optString("username", ""),
                        obj.optString("email", ""),
                        obj.optString("role", "User")
                ));
            }
        }
        return users;
    }

    public void showCreateUserDialog() {
        showUserDialog(null);
    }

    private void showUserDialog(@Nullable AdminUser existingUser) {
        DialogUserBinding dialogBinding = DialogUserBinding.inflate(getLayoutInflater());
        
        boolean isEdit = existingUser != null;
        if (isEdit) {
            dialogBinding.tvDialogTitle.setText(R.string.admin_dialog_edit_user);
            dialogBinding.etUsername.setText(existingUser.getUsername());
            dialogBinding.etEmail.setText(existingUser.getEmail());
            dialogBinding.etRole.setText(existingUser.getRole());
            dialogBinding.layoutPassword.setVisibility(View.GONE);
        } else {
            dialogBinding.tvDialogTitle.setText(R.string.admin_dialog_create_user);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnSave.setOnClickListener(v -> {
            String username = dialogBinding.etUsername.getText().toString().trim();
            String email = dialogBinding.etEmail.getText().toString().trim();
            String password = dialogBinding.etPassword.getText().toString().trim();
            String role = dialogBinding.etRole.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || role.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isEdit && password.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject payload = new JSONObject();
                payload.put("username", username);
                payload.put("email", email);
                payload.put("role", role);
                if (!isEdit) {
                    payload.put("password", password);
                }

                if (isEdit) {
                    updateUser(existingUser.getUserId(), payload);
                } else {
                    createUser(payload);
                }
                dialog.dismiss();
            } catch (JSONException e) {
                Toast.makeText(requireContext(), "Lỗi tạo JSON", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void createUser(JSONObject payload) {
        String url = apiClient.endpoint("/api/admin/users");
        sendWriteRequest(Request.Method.POST, url, payload, "Tạo user thành công");
    }

    private void updateUser(int userId, JSONObject payload) {
        String url = apiClient.endpoint("/api/admin/users/" + userId);
        sendWriteRequest(Request.Method.PUT, url, payload, "Cập nhật user thành công");
    }

    private void deleteUser(int userId) {
        String url = apiClient.endpoint("/api/admin/users/" + userId);
        sendWriteRequest(Request.Method.DELETE, url, null, "Xóa user thành công");
    }

    private void sendWriteRequest(int method, String url, @Nullable JSONObject payload, String successMessage) {
        StringRequest request = new StringRequest(
                method,
                url,
                response -> {
                    Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show();
                    loadUsers();
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
    public void onEditUser(AdminUser user) {
        showUserDialog(user);
    }

    @Override
    public void onDeleteUser(AdminUser user) {
        DialogConfirmDeleteBinding dialogBinding = DialogConfirmDeleteBinding.inflate(getLayoutInflater());
        dialogBinding.tvDeleteMessage.setText("Bạn có chắc chắn muốn xóa người dùng \"" + user.getUsername() + "\" không?");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnConfirmDelete.setOnClickListener(v -> {
            deleteUser(user.getUserId());
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
