package com.example.laba7;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.laba7.api.RetrofitClient;
import com.example.laba7.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister;
    private boolean isCheckingRealtor = false;
    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefsManager = SharedPreferencesManager.getInstance(this);
        if (prefsManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Вход...");

        RetrofitClient.getApiService().login(
                new com.example.laba7.api.ApiService.UserLoginRequest(email, password)
        ).enqueue(new Callback<com.example.laba7.api.ApiService.AuthResponse>() {
            @Override
            public void onResponse(Call<com.example.laba7.api.ApiService.AuthResponse> call,
                                   Response<com.example.laba7.api.ApiService.AuthResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Войти");

                if (response.isSuccessful() && response.body() != null) {
                    var authResponse = response.body();
                    if (authResponse.getUser() != null) {
                        String userRole = authResponse.getUser().getRole() != null
                                ? authResponse.getUser().getRole()
                                : "CLIENT";

                        prefsManager.saveLoginData(
                                authResponse.getUser().getEmail(),
                                authResponse.getUser().getFullName(),
                                authResponse.getToken() != null ? authResponse.getToken() : "",
                                userRole,
                                authResponse.getUser().getId()
                        );

                        Toast.makeText(LoginActivity.this,
                                "Добро пожаловать, " + authResponse.getUser().getFullName() + "!",
                                Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        Toast.makeText(LoginActivity.this, "Ошибка входа", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.laba7.api.ApiService.AuthResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Войти");
                Toast.makeText(LoginActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}