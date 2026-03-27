package com.example.motorcycletheory.models;

public class AdminExam {
    private int examId;
    private int userId;
    private String username;
    private int score;
    private int totalQuestions;
    private boolean isPassed;
    private String examDate;

    public AdminExam(int examId, int userId, String username, int score, int totalQuestions, boolean isPassed, String examDate) {
        this.examId = examId;
        this.userId = userId;
        this.username = username;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.isPassed = isPassed;
        this.examDate = examDate;
    }

    public int getExamId() {
        return examId;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public boolean isPassed() {
        return isPassed;
    }

    public String getExamDate() {
        return examDate;
    }
}
