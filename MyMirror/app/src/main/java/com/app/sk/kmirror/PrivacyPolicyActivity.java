package com.app.sk.kmirror;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class PrivacyPolicyActivity extends Activity {
    WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_policy);

        web =(WebView)findViewById(R.id.webView);
        web.loadUrl("file:///android_asset/privacy.html");

    }
}
