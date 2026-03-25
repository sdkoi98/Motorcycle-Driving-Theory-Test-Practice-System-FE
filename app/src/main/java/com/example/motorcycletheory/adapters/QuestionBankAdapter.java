package com.example.motorcycletheory.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.motorcycletheory.R;
import com.example.motorcycletheory.models.AdminQuestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class QuestionBankAdapter extends RecyclerView.Adapter<QuestionBankAdapter.ViewHolder> {

    public interface OnQuestionBankListener {
        void onQuestionClick(AdminQuestion question);
        void onBookmarkToggle(AdminQuestion question, boolean isBookmarked);
    }

    private List<AdminQuestion> questions = new ArrayList<>();
    private Set<Integer> bookmarkedIds;
    private final OnQuestionBankListener listener;

    public QuestionBankAdapter(OnQuestionBankListener listener, Set<Integer> bookmarkedIds) {
        this.listener = listener;
        this.bookmarkedIds = bookmarkedIds;
    }

    public void submitList(List<AdminQuestion> newList) {
        questions.clear();
        if (newList != null) {
            questions.addAll(newList);
        }
        notifyDataSetChanged();
    }

    public void updateBookmarks(Set<Integer> ids) {
        this.bookmarkedIds = ids;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question_bank, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminQuestion question = questions.get(position);
        holder.bind(question);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvQuestionId;
        private final TextView tvQuestionContent;
        private final TextView tvImportantBadge;
        private final TextView tvCategory;
        private final ImageButton btnBookmark;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionId = itemView.findViewById(R.id.tvQuestionId);
            tvQuestionContent = itemView.findViewById(R.id.tvQuestionContent);
            tvImportantBadge = itemView.findViewById(R.id.tvImportantBadge);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnBookmark = itemView.findViewById(R.id.btnBookmark);
        }

        void bind(AdminQuestion question) {
            tvQuestionId.setText("Câu #" + question.getQuestionId());
            tvQuestionContent.setText(question.getContent());
            tvImportantBadge.setVisibility(question.isImportant() ? View.VISIBLE : View.GONE);

            String categoryLabel = itemView.getContext().getString(
                    R.string.category_format, question.getCategoryId());
            tvCategory.setText(categoryLabel);

            boolean isBookmarked = bookmarkedIds != null && bookmarkedIds.contains(question.getQuestionId());
            btnBookmark.setImageResource(isBookmarked
                    ? android.R.drawable.btn_star_big_on
                    : android.R.drawable.btn_star_big_off);

            btnBookmark.setOnClickListener(v -> {
                if (listener != null) {
                    boolean currentlyBookmarked = bookmarkedIds != null
                            && bookmarkedIds.contains(question.getQuestionId());
                    listener.onBookmarkToggle(question, !currentlyBookmarked);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuestionClick(question);
                }
            });
        }
    }
}
