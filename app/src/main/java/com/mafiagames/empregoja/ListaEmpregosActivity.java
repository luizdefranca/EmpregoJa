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


    private void showJobs() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://api.indeed.com/ads/apisearch?publisher=7462486830937105&v=2&format=json&co=br&q=" + tipoVaga + "&l=" + cidade;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        gson = new Gson();
                        JsonWrapper jw = gson.fromJson(response, JsonWrapper.class);
                        jobs = jw.results;

                        mTotalVagas = (TextView) findViewById(R.id.totalVagas);
                        mTotalVagas.setText("Mostrando de " + jw.start + " a " + jw.end + " de " + jw.totalResults + " vagas encontradas");

                        mListView = (ListView) findViewById(R.id.jobsList);

                        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                Emprego emprego = jobs.get(position);

                                Intent detailIntent = new Intent(ListaEmpregosActivity.this, EmpregoDetalheActivity.class);

                                detailIntent.putExtra("title", emprego.jobtitle);
                                detailIntent.putExtra("url", emprego.url);

                                startActivity(detailIntent);
                            }

                        });

                        EmpregoAdapter adapter = new EmpregoAdapter(ListaEmpregosActivity.this, jobs);
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

        Intent intent = getIntent();
        cidade = intent.getStringExtra(MainActivity.INTENT_CIDADE);
        tipoVaga = intent.getStringExtra(MainActivity.INTENT_TIPO_VAGA);

        progressDialog = new ProgressDialog(ListaEmpregosActivity.this);
        progressDialog.setMessage("Carregando...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        showJobs();
    }
}