package com.example.eternalwayfinder;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParserTask extends AsyncTask<String, Integer, PolylineOptions> {

    private GoogleMap mMap;
    private PolylineOptions polylineOptions;
    private Polyline currentPolyline;
    private Marker currentPin;
    private LatLng searchedLocation;

    public ParserTask(GoogleMap map, PolylineOptions polylineOptions, LatLng searchedLocation) {
        this.mMap = map;
        this.polylineOptions = polylineOptions;
        this.searchedLocation = searchedLocation; // Initialize searched location
    }

    @Override
    protected PolylineOptions doInBackground(String... jsonData) {
        JSONObject jsonObject;
        JSONArray jsonArray;
        String routes;
        try {
            jsonObject = new JSONObject(jsonData[0]);
            jsonArray = jsonObject.getJSONArray("routes");
            routes = jsonArray.getJSONObject(0).getString("overview_polyline");
            String points = new JSONObject(routes).getString("points");
            polylineOptions.addAll(decodePoly(points)); // Add points to polylineOptions
        } catch (Exception e) {
            Log.e("ParserTask", "Error parsing data: " + e.toString());
        }
        return polylineOptions; // Return the polylineOptions
    }

    @Override
    protected void onPostExecute(PolylineOptions polylineOptions) {
        clearMap(); // Clear existing markers and polylines
        currentPolyline = mMap.addPolyline(polylineOptions); // Draw the polyline
        currentPin = mMap.addMarker(new MarkerOptions()
                .position(searchedLocation) // Position of the searched location
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))); // Red pin
        //showWomanIcon(); // Show the woman icon
    }

    private void clearMap() {
        if (currentPolyline != null) {
            currentPolyline.remove(); // Remove previous polyline
        }
        if (currentPin != null) {
            currentPin.remove(); // Remove previous pin marker
        }
        // Optionally clear all markers if needed
         mMap.clear();
    }

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
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) (lat / 1E5), (double) (lng / 1E5));
            poly.add(p);
        }

        return poly;
    }

    private void showWomanIcon() {
        LatLng womanIconLocation = new LatLng(14.626133074044594, 120.98899137337042); // Starting point
        mMap.addMarker(new MarkerOptions().position(womanIconLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.woman)));
    }
}
