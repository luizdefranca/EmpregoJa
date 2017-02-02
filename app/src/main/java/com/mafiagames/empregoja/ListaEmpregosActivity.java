package com.mafiagames.empregoja;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.Gson;

import java.util.ArrayList;

public class ListaEmpregosActivity extends AppCompatActivity {

    private String cidade;
    private String tipoVaga;
    private Gson gson;
    private ProgressDialog progressDialog;
    private ListView mListView;
    private TextView mTotalVagas;
    private ArrayList<Emprego> jobs;
    private int start = 1;
    private int totalVagas = 1;
    private EmpregoAdapter adapter;
    InterstitialAd mInterstitialAd;


    @Override
    protected void onResume() {
        super.onResume();

    }


    private void showJobs() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://api.indeed.com/ads/apisearch?publisher=5403611395821460&v=2&format=json&co=br&q=" + tipoVaga + "&l=" + cidade;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        gson = new Gson();
                        JsonWrapper jw = gson.fromJson(response, JsonWrapper.class);
                        jobs = jw.results;
                        totalVagas = jw.totalResults;

                        mTotalVagas = (TextView) findViewById(R.id.totalVagas);
                        mTotalVagas.setText(jw.totalResults + " vagas encontradas");

                        adapter = new EmpregoAdapter(ListaEmpregosActivity.this, jobs);
                        mListView.setAdapter(adapter);

                        progressDialog.dismiss();
                    }

                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("REQUEST", "That didn't work!");
                        Toast.makeText(ListaEmpregosActivity.this, "Um erro ocorreu ao carregar as vagas de emprego.", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
        );

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_empregos);
        Log.d("LISTAEMPREGOSACTIVITY", "onCreate");

        Intent intent = getIntent();
        cidade = intent.getStringExtra(MainActivity.INTENT_CIDADE);
        tipoVaga = intent.getStringExtra(MainActivity.INTENT_TIPO_VAGA);

        progressDialog = new ProgressDialog(ListaEmpregosActivity.this);
        progressDialog.setMessage("Carregando...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-8971294493511091/2109005569");



        mListView = (ListView) findViewById(R.id.jobsList);



        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Emprego emprego = jobs.get(position);

                Intent detailIntent = new Intent(ListaEmpregosActivity.this, EmpregoDetalheActivity.class);

                detailIntent.putExtra("title", emprego.jobtitle);
                detailIntent.putExtra("url", emprego.url);

                startActivity(detailIntent);
                AdView mAdView = (AdView) findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
                mInterstitialAd.loadAd(adRequest);
                mInterstitialAd.show();

            }

        });

        // Attach the listener to the AdapterView onCreate
        mListView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                if (totalItemsCount < totalVagas) {
                    loadNextDataFromApi();
                    return true;
                }

                return false;
            }
        });

        showJobs();
    }

    public void loadNextDataFromApi() {
        start += 10;

        progressDialog = new ProgressDialog(ListaEmpregosActivity.this);
        progressDialog.setMessage("Carregando...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://api.indeed.com/ads/apisearch?publisher=5403611395821460&v=2&format=json&co=br&q=" + tipoVaga + "&l=" + cidade + "&start=" + start;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        gson = new Gson();
                        JsonWrapper jw = gson.fromJson(response, JsonWrapper.class);
                        jobs.addAll(jw.results);

                        adapter.notifyDataSetChanged();

                        progressDialog.dismiss();
                    }

                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("REQUEST", "That didn't work!");
                        Toast.makeText(ListaEmpregosActivity.this, "Um erro ocorreu ao carregar mais vagas de emprego.", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
        );

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
