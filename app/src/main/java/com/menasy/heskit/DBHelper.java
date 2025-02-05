package com.menasy.heskit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "employee.db";
    public static final int DATABASE_VERSION = 4;

    public static final String TABLE_EMPLOYEES = "employees";
    public static final String TABLE_PAYMENTS = "payments";
    public static final String TABLE_TRANSFERS = "transfers";
    public static final String TABLE_OVER_DAYS = "over_days";

    public static final String TABLE_NOT_WORKS_DAYS = "not_works_days";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");


        String createEmployeeTable = "CREATE TABLE " + TABLE_EMPLOYEES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "surName TEXT, " +
                "worksDay INTEGER, " +
                "totalMoney INTEGER, " +
                "totalTransfer INTEGER DEFAULT 0, " +
                "totalNotWorksDay INTEGER DEFAULT 0, " +
                "dateIn TEXT)";

        String createPaymentsTable = "CREATE TABLE " + TABLE_PAYMENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "amount INTEGER, " +
                "paymentType TEXT,"+
                "paymentDate TEXT, " +
                "employeeId INTEGER, " +
                "FOREIGN KEY (employeeId) REFERENCES " + TABLE_EMPLOYEES + "(id))";

        String createTransfersTable = "CREATE TABLE " + TABLE_TRANSFERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "amount INTEGER, " +
                "transferDate TEXT, " +
                "sentToPerson TEXT, " +
                "employeeId INTEGER, " +
                "FOREIGN KEY (employeeId) REFERENCES " + TABLE_EMPLOYEES + "(id) ON DELETE CASCADE)";

        String createNotWorksDaysTable = "CREATE TABLE " + TABLE_NOT_WORKS_DAYS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "days INTEGER, " +
                "date TEXT, " +
                "reason TEXT, " +
                "employeeId INTEGER, " +
                "FOREIGN KEY (employeeId) REFERENCES " + TABLE_EMPLOYEES + "(id) ON DELETE CASCADE)";

        String createOverDaysTable = "CREATE TABLE " + TABLE_OVER_DAYS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, " +
                "daysAmount INTEGER, " +
                "employeeId INTEGER, " +
                "FOREIGN KEY (employeeId) REFERENCES " + TABLE_EMPLOYEES + "(id) ON DELETE CASCADE)";

        db.execSQL(createOverDaysTable);

        db.execSQL(createEmployeeTable);
        db.execSQL(createPaymentsTable);
        db.execSQL(createTransfersTable);
        db.execSQL(createNotWorksDaysTable);

    }
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
        db.execSQL("PRAGMA foreign_keys=ON;");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAYMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSFERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OVER_DAYS);
        onCreate(db);
    }

    public long addEmployee(String name, String surName, int worksDay, long totalMoney, String dateIn) {
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

    public long addPayment(long amount, String paymentType, String paymentDate, long employeeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("paymentType", paymentType);
        values.put("paymentDate", paymentDate);
        values.put("employeeId", employeeId);

        try {
            return db.insertOrThrow(TABLE_PAYMENTS, null, values);
        } catch (SQLException e) {
            Log.e("DB_ERROR", "Payment ekleme hatası: ", e);
            return -1;
        }
    }
    public Employee getEmployeeById(long employeeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_EMPLOYEES,
                new String[]{"id", "name", "surName", "worksDay", "totalMoney", "totalTransfer", "totalNotWorksDay", "dateIn"},
                "id=?",
                new String[]{String.valueOf(employeeId)},
                null, null, null
        );

        if(cursor != null && cursor.moveToFirst()) {
            Employee employee = new Employee();
            employee.setDbId(cursor.getLong(0));
            employee.setName(cursor.getString(1));
            employee.setSurName(cursor.getString(2));
            employee.setWorksDay(cursor.getInt(3));
            employee.setTotalMoney(cursor.getInt(4));
            employee.setTotalTransfer(cursor.getInt(5));
            employee.setTotalNotWorksDay(cursor.getInt(6)); // Yeni eklenen sütun

            String dateString = cursor.getString(7);
            employee.setDateIn(DateUtils.parseDateArray(dateString));

            cursor.close();
            return employee;
        }
        return null;
    }
    public ArrayList<EmployeePayment> getPaymentsForEmployee(long employeeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<EmployeePayment> payments = new ArrayList<>();

        Cursor cursor = db.query(
                TABLE_PAYMENTS,
                new String[]{"id", "amount", "paymentType", "paymentDate"},
                "employeeId=?",
                new String[]{String.valueOf(employeeId)},
                null, null,
                "id DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                long amount = cursor.getLong(1);
                String paymentType = cursor.getString(2);
                String paymentDate = cursor.getString(3);

                EmployeePayment payment = new EmployeePayment(amount, paymentType, DateUtils.parseDateArray(paymentDate));
                payment.setId(id);
                payments.add(payment);
            }
            cursor.close();
        }
        return payments;
    }

    public long getTotalPayments() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(totalMoney) FROM " + TABLE_EMPLOYEES, null);
        long total = 0;
        if(cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }
    public int getEmployeeCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_EMPLOYEES, null);
        int count = 0;
        if(cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public long addTransfer(long amount, String transferDate, String sentToPerson) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("transferDate", transferDate);
        values.put("sentToPerson", sentToPerson);
        return db.insert(TABLE_TRANSFERS, null, values);
    }

    public ArrayList<Transfer> getAllTransfers() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Transfer> transfers = new ArrayList<>();

        // Sorgu: "id", "amount", "transferDate", "sentToPerson" sütunları çekiliyor.
        Cursor cursor = db.query(DBHelper.TABLE_TRANSFERS,
                new String[]{"id", "amount", "transferDate", "sentToPerson"},
                null, null, null, null, "id DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Her bir sütun için indeks alınır.
                int idIndex = cursor.getColumnIndex("id");
                int amountIndex = cursor.getColumnIndex("amount");
                int transferDateIndex = cursor.getColumnIndex("transferDate");
                int sentToPersonIndex = cursor.getColumnIndex("sentToPerson");

                // Eğer herhangi bir sütun bulunamazsa (indeks -1 ise) o satırı atla
                if (idIndex == -1 || amountIndex == -1 || transferDateIndex == -1 || sentToPersonIndex == -1) {
                    Log.e("DBHelper", "Eksik sütun tespit edildi. Satır atlanıyor.");
                    continue;
                }

                // Sütunlardan veriler alınır
                int id = cursor.getInt(idIndex);
                long amount = cursor.getLong(amountIndex);
                String transferDate = cursor.getString(transferDateIndex);
                String sentToPerson = cursor.getString(sentToPersonIndex);

                // Transfer nesnesi oluşturulur
                Transfer transfer = new Transfer(amount, transferDate, sentToPerson);
                transfer.setId(id);
                transfers.add(transfer);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return transfers;
    }

    public long getTotalTransfers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM " + TABLE_TRANSFERS, null);
        long total = cursor.moveToFirst() ? cursor.getLong(0) : 0;
        cursor.close();
        return total;
    }

    public void deleteAllTransfers() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSFERS, null, null);
    }
    public int deleteTransfer(int transferId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TRANSFERS, "id=?", new String[]{String.valueOf(transferId)});
    }
    public ArrayList<Transfer> getTransfersForEmployee(long employeeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Transfer> transfers = new ArrayList<>();

        Cursor cursor = db.query(
                TABLE_TRANSFERS,
                new String[]{"id", "amount", "transferDate", "sentToPerson"},
                "employeeId=?",
                new String[]{String.valueOf(employeeId)},
                null,
                null,
                "id DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex("id");
                int amountIndex = cursor.getColumnIndex("amount");
                int transferDateIndex = cursor.getColumnIndex("transferDate");
                int sentToPersonIndex = cursor.getColumnIndex("sentToPerson");

                if (idIndex == -1 || amountIndex == -1 ||
                        transferDateIndex == -1 || sentToPersonIndex == -1) {
                    Log.e("DBHelper", "Eksik sütun tespit edildi. Satır atlanıyor.");
                    continue;
                }

                int id = cursor.getInt(idIndex);
                long amount = cursor.getLong(amountIndex);
                String transferDate = cursor.getString(transferDateIndex);
                String sentToPerson = cursor.getString(sentToPersonIndex);

                Transfer transfer = new Transfer(amount, transferDate, sentToPerson);
                transfer.setId(id);
                transfers.add(transfer);

            } while (cursor.moveToNext());

            cursor.close();
        }

        return transfers;
    }

    public long addTransfer(long amount, String transferDate, String sentToPerson, long employeeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("transferDate", transferDate);
        values.put("sentToPerson", sentToPerson);
        values.put("employeeId", employeeId);

        return db.insert(TABLE_TRANSFERS, null, values);
    }

    public long addNotWorksDay(int days, String date, String reason, long employeeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("days", days);
        values.put("date", date);
        values.put("reason", reason);
        values.put("employeeId", employeeId);
        return db.insert(TABLE_NOT_WORKS_DAYS, null, values);
    }


    public ArrayList<NotWorksDay> getNotWorksDaysForEmployee(long employeeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<NotWorksDay> days = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    TABLE_NOT_WORKS_DAYS,
                    new String[]{"id", "days", "date", "reason"},
                    "employeeId=?",
                    new String[]{String.valueOf(employeeId)},
                    null, null,
                    "id DESC"
            );

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                int daysCount = cursor.getInt(1);
                String date = cursor.getString(2);
                String reason = cursor.getString(3);

                NotWorksDay notWorksDay = new NotWorksDay(daysCount, date, reason);
                notWorksDay.setId(id);
                days.add(notWorksDay);
            }
        } finally {
            if(cursor != null) cursor.close();
        }
        return days;
    }

    public int deleteNotWorksDay(int dayId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NOT_WORKS_DAYS, "id=?", new String[]{String.valueOf(dayId)});
    }

    public long addOverDay(String date, int days, long employeeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("daysAmount", days);
        values.put("employeeId", employeeId);

        long result = db.insert(TABLE_OVER_DAYS, null, values);
        db.close();
        return result;
    }
    public ArrayList<OverDay> getOverDaysForEmployee(long employeeId) {
        ArrayList<OverDay> overDays = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_OVER_DAYS,
                new String[]{"id", "date", "daysAmount"},
                "employeeId=?",
                new String[]{String.valueOf(employeeId)},
                null, null, "date DESC"); // Tarihe göre ters sıralama

        if(cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String date = cursor.getString(1);
                int days = cursor.getInt(2);

                OverDay overDay = new OverDay(date, days);
                overDay.setId(id);
                overDays.add(overDay);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return overDays;
    }
    public int deleteOverDay(int overDayId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_OVER_DAYS, "id=?", new String[]{String.valueOf(overDayId)});
    }

}

