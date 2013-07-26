package com.jiahaoliuliu.android.sampleaccountandserver.completionhandler;

/**
 * This class is used to implement the error completion handler.
 */
public interface RequestDataCallback {

    /**
     * Method called when the operation has been finished.
     * @param data An array of bytes returned by the server
     * @param error Indication of if any error happened or not
     */
    void done(final byte[] data, final boolean error);
}
