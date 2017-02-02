package com.mafiagames.empregoja;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class EmpregoDetalheActivity extends AppCompatActivity {



    private WebView mWebView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_emprego_detalhe);


        // 1
        String title = this.getIntent().getExtras().getString("title");
        String url = this.getIntent().getExtras().getString("url");

        setTitle(title);



        mWebView = (WebView) findViewById(R.id.detail_web_view);
        mWebView.setWebViewClient(new WebViewClient());

        mWebView.loadUrl(url);


    }
}
