package com.menasy.heskit;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.menasy.heskit.databinding.FragmentCalisanlarBinding;
import java.util.ArrayList;

public class Calisanlar extends Fragment {

    @NonNull
    FragmentCalisanlarBinding bnd;
    static public ArrayList<Employee> empList = new ArrayList<>();
    static CalisanAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bnd = FragmentCalisanlarBinding.inflate(inflater, container, false);
        View view = bnd.getRoot();

        bnd.fragmentCalisanRecView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CalisanAdapter(empList);
        bnd.fragmentCalisanRecView.setAdapter(adapter);

        adapter.setOnEmployeeClickListener(employee -> {
            EmployeeProcces.setSelectedEmployee(employee);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToEmpProcces();
            }
        });

        loadEmployeeDataFromDB();
        setupClickListeners();
//        updateSummaryViews();
        return view;
    }

    private void setupClickListeners() {
        bnd.addEmployee.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToAddEmp();
            }
        });
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
                    empList.add(emp);
//                    updateSummaryViews();

                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
    private  void updateSummaryViews() {
        // Çalışan sayısını güncelle
        int employeeCount = empList.size();
        bnd.empCountTxtView.setText("Çalışan Sayısı: " + employeeCount);

        // Toplam harçlığı hesapla ve güncelle
        int totalPayment = getAllPayment();
        bnd.calisanTotalPaymentTxt.setText("Toplam Harçlık: " + totalPayment + "₺");
    }
    private static int getAllPayment()
    {
        int totalPayment = 0;

//        for (int i = 0; i < empList.size(); i++)
//        {
//            for (int j = 0; j < empList.get(i).getEmpPaymentLst().size(); j++)
//            {
//                Employee emp = empList.get(i);
//                for (int k = 0; k < emp.getEmpPaymentLst().size(); k++)
//                {
//                    totalPayment += emp.getEmpPaymentLst().get(k).getTakedMoney();
//                }
//            }
//        }

        for (Employee emp : empList)
        {
            for (EmployeePayment empPayment : emp.getEmpPaymentLst())
            {
                totalPayment += empPayment.getTakedMoney();
            }
        }
        return totalPayment;
    }
    private static int[] processDateIn(String dateInStr) {
        String[] dateParts = dateInStr.split("/");
        int[] dateIn = new int[dateParts.length];
        for (int i = 0; i < dateParts.length; i++) {
            dateIn[i] = Integer.parseInt(dateParts[i]);
        }
        return dateIn;
    }

}