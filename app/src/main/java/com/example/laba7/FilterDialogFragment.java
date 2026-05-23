package com.example.laba7;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.laba7.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class FilterDialogFragment extends BottomSheetDialogFragment {

    private TextInputEditText etCity, etPriceMin, etPriceMax;
    private Spinner spinnerType;
    private Button btnApply, btnReset;

    public interface OnFilterApplyListener {
        void onFilterApplied(String city, Double minPrice, Double maxPrice, String type);
    }

    private OnFilterApplyListener listener;

    public void setListener(OnFilterApplyListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_filter, container, false);

        etCity = view.findViewById(R.id.etCity);
        etPriceMin = view.findViewById(R.id.etPriceMin);
        etPriceMax = view.findViewById(R.id.etPriceMax);
        spinnerType = view.findViewById(R.id.spinnerType);
        btnApply = view.findViewById(R.id.btnApply);
        btnReset = view.findViewById(R.id.btnReset);

        // Настройка Spinner
        String[] types = {"Любой", "Квартира", "Дом", "Комната", "Земля", "Коммерческая"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        btnReset.setOnClickListener(v -> {
            etCity.setText("");
            etPriceMin.setText("");
            etPriceMax.setText("");
            spinnerType.setSelection(0);
            Toast.makeText(getContext(), "Фильтры сброшены", Toast.LENGTH_SHORT).show();
        });

        btnApply.setOnClickListener(v -> {
            String city = etCity.getText().toString().trim();
            Double minPrice = parsePrice(etPriceMin.getText().toString());
            Double maxPrice = parsePrice(etPriceMax.getText().toString());
            String type = spinnerType.getSelectedItem().toString();

            if (type.equals("Любой")) type = null;

            if (listener != null) {
                listener.onFilterApplied(city, minPrice, maxPrice, type);
            }
            dismiss();
        });

        return view;
    }

    private Double parsePrice(String text) {
        try {
            return text.isEmpty() ? null : Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}