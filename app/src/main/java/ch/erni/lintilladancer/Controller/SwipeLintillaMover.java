package ch.erni.lintilladancer.Controller;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Is class enables lintilla to be controlled by Swipe gestures. Therefore it implements the GestureDetector's
 * OnGestureListener interface. To biggest challenge here is to fine appropriate thresholds to translate
 * speed an direction of the gestures into lintilla commands.
 */

public class SwipeLintillaMover extends LintillaMover implements GestureDetector.OnGestureListener{

    //Storage member for the GestureDetector
    private GestureDetector mDetector;

    /*
    These thresholds for minimum swipe distance (pixels) and speed (pixels/second) have
    proven to function quite well to detect the desired gestures.
     */
    private final int swipe_Min_Distance = 200;
    private final int swipe_Min_Velocity = 1000;

    /**
     * The constructor passes the application context to the parent class and creates a
     * gesture detector object
     * @param context Application context
     */
    public SwipeLintillaMover(Context context){
        super(context);
        mDetector=new GestureDetector(context,this);
    }

    /**
     * This method dispatches a motion event from a fragment or activity to the gesture detecture
     * Should be called inside a OnTouchEvent listener from within a fragment or activity
     * @param event motion event
     * @return return value from the gesture detectors event handler
     */
    public boolean disptchMotionEvent(MotionEvent event){
        return mDetector.onTouchEvent(event);
    }


    /**
     * We're not interested in motion events. Always return false
     * @param e motion event from the dispatcher
     * @return constant false
     */
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    /**
     * A fling event ist the kind of swipe gesture we are interested in. The to events e1 and e2
     * code for the start point and the endpoint of the motion. Additionally there are to float variables
     * available, which contain the velocity in x and y direction. Depending on the direction and speed
     * of the gesture, we decide whether a movement for lintilla should be invoked.
     * @param e1 Start point of the gesture
     * @param e2 End point of the gesture
     * @param velocityX Velocity in x-direction
     * @param velocityY Velocity in y-direction
     * @return True if we detected a valid gesture, fals otherwise
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        //calculate the absolute swipe distance in x- and y-direction
        final float xDistance = Math.abs(e1.getX() - e2.getX());
        final float yDistance = Math.abs(e1.getY() - e2.getY());

        //get a handle on the absolute velocities
        velocityX = Math.abs(velocityX);
        velocityY = Math.abs(velocityY);
        //if not told differently, we did not detect a valid motion
        boolean result = false;

        /**
         * Given the velocity and distance exceeds the thresholds and the x-velocity is higher than
         * the velocity in y-direction, we invoke a forward or backward moving, depending on
         * the direction of the swipe gesture
         */
        if(velocityX > this.swipe_Min_Velocity && xDistance > this.swipe_Min_Distance && velocityX > velocityY){
            if(e1.getX() > e2.getX()) { // right to left
                moveLeft();
                Log.d("Lintilla movement", "Left");
            }else {
                moveRight();
                Log.d("Lintilla movement", "Left");
            }
            //now, we do have a valid gesture
            result = true;
        }
        /**
         * Given the velocity and distance exceeds the thresholds and the y-velocity is higher than
         * the velocity in x-direction, we invoke left or right turn, depending on
         * the direction of the swipe gesture
         */
        else if(velocityY > this.swipe_Min_Velocity && yDistance > this.swipe_Min_Distance && velocityY > velocityX){
            if(e1.getY() > e2.getY()) { // bottom to up
                moveForward();
                Log.d("Lintilla movement", "Forward");
            }else {
                moveBackward();
                Log.d("Lintilla movement", "Back");
            }
            //again, we have a valid gesture now
            result = true;
        }
        return result;
    }

    /**
     * A long press (>2000ms) stops lintilla
     * @param e motion event passed by the dispatcher
     */
    @Override
    public void onLongPress(MotionEvent e) {
        stop();
        Log.d("Lintilla movement", "Stop");
    }

    /**
     * We don't care about scroll events.
     * @param e1
     * @param e2
     * @param distanceX
     * @param distanceY
     * @return
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {

        return false;
    }

    /**
     * We don't care about show presses.
     * @param e
     */
    @Override
    public void onShowPress(MotionEvent e) {

    }

    /**
     * And neither we care about singleTapUp gestures.
     * @param e
     * @return
     */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

}
