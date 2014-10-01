package ch.erni.lintilladancer.Controller;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

/**
 * This class faciliates the magnetometer and accelerometer to control lintilla. Yaw and pitch ar used to translate
 * the phone's orientation into movement commands. This approach seems to be some kind of a best
 * practice to gather the orientation. Whoever likes to play around with rotation matrices and
 * quaternions, be free to implement something more sophisticated.
 */
public class GyroLintillaMover extends LintillaMover implements SensorEventListener {

    //Members for sensor handling
    private SensorManager sManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private final String LOG_TAG = "Lintialla Movement";

    /*
    This variable is used to check whether a change in movement needs to be translated to a
    lintilla command
     */
    private boolean isMoving = false;

    /*
    These members are used to calculate the orientation out of the sensor data
     */
    private float R[] = new float[9];
    private float I[] = new float[9];
    private float mGravity[] = new float[3];
    private float mGeomagnetic[] = new float[3];
    private TextView v, v1;

    /*
    Pitch and yaw never exceed 360 degrees. By setting the initial values to 1000, we check
    whether it is the first measurement.
     */
    private double initialPitch = 1000;
    private double initialYaw = 1000;

    /**
     * The constructor takes the application's context, which is passed to the superclass. The
     * TextViews are used to display the current pitch and yaw values. This might be replaced by and interface
     *
     * @param context Application's context
     * @param v       Storage TextView for pitch
     * @param v2      Storage text view for yaw
     */
    public GyroLintillaMover(Context context, TextView v, TextView v2) {
        super(context);
        /*
         * Get handles on the sensor manager and the system's magnetic sensor and
         * accelerometer
         */
        sManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        magnetometer = sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        this.v = v;
        this.v1 = v2;
    }

    /**
     * To start the sensor data caputring, we register this class as listeners for altered sensor date
     * of the to sensors we uses to measure the orientation
     */
    public void startCapturing() {
        sManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * Stop measuring. We unregister this class as listener from the sensor manager. The stop lintilla
     * for safety purposes, set the moving boolean to false and set the variables for pitch and yaw
     * back to their initial values.
     */
    public void stopCapturing() {
        sManager.unregisterListener(this);
        //stop();
        isMoving = false;
        initialPitch = 1000;
        initialYaw = 1000;
    }

    @Override
    /**
     * This method is called whenever the sensor accuracy changes. Not interesting for us though.
     */
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        //Relax, uhh :)
    }

    /**
     * This method is called whenever the sensor data changes. We use the SensorEvent to calculate
     * the orientation. Depending on the preceding orientation and on whether lintilla is moving,
     * we send a REST-command to lintilla to act according to the phone's orientation.
     *
     * @param event Event that stores the changed sensor data
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        //if sensor is unreliable, return void
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            v.setText("Inaccurate");
            v1.setText("inaccurate");
            return;
        }
        /*
        Check which of the to sensors changed its values and get the altered sensor
        measurements. We always calculate the orientation based on the most recent values.
         */
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        /*
        Given we have valid sensor data of both sensors, we can calculate the orientation
         */
        if (mGravity != null && mGeomagnetic != null) {
            //get a rotation matrix based on the current data
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                //calculate the orientation
                SensorManager.getOrientation(R, orientation);
                //if  this is the first measurement, store the measurement to enabled delta value
                //calculation
                if (initialPitch > 360) {
                    initialPitch = convertRadians(orientation[1]);
                    initialYaw = convertRadians((orientation[0]));
                }
                //get the current pitch and yaw values
                double pitch = convertRadians(orientation[1]);
                double yaw = convertRadians(orientation[0]);
                /**
                 * A pitch value of +/- 15 degrees from the original orientation triggers a forward/bacward
                 * movement, but only if yaw is smaller than 20 degrees and lintilla isn't moving yet.
                 * The same thing holds true for yaw values: +/- 20 degrees trigger a turning, but only if the pitch values
                 * is smaller than 10 degrees and lintilla isn't moving yet. If lintilla is moving and the phone
                 * crosses a neutral position, the lintilla is stopped.
                 *
                 * Roll-values are not exploited since they trigger display orientation changes.
                 */
                //If conditions for left turn apply, send command to lintilla nd set moving=true
                if ((yaw - initialYaw) < -20 && Math.abs(pitch - initialPitch) < 10 && !isMoving) {
                    //moveLeft();
                    Log.d(LOG_TAG, "Left");
                    isMoving = true;
                    //If conditions for right turn apply, send command to lintilla nd set moving=true
                } else if ((yaw - initialYaw) > 20 && Math.abs(pitch - initialPitch) < 10 && !isMoving) {
                    //moveRight();
                    Log.d(LOG_TAG, "Right");
                    isMoving = true;
                    //If conditions for forward movement apply, send command to lintilla nd set moving=true
                } else if ((pitch - initialPitch) > 15 && Math.abs(yaw - initialYaw) < 5 && !isMoving) {
                    // moveForward();
                    Log.d(LOG_TAG, "Forward");
                    isMoving = true;
                    //If conditions for backward movement apply, send command to lintilla nd set moving=true
                } else if ((pitch - initialPitch) < -15 && Math.abs(yaw - initialYaw) < 5 && !isMoving) {
                    //moveBackward();
                    Log.d(LOG_TAG, "Backward");
                    isMoving = true;
                    //If conditions to stop lintilla apply, send command to lintilla nd set moving=true
                } else if (Math.abs(pitch - initialPitch) < 10 && Math.abs(yaw - initialYaw) < 5 && isMoving) {
                    //stop();
                    Log.d(LOG_TAG, "Stop");
                    isMoving = false;
                }
                //display orientation on the main UI
                //TODO interface would be nicer. I wanted it to keep simple for a first version though
                v.setText(Double.toString(Math.round(pitch - initialPitch)));
                v1.setText(Double.toString(Math.round(yaw - initialYaw)));
            }
        }
    }

    /**
     * Helper method to convert radians to degrees
     *
     * @param rad Input in radians
     * @return output in degrees
     */
    private double convertRadians(float rad) {
        return 180 * rad / Math.PI;
    }


}
