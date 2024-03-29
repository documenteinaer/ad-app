package upb.airdocs;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;

public class UserActivity  extends AppCompatActivity {
    private static final String LOG_TAG = "UserActivity";
    final private static int MY_PERMISSIONS_REQUEST = 126;
    private boolean permissionGranted = false;
    Button postDocButton;
    Button searchDocButton;
    String devID;
    boolean mBound = false;
    boolean serviceStarted = false;
    //Messenger mMessenger = null;
    boolean firstRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        requestAllPermissions();

        restoreDevID();
        restoreFirstRun();
        getDevIDAndStartService();

        if (firstRun == true){
            firstRun = false;
            saveFirstRun();
            Intent intent = new Intent(getBaseContext(), HelperActivity.class);
            startActivity(intent);
        }


        postDocButton = (Button) findViewById(R.id.post_doc_button);
        postDocButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), PostDocumentActivity.class);
                startActivity(intent);
            }
        });

        searchDocButton = (Button) findViewById(R.id.search_doc_button);
        searchDocButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), SearchDocumentActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        invalidateOptionsMenu();
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        stopService();
        super.onDestroy();
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

    public void startService() {
        if (devID != null) {
            Intent serviceIntent = new Intent(this, ScanService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
            Log.d(LOG_TAG, "Start Scan Service");
            bindService(new Intent(this, ScanService.class), mConnection, Context.BIND_AUTO_CREATE);
            serviceStarted = true;
            Log.d(LOG_TAG, "Bind Scan Service");
        }
        else{
            serviceStarted = false;
        }
    }

    public void stopService() {
        if (mBound) {
            unbindService(mConnection);
            Log.d(LOG_TAG, "Unbind Scan Service");
        }
        Intent serviceIntent = new Intent(this, ScanService.class);
        stopService(serviceIntent);
        Log.d(LOG_TAG, "Stop Scan Service");
    }

    // Class for interacting with the main interface of the service.
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            // This is called when the connection with the iBinder has been established, giving us the object we can use
            // to interact with the iBinder.  We are communicating with the iBinder using a Messenger, so here we get a
            // client-side representation of that from the raw IBinder object.
            //mMessenger = new Messenger(iBinder);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected -- that is,
            // its process crashed.
            //mMessenger = null;
            mBound = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.user_settings:
                //Go to settings activity
                intent = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.testing_activity:
                //Go to testing activity
                intent = new Intent(getBaseContext(), TestingActivity.class);
                startActivity(intent);
                return true;
            case R.id.user_help:
                //Go to helper activity
                intent = new Intent(getBaseContext(), HelperActivity.class);
                startActivity(intent);
                return true;
            case R.id.user_exit:
                stopService();
                finishAffinity();
                System.exit(0);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getDevIDAndStartService(){
        if (devID == null) {
            UserActivity.AsyncTaskRunner runner = new UserActivity.AsyncTaskRunner();
            runner.execute();
        }
        else{
            startService();
        }
    }

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
            saveDevID();
            startService();
        }
    }

    private void saveDevID(){
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("devID", devID);
        Log.d(LOG_TAG, "Saved devID="+devID);
        editor.apply();
    }

    private void restoreDevID(){
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        devID = sharedPref.getString("devID", null);
    }

    private void restoreFirstRun(){
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        firstRun = sharedPref.getBoolean("firstRun", true);
    }

    private void saveFirstRun(){
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("firstRun", firstRun);
        editor.apply();
    }

}
