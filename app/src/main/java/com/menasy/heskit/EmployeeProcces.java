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
import com.menasy.heskit.databinding.FragmentEmployeeProccesBinding;

public class EmployeeProcces extends Fragment {

    private FragmentEmployeeProccesBinding bnd;
    private static Employee selectedEmp;

    public static void setSelectedEmp(Employee employee) {
        selectedEmp = employee;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bnd = FragmentEmployeeProccesBinding.inflate(inflater, container, false);
        return bnd.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (selectedEmp != null) {
            setupUI();
            setupButtons();
        }
    }
    public void onResume() {
        super.onResume();
        refreshEmployeeData();
    }
    public void refreshEmployeeData() {
        // Veritabanından güncel verileri yükle
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        Employee updatedEmployee = dbHelper.getEmployeeById(selectedEmp.getDbId());
        if(updatedEmployee != null) {
            selectedEmp = updatedEmployee;
            setupUI();
        }
    }
    private void setupUI() {
        bnd.empProcTitleTxt.setText(selectedEmp.getNameAndSurname());
        selectedEmp.displayDateIn(bnd.dateInTxt);
        bnd.countDayTxt.setText("Çalıştığı Gün Sayısı: " + selectedEmp.getWorksDay());
        bnd.takedMoneyTxtView.setText("Toplam Harçlık: " + selectedEmp.getTotalMoney() + "₺");
        bnd.makedTotalTransfer.setText("Toplam Havale: " + selectedEmp.getTotalTransfer() + "₺");
    }

    private void setupButtons() {
        bnd.getAddPaymentFragmentBut.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToAddPayment(selectedEmp);
            }
        });
        bnd.getHavaleFragmentBut.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTransfers(selectedEmp);
            }
        });
        bnd.deleteEmpBut.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Silme Onayı")
                .setMessage(selectedEmp.getNameAndSurname() + " silinsin mi?")
                .setPositiveButton("Evet", (dialog, which) -> deleteEmployee())
                .setNegativeButton("Hayır", null)
                .show();
    }

    private void deleteEmployee() {
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(
                    DBHelper.TABLE_PAYMENTS,
                    "employeeId=?",
                    new String[]{String.valueOf(selectedEmp.getDbId())}
            );

            int deletedRows = db.delete(
                    DBHelper.TABLE_EMPLOYEES,
                    "id=?",
                    new String[]{String.valueOf(selectedEmp.getDbId())}
            );

            if (deletedRows > 0) {
                Calisanlar.empList.removeIf(e -> e.getDbId() == selectedEmp.getDbId());
                db.setTransactionSuccessful();
                requireActivity().onBackPressed();
            }
        } catch (Exception e) {
            Log.e("DELETE_EMP", "Hata: ", e);
        } finally {
            db.endTransaction();
        }
        Start.refreshEmployeeCount();
        Start.refreshPaymentTotal();
        Start.refreshTransferTotal();
    }
}