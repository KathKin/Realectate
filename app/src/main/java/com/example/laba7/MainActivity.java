package com.example.laba7;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.laba7.utils.SharedPreferencesManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private TextView tvUserName;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private SharedPreferencesManager prefsManager;

    private final String[] tabTitles = {"Все", "Покупка", "Аренда"}; // Добавьте "Посуточно" если нужно

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefsManager = SharedPreferencesManager.getInstance(this);

        if (!prefsManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initViews();
        setupViewPager();
        setupTabs();
        loadUserName();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    private void setupViewPager() {
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(3); // Кэшируем все вкладки
    }

    private void setupTabs() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();
    }

    private void loadUserName() {
        String userName = prefsManager.getUserName();
        if (userName == null || userName.isEmpty()) {
            userName = prefsManager.getUserEmail();
        }
        tvUserName.setText(userName);

        tvUserName.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
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
}