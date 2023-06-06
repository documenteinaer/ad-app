package upb.airdocs.arhelpers;

import android.opengl.GLSurfaceView;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.ar.core.Earth;
import com.google.ar.core.GeospatialPose;
import com.google.ar.core.Session;

import upb.airdocs.ARActivity;
import upb.airdocs.R;
import upb.airdocs.common.helpers.SnackbarHelper;

public class HelloGeoView implements DefaultLifecycleObserver, OnMapReadyCallback {
    public View root;
    public GLSurfaceView surfaceView;

    private final SnackbarHelper snackbarHelper;
    public MapView mapView;
    private final TextView statusText;

    private Session session;
    private ARActivity activity;

    public HelloGeoView(ARActivity activity) {
        root = View.inflate(activity, R.layout.activity_aractivity, null);
        surfaceView = root.findViewById(R.id.surfaceview);
        this.session = activity.session;
        this.activity = activity;
        System.out.println("VLAD: mapView: "+ mapView);
        snackbarHelper = new SnackbarHelper();
        statusText = root.findViewById(R.id.statusText);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.mapView = new MapView(activity, googleMap);
    }

    void updateStatusText(Earth earth, GeospatialPose cameraGeospatialPose) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String poseText = "";
                if (cameraGeospatialPose != null) {
                    poseText = activity.getString(R.string.geospatial_pose,
                            cameraGeospatialPose.getLatitude(),
                            cameraGeospatialPose.getLongitude(),
                            cameraGeospatialPose.getHorizontalAccuracy(),
                            cameraGeospatialPose.getAltitude(),
                            cameraGeospatialPose.getVerticalAccuracy(),
                            cameraGeospatialPose.getHeading(),
                            cameraGeospatialPose.getHeadingAccuracy());

                }
                statusText.setText(activity.getResources().getString(R.string.earth_state,
                        earth.getEarthState().toString(),
                        earth.getTrackingState().toString(),
                        "",
                        poseText));
            }
        });
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        surfaceView.onResume();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        surfaceView.onPause();
    }
}
