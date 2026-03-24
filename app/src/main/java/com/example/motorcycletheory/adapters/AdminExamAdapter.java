package com.example.motorcycletheory.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.motorcycletheory.R;
import com.example.motorcycletheory.models.AdminExam;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminExamAdapter extends RecyclerView.Adapter<AdminExamAdapter.ViewHolder> {

    private List<AdminExam> exams = new ArrayList<>();
    private OnExamActionListener listener;

    public interface OnExamActionListener {
        void onViewExam(AdminExam exam);
        void onDeleteExam(AdminExam exam);
    }

    public void setOnExamActionListener(OnExamActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AdminExam> newExams) {
        this.exams = newExams != null ? newExams : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_exam, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminExam exam = exams.get(position);
        holder.bind(exam, listener);
    }

    @Override
    public int getItemCount() {
        return exams.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvExamIdAdmin;
        private final TextView tvExamStatusAdmin;
        private final TextView tvExamUserId;
        private final TextView tvExamScoreAdmin;
        private final TextView tvExamDateAdmin;
        private final MaterialButton btnViewExam;
        private final MaterialButton btnDeleteExam;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExamIdAdmin = itemView.findViewById(R.id.tvExamIdAdmin);
            tvExamStatusAdmin = itemView.findViewById(R.id.tvExamStatusAdmin);
            tvExamUserId = itemView.findViewById(R.id.tvExamUserId);
            tvExamScoreAdmin = itemView.findViewById(R.id.tvExamScoreAdmin);
            tvExamDateAdmin = itemView.findViewById(R.id.tvExamDateAdmin);
            btnViewExam = itemView.findViewById(R.id.btnViewExam);
            btnDeleteExam = itemView.findViewById(R.id.btnDeleteExam);
        }

        public void bind(AdminExam exam, OnExamActionListener listener) {
            tvExamIdAdmin.setText("Bài thi #" + exam.getExamId());
            tvExamUserId.setText("User #" + exam.getUserId());
            tvExamScoreAdmin.setText(exam.getScore() + "/" + exam.getTotalQuestions());
            tvExamDateAdmin.setText("Ngày: " + exam.getExamDate());

            if (exam.isPassed()) {
                tvExamStatusAdmin.setText(R.string.history_status_passed);
                tvExamStatusAdmin.setBackgroundResource(R.drawable.badge_pass);
            } else {
                tvExamStatusAdmin.setText(R.string.history_status_failed);
                tvExamStatusAdmin.setBackgroundResource(R.drawable.badge_fail);
            }

            btnViewExam.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewExam(exam);
                }
            });

            btnDeleteExam.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteExam(exam);
                }
            });
        }
    }
}
