package com.menasy.heskit;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
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

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmployeeProcces extends Fragment {

    private FragmentEmployeeProccesBinding bnd;
    private static Employee selectedEmp;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
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

    private void setDateTextWithColors(TextView textView, String startDate, String dismissDate) {
        String dateInfo = "Başlangıç Tarihi: " + startDate;
        String dismissInfo = dismissDate.isEmpty() ? "" : "\n\nİş Çıkış Tarihi:  " + dismissDate;
        SpannableString spannable = new SpannableString(dateInfo + dismissInfo);

        spannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.CYAN), 18, 18 + startDate.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (!dismissInfo.isEmpty()) {
            int dismissStart = dateInfo.length() + 2;

            spannable.setSpan(new ForegroundColorSpan(Color.WHITE), dismissStart, dismissStart + 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new ForegroundColorSpan(Color.CYAN), dismissStart + 18, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setText(spannable);
    }


    private void setupUI()
    {
        boolean isDismissed = selectedEmp.getDismissDate() != null;
        selectedEmp.setDismissCheck(!isDismissed);
        String dismissInfo = "";
        if (isDismissed) {
            dismissInfo = selectedEmp.getDismissDateStr();
            bnd.dismissEmpBut.setEnabled(false);
            bnd.dismissEmpBut.setBackgroundColor(Color.DKGRAY);
            bnd.dismissEmpBut.setText("İşten Çıkarıldı");
        }
        String dateInfo = selectedEmp.getDateInStr();

        setDateTextWithColors(bnd.dateInTxt, dateInfo, dismissInfo);
        bnd.empProcTitleTxt.setText(selectedEmp.getNameAndSurname());
        setStyledText(bnd.countDayTxt, "Çalıştığı Gün: ", selectedEmp.getWorksDay() + "", false);
        setStyledText(bnd.takedMoneyTxtView, "Harçlık: ", selectedEmp.getTotalMoney() + "₺", true);
        setStyledText(bnd.makedTotalTransfer, "Havale: ", selectedEmp.getTotalTransfer() + "₺", true);
        setStyledText(bnd.empNotWorksDayTxtView, "Çalışmadığı Gün: ", selectedEmp.getTotalNotWorksDay() + "", false);
        setStyledText(bnd.totalOverDayTxtView, "Toplam Mesai: ", selectedEmp.getTotalOverDay() + "", false);
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

        bnd.dismissEmpBut.setOnClickListener(v -> showDismissConfirmation());
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
                if (!executor.isShutdown()) {
                    executor.shutdown();
                }
            }
        });
    }
    private void showDismissConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("İşten Çıkarma Onayı")
                .setMessage(selectedEmp.getNameAndSurname() + " işten çıkarılsın mı?")
                .setPositiveButton("Evet", (d, w) -> showDatePickerDialog())
                .setNegativeButton("İptal", null)
                .show();
    }
    private void showDatePickerDialog() {
        Toast.makeText(getContext(), "İşten ayrılma tarihini giriniz !", Toast.LENGTH_LONG).show();
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {

                    String formattedDate = String.format(Locale.getDefault(),
                            "%02d.%02d.%d",
                            selectedDay,
                            selectedMonth + 1,
                            selectedYear
                    );

                    dismissEmployeeWithDate(formattedDate);
                },
                year, month, day
        );

        datePicker.show();
    }
    private void dismissEmployeeWithDate(String selectedDate) {
        executor.execute(() -> {
            SQLiteDatabase db = null;
            try {
                int[] parsedDate = DateUtils.parseDateArray(selectedDate);
                if(parsedDate == null) {
                    throw new IllegalArgumentException("Geçersiz tarih formatı!");
                }

                DBHelper dbHelper = Singleton.getInstance().getDataBase();
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();


                ContentValues values = new ContentValues();
                values.put("dismissDate", selectedDate);
                db.update(
                        DBHelper.TABLE_EMPLOYEES,
                        values,
                        "id=?",
                        new String[]{String.valueOf(selectedEmp.getDbId())}
                );

                selectedEmp.setDismissDate(parsedDate);
                selectedEmp.setDismissCheck(false);
                db.setTransactionSuccessful();

                requireActivity().runOnUiThread(() -> {
                    Calisanlar.loadEmployeeDataFromDB();
                    setupUI();
                    Toast.makeText(getContext(), "Çalışan " + selectedDate + " tarihinde işten çıkarıldı", Toast.LENGTH_SHORT).show();
                });

            } catch(Exception e) {
                Log.e("DISMISS_ERROR", "Hata: ", e);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                if(db != null) {
                    try {
                        db.endTransaction();
                    } catch(Exception e) {
                        Log.e("DISMISS_ERROR", "Transaction hatası: ", e);
                    }
                }
            }
        });
    }
}