package upb.airdocs;

import org.json.JSONException;
import org.json.JSONObject;

public class MagneticFingerprint {
    private float m_x;
    private float m_y;
    private float m_z;
    private double m_total;

    public MagneticFingerprint(float m_x, float m_y, float m_z, double m_total) {
        this.m_x = m_x;
        this.m_y = m_y;
        this.m_z = m_z;
        this.m_total = m_total;
    }

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("m_x", String.valueOf(m_x));
            jsonObject.put("m_y", String.valueOf(m_y));
            jsonObject.put("m_z", String.valueOf(m_z));
            jsonObject.put("m_total", String.valueOf(m_total));
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public String toString() {
        return "{m_x=" + m_x +
                ", m_y=" + m_y +
                ", m_z=" + m_z +
                ", m_total=" + m_total +
                '}';
    }
}
