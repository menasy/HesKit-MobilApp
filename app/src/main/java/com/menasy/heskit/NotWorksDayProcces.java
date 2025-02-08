package com.menasy.heskit;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
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
import com.menasy.heskit.databinding.FragmentNotWorksDayProccesBinding;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotWorksDayProcces extends Fragment {

    private FragmentNotWorksDayProccesBinding bnd;
    private static Employee selectedEmployee;
    private NotWorksDayAdapter adapter;

    // Tüm veritabanı işlemleri için tek Executor
    private static final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    public static void setSelectedEmployee(Employee employee) {
        selectedEmployee = employee;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bnd = FragmentNotWorksDayProccesBinding.inflate(inflater, container, false);
        return bnd.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (selectedEmployee != null) {
            loadData();
            setupUI();
            setupRecyclerView();
            setupButtons();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(); // Yeniden yükleme işlemi de aynı Executor'da
    }

    private void setupUI() {
        bnd.notWorksDayTitleTxtView.setText(selectedEmployee.getNameAndSurname());
    }

    private void setupRecyclerView() {
        adapter = new NotWorksDayAdapter(selectedEmployee.getEmpNotWorksDayLst(), (day, position) -> showDeleteDialog(day));
        bnd.notWorksDayRecView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bnd.notWorksDayRecView.setAdapter(adapter);
    }

    private void setupButtons() {
        bnd.addNotWorksDayBut.setOnClickListener(v -> addNotWorksDay());
        bnd.cleanAllNotWorksDayBut.setOnClickListener(v -> showCleanAllDialog());
    }

    private void loadData() {
        dbExecutor.execute(() -> {
            try {
                DBHelper dbHelper = Singleton.getInstance().getDataBase();
                ArrayList<NotWorksDay> days = dbHelper.getNotWorksDaysForEmployee(selectedEmployee.getDbId());

                requireActivity().runOnUiThread(() -> {
                    selectedEmployee.getEmpNotWorksDayLst().clear();
                    selectedEmployee.getEmpNotWorksDayLst().addAll(days);
                    selectedEmployee.setTotalNotWorksDay(calculateTotalNotWorksDay(days));
                    if(adapter != null) {
                        adapter.updateList(new ArrayList<>(days));
                    }
                });
            } catch(Exception e) {
                Log.e("LOAD_DATA", "Hata: ", e);
            }
        });
    }

    private int calculateTotalNotWorksDay(ArrayList<NotWorksDay> notWorksDays) {
        int total = 0;
        for (NotWorksDay t : notWorksDays) {
            total += t.getDays();
        }
        return total;
    }

    private void addNotWorksDay() {
        bnd.addNotWorksDayBut.setEnabled(false);
        String daysStr = bnd.notWorksDayAmountEditTxt.getText().toString().trim();
        String reason = bnd.notWorksDayReasonEditTxt.getText().toString().trim();

        if(daysStr.isEmpty()) {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Gün sayısı giriniz!", Toast.LENGTH_SHORT).show();
                bnd.addNotWorksDayBut.setEnabled(true);
            });
            return;
        }

        dbExecutor.execute(() -> {
            SQLiteDatabase db = null;
            try {
                int days = Integer.parseInt(daysStr);
                String date = DateUtils.getCurrentDate();

                DBHelper dbHelper = Singleton.getInstance().getDataBase();
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();

                long id = dbHelper.addNotWorksDay(days, date, reason, selectedEmployee.getDbId());
                if(id == -1) throw new Exception("NotWorksDay eklenemedi");

                // Employee güncelleme
                ContentValues empValues = new ContentValues();
                empValues.put("totalNotWorksDay", selectedEmployee.getTotalNotWorksDay() + days);
                db.update(DBHelper.TABLE_EMPLOYEES, empValues, "id=?",
                        new String[]{String.valueOf(selectedEmployee.getDbId())});

                db.setTransactionSuccessful();

                // UI Güncelleme
                NotWorksDay newDay = new NotWorksDay(days, date, reason);
                newDay.setId(id);

                requireActivity().runOnUiThread(() -> {
                    selectedEmployee.getEmpNotWorksDayLst().add(0, newDay);
                    selectedEmployee.setTotalNotWorksDay(selectedEmployee.getTotalNotWorksDay() + days);

                    if(adapter != null) {
                        adapter.addNotWorkDays(newDay);
                        bnd.notWorksDayRecView.smoothScrollToPosition(0);
                    }

                    bnd.notWorksDayAmountEditTxt.setText("1");
                    bnd.notWorksDayReasonEditTxt.setText("");
                    Toast.makeText(requireContext(), "Başarıyla eklendi", Toast.LENGTH_SHORT).show();
                });

            } catch(Exception e) {
                Log.e("ADD_NWD", "Hata: ", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                if(db != null) {
                    try {
                        db.endTransaction();
                    } catch(Exception e) {
                        Log.e("ADD_NWD", "Transaction kapatma hatası: ", e);
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    bnd.addNotWorksDayBut.setEnabled(true);
                    if (isAdded() && !isDetached()) {
                        requireActivity().onBackPressed();
                    }
                });
            }
        });
    }

    private void showDeleteDialog(NotWorksDay day) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Silme Onayı")
                .setMessage(day.getDays() + " gün silinsin mi?")
                .setPositiveButton("Evet", (d, w) -> deleteDay(day))
                .setNegativeButton("İptal", null)
                .show();
    }

    private void deleteDay(NotWorksDay day) {
        dbExecutor.execute(() -> {
            SQLiteDatabase db = null;
            try {
                DBHelper dbHelper = Singleton.getInstance().getDataBase();
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();

                // ID'ye göre silme işlemi
                int deletedRows = db.delete(
                        DBHelper.TABLE_NOT_WORKS_DAYS,
                        "id=?",
                        new String[]{String.valueOf(day.getId())}
                );

                if (deletedRows == 0) throw new Exception("Silme işlemi başarısız");

                // Employee toplamını güncelle
                int newTotal = selectedEmployee.getTotalNotWorksDay() - day.getDays();
                ContentValues empValues = new ContentValues();
                empValues.put("totalNotWorksDay", newTotal);
                db.update(DBHelper.TABLE_EMPLOYEES, empValues, "id=?",
                        new String[]{String.valueOf(selectedEmployee.getDbId())}
                );

                db.setTransactionSuccessful();

                requireActivity().runOnUiThread(() -> {
                    selectedEmployee.getEmpNotWorksDayLst().remove(day);
                    selectedEmployee.setTotalNotWorksDay(newTotal);

                    if (adapter != null) {
                        adapter.updateList(new ArrayList<>(selectedEmployee.getEmpNotWorksDayLst()));
                    }

                    Toast.makeText(requireContext(), "Silindi", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                Log.e("DELETE_NWD", "Hata: ", e);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (db != null) {
                    try {
                        db.endTransaction();
                    } catch (Exception e) {
                        Log.e("DELETE_NWD", "Transaction hatası: ", e);
                    }
                }
            }
        });
    }


    private void showCleanAllDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Tüm Kayıtları Sil")
                .setMessage("Tüm çalışılmayan gün kayıtları silinsin mi?")
                .setPositiveButton("Evet", (d, w) -> cleanAllDays())
                .setNegativeButton("İptal", null)
                .show();
    }

    private void cleanAllDays() {
        dbExecutor.execute(() -> {
            SQLiteDatabase db = null;
            try {
                DBHelper dbHelper = Singleton.getInstance().getDataBase();
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();

                db.delete(DBHelper.TABLE_NOT_WORKS_DAYS, "employeeId=?",
                        new String[]{String.valueOf(selectedEmployee.getDbId())});

                ContentValues empValues = new ContentValues();
                empValues.put("totalNotWorksDay", 0);
                db.update(DBHelper.TABLE_EMPLOYEES, empValues, "id=?",
                        new String[]{String.valueOf(selectedEmployee.getDbId())});

                db.setTransactionSuccessful();

                requireActivity().runOnUiThread(() -> {
                    selectedEmployee.getEmpNotWorksDayLst().clear();
                    selectedEmployee.setTotalNotWorksDay(0);
                    if(adapter != null) {
                        adapter.updateList(new ArrayList<>());
                    }
                    Toast.makeText(requireContext(), "Tüm kayıtlar silindi", Toast.LENGTH_SHORT).show();
                });

            } catch(Exception e) {
                Log.e("CLEAN_NWD", "Hata: ", e);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Silinemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                if(db != null) {
                    try {
                        db.endTransaction();
                    } catch(Exception e) {
                        Log.e("CLEAN_NWD", "Transaction kapatma hatası: ", e);
                    }
                }
            }
        });
    }
}