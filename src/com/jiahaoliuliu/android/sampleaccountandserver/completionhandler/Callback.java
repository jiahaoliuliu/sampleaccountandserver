package com.jiahaoliuliu.android.sampleaccountandserver.completionhandler;

/**
 * This class is used to implement the error completion handler.
 */
public interface Callback {

    /**
     * Method called when the operation has been finished.
     * @param error Indication of if any error happened or not
     */
    void done(final boolean error);
}
