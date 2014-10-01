package ch.erni.lintilladancer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import ch.erni.lintilladancer.Controller.GyroLintillaMover;
import ch.erni.lintilladancer.Controller.LintillaMover;
import ch.erni.lintilladancer.Controller.SpeechLintillaMover;
import ch.erni.lintilladancer.Controller.SwipeLintillaMover;

/**
 * This is the central controller UI of the Lintilla Controller. Lintilla can be controlled by
 * six buttons to move the robot in all directions. Additionally Lintilla can be controlled by
 * tilting the phone, voice commands or swipe gestures. Only one of the three sophisticated controllers
 * can be used at a time together with the standard controller.
 */
public class MainFragment extends Fragment {

    private LintillaMover basicLintillaMover; //Execute basic lintilla movements
    private SpeechLintillaMover speech = null; //Send commands to lintilla by speech recognizer
    private GyroLintillaMover mover; //Use the orientation sensor to control lintilla
    private SwipeLintillaMover swipeMover = null; //Use swipe commands to control lintilla

    /*
    Variables to store and access the UI elements
     */
    private Switch gyroSwitch;
    private Switch speechSwitch;
    private Switch swipeSwitch;

    private Button startSpeech;
    private Button moveForward;
    private Button moveBackward;
    private Button moveLeft;
    private Button moveRight;
    private Button stop;
    private Button dance;

    private ProgressBar rmsBar;
    private TextView pitchView;
    private TextView yawView;

    /**
     * Nothing to do in the constructor
     */
    public MainFragment() {
    }


    /*public interface GooglePlaceService {
        @GET("/maps/api/place/nearbysearch/json")
        void getPlaces(@Query("key") String apiKey, @Query("location") String loc, @Query("types") String types, @Query("radius") Integer radius, Callback<Response> cb);
    }*/

    /**
     * Setup of UI flow and objects to steer lintilla
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        /**
         * Store all UI elements to access them easier for functionality assignment
         */
        gyroSwitch = (Switch) rootView.findViewById(R.id.gyroSteeringSwitch);
        speechSwitch = (Switch) rootView.findViewById(R.id.enableSpeechSteering);
        swipeSwitch = (Switch) rootView.findViewById(R.id.swipeSteeringSwitch);

        startSpeech = (Button) rootView.findViewById(R.id.triggerRecognitionButton);
        moveBackward = (Button) rootView.findViewById(R.id.moveBackButton);
        moveForward = (Button) rootView.findViewById(R.id.moveForwardButton);
        moveLeft = (Button) rootView.findViewById(R.id.moveLeftButton);
        moveRight = (Button) rootView.findViewById(R.id.moveRightButton);
        stop = (Button) rootView.findViewById(R.id.lintillaStopButton);
        dance = (Button) rootView.findViewById(R.id.danceButton);

        pitchView = (TextView) rootView.findViewById(R.id.pitchTextView);
        yawView = (TextView) rootView.findViewById(R.id.yawTextView);
        rmsBar = (ProgressBar) rootView.findViewById(R.id.rmsBar);

        /*
        A bascic lintilla mover is always available. Excpet for the swipe controller enabled, it can
        always be used to control lintilla
        */
        basicLintillaMover = new LintillaMover(getActivity().getApplicationContext());

        /**
         * As soon as the orientation controller is enabled, we want to disable the Swipe controller
         * and the speech controller. Additionally an orientation-controller object is created and
         * initialized. For this purpose we attache a checkedChanged Listener to the switch button.
         */
        gyroSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                //if the button was switched to checked
                if (isChecked) {
                    //only enable the controller if the other controllers are disabled
                    if (!speechSwitch.isChecked() && !swipeSwitch.isChecked()) {
                        //disable the switches for speech and swipe control
                        speechSwitch.setEnabled(false);
                        swipeSwitch.setEnabled(false);
                        /*
                        Create a new gyro mover object and start sensor-data capturing. The constructor
                        takes to TextView objects to write the actual coordinates
                         */
                        //TODO Rpelace by listener
                        mover = new GyroLintillaMover(getActivity().getApplicationContext(), pitchView, yawView);
                        mover.startCapturing();
                    } else {
                        //unprobable case, that the switch can be checked even if it is disabled
                        gyroSwitch.setChecked(false);
                    }
                    //if the orientation-controller is disabled, stop the capturing, empty the
                    //textviews and enable the other controllers
                } else {
                    mover.stopCapturing();
                    speechSwitch.setEnabled(true);
                    swipeSwitch.setEnabled(true);
                    pitchView.setText("");
                    yawView.setText("");
                }

            }
        });

        /**
         * As soon as the speech controller is enabled, we want to disable the Swipe controller
         * and the orientation controller. As soon as the speech controller is unchecked, we
         * want to enable the other controllers and stop the listening.
         * For this purpose we attach a checkedChanged Listener to the switch button.
         */
        speechSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                //if the switch button is checked
                if (isChecked) {
                    //only if the orientation-controller and the swipe-controller are disabled
                    //enable the button to start create a recognitioner object
                    if (!gyroSwitch.isChecked() && !swipeSwitch.isChecked()) {
                        gyroSwitch.setEnabled(false);
                        swipeSwitch.setEnabled(false);
                        startSpeech.setEnabled(true);
                        //unprobable case, that the switch can be checked even if it is disabled
                    } else {
                        speechSwitch.setChecked(false);
                    }
                /*
                If the controller had been running and has now be unchecked, enabled
                the other controllers and stop the listening if actually a SpeechRegognizer object
                was created
                 */
                } else {
                    gyroSwitch.setEnabled(true);
                    swipeSwitch.setEnabled(true);
                    startSpeech.setEnabled(false);
                    if (speech != null) {
                        speech.stopListening();
                    }
                }

            }
        });

        /*
        When somebody tries to enable the swipe-controller, we want to make sure that neither the
        the orientation-controller nor the speech-controller are enabled. Given that fact, we disable
        all basic steering buttons except the stop button (we don't want lintilla to fall of cliffs).
        Also, we create a SwipeLintillaMover object. Everything is triggered by a click on the
        switch-button to enable or disable swipe-control.
         */
        swipeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                //if the switch has been set to enabled
                if (isChecked) {
                    //and neither the gyro-controller nor the speech-controller are active
                    if (!gyroSwitch.isChecked() && !speechSwitch.isChecked()) {
                        //create a SwipeLintilaMover
                        swipeMover = new SwipeLintillaMover(getActivity().getApplicationContext());
                        //disable all unnecessary and annyoing buttons for happy swiping
                        gyroSwitch.setEnabled(false);
                        speechSwitch.setEnabled(false);
                        moveRight.setEnabled(false);
                        moveForward.setEnabled(false);
                        moveLeft.setEnabled(false);
                        moveBackward.setEnabled(false);
                        dance.setEnabled(false);
                    } else {
                        swipeSwitch.setChecked(false);
                    }
                /*
                If the swipe-controller has been disabled, enable all deactivated buttons and
                delete the pointer to the SwipeLintillaMover
                 */
                } else {
                    swipeMover = null;
                    gyroSwitch.setEnabled(true);
                    speechSwitch.setEnabled(true);
                    moveRight.setEnabled(true);
                    moveForward.setEnabled(true);
                    moveLeft.setEnabled(true);
                    moveBackward.setEnabled(true);
                    dance.setEnabled(true);
                }

            }
        });

        /*
        If the button to send a voice command is clicked, we create a voice listener object.
        Since Android has issues with continues listening, for stability we create and destroy
        the SpeechLintillaMover object with each command.
         */
        startSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If there already exists and object, destroy. Otherwise we get problems since
                //only one voice recognizer may exist
                if (speech != null) {
                    speech.destroy();
                }
                //create the object and start listening
                speech = new SpeechLintillaMover(getActivity().getApplicationContext(), rmsBar);
                speech.startListening();
            }
        });

        /*
        Button to move lintilla forward has been clicked->send move command to lintilla
         */
        moveForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //basicLintillaMover.moveForward();
            }
        });

        /*
        Button to move lintilla backwards has been clicked->send move command to lintilla
         */
        moveBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //basicLintillaMover.moveBackward();
            }
        });

        /*
        Button to move lintilla to the left has been clicked->send move command to lintilla
         */
        moveLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //basicLintillaMover.moveLeft();
            }
        });

        /*
        Button to move lintilla to the right has been clicked->send dance command to lintilla
         */
        moveRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //basicLintillaMover.moveRight();
            }
        });

        /*
        Button to start lintilla's test sequence has been clicked->send move command to lintilla
         */
        dance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //basicLintillaMover.dance();
            }
        });

        /*
        Button to stop lintilla has been clicked->send stop command to lintilla
         */
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //basicLintillaMover.stop();
            }
        });

        /*
        For the swipe-controller to work, we have to listen for touch events. This listener is
        only used if the swipe-controller is enabled
         */
        rootView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                //if there really is a SwipeLintillaMover object, dispatch the motion event to the
                //controller to handle it
                if (swipeMover != null) {
                    swipeMover.disptchMotionEvent(event);
                }
                return true;
            }
        });

        /*
        Callback callback = new Callback() {
            @Override
            public void success(Object o, Response response) {
                try{
                    Log.d("Response: ", convertInputStreamToString(response.getBody().in()));
                }catch (IOException ioe){

                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d("Error:", "Not working");
                Log.d("Eroor: ", retrofitError.toString());
            }
        };
        */

        /*RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://maps.googleapis.com")
                .build();


        GooglePlaceService service = restAdapter.create(GooglePlaceService.class);
        service.getPlaces("AIzaSyC0DFg9ARJTr3I_52lXEk_q58jzO-fb_S0", "47.414892,8.55","bar",10000, callback);*/

        return rootView;
    }

    /*
     * It is important to stop all controlling activity and measuring in the onPause methods,
     * which is called when the application is paused. Especially the speech controller can crash the
     * application
     */
    public void onPause() {
        //call the parent's onPause
        super.onPause();
        //if the gyro switch is enabled, stop capturing and empty the text views
        if (gyroSwitch.isChecked()) {
            mover.stopCapturing();
            gyroSwitch.setChecked(false);
            pitchView.setText("");
            yawView.setText("");
        }
        //if the speech controller is enabled and there exists and active object, destroy it
        if (speechSwitch.isChecked()) {
            speechSwitch.setChecked(false);
            if (speech != null) {
                speech.destroy();
            }
        }
        //clear a possible SwipeLintillaMover object
        if (swipeSwitch.isChecked()) {
            swipeMover = null;
        }
    }

    /*
    On resume is easier to handle. Call the parent's onResume and we're done
     */
    public void onResume() {
        super.onResume();
    }

    /*
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }*/
}