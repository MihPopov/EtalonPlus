package com.mihpopov.etalonplus.Presentation;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mihpopov.etalonplus.R;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Адаптер для отображения списка страниц работы (и не только) в RecyclerView с возможностью из удаления.
 */
public class PageAdapter extends RecyclerView.Adapter<PageAdapter.PageViewHolder> {

    private final List<WorkAdapter.PageItem> pages;
    private final OnPageDeleteListener listener;

    public interface OnPageDeleteListener {
        void onPageDeleted(int pagePosition, boolean isNowEmpty);
    }

    public PageAdapter(List<WorkAdapter.PageItem> pages, OnPageDeleteListener listener) {
        this.pages = pages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, @SuppressLint("RecyclerView") int position) {
        WorkAdapter.PageItem page = pages.get(position);

        holder.pagePreview.setImageURI(page.uri);
        holder.pageNumber.setText("Страница №" + (position + 1));
        holder.pageSize.setText("Размер: " + formatFileSize(page.size));

        holder.pageDelete.setOnClickListener(v -> {
            pages.remove(position);
            notifyItemRemoved(position);
            if (listener != null) listener.onPageDeleted(position, pages.isEmpty());
            notifyItemRangeChanged(position, pages.size());
        });
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    public static class PageViewHolder extends RecyclerView.ViewHolder {
        ImageView pagePreview, pageDelete;
        TextView pageNumber, pageSize;

        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            pagePreview = itemView.findViewById(R.id.page_preview);
            pageNumber = itemView.findViewById(R.id.page_number);
            pageSize = itemView.findViewById(R.id.page_size);
            pageDelete = itemView.findViewById(R.id.page_delete);
        }
    }

    private String formatFileSize(long size) {
        DecimalFormat df = new DecimalFormat("#.##");
        float kb = size / 1024f;
        float mb = kb / 1024f;
        return mb > 1 ? df.format(mb) + " MB" : df.format(kb) + " KB";
    }
}