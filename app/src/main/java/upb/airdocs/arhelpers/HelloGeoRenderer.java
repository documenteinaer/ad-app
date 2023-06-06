package upb.airdocs.arhelpers;

import android.opengl.Matrix;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.maps.model.LatLng;
import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Earth;
import com.google.ar.core.Frame;
import com.google.ar.core.GeospatialPose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.io.IOException;

import upb.airdocs.ARActivity;
import upb.airdocs.common.helpers.DisplayRotationHelper;
import upb.airdocs.common.helpers.TrackingStateHelper;
import upb.airdocs.common.samplerender.Framebuffer;
import upb.airdocs.common.samplerender.Mesh;
import upb.airdocs.common.samplerender.SampleRender;
import upb.airdocs.common.samplerender.Shader;
import upb.airdocs.common.samplerender.Texture;
import upb.airdocs.common.samplerender.arcore.BackgroundRenderer;

public class HelloGeoRenderer implements SampleRender.Renderer, DefaultLifecycleObserver {
    public BackgroundRenderer backgroundRenderer;
    public Framebuffer virtualSceneFramebuffer;
    private boolean hasSetTextureNames;
    public Mesh virtualObjectMesh;
    public Shader virtualObjectShader;
    public Texture virtualObjectTexture;
    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private Session session;
    private final DisplayRotationHelper displayRotationHelper;
    private final TrackingStateHelper trackingStateHelper;
    private Anchor earthAnchor;
    private static final String TAG = "HelloGeoRenderer";
    private static final float Z_NEAR = 0.1F;
    private static final float Z_FAR = 1000.0F;
    private ARActivity activity;

    public HelloGeoRenderer(ARActivity activity) {
        session = activity.session;
        displayRotationHelper = new DisplayRotationHelper(activity);
        trackingStateHelper = new TrackingStateHelper(activity);
        this.activity = activity;
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        displayRotationHelper.onResume();
        hasSetTextureNames = false;
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        displayRotationHelper.onPause();
    }

    @Override
    public void onSurfaceCreated(SampleRender render) {
        try {
            backgroundRenderer = new BackgroundRenderer(render);
            virtualSceneFramebuffer = new Framebuffer(render, /*width=*/ 1, /*height=*/ 1);

            // Virtual object to render (Geospatial Marker)
            virtualObjectTexture =
                    Texture.createFromAsset(
                            render,
                            "models/spatial_marker_baked.png",
                            Texture.WrapMode.CLAMP_TO_EDGE,
                            Texture.ColorFormat.SRGB
                    );

            virtualObjectMesh = Mesh.createFromAsset(render, "models/geospatial_marker.obj");
            virtualObjectShader =
                    Shader.createFromAssets(
                            render,
                            "shaders/ar_unlit_object.vert",
                            "shaders/ar_unlit_object.frag",
                            /*defines=*/ null)
                            .setTexture("u_Texture", virtualObjectTexture);

            backgroundRenderer.setUseDepthVisualization(render, false);
            backgroundRenderer.setUseOcclusion(render, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceChanged(SampleRender render, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        virtualSceneFramebuffer.resize(width, height);
    }

    @Override
    public void onDrawFrame(SampleRender render) {
        if (session == null) {
            return;
        }

        //<editor-fold desc="ARCore frame boilerplate" defaultstate="collapsed">
        // Texture names should only be set once on a GL thread unless they change. This is done during
        // onDrawFrame rather than onSurfaceCreated since the session is not guaranteed to have been
        // initialized during the execution of onSurfaceCreated.
        if (!hasSetTextureNames) {
            session.setCameraTextureNames(new int[]{backgroundRenderer.getCameraColorTexture().getTextureId()});
            hasSetTextureNames = true;
        }

        // -- Update per-frame state

        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        // Obtain the current frame from ARSession. When the configuration is set to
        // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
        // camera framerate.
        Frame frame = null;
        try {
            frame = session.update();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }

        Camera camera = frame.getCamera();

        // BackgroundRenderer.updateDisplayGeometry must be called every frame to update the coordinates
        // used to draw the background camera image.
        backgroundRenderer.updateDisplayGeometry(frame);

        // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
        trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

        // -- Draw background
        if (frame.getTimestamp() != 0L) {
            // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
            // drawing possible leftover data from previous sessions if the texture is reused.
            backgroundRenderer.drawBackground(render);
        }

        // If not tracking, don't draw 3D objects.
        if (camera.getTrackingState() == TrackingState.PAUSED) {
            return;
        }

        // Get projection matrix.
        camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR);

        // Get camera matrix and draw.
        camera.getViewMatrix(viewMatrix, 0);

        render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f);
        //</editor-fold>

        // TODO: Obtain Geospatial information and display it on the map.
        Earth earth = session.getEarth();
        if (earth.getTrackingState() == TrackingState.TRACKING) {
            // TODO: the Earth object may be used here.
            GeospatialPose cameraGeospatialPose = earth.getCameraGeospatialPose();
            activity.view.mapView.updateMapPosition(cameraGeospatialPose.getLatitude(), cameraGeospatialPose.getLongitude(), cameraGeospatialPose.getHeading());
            activity.view.updateStatusText(earth, earth.getCameraGeospatialPose());

        }

        // Draw the placed anchor, if it exists.
        if (earthAnchor != null) {
            renderCompassAtAnchor(render, earthAnchor);
        }

        // Compose the virtual scene with the background.
        backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR);
    }

    void onMapClick(LatLng latLng) {
        // TODO: place an anchor at the given position.
        Earth earth;
        if (session.getEarth() == null) {
            return;
        }
        earth = session.getEarth();

        if (earth.getTrackingState() != TrackingState.TRACKING) {
            return;
        }
        earthAnchor.detach();
        // Place the earth anchor at the same altitude as that of the camera to make it easier to view.
        double altitude = earth.getCameraGeospatialPose().getAltitude() - 1;
        // The rotation quaternion of the anchor in the East-Up-South (EUS) coordinate system.
        float qx = 0f;
        float qy = 0f;
        float qz = 0f;
        float qw = 1f;
        earthAnchor =
                earth.createAnchor(latLng.latitude, latLng.longitude, altitude, qx, qy, qz, qw);
        activity.view.mapView.earthMarker.setPosition(latLng);
        activity.view.mapView.earthMarker.setVisible(true);
    }

    private void renderCompassAtAnchor(SampleRender sampleRender, Anchor anchor) {
        // Get the current pose of the Anchor in world space. The Anchor pose is updated
        // during calls to session.update() as ARCore refines its estimate of the world.
        anchor.getPose().toMatrix(modelMatrix, 0);

        // Calculate model/view/projection matrices
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);

        // Update shader properties and draw
        virtualObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);
        sampleRender.draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer);
    }
}
