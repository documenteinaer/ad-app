package upb.airdocs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.text.SimpleDateFormat;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WiFiScan {
    private static final String LOG_TAG = "WiFiScan";
    private WifiManager mWifiManager;
    private Context mContext;

    public WiFiScan(Context context){
        mContext = context;
    }

    public boolean startScan(){
        //Log.d(LOG_TAG, "Obtaining the WiFi Manager");
        mWifiManager = (WifiManager)
                mContext.getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager.isWifiEnabled()) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            mContext.registerReceiver(wifiScanReceiver, intentFilter);

            if (mWifiManager != null) {
                //Log.d(LOG_TAG, "WiFi Manager is not null, start scan");
                mWifiManager.startScan();
            }
            return true;
        }
        else{
            Toast.makeText(mContext,
                    "WiFi is not enabled. Scan will not start.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    public BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {

            boolean success = intent.getBooleanExtra(
                    WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (success) {
                //Log.d(LOG_TAG, "WiFi Scanned successfully");
                scanSuccess();
            } else {
                // scan failure handling
                Log.d(LOG_TAG, "WiFi Scan failed");
            }

            if (mWifiManager != null) {
                //Log.d(LOG_TAG, "WiFi Manager is not null, start scan");
                mWifiManager.startScan();
            }

        }
    };


    private void scanSuccess() {
        List<ScanResult> results = mWifiManager.getScanResults();
        for (ScanResult result: results){
            //Log.d(LOG_TAG, "Timestamp: "+result.timestamp+" SSID: "+result.SSID+
            //        " MAC: "+result.BSSID+" Frequency: "+result.frequency+
            //        " RSSI: "+result.level);

            WifiFingerprint wifiItem = new WifiFingerprint(result.SSID,
                    result.frequency, result.level);

            ScanService.currentFingerprint.addWifiFingerprint(result.BSSID, wifiItem);
            ScanService.currentFingerprint.addTimestamp(getTimestamp());
        }

        ScanService.currentFingerprint.printToLogFingerprint();
        ScanService.currentFingerprintCollection.addFingerprintToCollection(ScanService.currentFingerprint);
        ScanService.currentFingerprint = new Fingerprint();
    }

    public void unregisterReceiver(){
        mContext.unregisterReceiver(wifiScanReceiver);
    }

    private String getTimestamp(){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return formatter.format(date);
    }


}
