package upb.airdocs;

import java.util.ArrayList;

public class ScanItem {
    // type = 1 for Wifi
    // type = 2 for BLE
    private int type;
    private long timestamp;
    private String ssid;
    private String hardwareAddress;
    private int frequency;
    private int rssi;

    public ArrayList<Integer> getRssiList() {
        return rssiList;
    }

    public void addToRssiList(int rssi) {
        this.rssiList.add(new Integer(rssi));
    }

    private ArrayList<Integer> rssiList = new ArrayList<Integer>();


    public ScanItem(int type, long timestamp, String ssid, String hardwareAddress, int frequency, int rssi) {
        this.type = type;
        this.timestamp = timestamp;
        this.ssid = ssid;
        this.hardwareAddress = hardwareAddress;
        this.frequency = frequency;
        this.rssi = rssi;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    public String getHardwareAddress() {
        return hardwareAddress;
    }

    public void setHardwareAddress(String hardwareAddress) {
        this.hardwareAddress = hardwareAddress;
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
                ", timestamp=" + timestamp +
                ", ssid='" + ssid + '\'' +
                ", frequency=" + frequency +
                ", rssi=" + rssi +
                '}';
    }
}
