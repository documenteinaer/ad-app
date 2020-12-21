package upb.airdocs;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    //  Messenger for communicating with the service.
    Messenger mMessenger = null;
    // Flag indicating whether we have called bind on the service.
    boolean mBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestAllPermissions();

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();

        final Button startScanButton = (Button) findViewById(R.id.start_scan);
        startScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (scanActive == false){
                    if (permissionGranted == true) {
                        onStartScan();
                        scanActive = true;
                        startScanButton.setText("Stop Scan");
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Permissions have not been granted", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    onStopScan();
                    scanActive = false;
                    startScanButton.setText("Start Scan");
                }
            }
        });

        final Button sendButton = (Button) findViewById(R.id.send_fingerprints);
        final EditText addressEditText = (EditText) findViewById(R.id.address);
        final EditText portEditText = (EditText) findViewById(R.id.port);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String address = addressEditText.getText().toString();
                String port = portEditText.getText().toString();
                Log.d(LOG_TAG, "address= " + address + " port=" + port);
                onSendButton(address, port);
            }
        });
    }

    @Override
    protected void onDestroy() {
        stopService();
        super.onDestroy();
    }

    public void onSendButton(String address, String port) {
        ServerAddress serverAddress = new ServerAddress(address, port);
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

    public void onStartScan(){
        if (mBound) {
            // Create and send a message to the service, using a supported 'what' value
            Message msg = Message.obtain(null, ScanService.MSG_START_SCAN, 0, 0);
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    public void onStopScan(){
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

    private void requestAllPermissions(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST);

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

    private String  getID(){
        if(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
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

}
