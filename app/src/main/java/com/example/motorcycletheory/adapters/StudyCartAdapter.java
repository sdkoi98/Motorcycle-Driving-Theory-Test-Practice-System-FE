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

public class StudyCartAdapter extends RecyclerView.Adapter<StudyCartAdapter.ViewHolder> {

    public interface OnCartItemListener {
        void onRemoveItem(AdminQuestion question);
        void onItemClick(AdminQuestion question);
    }

    private List<AdminQuestion> items = new ArrayList<>();
    private final OnCartItemListener listener;

    public StudyCartAdapter(OnCartItemListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AdminQuestion> newList) {
        items.clear();
        if (newList != null) {
            items.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_study_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvQuestionId;
        private final TextView tvQuestionContent;
        private final TextView tvImportantBadge;
        private final TextView tvCategory;
        private final ImageButton btnRemove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionId = itemView.findViewById(R.id.tvQuestionId);
            tvQuestionContent = itemView.findViewById(R.id.tvQuestionContent);
            tvImportantBadge = itemView.findViewById(R.id.tvImportantBadge);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        void bind(AdminQuestion question) {
            tvQuestionId.setText("Câu #" + question.getQuestionId());
            tvQuestionContent.setText(question.getContent());
            tvImportantBadge.setVisibility(question.isImportant() ? View.VISIBLE : View.GONE);

            String categoryLabel = itemView.getContext().getString(
                    R.string.category_format, question.getCategoryId());
            tvCategory.setText(categoryLabel);

            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveItem(question);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(question);
                }
            });
        }
    }
}
