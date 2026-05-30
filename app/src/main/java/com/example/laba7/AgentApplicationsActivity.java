package com.example.laba7;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.laba7.adapter.ApplicationAdapter;
import com.example.laba7.api.RetrofitClient;
import com.example.laba7.model.Application;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgentApplicationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ApplicationAdapter adapter;
    private List<Application> applicationList = new ArrayList<>();

    private Long propertyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_applications_list);

        propertyId = getIntent().getLongExtra("property_id", -1);

        if (propertyId == -1) {
            Toast.makeText(this, "Ошибка: не передан ID объекта", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String title = getIntent().getStringExtra("property_title");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Заявки: " + (title != null ? title : "Объект"));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ApplicationAdapter(applicationList,
                (app, note) -> updateNote(app, note),
                this::deleteApp
        );
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadApplications);
        loadApplications();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadApplications() {
        if (isFinishing() || isDestroyed()) return;

        swipeRefresh.setRefreshing(true);

        RetrofitClient.getApiService().getApplicationsByPropertyId(propertyId)
                .enqueue(new Callback<List<Application>>() {
                    @Override
                    public void onResponse(Call<List<Application>> call, Response<List<Application>> response) {
                        if (isFinishing() || isDestroyed()) return;

                        swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null) {
                            applicationList.clear();
                            applicationList.addAll(response.body());
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(AgentApplicationsActivity.this,
                                    "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Application>> call, Throwable t) {
                        if (isFinishing() || isDestroyed()) return;

                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(AgentApplicationsActivity.this,
                                "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateNote(Application app, String note) {
        if (app.getId() == null) {
            Toast.makeText(AgentApplicationsActivity.this,
                    "Ошибка: неизвестный ID заявки", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("note", note);

        RetrofitClient.getApiService().updateAgentNote(app.getId(), body)
                .enqueue(new Callback<Application>() {
                    @Override
                    public void onResponse(Call<Application> call, Response<Application> response) {
                        if (isFinishing() || isDestroyed()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            app.setAgentNote(response.body().getAgentNote());
                            Toast.makeText(AgentApplicationsActivity.this,
                                    "Заметка сохранена", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AgentApplicationsActivity.this,
                                    "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Application> call, Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        Toast.makeText(AgentApplicationsActivity.this,
                                "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteApp(Application app) {
        new AlertDialog.Builder(this)
                .setTitle("🗑️ Удалить заявку?")
                .setMessage("Вы уверены?")
                .setPositiveButton("Удалить", (dialog, which) -> doDelete(app))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void doDelete(Application app) {
        RetrofitClient.getApiService().deleteApplication(app.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (isFinishing() || isDestroyed()) return;

                        if (response.isSuccessful()) {
                            applicationList.remove(app);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(AgentApplicationsActivity.this, "Заявка удалена", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AgentApplicationsActivity.this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(AgentApplicationsActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}