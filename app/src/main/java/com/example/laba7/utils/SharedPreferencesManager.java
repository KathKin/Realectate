package com.example.laba7.utils;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String PREF_NAME = "Laba7Prefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
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

    // Сохранить данные после входа
    public void saveLoginData(String email, String name, String token) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    // Проверка, авторизован ли пользователь
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Получить email
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    // Получить имя
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    // Получить токен
    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    // Выйти из системы
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
