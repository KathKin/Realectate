package com.example.laba7;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.android.material.button.MaterialButton;
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
    private android.widget.TextView tvEmpty;

    private MaterialButton btnTabAll, btnTabSale, btnTabRent;
    private ImageButton btnFilter;

    private PropertyAdapter adapter;
    private String tabType = "ALL";
    private List<Property> allProperties = new ArrayList<>();

    private SharedPreferencesManager prefsManager;
    private String currentUserRole;
    private Long currentUserId;

    private String filterCity = null;
    private Double filterMinPrice = null;
    private Double filterMaxPrice = null;
    private String filterPropertyType = null;

    private Uri selectedImageUri;

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

        prefsManager = SharedPreferencesManager.getInstance(requireContext());
        currentUserRole = prefsManager.getUserRole();
        currentUserId = prefsManager.getUserId();

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        fabAddProperty = view.findViewById(R.id.fabAddProperty);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        btnTabAll = view.findViewById(R.id.btnTabAll);
        btnTabSale = view.findViewById(R.id.btnTabSale);
        btnTabRent = view.findViewById(R.id.btnTabRent);
        btnFilter = view.findViewById(R.id.btnFilter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PropertyAdapter(new PropertyAdapter.OnItemClickListener() {
            @Override public void onItemClick(Property p) {
                Toast.makeText(getContext(), "Выбрано: " + p.getTitle(), Toast.LENGTH_SHORT).show();
            }
            @Override public void onItemRespond(Property p) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showRespondDialog(p);
                }
            }
            @Override public void onItemDelete(Property p) {
                showDeleteConfirmationDialog(p);
            }
        }, currentUserRole, currentUserId);

        recyclerView.setAdapter(adapter);

        setupTabButtons();

        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterDialog());
        }

        if ("AGENT".equals(currentUserRole)) {
            fabAddProperty.setVisibility(View.VISIBLE);
            fabAddProperty.setOnClickListener(v -> showAddPropertyDialog());
        } else {
            fabAddProperty.setVisibility(View.GONE);
        }

        swipeRefreshLayout.setOnRefreshListener(this::loadProperties);
        loadProperties();
    }

    private void setupTabButtons() {
        updateTabButtons();
        
        btnTabAll.setOnClickListener(v -> {
            tabType = "ALL";
            updateTabButtons();
            applyFilters();
        });

        btnTabSale.setOnClickListener(v -> {
            tabType = "SALE";
            updateTabButtons();
            applyFilters();
        });

        btnTabRent.setOnClickListener(v -> {
            tabType = "RENT";
            updateTabButtons();
            applyFilters();
        });
    }

    private void updateTabButtons() {
        // Сброс всех кнопок
        resetButtonStyle(btnTabAll);
        resetButtonStyle(btnTabSale);
        resetButtonStyle(btnTabRent);

        // Выделяем активную
        switch (tabType) {
            case "SALE":
                setActiveButtonStyle(btnTabSale);
                break;
            case "RENT":
                setActiveButtonStyle(btnTabRent);
                break;
            default:
                setActiveButtonStyle(btnTabAll);
                break;
        }
    }

    private void resetButtonStyle(MaterialButton btn) {
        btn.setStrokeColorResource(android.R.color.darker_gray);
        btn.setStrokeWidth(1);
        btn.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
    }

    private void setActiveButtonStyle(MaterialButton btn) {
        btn.setStrokeColorResource(android.R.color.holo_blue_dark);
        btn.setStrokeWidth(2);
        btn.setTextColor(getResources().getColor(android.R.color.holo_blue_dark, null));
    }

    private void loadProperties() {
        if (getContext() == null) return;
        swipeRefreshLayout.setRefreshing(true);
        tvEmpty.setVisibility(View.GONE);

        RetrofitClient.getApiService().getAllProperties().enqueue(new Callback<List<Property>>() {
            @Override public void onResponse(Call<List<Property>> call, Response<List<Property>> response) {
                if (!isAdded()) return;
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    allProperties = response.body();
                    applyFilters();
                } else {
                    Toast.makeText(getContext(), "Ошибка сервера: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<Property>> call, Throwable t) {
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

            String propType = p.getType() != null ? p.getType() : "";
            if (!"ALL".equals(tabType) && !Objects.equals(tabType, propType)) continue;

            if (filterCity != null && !filterCity.isEmpty()) {
                String city = p.getCity() != null ? p.getCity() : "";
                if (!city.toLowerCase().contains(filterCity.toLowerCase())) continue;
            }

            Double price = p.getPrice();
            if (price == null) continue;
            if (filterMinPrice != null && price < filterMinPrice) continue;
            if (filterMaxPrice != null && price > filterMaxPrice) continue;

            if (filterPropertyType != null && !filterPropertyType.isEmpty()) {
                String title = p.getTitle() != null ? p.getTitle() : "";
                if (!title.toLowerCase().contains(filterPropertyType.toLowerCase())) continue;
            }

            filteredList.add(p);
        }

        if (adapter != null) adapter.setProperties(filteredList);

        if (filteredList.isEmpty() && isAdded()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else if (isAdded()) {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    public void setFilterCity(String city) { this.filterCity = city; }
    public void setFilterPrice(Double min, Double max) {
        this.filterMinPrice = min;
        this.filterMaxPrice = max;
    }
    public void setFilterPropertyType(String type) { this.filterPropertyType = type; }

    public void resetFilters() {
        filterCity = null;
        filterMinPrice = null;
        filterMaxPrice = null;
        filterPropertyType = null;
        applyFilters();
        updateTabButtons();
    }

    public void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Фильтры");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);

        try {
            com.google.android.material.textfield.TextInputEditText etCity =
                    dialogView.findViewById(R.id.etFilterCity);
            com.google.android.material.textfield.TextInputEditText etMinPrice =
                    dialogView.findViewById(R.id.etMinPrice);
            com.google.android.material.textfield.TextInputEditText etMaxPrice =
                    dialogView.findViewById(R.id.etMaxPrice);
            com.google.android.material.textfield.TextInputEditText etPropertyType =
                    dialogView.findViewById(R.id.etPropertyType);

            if (etCity == null || etMinPrice == null || etMaxPrice == null || etPropertyType == null) {
                android.util.Log.e("DEBUG_FILTER", "Не найдены поля в диалоге!");
                Toast.makeText(getContext(), "Ошибка загрузки фильтра", Toast.LENGTH_SHORT).show();
                return;
            }

            if (filterCity != null) etCity.setText(filterCity);
            if (filterMinPrice != null) etMinPrice.setText(String.valueOf(filterMinPrice.longValue()));
            if (filterMaxPrice != null) etMaxPrice.setText(String.valueOf(filterMaxPrice.longValue()));
            if (filterPropertyType != null) etPropertyType.setText(filterPropertyType);

            builder.setPositiveButton("Применить", (dialog, which) -> {
                try {
                    String city = etCity.getText() != null ? etCity.getText().toString().trim() : null;
                    String minPriceStr = etMinPrice.getText() != null ? etMinPrice.getText().toString().trim() : null;
                    String maxPriceStr = etMaxPrice.getText() != null ? etMaxPrice.getText().toString().trim() : null;
                    String propertyType = etPropertyType.getText() != null ? etPropertyType.getText().toString().trim() : null;

                    setFilterCity(city.isEmpty() ? null : city);
                    setFilterPropertyType(propertyType.isEmpty() ? null : propertyType);

                    Double minPrice = null, maxPrice = null;
                    if (!minPriceStr.isEmpty()) {
                        try { minPrice = Double.parseDouble(minPriceStr); } catch (NumberFormatException ignored) {}
                    }
                    if (!maxPriceStr.isEmpty()) {
                        try { maxPrice = Double.parseDouble(maxPriceStr); } catch (NumberFormatException ignored) {}
                    }
                    setFilterPrice(minPrice, maxPrice);

                    applyFilters();
                    Toast.makeText(getContext(), "Фильтры применены", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    android.util.Log.e("DEBUG_FILTER", "Ошибка применения: " + e.getMessage());
                }
            });

            builder.setNeutralButton("Сбросить", (dialog, which) -> {
                resetFilters();
                Toast.makeText(getContext(), "Фильтры сброшены", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Отмена", null);
            builder.show();

        } catch (ClassCastException e) {
            android.util.Log.e("DEBUG_FILTER", "ClassCastException: " + e.getMessage());
            Toast.makeText(getContext(), "Ошибка интерфейса фильтра", Toast.LENGTH_LONG).show();
        }
    }

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

        ivPreview.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        if (selectedImageUri != null) {
            Glide.with(requireContext()).load(selectedImageUri).centerCrop().into(ivPreview);
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

    private void uploadImageAndCreateProperty(Property property) {
        try {
            byte[] bytes = readUriToBytes(selectedImageUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", "property.jpg", requestFile);

            RetrofitClient.getApiService().uploadFile(body).enqueue(new Callback<ResponseBody>() {
                @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String json = response.body().string();
                            String imageUrl = new JSONObject(json).optString("fileUrl", null);
                            createProperty(property, imageUrl);
                        } catch (Exception e) { createProperty(property, null); }
                    } else { createProperty(property, null); }
                }
                @Override public void onFailure(Call<ResponseBody> call, Throwable t) { createProperty(property, null); }
            });
        } catch (Exception e) { createProperty(property, null); }
    }

    private byte[] readUriToBytes(Uri uri) throws Exception {
        InputStream is = requireContext().getContentResolver().openInputStream(uri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead; byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) buffer.write(data, 0, nRead);
        return buffer.toByteArray();
    }

    private void createProperty(Property property, String imageUrl) {
        property.setImageUrl(imageUrl);
        RetrofitClient.getApiService().createProperty(property).enqueue(new Callback<Property>() {
            @Override public void onResponse(Call<Property> call, Response<Property> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), " Объявление создано!", Toast.LENGTH_LONG).show();
                    loadProperties();
                } else {
                    Toast.makeText(getContext(), " Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Property> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), " Сеть: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(Property property) {
        new AlertDialog.Builder(requireContext())
                .setTitle("🗑️ Удалить объявление")
                .setMessage("Удалить \"" + property.getTitle() + "\"? Это действие нельзя отменить!")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Удалить", (dialog, which) -> deleteProperty(property))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteProperty(Property property) {
        Toast.makeText(getContext(), " Удаление...", Toast.LENGTH_SHORT).show();
        RetrofitClient.getApiService().deleteProperty(property.getId()).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), " Удалено!", Toast.LENGTH_SHORT).show();
                    loadProperties();
                } else {
                    Toast.makeText(getContext(), " Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), " Сеть: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}