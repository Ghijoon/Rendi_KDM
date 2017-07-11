package com.ghijoon.klinikdayamedika;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ghijoon.klinikdayamedika.SQLHelper;
import com.ghijoon.klinikdayamedika.TambahSimpul;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapDirection mapDirection;

    LocationManager locationManager;
    LocationListener locationListener;

    LatLng dayaMedika = new LatLng(-6.189729, 106.763902);

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mapDirection = new MapDirection();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Toast.makeText(MapsActivity.this, location.toString(), Toast.LENGTH_SHORT).show();

                // Add a marker in the location and move the camera
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Lokasi Anda Di sini"));
                mMap.addMarker(new MarkerOptions().position(dayaMedika).title("Klinik Daya Medika"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                drawRoute(userLocation, dayaMedika);

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                try {
                    List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (listAddresses != null && listAddresses.size() > 0) {
                        String address = "";
                        // STREET
                        if (listAddresses.get(0).getSubThoroughfare() != null) {
                            address += listAddresses.get(0).getSubThoroughfare() + " ";
                        }
                        // AREA
                        if (listAddresses.get(0).getThoroughfare() != null) {
                            address += listAddresses.get(0).getThoroughfare() + ", ";
                        }
                        // TOWN
                        if (listAddresses.get(0).getLocality() != null) {
                            address += listAddresses.get(0).getLocality() + ", ";
                        }
                        // POSTAL CODE
                        if (listAddresses.get(0).getPostalCode() != null) {
                            address += listAddresses.get(0).getPostalCode() + ", ";
                        }
                        // COUNTRY
                        if (listAddresses.get(0).getCountryName() != null) {
                            address += listAddresses.get(0).getCountryName();
                        }
                        Toast.makeText(MapsActivity.this, address, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnowLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if(lastKnowLocation != null)
                {
                    LatLng userLocation = new LatLng(lastKnowLocation.getLatitude(), lastKnowLocation.getLongitude());
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Lokasi Anda Di sini"));
                    mMap.addMarker(new MarkerOptions().position(dayaMedika).title("Klinik Daya Medika"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
                    drawRoute(userLocation, dayaMedika);
                }
            }
        }
    }

    private void drawRoute(LatLng from, LatLng to)
    {
        LatLng fromTo[] = { from, to };
        // Pakai AsyncTask untuk menghindari error exception
        new getDirection().execute(fromTo);
    }

    private class getDirection extends AsyncTask<LatLng, Void, Document> {

        @Override
        protected Document doInBackground(LatLng... params)
        {
            Document document = mapDirection.getDocument(params[0], params[1], MapDirection.MODE_DRIVING);
            return document;
        }

        @Override
        protected void onPostExecute(Document result)
        {
            setResult(result);
        }

        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected void onProgressUpdate(Void... values)
        {

        }
    }

    public void setResult(Document document)
    {
        int duration = mapDirection.getDurationValue(document);
        String distance = mapDirection.getDistanceText(document);
        String start_address = mapDirection.getStartAddress(document);
        String copy_right = mapDirection.getCopyRights(document);

        // Ambil point direction / rute
        ArrayList<LatLng> directionPoint = mapDirection.getDirection(document);

        // Set konfigurasi rute
        PolylineOptions rectLine = new PolylineOptions().width(5).color(Color.RED);

        for (int i = 0; i < directionPoint.size(); i++)
        {
            rectLine.add(directionPoint.get(i));
        }

        mMap.addPolyline(rectLine);
    }}
