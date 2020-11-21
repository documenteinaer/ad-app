package upb.airdocs;

import java.util.ArrayList;

public class BLEFingerprint {
    private long timestamp;
    private String ssid;
    private int rssi;

    public BLEFingerprint(long timestamp, String ssid, int rssi) {
        this.timestamp = timestamp;
        this.ssid = ssid;
        this.rssi = rssi;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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
        return "{timestamp=" + timestamp +
                ", ssid='" + ssid + '\'' +
                ", rssi=" + rssi +
                '}';
    }
}
