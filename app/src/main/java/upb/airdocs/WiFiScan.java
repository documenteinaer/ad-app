package upb.airdocs;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;
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
    boolean stop = false;


    public WiFiScan(Context context){
        mContext = context;
    }

    public boolean startScan(){

        stop = false;

        LocationManager locationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            Toast.makeText(mContext,
                    "Network Provider disabled. Cannot start scan. Please enable Location Services.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }


        //Log.d(LOG_TAG, "Obtaining the WiFi Manager");
        mWifiManager = (WifiManager)
                mContext.getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager.isWifiEnabled()) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            mContext.registerReceiver(wifiScanReceiver, intentFilter);

            if (mWifiManager != null) {
                Log.d(LOG_TAG, "WiFi Manager is not null, start scan");
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
                Log.d(LOG_TAG, "WiFi Scanned successfully");
                scanSuccess();
            } else {
                // scan failure handling
                Log.d(LOG_TAG, "WiFi Scan failed");
            }

            if (mWifiManager != null && stop != true) {
                Log.d(LOG_TAG, "WiFi Manager is not null, start scan");
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
        //WiFiRTTScan wiFiRTTScan = new WiFiRTTScan(mContext);
        //wiFiRTTScan.prepareForScan(results);

        ScanService.currentFingerprint.printToLogFingerprint();
        ScanService.currentFingerprintCollection.addFingerprintToCollection(ScanService.currentFingerprint);
        ScanService.numberOfScansInCollection++;
        ScanService.numberOfTotalScans++;
        displayNumberOfScans();
        ScanService.currentFingerprint = new Fingerprint();
        beep();
        if (ScanService.numberOfScansInCollection >= ScanService.scanLimit){
            stop = true;
            Log.d(LOG_TAG, "The number of fingerprints reached the configured limit. Stopping now.");
            ((ScanService)mContext).stopScan();
            ((ScanService)mContext).stopScanInActivity();
        }
    }

    private void beep(){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(mContext, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterReceiver(){
        mContext.unregisterReceiver(wifiScanReceiver);
    }

    private String getTimestamp(){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return formatter.format(date);
    }

    public void displayNumberOfScans(){
        Intent intent = new Intent("msg");
        intent.putExtra("message", ScanService.UPDATE_SCAN_NUMBERS);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }


}
