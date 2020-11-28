package upb.airdocs;

import org.json.JSONException;
import org.json.JSONObject;

public class TelephonyFingerprint {
    /* TYPE = 1 for GSM
TYPE = 2 for LTE
TYPE = 3 for WCDMA
TYPE = 4 for CDMA
TYPE = 5 for TDSCDMA
 */
    private int type;
    private int cid;
    private int rssi;

    public TelephonyFingerprint(int type, int cid, int rssi) {
        this.type = type;
        this.cid = cid;
        this.rssi = rssi;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return "{" +
                "type=" + type +
                ", cid=" + cid +
                ", rssi=" + rssi +
                '}';
    }

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", String.valueOf(type));
            jsonObject.put("cid", String.valueOf(cid));
            jsonObject.put("rssi", String.valueOf(rssi));
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return jsonObject;
    }
}
