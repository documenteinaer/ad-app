package upb.airdocs;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

public class GPSScan implements LocationListener {

    private LocationManager locationManager;
    Context mContext;
    private static final String LOG_TAG = "GPSScan";

    public GPSScan(Context context) {
        mContext = context;
    }


    public void startScan() {
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(LOG_TAG, "Not enought permissions");
            return;
        }

        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        String str ="Last GPS location was: " + lastLocation.getTime() + " "
                + lastLocation.getLatitude() + " " + lastLocation.getLongitude();
        Log.d(LOG_TAG, str);
        Toast.makeText(mContext.getApplicationContext(), str, Toast.LENGTH_LONG).show();


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000,   // 1 sec
                0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        String str = "GPS location: " + location.getTime() + " "
                + location.getLatitude() + " " + location.getLongitude();

        Log.d(LOG_TAG, str);
        Toast.makeText(mContext.getApplicationContext(), str, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(mContext.getApplicationContext(), "Gps turned on ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(mContext.getApplicationContext(), "Gps turned off ", Toast.LENGTH_LONG).show();
    }
}
