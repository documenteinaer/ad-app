package upb.airdocs;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BLEFingerprint {
    private String name;
    private int rssi;

    public BLEFingerprint(String name, int rssi) {
        this.name = name;
        this.rssi = rssi;
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
        return "{name='" + name + '\'' +
                ", rssi=" + rssi +
                '}';
    }

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("rssi", String.valueOf(rssi));
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return jsonObject;
    }
}
