package upb.airdocs;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.indooratlas.android.sdk.IAARSession;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationManager.*;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IAPOI;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.IndoorAtlasInitProvider;
import com.indooratlas.android.sdk.resources.IALatLng;
import com.indooratlas.android.sdk.resources.IALocationListenerSupport;
import com.indooratlas.android.sdk.resources.IAVenue;

import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import upb.airdocs.hellogeospatial.HelloGeoActivity;

public class UserActivity  extends AppCompatActivity implements IALocationListener, IARegion.Listener {
    private static final String LOG_TAG = "UserActivity";
    final private static int MY_PERMISSIONS_REQUEST = 126;
    private boolean permissionGranted = false;
    Button postDocButton;
    Button searchDocButton;
    Button testButton;
    Button arButton;
    String devID;
    boolean mBound = false;
    boolean serviceStarted = false;
    //Messenger mMessenger = null;
    boolean firstRun;
    private IALocationManager mIALocationManager;
    private IALocationListener mIALocationListener;

    private IAARSession arSdk;

    private final String TAG = "mIALocationManager";
    private TextView currRegion;
    private TextView locationView;
    private ArrayList<String> nearPois = new ArrayList<>();

    private static final String API_KEY = "60bd1f86-9c8a-44e8-bec8-20ecf75688de";
    private static final String BASE_URL = "https://api.indooratlas.com/v2/maps/";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();

    public UserActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
//        mIALocationManager = IALocationManager.create(this);
//        currRegion = (TextView) findViewById(R.id.current_region);
        locationView = (TextView) findViewById(R.id.location_view);
        requestAllPermissions();

        restoreDevID();
        restoreFirstRun();
        getDevIDAndStartService();

        if (firstRun){
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

        arButton = (Button) findViewById(R.id.ar_button);
        arButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                Intent intent = new Intent(getBaseContext(), ARActivity.class);
                Intent intent = new Intent(getBaseContext(), HelloGeoActivity.class);
                startActivity(intent);
            }
        });




        mManager = IALocationManager.create(this);
        mManager.registerRegionListener(this);
        mManager.requestLocationUpdates(IALocationRequest.create(), this);

        arSdk = mManager.requestArUpdates();


        mUiVenue = (TextView) findViewById(R.id.text_view_venue);
        mUiVenueId = (TextView) findViewById(R.id.text_view_venue_id);
        mUiFloorPlan = (TextView) findViewById(R.id.text_view_floor_plan);
        mUiFloorPlanId = (TextView) findViewById(R.id.text_view_floor_plan_id);
        mUiFloorLevel = (TextView) findViewById(R.id.text_view_floor_level);
        mUiFloorCertainty = (TextView) findViewById(R.id.text_view_floor_certainty);


        testButton = (Button) findViewById(R.id.test_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("It works" + latitude + ", " + longitude);
                System.out.println("Level" + mCurrentFloorLevel);
//                arSdk.createArPOI(latitude, longitude, mCurrentFloorLevel);
                System.out.println("VENUE: " + mCurrentVenue.getVenue());

                System.out.println("POIS: "+mCurrentVenue.getVenue().getPOIs());


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mManager.setLocation(new IALocation.Builder()
                                .withLatitude(44.43493585)
                                .withLongitude(26.047781756301177)
                                .withAccuracy(10)
                                .withFloorLevel(0)
                                .build());

                        mManager.lockIndoors(true);
                    }
                }, 5000);
            }
        });

        updateUi();
    }

    private String createPOIJson(String name, double latitude, double longitude) {
        return "{\"name\":\"" + name + "\",\"coordinate\":{\"latitude\":" + latitude + ",\"longitude\":" + longitude + "}}"; // TODO: floor?
    }


    IALocationManager mManager;
    IARegion mCurrentVenue = null;
    IARegion mCurrentFloorPlan = null;
    Integer mCurrentFloorLevel = null;
    Float mCurrentCertainty = null;
    IALocation loc = null;

    TextView mUiVenue;
    TextView mUiVenueId;
    TextView mUiFloorPlan;
    TextView mUiFloorPlanId;
    TextView mUiFloorLevel;
    TextView mUiFloorCertainty;

    double latitude;
    double longitude;

    @Override
    public void onLocationChanged(IALocation iaLocation) {
        if (iaLocation == null) {
            return;
        }
        mCurrentFloorLevel = iaLocation.hasFloorLevel() ? iaLocation.getFloorLevel() : null;
        mCurrentCertainty = iaLocation.hasFloorCertainty() ? iaLocation.getFloorCertainty() : null;
        latitude = iaLocation.getLatitude();
        longitude = iaLocation.getLongitude();
        loc = iaLocation;

        ArrayList<String> foundPois = new ArrayList<>();
        Location currLocation = new Location("");
        currLocation.setLatitude(latitude);
        currLocation.setLongitude(longitude);
        if (mCurrentVenue != null) {
            for (IAPOI iapoi : mCurrentVenue.getVenue().getPOIs()) {
                if (iapoi.getFloor() != mCurrentFloorLevel) {
                    continue;
                }
                Location poiLocation = new Location("");
                IALatLng poiLatLng = iapoi.getLatLngFloor();
                poiLocation.setLatitude(poiLatLng.latitude);
                poiLocation.setLongitude(poiLatLng.longitude);
                System.out.println("dist to poi " + iapoi.getName() + " -> "  + currLocation.distanceTo(poiLocation) + " m");
//                if (currLocation.distanceTo(poiLocation) < 10) {
                    foundPois.add(iapoi.getName() + " -> "  + currLocation.distanceTo(poiLocation) + " m");
//                }
            }
        }

        nearPois = foundPois;

        updateUi();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onEnterRegion(IARegion iaRegion) {


        if (iaRegion.getType() == IARegion.TYPE_VENUE) {
            mCurrentVenue = iaRegion;
        } else if (iaRegion.getType() == IARegion.TYPE_FLOOR_PLAN) {
            mCurrentFloorPlan = iaRegion;
        }
        updateUi();
    }



    @Override
    public void onExitRegion(IARegion iaRegion) {
        if (iaRegion.getType() == IARegion.TYPE_VENUE) {
            mCurrentVenue = iaRegion;
        } else if (iaRegion.getType() == IARegion.TYPE_FLOOR_PLAN) {
            mCurrentFloorPlan = iaRegion;
        }
        updateUi();
    }

    void updateUi() {
        String venue = "Outside mapped area";
        String venueId = "";
        String floorPlan = "";
        String floorPlanId = "";
        String level = "";
        String certainty = "";
        if (mCurrentVenue != null) {
            venue = "In venue";
            venueId = mCurrentVenue.getId();
            if (mCurrentFloorPlan != null) {
                floorPlan = mCurrentFloorPlan.getName();
                floorPlanId = mCurrentFloorPlan.getId();
            } else {
                floorPlan = "No floor plan";
            }
        }
        if (mCurrentFloorLevel != null) {
            level = mCurrentFloorLevel.toString();
        }
        if (mCurrentCertainty != null) {
            certainty = "Certainty: " +mCurrentCertainty;
        }

        String poisText = "Available documents: "+ nearPois.toString();

        setText(locationView, latitude + ", " + longitude, true);
        setText(mUiVenue, venue, true);
        setText(mUiVenueId, venueId, true);
        setText(mUiFloorPlan, floorPlan, true);
        setText(mUiFloorPlanId, poisText, false);
        setText(mUiFloorLevel, level, true);
        setText(mUiFloorCertainty, certainty, false); // do not animate as changes can be frequent
    }

    /**
     * Set the text of a TextView and make a animation to notify when the value has changed
     */
    void setText(@NonNull TextView view, @NonNull String text, boolean animateWhenChanged) {
        if (!view.getText().toString().equals(text)) {
            view.setText(text);
            if (animateWhenChanged) {
                view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.notify_change));
            }
        }
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
        mManager.destroy();
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

//    @Override
//    public void onLocationChanged(IALocation iaLocation) {
//        System.out.println("[mIALocationManager] on location changed: "+iaLocation);
//        System.out.printf("[mIALocationManager] on location changed %f,%f, accuracy: %.2f\n", iaLocation.getLatitude(),
//                iaLocation.getLongitude(), iaLocation.getAccuracy());
//    }
//
//    @Override
//    public void onStatusChanged(String s, int i, Bundle bundle) {
//        System.out.println("[mIALocationManager] onStatusChanged: " + s + " -> " + i);
//    }
//
//    @Override
//    public void onEnterRegion(IARegion iaRegion) {
//
//        if (iaRegion.getType() == IARegion.TYPE_FLOOR_PLAN) {
//            // triggered when entering the mapped area of the given floor plan
//            Log.d(TAG, "Entered " + iaRegion.getName());
//            Log.d(TAG, "floor plan ID: " + iaRegion.getId());
////            mCurrentFloorPlan = iaRegion;
//        }
//        else if (iaRegion.getType() == IARegion.TYPE_VENUE) {
//            // triggered when near a new location
//            Log.d(TAG, "Location changed to " + iaRegion.getId());
//        } else {
//            Log.d(TAG, "whereAmI? "+iaRegion.getName());
//        }
//    }
//
//    @Override
//    public void onExitRegion(IARegion iaRegion) {
//
//    }

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
