package com.example.laba7.utils;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String PREF_NAME = "Laba7Prefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_USER_ID = "userId"; // ← НОВОЕ
    private static final String KEY_TOKEN = "token";

    private static SharedPreferencesManager instance;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private SharedPreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context.getApplicationContext());
        }
        return instance;
    }

    // ✅ Обновлённый метод: сохраняет ID пользователя
    public void saveLoginData(String email, String name, String token, String role, Long userId) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER_ROLE, role);
        editor.putLong(KEY_USER_ID, userId != null ? userId : 0L);
        editor.apply();
    }

    // Для обратной совместимости
    public void saveLoginData(String email, String name, String token, String role) {
        saveLoginData(email, name, token, role, 0L);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "CLIENT");
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    // ✅ НОВОЕ: получение ID пользователя
    public Long getUserId() {
        return prefs.getLong(KEY_USER_ID, 0L);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}