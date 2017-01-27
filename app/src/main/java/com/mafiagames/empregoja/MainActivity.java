package com.mafiagames.empregoja;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Network;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;
    private String cidade;
    private Gson gson;
    private ProgressDialog progressDialog;
    private ListView jobsList;
    private String jobsTitle[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Carregando...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        // Create an instance of GoogleAPIClient.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        Log.d("LOCATION MANAGER", String.valueOf(LocationServices.API));
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location mLastLocation = null;

        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        } catch (SecurityException ex) {
            Log.d("LOCATION MANAGER", ex.getMessage());
            Toast.makeText(this, "Um erro ocorreu ao capturar a localização.", Toast.LENGTH_LONG);
        }

        if (mLastLocation != null) {

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            cidade = addresses.get(0).getAddressLine(1).replace(" ", "").replace("-", "%2C+");

            getJobs();
        }
    }

    private void getJobs() {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://api.indeed.com/ads/apisearch?publisher=7462486830937105&v=2&format=json&co=br&q=java&l=" + cidade;

        jobsList = (ListView)findViewById(R.id.jobsList);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        gson = new Gson();
                        JsonWrapper jw = gson.fromJson(response, JsonWrapper.class);

                        jobsTitle = new String[jw.results.size()];

                        Log.d("EMPREGOS", String.valueOf(jw.results.size()));

                        for(int i=0; i < jw.results.size(); ++i){
                            Emprego e = jw.results.get(i);
                            jobsTitle[i] = e.jobtitle;
                        }

                        jobsList.setAdapter(new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, jobsTitle));
                        progressDialog.dismiss();
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("REQUEST", "That didn't work!");
                Toast.makeText(MainActivity.this, "Um erro ocorreu ao carregar as vagas de emprego.", Toast.LENGTH_LONG);
                progressDialog.dismiss();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("LOCATION MANAGER", "Conexão suspensa");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.d("LOCATION MANAGER", "Conexão falhou");

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }
}
