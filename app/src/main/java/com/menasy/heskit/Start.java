package com.menasy.heskit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import com.menasy.heskit.databinding.FragmentStartBinding;

public class Start extends Fragment {

    private FragmentStartBinding binding;
    private static Start instance;
    private OnBackPressedCallback backPressedCallback;

    // Instance alma metodu ekledik
    private static Start getInstance() {
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStartBinding.inflate(inflater, container, false);
        instance = this; // Instance'ı burada set ediyoruz

        setupBackPressHandler();
        setupClickListeners();
        updateAllStats();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAllStats();
    }

    private void setupClickListeners() {
        binding.empBut.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToEmployees();
            }
        });
    }

    private void updateAllStats() {
        updateTotalPayment();
        updateEmployeeCount();
        updateTotalTransfer();
    }

    private void updateTotalPayment() {
        new Thread(() -> {
            int total = Singleton.getInstance().getDataBase().getTotalPayments();
            int totalAll = Singleton.getInstance().getDataBase().getTotalTransfers()
                    + Singleton.getInstance().getDataBase().getTotalPayments();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    binding.totalPaymentTxtView.setText("Toplam Harçlık: " + total + "₺");
                    binding.totalAllMoney.setText("Toplam Para: " + totalAll + "₺");
                });
            }
        }).start();
    }

    private void updateEmployeeCount() {
        new Thread(() -> {
            int count = Singleton.getInstance().getDataBase().getEmployeeCount();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    binding.totalEmpStartTxt.setText("Toplam Çalışan: " + count);
                });
            }
        }).start();
    }

    private void updateTotalTransfer() {
        new Thread(() -> {
            try {
                int total = Singleton.getInstance().getDataBase().getTotalTransfers();
                int totalAll = Singleton.getInstance().getDataBase().getTotalTransfers()
                        + Singleton.getInstance().getDataBase().getTotalPayments();
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        if (binding != null) {
                            binding.totalTransferTxtView.setText("Toplam Havale: " + total + "₺");
                            binding.totalAllMoney.setText("Toplam Para: " + totalAll + "₺");
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("StartFragment", "Transfer güncelleme hatası", e);
            }
        }).start();
    }

    public static void refreshTransferTotal() {
        if (instance != null) {
            instance.updateTotalTransfer();
        }
    }
    public static void refreshPaymentTotal() {
        if (instance != null && instance.getActivity() != null) {
            instance.getActivity().runOnUiThread(instance::updateTotalPayment);
        }
    }

    public static void refreshEmployeeCount() {
        if (instance != null && instance.getActivity() != null) {
            instance.getActivity().runOnUiThread(instance::updateEmployeeCount);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (backPressedCallback != null) {
            backPressedCallback.remove();
        }
        binding = null;
        instance = null;
    }

    private void setupBackPressHandler() {
        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Start fragment'ındayken geri tuşunu devre dışı bırak
                if (isVisible() && getUserVisibleHint()) {
                    // Hiçbir şey yapma, fragment'te kalmaya devam et
                } else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(), // Lifecycle owner
                backPressedCallback
        );
    }
}