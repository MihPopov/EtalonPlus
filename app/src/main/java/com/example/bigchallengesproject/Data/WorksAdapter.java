package com.example.bigchallengesproject.Data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bigchallengesproject.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class WorksAdapter extends RecyclerView.Adapter<WorksAdapter.ViewHolder> {

    private final List<Uri> workUris;
    private final List<Long> workSizes;
    private final Context context;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public WorksAdapter(Context context, List<Uri> workUris, List<Long> workSizes, OnItemClickListener listener) {
        this.context = context;
        this.workUris = workUris;
        this.workSizes = workSizes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.work_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Uri imageUri = workUris.get(position);
        long size = workSizes.get(position);

        holder.workPreview.setImageURI(imageUri);
        holder.textNumber.setText("Работа №" + (position + 1));
        holder.textSize.setText("Размер: " + formatFileSize(size));

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return workUris.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView workPreview, delete;
        TextView textNumber, textSize;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            workPreview = itemView.findViewById(R.id.work_preview);
            textNumber = itemView.findViewById(R.id.work_number);
            textSize = itemView.findViewById(R.id.work_size);
            delete = itemView.findViewById(R.id.delete);
        }
    }

    private String formatFileSize(long size) {
        DecimalFormat df = new DecimalFormat("#.##");
        float kb = size / 1024f;
        float mb = kb / 1024f;
        return mb > 1 ? df.format(mb) + " MB" : df.format(kb) + " KB";
    }
}