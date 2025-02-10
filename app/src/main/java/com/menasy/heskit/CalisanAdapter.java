package com.menasy.heskit;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
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
    private boolean isSelectionMode = false;
    private ArrayList<Employee> selectedEmployees = new ArrayList<>();

    public interface OnEmployeeClickListener {
        void onEmployeeClick(Employee employee);
    }

    public interface OnSelectionListener {
        void onSelectionChanged(int selectedCount);
    }

    private OnSelectionListener selectionListener;

    public CalisanAdapter(ArrayList<Employee> list) {
        if(list != null) this.adapterEmpList = list;
    }

    public void setOnEmployeeClickListener(OnEmployeeClickListener listener) {
        this.listener = listener;
    }

    public void setOnSelectionListener(OnSelectionListener listener) {
        this.selectionListener = listener;
    }

    @NonNull
    @Override
    public CalisanHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CalisanRecyclerBinding binding = CalisanRecyclerBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CalisanHolder(binding, this); // Adapter'ı gönder
    }

    @Override
    public void onBindViewHolder(@NonNull CalisanHolder holder, int position) {
        Employee emp = adapterEmpList.get(position);
        String value = emp.getTotalTransferAndPayment() + "₺";
        String text = emp.getNameAndSurname() + "    " + value;
        SpannableString spannable = new SpannableString(text);

        int start = text.indexOf(value);
        if (start != -1) {
            spannable.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(holder.itemView.getContext(), R.color.currency_green)),
                    start,
                    text.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        holder.binding.calisanRecText.setText(spannable);

        // Seçim durumuna göre arkaplan rengi
        int backgroundColor;
        if (emp.isSelected()) {
            backgroundColor = Color.parseColor("#4CAF50");
        } else if (emp.isDismissed()) {
            backgroundColor = Color.RED;
        } else {
            backgroundColor = Color.TRANSPARENT;
        }
        holder.itemView.setBackgroundColor(backgroundColor);

        holder.binding.selectionIndicator.setVisibility(emp.isSelected() ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(position);
            } else if (listener != null) {
                listener.onEmployeeClick(emp);
            }
        });
    }

    @Override
    public int getItemCount() {
        return adapterEmpList.size();
    }

    public void updateList(ArrayList<Employee> newList) {
        adapterEmpList = new ArrayList<>(newList);
        exitSelectionMode();
        notifyDataSetChanged();
    }

    public static class CalisanHolder extends RecyclerView.ViewHolder {
        CalisanRecyclerBinding binding;

        public CalisanHolder(CalisanRecyclerBinding binding, CalisanAdapter adapter) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnLongClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if(pos != RecyclerView.NO_POSITION && !adapter.isSelectionMode) {
                    adapter.enterSelectionMode();
                    adapter.toggleSelection(pos);
                }
                return true;
            });
        }
    }

    private void toggleSelection(int position) {
        if(position == RecyclerView.NO_POSITION || position >= adapterEmpList.size()) return;

        Employee emp = adapterEmpList.get(position);
        emp.setSelected(!emp.isSelected());

        if (emp.isSelected()) {
            selectedEmployees.add(emp);
        } else {
            selectedEmployees.remove(emp);
        }

        if(selectedEmployees.isEmpty()) {
            exitSelectionMode();
        } else {
            if(selectionListener != null) {
                selectionListener.onSelectionChanged(selectedEmployees.size());
            }
            notifyItemChanged(position);
        }
    }

    public void enterSelectionMode() {
        isSelectionMode = true;
        notifyDataSetChanged();
    }

    public void exitSelectionMode() {
        if(!isSelectionMode) return;

        isSelectionMode = false;
        for (Employee emp : adapterEmpList) {
            emp.setSelected(false);
        }
        selectedEmployees.clear();

        if (selectionListener != null) {
            selectionListener.onSelectionChanged(0);
        }
        notifyDataSetChanged();
    }

    public ArrayList<Employee> getSelectedEmployees() {
        return new ArrayList<>(selectedEmployees);
    }

    public void selectAll() {
        selectedEmployees.clear();
        selectedEmployees.addAll(adapterEmpList);
        for(Employee emp : adapterEmpList) {
            emp.setSelected(true);
        }
        notifyDataSetChanged();
        if(selectionListener != null) selectionListener.onSelectionChanged(selectedEmployees.size());
        notifyItemRangeChanged(0, adapterEmpList.size());
    }

    public void deselectAll() {
        selectedEmployees.clear();
        for(Employee emp : adapterEmpList) {
            emp.setSelected(false);
        }
        if(selectionListener != null) selectionListener.onSelectionChanged(0);
        exitSelectionMode();
        notifyItemRangeChanged(0, adapterEmpList.size());
    }

    public boolean isAllSelected() {
        return selectedEmployees.size() == adapterEmpList.size();
    }
    
}