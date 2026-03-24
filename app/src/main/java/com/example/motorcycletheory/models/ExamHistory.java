package com.example.motorcycletheory.models;

public class ExamHistory {
    private int examId;
    private int score;
    private int totalQuestions;
    private boolean passed;
    private String date;
    private int duration;

    public ExamHistory(int examId, int score, int totalQuestions, boolean passed, String date, int duration) {
        this.examId = examId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.passed = passed;
        this.date = date;
        this.duration = duration;
    }

    public int getExamId() {
        return examId;
    }

    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getDate() {
        return date;
    }

    public int getDuration() {
        return duration;
    }
}
