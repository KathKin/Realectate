package com.example.laba7;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.laba7.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvRole;
    private MaterialButton btnLogout, btnMyApplications;
    private SharedPreferencesManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = SharedPreferencesManager.getInstance(this);

        initViews();
        loadData();
        setupListeners();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvProfileName);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvRole = findViewById(R.id.tvProfileRole);
        btnLogout = findViewById(R.id.btnLogoutProfile);
        btnMyApplications = findViewById(R.id.btnMyApplications);
    }

    private void loadData() {
        tvName.setText(prefs.getUserName());
        tvEmail.setText(prefs.getUserEmail());

        String role = prefs.getUserRole();
        String displayRole = "AGENT".equals(role) ? "Риэлтор" : "Клиент";
        tvRole.setText(displayRole);

        if ("CLIENT".equals(role)) {
            btnMyApplications.setVisibility(View.VISIBLE);
            btnMyApplications.setText("📋 Мои отклики");
        } else {
            btnMyApplications.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> confirmLogout());

        btnMyApplications.setOnClickListener(v -> {
            Long userId = prefs.getUserId();
            if (userId == null) {
                Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                return;
            }

            String role = prefs.getUserRole();

            if ("AGENT".equals(role)) {
                Intent intent = new Intent(ProfileActivity.this, AgentApplicationsActivity.class);
                intent.putExtra("agent_id", userId);
                startActivity(intent);
            } else {
                Intent intent = new Intent(ProfileActivity.this, MyApplicationsActivity.class);
                intent.putExtra("client_id", userId);
                startActivity(intent);
            }
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Выход")
                .setMessage("Вы действительно хотите выйти из аккаунта?")
                .setPositiveButton("Да", (dialog, which) -> performLogout())
                .setNegativeButton("Нет", null)
                .show();
    }

    private void performLogout() {
        prefs.logout();
        Toast.makeText(this, "Вы вышли из системы", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
