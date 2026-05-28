package com.example.laba7;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.laba7.adapter.PropertyAdapter;
import com.example.laba7.api.RetrofitClient;
import com.example.laba7.model.Application;
import com.example.laba7.model.Property;
import com.example.laba7.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView tvUserName;
    private ImageButton btnFilter;
    private MaterialButton btnToggleView;
    private SharedPreferencesManager prefsManager;
    private boolean isShowingApplications = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnToggleView = findViewById(R.id.btnToggleView);

        if (btnToggleView != null) {
            btnToggleView.setOnClickListener(v -> {
                toggleView();
            });
        }

        prefsManager = SharedPreferencesManager.getInstance(this);

        if (!prefsManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initViews();
        setupListeners();
        loadUserName();
        setupUserRole();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        btnFilter = findViewById(R.id.btnFilter);
        btnToggleView = findViewById(R.id.btnToggleView);
    }

    private void setupListeners() {
        tvUserName.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void toggleView() {
        if (!isShowingApplications) {
            btnToggleView.setText("🏠 Объявления");
            loadFragment(new ApplicationsFragment());
            isShowingApplications = true;
        } else {
            btnToggleView.setText("📥 Заявки");
            loadFragment(new PropertyListFragment());
            isShowingApplications = false;
        }
    }

    private void setupUserRole() {
        String role = prefsManager.getUserRole();

        if ("AGENT".equals(role)) {
            btnToggleView.setVisibility(View.VISIBLE);
            btnToggleView.setText("📥 Заявки");
            isShowingApplications = false;
            loadFragment(new PropertyListFragment());
        } else {
            btnToggleView.setVisibility(View.GONE);
            loadFragment(new PropertyListFragment());
        }
    }

    private void loadUserName() {
        String userName = prefsManager.getUserName();
        if (userName == null || userName.isEmpty()) {
            userName = prefsManager.getUserEmail();
        }
        tvUserName.setText(userName);
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!prefsManager.isLoggedIn()) {
            navigateToLogin();
        }
    }

    public void showRespondDialog(Property property) {
        Long clientId = prefsManager.getUserId();
        if (clientId == null || clientId <= 0) {
            Toast.makeText(this, "Ошибка авторизации. Войдите заново.", Toast.LENGTH_LONG).show();
            return;
        }

        if (property.getId() == null) {
            Toast.makeText(this, "Ошибка: ID объявления не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText inputMessage = new EditText(this);
        inputMessage.setHint("Сообщение агенту (необязательно)");
        inputMessage.setPadding(40, 30, 40, 30);
        inputMessage.setMaxLines(3);

        new AlertDialog.Builder(this)
                .setTitle("Отклик на объявление")
                .setMessage("Отправить заявку на:\n\"" + property.getTitle() + "\"?")
                .setView(inputMessage)
                .setPositiveButton("Отправить", (dialog, which) -> {
                    String messageText = inputMessage.getText().toString().trim();
                    if (messageText.isEmpty()) {
                        messageText = "Здравствуйте, интересует ваше объявление. Готов обсудить детали.";
                    }

                    Application application = new Application();
                    application.setPropertyId(property.getId());
                    application.setClientId(clientId);
                    application.setClientName(prefsManager.getUserName());
                    application.setClientPhone(prefsManager.getUserEmail());
                    application.setMessage(messageText);

                    sendApplication(application);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void sendApplication(Application app) {
        Toast.makeText(this, " Отправка заявки...", Toast.LENGTH_SHORT).show();

        RetrofitClient.getApiService().submitApplication(app).enqueue(new Callback<Application>() {
            @Override
            public void onResponse(Call<Application> call, Response<Application> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MainActivity.this,
                            "Заявка успешно отправлена!\nАгент свяжется с вами.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Ошибка сервера: " + response.code() + " " + response.message(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Application> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Ошибка сети: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}