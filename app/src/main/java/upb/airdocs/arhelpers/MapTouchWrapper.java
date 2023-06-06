package upb.airdocs.arhelpers;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.function.Function;

public class MapTouchWrapper extends FrameLayout {

    private int touchSlop = 0;
    private Point down;
    private Function<Point, Object> listener = null;


    private void setup(Context context) {
        ViewConfiguration vc = ViewConfiguration.get(context);
        this.touchSlop = vc.getScaledTouchSlop();
    }

//    TODO:
    private void setup(Function<Point, Object> listener) {
        this.listener = listener;
    }


    private final double distance(Point p1, Point p2) {
        double xDiff = (double)(p1.x - p2.x);
        double yDiff = (double)(p1.y - p2.y);
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    public MapTouchWrapper(Context context) {
        super(context);
        setup(context);
    }

    public MapTouchWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (listener == null) {
            return false;
        }
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        Point tapped = new Point(x, y);

        switch (ev.getAction()){
        case MotionEvent.ACTION_DOWN:
            down = tapped;
            break;
        case MotionEvent.ACTION_MOVE:
            if (down != null && distance(down, tapped) >= touchSlop) {
                down = null;
                break;
            }
        case MotionEvent.ACTION_UP:
            if (down != null && distance(down, tapped) < touchSlop) {
                this.listener.apply(tapped);
                return true;
            }
        }
        return false;
    }
}
