package com.example.laba7;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.laba7.adapter.PropertyAdapter;
import com.example.laba7.api.RetrofitClient;
import com.example.laba7.model.Property;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PropertyListFragment extends Fragment {

    private static final String ARG_TYPE = "property_type";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PropertyAdapter adapter;
    private String tabType; // SALE, RENT, ALL
    private List<Property> allProperties = new ArrayList<>();

    // Поля фильтров
    private String filterCity = null;
    private Double filterMinPrice = null;
    private Double filterMaxPrice = null;
    private String filterPropertyType = null;

    public static PropertyListFragment newInstance(String type) {
        PropertyListFragment fragment = new PropertyListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabType = getArguments().getString(ARG_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_property_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PropertyAdapter(property -> {
            Toast.makeText(getContext(), "Выбрано: " + property.getTitle(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadProperties);
        loadProperties();

        return view;
    }

    private void loadProperties() {
        swipeRefreshLayout.setRefreshing(true);
        Call<List<Property>> call = RetrofitClient.getApiService().getAllProperties();
        call.enqueue(new Callback<List<Property>>() {
            @Override
            public void onResponse(Call<List<Property>> call, Response<List<Property>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    allProperties = response.body();
                    applyFilters(); // Применяем фильтры сразу после загрузки
                } else {
                    Toast.makeText(getContext(), "Ошибка сервера", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Property>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔥 МЕТОД ДЛЯ ПРИМЕНЕНИЯ ФИЛЬТРОВ
    public void applyFilters() {
        List<Property> filteredList = new ArrayList<>();

        for (Property p : allProperties) {
            // 1. Фильтр по вкладке (Покупка/Аренда)
            if (!"ALL".equals(tabType) && !tabType.equals(p.getType())) continue;

            // 2. Фильтр по городу
            if (filterCity != null && !filterCity.isEmpty()) {
                if (!p.getCity().toLowerCase().contains(filterCity.toLowerCase())) continue;
            }

            // 3. Фильтр по цене
            double price = p.getPrice().doubleValue();
            if (filterMinPrice != null && price < filterMinPrice) continue;
            if (filterMaxPrice != null && price > filterMaxPrice) continue;

            // 4. Фильтр по типу недвижимости (если есть в title/description или отдельном поле)
            // Для примера проверяем title. В реальном проекте лучше добавить поле propertyType в модель
            if (filterPropertyType != null) {
                String title = p.getTitle().toLowerCase();
                boolean matches = title.contains(filterPropertyType.toLowerCase());
                if (!matches) continue;
            }

            filteredList.add(p);
        }

        adapter.setProperties(filteredList);
        if (filteredList.isEmpty() && !allProperties.isEmpty()) {
            Toast.makeText(getContext(), "Ничего не найдено по фильтрам", Toast.LENGTH_SHORT).show();
        }
    }

    // Сброс фильтров
    public void resetFilters() {
        filterCity = null;
        filterMinPrice = null;
        filterMaxPrice = null;
        filterPropertyType = null;
        applyFilters();
    }

    public void setFilterCity(String city) { this.filterCity = city; }
    public void setFilterPrice(Double min, Double max) {
        this.filterMinPrice = min;
        this.filterMaxPrice = max;
    }
    public void setFilterPropertyType(String type) { this.filterPropertyType = type; }
}