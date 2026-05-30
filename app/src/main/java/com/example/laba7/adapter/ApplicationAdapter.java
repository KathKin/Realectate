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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private List<Application> applicationList = new ArrayList<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public interface OnNoteSavedListener {
        void onNoteSaved(Application app, String note);
    }

    public interface OnDeleteListener {
        void onDelete(Application app);
    }

    public ApplicationAdapter(List<Application> applications,
                              OnNoteSavedListener noteListener,
                              OnDeleteListener deleteListener) {
        if (applications != null) {
            this.applicationList = applications;
        }
        this.noteListener = noteListener;
        this.deleteListener = deleteListener;
    }

    public ApplicationAdapter(List<Application> applications) {
        this(applications, null, null);
    }

    private OnNoteSavedListener noteListener;
    private OnDeleteListener deleteListener;

    public void setApplications(List<Application> applications) {
        this.applicationList = applications != null ? applications : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_application, parent, false);
        return new ViewHolder(view, noteListener, deleteListener);
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
        TextView tvClientName, tvClientPhone, tvPropertyId, tvMessage, tvDate;
        TextInputEditText etNote;
        MaterialButton btnSaveNote, btnDelete;

        private final OnNoteSavedListener noteListener;
        private final OnDeleteListener deleteListener;

        public ViewHolder(@NonNull View itemView,
                          OnNoteSavedListener noteListener,
                          OnDeleteListener deleteListener) {
            super(itemView);
            this.noteListener = noteListener;
            this.deleteListener = deleteListener;

            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvClientPhone = itemView.findViewById(R.id.tvClientPhone);
            tvPropertyId = itemView.findViewById(R.id.tvPropertyId);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvDate = itemView.findViewById(R.id.tvDate);

            etNote = itemView.findViewById(R.id.etNote);
            btnSaveNote = itemView.findViewById(R.id.btnSaveNote);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Application app) {
            if (btnSaveNote != null) {
                btnSaveNote.setOnClickListener(null);
            }
            if (btnDelete != null) {
                btnDelete.setOnClickListener(null);
            }

            tvClientName.setText(app.getClientName() != null ? app.getClientName() : "Неизвестно");
            tvClientPhone.setText(app.getClientPhone() != null ? app.getClientPhone() : "Нет телефона");
            tvPropertyId.setText("Объявление #" + app.getPropertyId());
            tvMessage.setText(app.getMessage() != null ? app.getMessage() : "Без сообщения");

            if (app.getCreatedAt() != null) {
                try {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(app.getCreatedAt().replace("Z", ""));
                    tvDate.setText(dateFormat.format(java.util.Date.from(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant())));
                } catch (Exception e) {
                    tvDate.setText(app.getCreatedAt());
                }
            }

            if (etNote != null) {
                etNote.setText(app.getAgentNote() != null ? app.getAgentNote() : "");
                etNote.setSelection(etNote.getText().length());
            }

            if (btnSaveNote != null && noteListener != null) {
                btnSaveNote.setOnClickListener(v -> {
                    if (etNote != null) {
                        String note = etNote.getText().toString().trim();
                        noteListener.onNoteSaved(app, note);
                    }
                });
            }

            if (btnDelete != null && deleteListener != null) {
                btnDelete.setOnClickListener(v -> {
                    deleteListener.onDelete(app);
                });
            }
        }
    }
}