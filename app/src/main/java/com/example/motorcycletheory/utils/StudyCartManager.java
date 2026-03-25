package com.example.motorcycletheory.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.motorcycletheory.models.AdminQuestion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StudyCartManager {
    private static final String PREF_NAME = "study_cart";
    private static final String KEY_CART_ITEMS = "cart_items";

    private final SharedPreferences preferences;

    public StudyCartManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void addQuestion(AdminQuestion question) {
        List<AdminQuestion> cart = getCartItems();
        for (AdminQuestion q : cart) {
            if (q.getQuestionId() == question.getQuestionId()) {
                return;
            }
        }
        cart.add(question);
        saveCart(cart);
    }

    public void removeQuestion(int questionId) {
        List<AdminQuestion> cart = getCartItems();
        cart.removeIf(q -> q.getQuestionId() == questionId);
        saveCart(cart);
    }

    public void clearCart() {
        preferences.edit().remove(KEY_CART_ITEMS).apply();
    }

    public boolean isInCart(int questionId) {
        List<AdminQuestion> cart = getCartItems();
        for (AdminQuestion q : cart) {
            if (q.getQuestionId() == questionId) {
                return true;
            }
        }
        return false;
    }

    public int getCartCount() {
        return getCartItems().size();
    }

    public List<AdminQuestion> getCartItems() {
        String json = preferences.getString(KEY_CART_ITEMS, "[]");
        List<AdminQuestion> items = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj == null) continue;
                items.add(new AdminQuestion(
                        obj.optInt("questionId", -1),
                        obj.optString("content", ""),
                        obj.optString("answerA", ""),
                        obj.optString("answerB", ""),
                        obj.optString("answerC", ""),
                        obj.optString("answerD", ""),
                        obj.optString("correctAnswer", ""),
                        obj.optInt("categoryId", 0),
                        obj.optBoolean("isImportant", false)
                ));
            }
        } catch (JSONException ignored) {
        }
        return items;
    }

    private void saveCart(List<AdminQuestion> items) {
        JSONArray array = new JSONArray();
        for (AdminQuestion q : items) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("questionId", q.getQuestionId());
                obj.put("content", q.getContent());
                obj.put("answerA", q.getAnswerA());
                obj.put("answerB", q.getAnswerB());
                obj.put("answerC", q.getAnswerC());
                obj.put("answerD", q.getAnswerD());
                obj.put("correctAnswer", q.getCorrectAnswer());
                obj.put("categoryId", q.getCategoryId());
                obj.put("isImportant", q.isImportant());
                array.put(obj);
            } catch (JSONException ignored) {
            }
        }
        preferences.edit().putString(KEY_CART_ITEMS, array.toString()).apply();
    }
}
