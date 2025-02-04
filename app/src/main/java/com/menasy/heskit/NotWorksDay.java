package com.menasy.heskit;

public class NotWorksDay {
    private String date;
    private String reason;
    private int days;
    private int id;

    public NotWorksDay(int days, String date, String reason) {
        this.days = days;
        this.date = date;
        this.reason = reason;
    }

    public int getDays() { return days; }
    public String getDate() { return date; }
    public String getReason() { return reason != null ? reason : ""; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}