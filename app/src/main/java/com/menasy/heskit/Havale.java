package com.menasy.heskit;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.menasy.heskit.databinding.FragmentHavaleBinding;
import java.util.ArrayList;

public class Havale extends Fragment {

    private FragmentHavaleBinding bnd;
    private static Employee selectedEmployee;
    private HavaleAdapter havaleAdapter;

    public static void setSelectedEmployee(Employee employee) {
        selectedEmployee = employee;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bnd = FragmentHavaleBinding.inflate(inflater, container, false);
        return bnd.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (selectedEmployee != null) {
            loadTransfersFromDB();
            setupUI();
            setupRecyclerView();
            setupButtons();
        }
    }

    private void loadTransfersFromDB() {
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        ArrayList<Transfer> transfers = dbHelper.getTransfersForEmployee(selectedEmployee.getDbId());
        selectedEmployee.getEmpTransferLst().clear();
        selectedEmployee.getEmpTransferLst().addAll(transfers);

        // Toplam transfer miktarını güncelle
        selectedEmployee.setTotalTransfer(calculateTotalTransfer(transfers));
    }

    private int calculateTotalTransfer(ArrayList<Transfer> transfers) {
        int total = 0;
        for (Transfer t : transfers) {
            total += t.getAmountTransfer();
        }
        return total;
    }

    private void setupUI() {
        bnd.transferTitleTextView.setText(selectedEmployee.getNameAndSurname());
    }



    private void setupRecyclerView() {
        havaleAdapter = new HavaleAdapter(selectedEmployee.getEmpTransferLst(), (payment, position) ->
                showDeleteTransferDialog(payment, position));
        bnd.transferRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bnd.transferRecyclerView.setAdapter(havaleAdapter);
    }

    private void setupButtons() {
        bnd.addTransfer.setOnClickListener(v -> addTransfer());
        bnd.cleanAllTransfer.setOnClickListener(v -> showCleanAllConfirmation());
    }

    private void addTransfer() {
        String amountStr = bnd.transferAmountTxt.getText().toString().trim();
        String recipient = bnd.sentPersonTxt.getText().toString().trim();

        if(amountStr.isEmpty() || recipient.isEmpty()) {
            Toast.makeText(requireContext(), "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
            return;
        }

        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int amount = Integer.parseInt(amountStr);
            if(amount <= 0) throw new NumberFormatException();

            long transferId = dbHelper.addTransfer(
                    amount,
                    DateUtils.getCurrentDate(),
                    recipient,
                    selectedEmployee.getDbId()
            );

            if(transferId == -1) throw new Exception("Havale eklenemedi");

            // Veritabanında totalTransfer'i güncelle
            selectedEmployee.setTotalTransfer(selectedEmployee.getTotalTransfer() + amount);
            ContentValues values = new ContentValues();
            values.put("totalTransfer", selectedEmployee.getTotalTransfer());
            db.update(DBHelper.TABLE_EMPLOYEES, values, "id=?", new String[]{String.valueOf(selectedEmployee.getDbId())});

            Transfer newTransfer = new Transfer(amount, DateUtils.getCurrentDate(), recipient);
            newTransfer.setId((int) transferId);
            selectedEmployee.getEmpTransferLst().add(0, newTransfer);
            havaleAdapter.notifyItemInserted(0);
            bnd.transferRecyclerView.smoothScrollToPosition(0);

            db.setTransactionSuccessful();

            // Verileri yenile
            Calisanlar.loadEmployeeDataFromDB();
            Start.refreshTransferTotal();

            Toast.makeText(requireContext(), "Havale başarıyla eklendi", Toast.LENGTH_SHORT).show();
            bnd.transferAmountTxt.setText("");
            bnd.sentPersonTxt.setText("");
        } catch(Exception e) {
            Log.e("ADD_TRANSFER", "Hata: ", e);
            Toast.makeText(requireContext(), "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.endTransaction();
        }
    }

    private void showDeleteTransferDialog(Transfer transfer, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Havale Silme")
                .setMessage(transfer.getAmountTransfer() + "₺ silinsin mi?")
                .setPositiveButton("Evet", (dialog, which) -> deleteTransfer(transfer, position))
                .setNegativeButton("Hayır", null)
                .show();
    }

    private void deleteTransfer(Transfer transfer, int position) {
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            int deletedRows = db.delete(DBHelper.TABLE_TRANSFERS, "id=?", new String[]{String.valueOf(transfer.getId())});
            if(deletedRows > 0) {
                // Veritabanında totalTransfer'i güncelle
                selectedEmployee.setTotalTransfer(selectedEmployee.getTotalTransfer() - transfer.getAmountTransfer());
                ContentValues values = new ContentValues();
                values.put("totalTransfer", selectedEmployee.getTotalTransfer());
                db.update(DBHelper.TABLE_EMPLOYEES, values, "id=?", new String[]{String.valueOf(selectedEmployee.getDbId())});

                selectedEmployee.getEmpTransferLst().remove(position);
                havaleAdapter.notifyItemRemoved(position);

                db.setTransactionSuccessful();

                // Verileri yenile
                Calisanlar.loadEmployeeDataFromDB();
                Start.refreshTransferTotal();
            }
        } catch(Exception e) {
            Log.e("DELETE_TRANSFER", "Hata: ", e);
        } finally {
            db.endTransaction();
        }
    }

    private void showCleanAllConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Tüm Havaleleri Sil")
                .setMessage("Tüm havale geçmişi kalıcı olarak silinsin mi?")
                .setPositiveButton("Evet", (dialog, which) -> cleanAllTransfers())
                .setNegativeButton("Hayır", null)
                .show();
    }

    private void cleanAllTransfers() {
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(DBHelper.TABLE_TRANSFERS, "employeeId=?", new String[]{String.valueOf(selectedEmployee.getDbId())});

            // Veritabanında totalTransfer'i sıfırla
            selectedEmployee.setTotalTransfer(0);
            ContentValues values = new ContentValues();
            values.put("totalTransfer", 0);
            db.update(DBHelper.TABLE_EMPLOYEES, values, "id=?", new String[]{String.valueOf(selectedEmployee.getDbId())});

            selectedEmployee.getEmpTransferLst().clear();
            havaleAdapter.updateList(new ArrayList<>());

            db.setTransactionSuccessful();

            // Verileri yenile
            Calisanlar.loadEmployeeDataFromDB();
            Start.refreshTransferTotal();

            Toast.makeText(requireContext(), "Tüm havaleler silindi", Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            Log.e("CLEAN_TRANSFERS", "Hata: ", e);
        } finally {
            db.endTransaction();
        }
    }
}