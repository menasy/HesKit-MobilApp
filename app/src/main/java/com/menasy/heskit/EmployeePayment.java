package com.menasy.heskit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EmployeePayment
{
    private static int index;
    static {
        index = 1;
    }
    private int id;
    private long   dbId;
    private int takedMoney;
    private int[] date;

    public EmployeePayment(int takedMoney, int[] date) {
        this.takedMoney = takedMoney;
        this.date = date;
        this.id = index++;
    }

    // Getter ve Setter metodları
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTakedMoney() { return takedMoney; }
    public int[] getDate() { return date; }

    public String getPaymentAndDate() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(date[2], date[1] - 1, date[0]); // Yıl, Ay (0 tabanlı), Gün
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", new Locale("tr", "TR"));
            return String.format("%d₺ - %s", takedMoney, sdf.format(calendar.getTime()));
        } catch (Exception e) {
            return "Geçersiz tarih!";
        }
    }

    public String getDateInStr() {
        return String.format("%d/%d/%d", date[0], date[1], date[2]);
    }

    public Long paymentPutDataBase(Long employeeId) {
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        return dbHelper.addPayment(this.takedMoney, this.getDateInStr(), employeeId);
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }
}