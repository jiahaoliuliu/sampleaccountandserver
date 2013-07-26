package com.jiahaoliuliu.android.sampleaccountandserver.model;

import org.json.JSONObject;

import android.content.Context;

import com.jiahaoliuliu.android.sampleaccountandserver.completionhandler.Callback;
import com.jiahaoliuliu.android.sampleaccountandserver.completionhandler.RequestJSONCallback;
import com.jiahaoliuliu.android.sampleaccountandserver.exception.SessionException;
import com.jiahaoliuliu.android.sampleaccountandserver.service.Service;
import com.jiahaoliuliu.android.sampleaccountandserver.util.AccountUtils;
import com.jiahaoliuliu.android.sampleaccountandserver.util.Preferences;

/**
 * The Session class models a user's session. It is the intermediate level between Controllers and Service.
 */
public final class Session {

    private static final String LOG_TAG = Session.class.getSimpleName();

    private Service service;

    private Preferences preferences;

    private static Session currentSession = null;

    /**
     * The constructor of the session.
     * Because it is a singleton, there is not parameters for the constructors and it's private
     */
    private Session() {}

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() or the first access to
     * SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final Session INSTANCE = new Session();
    }

    // It is synchronized to avoid problems with multithreading
    // Once get, it must initialize the service and the preferences based on the context
    private static synchronized Session getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // To avoid clone problem
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * This method starts the sign up process.
     * @param context Context in which execution must be
     * @param username the name of the user
     * @param password User's password
     * @param callback Callback to call when process finishes
     */
    public static void signUp(final Context context, final String username,
    		final String password, final Callback callback) {

        Service service = new Service(username);

        service.signUp(username, password, new Callback() {
            @Override
            public void done(final boolean error) {
                // If there is not error, the method login will call callback
                if (!error) {
                    Session.logIn(context, username, password, callback);
                } else {
                    callback.done(error);
                }
            }
        });
    }

    /**
     * Allow the user to log in.
     * It checks the user name and the password against the server.
     * @param context    The context utilized
     * @param username   The name of the user
     * @param password   The password
     * @param callback   The callback to call when the server returns an answer
     */
    public static void logIn(final Context context, final String username,
            final String password, final Callback callback) {

        final Service service = new Service(username);
        // The preferences is here to avoid to be used statically
        final Preferences preferences = new Preferences(context);

        service.logIn(username, password, new RequestJSONCallback() {
            @Override
            public void done(JSONObject jsonObject, boolean error) {
                if (!error) {
                    /*
                     * Creates an instance of the current session.
                     */
                    Session session = Session.getInstance();
                    session.setService(service);
                    session.setPreferences(preferences);

                    /*
                     * Save the current session
                     */
                    Session.setCurrentSession(session);
                    Session.currentSession.saveAsCurrentSession(context, username, password);
                }

                callback.done(error);
            }
        });
    }

    /**
     * Save the session in a persistent way.
     * @param context  The context utilized to get the data
     * @param username The name of the verified user
     * @param password The password of the verified user
     */
    private void saveAsCurrentSession(Context context, String username, String password) {
        // Save the user name
        preferences.setUserName(username);

        // Save the password
        AccountUtils.setPasswordByUserName(context, username, password);

    }

    /**
     * Check if there is any saved session
     * @param context The context used
     * @return true if there is any saved session
     *         false otherwise
     */
    public static boolean isSavedSessionExists(Context context) {
        Preferences preferences = new Preferences(context);

        // Check if the user name exists in the Shared preferences
        String username = preferences.getUsername();
        if (username == null || username.equals(Preferences.DEFAULT_USERNAME)) {
            return false;
        }

        // Check if the user account exists
        if (AccountUtils.getUserAccount(context, username) == null) {
        	return false;
        }

        return true;
    }

    /**
     * Get the current session.
     * @param context The context utilized to retrieve the data
     * @return The current session
     * @throws SessionException if some data is missing
     */
    public static synchronized Session getCurrentSession(Context context) throws SessionException {
        if (Session.currentSession == null) {
            Session.currentSession = Session.sessionFromCurrentSession(context);
        }

        return Session.currentSession;
    }

    /**
     * Creates a new session from the data saved in the persistent data storage.
     * It might return null. Use the method existSavedSession to check if
     * the session saved exists
     * Precondition: The user name exists in the Shared preferences and the user
     *  account exists.
     * @param context The context utilized.
     * @return A new session if all the data are set
     * @throws SessionException if some data is missing
     */
    private static Session sessionFromCurrentSession(Context context) throws SessionException {
        Preferences preferences = new Preferences(context);

        // Get the user name
        String username = preferences.getUsername();
        if (username == null || username.equals(Preferences.DEFAULT_USERNAME)) {
            throw new SessionException("Error getting the session from the current one. " +
            "The previous session doesn't exsit");
        }

        Service newService = new Service(username);
        Session newSession = Session.getInstance();
        newSession.setService(newService);
        newSession.setPreferences(preferences);

        return newSession;
    }

    // Getters & setters

    private static synchronized void setCurrentSession(Session session) {
        Session.currentSession = session;
    }

    /*
    private Service getService() {
        return service;
    }*/

    /**
     * Set the service as the service utilized for the sessions
     * This is private to prevent other to set the service
     * The service won't be set until the user has logged in.
     * @param service The service to set.
     */
    private void setService(Service service) {
        this.service = service;
    }

    public Preferences getPreferences() {
       return preferences;
    }

    /**
     * Set the preferences as the preferences utilized for the session.
     * This is private to prevent other ot set the preferences from outside
     * The preferences won't be set until the user has logged in.
     * @param preferences The preferences to set.
     */
    private void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

}