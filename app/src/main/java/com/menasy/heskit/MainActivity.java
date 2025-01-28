package com.menasy.heskit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    TextView totalPaymentTxt;
    TextView totalTransferTxt;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        totalPaymentTxt = findViewById(R.id.totalPaymentTxtView);
        totalTransferTxt = findViewById(R.id.totalTransferTxtView);
        Singleton.getInstance().initDatabase(this);

    }
    public void handleEmp(View view)
    {
        Intent intent = new Intent(this,MainActivity2.class);
        intent.putExtra("fragment","Calisanlar");
        startActivity(intent);
    }
    public void setTotalTxt(int totalPayment, int totalTransfer)
    {
       totalTransferTxt.setText("Toplam Havale: " + totalTransfer);
       totalPaymentTxt.setText("Toplam Harçlık: " + totalPayment);
    }
    public void handleHavale(View view)
    {
        Intent intent = new Intent(this,MainActivity2.class);
        intent.putExtra("fragment","Havale");
        startActivity(intent);
    }
}