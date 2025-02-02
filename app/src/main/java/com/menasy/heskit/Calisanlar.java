package com.menasy.heskit;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    static public ArrayList<Employee> empList = new ArrayList<>();
    static CalisanAdapter adapter;
    private static Calisanlar instance;
    private static ArrayList<Employee> originalEmpList = new ArrayList<>();//searchview için

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bnd = FragmentCalisanlarBinding.inflate(inflater, container, false);
        View view = bnd.getRoot();

        bnd.fragmentCalisanRecView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CalisanAdapter(empList);
        bnd.fragmentCalisanRecView.setAdapter(adapter);

        adapter.setOnEmployeeClickListener(employee -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToEmpProcces(employee);
            }
        });

        setupClickListeners();
        loadEmployeeDataFromDB();
        updateSummaryViews();
        setupSearchView();
        return view;
    }

    private void setupClickListeners() {
        bnd.addEmployee.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToAddEmp();
            }
        });
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
        for(Employee employee : originalEmpList) {
            String fullName = employee.getNameAndSurname().toLowerCase();
            if(fullName.contains(text)) {
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
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idColumnIndex = cursor.getColumnIndex("id");
                int nameColumnIndex = cursor.getColumnIndex("name");
                int surNameColumnIndex = cursor.getColumnIndex("surName");
                int totalMoneyColumnIndex = cursor.getColumnIndex("totalMoney");
                int dateInColumnIndex = cursor.getColumnIndex("dateIn");

                if (idColumnIndex != -1 && nameColumnIndex != -1 && surNameColumnIndex != -1
                        && totalMoneyColumnIndex != -1 && dateInColumnIndex != -1) {

                    long dbId = cursor.getLong(idColumnIndex);
                    String name = cursor.getString(nameColumnIndex);
                    String surName = cursor.getString(surNameColumnIndex);
                    int totalMoney = cursor.getInt(totalMoneyColumnIndex);
                    String dateInStr = cursor.getString(dateInColumnIndex);

                    int[] dateIn = processDateIn(dateInStr);
                    Employee emp = new Employee(name, surName, dateIn);
                    emp.setDbId(dbId); // ID'yi set et
                    emp.setTotalMoney(totalMoney);
                    ArrayList<EmployeePayment> payments = dbHelper.getPaymentsForEmployee(dbId);
                    emp.setEmpPaymentLst(payments);
                    empList.add(emp);

                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        if(getInstance() != null) {
            getInstance().updateSummaryViews();
        }
        originalEmpList.clear();
        originalEmpList.addAll(empList);
    }

    private static Calisanlar getInstance() {
        return instance;
    }

    private static int[] processDateIn(String dateInStr) {
        String[] dateParts = dateInStr.split("/");
        int[] dateIn = new int[dateParts.length];
        for (int i = 0; i < dateParts.length; i++) {
            dateIn[i] = Integer.parseInt(dateParts[i]);
        }
        return dateIn;
    }

    private void updateSummaryViews() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                int employeeCount = empList.size();
                bnd.empCountTxtView.setText("Çalışan Sayısı: " + employeeCount);

                int totalPayment = calculateTotalPayment();
                bnd.calisanTotalPaymentTxt.setText("Toplam Harçlık: " + totalPayment + "₺");
            });
        }
    }

    private int calculateTotalPayment() {
        return Singleton.getInstance().getDataBase().getTotalPayments();
    }

}