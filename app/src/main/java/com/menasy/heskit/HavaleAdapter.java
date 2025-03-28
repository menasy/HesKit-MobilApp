package com.menasy.heskit;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.menasy.heskit.databinding.TransferRecyclerBinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HavaleAdapter extends RecyclerView.Adapter<HavaleAdapter.HavaleHolder> implements Serializable {

    private ArrayList<Transfer> adapterPaymentList;
    private OnPaymentClickListener listener;

    public interface OnPaymentClickListener {
        void onPaymentClick(Transfer payment, int position);
    }


    public HavaleAdapter(ArrayList<Transfer> adapterPaymentList, OnPaymentClickListener listener) {
        this.adapterPaymentList = (adapterPaymentList != null) ? adapterPaymentList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public HavaleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TransferRecyclerBinding binding = TransferRecyclerBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new HavaleHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HavaleHolder holder, int position) {
        Transfer transfer = adapterPaymentList.get(position);
        String amount = "\uD83D\uDCB0 " + transfer.getAmountTransfer() + "₺";
        String info = transfer.getSentToPerson() + "  →  " + transfer.getTransferDate();
        holder.binding.transferRecText.setText(amount);
        holder.binding.transferRecInfoTxt.setText(info);

        holder.itemView.setOnClickListener(v -> {
            if(listener != null && position != RecyclerView.NO_POSITION) {
                listener.onPaymentClick(transfer, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (adapterPaymentList != null) ? adapterPaymentList.size() : 0;
    }

    public void updateList(List<Transfer> newList) {
        this.adapterPaymentList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void addPayment(Transfer payment) {
        if(adapterPaymentList == null) adapterPaymentList = new ArrayList<>();
        adapterPaymentList.add(0, payment);
        notifyItemInserted(0);
        notifyDataSetChanged();
    }

    public static class HavaleHolder extends RecyclerView.ViewHolder {
        final TransferRecyclerBinding binding;

        public HavaleHolder(@NonNull TransferRecyclerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
