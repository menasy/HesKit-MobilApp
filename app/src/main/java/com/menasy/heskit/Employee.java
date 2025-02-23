package com.menasy.heskit;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
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
    private int[] dismissDate;
    private String name;
    private String surName;
    private boolean dismissCheck;
    private int totalNotWorksDay;
    private long totalMoney;
    private long totalTransfer;
    private int totalOverDay;

    private ArrayList <EmployeePayment> empPaymentLst;
    private ArrayList <Transfer>    empTransferLst;
    private ArrayList <NotWorksDay>    empNotWorksDayLst;
    private ArrayList <OverDay>    empOverDayLst;

    private boolean isSelected = false;
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }


    public Employee(String name, String surName, int[] dateIn)
    {
        this.id = index++;
        this.dateIn = new int[3];
        this.name = name;
        this.surName = surName;
        this.dismissCheck = false;
        this.dismissDate = null;
        this.empPaymentLst = new ArrayList<>();
        this.empTransferLst = new ArrayList<>();
        this.empNotWorksDayLst = new ArrayList<>();
        this.empOverDayLst = new ArrayList<>();
        for(int i = 0; i < 3; i++)
            this.dateIn[i] = dateIn[i];
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

    private int calcWorksDay(int[] dateIn) {
        Calendar startDate = Calendar.getInstance();
        startDate.set(dateIn[2], dateIn[1]-1, dateIn[0]);

        Calendar endDate;
        if (this.dismissDate != null) {
            endDate = Calendar.getInstance();
            endDate.set(dismissDate[2], dismissDate[1]-1, dismissDate[0]);
        } else {
            endDate = Calendar.getInstance();
        }

        long difference = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        return (int) (difference / (1000 * 60 * 60 * 24)) + 1;
    }
    public String getDateInStr() {
        String formattedDate = dateIn[0] + "." + dateIn[1] + "." + dateIn[2];
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


    public int getWorksDay() {
        return calcWorksDay(this.dateIn);
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTotalMoney()
    {
        return this.totalMoney;
    }

    public long getTotalTransferAndPayment()
    {
        return (this.totalMoney + this.totalTransfer);
    }

    public Long empPutDataBase() {
        try {
            DBHelper dbHelper = Singleton.getInstance().getDataBase();
            return dbHelper.addEmployee(this.name,this.surName,this.getTotalMoney(),this.getDateInStr());
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

        Executors.newSingleThreadExecutor().execute(() -> {
            DBHelper dbHelper = Singleton.getInstance().getDataBase();
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("totalNotWorksDay", total);
            db.update(DBHelper.TABLE_EMPLOYEES, values, "id=?",
                    new String[]{String.valueOf(this.dbId)});
        });
    }




    public ArrayList<OverDay> getEmpOverDayLst() {
        return empOverDayLst;
    }

    public void setEmpOverDayLst(ArrayList<OverDay> empOverDayLst) {
        this.empOverDayLst = empOverDayLst;
    }

    public int getTotalOverDay() {
        return totalOverDay;
    }

    public void setTotalOverDay(int totalOverDay) {
        this.totalOverDay = totalOverDay;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                DBHelper dbHelper = Singleton.getInstance().getDataBase();
                dbHelper.updateEmployeeOverDay(this.dbId, totalOverDay);
            } catch (Exception e) {
                Log.e("THREAD_ERROR", "Mesai güncelleme hatası: ", e);
            } finally {
                executor.shutdown(); // Thread'i kapat
            }
        });
    }

    public int[] getDismissDate() { return dismissDate; }
    public void setDismissDate(int[] dismissDateArray) {
        if(dismissDateArray != null && dismissDateArray.length == 3) {
            this.dismissDate = new int[3];
            this.dismissDate[0] = dismissDateArray[0];
            this.dismissDate[1] = dismissDateArray[1];
            this.dismissDate[2] = dismissDateArray[2];
        } else {
            this.dismissDate = null;
        }
    }

    public String getDismissDateStr() {
        if(dismissDate == null) return "Çalışıyor";
        return dismissDate[0] + "." + dismissDate[1] + "." + dismissDate[2];
    }
    public void setDismissCheck(boolean val)
    {
        this.dismissCheck = val;
    }
    public boolean getDismissCheck()
    {
        return  this.dismissCheck;
    }
    public boolean isDismissed() {
        return dismissDate != null;
    }
}
