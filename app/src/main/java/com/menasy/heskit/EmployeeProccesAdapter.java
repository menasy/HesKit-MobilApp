package com.menasy.heskit;

import android.renderscript.ScriptGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.menasy.heskit.databinding.EmployeProcRecyclerBinding;

import java.util.ArrayList;

public class EmployeeProccesAdapter extends RecyclerView.Adapter<EmployeeProccesAdapter.EmplooyeProccesHolder> {
    private ArrayList<EmployeePayment> adapterEmpPymntList;
    private OnPaymentClickListener listener;

    public interface OnPaymentClickListener {
        void onPaymentClick(EmployeePayment payment, int position);
    }

    public void setOnPaymentClickListener(OnPaymentClickListener listener) {
        this.listener = listener;
    }
    public EmployeeProccesAdapter(ArrayList<EmployeePayment> adapterEmpPymntList) {
        this.adapterEmpPymntList = (adapterEmpPymntList != null) ? adapterEmpPymntList : new ArrayList<>();
    }

    @NonNull
    @Override
    public EmplooyeProccesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EmployeProcRecyclerBinding employeProcRecyclerBinding = EmployeProcRecyclerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new EmplooyeProccesHolder(employeProcRecyclerBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull EmplooyeProccesHolder holder, int position) {
        EmployeePayment empPayment = adapterEmpPymntList.get(position);
        holder.binding.empProcRecTxt.setText(empPayment.getPaymentInfo());

        holder.itemView.setOnClickListener(v -> {
            if(listener != null) {
                listener.onPaymentClick(empPayment, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (adapterEmpPymntList != null) ? adapterEmpPymntList.size() : 0;
    }

    public void updateList(ArrayList<EmployeePayment> newList) {
        this.adapterEmpPymntList.clear();
        if (newList != null) {
            this.adapterEmpPymntList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    public static class EmplooyeProccesHolder extends RecyclerView.ViewHolder {
        final EmployeProcRecyclerBinding binding;

        public EmplooyeProccesHolder(EmployeProcRecyclerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    public void addPayment(EmployeePayment payment) {
        adapterEmpPymntList.add(0, payment); // Listenin başına ekle
        notifyItemInserted(0); // Sadece eklenen öğeyi bildir
    }
}
