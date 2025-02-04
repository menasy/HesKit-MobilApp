package com.menasy.heskit;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executors;

public class Employee implements Serializable
{
    private static int index;
    static {
        index = 1;
    }

    private int id;
    private long dbId;
    private int[] dateIn;
    private String name;
    private String surName;
    private int worksDay;
    private int totalNotWorksDay;
    private long totalMoney;
    private long totalTransfer;
    private ArrayList <EmployeePayment> empPaymentLst;
    private ArrayList <Transfer>    empTransferLst;
    private ArrayList <NotWorksDay>    empNotWorksDayLst;



    public Employee(String name, String surName, int[] dateIn)
    {
        this.id = index++;
        this.dateIn = new int[3];
        this.name = name;
        this.surName = surName;
        this.empPaymentLst = new ArrayList<>();
        this.empTransferLst = new ArrayList<>();
        this.empNotWorksDayLst = new ArrayList<>();
        for(int i = 0; i < 3; i++)
            this.dateIn[i] = dateIn[i];
        this.worksDay = calcWorksDay(dateIn);
    }

    public Employee() {
        this.empPaymentLst = new ArrayList<>();
        this.empTransferLst = new ArrayList<>();
        this.dateIn = new int[3];
    }
    public ArrayList<EmployeePayment> getEmpPaymentLst() {
        return empPaymentLst;
    }
    public void setEmpPaymentLst(ArrayList<EmployeePayment> empPaymentLst) {
        this.empPaymentLst = empPaymentLst;
    }

    private  int calcWorksDay(int[] dateIn)
    {
        // Güncel tarih bilgilerini al
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Aylar 0'dan başlıyor
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Giriş tarihi bilgileri
        int inYear = dateIn[2];
        int inMonth = dateIn[1];
        int inDay = dateIn[0];

        // Giriş tarihini ve bugünü oluştur
        Calendar startDate = Calendar.getInstance();
        startDate.set(inYear, inMonth - 1, inDay); // Giriş tarihi

        Calendar endDate = Calendar.getInstance();
        endDate.set(year, month - 1, day); // Bugün

        // İki tarih arasındaki farkı gün cinsinden hesapla
        long differenceInMillis = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        int totalDays = (int) (differenceInMillis / (1000 * 60 * 60 * 24));

        return totalDays;
    }
    public void displayDateIn(TextView textView) {
        String formattedDate = dateIn[0] + "/" + dateIn[1] + "/" + dateIn[2];
        textView.setText("Başlangıç Tarihi: " + formattedDate);
    }
    public String getDateInStr() {
        String formattedDate = dateIn[0] + "/" + dateIn[1] + "/" + dateIn[2];
        return formattedDate;
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public void setTotalMoney(long totalMoney) {
        this.totalMoney = totalMoney;
    }

    public int[] getDateIn() {
        return dateIn;
    }

    public String getName() {
        return name;
    }
    public int getId() {
        return id;
    }
    public String getSurName() {
        return surName;
    }
    public String getNameAndSurname()
    {
        String dest = getName() + " " + getSurName();
        return dest;
    }
    public void setWorksDay(int worksDay) {
        this.worksDay = worksDay;
    }
    public int getWorksDay() {
        return worksDay;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }


    public void addPayment(EmployeePayment empPayment) {
        if (empPayment != null)
        {
            if (empPaymentLst == null)
                empPaymentLst = new ArrayList<>();
            empPaymentLst.add(empPayment);
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTotalMoney()
    {
        return this.totalMoney;
    }

    public void setDateIn(int day, int month, int year)
    {
        dateIn[0] = day;
        dateIn[1] = month;
        dateIn[2] = year;
    }

    public long getTotalTransferAndPayment()
    {
        return (this.totalMoney + this.totalTransfer);
    }

    public Long empPutDataBase() {
        try {
            DBHelper dbHelper = Singleton.getInstance().getDataBase();
            return dbHelper.addEmployee(this.name,this.surName,this.worksDay,this.getTotalMoney(),this.getDateInStr());
        } catch (SQLiteException e) {
            Log.e("DB", "Veritabanı hatası: " + e.getMessage());
            return -1L;
        }
    }

    public ArrayList<Transfer> getEmpTransferLst() {
        return empTransferLst;
    }

    public void setEmpTransferLst(ArrayList<Transfer> empTransferLst) {
        this.empTransferLst = empTransferLst;
    }

    public long getTotalTransfer() {
        return totalTransfer;
    }

    public void setTotalTransfer(long totalTransfer) {
        this.totalTransfer = totalTransfer;
    }

    public void addTransfer(Transfer transfer) {
        if(transfer != null) {
            if(empTransferLst == null) empTransferLst = new ArrayList<>();
            empTransferLst.add(transfer);
            totalTransfer += transfer.getAmountTransfer();
        }
    }

    public void removeTransfer(int position) {
        if(position >= 0 && position < empTransferLst.size()) {
            Transfer t = empTransferLst.get(position);
            totalTransfer -= t.getAmountTransfer();
            empTransferLst.remove(position);
        }
    }
    public void setDateInFromString(String dateStr) {
        this.dateIn = DateUtils.parseDateArray(dateStr);
    }

    public void setDateIn(int[] dateArray) {
        if(dateArray != null && dateArray.length == 3) {
            this.dateIn[0] = dateArray[0];
            this.dateIn[1] = dateArray[1];
            this.dateIn[2] = dateArray[2];
        }
    }

    public ArrayList<NotWorksDay> getEmpNotWorksDayLst() {
        if (empNotWorksDayLst == null) empNotWorksDayLst = new ArrayList<>();
        return empNotWorksDayLst;
    }

    public void setEmpNotWorksDayLst(ArrayList<NotWorksDay> empNotWorksDayLst) {
        this.empNotWorksDayLst = empNotWorksDayLst;
    }

    public int getTotalNotWorksDay() {
        return totalNotWorksDay;
    }

    public void setTotalNotWorksDay(int total) {
        this.totalNotWorksDay = total;
        // Veritabanını da güncelle
        Executors.newSingleThreadExecutor().execute(() -> {
            DBHelper dbHelper = Singleton.getInstance().getDataBase();
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("totalNotWorksDay", total);
            db.update(DBHelper.TABLE_EMPLOYEES, values, "id=?",
                    new String[]{String.valueOf(this.dbId)});
            db.close();
        });
    }
}
