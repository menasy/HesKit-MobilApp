package com.menasy.heskit;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.menasy.heskit.databinding.CalisanRecyclerBinding;
import java.io.Serializable;
import java.util.ArrayList;

public class CalisanAdapter extends RecyclerView.Adapter<CalisanAdapter.CalisanHolder> implements Serializable {

    private ArrayList<Employee> adapterEmpList;
    private OnEmployeeClickListener listener;

    public interface OnEmployeeClickListener {
        void onEmployeeClick(Employee employee);
    }

    public CalisanAdapter(ArrayList<Employee> adapterEmpList) {
        this.adapterEmpList = adapterEmpList;
    }

    public void setOnEmployeeClickListener(OnEmployeeClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CalisanHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CalisanRecyclerBinding binding = CalisanRecyclerBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CalisanHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CalisanHolder holder, int position) {
        Employee emp = adapterEmpList.get(position);
        String text = emp.getNameAndSurname() + "    " + emp.getTotalMoney() + "₺";
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(holder.itemView.getContext(), R.color.currency_green)),
                text.lastIndexOf("-") + 2,
                text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        holder.binding.calisanRecText.setText(text);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEmployeeClick(emp);
            }
        });
    }

    @Override
    public int getItemCount() {
        return adapterEmpList.size();
    }

    public void updateList(ArrayList<Employee> newList) {
        adapterEmpList.clear();
        adapterEmpList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class CalisanHolder extends RecyclerView.ViewHolder {
        CalisanRecyclerBinding binding;

        public CalisanHolder(CalisanRecyclerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}