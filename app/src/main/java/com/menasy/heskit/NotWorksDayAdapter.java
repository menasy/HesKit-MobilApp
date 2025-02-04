package com.menasy.heskit;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.menasy.heskit.databinding.FragmentNotWorksDayRecyclerBinding;
import com.menasy.heskit.databinding.FragmentNotWorksDayRecyclerBinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NotWorksDayAdapter extends RecyclerView.Adapter<NotWorksDayAdapter.NotWorksDayHolder> implements Serializable {

    private ArrayList<NotWorksDay> days;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(NotWorksDay day, int position);
    }

    public NotWorksDayAdapter(ArrayList<NotWorksDay> days, OnItemClickListener listener) {
        this.days = days != null ? days : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotWorksDayHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FragmentNotWorksDayRecyclerBinding binding = FragmentNotWorksDayRecyclerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new NotWorksDayHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotWorksDayHolder holder, int position) {
        NotWorksDay day = days.get(position);

        String info;

        if (day.getReason().matches(""))
            info = day.getDate();
        else
            info = day.getReason() + " → " + day.getDate();
        holder.binding.notWorksDayRecTxt1.setText("📌 " + day.getDays() + " Gün");
        holder.binding.notWorksDayRecTxt2.setText(info);

        holder.itemView.setOnClickListener(v -> {
            if(listener != null) listener.onItemClick(day, position);
        });
    }

    @Override
    public int getItemCount() { return days.size(); }

    public void updateList(List<NotWorksDay> newList) {
        days = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void addPayment(NotWorksDay day) {
        days.add(0, day);
        notifyItemInserted(0);
    }

    static class NotWorksDayHolder extends RecyclerView.ViewHolder {
        final FragmentNotWorksDayRecyclerBinding binding;
        NotWorksDayHolder(FragmentNotWorksDayRecyclerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
