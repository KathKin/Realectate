package com.example.laba7;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.laba7.adapter.MyApplicationAdapter;
import com.example.laba7.api.RetrofitClient;
import com.example.laba7.model.Application;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyApplicationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private MyApplicationAdapter adapter;
    private List<Application> applicationList = new ArrayList<>();
    private Long clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_my_applications); // Используем тот же layout

        // Получаем ID клиента из Intent
        clientId = getIntent().getLongExtra("client_id", -1);
        if (clientId == -1) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupAdapter();
        loadApplications();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
    }

    private void setupAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyApplicationAdapter(applicationList,
                // Сохранение заметки
                (app, note) -> updateApplicationNote(app, note),
                // Удаление отклика
                (app) -> deleteApplication(app)
        );
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadApplications);
    }

    private void loadApplications() {
        swipeRefresh.setRefreshing(true);

        RetrofitClient.getApiService().getApplicationsByClientId(clientId)
                .enqueue(new Callback<List<Application>>() {
                    @Override
                    public void onResponse(Call<List<Application>> call, Response<List<Application>> response) {
                        swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            applicationList.clear();
                            applicationList.addAll(response.body());
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(MyApplicationsActivity.this,
                                    "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Application>> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(MyApplicationsActivity.this,
                                "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateApplicationNote(Application app, String note) {
        Map<String, String> body = new HashMap<>();
        body.put("note", note);

        RetrofitClient.getApiService().updateApplicationNote(app.getId(), body)
                .enqueue(new Callback<Application>() {
                    @Override
                    public void onResponse(Call<Application> call, Response<Application> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            app.setNote(response.body().getNote());
                            Toast.makeText(MyApplicationsActivity.this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MyApplicationsActivity.this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Application> call, Throwable t) {
                        Toast.makeText(MyApplicationsActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteApplication(Application app) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Удалить отклик?")
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
                        if (response.isSuccessful()) {
                            applicationList.remove(app);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(MyApplicationsActivity.this, "Отклик удалён", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MyApplicationsActivity.this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(MyApplicationsActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
