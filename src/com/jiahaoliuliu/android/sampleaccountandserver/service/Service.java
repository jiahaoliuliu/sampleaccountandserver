package com.jiahaoliuliu.android.sampleaccountandserver.service;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.net.Uri;

import com.jiahaoliuliu.android.sampleaccountandserver.completionhandler.Callback;
import com.jiahaoliuliu.android.sampleaccountandserver.completionhandler.RequestDataCallback;
import com.jiahaoliuliu.android.sampleaccountandserver.completionhandler.RequestJSONCallback;
import com.jiahaoliuliu.android.sampleaccountandserver.service.HttpRequest.RequestMethod;

/**
 * This class is used to communicates with the remote server.
 */
public class Service {

    /**
     * The tag used for log.
     */
    private static final String LOG_TAG = Service.class.getSimpleName();

    /**
     * The base of the server url
     */
    private static final String BASE_URL = "http://www.google.com";

    /**
     * The user name used for the login, sign up and/or authentication.
     * The password is saved on the Account manager and it is associated
     * with the user name.
     */
    private String username;

    /**
     * The main constructor.
     * @param username The user name used for login, sign up and/or authentication
     *        This is necessary because the user name is used to get the hash for
     *        the authentication.
     */
    public Service(String username) {
        this.username = username;
    }

    /**
     * Method used for sign up.
     * @param username The user name used for sign up
     * @param invitationCode The invitation code used for sign up
     * @param password The password used for sign up
     * @param callback The callback to call when the sign up process has been finished
     */
    public void signUp(final String username,
            final String password, final Callback callback) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("email", username);
        parameters.put("password", password);

        final Uri finalUri = Uri.parse(BASE_URL + "/signup");

        HttpRequest httpRequest = new HttpRequest(finalUri, parameters, RequestMethod.RequestMethodPost);

        httpRequest.performRequestWithHandler(new RequestDataCallback() {

            @Override
            public void done(final byte[] data, final boolean error) {
                callback.done(error);
            }
        });
    }

    /**
     * Method used for login.
     * @param username The user name used for login
     * @param password The password used for login
     * @param jsonCallback The callback to call when the login process has been finished
     */
    public void logIn(final String username, final String password,
            final RequestJSONCallback jsonCallback) {

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("username_login", username);
        parameters.put("password", password);

        final Uri finalUri = Uri.parse(BASE_URL + "/login");

        HttpRequest httpRequest = new HttpRequest(finalUri, parameters, RequestMethod.RequestMethodPost);

        httpRequest.performRequestWithJSONHandler(new RequestJSONCallback() {

            @Override
            public void done(JSONObject jsonObject, boolean error) {
                jsonCallback.done(jsonObject, error);
            }
        });
    }

    // Getters & setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
