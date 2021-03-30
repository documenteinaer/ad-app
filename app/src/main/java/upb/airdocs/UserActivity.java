package upb.airdocs;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
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

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;

public class UserActivity extends Activity {
    private static final String LOG_TAG = "UserActivity";
    final private static int MY_PERMISSIONS_REQUEST = 126;
    // Flag indicating whether we have called bind on the service.
    private boolean mBound = false;
    private boolean scanActive = false;
    private boolean permissionGranted = false;
    private boolean send = false;
    private boolean search = false;

    //  Messenger for communicating with the service.
    Messenger mMessenger = null;

    Button scanSendDocButton;
    Button scanSearchDocButton;
    EditText sendDocumentURL;
    EditText receivedDocumentURL;
    TextView scanSendStatus;
    TextView scanSearchStatus;

    String address;
    String port;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        address = getIntent().getStringExtra("address");
        port = getIntent().getStringExtra("port");
        Log.d(LOG_TAG, "address= " + address + " port=" + port);

        requestAllPermissions();

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();

        scanSendStatus = (TextView) findViewById(R.id.scan_send_status);
        scanSearchStatus = (TextView) findViewById(R.id.scan_search_status);

        sendDocumentURL = (EditText) findViewById(R.id.send_document_url);
        receivedDocumentURL = (EditText) findViewById(R.id.received_document_url);

        scanSendDocButton = (Button) findViewById(R.id.scan_send_doc);
        scanSendDocButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (scanActive == false) {
                    if (permissionGranted == true) {
                        onStartScanSendDoc();
                        scanActive = true;
                        scanSendDocButton.setEnabled(false);
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    } else {
                        Toast.makeText(getApplicationContext(), "Permissions have not been granted", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        scanSearchDocButton = (Button) findViewById(R.id.scan_search_doc);
        scanSearchDocButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (scanActive == false) {
                    if (permissionGranted == true) {
                        onStartScanSearchDoc();
                        scanActive = true;
                        scanSearchDocButton.setEnabled(false);
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    } else {
                        Toast.makeText(getApplicationContext(), "Permissions have not been granted", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("msg"));
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopService();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        restoreFields();
        super.onStart();
    }

    @Override
    protected void onStop() {
        saveFields();
        super.onStop();
    }

    private void onStartScanSendDoc() {
        if (mBound) {
            Message msg = Message.obtain(null, ScanService.MSG_SCAN_SEND_DOC, 4, 0, null);
            try {
                mMessenger.send(msg);
                send = true;
            } catch (RemoteException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    private void onStartScanSearchDoc() {
        if (mBound) {
            Message msg = Message.obtain(null, ScanService.MSG_SCAN_SEARCH_DOC, 4, 0, null);
            try {
                mMessenger.send(msg);
                search = true;
            } catch (RemoteException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    private void requestAllPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST);

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

    public void startService(String devID) {
        Intent serviceIntent = new Intent(this, ScanService.class);
        serviceIntent.putExtra("devID", devID);
        ContextCompat.startForegroundService(this, serviceIntent);
        bindService(new Intent(this, ScanService.class), mConnection, Context.BIND_AUTO_CREATE);
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
            } catch (Exception e) {
                e.printStackTrace();
                id = null;
            }
            return id;
        }

        @Override
        protected void onPostExecute(String result) {
            startService(result);
        }
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            int msg = intent.getIntExtra("message", -1/*default value*/);
            if (msg == ScanService.ACT_STOP_SCAN) {
                scanActive = false;
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Log.d(LOG_TAG, "In broadcast receiver");
                if (send == true){
                    scanSendStatus.setText("Scan complete");
                    sendDocumentToServer();
                }
                if (search == true){
                    scanSearchStatus.setText("Scan complete");
                    searchDocumentOnServer();
                }
            }
            else if (msg == ScanService.MSG_SEND_DONE){
                if (send == true){
                    scanSendDocButton.setEnabled(true);
                    scanSendStatus.setText("Sent successfuly");
                    send = false;
                }
                if (search == true){
                    scanSearchDocButton.setEnabled(true);
                    scanSearchStatus.setText("Sent successfuly");
                    String receivedURL = intent.getStringExtra("receivedURL");
                    receivedDocumentURL.setText(receivedURL);
                    search = false;
                }
            }
        }
    };

    private void sendDocumentToServer() {
        String documentURL = sendDocumentURL.getText().toString();
        ServerAddress serverAddress = new ServerAddress(address, port, documentURL);
        if (mBound) {
            // Create and send a message to the service, using a supported 'what' value
            Message msg = Message.obtain(null, ScanService.MSG_ACTUAL_SEND_DOC, 0, 0, serverAddress);
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    private void searchDocumentOnServer() {
        ServerAddress serverAddress = new ServerAddress(address, port, null);
        if (mBound) {
            // Create and send a message to the service, using a supported 'what' value
            Message msg = Message.obtain(null, ScanService.MSG_ACTUAL_SEARCH_DOC, 0, 0, serverAddress);
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    private void saveFields(){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("ip", address);
        editor.putString("port", port);
        editor.apply();
    }

    private void restoreFields(){
        if (address == null) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            address = sharedPref.getString("ip", "192.168.142.105");
            port = sharedPref.getString("port", "8001");
        }
    }




}