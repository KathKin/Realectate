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
import com.example.laba7.dto.PropertyWithApplicationsDto;
import java.util.List;

public class AgentPropertyAdapter extends RecyclerView.Adapter<AgentPropertyAdapter.ViewHolder> {

    private List<PropertyWithApplicationsDto> list;
    private OnPropertyClickListener listener;

    public interface OnPropertyClickListener {
        void onClick(PropertyWithApplicationsDto property);
    }

    public AgentPropertyAdapter(List<PropertyWithApplicationsDto> list, OnPropertyClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_agent_property, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) { holder.bind(list.get(position)); }
    @Override public int getItemCount() { return list.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivPropertyImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCount = itemView.findViewById(R.id.tvCount);
        }

        public void bind(PropertyWithApplicationsDto dto) {
            tvTitle.setText(dto.getPropertyTitle());
            tvCount.setText(dto.getApplicationsCount() + " " +
                    (dto.getApplicationsCount() == 1 ? "заявка" :
                            dto.getApplicationsCount() < 5 ? "заявки" : "заявок"));

            String url = dto.getPropertyImageUrl();
            if (url != null && !url.isEmpty()) {
                Glide.with(itemView.getContext()).load(url).centerCrop().into(ivImage);
            } else {
                ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(dto);
            });
        }
    }
}
