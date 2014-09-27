package ch.erni.lintilladancer.Controller;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * Created by ue65403 on 26.09.2014.
 */

public class SwipeLintillaMover extends LintillaMover implements GestureDetector.OnGestureListener{

    private GestureDetector mDetector;

    private final int swipe_Min_Distance = 200;
    private final int swipe_Min_Velocity = 1000;

    public SwipeLintillaMover(Context context){
        super(context);
        mDetector=new GestureDetector(context,this);
    }

    public boolean disptchMotionEvent(MotionEvent event){
        return mDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        final float xDistance = Math.abs(e1.getX() - e2.getX());
        final float yDistance = Math.abs(e1.getY() - e2.getY());

        velocityX = Math.abs(velocityX);
        velocityY = Math.abs(velocityY);
        boolean result = false;

        if(velocityX > this.swipe_Min_Velocity && xDistance > this.swipe_Min_Distance && velocityX > velocityY){
            if(e1.getX() > e2.getX()) { // right to left
                //moveLeft();
                Log.d("Lintilla movement", "Left");
            }else {
                //moveRight();
                Log.d("Lintilla movement", "Left");
            }
            result = true;
        }
        else if(velocityY > this.swipe_Min_Velocity && yDistance > this.swipe_Min_Distance && velocityY > velocityX){
            if(e1.getY() > e2.getY()) { // bottom to up
                //moveForward();
                Log.d("Lintilla movement", "Forward");
            }else {
                //moveBackward();
                Log.d("Lintilla movement", "Back");
            }
            result = true;
        }

        return result;
    }
    @Override
    public void onLongPress(MotionEvent e) {
        //stop();
        Log.d("Lintilla movement", "Stop");
    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {

        return false;
    }
    @Override
    public void onShowPress(MotionEvent e) {

    }
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

}
