    package com.menasy.heskit;

    import android.view.LayoutInflater;
    import android.view.ViewGroup;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import com.menasy.heskit.databinding.EmployeProcRecyclerBinding;

    import java.io.Serializable;
    import java.util.ArrayList;

    public class EmployeeProccesAdapter extends RecyclerView.Adapter<EmployeeProccesAdapter.EmplooyeProccesHolder> implements Serializable
    {
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
            String paymentAmount = empPayment.getTakedMoneyStr();
            String info = empPayment.getPaymentInfo();
            holder.binding.empProcRecTxt.setText(paymentAmount);
            holder.binding.empProcRecInfoTxt.setText(info);

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
            this.adapterEmpPymntList = new ArrayList<>(newList); // Yeni liste oluştur
            notifyDataSetChanged(); // Tüm veri setini yenile
        }

        public static class EmplooyeProccesHolder extends RecyclerView.ViewHolder {
            final EmployeProcRecyclerBinding binding;

            public EmplooyeProccesHolder(EmployeProcRecyclerBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
