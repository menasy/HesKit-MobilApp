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
            DBHelper dbHelper = Singleton.getInstance().getDataBase();
            ArrayList<NotWorksDay> days = dbHelper.getNotWorksDaysForEmployee(selectedEmployee.getDbId());

            requireActivity().runOnUiThread(() -> {
                selectedEmployee.getEmpNotWorksDayLst().clear();
                selectedEmployee.getEmpNotWorksDayLst().addAll(days);
                adapter.updateList(days);
            });
        });
    }

    private void addNotWorksDay() {
        String daysStr = bnd.notWorksDayAmountEditTxt.getText().toString().trim();
        String reason = bnd.notWorksDayReasonEditTxt.getText().toString().trim();

        if(daysStr.isEmpty()) {
            Toast.makeText(requireContext(), "Gün sayısı giriniz!", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            SQLiteDatabase db = null;
            try {
                int days = Integer.parseInt(daysStr);
                String date = DateUtils.getCurrentDate();

                DBHelper dbHelper = Singleton.getInstance().getDataBase();
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();

                // NotWorksDay ekleme
                ContentValues nwValues = new ContentValues();
                nwValues.put("days", days);
                nwValues.put("date", date);
                nwValues.put("reason", reason);
                nwValues.put("employeeId", selectedEmployee.getDbId());
                long id = db.insert(DBHelper.TABLE_NOT_WORKS_DAYS, null, nwValues);

                if(id == -1) throw new Exception("NotWorksDay eklenemedi");

                // Employee totalNotWorksDay güncelleme
                int newTotal = selectedEmployee.getTotalNotWorksDay() + days;
                ContentValues empValues = new ContentValues();
                empValues.put("totalNotWorksDay", newTotal);
                db.update(DBHelper.TABLE_EMPLOYEES, empValues, "id=?", new String[]{String.valueOf(selectedEmployee.getDbId())});

                db.setTransactionSuccessful();

                requireActivity().runOnUiThread(() -> {
                    NotWorksDay newDay = new NotWorksDay(days, date, reason);
                    newDay.setId((int) id);
                    selectedEmployee.getEmpNotWorksDayLst().add(0, newDay);
                    selectedEmployee.setTotalNotWorksDay(newTotal);
                    adapter.addPayment(newDay);
                    bnd.notWorksDayAmountEditTxt.setText("");
                    bnd.notWorksDayReasonEditTxt.setText("");
                });

            } catch(Exception e) {
                Log.e("ADD_NWD", "Hata: ", e);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                if(db != null) {
                    db.endTransaction();
                    db.close();
                }
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

                // NotWorksDay silme
                int deletedRows = db.delete(DBHelper.TABLE_NOT_WORKS_DAYS, "id=?", new String[]{String.valueOf(day.getId())});
                if(deletedRows == 0) throw new Exception("Silme işlemi başarısız");

                // Employee totalNotWorksDay güncelleme
                int newTotal = selectedEmployee.getTotalNotWorksDay() - day.getDays();
                ContentValues empValues = new ContentValues();
                empValues.put("totalNotWorksDay", newTotal);
                db.update(DBHelper.TABLE_EMPLOYEES, empValues, "id=?", new String[]{String.valueOf(selectedEmployee.getDbId())});

                db.setTransactionSuccessful();

                requireActivity().runOnUiThread(() -> {
                    selectedEmployee.setTotalNotWorksDay(newTotal);
                    selectedEmployee.getEmpNotWorksDayLst().remove(position);
                    adapter.updateList(selectedEmployee.getEmpNotWorksDayLst());
                });

            } catch(Exception e) {
                Log.e("DELETE_NWD", "Hata: ", e);
            } finally {
                if(db != null) {
                    db.endTransaction();
                    db.close();
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
        Executors.newSingleThreadExecutor().execute(() -> {
            SQLiteDatabase db = null;
            try {
                DBHelper dbHelper = Singleton.getInstance().getDataBase();
                db = dbHelper.getWritableDatabase();
                db.beginTransaction();

                // Tüm NotWorksDay kayıtlarını sil
                db.delete(DBHelper.TABLE_NOT_WORKS_DAYS, "employeeId=?", new String[]{String.valueOf(selectedEmployee.getDbId())});

                // Employee totalNotWorksDay sıfırla
                ContentValues empValues = new ContentValues();
                empValues.put("totalNotWorksDay", 0);
                db.update(DBHelper.TABLE_EMPLOYEES, empValues, "id=?", new String[]{String.valueOf(selectedEmployee.getDbId())});

                db.setTransactionSuccessful();

                requireActivity().runOnUiThread(() -> {
                    selectedEmployee.setTotalNotWorksDay(0);
                    selectedEmployee.getEmpNotWorksDayLst().clear();
                    adapter.updateList(new ArrayList<>());
                });

            } catch(Exception e) {
                Log.e("CLEAN_NWD", "Hata: ", e);
            } finally {
                if(db != null) {
                    db.endTransaction();
                    db.close();
                }
            }
        });
    }
}