package com.menasy.heskit;

import static com.menasy.heskit.Calisanlar.adapter;
import static com.menasy.heskit.Calisanlar.empList;
import static com.menasy.heskit.DBHelper.TABLE_EMPLOYEES;
import static com.menasy.heskit.DBHelper.TABLE_PAYMENTS;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.menasy.heskit.databinding.FragmentAddEmployeBinding;
import com.menasy.heskit.databinding.FragmentEmployeeProccesBinding;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EmployeeProcces#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EmployeeProcces extends Fragment {

    CalisanAdapter getAdapter;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Employee getEmp;
    private FragmentEmployeeProccesBinding bnd;
    static EmployeeProccesAdapter empProccAdapter;

    public EmployeeProcces() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EmployeeProcces.
     */
    // TODO: Rename and change types and number of parameters
    public static EmployeeProcces newInstance(String param1, String param2) {
        EmployeeProcces fragment = new EmployeeProcces();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        bnd = FragmentEmployeeProccesBinding.inflate(inflater, container, false); // Kullanıcıdan gelen inflater'ı kullan
        view = bnd.getRoot();

        if (getArguments() != null) {
            // Argümanlardan çalışan ve adaptör bilgilerini al
            getEmp = (Employee) getArguments().getSerializable("Employee");
            getAdapter = (CalisanAdapter) getArguments().getSerializable("CalisanAdapter");

            if (getEmp != null) {
                // Çalışan bilgilerini UI'a yansıt
                bnd.empProcTitleTxt.setText(getEmp.getNameAndSurname());
                getEmp.displayDateIn(bnd.dateInTxt);
                bnd.countDayTxt.setText("Çalıştığı Gün Sayısı: " + getEmp.getWorksDay());
                bnd.takedMoneyTxtView.setText("Aldığı Toplam Harçlık: " + getEmp.getTotalMoney());

                // RecyclerView'i bir kez kur
                if (getEmp.getEmpPaymentLst() != null && !getEmp.getEmpPaymentLst().isEmpty()) {
                    empProccAdapter = new EmployeeProccesAdapter(getEmp.getEmpPaymentLst());
                } else {
                    empProccAdapter = new EmployeeProccesAdapter(new ArrayList<>());
                }
                bnd.paymentRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                bnd.paymentRecycler.setAdapter(empProccAdapter);

                // Buton tıklama olaylarını tanımla
                bnd.addMoneyBut.setOnClickListener(v -> addMoneyButton());
                bnd.deleteEmpBut.setOnClickListener(v -> deleteEmpButton());

            } else {
                // Çalışan bilgisi yoksa kullanıcıyı bilgilendir
                Toast.makeText(getContext(), "Çalışan bilgisi alınamadı.", Toast.LENGTH_SHORT).show();
            }
        }

        return view; // Fragment'ın görünümünü döndür
    }


    private void deleteEmp ()
    {
        if (empList != null)
        {
            int id;
            for (int i = 0; i < empList.size(); i++)
            {
                id = empList.get(i).getId();
                if (id == getEmp.getId())
                {
                    empList.remove(i);
                    break;
                }
            }
        }
    }
    //id ile ilgili sroun var
    public void deleteEmpButton()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(getEmp.getNameAndSurname() + " Silinsin Mi?");
        alert.setPositiveButton("Evet", (dialog, which) -> {
            Log.d("Bak", "DbId before delete:" + getEmp.getDbId());
            deleteEmpFromDatabase(getEmp.getDbId()); // Veritabanından sil
            deleteEmp(); // Listeden sil
            if (Calisanlar.adapter != null) {
                Calisanlar.adapter.updateList(Calisanlar.empList); // Adapteri güncelle
            }
            Toast.makeText(getContext(), "Çalışan Silindi!", Toast.LENGTH_SHORT).show();
            requireActivity().getOnBackPressedDispatcher().onBackPressed(); // Geriye dön
        });
        alert.setNegativeButton("Hayır", (dialog, which) ->
                Toast.makeText(getContext(), "Çalışan Silinmedi!", Toast.LENGTH_SHORT).show()
        );
        alert.show();
    }

    private void deleteEmpFromDatabase(long empId) {

        Log.d("Bak", "Veritabanından silme işlemi başlıyor. Emp ID: " + empId);
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Çalışanın ödemelerini sil
        int paymentDeleteResult = db.delete(TABLE_PAYMENTS, "employeeId = ?", new String[]{String.valueOf(empId)});
        Log.d("Bak", "Çalışanın ödemeleri silindi mi? Sonuç: " + paymentDeleteResult);
        // Çalışanı sil
        int empDeleteResult = db.delete(TABLE_EMPLOYEES, "id = ?", new String[]{String.valueOf(empId)});
        Log.d("Bak", "Çalışan silindi mi? Sonuç: " + empDeleteResult);

        db.close();
    }

    public void addMoneyButton() {
        String getMoneyStr = bnd.moneyEditTxt.getText().toString();

        if (!getMoneyStr.matches("") && !getMoneyStr.matches("0")) {
            int getMoney = Integer.parseInt(getMoneyStr);

            // Çalışanın ödeme listesine yeni ödeme ekle
            if (getEmp.getEmpPaymentLst() == null) {
                getEmp.setEmpPaymentLst(new ArrayList<>()); // Eğer liste null ise yeni bir liste oluştur
            }
            // Ödeme veritabanına kaydediliyor
            DBHelper dbHelper = Singleton.getInstance().getDataBase();
            long paymentId = dbHelper.addPayment(getMoney, DateUtils.getCurrentDate(), getEmp.getId());

            if (paymentId > 0) { // Veritabanına başarıyla eklendiyse
                // Tarihi parçalayarak diziye dönüştürelim
                int[] currentDate = DateUtils.getCurrentDateArray();
                EmployeePayment payment = new EmployeePayment(getMoney, currentDate);
                payment.setId((int) paymentId); // Veritabanından dönen ID'yi sete ekle
                getEmp.addPayment(payment);
                payment.paymentPutDataBase(getEmp.getDbId());
                // RecyclerView adaptörü güncelleniyor
                empProccAdapter.updateList(getEmp.getEmpPaymentLst());
                // Çalışanın toplam harçlık bilgisini güncelle
                updateEmployee(getEmp);
                bnd.takedMoneyTxtView.setText("Aldığı Toplam Harçlık: " + getEmp.getTotalMoney());
                // Çalışan listesini güncelle
                if (Calisanlar.adapter != null) {
                    Calisanlar.adapter.updateList(Calisanlar.empList); // Çalışanlar adaptörünü güncelle
                }
                // Başarı mesajı göster
                Toast.makeText(this.getContext(), "Harçlık Eklendi", Toast.LENGTH_SHORT).show();
            } else {
                // Veritabanı hatası
                Toast.makeText(this.getContext(), "Harçlık Veritabanına Eklenemedi!", Toast.LENGTH_SHORT).show();
            }

        }
        else {
            // Boş giriş hatası
            Toast.makeText(this.getContext(), "Harçlık Boş Olamaz!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void updateEmployee(Employee employee) {
        if (empList != null) {
            int id;
            for (int i = 0; i < empList.size(); i++) {
                id = empList.get(i).getId();
                if (id == employee.getId()) {
                    empList.set(i, employee);
                }
            }
        }
    }
    public ArrayList<Employee> getAllEmployees() {
        ArrayList<Employee> employees = new ArrayList<>();

        // Singleton üzerinden DBHelper alınıyor
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DBHelper.TABLE_EMPLOYEES, null, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // Veritabanından sütunları okuyarak Employee nesnesi oluşturuyoruz
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String surName = cursor.getString(cursor.getColumnIndexOrThrow("surName"));
                int worksDay = cursor.getInt(cursor.getColumnIndexOrThrow("worksDay"));
                int totalMoney = cursor.getInt(cursor.getColumnIndexOrThrow("totalMoney"));
                String dateInStr = cursor.getString(cursor.getColumnIndexOrThrow("dateIn"));

                // Tarihi parçalayarak diziye dönüştürüyoruz
                String[] dateParts = dateInStr.split("/");
                int[] dateIn = new int[3];
                dateIn[0] = Integer.parseInt(dateParts[0]); // Gün
                dateIn[1] = Integer.parseInt(dateParts[1]); // Ay
                dateIn[2] = Integer.parseInt(dateParts[2]); // Yıl

                // Employee nesnesi oluştur ve listeye ekle
                Employee employee = new Employee(name, surName, dateIn);
                employee.setId(id);
                employee.setWorksDay(worksDay);
                employee.setTotalMoney(totalMoney);

                // Ödemeleri ekliyoruz
                ArrayList<EmployeePayment> payments = getPaymentsForEmployee(id);
                employee.setEmpPaymentLst(payments);

                employees.add(employee);
            }
            cursor.close();
        }

        return employees;
    }

    private ArrayList<EmployeePayment> getPaymentsForEmployee(long employeeId) {
        ArrayList<EmployeePayment> payments = new ArrayList<>();

        // Singleton üzerinden DBHelper alınıyor
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = "employeeId = ?";
        String[] selectionArgs = {String.valueOf(employeeId)};
        Cursor cursor = db.query(DBHelper.TABLE_PAYMENTS, null, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // Veritabanından sütunları okuyarak EmployeePayment nesnesi oluşturuyoruz
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int amount = cursor.getInt(cursor.getColumnIndexOrThrow("amount"));
                String paymentDate = cursor.getString(cursor.getColumnIndexOrThrow("paymentDate"));

                // Tarihi parçalayarak diziye dönüştürüyoruz
                String[] dateParts = paymentDate.split("/");
                int[] date = new int[3];
                date[0] = Integer.parseInt(dateParts[0]); // Gün
                date[1] = Integer.parseInt(dateParts[1]); // Ay
                date[2] = Integer.parseInt(dateParts[2]); // Yıl

                // EmployeePayment nesnesi oluştur ve listeye ekle
                EmployeePayment payment = new EmployeePayment(amount, date);
                payment.setId(id);
                payments.add(payment);
            }
            cursor.close();
        }

        return payments;
    }


}