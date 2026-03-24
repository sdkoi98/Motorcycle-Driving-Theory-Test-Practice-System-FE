package com.example.motorcycletheory.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.motorcycletheory.R;
import com.example.motorcycletheory.databinding.ActivityMainBinding;
import com.example.motorcycletheory.fragments.AdminFragment;
import com.example.motorcycletheory.fragments.HistoryFragment;
import com.example.motorcycletheory.fragments.HomeFragment;
import com.example.motorcycletheory.fragments.ProfileFragment;
import com.example.motorcycletheory.utils.SessionManager;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = new SessionManager(this);
        boolean isAdmin = "Admin".equalsIgnoreCase(sessionManager.getRole());
        
        if (isAdmin) {
            // Admin: Ẩn tab "Thi thử" và "Lịch sử", chỉ giữ "Admin" và "Tài khoản"
            binding.bottomNav.getMenu().removeItem(R.id.nav_home);
            binding.bottomNav.getMenu().removeItem(R.id.nav_history);
            
            // Mặc định hiển thị AdminFragment cho Admin
            if (savedInstanceState == null) {
                switchFragment(new AdminFragment());
            }
        } else {
            // User thường: Ẩn tab "Admin"
            binding.bottomNav.getMenu().removeItem(R.id.nav_admin);
            
            // Mặc định hiển thị HomeFragment cho User
            if (savedInstanceState == null) {
                switchFragment(new HomeFragment());
            }
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                switchFragment(new HomeFragment());
                return true;
            }
            if (item.getItemId() == R.id.nav_history) {
                switchFragment(new HistoryFragment());
                return true;
            }
            if (item.getItemId() == R.id.nav_admin) {
                switchFragment(new AdminFragment());
                return true;
            }
            if (item.getItemId() == R.id.nav_profile) {
                switchFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
