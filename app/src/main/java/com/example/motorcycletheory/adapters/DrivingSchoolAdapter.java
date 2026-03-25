package com.example.motorcycletheory.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.motorcycletheory.R;
import com.example.motorcycletheory.models.DrivingSchool;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class DrivingSchoolAdapter extends RecyclerView.Adapter<DrivingSchoolAdapter.ViewHolder> {

    public interface OnSchoolActionListener {
        void onCallClick(DrivingSchool school);
        void onDirectionClick(DrivingSchool school);
        void onSchoolClick(DrivingSchool school);
    }

    private List<DrivingSchool> schools = new ArrayList<>();
    private final OnSchoolActionListener listener;

    public DrivingSchoolAdapter(OnSchoolActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<DrivingSchool> newList) {
        schools.clear();
        if (newList != null) {
            schools.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_driving_school, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(schools.get(position));
    }

    @Override
    public int getItemCount() {
        return schools.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSchoolName;
        private final TextView tvAddress;
        private final TextView tvPhone;
        private final TextView tvRating;
        private final MaterialButton btnCall;
        private final MaterialButton btnDirection;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSchoolName = itemView.findViewById(R.id.tvSchoolName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnDirection = itemView.findViewById(R.id.btnDirection);
        }

        void bind(DrivingSchool school) {
            tvSchoolName.setText(school.getName());
            tvAddress.setText(school.getAddress());
            tvPhone.setText(itemView.getContext().getString(R.string.map_phone_format, school.getPhone()));
            tvRating.setText(itemView.getContext().getString(R.string.map_rating_format, school.getRating()));

            btnCall.setOnClickListener(v -> {
                if (listener != null) listener.onCallClick(school);
            });
            btnDirection.setOnClickListener(v -> {
                if (listener != null) listener.onDirectionClick(school);
            });
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSchoolClick(school);
            });
        }
    }
}
