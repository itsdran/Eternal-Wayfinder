package com.example.eternalwayfinder;

import androidx.annotation.NonNull;
import androidx.compose.ui.text.intl.Locale;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.api.model.RectangularBounds;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.support.label.Category;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PlacesClient placesClient;
    private ImageButton voiceButton;
    private ImageButton demoButton;
    private ImageButton getDirections;
    private Polyline directionPolyline;
    private TextView distanceTextView;
    private TextView availableSlots;
    private RecyclerView placesRecyclerView;

    private TextToSpeech textToSpeech;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private List<Step> directions; // A list of directions (steps), fetched from your directions API
    private int currentStepIndex = 0; // Tracks the current step in the directions

    private static final LatLng CEMETERY_CENTER = new LatLng(14.631106881078026, 120.98913968441434);
    private static final LatLngBounds SEARCH_BOUNDS = new LatLngBounds(
            new LatLng(14.625693204090302, 120.9868748411516),
            new LatLng(14.636825304305608, 120.99329080198206)
    );

    private static final LatLng STARTING_POINT = new LatLng(14.626133074044594, 120.98899137337042);

    private Marker currentPlaceMarker;
    private Marker startingPointMarker;

    private String womanMarkerTitle;
    private String searchBarHint;
    private String voicePromptMessage;
    private String placeLabel;
    private String selectLocationMessage;
    private String selectedLocationLabel;
    private String voiceSearchNotSupportedMessage;
    private String availableSlotsMessage;
    private String selectStreet;
    private String goTo;
    private boolean isDemoMode = false;  // Tracks demo mode state
    private Marker womanMarker;

    private List<CustomPlace> customPlaces = Arrays.asList(
            new CustomPlace("Street 26", new LatLng(14.632529381361959, 120.99138373425612)),
            new CustomPlace("First Street", new LatLng(14.628934309985741, 120.98809863113692)),
            new CustomPlace("Second Street", new LatLng(14.628870726193881, 120.98911116503993)),
            new CustomPlace("Street 5", new LatLng(14.629727737997266, 120.9877954718654)),
            new CustomPlace("Street 9", new LatLng(14.630786416844652, 120.98781609348966)),
            new CustomPlace("Main Avenue", new LatLng(14.632885943021819, 120.9891967522133)),
            new CustomPlace("Avenue B", new LatLng(14.634706339714842, 120.98933491159607)),
            new CustomPlace("Street 16", new LatLng(14.630432398808233, 120.98858482023014)),
            new CustomPlace("Street 21", new LatLng(14.635951002724871, 120.98821667246641)),
            new CustomPlace("Libingan ng Beterano", new LatLng(14.635951002724871, 120.98821667246641)),
            new CustomPlace("Graves of Veterans", new LatLng(14.635951002724871, 120.98821667246641)),
            new CustomPlace("Street 24", new LatLng(14.635932485531571, 120.99165517107355)),
            new CustomPlace("Street 38", new LatLng(14.630344952687043, 120.98858777219426)),
            new CustomPlace("Entrance", new LatLng(14.626166366149295, 120.98897629263693)),
            new CustomPlace("Exit", new LatLng(14.626166366149295, 120.98897629263693)),
            new CustomPlace("Circle", new LatLng(14.629278622725826, 120.98838389143262))
    );

    private void initializeAvailableSlots() {
        availableSlotsMap.put("first street", 9);
        availableSlotsMap.put("second street", 5);
        availableSlotsMap.put("street 5", 3);
        availableSlotsMap.put("street 9", 2);
        availableSlotsMap.put("main avenue", 7);
        availableSlotsMap.put("avenue b", 4);
        availableSlotsMap.put("street 16", 1);
        availableSlotsMap.put("street 21", 6);
        availableSlotsMap.put("libingan ng beterano", 8);
        availableSlotsMap.put("graves of veterans", 10);
        availableSlotsMap.put("street 24", 0);
        availableSlotsMap.put("street 26", 5);
        availableSlotsMap.put("street 38", 1);
    }

    private HashMap<String, Integer> availableSlotsMap = new HashMap<>();
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Receive the language preference from the HomePage intent
        Intent intent = getIntent();
        String language = intent.getStringExtra("language");

        // Set language-specific text based on the received language
        if ("tl".equals(language)) {
            switchToFilipino();
        } else {
            switchToEnglish();
        }

        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));

        placesClient = Places.createClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setupAutocompleteFragment();
        initializeAvailableSlots();

        voiceButton = findViewById(R.id.voiceButton);
        voiceButton.setOnClickListener(v -> startVoiceSearch());

        distanceTextView = findViewById(R.id.distanceTextView);
        availableSlots = findViewById(R.id.availableSlots);

        View searchStreetButton = findViewById(R.id.searchStreet); // Initialize searchStreet button
        searchStreetButton.setOnClickListener(v -> showCustomPlacesDialog());

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();

        // Handle location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update directions when the user moves
                    updateDirections(location);
                }
            }
        };

        // Demo button to toggle predefined starting point
        demoButton = findViewById(R.id.demoButton);
        demoButton.setOnClickListener(v -> toggleDemoMode());

        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                if (language.equals("en")) {
                    textToSpeech.setLanguage(new Locale("en").getPlatformLocale()); // For US English
                } else if (language.equals("fil")) {
                    //textToSpeech.setLanguage(new Locale("fil").getPlatformLocale()); // For Filipino
                    textToSpeech.setLanguage(new Locale("hin").getPlatformLocale());

                }
                textToSpeech.setSpeechRate(0.8f);
            }
        });
    }

    private void showCustomPlacesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(selectStreet);

        // Create a TableLayout for the dialog
        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setStretchAllColumns(true);

        // Create table header
        TableRow headerRow = new TableRow(this);
        TextView streetHeader = new TextView(this);
        streetHeader.setText("Street");
        streetHeader.setPadding(16, 16, 16, 16);
        streetHeader.setTypeface(null, Typeface.BOLD);

        TextView slotsHeader = new TextView(this);
        slotsHeader.setText(availableSlotsMessage);
        slotsHeader.setPadding(16, 16, 16, 16);
        slotsHeader.setTypeface(null, Typeface.BOLD);

        TextView goToHeader = new TextView(this);
        goToHeader.setText(goTo);
        goToHeader.setPadding(16, 16, 16, 16);
        goToHeader.setTypeface(null, Typeface.BOLD);

        headerRow.addView(streetHeader);
        headerRow.addView(slotsHeader);
        headerRow.addView(goToHeader);
        tableLayout.addView(headerRow);

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Create a row for each custom place
        for (CustomPlace place : customPlaces) {
            String name = place.getName().toLowerCase();
            LatLng location = place.getLocation();
            String availableSlots = String.valueOf(availableSlotsMap.get(name));

            // Create a TableRow for each place
            TableRow row = new TableRow(this);

            // Street name
            TextView streetTextView = new TextView(this);
            streetTextView.setText(place.getName());
            streetTextView.setPadding(16, 16, 16, 16);
            row.addView(streetTextView);

            // Available slots
            TextView slotsTextView = new TextView(this);
            slotsTextView.setText(availableSlots);
            slotsTextView.setPadding(16, 16, 16, 16);
            row.addView(slotsTextView);

            // Action button
            Button goToButton = new Button(this);
            goToButton.setText(goTo);
            goToButton.setOnClickListener(v -> {
                // Show directions for the selected place
                getDirections(STARTING_POINT, location);
                displayDistance(STARTING_POINT, location);
                displayAvailableSlots(name); // Display slots for custom place

                // Dismiss the dialog
                dialog.dismiss(); // Dismiss the dialog here
            });
            row.addView(goToButton);

            // Add the row to the table
            tableLayout.addView(row);
        }

        // Set the TableLayout as the dialog view and show the dialog
        dialog.setView(tableLayout);
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialogInterface, which) -> dialogInterface.dismiss());
        dialog.show();
    }

    // Create location request with desired update intervals
    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // Update every 5 seconds
        locationRequest.setFastestInterval(2000); // Minimum 2 seconds between updates
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // Update directions dynamically as the user moves
    private void updateDirections(Location currentLocation) {
        if (directions != null && currentStepIndex < directions.size()) {
            Step nextStep = directions.get(currentStepIndex);
            float[] results = new float[1];
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                    nextStep.getLatitude(), nextStep.getLongitude(), results);
            float distanceToNextStep = results[0];

            // Check if user is close to the next step
            if (distanceToNextStep < 30) { // Threshold of 30 meters
                sayDirection(nextStep.getInstruction()); // Speak the direction
                currentStepIndex++; // Move to the next step
            }

            // Optionally, if destination is within a threshold, speak "You have arrived"
            LatLng destinationLatLng = directions.get(directions.size() - 1).getLatLng();
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                    destinationLatLng.latitude, destinationLatLng.longitude, results);
            float distanceToDestination = results[0];

            if (distanceToDestination < 30) {
                sayDirection("You have arrived at your destination.");
                stopLocationUpdates(); // Stop updates when user arrives
            }
        }
    }

    // Method to use TextToSpeech for speaking out directions
    private void sayDirection(String direction) {
        if (textToSpeech != null && !textToSpeech.isSpeaking()) {
            textToSpeech.speak(direction, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    // Stop location updates once the user arrives
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // Toggle demo mode function
    private void toggleDemoMode() {
        isDemoMode = !isDemoMode; // Toggle demo mode state
        if (isDemoMode) {
            mMap.clear();
            updateWomanMarker(STARTING_POINT); // Set woman marker to predefined coordinates
            Toast.makeText(this, "Demo mode ON", Toast.LENGTH_SHORT).show();
            distanceTextView.setText("");
        } else {
            getCurrentLocation(); // Fetch current location and update the woman's position
            Toast.makeText(this, "Demo mode OFF", Toast.LENGTH_SHORT).show();
        }
    }

    // Start location updates
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    removePreviousDirections();
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        updateWomanMarker(currentLatLng);
                        getDirections(currentLatLng, STARTING_POINT); // Get directions dynamically
                        displayDistance(currentLatLng, STARTING_POINT);
                    }
                });
    }

    private void updateWomanMarker(LatLng newLocation) {
        womanMarker = mMap.addMarker(new MarkerOptions()
                .position(newLocation)
                .title(womanMarkerTitle)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.woman)));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 16.5f));
    }

    private void displayAvailableSlots(String streetName) {
        // Convert streetName to lower case before retrieving from the map
        Integer availableSlots = availableSlotsMap.get(streetName.toLowerCase());
        if (availableSlots != null) {
            TextView slotsTextView = findViewById(R.id.availableSlots);
            slotsTextView.setText(availableSlotsMessage + " " + availableSlots);
        } else {
            // Optionally handle the case where no slots are found
            TextView slotsTextView = findViewById(R.id.availableSlots);
            slotsTextView.setText(""); // or display a message that no slots are available
        }
    }

    private void setupAutocompleteFragment() {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        RectangularBounds bounds = RectangularBounds.newInstance(SEARCH_BOUNDS.southwest, SEARCH_BOUNDS.northeast);
        autocompleteFragment.setLocationRestriction(bounds); // Set bounds restriction to the cemetery

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // Extract the place name and location (LatLng)
                String placeName = place.getName();
                LatLng placeLatLng = place.getLatLng();

                // Pass these to the method for handling place selection
                handlePlaceSelection(placeName, placeLatLng);
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i("PlacesError", "An error occurred: " + status);
            }
        });

        autocompleteFragment.setHint(searchBarHint);  // Set the hint initially based on the current language
    }

    private void handlePlaceSelection(String placeName, LatLng placeLatLng) {
        CustomPlace matchingCustomPlace = findCustomPlaceByName(placeName);
        if (matchingCustomPlace != null) {
            handleCustomPlaceSelection(matchingCustomPlace);
            displayAvailableSlots(matchingCustomPlace.getName());
            return;
        }

        if (!SEARCH_BOUNDS.contains(placeLatLng)) {
            Toast.makeText(this, "Place is outside the cemetery bounds.", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng origin = isDemoMode ? STARTING_POINT : getCurrentStartingPoint();

        getDirections(origin, placeLatLng);
        displayDistance(origin, placeLatLng);
        updateWomanMarker(origin);
    }

    private LatLng getCurrentStartingPoint() {
        return isDemoMode ? STARTING_POINT : womanMarker.getPosition(); // Use predefined or current location
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        configureMapSettings();

        toggleDemoMode();
        mMap.setOnMapLongClickListener(latLng -> {
            if (SEARCH_BOUNDS.contains(latLng)) {
                removePreviousDirections();
                addMarkerOnLongPress(getCurrentStartingPoint(), latLng);
            } else {
                Toast.makeText(this, selectLocationMessage, Toast.LENGTH_SHORT).show();  // Use the translated message here
            }
        });
    }

    private void configureMapSettings() {
        // Disable user interactions
        //mMap.getUiSettings().setScrollGesturesEnabled(false);  // Disable panning/scrolling
        //mMap.getUiSettings().setZoomGesturesEnabled(false);    // Disable zoom gestures (pinch to zoom)
        //mMap.getUiSettings().setTiltGesturesEnabled(false);    // Disable tilt gestures
        //mMap.getUiSettings().setRotateGesturesEnabled(false);  // Disable rotation gestures

        // Disable compass and my location button
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Set the initial camera position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(CEMETERY_CENTER)
                .zoom(17.5f)
                .tilt(15)
                .bearing(15)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // Restrict the view to the specified bounds and zoom levels
        mMap.setLatLngBoundsForCameraTarget(SEARCH_BOUNDS);
        mMap.setMinZoomPreference(5.5f);
        mMap.setMaxZoomPreference(16.5f);
    }

    private void removePreviousDirections() {
        if (currentPlaceMarker != null) {
            currentPlaceMarker.remove();
            currentPlaceMarker = null;
        }
        if (directionPolyline != null) {
            directionPolyline.remove();
            directionPolyline = null;
        }
        distanceTextView.setText("");
    }

    private void addMarkerOnLongPress(LatLng STARTING_POINT, LatLng latLng) {
        currentPlaceMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(selectedLocationLabel)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        getDirections(STARTING_POINT, latLng);
        displayDistance(STARTING_POINT, latLng);
        updateWomanMarker(STARTING_POINT);
    }

    private void getDirections(LatLng origin, LatLng destination) {
        //removePreviousDirections();
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(origin).icon(BitmapDescriptorFactory.fromResource(R.drawable.woman)));

        PolylineOptions polylineOptions = new PolylineOptions()
                .width(10)
                .color(Color.BLUE);

        String url = getDirectionsUrl(origin, destination);
        new FetchUrl(mMap, polylineOptions, destination).execute(url);
        new ParserTask(mMap, polylineOptions, destination).execute(url);

        // Fetch and speak directions
        getDirections = findViewById(R.id.getDirections);
        getDirections.setOnClickListener(v -> fetchAndSpeakDirections(url)); // Set OnClickListener
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchAndSpeakDirections(String url) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    return result.toString();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONArray routes = jsonObject.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            JSONArray legs = route.getJSONArray("legs");
                            if (legs.length() > 0) {
                                JSONObject leg = legs.getJSONObject(0);
                                JSONArray steps = leg.getJSONArray("steps");
                                StringBuilder directions = new StringBuilder();
                                for (int i = 0; i < steps.length(); i++) {
                                    JSONObject step = steps.getJSONObject(i);

                                    // Extracting direction and distance information
                                    String htmlInstructions = step.getString("html_instructions");
                                    String plainTextInstructions = Html.fromHtml(htmlInstructions).toString();
                                    String directionText = getDirectionText(step);

                                    JSONObject distanceObject = step.getJSONObject("distance");
                                    String distanceText = distanceObject.getString("text");

                                    // Combining instructions
                                    directions.append(directionText).append(" for ").append(distanceText).append(". ");
                                }
                                sayDirection(directions.toString());
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute(url);
    }

    private String getDirectionsUrl(LatLng origin, LatLng destination) {
        String strOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String strDest = "destination=" + destination.latitude + "," + destination.longitude;
        String key = "key=" + getString(R.string.google_maps_key);
        return "https://maps.googleapis.com/maps/api/directions/json?" + strOrigin + "&" + strDest + "&sensor=false&" + key;
    }

    // Helper function to parse maneuver or direction
    private String getDirectionText(JSONObject step) {
        try {
            String maneuver = step.optString("maneuver", "");
            String distanceText = step.getJSONObject("distance").getString("text");

            Intent intent = getIntent();
            String language = intent.getStringExtra("language");

            if (language.equals("tl")) {
                switch (maneuver) {
                    case "turn-left": return "Kumaliwa nang ";
                    case "turn-right": return "Kumanan nang ";
                    case "straight": return "Dumiretso nang ";
                    case "uturn-left": return "Gumawa nang U-turn pakaliwa nang ";
                    case "uturn-right": return "Gumawa nang U-turn pakanan nang " ;
                    case "roundabout-left": return "Pumunta sa rotonda pakaliwa nang ";
                    case "roundabout-right": return "Pumunta sa rotonda pakanan nang ";
                    case "fork-left": return "Kumanan sa kaliwang sangandaan nang ";
                    case "fork-right": return "Kumanan sa kanang sangandaan nang ";
                    default: return "Maglakad nang " + distanceText;
                }
            } else {
                switch (maneuver) {
                    case "turn-left": return "Turn left for ";
                    case "turn-right": return "Turn right for ";
                    case "straight": return "Continue straight for ";
                    case "uturn-left": return "Make a U-turn to the left for ";
                    case "uturn-right": return "Make a U-turn to the right for ";
                    case "roundabout-left": return "Take the roundabout to the left for ";
                    case "roundabout-right": return "Take the roundabout to the right for ";
                    case "fork-left": return "Keep left at the fork for ";
                    case "fork-right": return "Keep right at the fork for ";
                    default: return "Head for " + distanceText;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            boolean isFilipino = false;
            return isFilipino ? "Magpatuloy" : "Proceed";
        }
    }

    private void startVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, voicePromptMessage); // Set the voice prompt dynamically based on the language
        try {
            startActivityForResult(intent, 100);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, voiceSearchNotSupportedMessage, Toast.LENGTH_SHORT).show(); // Use the translated message here
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenInput = results.get(0);
                searchPlaces(spokenInput);

            }
        }
    }

    private void searchPlaces(String spokenInput) {
        // Check for custom places first
        List<CustomPlace> matchedCustomPlaces = new ArrayList<>();
        for (CustomPlace customPlace : customPlaces) {
            if (customPlace.getName().toLowerCase().contains(spokenInput.toLowerCase())) {
                matchedCustomPlaces.add(customPlace);
                displayAvailableSlots(customPlace.getName()); // Display slots for custom place
            }
        }

        if (!matchedCustomPlaces.isEmpty()) {
            for (CustomPlace customPlace : matchedCustomPlaces) {
                handleCustomPlaceSelection(customPlace);
                displayAvailableSlots(customPlace.getName()); // Display slots for custom place
            }
            return; // Stop further search if custom places are found
        }

        // If no custom place is found, use Google Places API for further search
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setLocationBias(RectangularBounds.newInstance(SEARCH_BOUNDS)) // Limit results to cemetery bounds
                .setQuery(spokenInput)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(prediction.getPlaceId(), Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)).build();

                placesClient.fetchPlace(placeRequest).addOnSuccessListener(placeResponse -> {
                    Place place = placeResponse.getPlace();
                    LatLng placeLatLng = place.getLatLng();

                    // Only add places within cemetery bounds
                    if (SEARCH_BOUNDS.contains(placeLatLng)) {
                        handlePlaceSelection(place.getName(), placeLatLng);
                        displayDistance(STARTING_POINT, placeLatLng);  // Display the distance
                    }
                });
            }
        }).addOnFailureListener(e -> Log.e("PlacesError", "Error fetching predictions: ", e));
    }

    private CustomPlace findCustomPlaceByName(String placeName) {
        for (CustomPlace customPlace : customPlaces) {
            if (customPlace.getName().equalsIgnoreCase(placeName)) {
                return customPlace;
            }
        }
        return null;
    }

    private void handleCustomPlaceSelection(CustomPlace customPlace) {
        removePreviousDirections();
        LatLng placeLatLng = customPlace.getLocation();
        getDirections(STARTING_POINT, placeLatLng);

        // Show a message to the user
        String message = (womanMarkerTitle.equals("You are here")) ? placeLabel + customPlace.getName() : placeLabel + customPlace.getName();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        displayDistance(STARTING_POINT, placeLatLng);  // Display the distance
    }

    private void displayDistance(LatLng origin, LatLng destination) {
        float[] results = new float[1];
        Location.distanceBetween(origin.latitude, origin.longitude, destination.latitude, destination.longitude, results);
        float distanceInMeters = results[0];
        float distanceInKm = distanceInMeters / 1000;
        // Update the distance label based on the selected language
        String distanceLabel = (womanMarkerTitle.equals("You are here")) ? "Distance: " : "Layo: ";
        distanceTextView.setText(String.format("%s%.2f m (%.2f km)", distanceLabel, distanceInMeters, distanceInKm));
    }

    private void addCustomPlaceMarkers() {
        // Clear any existing markers
        mMap.clear();

        // Loop through the custom places to add red pins and available slots
        for (CustomPlace customPlace : customPlaces) {
            LatLng latLng = customPlace.getLocation();
            String placeName = customPlace.getName();
            Integer slots = availableSlotsMap.get(placeName.toLowerCase());

            // Add marker for the custom place
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(placeName + " (" + availableSlotsMessage + " " + (slots != null ? slots : 0) + ")")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            // Show the info window for the marker (optional)
            marker.showInfoWindow();
        }

        // Move camera to show all markers if necessary
        if (customPlaces.size() > 0) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (CustomPlace customPlace : customPlaces) {
                builder.include(customPlace.getLocation());
            }
            configureMapSettings();
        }
    }

    private void switchToEnglish() {
        womanMarkerTitle = "You are here";
        searchBarHint = "Search a place";
        voicePromptMessage = "Say a location to search for";
        placeLabel = "Place: ";
        selectLocationMessage = "Please select a location within the cemetery.";  // Translation added here
        selectedLocationLabel = "Selected Location";
        voiceSearchNotSupportedMessage = "Voice search is not supported on this device.";
        availableSlotsMessage = "Free Slot";
        selectStreet = "Select a Street";
        goTo =  "Go To";
        if (startingPointMarker != null) {
            startingPointMarker.setTitle(womanMarkerTitle);
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setHint(searchBarHint);
    }

    private void switchToFilipino() {
        womanMarkerTitle = "Nandito ka";
        searchBarHint = "Maghanap ng lugar";
        voicePromptMessage = "Banggitin ang lugar na nais hanapin";
        placeLabel = "Lugar: ";
        selectLocationMessage = "Humanap lamang ng lugar sa loob ng sementeryo.";  // Translation added here
        selectedLocationLabel = "Napiling lugar";
        voiceSearchNotSupportedMessage = "Hindi suportado ng device ang voice search.";
        availableSlotsMessage = "Bakante:";
        selectStreet = "Pumili ng Street";
        goTo =  "Pumunta";
        if (startingPointMarker != null) {
            startingPointMarker.setTitle(womanMarkerTitle);
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setHint(searchBarHint);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clear the map
        if (mMap != null) {
            mMap.clear();
        }

        // Shutdown the Text-to-Speech engine
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
