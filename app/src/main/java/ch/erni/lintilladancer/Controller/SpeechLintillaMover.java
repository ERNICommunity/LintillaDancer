package ch.erni.lintilladancer.Controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class uses Android's speech recognition API to listen for voice commands. The API can be used
 * by implementing the RecognitionListener interface and overriding the respective methos.
 * <p/>
 * It is utterly important to have only one instance of the speech recognizer active. That's
 * why we should create and destroy an instance of this class each time we want to listen for a
 * voice command
 */
public class SpeechLintillaMover extends LintillaMover implements RecognitionListener {

    /**
     * These are all the commands we are going to listen for. Surprsingly android recognizes commands
     * in different languages, so feel free to add whatever you can think of :) Lintilla is a dirty
     * girl!
     */
    private final String[] vocabularyForward = {"forward", "vorwärts", "move"};
    private final String[] vocabularyStop = {"stop", "halt", "rest", "platz", "sitz"};
    private final String[] vocabularyBack = {"back", "zurück"};
    private final String[] vocabularyRight = {"right", "rechts", "steuerboard"};
    private final String[] vocabularyLeft = {"left", "links", "backboard"};
    private final String[] vocabularyDance = {"dance", "shake", "test"};
    private String LOG_TAG = "VoiceRecognitionActivity";

    private ProgressBar rmsBar;

    //Members for the SpeechRecognizer
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;

    //We're going to build up this one. Creates the link between commands and the actual movement
    private HashMap<String, String> vocabulary = new HashMap<String, String>();

    /**
     * In the constructor we pass the application's context to the super class. Then we create a recognizer
     * object and build up the hashmap to link between voice commands and steering commands. Finally we set up
     * an intent to start the actual listening.
     *
     * @param context
     */
    public SpeechLintillaMover(Context context, ProgressBar rmsBar) {
        super(context);
        this.rmsBar = rmsBar;
        speech = SpeechRecognizer.createSpeechRecognizer(context);
        //register this class as listener for voice inputs
        speech.setRecognitionListener(this);
        this.context = context;
        this.buildHashMap();
        /*
        Create a new recognizer intent. Surprisingly the listener for US-English works quite well
        for german commands too. Haven't tried slovakian or philippino yet though...
         */
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                context.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    /**
     * Command to start the recognition. Call the recognizers method with our intent as
     * a parameter.
     */
    public void startListening() {
        this.speech.startListening(recognizerIntent);
    }

    /**
     * Stop listening. Very simple. Call the recognizer's stop listening methods.
     */
    public void stopListening() {
        this.speech.stopListening();
    }

    /**
     * I suggest always calling destroy after a successful recognition. It helps to avoid problems with
     * multiple recognizers, etc.
     */
    public void destroy() {
        this.speech.destroy();
    }

    @Override
    /**
     * We are not going to do anything during the beginning of speech..
     */
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");

    }

    @Override
    /**
     *...neither we du during received buffers..
     */
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    /**
     * ...or at the end of speech input
     */
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
    }

    /**
     * However, if an error has occurred, we tried to find out what the actual error was.
     *
     * @param errorCode Some integer, that codes for some error message (we don't get error codes^^)
     */
    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
    }

    @Override
    /**
     * So many handlers...
     */
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    /**
     * Partial results...whatever :)
     */
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    /**
     * Since there is an annoying beep anyway, we don't want to do more annoying things on
     * ready of speech
     */
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    /**
     * Huh! That's important. As soon as we get results, we convert all possible results
     * to an array list of results as strings. Then we check whether one of the recognitions
     * matches a key in our hashmap and invoke the appropriate movement, based on the
     * value in the hashmap.
     *
     * @param results a bundle with results of the recognition, passed by the recognizer
     */
    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        //convert results to an arrayList
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        //Loop through the results and check against the hasmap's keys
        for (String result : matches) {
            text += result + "\n";
            if (vocabulary.containsKey(result.toLowerCase())) {
                invokeMovement(vocabulary.get(result.toLowerCase()));
            }
        }
        Log.d("Speech recognition", text);
    }

    /**
     * We faciliate this methods to display the voice level in the UI.
     *
     * @param rmsdB
     */
    @Override
    public void onRmsChanged(float rmsdB) {
        rmsBar.setProgress((int) rmsdB);
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
    }

    /**
     * This method converts the recognizer's error codes to human readable error messages.
     * Code should be self explanatory.
     *
     * @param errorCode recognizer error code
     * @return appropriate error message
     */
    public String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    /**
     * We create the link between all commands that code for a specific movement here.
     */
    private void buildHashMap() {
        for (String keyword : vocabularyForward) {
            vocabulary.put(keyword, "f");
        }
        for (String keyword : vocabularyBack) {
            vocabulary.put(keyword, "b");
        }
        for (String keyword : vocabularyLeft) {
            vocabulary.put(keyword, "l");
        }
        for (String keyword : vocabularyRight) {
            vocabulary.put(keyword, "r");
        }
        for (String keyword : vocabularyDance) {
            vocabulary.put(keyword, "d");
        }
        for (String keyword : vocabularyStop) {
            vocabulary.put(keyword, "s");
        }
    }

    /**
     * This might be looked at as the most important method of this class. At least it invokes
     * a rest-command to be sent to lintilla, depending on the recognized voice command.
     * The values l,r,f,b,s,d are only used in this class to code the different movements.
     *
     * @param key Movement key, obtained from the hashmap that links between voice commands and the movement keys
     */
    private void invokeMovement(String key) {
        if (key == "l") {
            moveLeft();
            Log.d("Lintilla movement", "Left");
        } else if (key == "r") {
            moveRight();
            Log.d("Lintilla movement", "Right");
        } else if (key == "f") {
            moveForward();
            Log.d("Lintilla movement", "Forward");
        } else if (key == "b") {
            moveBackward();
            Log.d("Lintilla movement", "Back");
        } else if (key == "s") {
            stop();
            Log.d("Lintilla movement", "Stop");
        } else if (key == "d") {
            dance();
            Log.d("Lintilla movement", "Left");
        }
    }
}
