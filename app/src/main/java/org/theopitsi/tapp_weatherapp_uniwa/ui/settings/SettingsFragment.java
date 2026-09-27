package org.theopitsi.tapp_weatherapp_uniwa.ui.settings;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import org.theopitsi.tapp_weatherapp_uniwa.R;
import org.theopitsi.tapp_weatherapp_uniwa.databinding.FragmentSettingsBinding;
import org.theopitsi.tapp_weatherapp_uniwa.service.NotificationService;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    Button btnAlertMaker;
    CheckBox btnFahrenheit;
    CheckBox btnAvrg;

    public SettingsFragment() {
        super(R.layout.fragment_settings);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnAlertMaker = binding.btnAlert;
        btnAlertMaker.setOnClickListener(v -> {
            Intent servInt = new Intent(requireContext(), NotificationService.class);
            requireActivity().startService(servInt);
            Toast.makeText(requireContext(), "Weather alert in 5 seconds!", Toast.LENGTH_LONG).show();
        });

        SharedPreferences prefs = requireContext().getSharedPreferences("weather_prefs", Context.MODE_PRIVATE);


        btnFahrenheit = binding.checkBoxFar;
        btnFahrenheit.setChecked(prefs.getInt("temp_units", 0) != 0);
        btnFahrenheit.setOnCheckedChangeListener((v,a) -> {
            prefs.edit().putInt("temp_units",a?1:0).apply();
            //why did i do this as int? because i started it like this and i was bored to change it
            //also because i wanted to do kelvin but who even uses kelvin
        });

        btnAvrg = binding.checkBoxAvg;
        btnAvrg.setChecked(prefs.getBoolean("use_avg_week", false));
        btnAvrg.setOnCheckedChangeListener((v,a) -> {
            prefs.edit().putBoolean("use_avg_week",a).apply();
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}