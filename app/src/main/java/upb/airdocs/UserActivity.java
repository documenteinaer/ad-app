package upb.airdocs;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;

public class UserActivity  extends AppCompatActivity {
    private static final String LOG_TAG = "UserActivity";
    Button postDocButton;
    Button searchDocButton;
    String devID;
    boolean mBound = false;
    boolean serviceStarted = false;
    //Messenger mMessenger = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        restoreDevID();
        getDevIDAndStartService();


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

    public void startService() {
        if (devID != null) {
            Intent serviceIntent = new Intent(this, ScanService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
            bindService(new Intent(this, ScanService.class), mConnection, Context.BIND_AUTO_CREATE);
            serviceStarted = true;
        }
        else{
            serviceStarted = false;
        }
    }

    public void stopService() {
        if (mBound)
            unbindService(mConnection);
        Intent serviceIntent = new Intent(this, ScanService.class);
        stopService(serviceIntent);
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
            case R.id.settings:
                //Go to settings activity
                intent = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.testing_activity:
                //Go to testing activity
                intent = new Intent(getBaseContext(), TestingActivity.class);
                startActivity(intent);
                return true;
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

}
