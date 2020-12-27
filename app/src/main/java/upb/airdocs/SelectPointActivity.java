package upb.airdocs;


import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Toast;

import java.lang.reflect.Field;

public class SelectPointActivity extends Activity {
    private static final String LOG_TAG = "SelectPointActivity";
    ImageView imageView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_point_layout);

        imageView = (ImageView) findViewById(R.id.selected_map);
        imageView.setImageResource(MainActivity.selectedMapID);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    //Log.d(LOG_TAG,"X: "+String.valueOf(event.getRawX())+ " "+
                    //        "Y: "+String.valueOf(event.getRawY()));
                    // Get the coordinates of the touch point x, y
                    float x = event.getX();
                    float y = event.getY();
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
                    Log.d(LOG_TAG, "X: " + dstX + " Y: " + dstY);
                    if (dstX < 0 || dstY  < 0){
                        Toast.makeText(getBaseContext(),
                                "Touched outside the map.",
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getBaseContext(),
                                "X: " + dstX + " Y: " + dstY,
                                Toast.LENGTH_SHORT).show();
                        MainActivity.x = dstX;
                        MainActivity.y = dstY;
                    }
                }




                return true;
            }
        });

        final Button backButton = (Button) findViewById(R.id.back_from_select_point);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

}
