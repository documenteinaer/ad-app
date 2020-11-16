package upb.airdocs;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.List;

public class TelephonyScan {
    Context mContext;
    private static final String LOG_TAG = "TelephonyScan";

    private TelephonyManager telephonyManager;
    private PhoneStateListener mPhoneStateListener;
    TelephonyManager.CellInfoCallback cellInfoCallback;


    public TelephonyScan(Context context){
        mContext = context;
    }

    public void startScan() {
        telephonyManager = (TelephonyManager) mContext.getApplicationContext()
                .getSystemService(Context.TELEPHONY_SERVICE);

        String networkName = telephonyManager.getNetworkOperatorName();
        String networkOperator = telephonyManager.getNetworkOperator();
        Log.d(LOG_TAG, "Current network name: "+ networkName + " " + networkOperator);

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){


            List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
            Log.d(LOG_TAG, "New data set:");

            for (CellInfo cellInfo : cellInfos)
                Log.d(LOG_TAG, "Cell info: " + cellInfo.toString());
        }

        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(final SignalStrength signalStrength) {
                telephonyManager.requestCellInfoUpdate(mContext.getMainExecutor(), cellInfoCallback);
            }

        };

        cellInfoCallback = new TelephonyManager.CellInfoCallback() {
            @Override
            public void onCellInfo(List<CellInfo> cellInfo) {
                Log.d(LOG_TAG, "New data set:");
                for (CellInfo info : cellInfo) {
                    Log.d(LOG_TAG, "Cell info: " + info.toString());
                }
            }
        };

        /**
         *	Register TelephonyManager updates
         */
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

    }

    /**
     * Unregisters PhoneStateListener
     */
    public void unregisterPhoneStateManager() {
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

}
