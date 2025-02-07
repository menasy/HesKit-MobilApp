package com.menasy.heskit;

import android.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.menasy.heskit.databinding.FragmentEmployeeProccesBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        Employee updatedEmployee = dbHelper.getEmployeeById(selectedEmp.getDbId());
        if(updatedEmployee != null) {
            selectedEmp = updatedEmployee;
            setupUI();
        }
    }

    private void setFormattedText(TextView textView, String label, String value, int color) {
        String fullText = label + " " + value;
        SpannableString spannable = new SpannableString(fullText);

        int start = fullText.indexOf(value); // Değerin başladığı yeri bul
        if (start != -1) {
            spannable.setSpan(new ForegroundColorSpan(color), start, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setText(spannable);
    }

    private void setStyledText(TextView textView, String label, String value, boolean isMoney) {
        int color = isMoney ? Color.GREEN : Color.CYAN;
        setFormattedText(textView, label, value, color);
    }

    private void setupUI() {
        bnd.empProcTitleTxt.setText(selectedEmp.getNameAndSurname());
        setStyledText(bnd.dateInTxt, "Başlangıç Tarihi: ", selectedEmp.getDateInStr() + "", false);
        setStyledText(bnd.countDayTxt, "Çalıştığı Gün Sayısı: ", selectedEmp.getWorksDay() + "", false);
        setStyledText(bnd.takedMoneyTxtView, "Harçlık: ", selectedEmp.getTotalMoney() + "₺", true);
        setStyledText(bnd.makedTotalTransfer, "Havale: ", selectedEmp.getTotalTransfer() + "₺", true);
        setStyledText(bnd.empNotWorksDayTxtView, "Çalışmadığı Gün Sayısı: ", selectedEmp.getTotalNotWorksDay() + "", false);

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

        bnd.empAddNotWorksDayBut.setOnClickListener(v -> {
            if (selectedEmp == null) {
                Log.e("EmpError", "Seçili çalışan null, işlem yapılamaz!");
                Toast.makeText(getContext(), "Çalışan bilgisi kayboldu, lütfen tekrar seçin.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToNotWorksDays(selectedEmp);
            }
        });
        bnd.deleteEmpBut.setOnClickListener(v -> showDeleteConfirmation());
        bnd.empGetOverDayFragment.setOnClickListener(v -> {
            OverDayProcces fragment = OverDayProcces.newInstance(selectedEmp);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });
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
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            DBHelper dbHelper = Singleton.getInstance().getDataBase();
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            try {
                // Önce ilişkili kayıtları sil
                db.delete(DBHelper.TABLE_PAYMENTS, "employeeId=?", new String[]{String.valueOf(selectedEmp.getDbId())});
                db.delete(DBHelper.TABLE_TRANSFERS, "employeeId=?", new String[]{String.valueOf(selectedEmp.getDbId())});

                // Çalışanı sil
                int deletedRows = db.delete(
                        DBHelper.TABLE_EMPLOYEES,
                        "id=?",
                        new String[]{String.valueOf(selectedEmp.getDbId())}
                );

                if(deletedRows > 0) {
                    db.setTransactionSuccessful();

                    // UI Güncellemeleri
                    requireActivity().runOnUiThread(() -> {
                        Calisanlar.empList.removeIf(e -> e.getDbId() == selectedEmp.getDbId());
                        Start.refreshEmployeeCount();
                        Start.refreshPaymentTotal();
                        Start.refreshTransferTotal();
                        Toast.makeText(getContext(), "Çalışan silindi", Toast.LENGTH_SHORT).show();
                        if(isAdded() && !isDetached()) {
                            requireActivity().onBackPressed();
                        }
                    });
                }
            } catch(Exception e) {
                Log.e("DELETE_EMP", "Hata: ", e);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Silme işlemi başarısız: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            } finally {
                db.endTransaction();
                executor.shutdown();
            }
        });
    }
}