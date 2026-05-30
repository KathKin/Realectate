package com.example.laba7;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.laba7.adapter.AgentPropertyAdapter;
import com.example.laba7.api.RetrofitClient;
import com.example.laba7.dto.PropertyWithApplicationsDto;
import com.example.laba7.utils.SharedPreferencesManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgentPropertiesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private AgentPropertyAdapter adapter;
    private List<PropertyWithApplicationsDto> propertyList = new ArrayList<>();
    private Long agentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_properties);

        agentId = getIntent().getLongExtra("agent_id", -1);
        android.util.Log.d("AGENT_PROPS", "Получил agent_id из Intent: " + agentId);

        if (agentId == -1) {
            SharedPreferencesManager prefs = SharedPreferencesManager.getInstance(this);
            Long userIdFromPrefs = prefs.getUserId();
            android.util.Log.d("AGENT_PROPS", "Получил userId из SharedPreferences: " + userIdFromPrefs);

            if (userIdFromPrefs != null && userIdFromPrefs != -1) {
                agentId = userIdFromPrefs;
            }
        }

        if (agentId == null || agentId == -1 || agentId <= 0) {
            Toast.makeText(this, " Ошибка авторизации!\nagentId = " + agentId, Toast.LENGTH_LONG).show();
            android.util.Log.e("AGENT_PROPS", " agentId не получен! Значение: " + agentId);
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Объекты с заявками");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AgentPropertyAdapter(propertyList, property -> {
            Intent intent = new Intent(AgentPropertiesActivity.this, AgentApplicationsActivity.class);
            intent.putExtra("property_id", property.getPropertyId());
            intent.putExtra("property_title", property.getPropertyTitle());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadProperties);
        loadProperties();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadProperties() {
        if (isFinishing() || isDestroyed()) return;
        swipeRefresh.setRefreshing(true);

        RetrofitClient.getApiService().getPropertiesWithApplications(agentId)
                .enqueue(new Callback<List<PropertyWithApplicationsDto>>() {
                    @Override
                    public void onResponse(Call<List<PropertyWithApplicationsDto>> call, Response<List<PropertyWithApplicationsDto>> response) {
                        if (isFinishing() || isDestroyed()) return;
                        swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            propertyList.clear();
                            propertyList.addAll(response.body());
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(AgentPropertiesActivity.this, "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<PropertyWithApplicationsDto>> call, Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(AgentPropertiesActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}