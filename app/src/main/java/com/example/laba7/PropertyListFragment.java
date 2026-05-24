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
import com.example.laba7.utils.SharedPreferencesManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PropertyListFragment extends Fragment {

    private static final String ARG_TYPE = "property_type";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PropertyAdapter adapter;
    private String tabType;
    private List<Property> allProperties = new ArrayList<>();

    // Поля фильтров
    private String filterCity = null;
    private Double filterMinPrice = null;
    private Double filterMaxPrice = null;
    private String filterPropertyType = null;

    public static PropertyListFragment newInstance(String type) {
        PropertyListFragment fragment = new PropertyListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type != null ? type : "ALL");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabType = getArguments().getString(ARG_TYPE);
            if (tabType == null) tabType = "ALL"; // Защита от null
        } else {
            tabType = "ALL";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_property_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferencesManager prefsManager = SharedPreferencesManager.getInstance(requireContext());

        adapter = new PropertyAdapter(new PropertyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Property property) {
                Toast.makeText(getContext(),
                        "Выбрано: " + property.getTitle(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemRespond(Property property) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showRespondDialog(property);
                }
            }
        }, prefsManager.getUserRole());

        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadProperties);
        loadProperties();

        return view;
    }

    private void loadProperties() {
        if (getContext() == null) return;

        swipeRefreshLayout.setRefreshing(true);
        Call<List<Property>> call = RetrofitClient.getApiService().getAllProperties();
        call.enqueue(new Callback<List<Property>>() {
            @Override
            public void onResponse(Call<List<Property>> call, Response<List<Property>> response) {
                if (!isAdded()) return;

                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    allProperties = response.body();
                    applyFilters();
                } else {
                    Toast.makeText(getContext(), "Ошибка сервера: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Property>> call, Throwable t) {
                if (!isAdded()) return;

                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔥 ИСПРАВЛЕННЫЙ МЕТОД С ЗАЩИТОЙ ОТ NULL
    public void applyFilters() {
        if (allProperties == null) return;

        List<Property> filteredList = new ArrayList<>();

        for (Property p : allProperties) {
            if (p == null) continue;

            // 1. Фильтр по вкладке (Покупка/Аренда) - БЕЗОПАСНОЕ СРАВНЕНИЕ
            String propertyType = p.getType() != null ? p.getType() : "";
            if (!"ALL".equals(tabType) && !Objects.equals(tabType, propertyType)) continue;

            // 2. Фильтр по городу - с проверкой на null
            if (filterCity != null && !filterCity.isEmpty()) {
                String city = p.getCity() != null ? p.getCity() : "";
                if (!city.toLowerCase().contains(filterCity.toLowerCase())) continue;
            }

            // 3. Фильтр по цене
            Double price = p.getPrice();
            if (price == null) continue;

            if (filterMinPrice != null && price < filterMinPrice) continue;
            if (filterMaxPrice != null && price > filterMaxPrice) continue;

            // 4. Фильтр по типу недвижимости - безопасное сравнение
            if (filterPropertyType != null && !filterPropertyType.isEmpty()) {
                String title = p.getTitle() != null ? p.getTitle() : "";
                if (!title.toLowerCase().contains(filterPropertyType.toLowerCase())) continue;
            }

            filteredList.add(p);
        }

        if (adapter != null) {
            adapter.setProperties(filteredList);
        }

        if (filteredList.isEmpty() && !allProperties.isEmpty() && isAdded()) {
            Toast.makeText(getContext(), "Ничего не найдено по фильтрам", Toast.LENGTH_SHORT).show();
        }
    }

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