package com.weedshop;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RideShareActivity extends AppCompatActivity implements OnMapReadyCallback {
    ArrayList markerPoints = new ArrayList();
    private static final int AUTOCOMPLETE_REQUEST_CODE = 202;
    private GoogleMap mMap;
    TextView txtBack, tvPickupAddress, tvDroffAddress;
    private int type;
    Double start, end;
    List<LatLng> listline;
    ArrayList<LatLng> mMarkerPoints;
    private LatLng mOrigin;
    private LatLng mDestination;
    private Polyline mPolyline;
    TextView tv_distance, tv_charge, tv_time;
    LinearLayout llview;
    Button btn_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ride_share);
        txtBack = findViewById(R.id.txtBack);
        tvPickupAddress = findViewById(R.id.tv_picupaddress);
        tvDroffAddress = findViewById(R.id.tv_droffaddress);
        llview = findViewById(R.id.llview);

        tv_distance = findViewById(R.id.tv_distance);
        tv_charge = findViewById(R.id.tv_charge);
        tv_time = findViewById(R.id.tv_time);
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RideShareActivity.this, MyTripActvity.class);
                intent.putExtra("title", "My Trip");
                startActivity(intent);
            }
        });

        listline = new ArrayList<>();
        mMarkerPoints = new ArrayList<>();
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.api_map_key));
        }

        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        tvPickupAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAddressapi();
                type = 1;
            }
        });
        tvDroffAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAddressapi();
                type = 2;
            }
        });
    }

    private void loadAddressapi() {
// Set the fields to specify which types of place data to return.
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i("xv", "Place: " + place.getName() + ", " + place.getId() + ", " + place.getAddress());
                String address = place.getAddress();

                if (type == 1) {
                    tvPickupAddress.setText(address);
                    start = Double.valueOf(String.valueOf(place.getLatLng().latitude));
                    end = Double.valueOf(String.valueOf(place.getLatLng().longitude));
                    // listline.add(place.getLatLng());

                    if (mMarkerPoints.size() >= 1) {

                        mMarkerPoints.set(0, place.getLatLng());

                    } else {
                        mMarkerPoints.add(place.getLatLng());
                    }
                    if (mMarkerPoints.size() == 2) {
                        drawPolyLineOnMap(mMarkerPoints);
                        llview.setVisibility(View.VISIBLE);
                    }

                } else {
                    tvDroffAddress.setText(address);
                    if (mMarkerPoints.size() >= 2) {

                        mMarkerPoints.set(1, place.getLatLng());
                    } else {
                        mMarkerPoints.add(place.getLatLng());
                    }

                    if (mMarkerPoints.size() == 2) {
                        drawPolyLineOnMap(mMarkerPoints);
                        llview.setVisibility(View.VISIBLE);
                    }
                }
                // do query with address

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("dgf", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public void onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields) //NIGERIA
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    public void drawPolyLineOnMap(ArrayList<LatLng> list) {
        mMap.clear();
        MarkerOptions options = new MarkerOptions();

        mMap.addMarker(new MarkerOptions().position(list.get(0)).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        mMap.addMarker(new MarkerOptions().position(list.get(1)).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        options.position(list.get(1));

        if (list.size() >= 2) {
            mOrigin = list.get(0);
            mDestination = list.get(1);
            drawRoute();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(list.get(0), 9));
    }

    private void drawRoute() {
        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(mOrigin, mDestination);
        DownloadTask downloadTask = new DownloadTask();
        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Key
        String key = "key=" + getString(R.string.api_map_key);

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception on download", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);

                final JSONObject json = new JSONObject(downloadUrl(url[0]));

                JSONArray routeArray = json.getJSONArray("routes");
                JSONObject routes = routeArray.getJSONObject(0);

                JSONArray newTempARr = routes.getJSONArray("legs");
                JSONObject newDisTimeOb = newTempARr.getJSONObject(0);

                JSONObject distOb = newDisTimeOb.getJSONObject("distance");
                JSONObject timeOb = newDisTimeOb.getJSONObject("duration");

                //Trip Charge cal

                Log.e("Diatance :", distOb.getString("text"));
                Log.e("Time :", timeOb.getString("text"));

                Float dis = Float.valueOf(distOb.getString("text").replaceAll("[^0-9?!\\.]", ""));
                double a = dis * 0.621371;

                double change = a * 0.80;


                tv_time.setText("Estimated Time : " + timeOb.getString("text"));
                tv_distance.setText("Estimated Distance : " + new DecimalFormat("##.#####").format(a) + " Miles");
                tv_charge.setText("Trip Charge : $" + new DecimalFormat("##.##").format(change));
                btn_confirm.setText("Confirm booking ($" + new DecimalFormat("##.##").format(change) + ")");


                Log.d("DownloadTask", "dismils : " + a);

                Log.d("DownloadTask", "DownloadTask : " + data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();


                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));


                    String distOb = point.get("distance");
                    String timeOb = point.get("duration");
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                if (mPolyline != null) {
                    mPolyline.remove();
                }
                mPolyline = mMap.addPolyline(lineOptions);


            } else
                Toast.makeText(getApplicationContext(), "No route is found", Toast.LENGTH_LONG).show();
        }
    }
}





