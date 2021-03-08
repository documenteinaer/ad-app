package upb.airdocs;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BLEFingerprint {
    private String name;
    private ArrayList<Integer> rssiArray = new ArrayList<Integer>();

    public BLEFingerprint(String name, int rssi) {
        this.name = name;
        rssiArray.add(rssi);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Integer> getRssi() {
        return rssiArray;
    }

    public void addRssi(int rssi) {
        rssiArray.add(rssi);
    }

    @Override
    public String toString() {
        return "{name='" + name + '\'' +
                ", rssi=" + rssiArray.toString() +
                '}';
    }

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            if (name != null) jsonObject.put("name", name);
            for (int i = 0; i < rssiArray.size(); i++) {
                jsonArray.put(rssiArray.get(i).toString());
            }
            jsonObject.put("rssi", jsonArray);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return jsonObject;
    }
}
