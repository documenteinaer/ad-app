package upb.airdocs;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aractivity);
        arObjects = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ARObject arObject = new ARObject(false, -0.5f-i);
            arObjects.add(arObject);
        }
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        setupModel();
        setUpPlane();
    }

    private void setupModel() {
        ModelRenderable.builder()
            .setSource(this,
                    RenderableSource.builder().setSource(
                            this, Uri.parse(MODEL_URL),
                            RenderableSource.SourceType.GLB)
                    .setScale(0.2f)
                    .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                    .build())
            .setRegistryId(MODEL_URL)
            .build()
            .thenAccept(renderable -> {
                System.out.println("[VLAD] Here");
                System.out.println("[VLAD] renderable"+ renderable);
                modelRenderable = renderable;
            })
            .exceptionally(throwable -> {
                Toast.makeText(ARActivity.this, "Can't load the model", Toast.LENGTH_SHORT).show();
                return null;
            });
    }

    private void onUpdateFrame(FrameTime frameTime) {

        Frame frame = arFragment.getArSceneView().getArFrame();
        // If there is no frame, just return.
        if (frame == null) {
            return;
        }


        //Making sure ARCore is tracking some feature points, makes the augmentation little stable.
        if(frame.getCamera().getTrackingState()== TrackingState.TRACKING) {
//            System.out.println("VLAD: frame.getCamera().getPose() "+ frame.getCamera().getPose());

            for (ARObject arObject : arObjects) {
                if (arObject.placed) {
                    continue;
                }
                System.out.println("VLAD: placing object "+ arObject);
                System.out.println("VLAD: currPose:"+frame.getCamera().getPose());
                Pose currPose = frame.getCamera().getPose();
                float[] currTranslation = currPose.getTranslation();
                for (int i = 0; i < currTranslation.length; i++) {
                    System.out.println("VLAD " +i + " -> " + currTranslation[i]);
                }

                // 0 0 0 -> realCoordinates -> 44.20, 22.30
                // doc 1                       45.20  23.30
                Pose pos = currPose.compose(Pose.makeTranslation(0, 0, arObject.currDistance)).
                        compose(Pose.makeRotation(0, 0, 45, 45));
                System.out.println("VLAD: placing object at:"+pos);

                Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(pos);
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());

                // Create the arrow node and add it to the anchor.
                Node arrow = new Node();
                arrow.setParent(anchorNode);
                arrow.setRenderable(modelRenderable);
                arObject.placed = true; //to place the arrow just once.
            }

        }

    }


    private void setUpPlane(){

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

//        arFragment.setOnTapArPlaneListener(((hitResult, plane, motionEvent) -> {
//            System.out.println("[VLAD] hitResult"+ hitResult);
//            System.out.println("[VLAD] plane"+ plane);
//            System.out.println("[VLAD] motionEvent"+ motionEvent);
//
//            Anchor anchor = hitResult.createAnchor();
//            System.out.println("[VLAD] anchor"+ anchor);
//
//            AnchorNode anchorNode = new AnchorNode(anchor);
//            System.out.println("[VLAD] anchorNode"+ anchorNode);
//
//            anchorNode.setParent(arFragment.getArSceneView().getScene());
//            System.out.println("[VLAD] arFragment.getArSceneView().getScene()"+ arFragment.getArSceneView().getScene());
//            System.out.println("[VLAD] anchorNode2"+ anchorNode);
//
//            createModel(anchorNode);
//        }));
    }

    private void createModel(AnchorNode anchorNode){
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(modelRenderable);
        node.select();
        System.out.println("[VLAD] node"+ node);
        System.out.println("[VLAD] arFragment"+ arFragment);
    }
}