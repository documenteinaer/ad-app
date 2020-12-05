package upb.airdocs;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
//import android.telephony.CellInfoTdscdma;
import android.telephony.CellInfoTdscdma;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class TelephonyScan {
    Context mContext;
    private static final String LOG_TAG = "TelephonyScan";

    private TelephonyManager telephonyManager;
    private PhoneStateListener mPhoneStateListener;
    //TelephonyManager.CellInfoCallback cellInfoCallback;


    public TelephonyScan(Context context){
        mContext = context;
    }

    public void startScan() {
        telephonyManager = (TelephonyManager) mContext.getApplicationContext()
                .getSystemService(Context.TELEPHONY_SERVICE);

        String networkName = telephonyManager.getNetworkOperatorName();
        String networkOperator = telephonyManager.getNetworkOperator();
        //Log.d(LOG_TAG, "Current network name: "+ networkName + " " + networkOperator);

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){


            List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
            //Log.d(LOG_TAG, "New data set:");

            for (CellInfo cellInfo : cellInfos) {
                //Log.d(LOG_TAG, "Cell info: " + cellInfo.toString());
                int cid = getCid(cellInfo);
                int rssi = getRssi(cellInfo);
                int type = getType(cellInfo);
                //Log.d(LOG_TAG, "type="+ type +" cid="+ cid +" rssi=" + rssi);
                TelephonyFingerprint telephonyFingerprint = new TelephonyFingerprint(type, cid, rssi);
                ScanService.currentFingerprint.addTelephonyFingerprint(telephonyFingerprint);
            }


        }

        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(final SignalStrength signalStrength) {

                if(Build.VERSION.SDK_INT < 29 ) {
                    Log.d(LOG_TAG, signalStrength.toString());

                    Log.i(LOG_TAG, "onSignalStrengthsChanged: " + signalStrength);
                    if (signalStrength.isGsm()) {
                        Log.i(LOG_TAG, "onSignalStrengthsChanged: getGsmBitErrorRate "
                                + signalStrength.getGsmBitErrorRate());
                        Log.i(LOG_TAG, "onSignalStrengthsChanged: getGsmSignalStrength "
                                + signalStrength.getGsmSignalStrength());
                    } else if (signalStrength.getCdmaDbm() > 0) {
                        Log.i(LOG_TAG, "onSignalStrengthsChanged: getCdmaDbm "
                                + signalStrength.getCdmaDbm());
                        Log.i(LOG_TAG, "onSignalStrengthsChanged: getCdmaEcio "
                                + signalStrength.getCdmaEcio());
                    } else {
                        Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoDbm "
                                + signalStrength.getEvdoDbm());
                        Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoEcio "
                                + signalStrength.getEvdoEcio());
                        Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoSnr "
                                + signalStrength.getEvdoSnr());
                    }
                    // Reflection code starts from here
                    try {
                        Method[] methods = android.telephony.SignalStrength.class
                                .getMethods();
                        for (Method mthd : methods) {
                            if (mthd.getName().equals("getLteSignalStrength")
                                    || mthd.getName().equals("getLteRsrp")
                                    || mthd.getName().equals("getLteRsrq")
                                    || mthd.getName().equals("getLteRssnr")
                                    || mthd.getName().equals("getLteCqi")) {
                                Log.i(LOG_TAG,
                                        "onSignalStrengthsChanged: " + mthd.getName() + " "
                                                + mthd.invoke(signalStrength));
                            }
                        }
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }

                }
                if(Build.VERSION.SDK_INT >= 29 ) {
                    TelephonyManager.CellInfoCallback cellInfoCallback = new TelephonyManager.CellInfoCallback() {
                        @Override
                        public void onCellInfo(List<CellInfo> cellInfo) {
                            //Log.d(LOG_TAG, "New data set:");
                            for (CellInfo info : cellInfo) {
                                //Log.d(LOG_TAG, "Cell info: " + info.toString());
                                int cid = getCid(info);
                                int rssi = getRssi(info);
                                int type = getType(info);
                                //Log.d(LOG_TAG, "type="+ type +" cid="+ cid +" rssi=" + rssi);
                                TelephonyFingerprint telephonyFingerprint = new TelephonyFingerprint(type, cid, rssi);
                                ScanService.currentFingerprint.addTelephonyFingerprint(telephonyFingerprint);
                            }
                        }
                    };

                    telephonyManager.requestCellInfoUpdate(mContext.getMainExecutor(), cellInfoCallback);
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

    public static int getCid(CellInfo cellInfo) {

        int cid = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (cellInfo instanceof CellInfoGsm) {
                    CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                    cid = cellInfoGsm.getCellIdentity().getCid();
                } else if (cellInfo instanceof CellInfoLte) {
                    CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                    cid = cellInfoLte.getCellIdentity().getCi();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                        && cellInfo instanceof CellInfoWcdma) {
                    CellInfoWcdma cellInfoLte = (CellInfoWcdma) cellInfo;
                    cid = cellInfoLte.getCellIdentity().getCid();
                } else if (cellInfo instanceof CellInfoCdma) {
                    CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                    cid = cellInfoCdma.getCellIdentity().getBasestationId();
                } /*else if (cellInfo instanceof CellInfoTdscdma) {
                    CellInfoTdscdma cellInfoTdscdma = (CellInfoTdscdma) cellInfo;
                    cid = cellInfoTdscdma.getCellIdentity().getCid();
                }*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cid;
    }

    public static int getRssi(CellInfo cellInfo) {

        int rssi = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (cellInfo instanceof CellInfoGsm) {
                    CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                    rssi = cellInfoGsm.getCellSignalStrength().getDbm();
                } else if (cellInfo instanceof CellInfoLte) {
                    CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                    //rssi = cellInfoLte.getCellSignalStrength().getRssi();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                        && cellInfo instanceof CellInfoWcdma) {
                    CellInfoWcdma cellInfoLte = (CellInfoWcdma) cellInfo;
                    rssi = cellInfoLte.getCellSignalStrength().getDbm();
                } else if (cellInfo instanceof CellInfoCdma) {
                    CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                    rssi = cellInfoCdma.getCellSignalStrength().getDbm();
                } /*else if (cellInfo instanceof CellInfoTdscdma) {
                    CellInfoTdscdma cellInfoTdscdma = (CellInfoTdscdma) cellInfo;
                    rssi = cellInfoTdscdma.getCellSignalStrength().getDbm();
                }*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rssi;
    }

    public static int getType(CellInfo cellInfo) {

        /* TYPE = 1 for GSM
        TYPE = 2 for LTE
        TYPE = 3 for WCDMA
        TYPE = 4 for CDMA
        TYPE = 5 for TDSCDMA
         */

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (cellInfo instanceof CellInfoGsm) {
                    return 1;
                } else if (cellInfo instanceof CellInfoLte) {
                    return 2;
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                        && cellInfo instanceof CellInfoWcdma) {
                    return 3;
                } else if (cellInfo instanceof CellInfoCdma) {
                    return 4;
                } /*else if (cellInfo instanceof CellInfoTdscdma) {
                    return 5;
                }*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

}
