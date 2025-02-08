package com.menasy.heskit;

import android.app.AlertDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Collections;

import com.menasy.heskit.databinding.FragmentOverDayProccesBinding;

import java.util.ArrayList;

public class OverDayProcces extends Fragment {

    private FragmentOverDayProccesBinding bnd;
    private OverDayAdapter overDayAdapter;
    private Employee employee;

    public static OverDayProcces newInstance(Employee employee) {
        OverDayProcces fragment = new OverDayProcces();
        Bundle args = new Bundle();
        args.putSerializable("employee", employee);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bnd = FragmentOverDayProccesBinding.inflate(inflater, container, false);
        return bnd.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments() != null) {
            employee = (Employee) getArguments().getSerializable("employee");
            setupUI();
            setupRecyclerView();
            loadOverDays();
            setupButtons();
        }
    }
    private void setupUI() {
        bnd.overDayTitleTextView.setText(employee.getNameAndSurname());
    }
    private void setupRecyclerView() {
        overDayAdapter = new OverDayAdapter(new ArrayList<>(), this::showDeleteDialog);
        bnd.overDayRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bnd.overDayRecyclerView.setAdapter(overDayAdapter);
    }

    private void loadOverDays() {
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        ArrayList<OverDay> overDays = dbHelper.getOverDaysForEmployee(employee.getDbId());
        Collections.reverse(overDays);
        employee.setEmpOverDayLst(overDays);
        overDayAdapter.updateList(overDays);
    }

    private void setupButtons() {
        bnd.cleanAllOverDay.setOnClickListener(v -> showCleanAllConfirmation());
        bnd.addOver.setOnClickListener(v -> showAddOverDayDialog());
    }
    private void showDeleteDialog(OverDay overDay, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Mesai Sil")
                .setMessage(overDay.getDaysAmount() + " gün mesai silinsin mi?")
                .setPositiveButton("Evet", (d, w) -> deleteOverDay(overDay, position))
                .setNegativeButton("İptal", null)
                .show();
    }

    private void deleteOverDay(OverDay overDay, int position) {
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        if(dbHelper.deleteOverDay(overDay.getId()) > 0) {
            employee.setTotalOverDay(employee.getTotalOverDay() - overDay.getDaysAmount());

            overDayAdapter.updateList(dbHelper.getOverDaysForEmployee(employee.getDbId()));
            Toast.makeText(requireContext(), "Mesai silindi", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCleanAllConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Tüm Mesaileri Sil")
                .setMessage("Tüm mesai kayıtları kalıcı olarak silinsin mi?")
                .setPositiveButton("Evet", (d, w) -> cleanAllOverDays())
                .setNegativeButton("İptal", null)
                .show();
    }

    private void cleanAllOverDays() {
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            employee.setTotalOverDay(0);

            db.delete(DBHelper.TABLE_OVER_DAYS, "employeeId=?", new String[]{String.valueOf(employee.getDbId())});
        } finally {
            db.close();
        }
        overDayAdapter.updateList(new ArrayList<>());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bnd = null;
    }


    private void showAddOverDayDialog() {

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_number_input, null);

        // Görünüm elementlerini bağladık
        EditText input = dialogView.findViewById(R.id.input_field);
        Button btnPositive = dialogView.findViewById(R.id.btn_positive);
        Button btnNegative = dialogView.findViewById(R.id.btn_negative);

        // Dialog'u oluştur
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Buton click listener'ları
        btnPositive.setOnClickListener(v -> {
            String enteredText = input.getText().toString().trim();
            if(enteredText.isEmpty()) {
                input.setError("Lütfen geçerli bir değer girin");
                return;
            }

            try {
                int hours = Integer.parseInt(enteredText);
                addOverDay(hours);
                dialog.dismiss();
            } catch (NumberFormatException e) {
                input.setError("Geçersiz sayı formatı");
            }
        });

        btnNegative.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // Klavye ayarları
        input.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        }, 100);
    }


    private void addOverDay(int hours) {
        DBHelper dbHelper = Singleton.getInstance().getDataBase();
        String currentDate = DateUtils.getCurrentDate();

        long insertedId = dbHelper.addOverDay(currentDate, hours, employee.getDbId());

        if(insertedId != -1) {
            employee.setTotalOverDay(employee.getTotalOverDay() + hours);

            OverDay newOverDay = new OverDay(currentDate, hours);
            newOverDay.setId((int) insertedId);
            overDayAdapter.addOverDay(newOverDay);
            bnd.overDayRecyclerView.smoothScrollToPosition(0);
        }
    }
}