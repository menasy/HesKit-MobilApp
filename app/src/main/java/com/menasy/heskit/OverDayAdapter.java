package com.menasy.heskit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.menasy.heskit.databinding.OverDayRecyclerBinding;

import java.util.ArrayList;
import java.util.Collections;

public class OverDayAdapter extends RecyclerView.Adapter<OverDayAdapter.ViewHolder> {

    private ArrayList<OverDay> overDays;
    private OnOverDayClickListener listener;

    public interface OnOverDayClickListener {
        void onOverDayClick(OverDay overDay, int position);
    }

    public OverDayAdapter(ArrayList<OverDay> overDays, OnOverDayClickListener listener) {
        this.overDays = overDays;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        OverDayRecyclerBinding binding = OverDayRecyclerBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OverDay overDay = overDays.get(position);
        holder.binding.overDayRecText.setText("📌 " + overDay.getDaysAmount() + " Saat");
        holder.binding.overDayRecDateTxt.setText(overDay.getDate());

        holder.itemView.setOnClickListener(v -> {
            if(listener != null && position != RecyclerView.NO_POSITION) {
                listener.onOverDayClick(overDay, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return overDays.size();
    }

    public void updateList(ArrayList<OverDay> newList) {
        // Tarihe göre ters sırala (en yeni en üstte)
        Collections.sort(newList, (o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        this.overDays = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final OverDayRecyclerBinding binding;

        public ViewHolder(@NonNull OverDayRecyclerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    public void addOverDay(OverDay newOverDay) {
        this.overDays.add(0, newOverDay);
        notifyItemInserted(0);
    }
}
