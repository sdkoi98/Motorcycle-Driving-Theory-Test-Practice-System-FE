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
import com.example.motorcycletheory.fragments.QuestionBankFragment;
import com.example.motorcycletheory.fragments.StudyCartFragment;
import com.example.motorcycletheory.utils.SessionManager;
import com.example.motorcycletheory.utils.StudyCartManager;
import com.google.android.material.badge.BadgeDrawable;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private BadgeDrawable cartBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = new SessionManager(this);
        boolean isAdmin = "Admin".equalsIgnoreCase(sessionManager.getRole());

        if (isAdmin) {
            binding.bottomNav.getMenu().clear();
            binding.bottomNav.getMenu()
                    .add(0, R.id.nav_admin, 0, R.string.admin)
                    .setIcon(android.R.drawable.ic_menu_manage);
            binding.bottomNav.getMenu()
                    .add(0, R.id.nav_profile, 1, R.string.profile)
                    .setIcon(android.R.drawable.ic_menu_myplaces);

            if (savedInstanceState == null) {
                switchFragment(new AdminFragment());
            }
        } else {
            setupCartBadge();

            if (savedInstanceState == null) {
                switchFragment(new HomeFragment());
            }
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchFragment(new HomeFragment());
                return true;
            }
            if (id == R.id.nav_question_bank) {
                switchFragment(new QuestionBankFragment());
                return true;
            }
            if (id == R.id.nav_cart) {
                switchFragment(new StudyCartFragment());
                return true;
            }
            if (id == R.id.nav_history) {
                switchFragment(new HistoryFragment());
                return true;
            }
            if (id == R.id.nav_admin) {
                switchFragment(new AdminFragment());
                return true;
            }
            if (id == R.id.nav_profile) {
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

    private void setupCartBadge() {
        cartBadge = binding.bottomNav.getOrCreateBadge(R.id.nav_cart);
        cartBadge.setBackgroundColor(getColor(R.color.danger_red));
        cartBadge.setBadgeTextColor(getColor(R.color.white));
        refreshCartBadge();
    }

    public void refreshCartBadge() {
        if (cartBadge == null) return;
        StudyCartManager cartManager = new StudyCartManager(this);
        int count = cartManager.getCartCount();
        if (count > 0) {
            cartBadge.setNumber(count);
            cartBadge.setVisible(true);
        } else {
            cartBadge.clearNumber();
            cartBadge.setVisible(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCartBadge();
    }
}
