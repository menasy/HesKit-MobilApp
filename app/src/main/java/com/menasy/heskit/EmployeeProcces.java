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
            empProccAdapter = new EmployeeProccesAdapter(
                    selectedEmployee.getEmpPaymentLst() != null ?
                            selectedEmployee.getEmpPaymentLst() : new ArrayList<>()
            );
            bnd.paymentRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            bnd.paymentRecycler.setAdapter(empProccAdapter);
        }

        private void setupButtons() {
            bnd.addMoneyBut.setOnClickListener(v -> addMoney());
            bnd.deleteEmpBut.setOnClickListener(v -> showDeleteConfirmation());
//            bnd.cleanAllPaymentBut.setOnClickListener(v -> cleanPayment());
        }
//        private  void cleanPayment()
//        {
//            selectedEmployee.
//        }
        private void addMoney() {
            // Butonu geçici olarak devre dışı bırak
            bnd.addMoneyBut.setEnabled(false);

            // Input validasyon
            String amountStr = bnd.moneyEditTxt.getText().toString().trim();
            String paymentTypeStr = bnd.paymentTypeEditTxt.getText().toString().trim();
            if(amountStr.isEmpty() || paymentTypeStr.isEmpty()) {
                Toast.makeText(getContext(), "Lütfen tüm alanları doldurunuz!", Toast.LENGTH_SHORT).show();
                bnd.addMoneyBut.setEnabled(true);
                return;
            }

            int amount;
            try {
                amount = Integer.parseInt(amountStr);
                if(amount <= 0) throw new NumberFormatException();
            } catch(NumberFormatException e) {
                Toast.makeText(getContext(), "Geçersiz miktar formatı!", Toast.LENGTH_SHORT).show();
                bnd.addMoneyBut.setEnabled(true);
                return;
            }

            SQLiteDatabase db = null;
            try {
                DBHelper dbHelper = Singleton.getInstance().getDataBase();
                db = dbHelper.getWritableDatabase();

                // Transaction başlat
                db.beginTransaction();

                // 1. Ödemeyi veritabanına ekle
                long paymentId = dbHelper.addPayment(
                        amount,
                        paymentTypeStr,
                        DateUtils.getCurrentDate(),
                        selectedEmployee.getDbId()
                );

                if(paymentId == -1) {
                    throw new Exception("Ödeme veritabanına eklenemedi");
                }

                // 2. Çalışanın istatistiklerini güncelle
                selectedEmployee.setTotalMoney(selectedEmployee.getTotalMoney() + amount);

                ContentValues values = new ContentValues();
                values.put("totalMoney", selectedEmployee.getTotalMoney());

                int updatedRows = db.update(
                        DBHelper.TABLE_EMPLOYEES,
                        values,
                        "id = ?",
                        new String[]{String.valueOf(selectedEmployee.getDbId())}
                );

                if(updatedRows != 1) {
                    throw new Exception("Çalışan güncellenemedi");
                }

                // 3. Ödeme listesini güncelle
                if(selectedEmployee.getEmpPaymentLst() == null) {
                    selectedEmployee.setEmpPaymentLst(new ArrayList<>());
                }

                EmployeePayment newPayment = new EmployeePayment(amount, paymentTypeStr, DateUtils.getCurrentDateArray());
                newPayment.setId((int) paymentId);
                selectedEmployee.getEmpPaymentLst().add(0, newPayment); // Listenin başına ekle

                // Transaction'ı onayla
                db.setTransactionSuccessful();

                // UI'yı güncelle
                refreshUI();
                Toast.makeText(getContext(), "Harçlık başarıyla eklendi", Toast.LENGTH_SHORT).show();

            } catch(Exception e) {
                Log.e("DB_TRANSACTION", "Transaction hatası: ", e);
                Toast.makeText(getContext(), "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show();

                // Hata durumunda değişiklikleri geri al
                if(db != null && db.inTransaction()) {
                    db.endTransaction();
                }

                // Önbelleği temizle
                if(selectedEmployee.getEmpPaymentLst() != null && !selectedEmployee.getEmpPaymentLst().isEmpty()) {
                    selectedEmployee.getEmpPaymentLst().remove(0);
                }

            } finally {
                try {
                    if(db != null) {
                        if(db.inTransaction()) {
                            db.endTransaction();
                        }
                        db.close();
                    }
                } catch(Exception e) {
                    Log.e("DB_CLOSE", "Bağlantı kapatma hatası: ", e);
                }
                bnd.addMoneyBut.setEnabled(true);
            }
        }
        private void refreshUI() {
            // Adapter'ı güncelle
            if (empProccAdapter != null) {
                empProccAdapter.updateList(selectedEmployee.getEmpPaymentLst());
            }
            // TextViews'ları güncelle
            bnd.takedMoneyTxtView.setText("Toplam Harçlık: " + selectedEmployee.getTotalMoney() + "₺");
            bnd.countDayTxt.setText("Çalışma Günü: " + selectedEmployee.getWorksDay());
            bnd.moneyEditTxt.setText("");
            bnd.paymentTypeEditTxt.setText("");

            // Ana liste ve adaptörü güncelle
            Calisanlar.adapter.updateList(Calisanlar.empList);
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
        }
    }