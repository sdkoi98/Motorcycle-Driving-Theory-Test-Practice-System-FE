package com.example.motorcycletheory.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.motorcycletheory.R;
import com.example.motorcycletheory.models.ExamHistory;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<ExamHistory> historyList = new ArrayList<>();

    public void submitList(List<ExamHistory> newList) {
        historyList.clear();
        if (newList != null) {
            historyList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ExamHistory item = historyList.get(position);
        holder.bind(item, position + 1);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvExamNumber;
        private final TextView tvExamDate;
        private final TextView tvScore;
        private final TextView tvStatus;
        private final TextView tvTotalQuestions;
        private final TextView tvDuration;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExamNumber = itemView.findViewById(R.id.tvExamNumber);
            tvExamDate = itemView.findViewById(R.id.tvExamDate);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTotalQuestions = itemView.findViewById(R.id.tvTotalQuestions);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }

        public void bind(ExamHistory item, int position) {
            tvExamNumber.setText("Bài thi #" + position);
            tvScore.setText(item.getScore() + "/" + item.getTotalQuestions());
            
            // Set status với màu và text
            if (item.isPassed()) {
                tvStatus.setText("ĐẬU");
                tvStatus.setBackgroundResource(R.drawable.badge_pass);
            } else {
                tvStatus.setText("TRƯỢT");
                tvStatus.setBackgroundResource(R.drawable.badge_fail);
            }
            
            tvTotalQuestions.setText(item.getTotalQuestions() + " câu hỏi");
            
            // Format date nếu có
            if (item.getDate() != null && !item.getDate().isEmpty()) {
                tvExamDate.setText(item.getDate());
            } else {
                tvExamDate.setVisibility(View.GONE);
            }
            
            // Duration (có thể tính từ data hoặc mặc định)
            tvDuration.setText("⏱ " + item.getDuration() + " phút");
        }
    }
}
