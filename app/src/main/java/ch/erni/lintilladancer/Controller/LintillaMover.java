package ch.erni.lintilladancer.Controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * This class sends rest-commands to lintilla. It faciliates the easy-to-use framework retrofit to
 * perform http-requests.
 */
public class LintillaMover {

    /**
     * This interface describes how a call to lintilla's rest interface has to look like.
     * The paramater movement is part of the path and codes the movement. The additional query parameter
     * specifies the direction if available. The interface needs to be extended as soon as
     * the lintilla-interface faciliates additional commands
     */
    public interface LintillaMoverService {
        /**
         * This method has to be called to invoke a movement
         *
         * @param movement  movement as a string. currently: move/stop/test
         * @param direction direction of the movement, currently only for move: f,b,l,r
         * @param cb        optional callback, can be used to explot the http-response
         */
        @GET("/{movement}")
        void moveLintilla(@Path("movement") String movement, @Query("params") String direction, Callback<Response> cb);
    }

    private String url; //member to store the base url
    private String lintillaNumber; //member to store the lintilla number
    private LintillaMoverService service = null; //member to store a handle on the rest-service
    /*
    retrofit-api needs a restAdapter object
     */
    private RestAdapter restAdapter = null;

    //we need the application context to access the preferences
    protected Context context;

    /**
     * We don't actually have an http-response yet. However we can use it to check, whether
     * commands have successfully been sent to lintilla.
     */
    private final Callback callback = new Callback() {
        /**
         *Response from the http-call. Retrofit also features autmoatical JSON-convertion if
         * an appropriate model for the JSON-response is available.
         * @param o
         * @param response contains the http-response as an input-stream
         */
        @Override
        public void success(Object o, Response response) {
            Log.d("Lintilla call", "Command successfully sent");
        }

        /**
         * On errors inside the framework, en error message is created
         * @param retrofitError
         */
        @Override
        public void failure(RetrofitError retrofitError) {
            Log.d("Error: ", retrofitError.toString());
        }
    };

    /**
     * The constructor the application context as a single parameter. It is used to access the
     * application's preferences. The only other function of the constructor is to create a
     * rest-adapter object for lintilla
     *
     * @param context
     */
    public LintillaMover(Context context) {
        this.context = context;
        //get preferences to build up the base url
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.url = prefs.getString("pref_url", null);
        this.lintillaNumber = prefs.getString("pref_lintillaID", null);
        restAdapter = new RestAdapter.Builder()
                .setEndpoint(url)
                .build();

        service = restAdapter.create(LintillaMoverService.class);
    }

    /**
     * Invokes a forward movement for Lintilla through the rest-service
     */
    public void moveForward() {
        service.moveLintilla("move", "f", callback);
    }

    /**
     * Invokes a forward backward for Lintilla through the rest-service
     */
    public void moveBackward() {
        service.moveLintilla("move", "b", callback);
    }

    /**
     * Invokes a left turn for Lintilla through the rest-service
     */
    public void moveLeft() {
        service.moveLintilla("move", "l", callback);
    }

    /**
     * Invokes a right turn for Lintilla through the rest-service
     */
    public void moveRight() {
        service.moveLintilla("move", "r", callback);
    }

    /**
     * Stops Lintilla through the rest-service
     */
    public void stop() {
        service.moveLintilla("stop", "", callback);
    }

    /**
     * Invokes Lintilla's test sequence through the rest-service
     */
    public void dance() {
        service.moveLintilla("test", "", callback);
    }
}
