package com.menasy.heskit;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import static com.menasy.heskit.Calisanlar.empList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.menasy.heskit.databinding.CalisanRecyclerBinding;

import java.util.ArrayList;

public class CalisanAdapter extends RecyclerView.Adapter<CalisanAdapter.CalisanHolder>
{
    ArrayList <Employee> adapterEmpList;

    public CalisanAdapter(ArrayList<Employee> adapterEmpList) {
        this.adapterEmpList = adapterEmpList;
    }

    @NonNull
    @Override
    public CalisanHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CalisanRecyclerBinding calisanRecyclerBinding = CalisanRecyclerBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new CalisanHolder(calisanRecyclerBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull CalisanHolder holder, int position) {
        Employee emp = adapterEmpList.get(position);
        holder.binding.calisanRecText.setText(emp.getNameAndSurname() + "\t\t" + String.valueOf(emp.getTotalMoney()) + "₺");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(holder.itemView.getContext(),MainActivity2.class);

                intent.putExtra("fragment","EmpProcces");
                intent.putExtra("Employee",emp);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return adapterEmpList.size();
    }

    public class CalisanHolder extends RecyclerView.ViewHolder
    {
        private CalisanRecyclerBinding binding;
        public CalisanHolder(CalisanRecyclerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    public void updateList(ArrayList<Employee> newList) {
        empList = newList;
        notifyDataSetChanged();
    }
}
