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
import com.menasy.heskit.databinding.FragmentAddPaymentBinding;
import java.util.ArrayList;

public class AddPayment extends Fragment {

    private FragmentAddPaymentBinding bnd;
    private static Employee selectedEmployee;
    private EmployeeProccesAdapter employeeProccesAdapter;

    public static void setSelectedEmployee(Employee employee) {
        selectedEmployee = employee;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bnd = FragmentAddPaymentBinding.inflate(inflater, container, false);
        return bnd.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (selectedEmployee != null) {
            setupRecyclerView(); // Önce adapter'ı başlat
            setupButtons();
            updatePaymentUI();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bnd = null; // Memory leak'i önlemek için
    }
    public void onResume() {
        super.onResume();
        refreshPaymentData();
    }

    private void refreshPaymentData() {
        if(selectedEmployee == null || getContext() == null) return;

        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        ArrayList<EmployeePayment> payments = dbHelper.getPaymentsForEmployee(selectedEmployee.getDbId());

        if(selectedEmployee.getEmpPaymentLst() != null && employeeProccesAdapter != null) {
            selectedEmployee.getEmpPaymentLst().clear();
            selectedEmployee.getEmpPaymentLst().addAll(payments);
            employeeProccesAdapter.updateList(payments);
        }
    }
    private void updatePaymentUI() {
        if(bnd != null && selectedEmployee != null && employeeProccesAdapter != null) {
            bnd.addPaymentTitleTxtView.setText(selectedEmployee.getNameAndSurname());
            employeeProccesAdapter.updateList(selectedEmployee.getEmpPaymentLst());
        }
    }

    private void setupRecyclerView() {
        if(selectedEmployee == null || getContext() == null) return;

        employeeProccesAdapter = new EmployeeProccesAdapter(selectedEmployee.getEmpPaymentLst());
        employeeProccesAdapter.setOnPaymentClickListener((payment, position) -> {
            if(isAdded()) showDeletePaymentDialog(payment, position);
        });

        if(bnd != null) {
            bnd.paymentRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
            bnd.paymentRecycler.setAdapter(employeeProccesAdapter);
        }
    }

    private void setupButtons() {
        bnd.addMoneyBut.setOnClickListener(v -> addMoney());
        bnd.cleanAllPaymentBut.setOnClickListener(v -> showCleanAllConfirmation());
    }

    private void showCleanAllConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Tüm Harçlıkları Sil")
                .setMessage("Tüm harçlık geçmişi kalıcı olarak silinsin mi?")
                .setPositiveButton("Evet", (dialog, which) -> cleanAllPayments())
                .setNegativeButton("Hayır", null)
                .show();
    }

    private void addMoney() {
        bnd.addMoneyBut.setEnabled(false);

        String amountStr = bnd.moneyEditTxt.getText().toString().trim();
        String paymentTypeStr = bnd.paymentTypeEditTxt.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), "Lütfen geçerli bir miktar giriniz!", Toast.LENGTH_SHORT).show();
            bnd.addMoneyBut.setEnabled(true);
            return;
        }

        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            long amount = Integer.parseUnsignedInt(amountStr);
            if (amount <= 0) throw new NumberFormatException();

            long paymentId = dbHelper.addPayment(
                    amount,
                    paymentTypeStr,
                    DateUtils.getCurrentDate(),
                    selectedEmployee.getDbId()
            );

            if (paymentId == -1) throw new Exception("Ödeme eklenemedi");

            selectedEmployee.setTotalMoney(selectedEmployee.getTotalMoney() + amount);

            ContentValues values = new ContentValues();
            values.put("totalMoney", selectedEmployee.getTotalMoney());
            db.update(DBHelper.TABLE_EMPLOYEES, values, "id=?", new String[]{String.valueOf(selectedEmployee.getDbId())});

            EmployeePayment newPayment = new EmployeePayment(amount, paymentTypeStr, DateUtils.getCurrentDateArray());
            newPayment.setId((int) paymentId);

            db.setTransactionSuccessful();
            db.endTransaction();

            requireActivity().runOnUiThread(() -> {
                selectedEmployee.getEmpPaymentLst().add(0, newPayment);
                if (employeeProccesAdapter != null) {
                    employeeProccesAdapter.updateList(selectedEmployee.getEmpPaymentLst());
                    employeeProccesAdapter.notifyDataSetChanged();  // **Tam güncelleme yap**
                    bnd.paymentRecycler.smoothScrollToPosition(0);
                }
                Calisanlar.loadEmployeeDataFromDB();
                Start.refreshPaymentTotal();
                bnd.moneyEditTxt.setText("");
                bnd.paymentTypeEditTxt.setText("");
            });

        } catch (Exception e) {
            Log.e("ADD_PAYMENT", "Hata: ", e);
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        } finally {
            if (db.inTransaction()) db.endTransaction();
            bnd.addMoneyBut.setEnabled(true);
        }
    }

    private void showDeletePaymentDialog(EmployeePayment payment, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Ödeme Silme")
                .setMessage(payment.getTakedMoneyStr() + " silinsin mi?")
                .setPositiveButton("Evet", (dialog, which) -> deletePayment(payment, position))
                .setNegativeButton("Hayır", null)
                .show();
    }

    private void deletePayment(EmployeePayment payment, int position) {
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            // Ödemeyi veritabanından sil
            int deletedRows = db.delete(DBHelper.TABLE_PAYMENTS, "id=?", new String[]{String.valueOf(payment.getId())});

            if(deletedRows > 0) {
                // TotalMoney'i hem local hem veritabanında güncelle
                long newTotal = selectedEmployee.getTotalMoney() - payment.getTakedMoney();
                selectedEmployee.setTotalMoney(newTotal);

                ContentValues values = new ContentValues();
                values.put("totalMoney", newTotal);
                db.update(DBHelper.TABLE_EMPLOYEES, values, "id=?", new String[]{String.valueOf(selectedEmployee.getDbId())});

                // Listeyi ve adapter'i güncelle
                selectedEmployee.getEmpPaymentLst().remove(position);
                employeeProccesAdapter.notifyItemRemoved(position);

                // Verileri yeniden yükle
                refreshPaymentData();
                db.setTransactionSuccessful();

                Calisanlar.loadEmployeeDataFromDB();
                Start.refreshPaymentTotal();
            }
        } catch(Exception e) {
            Log.e("DELETE_PAYMENT", "Hata: ", e);
        } finally {
            db.endTransaction();
        }
    }

    private void cleanAllPayments() {
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(DBHelper.TABLE_PAYMENTS, "employeeId=?", new String[]{String.valueOf(selectedEmployee.getDbId())});

            ContentValues values = new ContentValues();
            values.put("totalMoney", 0);
            db.update(DBHelper.TABLE_EMPLOYEES, values, "id=?", new String[]{String.valueOf(selectedEmployee.getDbId())});

            selectedEmployee.getEmpPaymentLst().clear();
            employeeProccesAdapter.updateList(new ArrayList<>());
            selectedEmployee.setTotalMoney(0);

            db.setTransactionSuccessful();

            Calisanlar.loadEmployeeDataFromDB();
            Start.refreshPaymentTotal();

            Toast.makeText(requireContext(), "Tüm ödemeler silindi", Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            Log.e("CLEAN_PAYMENTS", "Hata: ", e);
        } finally {
            db.endTransaction();
        }
    }
}