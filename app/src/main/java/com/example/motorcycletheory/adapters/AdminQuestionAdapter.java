package com.example.motorcycletheory.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.motorcycletheory.R;
import com.example.motorcycletheory.models.AdminQuestion;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminQuestionAdapter extends RecyclerView.Adapter<AdminQuestionAdapter.ViewHolder> {

    private List<AdminQuestion> questions = new ArrayList<>();
    private OnQuestionActionListener listener;

    public interface OnQuestionActionListener {
        void onViewQuestion(AdminQuestion question);
        void onEditQuestion(AdminQuestion question);
        void onDeleteQuestion(AdminQuestion question);
    }

    public void setOnQuestionActionListener(OnQuestionActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AdminQuestion> newQuestions) {
        this.questions = newQuestions != null ? newQuestions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminQuestion question = questions.get(position);
        holder.bind(question, listener);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvQuestionId;
        private final TextView tvImportantBadge;
        private final TextView tvQuestionContent;
        private final TextView tvCorrectAnswer;
        private final TextView tvCategory;
        private final MaterialButton btnViewQuestion;
        private final MaterialButton btnEditQuestion;
        private final MaterialButton btnDeleteQuestion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionId = itemView.findViewById(R.id.tvQuestionId);
            tvImportantBadge = itemView.findViewById(R.id.tvImportantBadge);
            tvQuestionContent = itemView.findViewById(R.id.tvQuestionContent);
            tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnViewQuestion = itemView.findViewById(R.id.btnViewQuestion);
            btnEditQuestion = itemView.findViewById(R.id.btnEditQuestion);
            btnDeleteQuestion = itemView.findViewById(R.id.btnDeleteQuestion);
        }

        public void bind(AdminQuestion question, OnQuestionActionListener listener) {
            tvQuestionId.setText("Câu #" + question.getQuestionId());
            tvQuestionContent.setText(question.getContent());
            tvCorrectAnswer.setText("Đáp án: " + question.getCorrectAnswer());
            tvCategory.setText("Category: " + question.getCategoryId());

            if (question.isImportant()) {
                tvImportantBadge.setVisibility(View.VISIBLE);
            } else {
                tvImportantBadge.setVisibility(View.GONE);
            }

            btnViewQuestion.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewQuestion(question);
                }
            });

            btnEditQuestion.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditQuestion(question);
                }
            });

            btnDeleteQuestion.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteQuestion(question);
                }
            });
        }
    }
}
