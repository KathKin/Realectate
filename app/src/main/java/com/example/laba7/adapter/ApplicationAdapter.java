package com.example.laba7.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.laba7.R;
import com.example.laba7.model.Application;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private List<Application> applicationList = new ArrayList<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public ApplicationAdapter(List<Application> applications) {
        if (applications != null) {
            this.applicationList = applications;
        }
    }

    public void setApplications(List<Application> applications) {
        this.applicationList = applications != null ? applications : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Application app = applicationList.get(position);
        holder.bind(app);
    }

    @Override
    public int getItemCount() {
        return applicationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvClientPhone, tvPropertyId, tvMessage, tvStatus, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvClientPhone = itemView.findViewById(R.id.tvClientPhone);
            tvPropertyId = itemView.findViewById(R.id.tvPropertyId);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        public void bind(Application app) {
            tvClientName.setText(app.getClientName() != null ? app.getClientName() : "Неизвестно");
            tvClientPhone.setText(app.getClientPhone() != null ? app.getClientPhone() : "Нет телефона");
            tvPropertyId.setText("Объявление #" + app.getPropertyId());
            tvMessage.setText(app.getMessage() != null ? app.getMessage() : "Без сообщения");

            // Форматирование даты
            if (app.getCreatedAt() != null) {
                try {
                    // Парсим ISO формат от сервера
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(app.getCreatedAt().replace("Z", ""));
                    tvDate.setText(dateFormat.format(java.util.Date.from(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant())));
                } catch (Exception e) {
                    tvDate.setText(app.getCreatedAt());
                }
            }

            // Цвет статуса
            String status = app.getStatus() != null ? app.getStatus().toUpperCase() : "NEW";
            tvStatus.setText(status);
            switch (status) {
                case "NEW":
                    tvStatus.setBackgroundColor(0xFFFF9800); // Оранжевый
                    break;
                case "VIEWED":
                    tvStatus.setBackgroundColor(0xFF2196F3); // Синий
                    break;
                case "CONTACTED":
                    tvStatus.setBackgroundColor(0xFF4CAF50); // Зелёный
                    break;
                default:
                    tvStatus.setBackgroundColor(0xFF9E9E9E); // Серый
            }
        }
    }
}