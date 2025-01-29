package com.menasy.heskit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.menasy.heskit.databinding.FragmentStartBinding;

public class Start extends Fragment {

    private FragmentStartBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStartBinding.inflate(inflater, container, false);
        setupClickListeners();
        return binding.getRoot();
    }

    private void setupClickListeners() {
        // Çalışanlar Butonu
        binding.empBut.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToEmployees();
            }
        });

        // Havale Butonu
        binding.havaleBut.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTransfers();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Memory leak'i önlemek için
    }
}