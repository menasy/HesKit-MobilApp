package com.menasy.heskit;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.menasy.heskit.databinding.FragmentCalisanlarBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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
        // Alt buton click listener
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
        // Custom layout inflate
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_number_input, null);
        // View elementlerini bulma
        EditText inputField = dialogView.findViewById(R.id.input_field);
        Button btnPositive = dialogView.findViewById(R.id.btn_positive);
        Button btnNegative = dialogView.findViewById(R.id.btn_negative);
        // Dialog oluşturma
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        // Arkaplan stilini ayarla
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_stats_card);
        // Pozitif buton click listener
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
        // Negatif buton click listener
        btnNegative.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        // İsteğe bağlı: Klavye otomatik açılması
        inputField.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(inputField, InputMethodManager.SHOW_IMPLICIT);
        }, 100);
    }

    private void processEnteredNumber(int number) {
        ArrayList<Employee> selectedEmployees = adapter.getSelectedEmployees();
        String currentDate = DateUtils.getCurrentDate();
        for (Employee emp : selectedEmployees) {
            OverDay overDay = new OverDay(currentDate, number);
            emp.getEmpOverDayLst().add(0, overDay);
            // Veritabanına kaydet
            DBHelper dbHelper = Singleton.getInstance().getDataBase();
            long insertedId = dbHelper.addOverDay(currentDate, number, emp.getDbId());
            overDay.setId((int) insertedId);
        }
        adapter.notifyItemRangeChanged(0, selectedEmployees.size());
        adapter.exitSelectionMode();
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
        adapter.updateList(filteredList);
    }

    public static void loadEmployeeDataFromDB() {
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_EMPLOYEES, null);
        empList.clear();
        originalEmpList.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idColumnIndex = cursor.getColumnIndex("id");
                int nameColumnIndex = cursor.getColumnIndex("name");
                int surNameColumnIndex = cursor.getColumnIndex("surName");
                int totalMoneyColumnIndex = cursor.getColumnIndex("totalMoney");
                int dateInColumnIndex = cursor.getColumnIndex("dateIn");
                int totalTransferIndex = cursor.getColumnIndex("totalTransfer");
                if (idColumnIndex != -1 && nameColumnIndex != -1 && surNameColumnIndex != -1
                        && totalMoneyColumnIndex != -1 && dateInColumnIndex != -1 && totalTransferIndex != -1) {
                    long dbId = cursor.getLong(idColumnIndex);
                    String name = cursor.getString(nameColumnIndex);
                    String surName = cursor.getString(surNameColumnIndex);
                    long totalMoney = cursor.getLong(totalMoneyColumnIndex);
                    String dateInStr = cursor.getString(dateInColumnIndex);
                    long totalTransfer = cursor.getLong(totalTransferIndex);
                    int[] dateIn = processDateIn(dateInStr);
                    Employee emp = new Employee(name, surName, dateIn);
                    emp.setDbId(dbId); // ID'yi set et
                    emp.setTotalMoney(totalMoney);
                    emp.setTotalTransfer(totalTransfer);
                    ArrayList<EmployeePayment> payments = dbHelper.getPaymentsForEmployee(dbId);
                    emp.setEmpPaymentLst(payments);
                    empList.add(emp);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        originalEmpList.addAll(empList);
    }

    private static int[] processDateIn(String dateInStr) {
        try {
            // Hem '/' hem de '.' karakterlerini ayırıcı olarak kullan
            String[] dateParts = dateInStr.split("[/\\.]+");
            int[] dateIn = new int[3];
            for (int i = 0; i < 3; i++) {
                dateIn[i] = Integer.parseInt(dateParts[i].trim());
            }
            return dateIn;
        } catch (Exception e) {
            Log.e("DateUtils", "Geçersiz tarih formatı: " + dateInStr);
            return new int[]{1, 1, 2023}; // Varsayılan değer
        }
    }
}