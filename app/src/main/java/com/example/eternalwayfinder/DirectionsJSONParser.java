package com.example.eternalwayfinder;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class DirectionsJSONParser {

    public void parse(String jsonData, GoogleMap mMap) {
        try {
            JSONObject jObject = new JSONObject(jsonData);
            JSONArray jRoutes = jObject.getJSONArray("routes");

            // Check if routes exist
            if (jRoutes.length() == 0) {
                Log.e("DirectionsJSONParser", "No routes found");
                return; // Exit if no routes
            }

            JSONArray jLegs = ((JSONObject) jRoutes.get(0)).getJSONArray("legs");
            JSONArray jSteps = ((JSONObject) jLegs.get(0)).getJSONArray("steps");

            // ArrayList to hold points of the route
            ArrayList<LatLng> points = new ArrayList<>();
            for (int i = 0; i < jSteps.length(); i++) {
                String polyline = ((JSONObject) jSteps.get(i)).getJSONObject("polyline").getString("points");
                points.addAll(decodePoly(polyline));
            }

            // Draw polyline on the map
            PolylineOptions lineOptions = new PolylineOptions();
            lineOptions.addAll(points);
            lineOptions.width(10); // Adjust the width of the polyline
            lineOptions.color(android.graphics.Color.BLUE); // Set the color of the polyline
            mMap.addPolyline(lineOptions);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Method to decode polyline into LatLng points
    private ArrayList<LatLng> decodePoly(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result >> 1) ^ -(result & 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result >> 1) ^ -(result & 1));
            lng += dlng;

            LatLng p = new LatLng((double) (lat / 1E5), (double) (lng / 1E5));
            poly.add(p);
        }
        return poly;
    }
}