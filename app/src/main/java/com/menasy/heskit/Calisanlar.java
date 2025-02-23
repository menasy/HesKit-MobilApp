package com.menasy.heskit;

import static com.menasy.heskit.DBHelper.TABLE_OVER_DAYS;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.menasy.heskit.databinding.FragmentCalisanlarBinding;
import java.util.ArrayList;


public class Calisanlar extends Fragment {

    @NonNull
    FragmentCalisanlarBinding bnd;
    static CalisanAdapter adapter;
    static public ArrayList<Employee> empList = new ArrayList<>();
    private static ArrayList<Employee> originalEmpList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bnd = FragmentCalisanlarBinding.inflate(inflater, container, false);
        if (adapter == null) {
            adapter = new CalisanAdapter(new ArrayList<>());
        }
        if (empList == null) {
            empList = new ArrayList<>();
        }

        initializeComponents();
        setupListeners();
        bnd.bottomActionButton.setVisibility(View.GONE);
        bnd.selectAllEmp.setVisibility(View.GONE);
        bnd.addEmployee.setVisibility(View.VISIBLE);
        loadEmployeeDataFromDB();
        adapter.updateList(empList);
        return bnd.getRoot();
    }

    public void onResume() {
        super.onResume();
        loadEmployeeDataFromDB();
        adapter.updateList(empList);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adapter != null) adapter.exitSelectionMode();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadEmployeeDataFromDB();
        adapter.updateList(empList);
    }

    private void initializeComponents() {
        bnd.fragmentCalisanRecView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CalisanAdapter(empList);
        bnd.fragmentCalisanRecView.setAdapter(adapter);

        adapter.setOnSelectionListener(selectedCount -> {
            boolean hasSelection = selectedCount > 0;
            bnd.bottomActionButton.setVisibility(hasSelection ? View.VISIBLE : View.GONE);
            bnd.selectAllEmp.setVisibility(hasSelection ? View.VISIBLE : View.GONE);
            bnd.addEmployee.setVisibility(hasSelection ? View.GONE : View.VISIBLE);
            bnd.bottomActionButton.setText(selectedCount + " Mesai Ekle");
            updateSelectAllButtonText();
        });
    }

    private void setupListeners() {
        bnd.bottomActionButton.setOnClickListener(v -> showNumberInputDialog());
        bnd.selectAllEmp.setOnClickListener(v -> toggleSelectAll());
        adapter.setOnEmployeeClickListener(employee -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToEmpProcces(employee);
            }
        });

        bnd.addEmployee.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToAddEmp();
            }
        });
        setupSearchView();
    }

    private void toggleSelectAll() {
        if (adapter.isAllSelected()) {
            adapter.deselectAll();
        } else {
            adapter.selectAll();
        }
    }

    private void updateSelectAllButtonText() {
        if (adapter == null || bnd.selectAllEmp == null) return;
        boolean allSelected = adapter.isAllSelected() && adapter.getItemCount() > 0;
        bnd.selectAllEmp.setText(allSelected ? "Tümünü Bırak" : " Tümünü Seç ");
    }

    private void showNumberInputDialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_number_input, null);

        EditText inputField = dialogView.findViewById(R.id.input_field);
        Button btnPositive = dialogView.findViewById(R.id.btn_positive);
        Button btnNegative = dialogView.findViewById(R.id.btn_negative);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_stats_card);

        btnPositive.setOnClickListener(v -> {
            String inputText = inputField.getText().toString().trim();
            if (!inputText.isEmpty()) {
                int value = Integer.parseInt(inputText);
                processEnteredNumber(value);
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(), "Lütfen geçerli bir değer girin", Toast.LENGTH_SHORT).show();
            }
        });

        btnNegative.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        //klavye
        inputField.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(inputField, InputMethodManager.SHOW_IMPLICIT);
        }, 100);
    }

    private void processEnteredNumber(int number) {
        ArrayList<Employee> selectedEmployees = adapter.getSelectedEmployees();
        String currentDate = DateUtils.getCurrentDate();
        DBHelper dbHelper = Singleton.getInstance().getDataBase();

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            for (Employee emp : selectedEmployees) {
                int newTotal = emp.getTotalOverDay() + number;
                emp.setTotalOverDay(newTotal);
                dbHelper.updateEmployeeOverDay(emp.getDbId(), newTotal);

                ContentValues values = new ContentValues();
                values.put("date", currentDate);
                values.put("daysAmount", number);
                values.put("employeeId", emp.getDbId());
                long insertedId = db.insert(TABLE_OVER_DAYS, null, values);

                OverDay overDay = new OverDay(currentDate, number);
                overDay.setId((int) insertedId);
                emp.getEmpOverDayLst().add(0, overDay);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DB_ERROR", "Mesai ekleme hatası: ", e);
            Toast.makeText(getContext(), "Hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if(db != null) {
                try {
                    db.endTransaction();
                } catch (Exception e) {
                    Log.e("DB_ERROR", "Transaction kapatma hatası: ", e);
                }
            }
            adapter.notifyDataSetChanged();
            adapter.exitSelectionMode();
        }
    }

    private void setupSearchView() {
        bnd.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText.toLowerCase());
                return true;
            }
        });
    }

    private void filterList(String text) {
        ArrayList<Employee> filteredList = new ArrayList<>();
        for (Employee employee : originalEmpList) {
            if (employee.getNameAndSurname().toLowerCase().contains(text)) {
                filteredList.add(employee);
            }
        }
        // Alfabetik sıralamayı koru
        filteredList.sort((e1, e2) -> {
            int nameCompare = e1.getName().compareToIgnoreCase(e2.getName());
            if(nameCompare == 0) {
                return e1.getSurName().compareToIgnoreCase(e2.getSurName());
            }
            return nameCompare;
        });
        adapter.updateList(filteredList);
    }

    public static void loadEmployeeDataFromDB() {
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {

            String query = "SELECT * FROM " + DBHelper.TABLE_EMPLOYEES +
                    " ORDER BY name COLLATE NOCASE ASC, surName COLLATE NOCASE ASC";

            cursor = db.rawQuery(query, null);

            empList.clear();
            originalEmpList.clear();

            if (cursor.moveToFirst()) {
                do {

                    int idColumnIndex = cursor.getColumnIndex("id");
                    int nameColumnIndex = cursor.getColumnIndex("name");
                    int surNameColumnIndex = cursor.getColumnIndex("surName");
                    int totalMoneyColumnIndex = cursor.getColumnIndex("totalMoney");
                    int dateInColumnIndex = cursor.getColumnIndex("dateIn");
                    int totalTransferIndex = cursor.getColumnIndex("totalTransfer");
                    int totalOverDayIndex = cursor.getColumnIndex("totalOverDay");
                    int dismissIndex = cursor.getColumnIndex("dismissDate");

                    if (idColumnIndex == -1 || nameColumnIndex == -1 ||
                            surNameColumnIndex == -1 || totalOverDayIndex == -1 ||
                            totalMoneyColumnIndex == -1 || dateInColumnIndex == -1 ||
                            totalTransferIndex == -1 || dismissIndex == -1) {
                        Log.e("DB_ERROR", "Bir veya daha fazla sütun bulunamadı!");
                        continue;
                    }


                    long dbId = cursor.getLong(idColumnIndex);
                    String name = cursor.getString(nameColumnIndex);
                    String surName = cursor.getString(surNameColumnIndex);
                    long totalMoney = cursor.getLong(totalMoneyColumnIndex);
                    String dateInStr = cursor.getString(dateInColumnIndex);
                    long totalTransfer = cursor.getLong(totalTransferIndex);
                    int totalOverDay = cursor.getInt(totalOverDayIndex);
                    int[] dateIn = DateUtils.parseDateArray(dateInStr);
                    String dismissDateStr = cursor.getString(dismissIndex);

                    Employee emp = new Employee(name, surName, dateIn);
                    emp.setDbId(dbId);
                    emp.setTotalMoney(totalMoney);
                    emp.setTotalTransfer(totalTransfer);
                    emp.setTotalOverDay(totalOverDay);
                    emp.setDismissDate(dismissDateStr != null ? DateUtils.parseDateArray(dismissDateStr) : null);


                    ArrayList<EmployeePayment> payments = dbHelper.getPaymentsForEmployee(dbId);
                    emp.setEmpPaymentLst(payments);

                    ArrayList<OverDay> overDays = dbHelper.getOverDaysForEmployee(dbId);
                    emp.setEmpOverDayLst(overDays);

                    empList.add(emp);

                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e("DB_ERROR", "Veri yükleme hatası: ", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (adapter != null) {
                adapter.updateList(empList);
            }
        }
        originalEmpList.addAll(empList);
    }
}