package com.menasy.heskit;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Calendar;

public class MainActivity2 extends AppCompatActivity {

    TextView dateText;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String fragName = getIntent().getStringExtra("fragment");
        if (fragName != null)
        {
            if (fragName.equals("Calisanlar"))
                showFragment(new Calisanlar());
            else if (fragName.equals("Havale"))
                showFragment(new Havale());
            else if (fragName.equals("EmpProcces"))
            {
                Employee emp = (Employee) getIntent().getSerializableExtra("Employee");

                // Fragment oluştur ve veriyi aktar
                EmployeeProcces fragmentEmpProcces = new EmployeeProcces();
                if (emp != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Employee", emp);
                    fragmentEmpProcces.setArguments(bundle);
                }
                showFragment(fragmentEmpProcces);
            }
        }

    }

    private void showFragment(Fragment fragment)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer,fragment).commit();
    }
    public void addEmployeFrag(View asd)
    {
        showFragment(new AddEmploye());
    }

}