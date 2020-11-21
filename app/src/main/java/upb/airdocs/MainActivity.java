package upb.airdocs;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    final private static int MY_PERMISSIONS_REQUEST = 126;
    private boolean scanActive = false;
    private boolean permissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestAllPermissions();

        final Button startScanButton = (Button) findViewById(R.id.start_scan);
        startScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (scanActive == false){
                    if (permissionGranted == true) {
                        startService();
                        scanActive = true;
                        startScanButton.setText("Stop Scan");
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Permissions have not been granted", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    stopService();
                    scanActive = false;
                    startScanButton.setText("Start Scan");
                }
            }
        });
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
    public void startService() {
        Intent serviceIntent = new Intent(this, ScanService.class);
        //serviceIntent.putExtra("inputExtra", "Scan Service");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, ScanService.class);
        stopService(serviceIntent);
    }
}
