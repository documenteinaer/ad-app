package upb.airdocs;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class MagneticScan {
    private static final String LOG_TAG = "MagneticScan";
    private SensorManager sensorManager;
    private Sensor sensor;
    private Context mContext;

    public MagneticScan(Context context){
        mContext = context;
    }

    public void startScan(){
        sensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);

        sensorManager.registerListener(sensorEventListenerMagneticField, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopScan(){
        sensorManager.unregisterListener(sensorEventListenerMagneticField);
    }

    SensorEventListener sensorEventListenerMagneticField = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float magneticX = event.values[0];
            float magneticY = event.values[1];
            float magneticZ = event.values[2];
            double magneticI = Math.sqrt(magneticX*magneticX + magneticY*magneticY + magneticZ*magneticZ);
            Log.d(LOG_TAG, "magneticX=" + magneticX + " magneticY=" + magneticY
                    + " magneticZ=" + magneticZ+ " magneticI=" + magneticI);
            MagneticFingerprint magneticFingerprint = new MagneticFingerprint(magneticX, magneticY,
                                                           magneticZ, magneticI);
            ScanService.currentFingerprint.addMagneticFingerprint(magneticFingerprint);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}
