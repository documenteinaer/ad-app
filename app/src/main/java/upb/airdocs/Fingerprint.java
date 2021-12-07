package upb.airdocs;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class Fingerprint{
    private static final String LOG_TAG = "Fingerprint";

    private String timestamp;

    private Hashtable<String,WifiFingerprint> wifiFingerprintHashtable = new Hashtable<String,WifiFingerprint>();
    private Hashtable<String,BLEFingerprint> bleFingerprintHashtable = new Hashtable<String,BLEFingerprint>();
    private ArrayList<GPSFingerprint> gpsFingerprintArrayList = new ArrayList<GPSFingerprint>();
    private ArrayList<TelephonyFingerprint> telephonyFingerprintArrayList = new ArrayList<TelephonyFingerprint>();

    public void addTimestamp(String timestamp){
        this.timestamp = timestamp;
    }

    public void addWifiFingerprint(String hwAddress, WifiFingerprint wifiFingerprint){
        wifiFingerprintHashtable.put(hwAddress, wifiFingerprint);
    }

    public void addBLEFingerprint(String hwAddress, String deviceName, int rssi){
        BLEFingerprint bleFingerprint = bleFingerprintHashtable.get(hwAddress);
        if (bleFingerprint == null) {
            bleFingerprint = new BLEFingerprint(deviceName, rssi);
        }
        else {
            bleFingerprint.addRssi(rssi);
        }
        bleFingerprintHashtable.put(hwAddress, bleFingerprint);
    }

    public void addGPSFingerprint(GPSFingerprint gpsFingerprint){
        gpsFingerprintArrayList.add(gpsFingerprint);
    }

    public void addTelephonyFingerprint(TelephonyFingerprint telephonyFingerprint){
        telephonyFingerprintArrayList.add(telephonyFingerprint);
    }




    public void printToLogFingerprint(){
        Log.d(LOG_TAG, "Fingerprint: ");
        Log.d(LOG_TAG, "Timestamp: "+ timestamp);
        if (!wifiFingerprintHashtable.isEmpty()) {
            Log.d(LOG_TAG, "Wifi Fingerprint: ");
            Log.d(LOG_TAG, wifiFingerprintHashtable.toString());
        }
        if (!bleFingerprintHashtable.isEmpty()) {
            Log.d(LOG_TAG, "BLE Fingerprint: ");
            Log.d(LOG_TAG, bleFingerprintHashtable.toString());
        }
        if (!gpsFingerprintArrayList.isEmpty()) {
            Log.d(LOG_TAG, "GPS Fingerprint: ");
            Log.d(LOG_TAG, gpsFingerprintArrayList.toString());
        }
        if (!telephonyFingerprintArrayList.isEmpty()) {
            Log.d(LOG_TAG, "Telephony Fingerprint: ");
            Log.d(LOG_TAG, telephonyFingerprintArrayList.toString());
        }
    }

    public JSONObject wifiFingerprintHashtableToJSON(){
        JSONObject wifiFingerprintJSON = new JSONObject();
        Enumeration<String> keys = wifiFingerprintHashtable.keys();
        while(keys.hasMoreElements()){
            String hwAddr = keys.nextElement();
            WifiFingerprint wifiFingerprint = wifiFingerprintHashtable.get(hwAddr);
            try {
                wifiFingerprintJSON.put(hwAddr, wifiFingerprint.toJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return wifiFingerprintJSON;
    }

    public JSONObject bleFingerprintHashtableToJSON(){
        JSONObject bleFingerprintJSON = new JSONObject();

        Enumeration<String> keys = bleFingerprintHashtable.keys();
        while(keys.hasMoreElements()){
            String hwAddr = keys.nextElement();
            BLEFingerprint bleFingerprint = bleFingerprintHashtable.get(hwAddr);
            try {
                bleFingerprintJSON.put(hwAddr, bleFingerprint.toJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return bleFingerprintJSON;
    }

    public JSONArray gpsFingerprintArrayListToJSON(){
        JSONArray gpsFingerprintJSON = new JSONArray();

        for (int i = 0; i < gpsFingerprintArrayList.size(); i++) {
            GPSFingerprint gpsFingerprint = gpsFingerprintArrayList.get(i);
            gpsFingerprintJSON.put(gpsFingerprint.toJSON());
        }

        return gpsFingerprintJSON;
    }

    public JSONArray telephonyFingerprintArrayListToJSON(){
        JSONArray telephonyFingerprintJSON = new JSONArray();

        for (int i = 0; i < telephonyFingerprintArrayList.size(); i++) {
            TelephonyFingerprint telephonyFingerprint = telephonyFingerprintArrayList.get(i);
            telephonyFingerprintJSON.put(telephonyFingerprint.toJSON());
        }

        return telephonyFingerprintJSON;
    }

    public JSONObject toJSON(boolean ble, boolean cellular, boolean gps){
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject wifiFingerprintJSON = wifiFingerprintHashtableToJSON();
            JSONObject bleFingerprintJSON = bleFingerprintHashtableToJSON();
            JSONArray gpsFingerprintJSON = gpsFingerprintArrayListToJSON();
            JSONArray telephonyFingerprintJSON = telephonyFingerprintArrayListToJSON();

            jsonObject.put("timestamp", timestamp);
            jsonObject.put("wifi", wifiFingerprintJSON);
            if (ble) {
                jsonObject.put("ble", bleFingerprintJSON);
            }
            if (gps) {
                jsonObject.put("gps", gpsFingerprintJSON);
            }
            if (cellular) {
                jsonObject.put("telephony", telephonyFingerprintJSON);
            }

            //Log.d(LOG_TAG, "Fingerprint JSON: "+ jsonObject.toString(4));

        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return jsonObject;
    }
}
