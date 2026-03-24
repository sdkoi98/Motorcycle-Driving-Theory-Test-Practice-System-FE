package com.example.motorcycletheory.models;

public class AdminUser {
    private int userId;
    private String username;
    private String email;
    private String role;

    public AdminUser(int userId, String username, String email, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatarLetter() {
        if (username != null && !username.isEmpty()) {
            return username.substring(0, 1).toUpperCase();
        }
        return "U";
    }
}
