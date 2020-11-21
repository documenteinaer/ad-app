package upb.airdocs;

import java.util.ArrayList;

public class WifiFingerprint {
    private long timestamp;
    private String ssid;
    private int frequency;
    private int rssi;


    public WifiFingerprint(long timestamp, String ssid, int frequency, int rssi) {
        this.timestamp = timestamp;
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
                ", frequency=" + frequency +
                ", rssi=" + rssi +
                '}';
    }
}
