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

    SQLHelper dbHelper;
    Cursor cursor;

    public String __global_endposition = null;
    public String __global_startposition = null;
    public int __global_simpul_awal;
    public int __global_simpul_akhir;
    public String __global_old_simpul_awal = "";
    public String __global_old_simpul_akhir = "";
    public int __global_maxRow0;
    public int __global_maxRow1;
    private String[][] __global_graphArray;
    private LatLng __global_yourCoordinate_exist = null;

    private GoogleMap mMap;
    private MapDirection mapDirection;

    LocationManager locationManager;
    LocationListener locationListener;

    public static LatLng userLocation;
    public static LatLng dayaMedika;

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

        dbHelper = new SQLHelper(this);
        try {
            dbHelper.createDataBase();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Koneksi Database Gagal", Toast.LENGTH_SHORT).show();
        }

        mapDirection = new MapDirection();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        dbHelper = new SQLHelper(this);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM klinik", null);
        cursor.moveToFirst();
        String[] koordinatDayaMedika = cursor.getString(2).split(",");
        __global_endposition = koordinatDayaMedika.toString();
        double latDayaMedika = Double.parseDouble(koordinatDayaMedika[0]);
        double lonDayaMedika = Double.parseDouble(koordinatDayaMedika[1]);

        dayaMedika = new LatLng(latDayaMedika, lonDayaMedika);
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
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Lokasi Anda"));
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
                    userLocation = new LatLng(lastKnowLocation.getLatitude(), lastKnowLocation.getLongitude());
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
    }

    public void startingScript(double latUser, double lngUser, double lat_endposition, double lng_endposition) throws JSONException {
        // delete temporary record DB
        deleteTemporaryRecord();

        // reset google map
        mMap.clear();

        // convert graph from DB to Array; graph[][]
        GraphToArray DBGraph = new GraphToArray();
        __global_graphArray = DBGraph.convertToArray(this); // return graph[][] Array

        // get max++ row temporary DB
        maxRowDB();

        // GET COORDINATE AWAL DI SEKITAR SIMPUL
        // coordinate awal lalu di konversi ke simpul awal
        // return __global_simpul_awal, __global_graphArray[][]
        // ==========================================
        KoordinatAwalAkhir start_coordinate_jalur = new KoordinatAwalAkhir();
        getSimpulAwalAkhirJalur(start_coordinate_jalur, latUser, lngUser, "awal");

        // GET COORDINATE AKHIR DI SEKITAR SIMPUL
        // coordinate akhir lalu di konversi ke simpul akhir
        // return __global_simpul_akhir, __global_graphArray[][]
        // ==========================================
        KoordinatAwalAkhir destination_coordinate_jalur = new KoordinatAwalAkhir();
        getSimpulAwalAkhirJalur(destination_coordinate_jalur, lat_endposition, lng_endposition, "akhir");

        // ALGORITMA DIJKSTRA
        // ==========================================
        Djikstra algo = new Djikstra();
        algo.jalurTercepat(__global_graphArray, __global_simpul_awal, __global_simpul_akhir);

        // no result for algoritma dijkstra
        if(algo.status == "die"){

            Toast.makeText(getApplicationContext(), "Lokasi Anda sudah dekat dengan lokasi tujuan", Toast.LENGTH_LONG).show();

        }else{
            // return jalur terpendek; example 1->5->6->7
            String[] exp = algo.jalur_terpendek1.split("->");

            // DRAW JALUR ANGKUTAN UMUM
            // =========================================
            drawJalur(algo.jalur_terpendek1, exp);
        }
    }


    /*
     * @fungsi
     *  menggambar jalur angkutan umum
     *  menentukan jenis angkutan umum yang melewati jalur tsb
     *  membuat marker untuk your position dan destination position
     * @parameter
     *  exp[] : jalur terpendek; example 1->5->6->7
     * @return
     *  no return
     */
    public void drawJalur(String alg, String[] exp) throws JSONException{

        int start = 0;

        // GAMBAR JALURNYA
        // ======================
        dbHelper = new SQLHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        for(int i = 0; i < exp.length-1; i++){

            ArrayList<LatLng> lat_lng = new ArrayList<LatLng>();

            cursor = db.rawQuery("SELECT jalur FROM graph where simpul_awal ="+exp[start]+" and simpul_tujuan ="+exp[(++start)], null);
            cursor.moveToFirst();


            // dapatkan koordinat Lat,Lng dari field koordinat (3)
            String json = cursor.getString(0).toString();

            // get JSON
            JSONObject jObject = new JSONObject(json);
            JSONArray jArrCoordinates = jObject.getJSONArray("coordinates");

            // get coordinate JSON
            for(int w = 0; w < jArrCoordinates.length(); w++){

                JSONArray latlngs = jArrCoordinates.getJSONArray(w);
                Double lats = latlngs.getDouble(0);
                Double lngs = latlngs.getDouble(1);


                lat_lng.add( new LatLng(lats, lngs) );

            }

            // buat rute
            PolylineOptions jalurBiasa = new PolylineOptions();
            jalurBiasa.addAll(lat_lng).width(5).color(0xff4b9efa).geodesic(true);
            mMap.addPolyline(jalurBiasa);

        }


        // BUAT MARKER UNTUK YOUR POSITION AND DESTINATION POSITION
        // ======================
        // your position
        mMap.addMarker(new MarkerOptions()
                .position(__global_yourCoordinate_exist)
                .title("Your position")
                .snippet("Your position")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        String[] exp_endCoordinate = __global_endposition.split(",");
        double lat_endPosition = Double.parseDouble(exp_endCoordinate[0]);
        double lng_endPosition = Double.parseDouble(exp_endCoordinate[1]);
        LatLng endx = new LatLng(lat_endPosition, lng_endPosition);

        // destination position
        mMap.addMarker(new MarkerOptions()
                .position(endx)
                .title("Destination position")
                .snippet("Destination position")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


        // TENTUKAN JENIS ANGKUTAN UMUM YANG MELEWATI JALUR TERSEBUT
        // ==========================================================
        // misal exp[] = 1->5->6->7
        int m = 0;


        String[] awal = __global_old_simpul_awal.split("-"); // misal 4-5
        String[] akhir = __global_old_simpul_akhir.split("-"); // misal 8-7

        int ganti_a = 0;
        int ganti_b = 0;
        int simpulAwalDijkstra = Integer.parseInt(exp[0]);

        String gabungSimpul_all = "";
        Map<String, ArrayList> listAngkutanUmum = new HashMap<String, ArrayList>();
        ArrayList<Integer> listSimpulAngkot = new ArrayList<Integer>();

        // cari simpul_old sebelum koordinat dipecah
        // misal 4-5 dipecah menjadi 4-6-5, berarti simpul_old awal = 5, simpul_old akhir = 4
        for(int e = 0; e < (exp.length - 1); e++){

            if(e == 0){ // awal

                // dijalankan jika hasil algo hanya 2 simpul, example : 4->5
                if(exp.length == 2 /* 2 simpul (4-5)*/){

                    // ada simpul baru di awal (10) dan di akhir (11), example 10->11
                    if( exp[0].equals(String.valueOf(__global_maxRow0)) && exp[1].equals(String.valueOf(__global_maxRow1)) ){

                        if(String.valueOf(__global_maxRow0).equals(akhir[0])){
                            ganti_b = Integer.parseInt(akhir[1]);
                        }else{
                            ganti_b = Integer.parseInt(akhir[0]);
                        }

                        if(String.valueOf(ganti_b).equals(awal[0])){
                            ganti_a = Integer.parseInt(awal[1]);
                        }else{
                            ganti_a = Integer.parseInt(awal[0]);
                        }
                    }
                    else{
                        // ada simpul baru di awal (10), example 10->5
                        // maka cari simpul awal yg oldnya
                        if( exp[0].equals(String.valueOf(__global_maxRow0)) ){

                            if(exp[1].equals(awal[1])){
                                ganti_a = Integer.parseInt(awal[0]);
                            }else{
                                ganti_a = Integer.parseInt(awal[1]);
                            }
                            ganti_b = Integer.parseInt(exp[1]);
                        }
                        // ada simpul baru di akhir (10), example 5->10
                        // maka cari simpul akhir yg oldnya
                        else if( exp[1].equals(String.valueOf(__global_maxRow0)) ){

                            if(exp[0].equals(akhir[0])){
                                ganti_b = Integer.parseInt(akhir[1]);
                            }else{
                                ganti_b = Integer.parseInt(akhir[0]);
                            }
                            ganti_a = Integer.parseInt(exp[0]);
                        }
                        // tidak ada penambahan simpul sama sekali
                        else{
                            ganti_a = Integer.parseInt(exp[0]);
                            ganti_b = Integer.parseInt(exp[1]);
                        }
                    }

        			/*
        			// 4 == 4
        			if(exp[0].equals(awal[0])){
            			ganti_a = Integer.parseInt(awal[0]);
            			//ganti_b = Integer.parseInt(awal[1]);
        			}else{
            			ganti_a = Integer.parseInt(awal[1]);
            			//ganti_b = Integer.parseInt(awal[0]);
        			}

        			if(String.valueOf(ganti_a).equals(akhir[0])){
            			ganti_b = Integer.parseInt(akhir[1]);
            			//ganti_b = Integer.parseInt(awal[1]);
        			}else{
            			ganti_b = Integer.parseInt(akhir[0]);
            			//ganti_b = Integer.parseInt(awal[0]);
        			}
        			*/

        			/*
        			 *         			// 4 == 4
        			if(exp[0].equals(awal[0])){
            			ganti_a = Integer.parseInt(akhir[0]);
            			ganti_b = Integer.parseInt(awal[1]);
        			}else{
            			ganti_a = Integer.parseInt(awal[1]);
            			ganti_b = Integer.parseInt(akhir[0]);
        			}
        			 */

                }
                // hasil algo lebih dr 2 : 4->5->8->7-> etc ..
                else{
                    if(exp[1].equals(awal[1])){ // 5 == 5
                        ganti_a = Integer.parseInt(awal[0]); // hasil 4
                    }else{
                        ganti_a = Integer.parseInt(awal[1]); // hasil 5
                    }

                    ganti_b = Integer.parseInt( exp[++m] );
                }
            }
            else if(e == (exp.length - 2)){ // akhir

                if(exp[ (exp.length - 2) ].equals(akhir[1])){ // 7 == 7
                    ganti_b = Integer.parseInt(akhir[0]); // hasil 8
                }else{
                    ganti_b = Integer.parseInt(akhir[1]); // hasil 7
                }

                ganti_a = Integer.parseInt( exp[m] );

            }else{ // tengah tengah
                ganti_a = Integer.parseInt( exp[m] );
                ganti_b = Integer.parseInt( exp[++m] );
            }

            gabungSimpul_all += "," + ganti_a + "-" + ganti_b + ","; // ,1-5,
            String gabungSimpul = "," + ganti_a + "-" + ganti_b + ","; // ,1-5,

            cursor = db.rawQuery("SELECT * FROM rute where simpul like '%" + gabungSimpul + "%'", null);
            cursor.moveToFirst();

            ArrayList<String> listAngkutan = new ArrayList<String>();

            for(int ae = 0; ae < cursor.getCount(); ae++){
                cursor.moveToPosition(ae);
                listAngkutan.add( cursor.getString(1).toString() );
            }

            listAngkutanUmum.put("angkutan" + e, listAngkutan);

            // add simpul angkot
            listSimpulAngkot.add( Integer.parseInt(exp[e]) );

        }


        String replace_jalur = gabungSimpul_all.replace(",,", ","); //  ,1-5,,5-6,,6-7, => ,1-5,5-6,6-7,
        cursor = db.rawQuery("SELECT * FROM rute where simpul like '%" + replace_jalur + "%'", null);
        cursor.moveToFirst();
        cursor.moveToPosition(0);

        // ada 1 angkot yg melewati jalur dari awal sampek akhir
        if(cursor.getCount() > 0){

            String siAngkot = cursor.getString(1).toString();

            // get coordinate
            cursor = db.rawQuery("SELECT jalur FROM graph where simpul_awal = '" + simpulAwalDijkstra + "'", null);
            cursor.moveToFirst();
            String json_coordinate = cursor.getString(0).toString();

            // manipulating JSON
            JSONObject jObject = new JSONObject(json_coordinate);
            JSONArray jArrCoordinates = jObject.getJSONArray("coordinates");
            JSONArray latlngs = jArrCoordinates.getJSONArray(0);

            // first latlng
            Double lats = latlngs.getDouble(0);
            Double lngs = latlngs.getDouble(1);

//            mMap.addMarker(new MarkerOptions()
//                    .position(new LatLng(lats, lngs))
//                    .title("Angkot")
//                    .snippet(siAngkot)
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))).showInfoWindow();

            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lats, lngs))
                    .title("Angkot")
                    .snippet(siAngkot)).showInfoWindow();

            // die()
            return;
        }

        // ada 2 atau lebih angkot yg melewati jalur dari awal sampek akhir
        int banyakAngkot = 0;
        int indexUrut = 0;
        int indexSimpulAngkot = 1;
        int lengthAngkutan = listAngkutanUmum.size();
        Map<String, ArrayList> angkotFix = new HashMap<String, ArrayList>();

        for(int en = 0; en < lengthAngkutan; en++ ){

            // temporary sementara sebelum di retainAll()
            ArrayList<String> temps = new ArrayList<String>();
            for(int u = 0; u < listAngkutanUmum.get("angkutan0").size(); u++){
                temps.add( listAngkutanUmum.get("angkutan0").get(u).toString() );
            }

            if(en > 0 ){
                ArrayList listSekarang1 = listAngkutanUmum.get("angkutan0");
                ArrayList listSelanjutnya1 = listAngkutanUmum.get("angkutan" + en);

                // intersection
                listSekarang1.retainAll(listSelanjutnya1);

                if(listSekarang1.size() > 0){

                    listSimpulAngkot.remove(indexSimpulAngkot);
                    --indexSimpulAngkot;

                    listAngkutanUmum.remove("angkutan" + en);

                    if(en == (lengthAngkutan - 1)){

                        ArrayList<String> tempDalam = new ArrayList<String>();
                        for(int es = 0; es < listSekarang1.size(); es++){
                            tempDalam.add( listSekarang1.get(es).toString() );
                        }

                        angkotFix.put("angkutanFix" + indexUrut, tempDalam);
                        ++indexUrut;
                    }
                }
                else if(listSekarang1.size() == 0){

                    angkotFix.put("angkutanFix" + indexUrut, temps);

                    ArrayList<String> tempDalam = new ArrayList<String>();
                    for(int es = 0; es < listSelanjutnya1.size(); es++){
                        tempDalam.add( listSelanjutnya1.get(es).toString() );
                    }

                    //if(en == 1) break;
                    listAngkutanUmum.get("angkutan0").clear();
                    listAngkutanUmum.put("angkutan0", tempDalam);

                    //if(en != (listAngkutanUmum.size() - 1)){
                    listAngkutanUmum.remove("angkutan" + en);
                    //}

                    ++indexUrut;

                    if(en == (lengthAngkutan - 1)){

                        ArrayList<String> tempDalam2 = new ArrayList<String>();
                        for(int es = 0; es < listSelanjutnya1.size(); es++){
                            tempDalam2.add( listSelanjutnya1.get(es).toString() );
                        }

                        angkotFix.put("angkutanFix" + indexUrut, tempDalam2);
                        ++indexUrut;
                    }
                }

                ++indexSimpulAngkot;
            }
        }

        for(int r = 0; r < listSimpulAngkot.size(); r++){
            String simpulx = listSimpulAngkot.get(r).toString();
            // get coordinate simpulAngkutan
            cursor = db.rawQuery("SELECT jalur FROM graph where simpul_awal = '" + simpulx + "'", null);
            cursor.moveToPosition(0);

            // dapatkan koordinat Lat,Lng dari field koordinat (3)
            String json = cursor.getString(0).toString();

            // get JSON
            JSONObject jObject = new JSONObject(json);
            JSONArray jArrCoordinates = jObject.getJSONArray("coordinates");

            // get first coordinate JSON
            JSONArray latlngs = jArrCoordinates.getJSONArray(0);
            Double lats = latlngs.getDouble(0);
            Double lngs = latlngs.getDouble(1);

            LatLng simpulAngkot = new LatLng(lats, lngs);
            String siAngkot = angkotFix.get("angkutanFix" + r).toString();

            if(r == 0){
                mMap.addMarker(new MarkerOptions()
                        .position(simpulAngkot)
                        .title("Angkot")
                        .snippet(siAngkot)).showInfoWindow();
            }else{
                mMap.addMarker(new MarkerOptions()
                        .position(simpulAngkot)
                        .title("Angkot")
                        .snippet(siAngkot));
            }
        }

    }

    public void getSimpulAwalAkhirJalur(KoordinatAwalAkhir objects, double latx, double lngx, String statusObject) throws JSONException{

        // return JSON index posisi koordinat, nodes0, nodes1
        JSONObject jStart = objects.getSimpul(latx, lngx, this);

        // index JSON
        String status = jStart.getString("status");
        int node_simpul_awal0 = jStart.getInt("node_simpul_awal0");
        int node_simpul_awal1 = jStart.getInt("node_simpul_awal1");
        int index_coordinate_json = jStart.getInt("index_coordinate_json");


        int fix_simpul_awal = 0;

        // jika koordinat tepat di atas posisi simpul/node
        // maka tidak perlu menambahkan simpul baru
        if(status.equals("jalur_none")){

            //tentukan simpul awal atau akhir yg dekat dgn posisi user
            if(index_coordinate_json == 0){ // awal
                fix_simpul_awal = node_simpul_awal0;
            }else{ // akhir
                fix_simpul_awal = node_simpul_awal1;
            }

            if(statusObject == "awal"){

                // return
                __global_old_simpul_awal = node_simpul_awal0 + "-" + node_simpul_awal1;
                __global_simpul_awal = fix_simpul_awal; // misal 0
            }else{

                // return
                __global_old_simpul_akhir = node_simpul_awal0 + "-" + node_simpul_awal1;
                __global_simpul_akhir = fix_simpul_awal; // misal 0
            }


        }
        // jika koordinat berada diantara simpul 5 dan simpul 4 atau simpul 4 dan simpul 5
        // maka perlu menambahkan simpul baru
        else if(status.equals("jalur_double")){

            // return
            if(statusObject == "awal"){

                // cari simpul (5,4) dan (4-5) di Tambah_simpul.java
                TambahSimpul obj_tambah = new TambahSimpul();
                obj_tambah.dobelSimpul(node_simpul_awal0, node_simpul_awal1, index_coordinate_json,
                        this, __global_graphArray, 401
                ); // 401 : row id yg baru


                // return
                __global_old_simpul_awal = obj_tambah.simpul_lama;
                __global_simpul_awal = obj_tambah.simpul_baru; // misal 6
                __global_graphArray = obj_tambah.modif_graph; // graph[][]

            }else{

                // cari simpul (5,4) dan (4-5) di Tambah_simpul.java
                TambahSimpul obj_tambah = new TambahSimpul();
                obj_tambah.dobelSimpul(node_simpul_awal0, node_simpul_awal1, index_coordinate_json,
                        this, __global_graphArray, 501
                ); // 501 : row id yg baru


                // return
                __global_old_simpul_akhir = obj_tambah.simpul_lama;
                __global_simpul_akhir = obj_tambah.simpul_baru; // misal 4
                __global_graphArray = obj_tambah.modif_graph; // graph[][]

            }

        }
        // jika koordinat hanya berada diantara simpul 5 dan simpul 4
        // maka perlu menambahkan simpul baru
        else if(status.equals("jalur_single")){

            if(statusObject == "awal"){

                // cari simpul (5,4) di Tambah_simpul.java
                TambahSimpul obj_tambah1 = new TambahSimpul();
                obj_tambah1.singleSimpul(node_simpul_awal0, node_simpul_awal1, index_coordinate_json,
                        this, __global_graphArray, 401
                ); // 401 : row id yg baru


                // return
                __global_old_simpul_awal = obj_tambah1.simpul_lama;
                __global_simpul_awal = obj_tambah1.simpul_baru; // misal 6
                __global_graphArray = obj_tambah1.modif_graph; // graph[][]

            }else{

                // cari simpul (5,4) di Tambah_simpul.java
                TambahSimpul obj_tambah1 = new TambahSimpul();
                obj_tambah1.singleSimpul(node_simpul_awal0, node_simpul_awal1, index_coordinate_json,
                        this, __global_graphArray, 501
                ); // 501 : row id yg baru


                // return
                __global_old_simpul_akhir = obj_tambah1.simpul_lama;
                __global_simpul_akhir = obj_tambah1.simpul_baru; // misal 4
                __global_graphArray = obj_tambah1.modif_graph; // graph[][]
            }
        }
    }




    /*
     * @fungsi
     *  delete temporary record DB
     *  (temporary ini digunakan untuk menampung sementara simpul baru)
     * @parameter
     *  no parameter
     * @return
     *  no returen
     */
    public void deleteTemporaryRecord(){
        // delete DB
        final SQLiteDatabase dbDelete = dbHelper.getWritableDatabase();

        // delete temporary record DB
        for(int i = 0; i < 4; i++){
            //hapus simpul awal tambahan, mulai dr id 401,402,403,404
            String deleteQuery_ = "DELETE FROM graph where id ='"+ (401+i) +"'";
            dbDelete.execSQL(deleteQuery_);

            //hapus simpul tujuan tambahan, mulai dr id 501,502,503,504
            String deleteQuery = "DELETE FROM graph where id ='"+ (501+i) +"'";
            dbDelete.execSQL(deleteQuery);
        }
    }

    public void maxRowDB(){
        dbHelper = new SQLHelper(this);
        SQLiteDatabase dbRead = dbHelper.getReadableDatabase();

        cursor = dbRead.rawQuery("SELECT max(simpul_awal), max(simpul_tujuan) FROM graph", null);
        cursor.moveToFirst();
        int max_simpul_db		= 0;
        int max_simpulAwal_db 	= Integer.parseInt(cursor.getString(0).toString());
        int max_simpulTujuan_db = Integer.parseInt(cursor.getString(1).toString());

        if(max_simpulAwal_db >= max_simpulTujuan_db){
            max_simpul_db = max_simpulAwal_db;
        }else{
            max_simpul_db = max_simpulTujuan_db;
        }

        // return
        __global_maxRow0 = (max_simpul_db+1);
        __global_maxRow1 = (max_simpul_db+2);
    }
}
