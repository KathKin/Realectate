package com.example.laba7.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
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
    private String currentUserRole;
    private Long currentUserId;

    public interface OnItemClickListener {
        void onItemClick(Property property);
        void onItemRespond(Property property);
        void onItemDelete(Property property);
    }

    public PropertyAdapter(OnItemClickListener listener, String currentUserRole, Long currentUserId) {
        this.listener = listener;
        this.currentUserRole = currentUserRole;
        this.currentUserId = currentUserId;
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
        return new ViewHolder(view, currentUserRole, currentUserId);
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
        ImageView ivPropertyImage;
        TextView tvTitle, tvCity, tvPrice, tvRooms, tvArea, tvType;
        MaterialButton btnRespond;
        MaterialButton btnDelete;

        private String currentUserRole;
        private Long currentUserId;

        public ViewHolder(@NonNull View itemView, String currentUserRole, Long currentUserId) {
            super(itemView);
            this.currentUserRole = currentUserRole;
            this.currentUserId = currentUserId;

            ivPropertyImage = itemView.findViewById(R.id.ivPropertyImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvRooms = itemView.findViewById(R.id.tvRooms);
            tvArea = itemView.findViewById(R.id.tvArea);
            tvType = itemView.findViewById(R.id.tvType);
            btnRespond = itemView.findViewById(R.id.btnRespond);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Property property, OnItemClickListener listener) {
            // Загрузка текста
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

            String imageUrl = property.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .centerCrop()
                        .into(ivPropertyImage);
            } else {
                ivPropertyImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            Long propertyAgentId = property.getAgentId();

            if (propertyAgentId == null && property.getAgent() != null) {
                propertyAgentId = property.getAgent().getId();
            }

            boolean isOwner = "AGENT".equals(currentUserRole) &&
                    propertyAgentId != null &&
                    propertyAgentId.equals(currentUserId);

            if (isOwner) {
                btnDelete.setVisibility(View.VISIBLE);
                btnRespond.setVisibility(View.GONE);

                btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemDelete(property);
                    }
                });
            } else {
                btnDelete.setVisibility(View.GONE);

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
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(property);
                }
            });
        }
    }
}