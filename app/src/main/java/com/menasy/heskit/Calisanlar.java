package com.menasy.heskit;

import static com.menasy.heskit.EmployeeProcces.updateEmployee;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.menasy.heskit.databinding.CalisanRecyclerBinding;
import com.menasy.heskit.databinding.FragmentCalisanlarBinding;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Calisanlar#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Calisanlar extends Fragment {
    @NonNull FragmentCalisanlarBinding bnd;
    static public ArrayList <Employee> empList;
    static CalisanAdapter adapter;

    static {
        empList = new ArrayList<>();
    }
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Calisanlar() {
        // Required empty public constructor
    }



    // TODO: Rename and change types and number of parameters
    public static Calisanlar newInstance(String param1, String param2) {
        Calisanlar fragment = new Calisanlar();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        bnd = FragmentCalisanlarBinding.inflate(inflater, container, false);
        View view = bnd.getRoot();

        // RecyclerView ve Adapter'ı ayarla
        bnd.fragmentCalisanRecView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CalisanAdapter(empList);
        bnd.fragmentCalisanRecView.setAdapter(adapter);
        loadEmployeeDataFromDB();
        setupClickListeners();
        return view;
    }

    private void setupClickListeners() {
        // Çalışanlar Butonu
        bnd.addEmployee.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToAddEmp();
            }
        });
    }
    public  static void loadEmployeeDataFromDB() {
        // Veritabanından çalışan verilerini çekme işlemi
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_EMPLOYEES, null);

        empList.clear(); // Listeyi temizle
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Cursor'dan sütun değerlerini almak
                int nameColumnIndex = cursor.getColumnIndex("name");
                int surNameColumnIndex = cursor.getColumnIndex("surName");
                int totalMoneyColumnIndex = cursor.getColumnIndex("totalMoney");
                int dateInColumnIndex = cursor.getColumnIndex("dateIn");

                if (nameColumnIndex != -1 && surNameColumnIndex != -1 && totalMoneyColumnIndex != -1 && dateInColumnIndex != -1) {
                    String name = cursor.getString(nameColumnIndex);
                    String surName = cursor.getString(surNameColumnIndex);
                    int totalMoney = cursor.getInt(totalMoneyColumnIndex);
                    String dateInStr = cursor.getString(dateInColumnIndex);

                    // dateInString'i işleyerek dateIn array'e dönüştürme
                    int[] dateIn = processDateIn(dateInStr);

                    // Çalışan nesnesi oluştur
                    Employee emp = new Employee(name, surName, dateIn);
                    emp.setTotalMoney(totalMoney);
                    empList.add(emp);
                    updateEmployee(emp);
                } else {
                    // Sütun adlarının yanlış olması durumunda hata mesajı log'lama
                    Log.e("DBHelper", "Veritabanında eksik sütunlar var!");
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
    private static int[]   processDateIn(String dateInStr) {
        // dateInStr'yi int[] formatına dönüştürmek için örnek bir metot
        // Burada veritabanındaki tarih formatına göre düzenleme yapman gerekebilir.
        // Örnek olarak "yyyy-MM-dd" formatında bir tarih dizisi işleniyor.
        String[] dateParts = dateInStr.split("/");
        int[] dateIn = new int[dateParts.length];
        for (int i = 0; i < dateParts.length; i++) {
            dateIn[i] = Integer.parseInt(dateParts[i]);
        }
        return dateIn;
    }
}


