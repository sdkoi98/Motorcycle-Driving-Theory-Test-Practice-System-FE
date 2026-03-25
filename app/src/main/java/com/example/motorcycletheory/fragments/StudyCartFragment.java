package com.example.motorcycletheory.fragments;

import android.app.AlertDialog;
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

import com.example.motorcycletheory.R;
import com.example.motorcycletheory.activities.ExamConfirmActivity;
import com.example.motorcycletheory.activities.QuestionDetailActivity;
import com.example.motorcycletheory.adapters.StudyCartAdapter;
import com.example.motorcycletheory.databinding.FragmentStudyCartBinding;
import com.example.motorcycletheory.models.AdminQuestion;
import com.example.motorcycletheory.utils.StudyCartManager;

import java.util.List;

public class StudyCartFragment extends Fragment implements StudyCartAdapter.OnCartItemListener {
    private FragmentStudyCartBinding binding;
    private StudyCartAdapter adapter;
    private StudyCartManager cartManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStudyCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cartManager = new StudyCartManager(requireContext());
        adapter = new StudyCartAdapter(this);

        binding.rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCart.setAdapter(adapter);

        binding.btnClearAll.setOnClickListener(v -> confirmClearAll());
        binding.btnPractice.setOnClickListener(v -> startPractice());

        loadCart();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCart();
    }

    private void loadCart() {
        List<AdminQuestion> items = cartManager.getCartItems();
        updateSummary(items);

        if (items.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.rvCart.setVisibility(View.GONE);
            binding.btnPractice.setEnabled(false);
            binding.btnPractice.setAlpha(0.5f);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.rvCart.setVisibility(View.VISIBLE);
            binding.btnPractice.setEnabled(true);
            binding.btnPractice.setAlpha(1f);
            adapter.submitList(items);
        }
    }

    private void updateSummary(List<AdminQuestion> items) {
        binding.tvCartCount.setText(String.valueOf(items.size()));

        int importantCount = 0;
        for (AdminQuestion q : items) {
            if (q.isImportant()) importantCount++;
        }
        binding.tvImportantCount.setText(getString(R.string.cart_important_format, importantCount));
    }

    private void confirmClearAll() {
        if (cartManager.getCartCount() == 0) return;

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.admin_dialog_confirm_delete))
                .setMessage(getString(R.string.cart_clear_confirm))
                .setPositiveButton(getString(R.string.admin_btn_delete), (dialog, which) -> {
                    cartManager.clearCart();
                    loadCart();
                    updateCartBadge();
                    Toast.makeText(requireContext(), getString(R.string.cart_cleared), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.admin_btn_cancel), null)
                .show();
    }

    private void startPractice() {
        if (cartManager.getCartCount() == 0) {
            Toast.makeText(requireContext(), getString(R.string.cart_empty_cannot_practice), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), ExamConfirmActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRemoveItem(AdminQuestion question) {
        cartManager.removeQuestion(question.getQuestionId());
        loadCart();
        updateCartBadge();
        Toast.makeText(requireContext(), getString(R.string.toast_removed_from_cart), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(AdminQuestion question) {
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
