package com.mihpopov.etalonplus.Presentation;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mihpopov.etalonplus.R;
import com.google.android.material.card.MaterialCardView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для отображения списка работ в RecyclerView.
 */
public class WorkAdapter extends RecyclerView.Adapter<WorkAdapter.WorkViewHolder> {

    private final List<WorkItem> works;
    private final OnPageAddListener pageAddListener;

    public interface OnPageAddListener {
        void onAddFromGallery(int workPosition);
        void onAddFromCamera(int workPosition);
    }

    public static class WorkItem {
        public List<PageItem> pages = new ArrayList<>();

        public void addPage(Uri uri, long size) {
            pages.add(new PageItem(uri, size));
        }

        public List<PageItem> getPages() {
            return pages;
        }
    }

    public static class PageItem {
        public Uri uri;
        public long size;

        public PageItem(Uri uri, long size) {
            this.uri = uri;
            this.size = size;
        }

        public Uri getUri() {
            return uri;
        }
    }

    public WorkAdapter(List<WorkItem> works, OnPageAddListener pageAddListener) {
        this.works = works;
        this.pageAddListener = pageAddListener;
    }

    @NonNull
    @Override
    public WorkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.work_card, parent, false);
        return new WorkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkViewHolder holder, @SuppressLint("RecyclerView") int position) {
        WorkItem workItem = works.get(position);

        holder.workNumber.setText("Работа №" + (position + 1));

        PageAdapter pageAdapter = new PageAdapter(workItem.pages, new PageAdapter.OnPageDeleteListener() {
            @Override
            public void onPageDeleted(int pagePosition, boolean isNowEmpty) {
                if (isNowEmpty) {
                    works.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, works.size());
                }
            }
        });

        holder.pagesRecycler.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.pagesRecycler.setAdapter(pageAdapter);

        holder.addFromGallery.setOnClickListener(v -> {
            pageAddListener.onAddFromGallery(position);
        });

        holder.addFromCamera.setOnClickListener(v -> {
            pageAddListener.onAddFromCamera(position);
        });
    }

    @Override
    public int getItemCount() {
        return works.size();
    }

    public static class WorkViewHolder extends RecyclerView.ViewHolder {
        TextView workNumber;
        RecyclerView pagesRecycler;
        MaterialCardView addFromGallery;
        MaterialCardView addFromCamera;

        public WorkViewHolder(@NonNull View itemView) {
            super(itemView);
            workNumber = itemView.findViewById(R.id.work_number);
            pagesRecycler = itemView.findViewById(R.id.pages_recycler);
            addFromGallery = itemView.findViewById(R.id.add_page_upload_card);
            addFromCamera = itemView.findViewById(R.id.add_page_camera_card);
        }
    }
}