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
import android.telephony.CellLocation;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
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
                Log.d(LOG_TAG, "Cell info: " + cellInfo.toString());
                int cid = getCid(cellInfo);
                if (cid != 2147483647) {
                    int rssi = getRssi(cellInfo);
                    int type = getType(cellInfo);
                    int mcc = getMCC(cellInfo);
                    int mnc = getMNC(cellInfo);
                    Log.d(LOG_TAG, "type=" + type + " cid=" + cid +
                            " rssi=" + rssi + " mcc=" + mcc + " mnc=" + mnc);
                    TelephonyFingerprint telephonyFingerprint = new TelephonyFingerprint(type, cid, rssi, mcc, mnc);
                    ScanService.currentFingerprint.addTelephonyFingerprint(telephonyFingerprint);
                }
            }


        }

        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(final SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);


                if(Build.VERSION.SDK_INT < 29 ) {
                    Log.d(LOG_TAG, signalStrength.toString());

                    if (signalStrength.isGsm()) {
                        int mSignalStrength = (2 * signalStrength.getGsmSignalStrength()) - 113; // -> dBm
                        Log.d(LOG_TAG, "dbm of current connection: " + mSignalStrength);
                    }

                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();

                        for (CellInfo cellInfo : cellInfos) {
                            Log.d(LOG_TAG, "Cell info: " + cellInfo.toString());
                            int cid = getCid(cellInfo);
                            if (cid != 2147483647) {
                                int rssi = getRssi(cellInfo);
                                int type = getType(cellInfo);
                                int mcc = getMCC(cellInfo);
                                int mnc = getMNC(cellInfo);
                                Log.d(LOG_TAG, "type=" + type + " cid=" + cid +
                                        " rssi=" + rssi + " mcc=" + mcc + " mnc=" + mnc);
                                TelephonyFingerprint telephonyFingerprint = new TelephonyFingerprint(type, cid, rssi, mcc, mnc);
                                ScanService.currentFingerprint.addTelephonyFingerprint(telephonyFingerprint);
                            }
                        }
                    }


                }
                if(Build.VERSION.SDK_INT >= 29 ) {
                    TelephonyManager.CellInfoCallback cellInfoCallback = new TelephonyManager.CellInfoCallback() {
                        @Override
                        public void onCellInfo(List<CellInfo> cellInfo) {

                            for (CellInfo info : cellInfo) {
                                Log.d(LOG_TAG, "Cell info: " + info.toString());
                                int cid = getCid(info);

                                if (cid != 2147483647) {
                                    int rssi = getRssi(info);
                                    int type = getType(info);
                                    int mcc = getMCC(info);
                                    int mnc = getMNC(info);
                                    Log.d(LOG_TAG, "type=" + type + " cid=" + cid +
                                            " rssi=" + rssi + " mcc=" + mcc + " mnc=" + mnc);
                                    TelephonyFingerprint telephonyFingerprint = new TelephonyFingerprint(type, cid, rssi, mcc, mnc);
                                    ScanService.currentFingerprint.addTelephonyFingerprint(telephonyFingerprint);
                                }
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

        int cid = -1;
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        rssi = cellInfoLte.getCellSignalStrength().getRssi();
                        Log.d(LOG_TAG, "getRssi: "+rssi+" getDbm: "+cellInfoLte.getCellSignalStrength().getDbm());
                    }
                    else{
                        rssi = cellInfoLte.getCellSignalStrength().getDbm();
                    }
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
        TYPE = 2 for WCDMA
        TYPE = 3 for CDMA
        TYPE = 4 for TDSCDMA
        TYPE = 5 for LTE
         */

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (cellInfo instanceof CellInfoGsm) {
                    return 1;
                } else if (cellInfo instanceof CellInfoLte) {
                    return 5;
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                        && cellInfo instanceof CellInfoWcdma) {
                    return 2;
                } else if (cellInfo instanceof CellInfoCdma) {
                    return 3;
                } /*else if (cellInfo instanceof CellInfoTdscdma) {
                    return 4;
                }*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    private static int getMCC(CellInfo cellInfo) {
        // Only for Gsm, Wcdma and LTE

        int mcc = -1;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (cellInfo instanceof CellInfoGsm) {
                    CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                    mcc = cellInfoGsm.getCellIdentity().getMcc();
                } else if (cellInfo instanceof CellInfoLte) {
                    CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                    mcc = cellInfoLte.getCellIdentity().getMcc();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                        && cellInfo instanceof CellInfoWcdma) {
                    CellInfoWcdma cellInfoLte = (CellInfoWcdma) cellInfo;
                    mcc = cellInfoLte.getCellIdentity().getMcc();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mcc;
    }

    private static int getMNC(CellInfo cellInfo) {
        // Only for Gsm, Wcdma and LTE

        int mnc = -1;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (cellInfo instanceof CellInfoGsm) {
                    CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                    mnc = cellInfoGsm.getCellIdentity().getMnc();
                } else if (cellInfo instanceof CellInfoLte) {
                    CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                    mnc = cellInfoLte.getCellIdentity().getMnc();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                        && cellInfo instanceof CellInfoWcdma) {
                    CellInfoWcdma cellInfoLte = (CellInfoWcdma) cellInfo;
                    mnc = cellInfoLte.getCellIdentity().getMnc();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mnc;
    }

}
