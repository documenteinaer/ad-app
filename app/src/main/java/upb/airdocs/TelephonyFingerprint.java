package upb.airdocs;

import org.json.JSONException;
import org.json.JSONObject;

public class TelephonyFingerprint {
    /* TYPE = 1 for GSM
TYPE = 2 for WCDMA
TYPE = 3 for CDMA
TYPE = 4 for TDSCDMA
TYPE = 5 for LTE
 */
    private int type;
    private int cid;
    private int rssi;
    private int mcc;
    private int mnc;

    public TelephonyFingerprint(int type, int cid, int rssi, int mcc, int mnc) {
        this.type = type;
        this.cid = cid;
        this.rssi = rssi;
        this.mcc = mcc;
        this.mnc = mnc;
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

    public int getMcc() {
        return mcc;
    }

    public void setMcc(int mcc) {
        this.mcc = mcc;
    }

    public int getMnc() {
        return mnc;
    }

    public void setMnc(int mnc) {
        this.mnc = mnc;
    }

    @Override
    public String toString() {
        return "TelephonyFingerprint{" +
                "type=" + type +
                ", cid=" + cid +
                ", rssi=" + rssi +
                ", mcc=" + mcc +
                ", mnc=" + mnc +
                '}';
    }

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", String.valueOf(type));
            jsonObject.put("cid", String.valueOf(cid));
            jsonObject.put("rssi", String.valueOf(rssi));
            jsonObject.put("mcc", String.valueOf(mcc));
            jsonObject.put("mnc", String.valueOf(mnc));
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return jsonObject;
    }
}
