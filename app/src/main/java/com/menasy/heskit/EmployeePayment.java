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
    private String paymentType;
    private int[] date;

    public EmployeePayment(int takedMoney, String paymentType, int[] date) {
        this.takedMoney = takedMoney;
        this.date = date;
        this.id = index++;
        this.paymentType = paymentType;
    }

    // Getter ve Setter metodları
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTakedMoney() { return takedMoney; }
    public int[] getDate() { return date; }

    public String getPaymentInfo() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(date[2], date[1] - 1, date[0]); // Yıl, Ay (0 tabanlı), Gün
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", new Locale("tr", "TR"));
            String checkPaymentType;
            if (paymentType.matches(""))
                checkPaymentType = "";
            else
                checkPaymentType = "( " + paymentType + " )";
            return String.format("%d₺  %s -> %s", takedMoney, checkPaymentType, sdf.format(calendar.getTime()));
        } catch (Exception e) {
            return "Geçersiz tarih!";
        }
    }

    public String getDateInStr() {
        return String.format("%d/%d/%d", date[0], date[1], date[2]);
    }

    public Long paymentPutDataBase(Long employeeId) {
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        return dbHelper.addPayment(this.takedMoney, this.getPaymentType(), this.getDateInStr(), employeeId);
    }

    public long getDbId() {
        return dbId;
    }

    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
}