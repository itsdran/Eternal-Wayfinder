package com.example.eternalwayfinder;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchUrl extends AsyncTask<String, String, String> {

    private GoogleMap mMap;
    private PolylineOptions polylineOptions;
    private LatLng destination;

    public FetchUrl(GoogleMap map, PolylineOptions polylineOptions, LatLng destination) {
        this.mMap = map;
        this.polylineOptions = polylineOptions;
        this.destination = destination; // Save the searched destination
    }

    @Override
    protected String doInBackground(String... url) {
        String data = "";
        try {
            data = downloadUrl(url[0]); // Download the data from the URL
        } catch (Exception e) {
            Log.e("FetchUrl", "Error fetching data: " + e.toString());
        }
        return data; // Return the downloaded data
    }

    @Override
    protected void onPostExecute(String result) {
        // Pass the result to ParserTask
        new ParserTask(mMap, polylineOptions, destination).execute(result);
    }

    private String downloadUrl(String strUrl) throws Exception {
        StringBuilder data = new StringBuilder();
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return data.toString(); // Return the response as a string
    }
}
