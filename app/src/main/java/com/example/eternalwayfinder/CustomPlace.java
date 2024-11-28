package com.example.eternalwayfinder;

import com.google.android.gms.maps.model.LatLng;

public class CustomPlace {
    private String name;
    private LatLng location;
    private LatLng latLng;
    private int availableSlots; // Add this field

    public CustomPlace(String name, LatLng location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public LatLng getLocation() {
        return location;
    }
    public LatLng getLatLng() {
        return latLng;
    }
    public int getAvailableSlots() {
        return availableSlots; // Getter for available slots
    }
}
