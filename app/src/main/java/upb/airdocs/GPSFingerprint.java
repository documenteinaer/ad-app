package upb.airdocs;


import org.json.JSONException;
import org.json.JSONObject;

class GPSFingerprint {
    private double latitude;
    private double longitude;

    public GPSFingerprint( double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "{latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("latitude", String.valueOf(latitude));
            jsonObject.put("longitude", String.valueOf(longitude));
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return jsonObject;
    }
}
