package upb.airdocs;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    final private static int MY_PERMISSIONS_REQUEST = 126;
    private boolean scanActive = false;
    private boolean permissionGranted = false;

    public static String selectedMap = "precis_subsol.png";
    public static int selectedMapID = R.drawable.precis_subsol;
    public static float x = -1;
    public static float y = -1;


    //  Messenger for communicating with the service.
    Messenger mMessenger = null;
    // Flag indicating whether we have called bind on the service.
    boolean mBound;

    String address;
    String port;

    String devID;
    boolean serviceStarted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestAllPermissions();

        restoreFields();

        getDevIDAndStartService();

        final Button selectMapButton = (Button) findViewById(R.id.select_map);
        selectMapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), MapGalleryActivity.class));
            }
        });

        final Button selectPointButton = (Button) findViewById(R.id.select_point);
        selectPointButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), SelectPointActivity.class));
            }
        });


        final EditText comment = (EditText) findViewById(R.id.comment);

        final EditText numberOfScans = (EditText) findViewById(R.id.number_scans);


        final Button startScanButton = (Button) findViewById(R.id.start_scan);
        startScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (devID == null && serviceStarted == false){
                    getDevIDAndStartService();
                    Toast.makeText(getApplicationContext(), "Wait a few seconds and press again", Toast.LENGTH_LONG).show();
                }
                else if (devID != null && serviceStarted == false){
                    startService();
                    Toast.makeText(getApplicationContext(), "Wait a few seconds and press again", Toast.LENGTH_LONG).show();
                }
                else if (scanActive == false) {
                    if (permissionGranted == true) {
                        onStartScan(comment.getText().toString(), numberOfScans.getText().toString());
                        scanActive = true;
                        startScanButton.setText("Stop Scan");
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    } else {
                        Toast.makeText(getApplicationContext(), "Permissions have not been granted", Toast.LENGTH_LONG).show();
                    }
                } else {
                    onStopScan();
                    scanActive = false;
                    startScanButton.setText("Start Scan");
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        });

        final Button sendButton = (Button) findViewById(R.id.send_fingerprints);
        final EditText addressEditText = (EditText) findViewById(R.id.address);
        final EditText portEditText = (EditText) findViewById(R.id.port);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                address = addressEditText.getText().toString();
                port = portEditText.getText().toString();
                Log.d(LOG_TAG, "address= " + address + " port=" + port);
                onSendButton(address, port);
            }
        });

        final Button switchToUsermodeButton = (Button) findViewById(R.id.switch_to_usermode);
        switchToUsermodeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), UserActivity.class);
                address = addressEditText.getText().toString();
                port = portEditText.getText().toString();
                intent.putExtra("address", address);
                intent.putExtra("port", port);
                startActivity(intent);
            }
        });
    }

    private void getDevIDAndStartService(){
        if (devID == null) {
            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.execute();
        }
        else{
            final TextView devIDTextView = (TextView) findViewById(R.id.devID);
            devIDTextView.setText("Device "+devID);
            startService();
        }
    }

    public void onSendButton(String address, String port) {
        ServerAddress serverAddress = new ServerAddress(address, port, null);
        if (mBound) {
            // Create and send a message to the service, using a supported 'what' value
            Message msg = Message.obtain(null, ScanService.MSG_SEND, 0, 0, serverAddress);
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    public void onStartScan(String comment, String noSscans) {
        if (mBound) {
            int nos =Integer.parseInt(noSscans);
            AuxObj auxObj = new AuxObj(comment, selectedMap, x, y, nos);
            // Create and send a message to the service, using a supported 'what' value
            Message msg = Message.obtain(null, ScanService.MSG_START_SCAN, 0, 0, auxObj);
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    public void onStopScan() {
        if (mBound) {
            // Create and send a message to the service, using a supported 'what' value
            Message msg = Message.obtain(null, ScanService.MSG_STOP_SCAN, 0, 0);
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    private void requestAllPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_TAG, "Permission granted");
                    //ScanService.startService(getApplicationContext());
                    permissionGranted = true;

                } else {
                    Log.d(LOG_TAG, "Permission not granted");
                    //Do smth
                    Toast.makeText(getApplicationContext(), "Permissions have not been granted", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    public void startService() {
        if (devID != null) {
            Intent serviceIntent = new Intent(this, ScanService.class);
            serviceIntent.putExtra("devID", devID);
            ContextCompat.startForegroundService(this, serviceIntent);
            bindService(new Intent(this, ScanService.class), mConnection, Context.BIND_AUTO_CREATE);
            serviceStarted = true;
        }
        else{
            serviceStarted = false;
        }
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, ScanService.class);
        stopService(serviceIntent);
    }

    // Class for interacting with the main interface of the service.
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            // This is called when the connection with the iBinder has been established, giving us the object we can use
            // to interact with the iBinder.  We are communicating with the iBinder using a Messenger, so here we get a
            // client-side representation of that from the raw IBinder object.
            mMessenger = new Messenger(iBinder);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected -- that is,
            // its process crashed.
            mMessenger = null;
            mBound = false;
        }
    };

    private String getID() {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            //Google Play Services are available
            AdvertisingIdClient.Info adInfo = null;

            try {
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(this);
                if (adInfo != null) {
                    String AdId = adInfo.getId();
                    return AdId;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (GooglePlayServicesRepairableException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String id;
            try {
                id = getID();
                devID = id;
            } catch (Exception e) {
                e.printStackTrace();
                id = null;
            }
            return id;
        }

        @Override
        protected void onPostExecute(String result) {
            final TextView devIDTextView = (TextView) findViewById(R.id.devID);
            devIDTextView.setText("Device "+devID);
            startService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("msg"));

        if (selectedMap != null) {
            final EditText mapName = (EditText) findViewById(R.id.map_name);
            mapName.setText(selectedMap);
        }
        if (x >= 0 && y >= 0) {
            final EditText coordinateX = (EditText) findViewById(R.id.coordinate_x);
            coordinateX.setText(Float.toString(x));
            final EditText coordinateY = (EditText) findViewById(R.id.coordinate_y);
            coordinateY.setText(Float.toString(y));
        }
        if (mBound){ //Obtain information from the service and update the activity
            int numberOfScansInCollection = ScanService.numberOfScansInCollection;
            int numberOfTotalScans = ScanService.numberOfTotalScans;
            int collections = ScanService.numberOfCollections;
            final TextView scans = (TextView) findViewById(R.id.number_of_scans);
            scans.setText(numberOfScansInCollection + " fingerprints in the current collection\n" +
                    numberOfTotalScans + " fingerprints in total\n" +
                    collections + " collections");

            int sent = ScanService.sent;
            final TextView sendStatus = (TextView) findViewById(R.id.send_status);
            if (sent == 1) {
                sendStatus.setText("Sent successfully");
            } else if (sent == 0) {
                sendStatus.setText("Send failed");
            } else {
                sendStatus.setText("Unsent");
            }
        }

    }


    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            int msg = intent.getIntExtra("message", -1/*default value*/);
            if (msg == ScanService.ACT_STOP_SCAN) {
                scanActive = false;
                Button startScanButton = (Button) findViewById(R.id.start_scan);
                startScanButton.setText("Start Scan");
            }
            if (msg == ScanService.UPDATE_SCAN_NUMBERS) {
                int numberOfScansInCollection = ScanService.numberOfScansInCollection;
                int numberOfTotalScans = ScanService.numberOfTotalScans;
                int collections = ScanService.numberOfCollections;
                final TextView scans = (TextView) findViewById(R.id.number_of_scans);
                scans.setText(numberOfScansInCollection + " fingerprints in the current collection\n" +
                        numberOfTotalScans + " fingerprints in total\n" +
                        collections + " collections");
            }
            if (msg == ScanService.UPDATE_SEND_STATUS) {
                Log.d(LOG_TAG, "Intent received");
                int sent = ScanService.sent;
                final TextView sendStatus = (TextView) findViewById(R.id.send_status);
                if (sent == 1) {
                    sendStatus.setText("Sent successfully");
                } else if (sent == 0) {
                    sendStatus.setText("Send failed");
                } else {
                    sendStatus.setText("Unsent");
                }
            }
        }
    };

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        saveFields();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopService();
        saveFields();
        super.onDestroy();
    }

    private void saveFields(){
        final EditText addressEditText = (EditText) findViewById(R.id.address);
        final EditText portEditText = (EditText) findViewById(R.id.port);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("ip", addressEditText.getText().toString());
        editor.putString("port", portEditText.getText().toString());
        editor.putString("selectedMap", selectedMap);
        editor.putInt("selectedMapID", selectedMapID);
        editor.putFloat("x", x);
        editor.putFloat("y", y);
        editor.putString("devID", devID);
        editor.apply();
    }

    private void restoreFields(){
        final EditText addressEditText = (EditText) findViewById(R.id.address);
        final EditText portEditText = (EditText) findViewById(R.id.port);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        address = sharedPref.getString("ip", "192.168.142.105");
        addressEditText.setText(address);
        port = sharedPref.getString("port", "8001");
        portEditText.setText(port);
        selectedMap = sharedPref.getString("selectedMap", "precis_subsol.png");
        selectedMapID = sharedPref.getInt("selectedMapID", R.drawable.precis_subsol);
        x = sharedPref.getFloat("x", Float.parseFloat("-1"));
        y = sharedPref.getFloat("y", Float.parseFloat("-1"));
        devID = sharedPref.getString("devID", null);
    }

}
