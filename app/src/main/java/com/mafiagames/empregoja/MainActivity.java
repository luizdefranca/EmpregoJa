package com.mafiagames.empregoja;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;
    private Location mLastLocation;
    private String cidade;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final String INTENT_CIDADE = "CIDADE";
    public static final String INTENT_TIPO_VAGA = "TIPO_VAGA";

    private boolean hasPermissaoLocalizacaoExata() {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasPermissaoLocalizacaoAproximada() {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void setCidade() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        cidade = addresses.get(0).getAddressLine(1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        super.onStart();

        // Verificamos se temos permissão para acessar localização exata ou aproximada
        if (!hasPermissaoLocalizacaoExata() || !hasPermissaoLocalizacaoAproximada()) {

            // Se não temos nenhuma permissão, pedimos permissão para localização exata
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        } else {
            googleApiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    googleApiClient.connect();

                } else {

                    // A permissão não foi dada. O app ficará em loop até a permissão ser obtida
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        } catch (SecurityException ex) {
            Log.d("LOCATION MANAGER", ex.getMessage());
        }

        // Conseguimos pegar a localização
        //if (mLastLocation != null) {



            Button btnListarVagas = (Button) findViewById(R.id.btnListarVagas);
            btnListarVagas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (mLastLocation != null) {
                        // Vamos pegar a cidade
                        setCidade();

                        TextView viewCidade = (TextView) findViewById(R.id.cidade);
                        viewCidade.setText(cidade);

                        EditText tipoVaga = (EditText) findViewById(R.id.tipoVaga);
                        tipoVaga.requestFocus();

                        listJobs(view);
                    } else {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        Intent callGPSSettingIntent = new Intent(
                                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivity(callGPSSettingIntent);
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        AlertDialog mNoGpsDialog = builder.setMessage("Por favor ative sua localização para usar esse aplicativo.")
                                .setPositiveButton("Ativar", dialogClickListener)
                                .create();
                        mNoGpsDialog.show();
                    }
                }
            });

        //} else {

            //Toast.makeText(this, "Um erro ocorreu ao capturar a localização.", Toast.LENGTH_LONG).show();
        //}
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

    public void listJobs(View view) {
        Intent intent = new Intent(this, ListaEmpregosActivity.class);

        EditText tipoVaga = (EditText) findViewById(R.id.tipoVaga);
        String message = tipoVaga.getText().toString();

        intent.putExtra(INTENT_TIPO_VAGA, message);
        intent.putExtra(INTENT_CIDADE, cidade.replace(" ", "").replace("-", "%2C+"));

        startActivity(intent);
    }
}
