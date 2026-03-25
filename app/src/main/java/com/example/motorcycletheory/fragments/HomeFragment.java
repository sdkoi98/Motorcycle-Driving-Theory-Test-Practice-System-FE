package com.example.motorcycletheory.fragments;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.motorcycletheory.R;
import com.example.motorcycletheory.activities.DrivingSchoolMapActivity;
import com.example.motorcycletheory.activities.ExamConfirmActivity;
import com.example.motorcycletheory.databinding.FragmentHomeBinding;
import com.example.motorcycletheory.utils.NotificationHelper;
import com.example.motorcycletheory.utils.SessionManager;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private NotificationHelper notificationHelper;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    enableReminder();
                } else {
                    if (binding != null) binding.switchReminder.setChecked(false);
                    Toast.makeText(requireContext(), "Cần cấp quyền thông báo để nhắc nhở", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationHelper = new NotificationHelper(requireContext());

        binding.btnGenerateExam.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(requireContext());
            if (!sessionManager.isLoggedIn()) {
                Toast.makeText(requireContext(), getString(R.string.home_not_logged_in), Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(requireContext(), ExamConfirmActivity.class));
        });

        binding.cardMap.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), DrivingSchoolMapActivity.class)));

        setupReminderCard();
    }

    private void setupReminderCard() {
        boolean enabled = notificationHelper.isReminderEnabled();
        binding.switchReminder.setChecked(enabled);
        updateReminderStatus();

        binding.switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return;
            if (isChecked) {
                requestNotificationPermissionAndEnable();
            } else {
                notificationHelper.cancelReminder();
                updateReminderStatus();
                Toast.makeText(requireContext(), getString(R.string.notification_disabled), Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSetTime.setOnClickListener(v -> showTimePicker());
        binding.btnSetTime.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    private void requestNotificationPermissionAndEnable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        enableReminder();
    }

    private void enableReminder() {
        int hour = notificationHelper.getReminderHour();
        int minute = notificationHelper.getReminderMinute();
        notificationHelper.scheduleReminder(hour, minute);
        updateReminderStatus();
        binding.btnSetTime.setVisibility(View.VISIBLE);
        Toast.makeText(requireContext(),
                getString(R.string.notification_enabled, hour, minute), Toast.LENGTH_SHORT).show();
    }

    private void showTimePicker() {
        int currentHour = notificationHelper.getReminderHour();
        int currentMinute = notificationHelper.getReminderMinute();

        new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            notificationHelper.scheduleReminder(hourOfDay, minute);
            updateReminderStatus();
            Toast.makeText(requireContext(),
                    getString(R.string.notification_enabled, hourOfDay, minute), Toast.LENGTH_SHORT).show();
        }, currentHour, currentMinute, true).show();
    }

    private void updateReminderStatus() {
        if (binding == null) return;
        if (notificationHelper.isReminderEnabled()) {
            int h = notificationHelper.getReminderHour();
            int m = notificationHelper.getReminderMinute();
            binding.tvReminderStatus.setText(getString(R.string.notification_enabled, h, m));
            binding.btnSetTime.setVisibility(View.VISIBLE);
        } else {
            binding.tvReminderStatus.setText("Chưa bật nhắc nhở");
            binding.btnSetTime.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
