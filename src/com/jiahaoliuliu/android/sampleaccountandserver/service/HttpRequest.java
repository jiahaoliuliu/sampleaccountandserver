package com.jiahaoliuliu.android.sampleaccountandserver.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.jiahaoliuliu.android.sampleaccountandserver.completionhandler.RequestDataCallback;
import com.jiahaoliuliu.android.sampleaccountandserver.completionhandler.RequestJSONCallback;
import com.jiahaoliuliu.android.sampleaccountandserver.util.SecurityUtils;

/**
 * The Request class used to establish REST communication with the server.
 */
public class HttpRequest {

    /**
     * The tag used in logs.
     */
    private static final String LOG_TAG = HttpRequest.class.getSimpleName();

    /**
     * Enumerated data which represents the REST methods used in the Request.
     */
    public enum RequestMethod {
        /**
         * Method get.
         */
        RequestMethodGet,
        /**
         * Method post.
         */
        RequestMethodPost,
        /**
         * Method put.
         */
        RequestMethodPut,
        /**
         * Method delete.
         */
        RequestMethodDelete
    }

    /**
     * The uri used to establish the REST communication.
     */
    private Uri uri;

    /**
     * The list of parameters to sent to the server.
     */
    private Map<String, String> parameters;

    /**
     * The request method established.
     */
    private final RequestMethod requestMethod;

    /**
     * The headers used.
     */
    private final Map<String, String> headerFields;

    /**
     * The instance of server Fetcher which is used to communicates with the server.
     */
    private ServerFetcher serverFetcher;

    /**
     * The instance of server Fetcher which is used to communicates with the server.
     */
    private final ExecutorServiceSingleton executorServiceSingleton;


    /**
     * The threadPool which contains all the threads used to communicate with the server.
     */
    private final ExecutorService threadPool;

    /**
     * The handler used to execute all the callbacks in the main thread.
     */
    private final Handler handler;

    /**
     * Method used to set the default header fields.
     * @return A map of default fields
     */
    private Map<String, String> defaultHeaderFields() {
        Map<String, String> headerFields = new HashMap<String, String>();
        /*
         * TODO Get locale language
         */
        String localelanguage = "en";
        headerFields.put("Accept-Language", localelanguage);
        return headerFields;
    }

    /**
     * The method used to parse URI and the parameters.
     * @param uri The URI of the server to connect
     * @param parameters The list of parameters to append
     * @return A URI with appended parameters
     */
    private Uri parseUriAndParameters(Uri uri, Map<String, String> parameters) {
        String listParameters = "";

        if (parameters != null) {
            for (String key : parameters.keySet()) {
                listParameters += key + "=" + parameters.get(key);
                listParameters += "&";
            }
        }

        /*
         * Remove the last character if it is "&"
         */
        if (listParameters.endsWith("&")) {
            listParameters = listParameters.substring(0, listParameters.length() - 1);
        }

        Uri result = Uri.parse(uri.toString() + listParameters);
        return result;
    }

    /**
     * Method used to set the authentication data in the request.
     * @param username The user name to set
     * @param password The password to set
     */
    public void setUsernamePassword(String username, String password) {
        /*
         * Encode the data
         */
        byte[] ascii = EncodingUtils.getAsciiBytes(username + password);
        /*
         * The code should not end with the EOL character
         */
        String authentication = SecurityUtils.base64Encode(ascii);

        /*
         * Add the authentication to the header
         */
        headerFields.put("Authorization", authentication);
    }

    /**
     * The main constructor of the class.
     * @param uri The URI of the server.
     * @param parameters The list of the parameters used.
     * @param requestMethod The request method
     */
    public HttpRequest(Uri uri, Map<String, String> parameters, RequestMethod requestMethod) {
        executorServiceSingleton = ExecutorServiceSingleton.instance();
        threadPool = executorServiceSingleton.getExecutorService();

        this.requestMethod = requestMethod;

        // If the request method is get, all the parameters is shown in the uri
        if (requestMethod == RequestMethod.RequestMethodGet) {
            this.uri = parseUriAndParameters(uri, parameters);
            Log.d("Request uri", uri.toString());
        } else {
            this.uri = uri;
            this.parameters = parameters;
        }

        headerFields = defaultHeaderFields();

        handler = new Handler();
    }

    /**
     * Method used to send request to the server, which returns an array of bytes.
     * @param requestDataCallback The callback to call when the communication finishes
     */
    public void performRequestWithHandler(final RequestDataCallback requestDataCallback) {
        serverFetcher = new ServerFetcher(requestMethod, uri, parameters, headerFields, new RequestDataCallback() {
            @Override
            public void done(final byte[] data, final boolean error) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(LOG_TAG, "Request to " + uri.toString() + "done");
                        requestDataCallback.done(data, error);
                    }
                });
            }
        });
        threadPool.execute(serverFetcher);
    }

    /**
     * Method used to send request to the server, which parse the content returned to a json object.
     * @param jsonHandler The callback to call when the communication finishes.
     */
    public void performRequestWithJSONHandler(final RequestJSONCallback jsonHandler) {
        /*
         * Add new header
         */
        headerFields.put("Accept", "application/json");

        serverFetcher = new ServerFetcher(requestMethod, uri,
                parameters, headerFields,
                new RequestDataCallback() {

                @Override
                public void done(final byte[] data, final boolean error) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!error && data != null && data.length > 0) {
                                try {
                                    // For latin languages, the codification must be ISO 8859-1
                                    //http://es.wikipedia.org/wiki/ISO_8859-1
                                    String jsonString = new String(data, "US-ASCII");
                                    Log.v(HttpRequest.LOG_TAG, jsonString);
                                    /*
                                     * Parse JSON data
                                     */
                                    JSONObject jsonObject = new JSONObject(jsonString);
                                    jsonHandler.done(jsonObject, error);
                                } catch (Exception exception) {
                                    Log.e(HttpRequest.LOG_TAG, exception.getLocalizedMessage(), exception);
                                    jsonHandler.done(null, true);
                                }
                            } else {
                                jsonHandler.done(null, error);
                            }
                        }
                    });
                }
            });
        threadPool.execute(serverFetcher);
    }

    /**
     * Informs that the request has been finished or not.
     * @return True if the request has been finished.
     *         False otherwise
     */
    public boolean hasFinished() {
        return (serverFetcher == null || !serverFetcher.isRunning);
    }

    /**
     * Method used to cancel the actual request.
     */
    public void cancelRequest() {
        if (serverFetcher != null && serverFetcher.isRunning) {
            serverFetcher.stopFetching();
        }

        serverFetcher = null;
    }

    public Uri getUri() {
        return uri;
    }

    /**
     * The runnable class used to connect with the server.
     */
    private class ServerFetcher implements Runnable {

        /**
         * The tag utilized for the log.
         */
        private static final String LOG_TAG = "ServerFetcher";

        /**
         * The registration time out before it launches the exception.
         */
        private static final int REGISTRATION_TIMEOUT = 3 * 1000;

        /**
         * The wait time out before it launches the exception.
         */
        private static final int WAIT_TIMEOUT = 30 * 1000;

        /**
         * The http client utilized.
         */
        private final HttpClient httpClient = new DefaultHttpClient();

        /**
         * The variable to record the running state (Yes/No).
         */
        private boolean isRunning = false;
        /**
         * The list of parameters of the HTTP client.
         */
        private final HttpParams params = httpClient.getParams();

        /**
         * The http response from the server.
         */
        private HttpResponse response;

        /**
         * The URI to connect.
         */
        private final Uri uri;

        /**
         * The list of parameters that includes in the http request.
         */
        private final Map<String, String> parameters;
        /**
         * The headers used.
         */
        private final Map<String, String> headerFields;

        /**
         * The callback to call when the operation finishes.
         */
        private final RequestDataCallback requestDataCallback;


        /**
         *  The final data obtained from the server.
         */
        private byte[] dataObtained;

        /**
         * The variable to save the final state of the operation.
         */
        private boolean error = false;

        /**
         * The main constructor.
         * @param requestMethod The REST method to perform
         * @param uri The Uri of the server to connect
         * @param parameters The list of parameters to be added to the HTTP request
         * @param headerFields The header of the HTTP request
         * @param requestDataCallback The Callback to call when the operation finishes
         */
        public ServerFetcher(RequestMethod requestMethod, Uri uri, Map<String,
                String> parameters, Map<String, String> headerFields,
                RequestDataCallback requestDataCallback) {
            this.uri = uri;
            this.parameters = parameters;
            this.headerFields = headerFields;

            this.requestDataCallback = requestDataCallback;
        }

        /**
         * The method which tells the actual state of the operation.
         * @return True if the server has not returned the response yet
         *         False otherwise
         */
        public boolean isRunning() {
            return isRunning;
        }

        /**
         * This methods stops the communication with the server.
         */
        public void stopFetching() {
            /*
             * TODO Cancel the http request
             */
        }

        @Override
        public void run() {
            Log.v(ServerFetcher.LOG_TAG, ServerFetcher.LOG_TAG + " running");
            isRunning = true;

            try {
                /*
                 * Set the connection parameters
                 */
                HttpConnectionParams.setConnectionTimeout(params, ServerFetcher.REGISTRATION_TIMEOUT);
                HttpConnectionParams.setSoTimeout(params, ServerFetcher.WAIT_TIMEOUT);
                ConnManagerParams.setTimeout(params, ServerFetcher.WAIT_TIMEOUT);

                /*
                 * Create ServerFetcher and prepare the data
                 */
                if (requestMethod == RequestMethod.RequestMethodGet) {
                    HttpGet httpGet = new HttpGet(uri.toString());

                    /*
                     * Add the headers
                     */
                    for (String key : headerFields.keySet()) {
                        httpGet.addHeader(key, headerFields.get(key));
                    }

                    /*
                     * Response from the Http Request
                     */
                    response = httpClient.execute(httpGet);

                } else if (requestMethod == RequestMethod.RequestMethodPost) {

                    HttpPost httpPost = new HttpPost(uri.toString());

                    /*
                     * Add the headers
                     */
                    for (String key : headerFields.keySet()) {
                        httpPost.addHeader(key, headerFields.get(key));
                    }

                    /*
                     * Add the values in the parameters
                     */
                    if (parameters != null) {
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                        for (String key : parameters.keySet()) {
                            nameValuePairs.add(new BasicNameValuePair(key, parameters.get(key)));
                        }
                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                    }

                    /*
                     * Response from the Http Request
                     */
                    response = httpClient.execute(httpPost);

                } else if (requestMethod == RequestMethod.RequestMethodPut) {
                    /*
                     * TODO Implement it
                     */
                    Log.w(ServerFetcher.LOG_TAG, "Executing Request method not implemented: put");
                } else if (requestMethod == RequestMethod.RequestMethodDelete) {
                    /*
                     * TODO Implement it
                     */
                    Log.w(ServerFetcher.LOG_TAG, "Executing Request method not implemented: delete");
                }

                StatusLine statusLine = response.getStatusLine();
                Log.v(ServerFetcher.LOG_TAG, "Status code" + statusLine.getStatusCode());
                /*
                 * Check the Http Request for success
                 */
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    response.getEntity().writeTo(out);
                    out.close();
                    dataObtained = out.toByteArray();
                    error = false;
                } else if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                    out.close();
                    response.getEntity().getContent().close();
                    error = true;
                } else {
                    /*
                     * Close the connection
                     */
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (Exception e) {
                Log.w(LOG_TAG, e.getLocalizedMessage(), e);
                error = true;
            } finally {
                isRunning = false;
                if (requestDataCallback != null) {
                    requestDataCallback.done(dataObtained, error);
                }
            }
        }
    }

    /**
     * The class which creates the threadPool as singleton.
     */
    private static final class ExecutorServiceSingleton {

        /**
         * The number of the threads which are running in parallel.
         */
        private static final int MAXIMUM_NUM_RUNNING_THREAD = 3;

        /**
         * A class to hold the singleton.
         */
        private static class SingletonHolder {
            /**
             * The instance of the class.
             */
            private static final ExecutorServiceSingleton INSTANCE = new ExecutorServiceSingleton();
        }

        /**
         * Creates the executor server as soft reference.
         */
        private SoftReference<ExecutorService> executorServiceReference = new SoftReference<ExecutorService>(
                createExecutorService());

        /**
         * The empty constructor of the class.
         */
        private ExecutorServiceSingleton() {
        };

        /**
         * The public method to return the instance.
         * @return A instance of the Singleton holder
         */
        public static ExecutorServiceSingleton instance() {
            return SingletonHolder.INSTANCE;
        }

        /**
         * Method used to get the executor service.
         * @return The executor service. Create it if it has not been created before
         */
        public ExecutorService getExecutorService() {
            ExecutorService executorService = executorServiceReference.get();

            if (executorService == null) {
                // (the reference was cleared)
                executorService = createExecutorService();
                executorServiceReference = new SoftReference<ExecutorService>(executorService);
            }

            return executorService;
        }

        /**
         * The method which creates a threadPool with limit number of threads.
         * Those threads will be re-utilized and they should exist while the application is running.
         * @return A threadPool with always same number of threads
         */
        private ExecutorService createExecutorService() {
            return Executors.newFixedThreadPool(ExecutorServiceSingleton.MAXIMUM_NUM_RUNNING_THREAD);
        }
    }
}
