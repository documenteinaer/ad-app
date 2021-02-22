package upb.airdocs;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;
import android.net.wifi.rtt.WifiRttManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class WiFiRTTScan {
    private static final String LOG_TAG = "WiFiRTTScan";
    private Context mContext;
    private WifiRttManager mWifiRttManager;
    private RttRangingResultCallback mRttRangingResultCallback;
    private int mNumberOfRangeRequests = 0;
    private int mMillisecondsDelayBeforeNewRangingRequest = 1000;
    private final Handler mRangeRequestDelayHandler = new Handler();
    private RangingRequest rangingRequest;
    private int maxPeers = 0;

    public WiFiRTTScan(Context context) {
        mContext = context;
    }

    public boolean prepareForScan(List<ScanResult> scanResults) {
        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_RTT)) {
            Log.d(LOG_TAG, "This device has the Wifi RTT feature");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mWifiRttManager = (WifiRttManager) mContext.getSystemService(Context.WIFI_RTT_RANGING_SERVICE);
                if (mWifiRttManager.isAvailable()) {
                    Log.d(LOG_TAG, "WiFi RTT is available");

                    mRttRangingResultCallback = new RttRangingResultCallback();

                    RangingRequest.Builder builder = new RangingRequest.Builder();
                    builder.addAccessPoints(scanResults);
                    rangingRequest = builder.build();

                    maxPeers = rangingRequest.getMaxPeers();

                    Log.d(LOG_TAG, "max peers: "+maxPeers);

                    if (startRangingRequest()){
                        return true;
                    }

                } else {
                    Log.d(LOG_TAG, "WiFi RTT is not available");
                    return false;
                }
            }
            else{
                return false;
            }
        }
        else{
            Log.d(LOG_TAG, "This device does not have Wifi RTT feature");
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private boolean startRangingRequest() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "Location permission is not granted");
            return false;
        }

        mNumberOfRangeRequests++;

        mWifiRttManager.startRanging(
                rangingRequest, mContext.getMainExecutor(), mRttRangingResultCallback);

        return true;
    }

    // Class that handles callbacks for all RangingRequests and issues new RangingRequests.
    @RequiresApi(api = Build.VERSION_CODES.P)
    private class RttRangingResultCallback extends RangingResultCallback {

        private void queueNextRangingRequest() {
            mRangeRequestDelayHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            startRangingRequest();
                        }
                    },
                    mMillisecondsDelayBeforeNewRangingRequest);
        }

        @Override
        public void onRangingFailure(int code) {
            Log.d(LOG_TAG, "onRangingFailure() code: " + code);
            queueNextRangingRequest();
        }

        @Override
        public void onRangingResults(@NonNull List<RangingResult> list) {
            Log.d(LOG_TAG, "onRangingResults(): " + list);

            /*if (mNumberOfRangeRequests < 4){
                queueNextRangingRequest();
            }*/
        }

    }
}
