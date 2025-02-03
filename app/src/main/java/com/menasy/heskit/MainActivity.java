package com.menasy.heskit;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragmentContainer), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Singleton.getInstance().initDatabase(this);

        if (savedInstanceState == null) {
            showFragment(new Start());
        }
    }

    public void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void navigateToEmployees() {
        showFragment(new Calisanlar());
    }

    public void navigateToTransfers(Employee employee) {
        Havale.setSelectedEmployee(employee);
        showFragment(new Havale());
    }

    public void navigateToAddEmp() {
        showFragment(new AddEmploye());
    }

    public void navigateToEmpProcces(Employee employee) {
        EmployeeProcces.setSelectedEmp(employee);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new EmployeeProcces(), "employee_procces") // Tag ekleyin
                .addToBackStack(null)
                .commit();
    }

    public void navigateToAddPayment(Employee employee) {
        AddPayment.setSelectedEmployee(employee);
        showFragment(new AddPayment());
    }

}