package upb.airdocs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class WifiFingerprint {
    private String ssid;
    private int frequency;
    private int rssi;


    public WifiFingerprint(String ssid, int frequency, int rssi) {
        this.ssid = ssid;
        this.frequency = frequency;
        this.rssi = rssi;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return "{ssid='" + ssid + '\'' +
                ", frequency=" + frequency +
                ", rssi=" + rssi +
                '}';
    }

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            jsonObject.put("ssid", ssid);
            jsonObject.put("frequency", String.valueOf(frequency));
            jsonArray.put(String.valueOf(rssi));
            jsonObject.put("rssi", jsonArray);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return jsonObject;
    }
}
