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
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.ResponseBody;  // ← Для ResponseBody
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
    private FloatingActionButton fabAddProperty;
    private String currentUserRole;
    private Uri selectedImageUri;

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
        fabAddProperty = view.findViewById(R.id.fabAddProperty);  // 🔥 НОВОЕ

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferencesManager prefsManager = SharedPreferencesManager.getInstance(requireContext());
        currentUserRole = prefsManager.getUserRole();  // 🔥 Сохраняем роль

        // 🔥 Показываем FAB только риэлторам
        if ("AGENT".equals(currentUserRole)) {
            fabAddProperty.setVisibility(View.VISIBLE);
            fabAddProperty.setOnClickListener(v -> showAddPropertyDialog());
        } else {
            fabAddProperty.setVisibility(View.GONE);
        }

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


    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    // Можно сразу показать превью, если нужно
                }
            });

    // 🔥 Диалог добавления нового объявления
    private void showAddPropertyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Новое объявление");

        // Создаём форму ввода
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_property, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etAddress = dialogView.findViewById(R.id.etAddress);
        EditText etCity = dialogView.findViewById(R.id.etCity);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);
        EditText etRooms = dialogView.findViewById(R.id.etRooms);
        EditText etArea = dialogView.findViewById(R.id.etArea);
        ImageView ivPreview = dialogView.findViewById(R.id.ivPreview);

        // Клик по превью фото → выбор изображения
        ivPreview.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Слушатель для обновления превью при выборе фото
        // (можно вынести в отдельный метод, если нужно)

        builder.setPositiveButton("Создать", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String roomsStr = etRooms.getText().toString().trim();
            String areaStr = etArea.getText().toString().trim();

            if (title.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(getContext(), "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                return;
            }

            // Создаём объект Property
            com.example.laba7.model.Property newProperty = new com.example.laba7.model.Property();
            newProperty.setTitle(title);
            newProperty.setAddress(address);
            newProperty.setCity(city);
            newProperty.setPrice(Double.parseDouble(priceStr));
            newProperty.setRooms(roomsStr.isEmpty() ? 1 : Integer.parseInt(roomsStr));
            newProperty.setArea(areaStr.isEmpty() ? 0.0 : Double.parseDouble(areaStr));
            newProperty.setType("SALE"); // или добавьте выбор типа

            // Если фото выбрано — сначала загружаем его
            if (selectedImageUri != null) {
                uploadImageAndCreateProperty(newProperty);
            } else {
                // Создаём без фото
                createProperty(newProperty, null);
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void uploadImageAndCreateProperty(Property property) {
        try {
            byte[] bytes = readUriToBytes(selectedImageUri);
            MediaType mediaType = MediaType.parse("image/jpeg");
            RequestBody requestFile = RequestBody.create(bytes, mediaType);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);

            RetrofitClient.getApiService().uploadFile(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            // 🔥 Парсим JSON ответ
                            String jsonResponse = response.body().string();
                            org.json.JSONObject json = new org.json.JSONObject(jsonResponse);

                            // Получаем URL фото
                            String imageUrl = json.optString("fileUrl", null);

                            // Создаём объявление с imageUrl
                            createProperty(property, imageUrl);

                        } catch (Exception e) {
                            e.printStackTrace();
                            createProperty(property, null);
                        }
                    } else {
                        createProperty(property, null);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    createProperty(property, null);
                }
            });
        } catch (Exception e) {
            createProperty(property, null);
        }
    }

    // 🔥 Вспомогательный метод: чтение байтов из Uri
    private byte[] readUriToBytes(Uri uri) throws Exception {
        java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    // 🔥 Создание объявления на сервере
    private void createProperty(com.example.laba7.model.Property property, String imageUrl) {
        property.setImageUrl(imageUrl);

        RetrofitClient.getApiService().createProperty(property).enqueue(new Callback<com.example.laba7.model.Property>() {
            @Override
            public void onResponse(Call<com.example.laba7.model.Property> call, Response<com.example.laba7.model.Property> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "✅ Объявление создано!", Toast.LENGTH_LONG).show();
                    loadProperties(); // Обновляем список
                } else {
                    Toast.makeText(getContext(), "❌ Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.laba7.model.Property> call, Throwable t) {
                Toast.makeText(getContext(), "❌ Сеть: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}