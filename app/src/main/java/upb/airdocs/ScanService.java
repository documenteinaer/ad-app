package upb.airdocs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ScanService extends Service {
    private static final String LOG_TAG = "ScanService";
    private static final Integer ID_FOREGROUND = 1890;
    public static final String CHANNEL_ID = "ScanServiceChannel";

    private final int PHONE_STATE_REQUEST = 1;

    public static Fingerprint currentFingerprint = new Fingerprint();
    public static FingerprintCollection currentFingerprintCollection = new FingerprintCollection();
    public static List<FingerprintCollection> collectionsList = new ArrayList<FingerprintCollection>();


    WiFiScan wiFiScan = new WiFiScan(this);
    BLEScan bleScan = new BLEScan(this);
    GPSScan gpsScan = new GPSScan(this);
    TelephonyScan telephonyScan = new TelephonyScan(this);

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    public static final int MSG_SEND = 1;
    public static final int MSG_START_SCAN = 2;
    public static final int MSG_STOP_SCAN = 3;

    public static String devId = null;

    public ScanService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "Starting Scan Service.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        devId = intent.getStringExtra("devID");
        Log.d(LOG_TAG,"DeviceID=" + devId);
        setForeground(intent);
        return (START_STICKY);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "Destroying Scan Service.");
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

    private void doScan() {
        currentFingerprintCollection.setDevId(devId);

        wiFiScan.startScan();

        bleScan.startScan();

        gpsScan.startScan();

        telephonyScan.startScan();

    }

    private void stopScan(){
        saveAndRenewCollection();

        if (telephonyScan != null) {
            telephonyScan.unregisterPhoneStateManager();
        }

        if (wiFiScan != null) {
            wiFiScan.unregisterReceiver();
        }
        if (bleScan != null) {
            bleScan.stopScan();
        }
        if (gpsScan != null) {
            gpsScan.stopScan();
        }
    }

    private void sendFingerprintsToServer(final String address, final String port){
        final JSONObject fingerprintCollectionsJSON = buildFinalJSON();
        try {
            Log.d(LOG_TAG, "Final JSON:");
            Log.d(LOG_TAG, fingerprintCollectionsJSON.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        new Thread(new Runnable() {
            public void run() {
                sendJSONtoServer(address, port, fingerprintCollectionsJSON);
            }
        }).start();
    }

    /* Install this mini JSON server: https://gist.github.com/nitaku/10d0662536f37a087e1b */

    public void sendJSONtoServer(String address, String port, JSONObject jsonObject){

        try {
            URL url = new URL("http://"+address+":"+port);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            //String jsonInputString = "{\"this\": \"is a test\", \"received\": \"ok\"}";
            String jsonInputString = jsonObject.toString();
            try(OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                Log.d(LOG_TAG, response.toString());

                collectionsList = new ArrayList<FingerprintCollection>();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Handler of incoming messages from clients.
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEND:
                    String address = ((ServerAddress)msg.obj).getAddress();
                    String port = ((ServerAddress)msg.obj).getPort();
                    Log.d(LOG_TAG, "address= " + address + " port=" + port);
                    sendFingerprintsToServer(address, port);
                    break;
                case MSG_START_SCAN:
                    String comment = (String)msg.obj;
                    currentFingerprintCollection.setComment(comment);
                    doScan();
                    break;
                case MSG_STOP_SCAN:
                    stopScan();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    private void saveAndRenewCollection(){
        if (currentFingerprintCollection != null){
                //Log.d(LOG_TAG, "-------Saving collection-------");
                //currentFingerprintCollection.printToLogFingerprint();
                //Log.d(LOG_TAG, "-------------------------------");
                collectionsList.add(currentFingerprintCollection);
                currentFingerprintCollection = new FingerprintCollection();
                currentFingerprint = new Fingerprint();

        }

        //printCollectionsList();
    }

    private JSONObject buildFinalJSON(){
        JSONObject jsonObject = new JSONObject();

        try{
            for (int i = 0; i < collectionsList.size(); i++) {
                FingerprintCollection fingerprintCollection = collectionsList.get(i);
                jsonObject.put("collection"+i, fingerprintCollection.toJSON());
            }
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return jsonObject;

    }

    private void printCollectionsList(){
        for (int i = 0; i < collectionsList.size(); i++) {
            Log.d(LOG_TAG, "------Print colection "+ i+"----------");
            FingerprintCollection fingerprintCollection = collectionsList.get(i);
            fingerprintCollection.printToLogFingerprint();
            Log.d(LOG_TAG, "---------------------------");
        }
    }

}
