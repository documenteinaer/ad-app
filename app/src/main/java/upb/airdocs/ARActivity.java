package upb.airdocs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import upb.airdocs.arhelpers.GeoPermissionsHelper;
import upb.airdocs.arhelpers.HelloGeoRenderer;
import upb.airdocs.arhelpers.HelloGeoView;
import upb.airdocs.common.helpers.FullScreenHelper;
import upb.airdocs.common.samplerender.SampleRender;

class ARObject {
    boolean placed;
    float currDistance;

    ARObject() {}
    ARObject(boolean placed, float currDistance) {
        this.placed = placed;
        this.currDistance = currDistance;
    }

    @Override
    public String toString() {
        return "ARObject{" +
                "placed=" + placed +
                ", currDistance=" + currDistance +
                '}';
    }
}

public class ARActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ModelRenderable modelRenderable;
    private String MODEL_URL = "https://modelviewer.dev/shared-assets/models/Astronaut.glb";
    private ArrayList<ARObject> arObjects;
    private Set features = new HashSet();
    public Session session;
    private SampleRender render;


    public HelloGeoView view;
    public HelloGeoRenderer renderer;
    private static final String TAG = "HelloGeoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure session features.
//        session = tryCreateSession();
//        session.configure(
//            session.getConfig().setGeospatialMode(Config.GeospatialMode.ENABLED)
//        );
//
//        // Set up the Hello AR renderer.
//        view = new HelloGeoView(this);
////        renderer = new HelloGeoRenderer(this);
//
//        setContentView(view.root);
//        new SampleRender(view.surfaceView, new HelloGeoRenderer(this), getAssets());



//        arObjects = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            ARObject arObject = new ARObject(false, -0.5f-i);
//            arObjects.add(arObject);
//        }
//        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
//        setupModel();
//        setUpPlane();
    }

    private Session tryCreateSession() {
        // The app must have been given the CAMERA permission. If we don't have it yet, request it.
        Session session = null;
        if (!GeoPermissionsHelper.hasGeoPermissions(this)) {
            GeoPermissionsHelper.requestPermissions(this);
            return null;
        }
        try {
            session = new Session(this, features);
        } catch (UnavailableArcoreNotInstalledException e) {
            e.printStackTrace();
        } catch (UnavailableApkTooOldException e) {
            e.printStackTrace();
        } catch (UnavailableSdkTooOldException e) {
            e.printStackTrace();
        } catch (UnavailableDeviceNotCompatibleException e) {
            e.printStackTrace();
        }
        return session;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!GeoPermissionsHelper.hasGeoPermissions(this)) {
            // Use toast instead of snackbar here since the activity will exit.
            Toast.makeText(this, "Camera and location permissions are needed to run this application", Toast.LENGTH_LONG).show();
            if (!GeoPermissionsHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                GeoPermissionsHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }

    //    private void setupModel() {
//        ModelRenderable.builder()
//            .setSource(this,
//                    RenderableSource.builder().setSource(
//                            this, Uri.parse(MODEL_URL),
//                            RenderableSource.SourceType.GLB)
//                    .setScale(0.2f)
//                    .setRecenterMode(RenderableSource.RecenterMode.ROOT)
//                    .build())
//            .setRegistryId(MODEL_URL)
//            .build()
//            .thenAccept(renderable -> {
//                System.out.println("[VLAD] Here");
//                System.out.println("[VLAD] renderable"+ renderable);
//                modelRenderable = renderable;
//            })
//            .exceptionally(throwable -> {
//                Toast.makeText(ARActivity.this, "Can't load the model", Toast.LENGTH_SHORT).show();
//                return null;
//            });
//    }
//
//    private void onUpdateFrame(FrameTime frameTime) {
//
//        Frame frame = arFragment.getArSceneView().getArFrame();
//        // If there is no frame, just return.
//        if (frame == null) {
//            return;
//        }
//
//
//        //Making sure ARCore is tracking some feature points, makes the augmentation little stable.
//        if(frame.getCamera().getTrackingState()== TrackingState.TRACKING) {
////            System.out.println("VLAD: frame.getCamera().getPose() "+ frame.getCamera().getPose());
//
//            for (ARObject arObject : arObjects) {
//                if (arObject.placed) {
//                    continue;
//                }
//                System.out.println("VLAD: placing object "+ arObject);
//                System.out.println("VLAD: currPose:"+frame.getCamera().getPose());
//                Pose currPose = frame.getCamera().getPose();
//                float[] currTranslation = currPose.getTranslation();
//                for (int i = 0; i < currTranslation.length; i++) {
//                    System.out.println("VLAD " +i + " -> " + currTranslation[i]);
//                }
//
//                // 0 0 0 -> realCoordinates -> 44.20, 22.30
//                // doc 1                       45.20  23.30
//                Pose pos = currPose.compose(Pose.makeTranslation(0, 0, arObject.currDistance)).
//                        compose(Pose.makeRotation(0, 0, 45, 45));
//                System.out.println("VLAD: placing object at:"+pos);
//
//                Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(pos);
//                AnchorNode anchorNode = new AnchorNode(anchor);
//                anchorNode.setParent(arFragment.getArSceneView().getScene());
//
//                // Create the arrow node and add it to the anchor.
//                Node arrow = new Node();
//                arrow.setParent(anchorNode);
//                arrow.setRenderable(modelRenderable);
//                arObject.placed = true; //to place the arrow just once.
//            }
//
//        }
//
//    }
//
//
//    private void setUpPlane(){
//
//        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
//
////        arFragment.setOnTapArPlaneListener(((hitResult, plane, motionEvent) -> {
////            System.out.println("[VLAD] hitResult"+ hitResult);
////            System.out.println("[VLAD] plane"+ plane);
////            System.out.println("[VLAD] motionEvent"+ motionEvent);
////
////            Anchor anchor = hitResult.createAnchor();
////            System.out.println("[VLAD] anchor"+ anchor);
////
////            AnchorNode anchorNode = new AnchorNode(anchor);
////            System.out.println("[VLAD] anchorNode"+ anchorNode);
////
////            anchorNode.setParent(arFragment.getArSceneView().getScene());
////            System.out.println("[VLAD] arFragment.getArSceneView().getScene()"+ arFragment.getArSceneView().getScene());
////            System.out.println("[VLAD] anchorNode2"+ anchorNode);
////
////            createModel(anchorNode);
////        }));
//    }
//
//    private void createModel(AnchorNode anchorNode){
//        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
//        node.setParent(anchorNode);
//        node.setRenderable(modelRenderable);
//        node.select();
//        System.out.println("[VLAD] node"+ node);
//        System.out.println("[VLAD] arFragment"+ arFragment);
//    }
}