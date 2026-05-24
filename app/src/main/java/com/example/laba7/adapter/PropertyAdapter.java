package com.example.laba7.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.laba7.R;
import com.example.laba7.model.Property;
import com.google.android.material.button.MaterialButton;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.ViewHolder> {

    private List<Property> propertyList = new ArrayList<>();
    private OnItemClickListener listener;
    private String currentUserRole; // "CLIENT" или "AGENT"

    public interface OnItemClickListener {
        void onItemClick(Property property);
        void onItemRespond(Property property);
    }

    public PropertyAdapter(OnItemClickListener listener, String currentUserRole) {
        this.listener = listener;
        this.currentUserRole = currentUserRole;
    }

    public void setProperties(List<Property> properties) {
        this.propertyList = properties != null ? properties : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_property, parent, false);
        return new ViewHolder(view, currentUserRole); // ← Передаём роль в ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Property property = propertyList.get(position);
        holder.bind(property, listener);
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCity, tvPrice, tvRooms, tvArea, tvType;
        MaterialButton btnRespond;
        private String currentUserRole; // ← Поле для хранения роли

        public ViewHolder(@NonNull View itemView, String currentUserRole) {
            super(itemView);
            this.currentUserRole = currentUserRole; // ← Сохраняем роль
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvRooms = itemView.findViewById(R.id.tvRooms);
            tvArea = itemView.findViewById(R.id.tvArea);
            tvType = itemView.findViewById(R.id.tvType);
            btnRespond = itemView.findViewById(R.id.btnRespond);
        }

        public void bind(Property property, OnItemClickListener listener) {
            tvTitle.setText(property.getTitle());
            tvCity.setText(property.getCity() + ", " + property.getAddress());

            NumberFormat format = NumberFormat.getInstance(new Locale("ru", "KZ"));
            tvPrice.setText(format.format(property.getPrice()) + " ₸");

            tvRooms.setText("Комнат: " + property.getRooms());
            tvArea.setText("Площадь: " + property.getArea() + " м²");

            String typeText = "SALE".equals(property.getType()) ? "Продажа" : "Аренда";
            tvType.setText(typeText);

            if ("RENT".equals(property.getType())) {
                tvType.setBackgroundColor(0xFFFF9800);
            } else {
                tvType.setBackgroundColor(0xFF4CAF50);
            }

            // 🔥 Показываем кнопку только клиентам
            if ("CLIENT".equals(currentUserRole)) {
                btnRespond.setVisibility(View.VISIBLE);
                btnRespond.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemRespond(property);
                    }
                });
            } else {
                btnRespond.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(property);
                }
            });
        }
    }
}