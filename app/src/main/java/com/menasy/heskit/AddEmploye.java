package com.menasy.heskit;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.menasy.heskit.databinding.FragmentAddEmployeBinding;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEmploye extends Fragment {
    private int[] dateIn;
    private FragmentAddEmployeBinding bnd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bnd = FragmentAddEmployeBinding.inflate(inflater, container, false);
        View view = bnd.getRoot();
        bnd.editTextDate.setOnClickListener(v -> showDatePickerDialog());
        bnd.empSaveBut.setOnClickListener(v -> employeeSaveBut());
        return view;
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    Calendar today = Calendar.getInstance();
                    if (selectedDate.after(today)) {
                        Toast.makeText(getContext(), "Geçersiz tarih!", Toast.LENGTH_SHORT).show();
                    } else {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("tr", "TR"));
                        String formattedDate = dateFormat.format(selectedDate.getTime());
                        setDate(selectedYear, selectedMonth, selectedDay, formattedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void setDate(int year, int month, int day, String formattedDate) {
        bnd.editTextDate.setText(formattedDate);
        dateIn = new int[]{day, month + 1, year};
    }

    public void employeeSaveBut() {
        if (bnd.editNameTxt.getText().toString().isEmpty() ||
                bnd.editSurnameTxt.getText().toString().isEmpty() ||
                bnd.editTextDate.getText().toString().isEmpty()) {

            Toast.makeText(getContext(), "Lütfen tüm alanları doldurun!", Toast.LENGTH_LONG).show();
            return;
        }

        Employee emp = new Employee(
                bnd.editNameTxt.getText().toString(),
                bnd.editSurnameTxt.getText().toString(),
                dateIn
        );

        long dbId = emp.empPutDataBase();
        emp.setDbId(dbId);
        Calisanlar.empList.add(emp);
        Calisanlar.adapter.updateList(Calisanlar.empList);
        Start.refreshEmployeeCount();
        Start.refreshPaymentTotal();
        Toast.makeText(getContext(), "Başarıyla kaydedildi", Toast.LENGTH_LONG).show();
        clearFields();
    }

    private void clearFields() {
        bnd.editNameTxt.setText("");
        bnd.editSurnameTxt.setText("");
        bnd.editTextDate.setText("");
    }
}