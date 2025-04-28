package com.example.bigchallengesproject.Presentation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bigchallengesproject.Data.Etalon;
import com.example.bigchallengesproject.R;

import java.util.List;

public class EtalonsUseAdapter extends RecyclerView.Adapter<EtalonsUseAdapter.EtalonViewHolder> {

    private Context context;
    private List<Etalon> etalonList;
    private OnEtalonUseListener useListener;

    public interface OnEtalonUseListener {
        void onUse(Etalon etalon);
    }

    public EtalonsUseAdapter(Context context, List<Etalon> etalonList, OnEtalonUseListener useListener) {
        this.context = context;
        this.etalonList = etalonList;
        this.useListener = useListener;
    }

    @NonNull
    @Override
    public EtalonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.etalon_use_card, parent, false);
        return new EtalonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EtalonViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Etalon etalon = etalonList.get(position);

        holder.etalonName.setText(etalon.getName());
        holder.etalonDate.setText("Дата создания: " + etalon.getCreationDate());

        if (etalon.getIcon() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(etalon.getIcon(), 0, etalon.getIcon().length);
            holder.etalonIcon.setImageBitmap(bitmap);
        } else {
            holder.etalonIcon.setImageResource(R.drawable.upload);
        }

        holder.useView.setOnClickListener(v -> {
            if (useListener != null) useListener.onUse(etalon);
        });
    }

    @Override
    public int getItemCount() {
        return etalonList.size();
    }

    public static class EtalonViewHolder extends RecyclerView.ViewHolder {
        TextView etalonName, etalonDate;
        ImageView etalonIcon, useView;

        public EtalonViewHolder(@NonNull View itemView) {
            super(itemView);
            etalonName = itemView.findViewById(R.id.etalon_use_name);
            etalonDate = itemView.findViewById(R.id.etalon_use_date);
            etalonIcon = itemView.findViewById(R.id.etalon_use_icon);
            useView = itemView.findViewById(R.id.etalon_use);
        }
    }
}