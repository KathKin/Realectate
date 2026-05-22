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
    private String propertyType;
    private List<Property> allProperties = new ArrayList<>();

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
            propertyType = getArguments().getString(ARG_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_property_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PropertyAdapter(property -> {
            Toast.makeText(getContext(),
                    "Выбрано: " + property.getTitle(),
                    Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadProperties);
        loadProperties();

        return view;
    }

    private void loadProperties() {
        swipeRefreshLayout.setRefreshing(true);

        // Загружаем все свойства, потом фильтруем
        Call<List<Property>> call = RetrofitClient.getApiService().getAllProperties();
        call.enqueue(new Callback<List<Property>>() {
            @Override
            public void onResponse(Call<List<Property>> call, Response<List<Property>> response) {
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    allProperties = response.body();
                    filterProperties();
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

    private void filterProperties() {
        List<Property> filteredList = new ArrayList<>();

        if (propertyType == null || propertyType.equals("ALL")) {
            filteredList.addAll(allProperties);
        } else {
            for (Property property : allProperties) {
                if (propertyType.equals("SALE") && "SALE".equals(property.getType())) {
                    filteredList.add(property);
                } else if (propertyType.equals("RENT") && "RENT".equals(property.getType())) {
                    filteredList.add(property);
                }
                // Для "посуточно" можно добавить отдельную логику, если есть такой тип
            }
        }

        adapter.setProperties(filteredList);

        if (filteredList.isEmpty()) {
            Toast.makeText(getContext(), "Нет объявлений в этой категории", Toast.LENGTH_SHORT).show();
        }
    }

    // Метод для обновления данных при переключении вкладок
    public void refreshData() {
        filterProperties();
    }
}