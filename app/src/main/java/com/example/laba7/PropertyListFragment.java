package com.example.laba7;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.laba7.adapter.PropertyAdapter;
import com.example.laba7.api.RetrofitClient;
import com.example.laba7.model.Property;
import com.example.laba7.utils.SharedPreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PropertyListFragment extends Fragment {
    private static final String ARG_TYPE = "property_type";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAddProperty;
    private TextView tvEmpty;

    private PropertyAdapter adapter;
    private String tabType;
    private List<Property> allProperties = new ArrayList<>();

    // Поля для авторизации
    private SharedPreferencesManager prefsManager;
    private String currentUserRole;
    private Long currentUserId;

    // Поля для добавления объявления
    private Uri selectedImageUri;

    // Поля для фильтров
    private String filterCity = null;
    private Double filterMinPrice = null;
    private Double filterMaxPrice = null;
    private String filterPropertyType = null;

    // Лаунчер для выбора фото
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                }
            });

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
            if (tabType == null) tabType = "ALL";
        } else {
            tabType = "ALL";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_property_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация менеджера предпочтений
        prefsManager = SharedPreferencesManager.getInstance(requireContext());
        currentUserRole = prefsManager.getUserRole();
        currentUserId = prefsManager.getUserId();

        // Привязка элементов интерфейса
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        fabAddProperty = view.findViewById(R.id.fabAddProperty);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        // Настройка RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 🔥 ПРАВИЛЬНОЕ СОЗДАНИЕ АДАПТЕРА (передаём роль и ID!)
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

            @Override
            public void onItemDelete(Property property) {
                showDeleteConfirmationDialog(property);
            }
        }, currentUserRole, currentUserId); // ← Передаём 3 аргумента

        recyclerView.setAdapter(adapter);

        // Показываем FAB только риэлторам
        if ("AGENT".equals(currentUserRole)) {
            fabAddProperty.setVisibility(View.VISIBLE);
            fabAddProperty.setOnClickListener(v -> showAddPropertyDialog());
        } else {
            fabAddProperty.setVisibility(View.GONE);
        }

        // Настройка обновления
        swipeRefreshLayout.setOnRefreshListener(this::loadProperties);
        loadProperties();
    }

    private void loadProperties() {
        if (getContext() == null) return;

        swipeRefreshLayout.setRefreshing(true);
        tvEmpty.setVisibility(View.GONE);

        RetrofitClient.getApiService().getAllProperties().enqueue(new Callback<List<Property>>() {
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

    public void applyFilters() {
        if (allProperties == null) return;

        List<Property> filteredList = new ArrayList<>();

        for (Property p : allProperties) {
            if (p == null) continue;

            // Фильтр по вкладке
            String propertyType = p.getType() != null ? p.getType() : "";
            if (!"ALL".equals(tabType) && !Objects.equals(tabType, propertyType)) continue;

            // Фильтр по городу
            if (filterCity != null && !filterCity.isEmpty()) {
                String city = p.getCity() != null ? p.getCity() : "";
                if (!city.toLowerCase().contains(filterCity.toLowerCase())) continue;
            }

            // Фильтр по цене
            Double price = p.getPrice();
            if (price == null) continue;
            if (filterMinPrice != null && price < filterMinPrice) continue;
            if (filterMaxPrice != null && price > filterMaxPrice) continue;

            // Фильтр по типу недвижимости
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

        if (filteredList.isEmpty() && isAdded()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else if (isAdded()) {
            tvEmpty.setVisibility(View.GONE);
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

    private void showAddPropertyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Новое объявление");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_property, null);
        builder.setView(dialogView);

        TextInputEditText etTitle = dialogView.findViewById(R.id.etTitle);
        TextInputEditText etAddress = dialogView.findViewById(R.id.etAddress);
        TextInputEditText etCity = dialogView.findViewById(R.id.etCity);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etPrice);
        TextInputEditText etRooms = dialogView.findViewById(R.id.etRooms);
        TextInputEditText etArea = dialogView.findViewById(R.id.etArea);
        ImageView ivPreview = dialogView.findViewById(R.id.ivPreview);

        // Клик по превью → выбор фото
        ivPreview.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Слушатель для обновления превью при выборе фото
        if (selectedImageUri != null) {
            Glide.with(requireContext())
                    .load(selectedImageUri)
                    .centerCrop()
                    .into(ivPreview);
        }

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

            Property newProperty = new Property();
            newProperty.setTitle(title);
            newProperty.setAddress(address);
            newProperty.setCity(city);
            newProperty.setPrice(Double.parseDouble(priceStr));
            newProperty.setRooms(roomsStr.isEmpty() ? 1 : Integer.parseInt(roomsStr));
            newProperty.setArea(areaStr.isEmpty() ? 0.0 : Double.parseDouble(areaStr));
            newProperty.setType("SALE");
            newProperty.setAgentId(currentUserId);

            if (selectedImageUri != null) {
                uploadImageAndCreateProperty(newProperty);
            } else {
                createProperty(newProperty, null);
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    // Загрузка фото и создание объявления
    private void uploadImageAndCreateProperty(Property property) {
        try {
            byte[] bytes = readUriToBytes(selectedImageUri);
            MediaType mediaType = MediaType.parse("image/jpeg");
            RequestBody requestFile = RequestBody.create(bytes, mediaType);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", "property_image.jpg", requestFile);

            RetrofitClient.getApiService().uploadFile(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String jsonResponse = response.body().string();
                            JSONObject json = new JSONObject(jsonResponse);
                            String imageUrl = json.optString("fileUrl", null);
                            createProperty(property, imageUrl);
                        } catch (Exception e) {
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

    // Чтение байтов из Uri
    private byte[] readUriToBytes(Uri uri) throws Exception {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    // Создание объявления на сервере
    private void createProperty(Property property, String imageUrl) {
        property.setImageUrl(imageUrl);

        if (property.getAgentId() == null) {
            property.setAgentId(currentUserId);
        }

        RetrofitClient.getApiService().createProperty(property).enqueue(new Callback<Property>() {
            @Override
            public void onResponse(Call<Property> call, Response<Property> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), " Объявление создано!", Toast.LENGTH_LONG).show();
                    loadProperties();
                } else {
                    Toast.makeText(getContext(), " Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Property> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), " Сеть: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔥 Диалог подтверждения удаления
    private void showDeleteConfirmationDialog(Property property) {
        new AlertDialog.Builder(requireContext())
                .setTitle("🗑️ Удалить объявление")
                .setMessage("Вы уверены, что хотите удалить объявление?\n\n\"" + property.getTitle() + "\"\n\nЭто действие нельзя отменить!")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Удалить", (dialog, which) -> {
                    deleteProperty(property);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // 🔥 Метод удаления
    private void deleteProperty(Property property) {
        Toast.makeText(getContext(), "⏳ Удаление...", Toast.LENGTH_SHORT).show();

        RetrofitClient.getApiService().deleteProperty(property.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(),
                            "✅ Объявление успешно удалено!",
                            Toast.LENGTH_SHORT).show();
                    loadProperties();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Неизвестная ошибка";
                        Toast.makeText(getContext(),
                                "Ошибка: " + errorBody,
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(),
                                "Ошибка сервера: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!isAdded()) return;

                Toast.makeText(getContext(),
                        "Ошибка сети: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}