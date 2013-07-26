package com.jiahaoliuliu.android.sampleaccountandserver.completionhandler;

import org.json.JSONObject;

/**
 * This class is used to implement the error completion handler.
 */
public interface RequestJSONCallback {

    /**
     * Method called when the operation has been finished.
     * @param jsonObject The JSON object returned by the server
     * @param error Indication of if any error happened or not
     */
    void done(final JSONObject jsonObject, final boolean error);
}
