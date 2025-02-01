package com.menasy.heskit;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.menasy.heskit.databinding.FragmentHavaleBinding;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class Havale extends Fragment {

    private FragmentHavaleBinding binding;
    private HavaleAdapter adapter;
    private ArrayList<Transfer> transferList = new ArrayList<>();
    private DBHelper dbHelper;

    public Havale() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHavaleBinding.inflate(inflater, container, false);
        dbHelper = Singleton.getInstance().getDataBase();

        setupRecyclerView();
        setupClickListeners();
        loadTransfers();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        binding.transferRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HavaleAdapter(transferList, (transfer, position) ->
                showDeleteConfirmationDialog(transfer, position)
        );
        binding.transferRecyclerView.setAdapter(adapter);
    }

    private void showDeleteConfirmationDialog(Transfer transfer, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Transfer Sil")
                .setMessage(transfer.getAmountTransfer() + "₺ tutarındaki transferi silmek istediğinize emin misiniz?")
                .setPositiveButton("Evet", (dialog, which) -> deleteTransfer(transfer, position))
                .setNegativeButton("Hayır", null)
                .show();
    }
    private void deleteTransfer(Transfer transfer, int position) {
        Executors.newSingleThreadExecutor().execute(() -> {
            int deletedRows = dbHelper.deleteTransfer(transfer.getId());

            requireActivity().runOnUiThread(() -> {
                if(deletedRows > 0) {
                    transferList.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateUI();
                    Toast.makeText(getContext(), "Transfer silindi", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Silme işlemi başarısız!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    private void setupClickListeners() {
        binding.addTransfer.setOnClickListener(v -> addTransfer());
        binding.cleanAllTransfer.setOnClickListener(v -> showCleanAllDialog());
    }

    private void addTransfer() {
        String amountStr = binding.TransferAmountTxt.getText().toString().trim();
        String recipient = binding.sentPersonTxt.getText().toString().trim();

        if (!isValidInput(amountStr, recipient)) return;

        int amount = Integer.parseInt(amountStr);
        String currentDate = DateUtils.getCurrentDate();

        Executors.newSingleThreadExecutor().execute(() -> {
            long id = dbHelper.addTransfer(amount, currentDate, recipient);

            requireActivity().runOnUiThread(() -> {
                if (id != -1) {
                    Transfer newTransfer = new Transfer(amount, currentDate, recipient);
                    newTransfer.setId((int) id);
                    transferList.add(0, newTransfer);
                    adapter.notifyItemInserted(0);
                    clearInputFields();
                    updateUI();
                } else {
                    Toast.makeText(getContext(), "Havale eklenemedi!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private boolean isValidInput(String amountStr, String recipient) {
        if (amountStr.isEmpty() || !amountStr.matches("\\d+")) {
            showError("Geçersiz miktar!");
            return false;
        }

        int amount = Integer.parseInt(amountStr);
        if (amount <= 0) {
            showError("Miktar 0'dan büyük olmalı!");
            return false;
        }

        if (recipient.isEmpty()) {
            showError("Alıcı adı giriniz!");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void clearInputFields() {
        binding.TransferAmountTxt.setText("");
        binding.sentPersonTxt.setText("");
    }

    private void updateUI() {
        binding.transferCountTxt.setText("Havale Sayısı: " + transferList.size());
        Start.refreshTransferTotal();
    }

    private void showTransferDetails(Transfer transfer) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Havale Detayı")
                .setMessage(
                        "Alıcı: " + transfer.getSentToPerson() + "\n" +
                                "Miktar: " + transfer.getAmountTransfer() + "₺\n" +
                                "Tarih: " + transfer.getTransferDate()
                )
                .setPositiveButton("Tamam", null)
                .show();
    }

    private void showCleanAllDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Tümünü Sil")
                .setMessage("Tüm havale geçmişini silmek istediğinize emin misiniz?")
                .setPositiveButton("Evet", (dialog, which) -> cleanAllTransfers())
                .setNegativeButton("İptal", null)
                .show();
    }

    private void cleanAllTransfers() {
        Executors.newSingleThreadExecutor().execute(() -> {
            dbHelper.deleteAllTransfers();

            requireActivity().runOnUiThread(() -> {
                transferList.clear();
                adapter.notifyDataSetChanged();
                updateUI();
                Toast.makeText(getContext(), "Tüm havaleler silindi", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadTransfers() {
        Executors.newSingleThreadExecutor().execute(() -> {
            ArrayList<Transfer> transfers = dbHelper.getAllTransfers();

            requireActivity().runOnUiThread(() -> {
                if (transfers != null) {
                    transferList.clear();
                    transferList.addAll(transfers);
                    adapter.notifyDataSetChanged();
                    binding.transferCountTxt.setText("Havale Sayısı: " + transferList.size());
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}