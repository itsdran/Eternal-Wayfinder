package com.example.eternalwayfinder;

import com.google.android.gms.maps.model.LatLng;

public class Step {
    private double latitude;
    private double longitude;
    private String instruction;
    private LatLng latLng;

    public Step(double latitude, double longitude, String instruction) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.instruction = instruction;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getInstruction() {
        return instruction;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
