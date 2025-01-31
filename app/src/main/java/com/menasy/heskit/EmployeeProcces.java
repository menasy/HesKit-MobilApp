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
    import androidx.fragment.app.Fragment;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import com.menasy.heskit.databinding.FragmentEmployeeProccesBinding;
    import java.util.ArrayList;

    public class EmployeeProcces extends Fragment {

        private FragmentEmployeeProccesBinding bnd;
        private static Employee selectedEmployee;
        private EmployeeProccesAdapter empProccAdapter;

        public static void setSelectedEmployee(Employee employee) {
            selectedEmployee = employee;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            bnd = FragmentEmployeeProccesBinding.inflate(inflater, container, false);
            View view = bnd.getRoot();

            if (selectedEmployee != null) {
                setupUI();
                setupRecyclerView();
                setupButtons();
            }
            return view;
        }

        private void setupUI() {
            bnd.empProcTitleTxt.setText(selectedEmployee.getNameAndSurname());
            selectedEmployee.displayDateIn(bnd.dateInTxt);
            bnd.countDayTxt.setText("Çalışma Günü: " + selectedEmployee.getWorksDay());
            bnd.takedMoneyTxtView.setText("Toplam Harçlık: " + selectedEmployee.getTotalMoney() + "₺");
        }

        private void setupRecyclerView() {

            empProccAdapter = new EmployeeProccesAdapter(selectedEmployee.getEmpPaymentLst());
            empProccAdapter.setOnPaymentClickListener((payment, position) -> showDeletePaymentDialog(payment, position));
            bnd.paymentRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            bnd.paymentRecycler.setAdapter(empProccAdapter);
        }

        private void setupButtons() {
            bnd.addMoneyBut.setOnClickListener(v -> addMoney());
            bnd.deleteEmpBut.setOnClickListener(v -> showDeleteConfirmation());
            bnd.cleanAllPaymentBut.setOnClickListener(v -> showCleanAllConfirmation());
        }

        private void showCleanAllConfirmation() {
            new AlertDialog.Builder(getContext())
                    .setTitle("Tüm Harçlıkları Sil")
                    .setMessage("Tüm harçlık geçmişi kalıcı olarak silinsin mi?")
                    .setPositiveButton("Evet", (dialog, which) -> cleanAllPayments())
                    .setNegativeButton("Hayır", null)
                    .show();
        }
        private void showDeletePaymentDialog(EmployeePayment payment, int position) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Ödeme Silme")
                    .setMessage(payment.getPaymentInfo() + "\nBu ödemeyi silmek istediğinize emin misiniz?")
                    .setPositiveButton("Evet", (dialog, which) -> deletePayment(payment, position))
                    .setNegativeButton("Hayır", null)
                    .show();
        }

        private void deletePayment(EmployeePayment payment, int position) {
            DBHelper dbHelper = Singleton.getInstance().getDataBase();
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            try {
                db.beginTransaction();

                // 1. Ödemeyi veritabanından sil
                int deletedRows = db.delete(
                        DBHelper.TABLE_PAYMENTS,
                        "id=? AND employeeId=?",
                        new String[]{String.valueOf(payment.getId()), String.valueOf(selectedEmployee.getDbId())}
                );

                if(deletedRows > 0) {
                    // 2. Çalışanın totalMoney'sini güncelle
                    int newTotal = selectedEmployee.getTotalMoney() - payment.getTakedMoney();
                    selectedEmployee.setTotalMoney(newTotal);

                    // 3. Veritabanında totalMoney güncelle
                    ContentValues values = new ContentValues();
                    values.put("totalMoney", newTotal);
                    db.update(
                            DBHelper.TABLE_EMPLOYEES,
                            values,
                            "id=?",
                            new String[]{String.valueOf(selectedEmployee.getDbId())}
                    );

                    // 4. Listeleri güncelle
                    selectedEmployee.getEmpPaymentLst().remove(position);
                    empProccAdapter.notifyItemRemoved(position);

                    // 5. UI ve diğer güncellemeler
                    getActivity().runOnUiThread(() -> {
                        bnd.takedMoneyTxtView.setText("Toplam Harçlık: " + newTotal + "₺");
                        Calisanlar.loadEmployeeDataFromDB();
                        Start.refreshPaymentTotal();
                        Toast.makeText(getContext(), "Ödeme silindi", Toast.LENGTH_SHORT).show();
                    });

                    db.setTransactionSuccessful();
                }
            } catch(Exception e) {
                Log.e("DELETE_PAYMENT", "Hata: ", e);
                Toast.makeText(getContext(), "Silme işlemi başarısız: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                db.endTransaction();
                db.close();
            }
        }
        private void cleanAllPayments() {
            DBHelper dbHelper = Singleton.getInstance().getDataBase();
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            try {
                db.beginTransaction();

                // 1. Tüm ödemeleri sil
                int deletedPayments = db.delete(
                        DBHelper.TABLE_PAYMENTS,
                        "employeeId=?",
                        new String[]{String.valueOf(selectedEmployee.getDbId())}
                );

                // 2. Çalışanın totalMoney'sini sıfırla
                ContentValues values = new ContentValues();
                values.put("totalMoney", 0);
                int updatedRows = db.update(
                        DBHelper.TABLE_EMPLOYEES,
                        values,
                        "id=?",
                        new String[]{String.valueOf(selectedEmployee.getDbId())}
                );

                if(updatedRows == 1) {
                    // 3. UI ve verileri güncelle
                    selectedEmployee.setTotalMoney(0);
                    selectedEmployee.getEmpPaymentLst().clear();

                    getActivity().runOnUiThread(() -> {
                        empProccAdapter.updateList(new ArrayList<>());
                        bnd.takedMoneyTxtView.setText("Toplam Harçlık: 0₺");
                        Calisanlar.loadEmployeeDataFromDB();
                        Start.refreshPaymentTotal();
                        Toast.makeText(getContext(), deletedPayments + " ödeme silindi", Toast.LENGTH_SHORT).show();
                    });

                    db.setTransactionSuccessful();
                }
            } catch(Exception e) {
                Log.e("CLEAN_PAYMENTS", "Hata: ", e);
                Toast.makeText(getContext(), "Silme işlemi başarısız: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                db.endTransaction();
                db.close();
            }
        }


        private void addMoney() {
    bnd.addMoneyBut.setEnabled(false);

    // Input validation
    String amountStr = bnd.moneyEditTxt.getText().toString().trim();
    String paymentTypeStr = bnd.paymentTypeEditTxt.getText().toString().trim();

    if(amountStr.isEmpty()) {
        Toast.makeText(getContext(), "Lütfen geçerli bir miktar giriniz!", Toast.LENGTH_SHORT).show();
        bnd.addMoneyBut.setEnabled(true);
        return;
    }
    else if (paymentTypeStr.isEmpty())
        paymentTypeStr = "";

    try {
        int amount = Integer.parseInt(amountStr);
        if(amount <= 0) throw new NumberFormatException();

        SQLiteDatabase db = null;
        try {
            DBHelper dbHelper = Singleton.getInstance().getDataBase();
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            // 1. Add payment to database
            long paymentId = dbHelper.addPayment(
                    amount,
                    paymentTypeStr,
                    DateUtils.getCurrentDate(),
                    selectedEmployee.getDbId()
            );

            if(paymentId == -1) throw new Exception("Ödeme veritabanına eklenemedi");

            // 2. Update employee stats
            selectedEmployee.setTotalMoney(selectedEmployee.getTotalMoney() + amount);

            // Update database
            ContentValues values = new ContentValues();
            values.put("totalMoney", selectedEmployee.getTotalMoney());
            int updatedRows = db.update(
                    DBHelper.TABLE_EMPLOYEES,
                    values,
                    "id = ?",
                    new String[]{String.valueOf(selectedEmployee.getDbId())}
            );
            if(updatedRows != 1) throw new Exception("Çalışan güncellenemedi");

            // 3. Update UI
            EmployeePayment newPayment = new EmployeePayment(amount, paymentTypeStr, DateUtils.getCurrentDateArray());
            newPayment.setId((int) paymentId);

            // Add to both adapter and underlying data
            selectedEmployee.getEmpPaymentLst().add(0, newPayment);
            empProccAdapter.notifyItemInserted(0);

            // Scroll to top to show new item
            bnd.paymentRecycler.smoothScrollToPosition(0);

            db.setTransactionSuccessful();
            Calisanlar.loadEmployeeDataFromDB();
            Start.refreshEmployeeCount();
            Start.refreshPaymentTotal();
            Toast.makeText(getContext(), "Harçlık başarıyla eklendi", Toast.LENGTH_SHORT).show();
            // Update summary views
            bnd.takedMoneyTxtView.setText("Toplam Harçlık: " + selectedEmployee.getTotalMoney() + "₺");
            bnd.moneyEditTxt.setText("");
            bnd.paymentTypeEditTxt.setText("");

        } finally {
            if(db != null) {
                if(db.inTransaction()) db.endTransaction();
                db.close();
            }
        }

    } catch(NumberFormatException e) {
        Toast.makeText(getContext(), "Geçersiz miktar formatı!", Toast.LENGTH_SHORT).show();
    } catch(Exception e) {
        Log.e("DB_TRANSACTION", "Hata: ", e);
        Toast.makeText(getContext(), "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show();

        // Rollback UI changes
        if(empProccAdapter != null && !selectedEmployee.getEmpPaymentLst().isEmpty()) {
            selectedEmployee.getEmpPaymentLst().remove(0);
            empProccAdapter.notifyItemRemoved(0);
        }
    } finally {
        bnd.addMoneyBut.setEnabled(true);
    }
}

        private void showDeleteConfirmation() {
            new AlertDialog.Builder(getContext())
                    .setTitle("Silme Onayı")
                    .setMessage(selectedEmployee.getNameAndSurname() + " silinsin mi?")
                    .setPositiveButton("Evet", (dialog, which) -> deleteEmployee())
                    .setNegativeButton("Hayır", null)
                    .show();
        }

        private void deleteEmployee() {
            DBHelper dbHelper = Singleton.getInstance().getDataBase();
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            try {
                db.beginTransaction();

                // Önce ödemeleri sil
                db.delete(
                        DBHelper.TABLE_PAYMENTS,
                        "employeeId=?",
                        new String[]{String.valueOf(selectedEmployee.getDbId())}
                );

                // Sonra çalışanı sil
                int deletedRows = db.delete(
                        DBHelper.TABLE_EMPLOYEES,
                        "id=?",
                        new String[]{String.valueOf(selectedEmployee.getDbId())}
                );

                if (deletedRows > 0) {
                    // Listeden kaldır
                    Calisanlar.empList.removeIf(e -> e.getDbId() == selectedEmployee.getDbId());
                    db.setTransactionSuccessful();
                    getActivity().onBackPressed();
                }
            } finally {
                db.endTransaction();
                db.close();
            }
            Start.refreshEmployeeCount();
            Start.refreshPaymentTotal();
        }
    }