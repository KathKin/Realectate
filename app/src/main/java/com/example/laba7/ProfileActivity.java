package com.example.laba7;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.laba7.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvRole;
    private MaterialButton btnLogout;
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
    }

    private void loadData() {
        tvName.setText(prefs.getUserName());
        tvEmail.setText(prefs.getUserEmail());

        // ✅ ИСПРАВЛЕНО: отображаем реальную роль
        String role = prefs.getUserRole();
        String displayRole = "AGENT".equals(role) ? "Риэлтор" : "Клиент";
        tvRole.setText(displayRole);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> confirmLogout());
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
