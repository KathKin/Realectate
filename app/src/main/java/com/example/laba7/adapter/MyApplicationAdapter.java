package com.example.laba7.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.laba7.R;
import com.example.laba7.model.Application;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class MyApplicationAdapter extends RecyclerView.Adapter<MyApplicationAdapter.ViewHolder> {

    private List<Application> applicationList;
    private OnNoteSavedListener noteListener;
    private OnDeleteListener deleteListener;

    public interface OnNoteSavedListener {
        void onNoteSaved(Application app, String note);
    }

    public interface OnDeleteListener {
        void onDelete(Application app);
    }

    public MyApplicationAdapter(List<Application> list, OnNoteSavedListener noteListener, OnDeleteListener deleteListener) {
        this.applicationList = list != null ? list : new java.util.ArrayList<>();
        this.noteListener = noteListener;
        this.deleteListener = deleteListener;
    }

    public void setApplications(List<Application> applications) {
        this.applicationList = applications != null ? applications : new java.util.ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_application, parent, false);
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

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPropertyImage;
        TextView tvTitle, tvPrice, tvLocation, tvType;
        TextInputEditText etNote;
        MaterialButton btnSaveNote, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvType = itemView.findViewById(R.id.tvType);
            etNote = itemView.findViewById(R.id.etNote);
            btnSaveNote = itemView.findViewById(R.id.btnSaveNote);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            ivPropertyImage = itemView.findViewById(R.id.ivPropertyImage);
        }

        public void bind(Application app) {
            if (app.getProperty() != null) {
                tvTitle.setText(app.getProperty().getTitle());
                String price = String.format("%,d ₽", app.getProperty().getPrice().longValue());
                tvPrice.setText(price);
                tvLocation.setText(app.getProperty().getCity() + ", " + app.getProperty().getAddress());

                String type = "SALE".equals(app.getProperty().getType()) ? "Продажа" : "Аренда";
                tvType.setText(type);
                tvType.setBackgroundColor("SALE".equals(app.getProperty().getType()) ? 0xFF4CAF50 : 0xFFFF9800);

                // 🔧 Загрузка фото
                String imageUrl = app.getProperty().getImageUrl();
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
            } else {
                tvTitle.setText("Объявление #" + app.getPropertyId());
                tvPrice.setText("");
                tvLocation.setText("");
                tvType.setText("");
                tvType.setVisibility(View.GONE);
                ivPropertyImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            etNote.setText(app.getNote() != null ? app.getNote() : "");

            btnSaveNote.setOnClickListener(v -> {
                String note = etNote.getText().toString().trim();
                if (noteListener != null) noteListener.onNoteSaved(app, note);
            });

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDelete(app);
            });
        }
    }
}
