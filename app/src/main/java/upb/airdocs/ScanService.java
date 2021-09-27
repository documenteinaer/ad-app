package upb.airdocs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
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
    AudioScan audioScan = new AudioScan(this);
    //WiFiScanNew wiFiScanNew = new WiFiScanNew(this);

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    public static final int MSG_SEND = 1;
    public static final int MSG_START_SCAN = 2;
    public static final int MSG_STOP_SCAN = 3;
    public static final int MSG_SCAN_TO_POST_DOC = 4;
    public static final int MSG_SCAN_TO_SEARCH_DOC = 5;
    public static final int MSG_SEND_DONE = 6;
    public static final int MSG_ACTUAL_SEND_DOC = 7;
    public static final int MSG_ACTUAL_SEARCH_DOC = 8;

    public static final int ACT_STOP_SCAN = 1;
    public static final int UPDATE_SCAN_NUMBERS = 2;
    public static final int UPDATE_SEND_STATUS = 3;
    public static final int ACT_STOP_SCAN_FAILED = 4;

    public static String devId = null;

    private boolean scanning = false;

    public static int numberOfScansInCollection = 0;
    public static int numberOfTotalScans = 0;
    public static int numberOfCollections = 0;
    public static int sent = -1;

    public static int scanLimit = 1;

    //Message type
    private static final int TYPE_TESTING = 0;
    private static final int TYPE_SEND_DOC = 1;
    private static final int TYPE_SEARCH_DOC = 2;

    String address;
    String port;
    int scan_no;
    String comment;
    float threshold;
    boolean ble, cellular, gps, audio;
    String docName;
    String file;
    String fileType;

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
        setForeground(intent);
        return (START_STICKY);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "Destroying Scan Service.");
        stopForeground(true);
    }

    private void getDevId(){
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        devId = sharedPref.getString("devID", null);
        Log.d(LOG_TAG, "devID="+devId);
    }

    private void setForeground(Intent intent) {
        Log.d(LOG_TAG, "Set Foreground Service");
        //String input = intent.getStringExtra("inputExtra");
        String input = "Scan Service";
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, UserActivity.class);
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
        numberOfScansInCollection = 0;
        getDevId();
        currentFingerprintCollection.setDevId(devId);
        numberOfCollections++;

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            scanning = wiFiScanNew.startScan();
        }
        else{
            scanning = wiFiScan.startScan();
        }*/

        scanning = wiFiScan.startScan();

        if (scanning) {
            if (audio) {
                audioScan.startScan();
            }
            if (ble) {
                bleScan.startScan();
            }
            if (gps) {
                gpsScan.startScan();
            }
            if (cellular) {
                telephonyScan.startScan();
            }
        }
        else{
            stopScanFailedInActivity();
        }

    }

    public void stopScan(){

        if (scanning) {
            scanning = false;
            saveAndRenewCollection();

            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (wiFiScanNew != null) {
                    wiFiScanNew.unregisterReceiver();
                }
            }
            else{
                if (wiFiScan != null) {
                    wiFiScan.unregisterReceiver();
                }
            }*/

            if (wiFiScan != null) {
                wiFiScan.unregisterReceiver();
            }

            if (audio && audioScan != null){
                audioScan.stopScan();
            }

            if (cellular && telephonyScan != null) {
                telephonyScan.unregisterPhoneStateManager();
            }

            if (ble && bleScan != null) {
                bleScan.stopScan();
            }
            if (gps && gpsScan != null) {
                gpsScan.stopScan();
            }
            numberOfScansInCollection = 0;
            displayNumberOfScans();
        }
    }

    private void sendFingerprintsToServer(final int type){
        final JSONObject fingerprintCollectionsJSON = buildFinalJSON(ble, cellular, gps);
        final JSONObject jsonObjectFinal = new JSONObject();
        try {
            if (type == TYPE_TESTING) {
                jsonObjectFinal.put("type", "TEST");
                jsonObjectFinal.put("fingerprints", fingerprintCollectionsJSON);
            }
            else if (type == TYPE_SEND_DOC){
                if (file != null && docName == null){
                    Log.d(LOG_TAG, "Failed due to null filename");
                    sent = 0;
                    displaySendStatus();
                    return;
                }
                jsonObjectFinal.put("type", "POST");
                jsonObjectFinal.put("document", docName);
                if (file != null){
                    jsonObjectFinal.put("file", file);
                }
                if (fileType != null){
                    jsonObjectFinal.put("filetype", fileType);
                }
                jsonObjectFinal.put("fingerprints", fingerprintCollectionsJSON);
            }
            else if (type == TYPE_SEARCH_DOC){
                jsonObjectFinal.put("type", "SEARCH");
                jsonObjectFinal.put("threshold", String.valueOf(threshold));
                jsonObjectFinal.put("fingerprints", fingerprintCollectionsJSON);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Log.d(LOG_TAG, "Final JSON:");
            Log.d(LOG_TAG, jsonObjectFinal.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            public void run() {
                sendJSONtoServer(jsonObjectFinal, type);
            }
        }).start();
    }

    /* Install this mini JSON server: https://gist.github.com/nitaku/10d0662536f37a087e1b */

    public void sendJSONtoServer(JSONObject jsonObject, int type){

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
            catch(Exception e) {
                sent = 0;
                e.printStackTrace();
                Log.d(LOG_TAG, "Failed - connect exception");
                if (type == TYPE_SEND_DOC || type == TYPE_SEARCH_DOC){
                    collectionsList = new ArrayList<FingerprintCollection>();
                    numberOfCollections = 0;
                    numberOfScansInCollection = 0;
                    numberOfTotalScans = 0;
                }
                displaySendStatus();
                return;
            }

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                Log.d(LOG_TAG, response.toString() + " " + type);

                if (response.toString().equals("<html><body><h1>Successful Testing</h1></body></html>") && (type == TYPE_TESTING)){
                        collectionsList = new ArrayList<FingerprintCollection>();
                        numberOfCollections = 0;
                        numberOfScansInCollection = 0;
                        numberOfTotalScans = 0;
                        displayNumberOfScans();
                        sent = 1;
                        Log.d(LOG_TAG, "Success (testing)");
                        displaySendStatus();
                }
                else if (response.toString().equals("<html><body><h1>Successful Sending</h1></body></html>") && type == TYPE_SEND_DOC ){
                        collectionsList = new ArrayList<FingerprintCollection>();
                        numberOfCollections = 0;
                        numberOfScansInCollection = 0;
                        numberOfTotalScans = 0;
                        sent = 1;
                        Log.d(LOG_TAG, "Success (send doc)");
                        announceSendDone(null);
                }
                else if (type == TYPE_SEARCH_DOC){
                        collectionsList = new ArrayList<FingerprintCollection>();
                        numberOfCollections = 0;
                        numberOfScansInCollection = 0;
                        numberOfTotalScans = 0;
                        sent = 1;
                        Log.d(LOG_TAG, "Success (search doc)");
                        /*String delims = "[<>]+";
                        String[] tokens = response.toString().split(delims);
                        String receivedURL = tokens[4];
                        announceSendDone(receivedURL);*/
                        announceSendDone(response.toString());
                        //Log.d(LOG_TAG, response.toString());
                }
                else{
                    Log.d(LOG_TAG, "Failed");
                    sent = 0;
                    displaySendStatus();
                }


            }

        } catch (MalformedURLException e) {
            sent = 0;
            e.printStackTrace();
        } catch (IOException e) {
            sent = 0;
            e.printStackTrace();
        }

    }

    // Handler of incoming messages from clients.
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            restoreAllFields();
            switch (msg.what) {
                case MSG_SEND:
                    Log.d(LOG_TAG, "Send test fingerprints");
                    Log.d(LOG_TAG, "address=" + address + " port=" + port);
                    sendFingerprintsToServer(TYPE_TESTING);
                    break;
                case MSG_START_SCAN:
                    restoreFieldsTesting();
                    Log.d(LOG_TAG, "Scan for testing");
                    Log.d(LOG_TAG, "scan_no=" + scanLimit);
                    doScan();
                    break;
                case MSG_STOP_SCAN:
                    stopScan();
                    break;
                case MSG_SCAN_TO_POST_DOC:
                    Log.d(LOG_TAG, "Scan to post document");
                    Log.d(LOG_TAG, "scan_no=" + scanLimit);
                    doScan();
                    break;
                case MSG_SCAN_TO_SEARCH_DOC:
                    Log.d(LOG_TAG, "Scan to search document");
                    Log.d(LOG_TAG, "scan_no=" + scanLimit);
                    doScan();
                    break;
                case MSG_ACTUAL_SEND_DOC:
                    Log.d(LOG_TAG, "Post document");
                    Log.d(LOG_TAG, "address= " + address + " port=" + port);
                    sendFingerprintsToServer(TYPE_SEND_DOC);
                    break;
                case MSG_ACTUAL_SEARCH_DOC:
                    Log.d(LOG_TAG, "Search document");
                    Log.d(LOG_TAG, "address= " + address + " port=" + port);
                    sendFingerprintsToServer(TYPE_SEARCH_DOC);
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

    private JSONObject buildFinalJSON(boolean ble, boolean cellular, boolean gps){
        JSONObject jsonObject = new JSONObject();

        try{
            for (int i = 0; i < collectionsList.size(); i++) {
                FingerprintCollection fingerprintCollection = collectionsList.get(i);
                jsonObject.put("collection"+i, fingerprintCollection.toJSON(ble, cellular, gps));
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

    public void stopScanInActivity(){
        Log.d(LOG_TAG, "Stop Scan in Activity");
        Intent intent = new Intent("msg");
        intent.putExtra("message", ACT_STOP_SCAN);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void stopScanFailedInActivity(){
        Log.d(LOG_TAG, "Stop Scan Failed in Activity");
        Intent intent = new Intent("msg");
        intent.putExtra("message", ACT_STOP_SCAN_FAILED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void displayNumberOfScans(){
        Intent intent = new Intent("msg");
        intent.putExtra("message", ScanService.UPDATE_SCAN_NUMBERS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void displaySendStatus(){
        Intent intent = new Intent("msg");
        intent.putExtra("message", UPDATE_SEND_STATUS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void announceSendDone(String jsonString){
        Log.d(LOG_TAG, "Announce Send Done to Activity");
        Intent intent = new Intent("msg");
        intent.putExtra("message", MSG_SEND_DONE);
        if (jsonString!= null) intent.putExtra("json", jsonString);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void restoreFieldsTesting(){
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);

        String selectedMap = sharedPref.getString("selectedMap", "precis_subsol.png");
        float x_p = sharedPref.getFloat("x_p", Float.parseFloat("-1"));
        float y_p = sharedPref.getFloat("y_p", Float.parseFloat("-1"));
        float x = sharedPref.getFloat("x", Float.parseFloat("-1"));
        float y = sharedPref.getFloat("y", Float.parseFloat("-1"));
        float z = sharedPref.getFloat("z", Float.parseFloat("-1"));

        currentFingerprintCollection.setMap(selectedMap);
        currentFingerprintCollection.setX_P(x_p);
        currentFingerprintCollection.setY_P(y_p);
        currentFingerprintCollection.setX(x);
        currentFingerprintCollection.setY(y);
        currentFingerprintCollection.setZ(z);
    }

    private void restoreAllFields(){
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);

        address = sharedPref.getString("ip", "192.168.142.105");
        port = sharedPref.getString("port", "8001");
        scanLimit = sharedPref.getInt("scan_no", 1);
        devId = sharedPref.getString("devID", null);
        comment = sharedPref.getString("comment", "-");
        threshold = sharedPref.getFloat("threshold", 0.25f);
        docName = sharedPref.getString("docName", "-");

        ble = sharedPref.getBoolean("ble", true);
        cellular = sharedPref.getBoolean("cellular", true);
        gps = sharedPref.getBoolean("gps", true);
        audio = sharedPref.getBoolean("audio", true);

        file = sharedPref.getString("file", null);
        fileType = sharedPref.getString("filetype", null);

        currentFingerprintCollection.setComment(comment);
    }

}
