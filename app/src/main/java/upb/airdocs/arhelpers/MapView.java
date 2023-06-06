package upb.airdocs.arhelpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.internal.IGoogleMapDelegate;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import upb.airdocs.ARActivity;
import upb.airdocs.R;

public class MapView {
    private int CAMERA_MARKER_COLOR = Color.argb(255, 0, 255, 0);
    private int EARTH_MARKER_COLOR = Color.argb(255, 125, 125, 125);

    boolean setInitialCameraPosition = false;
    Marker cameraMarker;
    boolean cameraIdle = true;
    ARActivity activity;
    GoogleMap googleMap;
    public Marker earthMarker;



    MapView(ARActivity activity, GoogleMap googleMap) {
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);

        googleMap.setOnMarkerClickListener(unused -> false);

        // Add listeners to keep track of when the GoogleMap camera is moving.
        googleMap.setOnCameraMoveListener(() -> cameraIdle = false); // TODO: does it work like this?
        googleMap.setOnCameraIdleListener(() -> cameraIdle = false);
        cameraMarker = createMarker(CAMERA_MARKER_COLOR);
        earthMarker = createMarker(EARTH_MARKER_COLOR);
        this.activity = activity;
    }


    void updateMapPosition(Double latitude, Double longitude, Double heading) {
        LatLng position = new LatLng(latitude, longitude);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // If the map is already in the process of a camera update, then don't move it.
                if (!cameraIdle) {
                    return;
                }
                cameraMarker.setVisible(true);
                cameraMarker.setPosition(position);
                cameraMarker.setRotation(heading.floatValue());

                CameraPosition.Builder cameraPositionBuilder;
                if (!setInitialCameraPosition) {
                    // Set the camera position with an initial default zoom level.
                    setInitialCameraPosition = true;
                    cameraPositionBuilder = new CameraPosition.Builder().zoom(21f).target(position);
                } else {
                    // Set the camera position and keep the same zoom level.
                    cameraPositionBuilder = new CameraPosition.Builder()
                            .zoom(googleMap.getCameraPosition().zoom)
                            .target(position);
                }
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPositionBuilder.build()));
            }
        });

    }

    /** Creates and adds a 2D anchor marker on the 2D map view.  */
    private Marker createMarker(int color) {
        MarkerOptions markersOptions = new MarkerOptions()
                .position(new LatLng(0.0,0.0))
                .draggable(false)
                .anchor(0.5f, 0.5f)
                .flat(true)
                .visible(false)
                .icon(BitmapDescriptorFactory.fromBitmap(createColoredMarkerBitmap(color)));
        return googleMap.addMarker(markersOptions);
    }

    private Bitmap createColoredMarkerBitmap(int color) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inMutable = true;
        Bitmap navigationIcon = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_navigation_white_48dp, opt);
        Paint p = new Paint();
        p.setColorFilter(new LightingColorFilter(color,  /* add= */1));
        Canvas canvas = new Canvas(navigationIcon);
        canvas.drawBitmap(navigationIcon,  /* left= */0f,  /* top= */0f, p);
        return navigationIcon;
    }

}
