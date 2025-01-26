package com.menasy.heskit;

import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

public class Employee implements Serializable
{
    private static int index;
    static {
        index = 0;
    }

    private int id;
    private int[] dateIn;
    private String name;
    private String surName;
    private int worksDay;
    private ArrayList <EmployeePayment> empPaymentLst;



    public Employee(String name, String surName, int[] dateIn)
    {
        this.id = index++;
        this.dateIn = new int[3];
        this.name = name;
        this.surName = surName;
        this.empPaymentLst = null;
        for(int i = 0; i < 3; i++)
            this.dateIn[i] = dateIn[i];
        this.worksDay = calcWorksDay(dateIn);
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
        int totalDays = (int) (differenceInMillis / (1000 * 60 * 60 * 24)); // Milisaniyeden güne dönüştür

        return totalDays;
    }
    public void displayDateIn(TextView textView) {
        String formattedDate = dateIn[0] + "/" + dateIn[1] + "/" + dateIn[2]; // Gün/Ay/Yıl formatında
        textView.setText("Başlangıç Tarihi: " + formattedDate);
    }
    public String getDateInStr() {
        String formattedDate = dateIn[0] + "/" + dateIn[1] + "/" + dateIn[2]; // Gün/Ay/Yıl formatında
        return formattedDate;
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
    public void setDateIn(int[] dateIn) {
        this.dateIn = dateIn;
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

    public int getTotalMoney()
    {
        if (empPaymentLst == null)
            return 0;
        int sum = 0, i = -1;
        while (++i < empPaymentLst.size())
            sum += empPaymentLst.get(i).getPayment();
        return sum;
    }


    public void setDateIn(int day, int month, int year)
    {
        dateIn[0] = day;
        dateIn[1] = month;
        dateIn[2] = year;
    }

    public void deleteEmpPayment(int payment, int[] date)
    {
        if (empPaymentLst != null)
        {
            int id;
            for (int i = 0; i < empPaymentLst.size(); i++)
            {
                id = empPaymentLst.get(i).getId();
                if (id == empPaymentLst.get(i).getId())
                {
                    empPaymentLst.remove(i);
                    break;
                }
            }
        }
    }

    public Long empPutDataBase()
    {
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
         long empId = dbHelper.addEmployee(this.name,this.surName,this.worksDay,this.getTotalMoney(),this.getDateInStr());
         return  empId;
    }
}
