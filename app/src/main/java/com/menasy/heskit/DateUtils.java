package com.menasy.heskit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateUtils {

    public static String getCurrentDate()
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.yyyy", new Locale("tr", "TR"));
        return dateFormat.format(calendar.getTime());
    }
    public static int[] getCurrentDateArray() {
        Calendar calendar = Calendar.getInstance();

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1; // Ay 0'dan başlıyor, +1 ekledim
        int year = calendar.get(Calendar.YEAR);

        // Güncel tarihi array olarak döndür
        return new int[]{day, month, year};
    }

}

