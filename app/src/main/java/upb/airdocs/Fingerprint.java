package upb.airdocs;

import android.util.Log;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class Fingerprint{
    private static final String LOG_TAG = "Fingerprint";

    private Hashtable<String,WifiFingerprint> wifiFingerprintHashtable = new Hashtable<String,WifiFingerprint>();
    private Hashtable<String,ArrayList<BLEFingerprint>> bleFingerprintHashtable = new Hashtable<String,ArrayList<BLEFingerprint>>();
    private ArrayList<GPSFingerprint> gpsFingerprintArrayList = new ArrayList<GPSFingerprint>();

    public void addWifiFingerprint(String hwAddress, WifiFingerprint wifiFingerprint){
        wifiFingerprintHashtable.put(hwAddress, wifiFingerprint);
    }

    public void addBLEFingerprint(String hwAddress, BLEFingerprint bleFingerprint){
        ArrayList<BLEFingerprint> bleList = bleFingerprintHashtable.get(hwAddress);
        if (bleList == null)  bleList = new ArrayList<BLEFingerprint>();
        bleList.add(bleFingerprint);
        bleFingerprintHashtable.put(hwAddress, bleList);
    }

    public void addGPSFingerprint(GPSFingerprint gpsFingerprint){
        gpsFingerprintArrayList.add(gpsFingerprint);
    }




    void printToLogFingerprint(){
        Log.d(LOG_TAG, "Fingerprint: ");
        Log.d(LOG_TAG, "Wifi Fingerprint: ");
        Log.d(LOG_TAG, wifiFingerprintHashtable.toString());
        Log.d(LOG_TAG, "BLE Fingerprint: ");
        Log.d(LOG_TAG, bleFingerprintHashtable.toString());
        Log.d(LOG_TAG, "GPS Fingerprint: ");
        Log.d(LOG_TAG, gpsFingerprintArrayList.toString());
    }
}
