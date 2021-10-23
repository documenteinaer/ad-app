package upb.airdocs;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    private static final String LOG_TAG = "SettingsActivity";

    private boolean mBound = false;
    Messenger mMessenger = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        restoreFields();

        bindScanService();

        final Button saveButton = (Button) findViewById(R.id.save_settings);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveFields();
                finish();
            }
        });

        final Button deleteButton = (Button) findViewById(R.id.delete_all_documents);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteAllDocs();
                //finish();
            }
        });

        final TextView versionText = (TextView) findViewById(R.id.user_version_name);
        String versionTextString = "Application version: "+ BuildConfig.VERSION_NAME;
        versionText.setText(versionTextString);
    }

    @Override
    protected void onDestroy() {
        unbindScanService();
        super.onDestroy();
    }

    public void bindScanService() {
        //Intent serviceIntent = new Intent(this, ScanService.class);
        //ContextCompat.startForegroundService(this, serviceIntent);
        bindService(new Intent(this, ScanService.class), mConnection, Context.BIND_AUTO_CREATE);
        Log.d(LOG_TAG, "Bind Scan Service");
    }

    public void unbindScanService() {
        if (mBound) {
            unbindService(mConnection);
            Log.d(LOG_TAG, "Unbind Scan Service");
        }
        //Intent serviceIntent = new Intent(this, ScanService.class);
        //stopService(serviceIntent);
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

    private void deleteAllDocs() {
        if (mBound) {
            // Create and send a message to the service, using a supported 'what' value
            Message msg = Message.obtain(null, ScanService.MSG_DEL_ALL, 0, 0);
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    private void saveFields(){
        final EditText noscansEditText = (EditText) findViewById(R.id.no_scans_user);
        final EditText addressEditText = (EditText) findViewById(R.id.address_user);
        final EditText portEditText = (EditText) findViewById(R.id.port_user);
        final EditText thresholdEditText = (EditText) findViewById(R.id.threshold);

        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("scan_no", Integer. parseInt(noscansEditText.getText().toString()));
        editor.putString("ip", addressEditText.getText().toString());
        editor.putString("port", portEditText.getText().toString());
        editor.putFloat("threshold", Float.valueOf(thresholdEditText.getText().toString()));
        editor.apply();
    }

    private void restoreFields(){
        final EditText noscansEditText = (EditText) findViewById(R.id.no_scans_user);
        final EditText addressEditText = (EditText) findViewById(R.id.address_user);
        final EditText portEditText = (EditText) findViewById(R.id.port_user);
        final EditText thresholdEditText = (EditText) findViewById(R.id.threshold);

        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        int noscans = sharedPref.getInt("scan_no", 1);
        Log.d(LOG_TAG, "no scans = " + noscans);
        noscansEditText.setText(String.valueOf(noscans));
        String address = sharedPref.getString("ip", "192.168.142.123");
        addressEditText.setText(address);
        String port = sharedPref.getString("port", "8001");
        portEditText.setText(port);
        float threshold  = sharedPref.getFloat("threshold", 0.25f);
        thresholdEditText.setText(String.valueOf(threshold));

    }

}
