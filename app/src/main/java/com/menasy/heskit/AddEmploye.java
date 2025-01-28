package com.menasy.heskit;

import static com.menasy.heskit.Calisanlar.empList;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.menasy.heskit.databinding.FragmentAddEmployeBinding;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddEmploye#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddEmploye extends Fragment {
    private int[] dateIn;
    private FragmentAddEmployeBinding bnd;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AddEmploye() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddEmploye.
     */
    // TODO: Rename and change types and number of parameters
    public static AddEmploye newInstance(String param1, String param2) {
        AddEmploye fragment = new AddEmploye();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        bnd = FragmentAddEmployeBinding.inflate(getLayoutInflater());
        view = bnd.getRoot();
        bnd.editTextDate.setOnClickListener(v -> showDatePickerDialog());
        bnd.empSaveBut.setOnClickListener(v -> employeeSaveBut());
        return view;
    }
    private void showDatePickerDialog() {
        // Mevcut tarih bilgisi
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Seçilen tarihi Türkçe formatlama
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    // Bugünün tarihini al
                    Calendar today = Calendar.getInstance();

                    // Eğer seçilen tarih bugünden ilerideyse, kullanıcıya uyarı ver
                    if (selectedDate.after(today)) {
                        Toast.makeText(getContext(), "Seçilen tarih bugünden ileride olamaz!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Türkçe tarih formatı
                        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("tr", "TR"));
                        String formattedDate = dateFormat.format(selectedDate.getTime());

                        setDate(selectedYear, selectedMonth, selectedDay, formattedDate);
                    }
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void setDate(int selectedYear, int selectedMonth, int selectedDay, String formattedDate)
    {
        // Seçilen tarihi TextView'e set et
        bnd.editTextDate.setText(formattedDate);

        // Tarih bilgisini bir diziye kaydet
        dateIn = new int[3];
        dateIn[0] = selectedDay;
        dateIn[1] = selectedMonth + 1;  // Ayı 1 artırarak göster
        dateIn[2] = selectedYear;
    }
    public void employeeSaveBut()
    {
        if (bnd.editNameTxt.getText().toString().matches("")
            || bnd.editSurnameTxt.getText().toString().matches("")
            || bnd.editTextDate.getText().toString().matches(""))
        {
            Toast.makeText(this.getContext(),"Kaydedilemedi !\n Lütfen Tüm Bilgileri Doldurun !",Toast.LENGTH_LONG).show();
            return;
        }
        Employee emp = new Employee(bnd.editNameTxt.getText().toString(), bnd.editSurnameTxt.getText().toString(), dateIn);
        long dbId = emp.empPutDataBase();
        Log.d("Bak", "DbId eklendi:  " + dbId);
        emp.setDbId(dbId);
        empList.add(emp);
        Log.d("Bak", "DbId eklendi:  " + emp.getDbId());
        Log.d("Bak", "Id eklendi:  " + emp.getId());
        Calisanlar.adapter.updateList(Calisanlar.empList); // Adapteri güncelle
        Toast.makeText(this.getContext(),"Kaydedildi", Toast.LENGTH_LONG).show();
        bnd.editNameTxt.setText("");
        bnd.editSurnameTxt.setText("");
        bnd.editTextDate.setText("");
    }
}