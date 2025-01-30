package com.menasy.heskit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "employee.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_EMPLOYEES = "employees";
    public static final String TABLE_PAYMENTS = "payments";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        // Yabancı anahtarları etkinleştir
        db.execSQL("PRAGMA foreign_keys = ON;");

        // Çalışanlar tablosu
        String createEmployeeTable = "CREATE TABLE " + TABLE_EMPLOYEES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "surName TEXT, " +
                "worksDay INTEGER, " +
                "totalMoney INTEGER, " +
                "dateIn TEXT)";  // FOREIGN KEY burada gereksiz

        // Ödemeler tablosu
        String createPaymentsTable = "CREATE TABLE " + TABLE_PAYMENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "amount INTEGER, " +
                "paymentDate TEXT, " +
                "employeeId INTEGER, " +
                "FOREIGN KEY (employeeId) REFERENCES " + TABLE_EMPLOYEES + "(id))";

        db.execSQL(createEmployeeTable);
        db.execSQL(createPaymentsTable);
    }
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Yabancı anahtar kısıtlamalarını etkinleştir
        db.setForeignKeyConstraintsEnabled(true);
        // Veya: db.execSQL("PRAGMA foreign_keys = ON;");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAYMENTS);
        onCreate(db);
    }

    public long addEmployee(String name, String surName, int worksDay, int totalMoney, String dateIn) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("surName", surName);
        values.put("worksDay", worksDay);
        values.put("totalMoney", totalMoney);
        values.put("dateIn", dateIn);

        long employeeId = db.insert(TABLE_EMPLOYEES, null, values);
        db.close();
        return employeeId; // Eklenen çalışanın ID'sini döner
    }

    public long addPayment(int amount, String paymentDate, long employeeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("paymentDate", paymentDate);
        values.put("employeeId", employeeId);

        try {
            return db.insertOrThrow(TABLE_PAYMENTS, null, values);
        } catch (SQLException e) {
            Log.e("DB_ERROR", "Payment ekleme hatası: ", e);
            return -1;
        }
    }


}

