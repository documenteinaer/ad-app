package upb.airdocs;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.AccessNetworkConstants;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.NetworkScanRequest;
import android.telephony.PhoneStateListener;
import android.telephony.RadioAccessSpecifier;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyScanManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ScanService extends Service {
    private static final String LOG_TAG = "ScanService";
    private static final Integer ID_FOREGROUND = 1890;
    public static final String CHANNEL_ID = "ScanServiceChannel";

    private final int PHONE_STATE_REQUEST = 1;

    TelephonyScan telephonyScan;

    public static List<Fingerprint> itemList = new ArrayList<Fingerprint>();
    public static Fingerprint currentFingerprint = new Fingerprint();

    WiFiScan wiFiScan = new WiFiScan(this);
    BLEScan bleScan = new BLEScan(this);
    GPSScan gpsScan = new GPSScan(this);


    public ScanService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "Starting Scan Service.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setForeground(intent);

        Log.d(LOG_TAG, "Scan service works hard here");
        doWork();

        return (START_STICKY);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "Destroying Scan Service.");
        //telephonyScan.unregisterPhoneStateManager();

        if (wiFiScan != null) {
            wiFiScan.unregisterReceiver();
        }
        if (bleScan != null) {
            bleScan.stopScan();
        }
        if (gpsScan != null) {
            gpsScan.stopScan();
        }

        stopForeground(true);
    }

    private void setForeground(Intent intent) {
        Log.d(LOG_TAG, "Set Foreground Service");
        //String input = intent.getStringExtra("inputExtra");
        String input = "Scan Service";
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(ID_FOREGROUND, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Scan Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void doWork() {

        wiFiScan.startScan();

        bleScan.startScan();

        gpsScan.startScan();

        //telephonyScan = new TelephonyScan(this);
        //telephonyScan.startScan();



    }


}
