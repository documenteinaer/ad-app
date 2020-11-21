package upb.airdocs;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BLEFingerprint {
    private long timestamp;
    private String name;
    private int rssi;

    public BLEFingerprint(long timestamp, String name, int rssi) {
        this.timestamp = timestamp;
        this.name = name;
        this.rssi = rssi;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return "{timestamp=" + timestamp +
                ", name='" + name + '\'' +
                ", rssi=" + rssi +
                '}';
    }

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("timestamp", String.valueOf(timestamp));
            jsonObject.put("name", name);
            jsonObject.put("rssi", String.valueOf(rssi));
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return jsonObject;
    }
}
