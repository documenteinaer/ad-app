package upb.airdocs;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.android.gestures.AndroidGesturesManager;
import com.mapbox.android.gestures.Constants;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.android.gestures.RotateGestureDetector;
import com.mapbox.android.gestures.StandardGestureDetector;
import com.mapbox.android.gestures.StandardScaleGestureDetector;
import com.mapbox.android.gestures.*;

import java.util.HashSet;
import java.util.Set;

public class SelectPointActivity extends Activity {
    private static final String LOG_TAG = "SelectPointActivity";
    private ImageView imageView;
    private Button backButton;
    private ScaleGestureDetector scaleGestureDetector;
    private AndroidGesturesManager gesturesManager;
    private float mScaleFactor = 1.0f;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_point_layout);

        setupGesturesManager();
        setupViews();
    }

    private void setupGesturesManager(){
        gesturesManager = new AndroidGesturesManager(this);
        gesturesManager.setStandardGestureListener(new StandardGestureListener());
        gesturesManager.setStandardScaleGestureListener(new ScaleGestureDetector());
        gesturesManager.setMoveGestureListener(new MoveGestureDetector());
        gesturesManager.getMoveGestureDetector().setEnabled(true);
    }

    private void setupViews(){
        imageView = (ImageView) findViewById(R.id.selected_map);
        imageView.setImageResource(MainActivity.selectedMapID);
        backButton = (Button) findViewById(R.id.back_from_select_point);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gesturesManager.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private class StandardGestureListener extends StandardGestureDetector.SimpleStandardOnGestureListener{
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(LOG_TAG, "Simple tap gesture");


            int coord[] = new int[2];
            imageView.getLocationOnScreen(coord);


            Log.d(LOG_TAG, "img x: "+coord[0]+" img y: "+coord[1]);

            Log.d(LOG_TAG, "event x: "+e.getX()+" event y: "+e.getY());

            float x = (e.getX() - coord[0])/mScaleFactor;
            float y = (e.getY() - coord[1])/mScaleFactor;
            Log.d(LOG_TAG, "adjusted x: "+x+" adjusted y: "+y);

            // The coordinates of the target point
            float dst[] = new float[2];
            // Get the matrix of ImageView
            Matrix imageMatrix = imageView.getImageMatrix();
            // Create an inverse matrix
            Matrix inverseMatrix = new Matrix();
            // Inverse, the inverse matrix is ​​assigned
            imageMatrix.invert(inverseMatrix);
            // Get the value of the target point dst through the inverse matrix mapping
            inverseMatrix.mapPoints(dst, new float[]{x, y});
            float dstX = dst[0];
            float dstY = dst[1];
            Log.d(LOG_TAG, "Image X: " + dstX + " Y: " + dstY);
            drawPoint(dstX, dstY);
            TextView textView = findViewById(R.id.coordinates);
            textView.setText("X: " + dstX + " Y: " + dstY);
            if (dstX < 0 || dstY < 0) {
                Toast.makeText(getBaseContext(),
                        "Touched outside the map.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(),
                        "X: " + dstX + " Y: " + dstY,
                        Toast.LENGTH_SHORT).show();
                MainActivity.x = dstX;
                MainActivity.y = dstY;
            }

            return super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleGestureDetector extends StandardScaleGestureDetector.SimpleStandardOnScaleGestureListener {
        private static final float ROTATION_THRESHOLD_WHEN_SCALING = 30;

        @Override
        public boolean onScaleBegin(StandardScaleGestureDetector detector) {
            gesturesManager.getMoveGestureDetector().setEnabled(false); // this interrupts a gesture as well
            return true;
        }

        @Override
        public boolean onScale(StandardScaleGestureDetector detector) {
            Log.d(LOG_TAG, "scale gesture");
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
            imageView.setScaleX(mScaleFactor);
            imageView.setScaleY(mScaleFactor);
            Log.d(LOG_TAG, "scale factor: "+mScaleFactor);
            return super.onScale(detector);
        }

        @Override
        public void onScaleEnd(StandardScaleGestureDetector detector, float velocityX, float velocityY) {
            gesturesManager.getMoveGestureDetector().setEnabled(true);
        }
    }

    private class MoveGestureDetector extends com.mapbox.android.gestures.MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(com.mapbox.android.gestures.MoveGestureDetector detector, float distanceX, float distanceY) {
            if (distanceX != 0 || distanceY != 0) {
                Log.d(LOG_TAG, "moving gesture: x: "+distanceX+" y: "+distanceY);
                imageView.setTranslationX(imageView.getTranslationX() - distanceX);
                imageView.setTranslationY(imageView.getTranslationY() - distanceY);
                //moveX -= distanceX;
                //moveY -= distanceY;
                //Log.d(LOG_TAG, "moveX: "+moveX+" moveY: "+moveY);
            }
            return super.onMove(detector, distanceX, distanceY);
        }
    }

    private void drawPoint(float coordinateX, float coordinateY) {
        Bitmap myBitmap = BitmapFactory.decodeResource(getResources(),MainActivity.selectedMapID);

        Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(tempBitmap);

        tempCanvas.drawBitmap(myBitmap, 0, 0, null);

        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(5);
        tempCanvas.drawCircle(coordinateX, coordinateY, 15, paint);

        imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
    }

}
