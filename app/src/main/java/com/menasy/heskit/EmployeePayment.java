package com.menasy.heskit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EmployeePayment
{
    private static int index;
    static {
        index = 0;
    }
    private     int id;
    private     int takedMoney;
    private     int[] date;


    public void setId(int id) {
        this.id = id;
    }

    public void setPayment(int takedMoney) {
        this.takedMoney = takedMoney;
    }

    public void setDate(int[] date) {
        this.date = date;
    }

    public EmployeePayment(int takedMoney, int[] date)
    {
        this.id = index++;
        this.takedMoney = takedMoney;
        this.date = date;
    }

    public int getTakedMoney() {
        return takedMoney;
    }

    public void setTakedMoney(int takedMoney) {
        this.takedMoney = takedMoney;
    }

    public int getId() {
        return id;
    }

    public int getPayment() {
        return takedMoney;
    }

    public int[] getDate() {
        return date;
    }
    public String getPaymentAndDate() {
        if (date != null && date.length == 3) {
            // Tarihi formatla
            Calendar calendar = Calendar.getInstance();

            // Aylar zaten 0 tabanlı, burada -1 yapmaya gerek yok
            calendar.set(Calendar.DAY_OF_MONTH, date[0]);
            calendar.set(Calendar.MONTH, date[1] - 1); // Ayı düzgün şekilde 0 tabanlı yapıyoruz
            calendar.set(Calendar.YEAR, date[2]);

            // Tarih formatlama
            SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.yyyy", new Locale("tr", "TR"));
            String formattedDate = dateFormat.format(calendar.getTime());

            // Ödeme ve tarih bilgisini döndür
            return String.format("%d₺       %s", takedMoney, formattedDate);
        } else {
            return "Tarih bilgisi geçersiz!";
        }
    }

    public String getDateInStr() {
        String formattedDate = date[0] + "." + date[1] + "." + date[2]; // Gün/Ay/Yıl formatında
        return formattedDate;
    }
    public Long paymentPutDataBase(Long employeeId)
    {
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        long paymentId = dbHelper.addPayment(this.getTakedMoney(),this.getDateInStr(), employeeId);
        return  paymentId;
    }
}
