package com.example.motorcycletheory.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.motorcycletheory.R;
import com.example.motorcycletheory.models.AdminUser;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private List<AdminUser> users = new ArrayList<>();
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onEditUser(AdminUser user);
        void onDeleteUser(AdminUser user);
    }

    public void setOnUserActionListener(OnUserActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AdminUser> newUsers) {
        this.users = newUsers != null ? newUsers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminUser user = users.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserAvatar;
        private final TextView tvUserName;
        private final TextView tvUserEmail;
        private final TextView tvUserRole;
        private final ImageButton btnEditUser;
        private final ImageButton btnDeleteUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserAvatar = itemView.findViewById(R.id.tvUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            btnEditUser = itemView.findViewById(R.id.btnEditUser);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }

        public void bind(AdminUser user, OnUserActionListener listener) {
            tvUserAvatar.setText(user.getAvatarLetter());
            tvUserName.setText(user.getUsername());
            tvUserEmail.setText(user.getEmail());
            tvUserRole.setText(user.getRole());

            btnEditUser.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditUser(user);
                }
            });

            btnDeleteUser.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteUser(user);
                }
            });
        }
    }
}
