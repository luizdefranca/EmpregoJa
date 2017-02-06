package com.mafiagames.empregoja;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
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
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String cidade;
    private boolean hasCidadeLocalizada = false;
    private GoogleApiClient googleApiClient;
    private Location mLastLocation;
    private Button btnListarVagas;
    private ImageButton btnLocalizarCidade;
    private EditText textCidade;
    private EditText textTipoVaga;
    private ProgressDialog progressDialog;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int ACTIVATE_LOCATION_ACTIVITY_RESULT = 2;
    private static final String LOG_LOCATION_MANAGER = "LOCATION_MANAGER";
    private static final String LOG_APP = "MAIN_ACTIVITY";
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

    private void alertAtivarLocalizacao() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent locationIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(locationIntent, ACTIVATE_LOCATION_ACTIVITY_RESULT);
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        AlertDialog alert = builder.setMessage("Por favor, ative sua localização para usar esse aplicativo.")
                .setPositiveButton("Ativar", dialogClickListener)
                .create();
        alert.show();

        /*alert.setOnDismissListener(new AlertDialog.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                progressDialog.dismiss();
            }
        });*/
    }

    private void localizarCidade() {
        
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
        } catch (Exception e) {
            Log.d(LOG_LOCATION_MANAGER, e.getMessage());
        }

        if (addresses == null || addresses.isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(MainActivity.this, "Não foi possível encontrar sua localização. Por favor, digite a cidade.", Toast.LENGTH_SHORT).show();
        } else {
            // Encontramos a cidade usando o localizador
            cidade = addresses.get(0).getAddressLine(1);

            hasCidadeLocalizada = true;

            textCidade.setText(cidade);
            textCidade.setEnabled(false);
        }

        if (cidade != null && !cidade.isEmpty()) {
            textTipoVaga.requestFocus();
        } else {
            textCidade.requestFocus();
        }

        progressDialog.dismiss();
    }

    private void verificarCidade() {

        // A cidade não foi localizada automaticamente. Vamos ver se o usuário digitou algo
        if (!hasCidadeLocalizada) {
            cidade = textCidade.getText().toString();
            Log.d(LOG_APP, cidade);
        }

    }

    private void initLocalizarCidade() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean isProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(!isProviderEnabled) {
            alertAtivarLocalizacao();
        } else {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Carregando...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();

            // Se a Google API Client nao estiver conectada, conectamos
            if (!googleApiClient.isConnected()) {
                googleApiClient.connect();
            } else {

                // Se estiver, vamos apenas localizar a cidade
                localizarCidade();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnListarVagas = (Button) findViewById(R.id.btnListarVagas);
        btnLocalizarCidade = (ImageButton) findViewById(R.id.btnLocalizarCidade);
        textCidade = (EditText) findViewById(R.id.cidade);
        textTipoVaga = (EditText) findViewById(R.id.tipoVaga);

        btnLocalizarCidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Verificamos se temos permissão para acessar localização exata ou aproximada
                if (!hasPermissaoLocalizacaoExata() || !hasPermissaoLocalizacaoAproximada()) {

                    // Se não temos nenhuma permissão, pedimos permissão para localização exata
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                } else {

                    initLocalizarCidade();
                }
            }
        });

        btnListarVagas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                verificarCidade();

                if (!cidade.isEmpty()) {
                    listJobs(view);
                } else {
                    Toast.makeText(MainActivity.this, "É necessário definir uma cidade antes de realizar a busca", Toast.LENGTH_SHORT).show();
                }
            }
        });

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ACTIVATE_LOCATION_ACTIVITY_RESULT) {
            initLocalizarCidade();
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

                    initLocalizarCidade();
                } else {
                    Toast.makeText(MainActivity.this, "Esse aplicativo precisa de permissão para localizar sua cidade.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        } catch (SecurityException ex) {
            Log.d(LOG_LOCATION_MANAGER, ex.getMessage());
        }

        localizarCidade();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_LOCATION_MANAGER, "Conexão suspensa");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.d(LOG_LOCATION_MANAGER, "Conexão falhou");

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
