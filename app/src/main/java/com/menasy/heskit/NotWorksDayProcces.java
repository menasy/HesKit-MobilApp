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
import java.util.concurrent.Executors;

public class NotWorksDayProcces extends Fragment {

    private FragmentNotWorksDayProccesBinding bnd;
    private static Employee selectedEmployee;
    private NotWorksDayAdapter adapter;

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
        loadData();
        if(adapter != null) {
            adapter.updateList(selectedEmployee.getEmpNotWorksDayLst());
        }
    }

    private void setupUI() {
        bnd.notWorksDayTitleTxtView.setText(selectedEmployee.getNameAndSurname());
    }

    private void setupRecyclerView() {
        adapter = new NotWorksDayAdapter(selectedEmployee.getEmpNotWorksDayLst(), (day, position) -> showDeleteDialog(day, position));
        bnd.notWorksDayRecView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bnd.notWorksDayRecView.setAdapter(adapter);
    }

    private void setupButtons() {
        bnd.addNotWorksDayBut.setOnClickListener(v -> addNotWorksDay());
        bnd.cleanAllNotWorksDayBut.setOnClickListener(v -> showCleanAllDialog());
    }

    private void loadData() {
        Executors.newSingleThreadExecutor().execute(() -> {
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
            Toast.makeText(requireContext(), "Gün sayısı giriniz!", Toast.LENGTH_SHORT).show();
            bnd.addNotWorksDayBut.setEnabled(true);
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                int days = Integer.parseInt(daysStr);
                String date = DateUtils.getCurrentDate();

                DBHelper dbHelper = Singleton.getInstance().getDataBase();
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                try {
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
                    newDay.setId((int) id);

                    requireActivity().runOnUiThread(() -> {
                        selectedEmployee.getEmpNotWorksDayLst().add(0, newDay);
                        selectedEmployee.setTotalNotWorksDay(selectedEmployee.getTotalNotWorksDay() + days);

                        if(adapter != null) {
                            adapter.addNotWorkDays(newDay);
                            bnd.notWorksDayRecView.smoothScrollToPosition(0);
                        }

                        bnd.notWorksDayAmountEditTxt.setText("1");
                        bnd.notWorksDayReasonEditTxt.setText("");
                    });

                } finally {
                    db.endTransaction();

                }

            } catch(Exception e) {
                Log.e("ADD_NWD", "Hata: ", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                requireActivity().runOnUiThread(() -> {
                    loadData();
                    bnd.addNotWorksDayBut.setEnabled(true);
                    if (isAdded() && !isDetached()) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(
                                    requireContext(),
                                    "Başarıyla eklendi",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                        requireActivity().onBackPressed();
                    }
                });
            }
        });
    }

    private void showDeleteDialog(NotWorksDay day, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Silme Onayı")
                .setMessage(day.getDays() + " gün silinsin mi?")
                .setPositiveButton("Evet", (d, w) -> deleteDay(day, position))
                .setNegativeButton("İptal", null)
                .show();
    }

    private void deleteDay(NotWorksDay day, int position) {
        Executors.newSingleThreadExecutor().execute(() -> {
            SQLiteDatabase db = null;
            try {
                DBHelper dbHelper = Singleton.getInstance().getDataBase();
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();

                // Silinecek ID'nin var olup olmadığını kontrol ediyoruz.
                Cursor cursor = db.rawQuery("SELECT id FROM " + DBHelper.TABLE_NOT_WORKS_DAYS + " WHERE id=?", new String[]{String.valueOf(day.getId())});
                if (cursor.getCount() == 0) {
                    cursor.close();
                    throw new Exception("Silinecek kayıt bulunamadı, ID: " + day.getId());
                }
                cursor.close();

                // Silme işlemi
                int deletedRows = db.delete(
                        DBHelper.TABLE_NOT_WORKS_DAYS,
                        "id=?",
                        new String[]{String.valueOf(day.getId())}
                );

                if(deletedRows == 0) throw new Exception("Silme işlemi başarısız");

                // Çalışan güncelleme
                int newTotal = selectedEmployee.getTotalNotWorksDay() - day.getDays();
                ContentValues empValues = new ContentValues();
                empValues.put("totalNotWorksDay", newTotal);
                db.update(
                        DBHelper.TABLE_EMPLOYEES,
                        empValues,
                        "id=?",
                        new String[]{String.valueOf(selectedEmployee.getDbId())}
                );

                db.setTransactionSuccessful();

                requireActivity().runOnUiThread(() -> {
                    selectedEmployee.getEmpNotWorksDayLst().remove(position);
                    if(adapter != null) {
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, selectedEmployee.getEmpNotWorksDayLst().size());
                    }
                    selectedEmployee.setTotalNotWorksDay(newTotal);
                    Toast.makeText(requireContext(), "Silindi", Toast.LENGTH_SHORT).show();
                });

            } catch(Exception e) {
                Log.e("DELETE_NWD", "Hata: ", e);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Silinemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                if(db != null) {
                    try {
                        db.endTransaction();
                    } catch(Exception e) {
                        Log.e("DELETE_NWD", "Transaction kapatma hatası: ", e);
                    }
                }
                loadData();
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
        Executors.newSingleThreadExecutor().execute(() -> {
            SQLiteDatabase db = null;
            try {
                DBHelper dbHelper = Singleton.getInstance().getDataBase();
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();

                // 1. Tüm kayıtları sil
                db.delete(
                        DBHelper.TABLE_NOT_WORKS_DAYS,
                        "employeeId=?",
                        new String[]{String.valueOf(selectedEmployee.getDbId())}
                );

                // 2. Total değerini sıfırla
                ContentValues empValues = new ContentValues();
                empValues.put("totalNotWorksDay", 0);
                db.update(
                        DBHelper.TABLE_EMPLOYEES,
                        empValues,
                        "id=?",
                        new String[]{String.valueOf(selectedEmployee.getDbId())}
                );

                db.setTransactionSuccessful();

                // 3. UI Güncellemelerini ana thread'de yap
                requireActivity().runOnUiThread(() -> {
                    // Adapter'ı temizle
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
                loadData();

            }
        });
    }
}