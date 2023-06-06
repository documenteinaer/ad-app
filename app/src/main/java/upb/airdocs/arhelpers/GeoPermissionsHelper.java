package upb.airdocs.arhelpers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.security.Permission;
import java.util.ArrayList;

public class GeoPermissionsHelper {

    private static final ArrayList<String> PERMISSIONS = new ArrayList<String>() {
        {
            add(Manifest.permission.CAMERA);
            add(Manifest.permission.ACCESS_FINE_LOCATION);

        }
    };

    public static boolean hasGeoPermissions(Activity activity) {
        for (String perm : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, (String[]) PERMISSIONS.toArray(), 0);
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity) {
        for (String perm : PERMISSIONS) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                return true;
            }
        }
        return false;
    }

    public static void launchPermissionSettings(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }
}
