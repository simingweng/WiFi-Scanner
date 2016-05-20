package com.simon.android.wifiscanner;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final int REQUEST_SCAN_ALWAYS_AVAILABLE = 0;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 1;
    private WifiManager wifiManager;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        receiver = new WifiScanResultReceiver();
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiver, filter);
        if(wifiManager.isScanAlwaysAvailable() || wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED || wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING){
            Log.i(TAG, "start WiFi scanning");
            wifiManager.startScan();
        }else {
            Log.i(TAG, "request user to allow WiFi Scan Mode");
            Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
            startActivityForResult(intent, REQUEST_SCAN_ALWAYS_AVAILABLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_SCAN_ALWAYS_AVAILABLE:
                if(resultCode == RESULT_OK){
                    Log.i(TAG, "WiFi Scan Mode is allowed");
                    wifiManager.startScan();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_ACCESS_COARSE_LOCATION:
                Log.i(TAG, "permission has been granted to read WiFi scanning result");
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    for(ScanResult result : wifiManager.getScanResults()){
                        Log.i(TAG, result.toString());
                    }
                }
                Log.i(TAG, "start WiFi scanning after previous scanning is done");
                wifiManager.startScan();
                break;
        }
    }

    private class WifiScanResultReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "scan result becomes available");
            boolean isResultUpdated = true;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
            }else {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isResultUpdated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                }
                if(isResultUpdated){
                    Log.i(TAG, "scan result has changed");
                    for(ScanResult result : wifiManager.getScanResults()){
                        Log.i(TAG, result.toString());
                    }
                    Log.i(TAG, "start WiFi scanning after previous scanning is done");
                    wifiManager.startScan();
                }
            }
        }
    }
}
