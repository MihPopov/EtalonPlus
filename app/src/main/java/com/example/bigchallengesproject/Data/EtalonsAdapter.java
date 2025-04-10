package com.example.bigchallengesproject.Data;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bigchallengesproject.Activities.EtalonActivity;
import com.example.bigchallengesproject.R;

import java.util.List;

public class EtalonsAdapter extends RecyclerView.Adapter<EtalonsAdapter.EtalonViewHolder> {

    private Context context;
    private List<Etalon> etalonList;
    private OnEtalonDeleteListener deleteListener;

    public interface OnEtalonDeleteListener {
        void onDelete(int position);
    }

    public EtalonsAdapter(Context context, List<Etalon> etalonList, OnEtalonDeleteListener deleteListener) {
        this.context = context;
        this.etalonList = etalonList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public EtalonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.etalon_card, parent, false);
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

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EtalonActivity.class);
            intent.putExtra("etalon_id", etalon.getId());
            context.startActivity(intent);
        });

        holder.etalonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Удаление эталона")
                        .setMessage("Вы уверены, что хотите удалить этот эталон?")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            if (deleteListener != null) deleteListener.onDelete(position);
                        })
                        .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return etalonList.size();
    }

    public static class EtalonViewHolder extends RecyclerView.ViewHolder {
        TextView etalonName, etalonDate;
        ImageView etalonIcon, etalonDelete;
        CardView cardView;

        public EtalonViewHolder(@NonNull View itemView) {
            super(itemView);
            etalonName = itemView.findViewById(R.id.etalon_name);
            etalonDate = itemView.findViewById(R.id.etalon_date);
            etalonIcon = itemView.findViewById(R.id.etalon_icon);
            etalonDelete = itemView.findViewById(R.id.etalon_delete);
            cardView = (CardView) itemView;
        }
    }
}