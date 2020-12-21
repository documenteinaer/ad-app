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
    private String id;

    private Hashtable<String,WifiFingerprint> wifiFingerprintHashtable = new Hashtable<String,WifiFingerprint>();
    private Hashtable<String,ArrayList<BLEFingerprint>> bleFingerprintHashtable = new Hashtable<String,ArrayList<BLEFingerprint>>();
    private ArrayList<GPSFingerprint> gpsFingerprintArrayList = new ArrayList<GPSFingerprint>();
    private ArrayList<TelephonyFingerprint> telephonyFingerprintArrayList = new ArrayList<TelephonyFingerprint>();

    public void addTimestamp(String timestamp){
        this.timestamp = timestamp;
    }
    public void addID(String id){
        this.id = id;
    }

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

    public void addTelephonyFingerprint(TelephonyFingerprint telephonyFingerprint){
        telephonyFingerprintArrayList.add(telephonyFingerprint);
    }




    void printToLogFingerprint(){
        Log.d(LOG_TAG, "Fingerprint: ");
        Log.d(LOG_TAG, "Timestamp: "+ timestamp);
        Log.d(LOG_TAG, "ID: "+ id);
        Log.d(LOG_TAG, "Wifi Fingerprint: ");
        Log.d(LOG_TAG, wifiFingerprintHashtable.toString());
        Log.d(LOG_TAG, "BLE Fingerprint: ");
        Log.d(LOG_TAG, bleFingerprintHashtable.toString());
        Log.d(LOG_TAG, "GPS Fingerprint: ");
        Log.d(LOG_TAG, gpsFingerprintArrayList.toString());
        Log.d(LOG_TAG, "Telephony Fingerprint: ");
        Log.d(LOG_TAG, telephonyFingerprintArrayList.toString());
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
            ArrayList<BLEFingerprint> bleFingerprintArrayList = bleFingerprintHashtable.get(hwAddr);
            JSONArray bleJSONArray = new JSONArray();
            for (int i = 0; i < bleFingerprintArrayList.size(); i++) {
                BLEFingerprint bleFingerprint = bleFingerprintArrayList.get(i);
                bleJSONArray.put(bleFingerprint.toJSON());
            }
            try {
                bleFingerprintJSON.put(hwAddr, bleJSONArray);
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

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject wifiFingerprintJSON = wifiFingerprintHashtableToJSON();
            JSONObject bleFingerprintJSON = bleFingerprintHashtableToJSON();
            JSONArray gpsFingerprintJSON = gpsFingerprintArrayListToJSON();
            JSONArray telephonyFingerprintJSON = telephonyFingerprintArrayListToJSON();

            jsonObject.put("timestamp", timestamp);
            jsonObject.put("id", id);
            jsonObject.put("wifi", wifiFingerprintJSON);
            jsonObject.put("ble", bleFingerprintJSON);
            jsonObject.put("gps", gpsFingerprintJSON);
            jsonObject.put("telephony",telephonyFingerprintJSON);

            //Log.d(LOG_TAG, "Fingerprint JSON: "+ jsonObject.toString(4));

        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return jsonObject;
    }
}
