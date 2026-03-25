package com.example.motorcycletheory.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.motorcycletheory.R;
import com.example.motorcycletheory.databinding.ActivityQuestionDetailBinding;
import com.example.motorcycletheory.models.AdminQuestion;
import com.example.motorcycletheory.utils.StudyCartManager;

public class QuestionDetailActivity extends AppCompatActivity {
    public static final String EXTRA_QUESTION_ID = "extra_question_id";
    public static final String EXTRA_CONTENT = "extra_content";
    public static final String EXTRA_ANSWER_A = "extra_answer_a";
    public static final String EXTRA_ANSWER_B = "extra_answer_b";
    public static final String EXTRA_ANSWER_C = "extra_answer_c";
    public static final String EXTRA_ANSWER_D = "extra_answer_d";
    public static final String EXTRA_CORRECT = "extra_correct";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_IMPORTANT = "extra_important";

    private ActivityQuestionDetailBinding binding;
    private StudyCartManager cartManager;
    private AdminQuestion question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuestionDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartManager = new StudyCartManager(this);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        loadQuestion();
        setupCartButton();
    }

    private void loadQuestion() {
        int questionId = getIntent().getIntExtra(EXTRA_QUESTION_ID, -1);
        String content = getIntent().getStringExtra(EXTRA_CONTENT);
        String answerA = getIntent().getStringExtra(EXTRA_ANSWER_A);
        String answerB = getIntent().getStringExtra(EXTRA_ANSWER_B);
        String answerC = getIntent().getStringExtra(EXTRA_ANSWER_C);
        String answerD = getIntent().getStringExtra(EXTRA_ANSWER_D);
        String correct = getIntent().getStringExtra(EXTRA_CORRECT);
        int categoryId = getIntent().getIntExtra(EXTRA_CATEGORY, 0);
        boolean isImportant = getIntent().getBooleanExtra(EXTRA_IMPORTANT, false);

        if (questionId <= 0 || content == null) {
            Toast.makeText(this, "Dữ liệu câu hỏi không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        question = new AdminQuestion(questionId, content, answerA, answerB,
                answerC, answerD, correct, categoryId, isImportant);

        binding.tvQuestionId.setText(getString(R.string.detail_question_id, questionId));
        binding.tvQuestionContent.setText(content);

        binding.tvAnswerA.setText("A. " + answerA);
        binding.tvAnswerB.setText("B. " + answerB);
        binding.tvAnswerC.setText("C. " + answerC);
        binding.tvAnswerD.setText("D. " + answerD);

        binding.tvCorrectAnswer.setText("Đáp án " + correct);
        binding.tvCategory.setText(getString(R.string.category_format, categoryId));

        if (isImportant) {
            binding.tvImportantBadge.setVisibility(View.VISIBLE);
        }

        highlightCorrectAnswer(correct);
    }

    private void highlightCorrectAnswer(String correct) {
        TextView target = null;
        if ("A".equalsIgnoreCase(correct)) target = binding.tvAnswerA;
        else if ("B".equalsIgnoreCase(correct)) target = binding.tvAnswerB;
        else if ("C".equalsIgnoreCase(correct)) target = binding.tvAnswerC;
        else if ("D".equalsIgnoreCase(correct)) target = binding.tvAnswerD;

        if (target != null) {
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(getColor(R.color.correct_answer_bg));
            bg.setCornerRadius(8 * getResources().getDisplayMetrics().density);
            bg.setStroke((int) (2 * getResources().getDisplayMetrics().density), getColor(R.color.brand_green));
            target.setBackground(bg);
            target.setTextColor(getColor(R.color.brand_dark));
        }
    }

    private void setupCartButton() {
        updateCartButtonState();

        binding.btnAddToCart.setOnClickListener(v -> {
            if (question == null) return;
            boolean inCart = cartManager.isInCart(question.getQuestionId());
            if (inCart) {
                cartManager.removeQuestion(question.getQuestionId());
                Toast.makeText(this, getString(R.string.toast_removed_from_cart), Toast.LENGTH_SHORT).show();
            } else {
                cartManager.addQuestion(question);
                Toast.makeText(this, getString(R.string.toast_added_to_cart), Toast.LENGTH_SHORT).show();
            }
            updateCartButtonState();
        });
    }

    private void updateCartButtonState() {
        if (question == null) return;
        boolean inCart = cartManager.isInCart(question.getQuestionId());
        if (inCart) {
            binding.btnAddToCart.setText(getString(R.string.btn_remove_from_cart));
            binding.btnAddToCart.setIconResource(android.R.drawable.btn_star_big_on);
            binding.btnAddToCart.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(getColor(R.color.danger_red)));
        } else {
            binding.btnAddToCart.setText(getString(R.string.btn_add_to_cart));
            binding.btnAddToCart.setIconResource(android.R.drawable.btn_star_big_off);
            binding.btnAddToCart.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(getColor(R.color.brand_green)));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
