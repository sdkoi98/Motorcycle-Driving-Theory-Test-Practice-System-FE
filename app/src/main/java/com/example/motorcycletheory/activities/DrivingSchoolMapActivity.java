package com.example.motorcycletheory.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.motorcycletheory.R;
import com.example.motorcycletheory.adapters.DrivingSchoolAdapter;
import com.example.motorcycletheory.databinding.ActivityDrivingSchoolMapBinding;
import com.example.motorcycletheory.models.DrivingSchool;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class DrivingSchoolMapActivity extends AppCompatActivity
        implements OnMapReadyCallback, DrivingSchoolAdapter.OnSchoolActionListener {

    private ActivityDrivingSchoolMapBinding binding;
    private GoogleMap googleMap;
    private List<DrivingSchool> schools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDrivingSchoolMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        schools = getDrivingSchools();

        DrivingSchoolAdapter adapter = new DrivingSchoolAdapter(this);
        binding.rvSchools.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSchools.setAdapter(adapter);
        adapter.submitList(schools);

        try {
            SupportMapFragment mapFragment = (SupportMapFragment)
                    getSupportFragmentManager().findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        } catch (Exception e) {
            // Google Maps SDK not configured, list-only mode
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        for (DrivingSchool school : schools) {
            LatLng position = new LatLng(school.getLatitude(), school.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(school.getName())
                    .snippet(school.getAddress()));
            boundsBuilder.include(position);
        }

        try {
            LatLngBounds bounds = boundsBuilder.build();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            LatLng defaultLocation = new LatLng(16.0, 108.0);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 5));
        }
    }

    @Override
    public void onCallClick(DrivingSchool school) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + school.getPhone()));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.map_no_phone_app), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDirectionClick(DrivingSchool school) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + school.getLatitude() + "," + school.getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        try {
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination="
                        + school.getLatitude() + "," + school.getLongitude());
                startActivity(new Intent(Intent.ACTION_VIEW, webUri));
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.map_no_maps_app), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSchoolClick(DrivingSchool school) {
        if (googleMap != null) {
            LatLng position = new LatLng(school.getLatitude(), school.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        }
    }

    private List<DrivingSchool> getDrivingSchools() {
        List<DrivingSchool> list = new ArrayList<>();
        list.add(new DrivingSchool(1, "Trung tâm Sát hạch Lái xe Hà Nội",
                "Km 12, Đại lộ Thăng Long, Nam Từ Liêm, Hà Nội",
                "024-3765-4321", 21.0033, 105.7594, 4.5f));
        list.add(new DrivingSchool(2, "Trung tâm Sát hạch Lái xe TP.HCM (Quận 12)",
                "200 Quốc lộ 1A, Quận 12, TP. Hồ Chí Minh",
                "028-3891-2345", 10.8573, 106.6421, 4.3f));
        list.add(new DrivingSchool(3, "Trung tâm ĐTSH Lái xe Đà Nẵng",
                "Km 7, đường Trường Sa, Ngũ Hành Sơn, Đà Nẵng",
                "0236-3847-567", 16.0194, 108.2476, 4.6f));
        list.add(new DrivingSchool(4, "Trung tâm Sát hạch Lái xe Cần Thơ",
                "Quốc lộ 91B, Phong Điền, Cần Thơ",
                "0292-3862-456", 10.0341, 105.7220, 4.2f));
        list.add(new DrivingSchool(5, "Trung tâm ĐTSH Lái xe Hải Phòng",
                "Quốc lộ 10, An Dương, Hải Phòng",
                "0225-3745-678", 20.8648, 106.6561, 4.4f));
        list.add(new DrivingSchool(6, "Trung tâm Sát hạch Lái xe Bình Dương",
                "Đại lộ Bình Dương, TX Dĩ An, Bình Dương",
                "0274-3823-789", 10.9032, 106.6625, 4.1f));
        list.add(new DrivingSchool(7, "Trung tâm ĐTSH Lái xe Đồng Nai",
                "Quốc lộ 1A, Trảng Bom, Đồng Nai",
                "0251-3865-321", 10.9524, 106.9564, 4.3f));
        list.add(new DrivingSchool(8, "Trung tâm Sát hạch Lái xe Huế",
                "Đường Tự Đức, TP. Huế, Thừa Thiên Huế",
                "0234-3826-543", 16.4637, 107.5909, 4.5f));
        return list;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
