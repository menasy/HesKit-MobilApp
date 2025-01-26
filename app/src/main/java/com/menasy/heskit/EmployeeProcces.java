package com.menasy.heskit;

import static com.menasy.heskit.Calisanlar.empList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.menasy.heskit.databinding.FragmentAddEmployeBinding;
import com.menasy.heskit.databinding.FragmentEmployeeProccesBinding;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EmployeeProcces#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EmployeeProcces extends Fragment {

    CalisanAdapter getAdapter;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Employee getEmp;
    private FragmentEmployeeProccesBinding bnd;
    static EmployeeProccesAdapter empProccAdapter = null;

    public EmployeeProcces() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EmployeeProcces.
     */
    // TODO: Rename and change types and number of parameters
    public static EmployeeProcces newInstance(String param1, String param2) {
        EmployeeProcces fragment = new EmployeeProcces();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        bnd = FragmentEmployeeProccesBinding.inflate(inflater, container, false); // Kullanıcıdan gelen inflater'ı kullan
        view = bnd.getRoot();

        if (getArguments() != null) {
            // Argümanlardan çalışan ve adaptör bilgilerini al
            getEmp = (Employee) getArguments().getSerializable("Employee");
            getAdapter = (CalisanAdapter) getArguments().getSerializable("CalisanAdapter");

            if (getEmp != null) {
                // Çalışan bilgilerini UI'a yansıt
                bnd.empProcTitleTxt.setText(getEmp.getNameAndSurname());
                getEmp.displayDateIn(bnd.dateInTxt);
                bnd.countDayTxt.setText("Çalıştığı Gün Sayısı: " + getEmp.getWorksDay());
                bnd.takedMoneyTxtView.setText("Aldığı Toplam Harçlık: " + getEmp.getTotalMoney());

                // RecyclerView'i bir kez kur
                if (getEmp.getEmpPaymentLst() != null && !getEmp.getEmpPaymentLst().isEmpty()) {
                    empProccAdapter = new EmployeeProccesAdapter(getEmp.getEmpPaymentLst());
                    bnd.paymentRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                    bnd.paymentRecycler.setAdapter(empProccAdapter);
                } else {
                    empProccAdapter = new EmployeeProccesAdapter(new ArrayList<>());
                    bnd.paymentRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                    bnd.paymentRecycler.setAdapter(empProccAdapter);
                }

                // Buton tıklama olaylarını tanımla
                bnd.addMoneyBut.setOnClickListener(v -> addMoneyButton());
                bnd.deleteEmpBut.setOnClickListener(v -> deleteEmpButton(getEmp));
            } else {
                // Çalışan bilgisi yoksa kullanıcıyı bilgilendir
                Toast.makeText(getContext(), "Çalışan bilgisi alınamadı.", Toast.LENGTH_SHORT).show();
            }
        }

        return view; // Fragment'ın görünümünü döndür
    }

    private void deleteEmp (Employee employee)
    {
        if (empList != null)
        {
            int id;
            for (int i = 0; i < empList.size(); i++)
            {
                id = empList.get(i).getId();
                if (id == employee.getId())
                {
                    empList.remove(i);
                    break;
                }
            }
        }
    }
    public void deleteEmpButton(Employee employee)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(employee.getNameAndSurname() + " Silinsin Mi ?");
        alert.setPositiveButton("Evet", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                deleteEmp(employee);
                if (Calisanlar.adapter != null)
                    Calisanlar.adapter.updateList(Calisanlar.empList);
                Toast.makeText(getContext(),"Çalışan Silindi !", Toast.LENGTH_SHORT).show();
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
        alert.setNegativeButton("Hayır", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "Çalışan Silinmedi !", Toast.LENGTH_SHORT).show();
                return;
            }
        });
        alert.show();
    }

    public void addMoneyButton() {
        String getMoneyStr = bnd.moneyEditTxt.getText().toString();

        if (!getMoneyStr.matches("") && !getMoneyStr.matches("0")) {
            int getMoney = Integer.parseInt(getMoneyStr);

            // Çalışanın ödeme listesine yeni ödeme ekle
            if (getEmp.getEmpPaymentLst() == null) {
                getEmp.setEmpPaymentLst(new ArrayList<>()); // Eğer liste null ise yeni bir liste oluştur
            }
            getEmp.addPayment(new EmployeePayment(getMoney, DateUtils.getCurrentDateArray()));

            // Adapteri güncelle veya gerekiyorsa yeniden oluştur
            if (empProccAdapter == null) {
                empProccAdapter = new EmployeeProccesAdapter(getEmp.getEmpPaymentLst());
                bnd.paymentRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                bnd.paymentRecycler.setAdapter(empProccAdapter);
            } else {
                empProccAdapter.updateList(getEmp.getEmpPaymentLst()); // Yeni veriyi adaptöre ilet
            }

            // Toplam harçlık bilgisini güncelle
            bnd.takedMoneyTxtView.setText("Aldığı Toplam Harçlık: " + getEmp.getTotalMoney());

            // Çalışan listesini güncelle
            updateEmployee(getEmp); // Çalışanı üst listeye kaydet
            if (Calisanlar.adapter != null) {
                Calisanlar.adapter.updateList(Calisanlar.empList); // Çalışanlar adaptörünü güncelle
            }

            // Başarı mesajı göster
            Toast.makeText(this.getContext(), "Harçlık Eklendi", Toast.LENGTH_SHORT).show();
        } else {
            // Boş giriş hatası
            Toast.makeText(this.getContext(), "Harçlık Boş Olamaz!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void updateEmployee(Employee employee)
    {
        if (empList != null)
        {
            int id;
            for (int i = 0; i < empList.size(); i++)
            {
                id = empList.get(i).getId();
                if (id == employee.getId())
                    empList.set(i, employee);
            }
        }
    }
}