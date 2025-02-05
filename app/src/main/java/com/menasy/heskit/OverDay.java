package com.menasy.heskit;

public class OverDay
{
    private String date;
    private int daysAmount;
    private int id;

    public OverDay(String date, int daysAmount) {
        this.date = date;
        this.daysAmount = daysAmount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDaysAmount() {
        return daysAmount;
    }

    public void setDaysAmount(int daysAmount) {
        this.daysAmount = daysAmount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
