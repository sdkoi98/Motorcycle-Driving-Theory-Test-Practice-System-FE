package com.example.motorcycletheory.models;

public class AdminQuestion {
    private int questionId;
    private String content;
    private String answerA;
    private String answerB;
    private String answerC;
    private String answerD;
    private String correctAnswer;
    private int categoryId;
    private boolean isImportant;

    public AdminQuestion(int questionId, String content, String answerA, String answerB,
                         String answerC, String answerD, String correctAnswer,
                         int categoryId, boolean isImportant) {
        this.questionId = questionId;
        this.content = content;
        this.answerA = answerA;
        this.answerB = answerB;
        this.answerC = answerC;
        this.answerD = answerD;
        this.correctAnswer = correctAnswer;
        this.categoryId = categoryId;
        this.isImportant = isImportant;
    }

    public int getQuestionId() {
        return questionId;
    }

    public String getContent() {
        return content;
    }

    public String getAnswerA() {
        return answerA;
    }

    public String getAnswerB() {
        return answerB;
    }

    public String getAnswerC() {
        return answerC;
    }

    public String getAnswerD() {
        return answerD;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAnswerA(String answerA) {
        this.answerA = answerA;
    }

    public void setAnswerB(String answerB) {
        this.answerB = answerB;
    }

    public void setAnswerC(String answerC) {
        this.answerC = answerC;
    }

    public void setAnswerD(String answerD) {
        this.answerD = answerD;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }
}
