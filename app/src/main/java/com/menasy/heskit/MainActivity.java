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
        setContentView(R.layout.activity_main); // SADECE 1 KEZ ÇAĞRILDI

        // WindowInsets için düzeltme
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragmentContainer), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Veritabanı başlatma
        Singleton.getInstance().initDatabase(this);

        // İlk fragment'ı yükle
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

    // Buton click metodları (Start fragment'tan çağrılacak)
    public void navigateToEmployees() {
        showFragment(new Calisanlar());
    }

    public void navigateToTransfers() {
        showFragment(new Havale());
    }
    public void navigateToAddEmp() {
        showFragment(new AddEmploye());
    }
    public void navigateToEmpProcces(){
        showFragment(new EmployeeProcces());
    }
}