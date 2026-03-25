package com.example.motorcycletheory.models;

public class DrivingSchool {
    private final int id;
    private final String name;
    private final String address;
    private final String phone;
    private final double latitude;
    private final double longitude;
    private final float rating;

    public DrivingSchool(int id, String name, String address, String phone,
                         double latitude, double longitude, float rating) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public float getRating() { return rating; }
}
