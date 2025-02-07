package com.menasy.heskit;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import com.menasy.heskit.databinding.FragmentStartBinding;

public class Start extends Fragment {

    private FragmentStartBinding binding;
    private static Start instance;
    private OnBackPressedCallback backPressedCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStartBinding.inflate(inflater, container, false);
        instance = this;

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

    private void setFormattedText(TextView textView, String label, String value) {
        SpannableString spannable = new SpannableString(label + "\n" + value);
        int start = label.length() + 1; // "\n" sonrası başlıyor
        spannable.setSpan(new ForegroundColorSpan(Color.GREEN), start, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannable);
    }

    private void updateTotalPayment() {
        new Thread(() -> {
            long total = Singleton.getInstance().getDataBase().getTotalPayments();
            long totalAll = Singleton.getInstance().getDataBase().getTotalTransfers()
                    + Singleton.getInstance().getDataBase().getTotalPayments();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    setFormattedText(binding.totalPaymentTxtView, "Toplam Harçlık", total + "₺");
                    setFormattedText(binding.totalAllMoney, "Toplam Para", totalAll + "₺");
                });
            }
        }).start();
    }

    private void updateEmployeeCount() {
        new Thread(() -> {
            int count = Singleton.getInstance().getDataBase().getEmployeeCount();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    setFormattedText(binding.totalEmpStartTxt, "Toplam Çalışan", count + "");
                });
            }
        }).start();
    }

    private void updateTotalTransfer() {
        new Thread(() -> {
            try {
                long total = Singleton.getInstance().getDataBase().getTotalTransfers();
                long totalAll = Singleton.getInstance().getDataBase().getTotalTransfers()
                        + Singleton.getInstance().getDataBase().getTotalPayments();
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        if (binding != null) {
                            setFormattedText(binding.totalTransferTxtView, "Toplam Havale", total + "₺");
                            setFormattedText(binding.totalAllMoney, "Toplam Para", totalAll + "₺");

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
                getViewLifecycleOwner(),
                backPressedCallback
        );
    }
}