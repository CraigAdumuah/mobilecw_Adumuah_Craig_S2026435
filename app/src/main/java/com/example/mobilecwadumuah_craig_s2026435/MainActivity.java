package com.example.mobilecwadumuah_craig_s2026435;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnMapReadyCallback {
    private GoogleMap mMap;
    private Spinner locationSpinner;
    private Button showWeatherButton;
    private String currentLocation;
    private TextView rawDataDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rawDataDisplay = findViewById(R.id.rawDataDisplay);
        locationSpinner = findViewById(R.id.locationSpinner);

        String[] cities = {"Glasgow", "London", "New York", "Oman", "Mauritius", "Bangladesh"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cities);
        locationSpinner.setAdapter(adapter);

        locationSpinner.setOnItemSelectedListener(this);

        showWeatherButton = findViewById(R.id.showWeatherButton);
        showWeatherButton.setOnClickListener(v -> fetchWeatherDataFromAPI(currentLocation));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        currentLocation = parent.getItemAtPosition(position).toString();
        updateMapForLocation(currentLocation);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Handle no location selected (optional)
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add default marker (e.g., Glasgow) when the map is ready
        updateMapForLocation("Glasgow");
    }

    private void updateMapForLocation(String location) {
        // Dummy location coordinates (replace with actual coordinates)
        double lat, lng;
        switch (location) {
            case "Glasgow":
                lat = 55.8642;
                lng = -4.2518;
                break;
            case "London":
                lat = 51.5074;
                lng = -0.1278;
                break;
            case "New York":
                lat = 40.7128;
                lng = -74.0060;
                break;
            case "Oman":
                lat = 21.4735;
                lng = 55.9754;
                break;
            case "Mauritius":
                lat = -20.3484;
                lng = 57.5522;
                break;
            case "Bangladesh":
                lat = 23.6850;
                lng = 90.3563;
                break;
            default:
                lat = 0;
                lng = 0;
                break;
        }
        LatLng coordinates = new LatLng(lat, lng);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(coordinates).title(location));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 10));
    }

    private void showWeatherDialog(String weatherData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Weather Data for " + currentLocation);
        builder.setMessage(weatherData);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void fetchWeatherDataFromAPI(String location) {
        new Thread(() -> {
            try {
                // Construct the API URL with the location name
                String apiKey = "AIzaSyAg97kBHmdoiYUf7gS6HWHw7a9mHSP0bdE";
                String apiUrl = "https://api.weatherapi.com/v1/forecast.xml?key=" + apiKey + "&q=" + location + "&days=3";

                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the XML response
                String weatherData = parseXmlData(response.toString());

                // Display the weather data in a dialog
                runOnUiThread(() -> showWeatherDialog(weatherData));

                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String parseXmlData(String xmlData) {
        StringBuilder weatherData = new StringBuilder();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlData));

            int eventType = parser.getEventType();
            String title = "";
            String description = "";
            String pubDate = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    if ("title".equals(tagName)) {
                        title = parser.nextText().trim();
                    } else if ("description".equals(tagName)) {
                        description = parser.nextText().trim();
                    } else if ("pubDate".equals(tagName)) {
                        pubDate = parser.nextText().trim();
                    }
                } else if (eventType == XmlPullParser.END_TAG && "item".equals(parser.getName())) {
                    weatherData.append("Title: ").append(title).append("\n");
                    weatherData.append("Description: ").append(description).append("\n");
                    weatherData.append("PubDate: ").append(pubDate).append("\n\n");

                    title = "";
                    description = "";
                    pubDate = "";
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return weatherData.toString();
    }
}
